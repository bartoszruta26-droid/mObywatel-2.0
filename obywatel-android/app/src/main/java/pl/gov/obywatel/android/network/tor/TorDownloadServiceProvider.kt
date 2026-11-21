// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.tor

import android.content.Context

/**
 * Process-wide owner for the embedded Tor runtime and the shared download service.
 *
 * Keeping one runtime prevents screens and download workers from creating competing
 * Guardian services or losing the user's explicitly selected transport configuration.
 */
object TorDownloadServiceProvider {
    @Volatile
    private var sharedRuntime: GuardianTorRuntime? = null

    @Volatile
    private var sharedService: TorDownloadService? = null

    @Synchronized
    fun getRuntime(context: Context): GuardianTorRuntime {
        return sharedRuntime ?: GuardianTorRuntime(context.applicationContext).also {
            sharedRuntime = it
        }
    }

    @Synchronized
    fun get(context: Context): TorDownloadService {
        return sharedService ?: TorDownloadService(
            context.applicationContext,
            getRuntime(context.applicationContext)
        ).also {
            sharedService = it
        }
    }
}
