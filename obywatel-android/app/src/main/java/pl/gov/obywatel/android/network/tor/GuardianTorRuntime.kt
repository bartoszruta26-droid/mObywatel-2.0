// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.tor

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.IBinder
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.torproject.jni.TorService

/**
 * Owns the embedded Guardian Project TorService while this runtime is started.
 *
 * start() is asynchronous. Observe [state] until READY before creating requests.
 * stop() unbinds the service; TorService shuts down when its bound lifetime ends.
 */
class GuardianTorRuntime(context: Context) : TorRuntime {
    private val appContext = context.applicationContext
    @Volatile private var currentState: TorState = TorState.STOPPED
    @Volatile private var torService: TorService? = null
    @Volatile private var socksPort: Int = -1
    @Volatile private var socksPorts: List<Int> = emptyList()
    @Volatile private var bound = false
    @Volatile private var receiverRegistered = false
    @Volatile private var localErrorReceiverRegistered = false
    @Volatile private var circuitEstablished = false
    @Volatile private var lastFailureMessage: String? = null
    @Volatile private var lastServiceStatus: String? = null
    @Volatile private var statusSequence: Long = 0L
    @Volatile private var serviceConnectionLost = false

    override val state: TorState
        get() = currentState

    /** Detailed reason delivered by Guardian's ACTION_ERROR broadcast, if available. */
    override fun failureMessage(): String? = lastFailureMessage

    // TorService emits ACTION_ERROR through LocalBroadcastManager as well as the
    // process broadcast. Subscribe to the local channel explicitly: it is the
    // authoritative same-process path for the original native startup exception.
    private val localErrorReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != TorService.ACTION_ERROR) return
            val sourcePackage = intent.getStringExtra(TorService.EXTRA_SERVICE_PACKAGE_NAME)
            if (sourcePackage != null && sourcePackage != appContext.packageName) return
            lastFailureMessage = intent.getStringExtra(Intent.EXTRA_TEXT)
                ?.trim()?.takeIf { it.isNotEmpty() }
                ?: "Guardian Tor zgłosił ACTION_ERROR bez opisu."
            socksPort = -1
            socksPorts = emptyList()
            circuitEstablished = false
            currentState = TorState.FAILED
        }
    }

    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != TorService.ACTION_STATUS) return
            val sourcePackage = intent.getStringExtra(TorService.EXTRA_SERVICE_PACKAGE_NAME)
            if (sourcePackage != null && sourcePackage != appContext.packageName) return
            val status = intent.getStringExtra(TorService.EXTRA_STATUS)
            lastServiceStatus = status
            statusSequence++
            when (status) {
                TorService.STATUS_STARTING -> currentState = TorState.BOOTSTRAPPING
                TorService.STATUS_ON -> {
                    circuitEstablished = true
                    publishReady()
                }
                TorService.STATUS_STOPPING, TorService.STATUS_OFF -> {
                    socksPort = -1
                    socksPorts = emptyList()
                    circuitEstablished = false
                    if (currentState != TorState.STOPPING) {
                        if (lastFailureMessage.isNullOrBlank()) {
                            lastFailureMessage = "Guardian Tor zakończył pracę ze statusem " +
                                intent.getStringExtra(TorService.EXTRA_STATUS) + "."
                        }
                        currentState = TorState.FAILED
                    }
                }
            }
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as? TorService.LocalBinder
            val service = localBinder?.service
            if (service == null) {
                lastFailureMessage = "Guardian Tor: binder usługi nie udostępnił TorService.LocalBinder."
                currentState = TorState.FAILED
                return
            }
            torService = service
            serviceConnectionLost = false
            val bootstrap = runCatching { service.getInfo("status/bootstrap-phase") }.getOrNull().orEmpty()
            circuitEstablished = circuitEstablished || bootstrap.contains("PROGRESS=100") || bootstrap.contains("TAG=done")
            publishReady()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceConnectionLost = true
            torService = null
            socksPort = -1
            socksPorts = emptyList()
            lastFailureMessage = "Guardian Tor: połączenie z usługą zostało zerwane."
            currentState = TorState.FAILED
        }

        override fun onBindingDied(name: ComponentName?) {
            serviceConnectionLost = true
            torService = null
            socksPort = -1
            socksPorts = emptyList()
            lastFailureMessage = "Guardian Tor: powiązanie z usługą uległo awarii (onBindingDied)."
            currentState = TorState.FAILED
        }

        override fun onNullBinding(name: ComponentName?) {
            serviceConnectionLost = true
            torService = null
            socksPort = -1
            socksPorts = emptyList()
            lastFailureMessage = "Guardian Tor: system zwrócił puste powiązanie usługi (onNullBinding)."
            currentState = TorState.FAILED
        }
    }

    @Synchronized
    override fun start() {
        if (currentState == TorState.READY || currentState == TorState.STARTING ||
            currentState == TorState.BOOTSTRAPPING
        ) return
        val restartFailedService = currentState == TorState.FAILED
        currentState = TorState.STARTING
        lastFailureMessage = null
        circuitEstablished = false
        try {
            configureIsolatedSocksListeners()
            val filter = IntentFilter().apply {
                addAction(TorService.ACTION_STATUS)
            }
            if (!receiverRegistered) {
                ContextCompat.registerReceiver(
                    appContext,
                    statusReceiver,
                    filter,
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )
                receiverRegistered = true
            }
            if (!localErrorReceiverRegistered) {
                LocalBroadcastManager.getInstance(appContext).registerReceiver(
                    localErrorReceiver,
                    IntentFilter(TorService.ACTION_ERROR)
                )
                localErrorReceiverRegistered = true
            }
            prepareFailedServiceForRestart(restartFailedService)
            lastFailureMessage = null
            currentState = TorState.STARTING
            serviceConnectionLost = false
            bound = appContext.bindService(
                Intent(appContext, TorService::class.java),
                connection,
                Context.BIND_AUTO_CREATE
            )
            if (!bound) {
                lastFailureMessage = "Android odmówił powiązania z wbudowaną usługą Guardian Tor (bindService=false)."
                currentState = TorState.FAILED
                unregisterReceiverIfNeeded()
            }
        } catch (failure: Exception) {
            lastFailureMessage = failure.message ?: failure.javaClass.simpleName
            currentState = TorState.FAILED
            unregisterReceiverIfNeeded()
            throw IllegalStateException("Unable to bind embedded Tor service.", failure)
        }
    }

    @Synchronized
    override fun stop() {
        if (currentState == TorState.STOPPED) return
        currentState = TorState.STOPPING
        socksPort = -1
        socksPorts = emptyList()
        circuitEstablished = false
        torService = null
        if (bound) {
            runCatching { appContext.unbindService(connection) }
            bound = false
        }
        unregisterReceiverIfNeeded()
        currentState = TorState.STOPPED
    }

    override fun proxyEndpoint(): TorProxyEndpoint? = proxyEndpoints().firstOrNull()

    override fun proxyEndpoints(): List<TorProxyEndpoint> {
        if (currentState != TorState.READY) return emptyList()
        return socksPorts.mapNotNull { port ->
            runCatching { TorProxyEndpoint("127.0.0.1", port) }.getOrNull()
        }.distinctBy { it.port }
    }

    /**
     * Extra listeners explicitly configured with Tor stream-isolation flags.
     * The primary SOCKS listener remains available as a compatibility/fallback endpoint,
     * but is excluded from round-robin when isolated listeners are active.
     */
    fun isolatedProxyEndpoints(): List<TorProxyEndpoint> =
        proxyEndpoints().filter { it.port in 19051..19080 }

    @Synchronized
    private fun publishReady() {
        if (!circuitEstablished) return
        val port = torService?.socksPort ?: return
        if (port in 1..65535 && currentState != TorState.STOPPING) {
            socksPort = port
            val reported = runCatching { torService?.getInfo("net/listeners/socks") }.getOrNull()
            val parsedPorts = reported.orEmpty().split(Regex("\\s+"))
                .mapNotNull { token -> token.trim().trim('"').substringAfterLast(':', "").toIntOrNull() }
                .filter { it in 1..65535 }
            val candidates = (parsedPorts + port).distinct()
            // GETINFO can race with listener startup. Never trust TorService.socksPort
            // without proving that the corresponding loopback socket is accepting TCP.
            socksPorts = candidates.filter { candidate ->
                runCatching {
                    java.net.Socket().use { socket ->
                        socket.connect(java.net.InetSocketAddress("127.0.0.1", candidate), 500)
                        true
                    }
                }.getOrDefault(false)
            }
            val verifiedPrimary = socksPorts.firstOrNull() ?: return
            socksPort = verifiedPrimary
            currentState = TorState.READY
        }
    }

    private fun configureIsolatedSocksListeners() {
        // TorService chooses its primary SOCKS port dynamically. This is important
        // when Orbot already owns 9050: Guardian must not try to bind the same port.
        val availablePorts = (19051..19080).filter { candidate ->
            runCatching { java.net.ServerSocket(candidate).use { true } }.getOrDefault(false)
        }.take(4)
        val torrc = TorService.getTorrc(appContext)
        val existing = if (torrc.exists()) torrc.readText() else ""
        val filtered = existing.lineSequence().filterNot { line ->
            val port = Regex("""^\s*SocksPort\s+127[.]0[.]0[.]1:(\d+)""").find(line)?.groupValues?.getOrNull(1)?.toIntOrNull()
            (port != null && port in 19051..19080) || line.trim().startsWith("MaxCircuitDirtiness ")
        }.toList()
        val listeners = availablePorts.map { port ->
            "SocksPort 127.0.0.1:$port IsolateClientAddr IsolateDestAddr IsolateDestPort"
        }
        val region = TorExitRegionSettings.get(appContext)
        val exitPreference = when (region) {
            TorExitRegion.POLAND -> listOf("ExitNodes {pl}", "StrictNodes 0")
            TorExitRegion.EUROPE -> listOf(
                "ExitNodes " + region.torCountryCodes.joinToString(",") { "{$it}" },
                "StrictNodes 0"
            )
            TorExitRegion.WORLD -> emptyList()
        }
        torrc.writeText(
            (filtered + "MaxCircuitDirtiness 480" + exitPreference + listeners)
                .joinToString("\n", postfix = "\n")
        )
    }

    private fun unregisterReceiverIfNeeded() {
        if (receiverRegistered) {
            runCatching { appContext.unregisterReceiver(statusReceiver) }
            receiverRegistered = false
        }
        if (localErrorReceiverRegistered) {
            runCatching { LocalBroadcastManager.getInstance(appContext).unregisterReceiver(localErrorReceiver) }
            localErrorReceiverRegistered = false
        }
    }

    /**
     * TorService calls stopSelf() when its native Tor thread exits. Because this
     * runtime is bound, that does not destroy the failed service instance. A plain
     * second bind would reconnect to the same dead instance without starting Tor
     * again. Release that binding and wait for the service's OFF lifecycle event
     * before creating a fresh service instance.
     */
    private fun prepareFailedServiceForRestart(wasFailed: Boolean) {
        if (!wasFailed || !bound) return
        val previousSequence = statusSequence
        val awaitServiceShutdown = !serviceConnectionLost && lastServiceStatus != TorService.STATUS_OFF
        runCatching { appContext.unbindService(connection) }
        bound = false
        torService = null
        socksPort = -1
        socksPorts = emptyList()
        circuitEstablished = false
        runCatching { appContext.stopService(Intent(appContext, TorService::class.java)) }

        if (awaitServiceShutdown) {
            val deadline = System.currentTimeMillis() + 5_000L
            while (statusSequence <= previousSequence || lastServiceStatus != TorService.STATUS_OFF) {
                if (System.currentTimeMillis() >= deadline) {
                    throw IllegalStateException(
                        "Guardian Tor: zwolniono nieudane powiązanie, ale usługa nie potwierdziła stanu OFF; ponowne uruchomienie wstrzymano."
                    )
                }
                try {
                    Thread.sleep(50L)
                } catch (interrupted: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw IllegalStateException("Oczekiwanie na zamknięcie usługi Guardian Tor zostało przerwane.", interrupted)
                }
            }
        }
        lastServiceStatus = null
    }
}
