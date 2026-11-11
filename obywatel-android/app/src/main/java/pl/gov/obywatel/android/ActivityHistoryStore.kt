// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Local, encrypted activity history for document operations.
 * The archive is private to the application and is not a legal audit log.
 */
internal object ActivityHistoryStore {
    private const val KEY_ALIAS = "activity_history_aes"
    private const val FILE_NAME = "activity-history.enc"
    private const val MAX_ENTRIES = 200

    @Synchronized
    fun record(context: Context, category: String, action: String, detail: String) {
        runCatching {
            val entries = read(context)
            entries.put(JSONObject().apply {
                put("timestamp", System.currentTimeMillis())
                put("category", category.take(80))
                put("action", action.take(160))
                put("detail", detail.take(240))
            })
            val trimmed = JSONArray()
            val start = (entries.length() - MAX_ENTRIES).coerceAtLeast(0)
            for (index in start until entries.length()) trimmed.put(entries.getJSONObject(index))
            write(context, trimmed.toString().toByteArray(Charsets.UTF_8))
        }
    }

    private fun read(context: Context): JSONArray = runCatching {
        val file = File(context.noBackupFilesDir, FILE_NAME)
        if (!file.exists()) JSONArray()
        else JSONArray(String(decrypt(file.readBytes()), Charsets.UTF_8))
    }.getOrDefault(JSONArray())

    private fun write(context: Context, plain: ByteArray) {
        val target = File(context.noBackupFilesDir, FILE_NAME)
        val temp = File(target.parentFile, "${target.name}.tmp")
        temp.writeBytes(encrypt(plain))
        if (!temp.renameTo(target)) {
            temp.copyTo(target, overwrite = true)
            temp.delete()
        }
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build())
        }.generateKey()
    }

    private fun encrypt(plain: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        return cipher.iv + cipher.doFinal(plain)
    }

    private fun decrypt(encrypted: ByteArray): ByteArray {
        require(encrypted.size > 12) { "Nieprawidłowe archiwum historii." }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(),
            GCMParameterSpec(128, encrypted.copyOfRange(0, 12)))
        return cipher.doFinal(encrypted.copyOfRange(12, encrypted.size))
    }
}
