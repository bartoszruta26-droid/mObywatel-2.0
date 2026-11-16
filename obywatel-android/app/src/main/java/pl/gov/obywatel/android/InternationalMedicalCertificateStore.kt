// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Replaces a file without exposing a partially written destination.
 * The temporary file is synced before the same-filesystem atomic rename.
 * If atomic replacement is unavailable, the operation fails and leaves the
 * previous destination untouched rather than falling back to a truncating copy.
 */
internal fun writeMedicalCertificateAtomically(directory: File, fileName: String, encrypted: ByteArray) {
    require(fileName.matches(Regex("[A-Za-z0-9_.-]{1,128}")) && !fileName.startsWith(".")) {
        "Invalid medical certificate file name."
    }
    require(encrypted.isNotEmpty()) { "Encrypted medical certificate cannot be empty." }
    check(directory.exists() || directory.mkdirs()) { "Could not create medical certificate directory." }
    val canonicalDirectory = directory.canonicalFile
    val target = File(canonicalDirectory, fileName)
    val temporary = File(canonicalDirectory, ".$fileName.tmp")
    require(target.parentFile == canonicalDirectory && temporary.parentFile == canonicalDirectory) {
        "Invalid medical certificate storage path."
    }
    try {
        FileOutputStream(temporary, false).use { output ->
            output.write(encrypted)
            output.fd.sync()
        }
        Files.move(
            temporary.toPath(),
            target.toPath(),
            StandardCopyOption.ATOMIC_MOVE,
            StandardCopyOption.REPLACE_EXISTING
        )
    } catch (failure: Exception) {
        runCatching { temporary.delete() }
        throw IllegalStateException(
            "Could not atomically replace the encrypted medical certificate; the previous file was preserved.",
            failure
        )
    }
}

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
    private const val MAX_ENCRYPTED_BYTES = 10 * 1024 * 1024
    private const val MAX_PLAINTEXT_BYTES = 9 * 1024 * 1024

    fun load(context: Context): Map<String, String> = loadStrict(context)

    fun loadStrict(context: Context): Map<String, String> {
        val file = File(context.noBackupFilesDir, FILE_NAME)
        if (!file.exists()) return emptyMap()
        require(file.length() in (IV_LENGTH_BYTES + 1).toLong()..MAX_ENCRYPTED_BYTES.toLong()) {
            "Encrypted medical certificate file has an invalid size."
        }
        val encrypted = file.readBytes()
        require(encrypted.size > IV_LENGTH_BYTES && encrypted.size <= MAX_ENCRYPTED_BYTES) {
            "Invalid encrypted medical certificate file."
        }
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
        val plaintext = json.toString().toByteArray(Charsets.UTF_8)
        require(plaintext.size <= MAX_PLAINTEXT_BYTES) {
            "Medical certificate data exceeds the permitted storage size."
        }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.iv + cipher.doFinal(plaintext)
        require(encrypted.size <= MAX_ENCRYPTED_BYTES) {
            "Encrypted medical certificate exceeds the permitted storage size."
        }
        writeMedicalCertificateAtomically(context.noBackupFilesDir, FILE_NAME, encrypted)
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


/** Multiple certificates share the encrypted Keystore-protected file without exposing clinical data. */
internal object InternationalMedicalCertificateRecords {
    private const val INDEX_KEY = "imc_record_index"
    private const val RECORD_PREFIX = "imc_record_"

    fun list(context: Context): List<Pair<String, Map<String, String>>> {
        val all = InternationalMedicalCertificateStore.loadStrict(context)
        val ids = all[INDEX_KEY].orEmpty().split(',').filter { it.matches(Regex("[A-Za-z0-9_-]{1,64}")) }
        if (ids.isNotEmpty()) return ids.mapNotNull { id -> decodeRecord(id, all[RECORD_PREFIX + id]) }
        // One-time, lossless migration of the original single-certificate format.
        val legacy = all.filterKeys { it.startsWith("doc_international_medical_certificate_") || it.startsWith("imc_") }
        return if (legacy.isEmpty()) emptyList() else listOf("legacy" to legacy)
    }

    fun load(context: Context, id: String): Map<String, String> {
        require(id.matches(Regex("[A-Za-z0-9_-]{1,64}"))) { "Invalid certificate identifier." }
        val all = InternationalMedicalCertificateStore.loadStrict(context)
        if (id == "legacy" && all[RECORD_PREFIX + id] == null) {
            return all.filterKeys { it.startsWith("doc_international_medical_certificate_") || it.startsWith("imc_") }
        }
        return decodeRecord(id, all[RECORD_PREFIX + id])?.second.orEmpty()
    }

    fun save(context: Context, id: String, values: Map<String, String>) {
        require(id.matches(Regex("[A-Za-z0-9_-]{1,64}"))) { "Invalid certificate identifier." }
        val all = InternationalMedicalCertificateStore.loadStrict(context).toMutableMap()
        val ids = all[INDEX_KEY].orEmpty().split(',').filter { it.matches(Regex("[A-Za-z0-9_-]{1,64}")) }.toMutableSet()
        if (ids.isEmpty()) {
            val legacy = all.filterKeys { it.startsWith("doc_international_medical_certificate_") || it.startsWith("imc_") }
            if (legacy.isNotEmpty() && id != "legacy") {
                val json = JSONObject()
                legacy.forEach { (key, value) -> json.put(key, value) }
                all[RECORD_PREFIX + "legacy"] = json.toString()
                ids.add("legacy")
                legacy.keys.forEach(all::remove)
            }
        }
        val json = JSONObject()
        values.forEach { (key, value) ->
            require(key.matches(Regex("[A-Za-z0-9_-]{1,64}")))
            json.put(key, value)
        }
        all[RECORD_PREFIX + id] = json.toString()
        ids.add(id)
        all[INDEX_KEY] = ids.sorted().joinToString(",")
        InternationalMedicalCertificateStore.save(context, all)
    }

    fun delete(context: Context, id: String) {
        require(id.matches(Regex("[A-Za-z0-9_-]{1,64}"))) { "Invalid certificate identifier." }
        val all = InternationalMedicalCertificateStore.loadStrict(context).toMutableMap()
        val ids = all[INDEX_KEY].orEmpty().split(',').filter { it.isNotBlank() && it != id }
        all.remove(RECORD_PREFIX + id)
        all[INDEX_KEY] = ids.joinToString(",")
        if (id == "legacy") {
            all.keys.filter { it.startsWith("doc_international_medical_certificate_") || it.startsWith("imc_") }
                .toList().forEach(all::remove)
        }
        InternationalMedicalCertificateStore.save(context, all)
    }

    private fun decodeRecord(id: String, serialized: String?): Pair<String, Map<String, String>>? {
        if (serialized.isNullOrBlank()) return null
        val json = JSONObject(serialized)
        val values = linkedMapOf<String, String>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            require(key.matches(Regex("[A-Za-z0-9_-]{1,64}")) && !json.isNull(key))
            values[key] = json.getString(key)
        }
        return id to values
    }
}
