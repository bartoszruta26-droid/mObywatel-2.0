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
            // STATUS_ON is emitted after the first Tor circuit is established.
            // If the status broadcast raced with binding, the port is still checked
            // here; READY is published only when the service reports a valid SOCKS port.
            publishReady()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            torService = null
            socksPort = -1
            currentState = TorState.FAILED
        }

        override fun onBindingDied(name: ComponentName?) {
            torService = null
            socksPort = -1
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
        circuitEstablished = false
        torService = null
        if (bound) {
            runCatching { appContext.unbindService(connection) }
            bound = false
        }
        unregisterReceiverIfNeeded()
        currentState = TorState.STOPPED
    }

    override fun proxyEndpoint(): TorProxyEndpoint? {
        if (currentState != TorState.READY) return null
        val port = socksPort
        return if (port in 1..65535) TorProxyEndpoint("127.0.0.1", port) else null
    }

    @Synchronized
    private fun publishReady() {
        if (!circuitEstablished) return
        val port = torService?.socksPort ?: return
        if (port in 1..65535 && currentState != TorState.STOPPING) {
            socksPort = port
            currentState = TorState.READY
        }
    }

    private fun unregisterReceiverIfNeeded() {
        if (receiverRegistered) {
            runCatching { appContext.unregisterReceiver(statusReceiver) }
            receiverRegistered = false
        }
    }
}
