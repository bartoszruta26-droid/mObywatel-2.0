// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0

package pl.gov.obywatel.android

import android.content.Context
import java.security.MessageDigest

/** Persistent, process-safe-enough replay guard for a single Android app installation. */
internal class OwdReplayStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("owd_consumed_sessions_v1", Context.MODE_PRIVATE)

    fun consume(sessionId: String, expiresAtEpochSeconds: Long,
                nowEpochSeconds: Long = System.currentTimeMillis() / 1000): Boolean = synchronized(processLock) {
        if (sessionId.isBlank() || expiresAtEpochSeconds <= nowEpochSeconds) return@synchronized false
        val key = digest(sessionId)
        if (preferences.contains(key)) return@synchronized false
        preferences.edit().putLong(key, expiresAtEpochSeconds).commit()
    }

    fun prune(nowEpochSeconds: Long = System.currentTimeMillis() / 1000) = synchronized(processLock) {
        val editor = preferences.edit()
        preferences.all.forEach { (key, value) ->
            if (value is Long && value <= nowEpochSeconds) editor.remove(key)
        }
        editor.commit()
    }

    private fun digest(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it.toInt() and 0xff) }

    private companion object {
        val processLock = Any()
    }
}
