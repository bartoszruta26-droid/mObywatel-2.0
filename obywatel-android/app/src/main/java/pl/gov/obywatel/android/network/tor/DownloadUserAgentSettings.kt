// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.tor

import android.content.Context
import android.webkit.WebSettings

enum class DownloadUserAgentProfile(val label: String) {
    MOBYWATEL("mObywatel"),
    BRAVE("Brave Browser"),
    CHROME("Chrome Browser"),
    VIVALDI("Vivaldi Browser"),
    CUSTOM("Własny agent — wpis ręczny")
}

/**
 * Stores a user-selected HTTP User-Agent profile. Browser profiles are compatibility
 * header presets only; they do not turn the app into a browser or alter its TLS stack.
 */
object DownloadUserAgentSettings {
    private const val PREFS = "download_connection_settings"
    private const val KEY_PROFILE = "user_agent_profile"
    private const val KEY_CUSTOM = "user_agent_custom"
    private const val DEFAULT_PROFILE = "MOBYWATEL"
    private const val DEFAULT_MOBYWATEL =
        "mObywatel-2.0/2026.11 (Android; +https://github.com/bartoszruta26-droid/mObywatel-2.0)"

    fun profile(context: Context): DownloadUserAgentProfile {
        val stored = prefs(context).getString(KEY_PROFILE, DEFAULT_PROFILE)
        return runCatching { DownloadUserAgentProfile.valueOf(stored.orEmpty()) }
            .getOrDefault(DownloadUserAgentProfile.MOBYWATEL)
    }

    fun customValue(context: Context): String =
        prefs(context).getString(KEY_CUSTOM, "").orEmpty()

    fun setProfile(context: Context, profile: DownloadUserAgentProfile) {
        prefs(context).edit().putString(KEY_PROFILE, profile.name).apply()
    }

    fun setCustomValue(context: Context, value: String) {
        require(value.isNotBlank()) { "Wartość User-Agent nie może być pusta." }
        require(value.length <= 512) { "Wartość User-Agent może mieć maksymalnie 512 znaków." }
        require(value.none { it == '\r' || it == '\n' }) {
            "Wartość User-Agent nie może zawierać znaków nowej linii."
        }
        prefs(context).edit().putString(KEY_CUSTOM, value.trim()).apply()
    }

    fun value(context: Context): String {
        val preferences = prefs(context)
        val chromiumBase = WebSettings.getDefaultUserAgent(context)
            .replace("; wv)", ")")
            .replace("Version/4.0 ", "")
            .trim()
        return when (profile(context)) {
            DownloadUserAgentProfile.MOBYWATEL -> DEFAULT_MOBYWATEL
            // Brave intentionally minimizes browser-specific User-Agent differences.
            DownloadUserAgentProfile.BRAVE -> chromiumBase
            DownloadUserAgentProfile.CHROME -> chromiumBase
            DownloadUserAgentProfile.VIVALDI -> "$chromiumBase Vivaldi"
            DownloadUserAgentProfile.CUSTOM -> preferences.getString(KEY_CUSTOM, "")
                ?.trim()?.takeIf { it.isNotEmpty() } ?: DEFAULT_MOBYWATEL
        }
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
