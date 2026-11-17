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
    @Volatile private var circuitEstablished = false

    override val state: TorState
        get() = currentState

    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != TorService.ACTION_STATUS) return
            when (intent.getStringExtra(TorService.EXTRA_STATUS)) {
                TorService.STATUS_STARTING -> currentState = TorState.BOOTSTRAPPING
                TorService.STATUS_ON -> {
                    circuitEstablished = true
                    publishReady()
                }
                TorService.STATUS_STOPPING, TorService.STATUS_OFF -> {
                    socksPort = -1
                    socksPorts = emptyList()
                    circuitEstablished = false
                    if (currentState != TorState.STOPPING) currentState = TorState.FAILED
                }
            }
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as? TorService.LocalBinder
            val service = localBinder?.service
            if (service == null) {
                currentState = TorState.FAILED
                return
            }
            torService = service
            val bootstrap = runCatching { service.getInfo("status/bootstrap-phase") }.getOrNull().orEmpty()
            circuitEstablished = bootstrap.contains("PROGRESS=100") || bootstrap.contains("TAG=done")
            // STATUS_ON is emitted after the first Tor circuit is established.
            // If the status broadcast raced with binding, the port is still checked
            // here; READY is published only when the service reports a valid SOCKS port.
            publishReady()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            torService = null
            socksPort = -1
            socksPorts = emptyList()
            currentState = TorState.FAILED
        }

        override fun onBindingDied(name: ComponentName?) {
            torService = null
            socksPort = -1
            socksPorts = emptyList()
            currentState = TorState.FAILED
        }

        override fun onNullBinding(name: ComponentName?) {
            torService = null
            socksPort = -1
            currentState = TorState.FAILED
        }
    }

    @Synchronized
    override fun start() {
        if (currentState == TorState.READY || currentState == TorState.STARTING ||
            currentState == TorState.BOOTSTRAPPING
        ) return
        currentState = TorState.STARTING
        circuitEstablished = false
        try {
            configureIsolatedSocksListeners()
            ContextCompat.registerReceiver(
                appContext,
                statusReceiver,
                IntentFilter(TorService.ACTION_STATUS),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            receiverRegistered = true
            bound = appContext.bindService(
                Intent(appContext, TorService::class.java),
                connection,
                Context.BIND_AUTO_CREATE
            )
            if (!bound) {
                currentState = TorState.FAILED
                unregisterReceiverIfNeeded()
            }
        } catch (failure: Exception) {
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
            socksPorts = candidates.filter { candidate ->
                candidate == port || runCatching {
                    java.net.Socket().use { socket ->
                        socket.connect(java.net.InetSocketAddress("127.0.0.1", candidate), 250)
                        true
                    }
                }.getOrDefault(false)
            }
            currentState = TorState.READY
        }
    }

    private fun configureIsolatedSocksListeners() {
        // Reserve a small deterministic range, but skip ports already used by other local services.
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
        torrc.writeText((filtered + "MaxCircuitDirtiness 480" + listeners).joinToString("\n", postfix = "\n"))
    }
    private fun unregisterReceiverIfNeeded() {
        if (receiverRegistered) {
            runCatching { appContext.unregisterReceiver(statusReceiver) }
            receiverRegistered = false
        }
    }
}
