// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.tor

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * User-managed local Tor SOCKS clients. Remote SOCKS endpoints are deliberately rejected:
 * an arbitrary public proxy is not a trusted Tor entry point.
 */
data class TorClientProfile(
    val id: String,
    val name: String,
    val host: String,
    val port: Int,
    val enabled: Boolean = true
) {
    fun endpoint(): TorProxyEndpoint = TorProxyEndpoint(host, port)
}

object TorClientRegistry {
    private const val PREFS = "tor_client_registry"
    private const val KEY_CLIENTS = "clients"
    private const val KEY_ACTIVE = "active_client_id"
    private const val KEY_ORBOT_MIGRATION = "orbot_profile_migration_v1"
    private const val KEY_LISTENER_PROFILE_MIGRATION = "listener_profile_migration_v2"

    fun load(context: Context): List<TorClientProfile> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_CLIENTS, null)
        if (raw.isNullOrBlank()) {
            val defaults = listOf(
                TorClientProfile("guardian-embedded", "Wbudowany Tor — port podstawowy", "127.0.0.1", 9050),
                TorClientProfile("orbot-local", "Orbot — lokalny SOCKS5", "127.0.0.1", 9050, true)
            )
            save(context, defaults)
            return defaults
        }
        val loaded = runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val profile = TorClientProfile(
                        id = item.getString("id"),
                        name = item.getString("name"),
                        host = item.getString("host"),
                        port = item.getInt("port"),
                        enabled = item.optBoolean("enabled", true)
                    )
                    runCatching { profile.endpoint() }.getOrNull()?.let { add(profile) }
                }
            }
        }.getOrDefault(emptyList())
        if (!prefs.getBoolean(KEY_ORBOT_MIGRATION, false)) {
            val migrated = loaded.filterNot { it.id.startsWith("guardian-isolated-") }.map { profile ->
                if (profile.id == "orbot-local" && profile.name.startsWith("Orbot")) {
                    profile.copy(enabled = true)
                } else profile
            }
            save(context, migrated)
            prefs.edit().putBoolean(KEY_ORBOT_MIGRATION, true).apply()
            return migrated
        }
        if (!prefs.getBoolean(KEY_LISTENER_PROFILE_MIGRATION, false)) {
            val normalized = loaded.filterNot { it.id.startsWith("guardian-isolated-") }
            save(context, normalized)
            prefs.edit().putBoolean(KEY_LISTENER_PROFILE_MIGRATION, true).apply()
            return normalized
        }
        return loaded
    }

    fun save(context: Context, clients: List<TorClientProfile>) {
        val array = JSONArray()
        clients.forEach { profile ->
            array.put(JSONObject().put("id", profile.id).put("name", profile.name)
                .put("host", profile.host).put("port", profile.port).put("enabled", profile.enabled))
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_CLIENTS, array.toString()).apply()
    }

    fun activeId(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_ACTIVE, "guardian-embedded") ?: "guardian-embedded"

    fun setActive(context: Context, id: String) {
        require(load(context).any { it.id == id && it.enabled })
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_ACTIVE, id).apply()
    }

    fun newProfile(name: String, host: String, port: Int): TorClientProfile {
        val profile = TorClientProfile(UUID.randomUUID().toString(), name.trim(), host.trim(), port)
        require(profile.name.isNotBlank()) { "Podaj nazwę klienta." }
        profile.endpoint()
        return profile
    }
}
