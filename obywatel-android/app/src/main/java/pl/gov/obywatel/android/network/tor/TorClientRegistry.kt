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
    private const val KEY_ORBOT_PORT_MIGRATION = "orbot_port_migration_v1"
    private const val KEY_ORBOT_DEFAULT_PORT_MIGRATION = "orbot_default_port_migration_v2"
    private const val KEY_EXIT_COUNTRY_PREFS = "exit_country_preferences_v1"

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
                when {
                    profile.id == "guardian-embedded" -> profile.copy(enabled = true)
                    profile.id == "orbot-local" && profile.name.startsWith("Orbot") -> profile.copy(enabled = true)
                    else -> profile
                }
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
        if (!prefs.getBoolean(KEY_ORBOT_PORT_MIGRATION, false)) {
            // Move the bundled Orbot profile away from Guardian's usual SOCKS port.
            val normalized = loaded.map { profile ->
                if (profile.id == "orbot-local" && profile.port == 9050) profile.copy(port = 9150) else profile
            }
            save(context, normalized)
            prefs.edit().putBoolean(KEY_ORBOT_PORT_MIGRATION, true).apply()
            return normalized
        }
        if (!prefs.getBoolean(KEY_ORBOT_DEFAULT_PORT_MIGRATION, false)) {
            // Older app builds forced Orbot to 9150 to work around Guardian's
            // 9050 listener. Guardian now owns a separate 19040+ range, so restore
            // Orbot's standard local SOCKS5 port for existing default profiles.
            val normalized = loaded.map { profile ->
                if (profile.id == "orbot-local" && profile.port == 9150) profile.copy(port = 9050) else profile
            }
            save(context, normalized)
            prefs.edit().putBoolean(KEY_ORBOT_DEFAULT_PORT_MIGRATION, true).apply()
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

    fun activeId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY_ACTIVE, "guardian-embedded") ?: "guardian-embedded"
        if (id.startsWith("guardian-isolated-")) {
            prefs.edit().putString(KEY_ACTIVE, "guardian-embedded").apply()
            return "guardian-embedded"
        }
        return id
    }

    fun setActive(context: Context, id: String) {
        require(load(context).any { it.id == id && it.enabled })
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_ACTIVE, id).apply()
    }

    /** Persist country preference for Tor clients whose configuration is owned elsewhere. */
    fun setExitCountryPreference(context: Context, clientId: String, countryCodes: List<String>) {
        require(clientId.isNotBlank())
        require(countryCodes.all { it.matches(Regex("[a-z]{2}")) })
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = runCatching { JSONObject(prefs.getString(KEY_EXIT_COUNTRY_PREFS, "{}").orEmpty()) }
            .getOrDefault(JSONObject())
        json.put(clientId, JSONArray(countryCodes))
        prefs.edit().putString(KEY_EXIT_COUNTRY_PREFS, json.toString()).apply()
    }

    fun exitCountryPreference(context: Context, clientId: String): List<String> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_EXIT_COUNTRY_PREFS, "{}")
        val array = runCatching { JSONObject(raw.orEmpty()).optJSONArray(clientId) }.getOrNull()
            ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                array.optString(index).takeIf { it.matches(Regex("[a-z]{2}")) }?.let(::add)
            }
        }
    }

    fun newProfile(name: String, host: String, port: Int): TorClientProfile {
        val profile = TorClientProfile(UUID.randomUUID().toString(), name.trim(), host.trim(), port)
        require(profile.name.isNotBlank()) { "Podaj nazwę klienta." }
        profile.endpoint()
        return profile
    }
}
