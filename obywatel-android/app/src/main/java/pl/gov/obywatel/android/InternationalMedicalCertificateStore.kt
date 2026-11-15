// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import org.json.JSONObject
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Dedicated encrypted store for the international medical certificate.
 * It uses a separate Android Keystore key and file; clinical data is not mixed
 * with the general identity-profile archive.
 */
internal object InternationalMedicalCertificateStore {
    private const val KEY_ALIAS = "international_medical_certificate_aes"
    private const val FILE_NAME = "international_medical_certificate.enc"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val IV_LENGTH_BYTES = 12

    fun load(context: Context): Map<String, String> =
        runCatching { loadStrict(context) }.getOrDefault(emptyMap())

    fun loadStrict(context: Context): Map<String, String> {
        val file = File(context.noBackupFilesDir, FILE_NAME)
        if (!file.exists()) return emptyMap()
        val encrypted = file.readBytes()
        require(encrypted.size > IV_LENGTH_BYTES) { "Invalid encrypted medical certificate file." }
        val iv = encrypted.copyOfRange(0, IV_LENGTH_BYTES)
        val ciphertext = encrypted.copyOfRange(IV_LENGTH_BYTES, encrypted.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
        val json = JSONObject(String(cipher.doFinal(ciphertext), Charsets.UTF_8))
        val result = linkedMapOf<String, String>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            require(key.matches(Regex("[A-Za-z0-9_-]{1,64}"))) {
                "Invalid medical certificate field key."
            }
            require(!json.isNull(key)) { "Medical certificate field cannot be null." }
            result[key] = json.getString(key)
        }
        return result
    }

    fun save(context: Context, values: Map<String, String>) {
        require(values.keys.all { it.matches(Regex("[A-Za-z0-9_-]{1,64}")) }) {
            "Medical certificate contains an invalid field key."
        }
        val json = JSONObject()
        values.forEach { (key, value) -> json.put(key, value) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.iv + cipher.doFinal(json.toString().toByteArray(Charsets.UTF_8))
        val target = File(context.noBackupFilesDir, FILE_NAME)
        val temporary = File(context.noBackupFilesDir, "$FILE_NAME.tmp")
        temporary.writeBytes(encrypted)
        if (!temporary.renameTo(target)) {
            temporary.copyTo(target, overwrite = true)
            check(temporary.delete()) { "Could not remove temporary medical certificate file." }
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }
}
