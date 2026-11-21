// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.studiaonline

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.nio.CharBuffer
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Stores Studia Online credentials only on this Android device.
 *
 * The AES-256 key is generated inside Android Keystore and is never exported.
 * The repository contains this implementation, not a user's login or password.
 */
class StudiaOnlineCredentialStore(context: Context) {
    private val appContext = context.applicationContext

    data class Credentials(
        val username: String,
        val password: CharArray
    )

    @Synchronized
    fun save(username: String, password: CharArray) {
        require(username.isNotBlank()) { "Nazwa użytkownika nie może być pusta." }
        require(password.isNotEmpty()) { "Hasło nie może być puste." }

        val usernameBytes = username.toByteArray(StandardCharsets.UTF_8)
        val passwordBytes = encodePassword(password)
        val plain = ByteArrayOutputStream().use { buffer ->
            DataOutputStream(buffer).use { output ->
                output.writeInt(usernameBytes.size)
                output.write(usernameBytes)
                output.writeInt(passwordBytes.size)
                output.write(passwordBytes)
            }
            buffer.toByteArray()
        }

        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            val encrypted = cipher.doFinal(plain)
            val payload = cipher.iv + encrypted
            check(preferences().edit()
                .putString(KEY_PAYLOAD, Base64.encodeToString(payload, Base64.NO_WRAP))
                .commit()) { "Nie udało się zapisać zaszyfrowanych danych logowania." }
        } finally {
            usernameBytes.fill(0)
            passwordBytes.fill(0)
            plain.fill(0)
        }
    }

    @Synchronized
    fun load(): Credentials? {
        val encoded = preferences().getString(KEY_PAYLOAD, null) ?: return null
        val payload = try {
            Base64.decode(encoded, Base64.NO_WRAP)
        } catch (failure: IllegalArgumentException) {
            clear()
            return null
        }
        if (payload.size <= IV_LENGTH) {
            clear()
            return null
        }

        val iv = payload.copyOfRange(0, IV_LENGTH)
        val ciphertext = payload.copyOfRange(IV_LENGTH, payload.size)
        val plain = try {
            Cipher.getInstance(TRANSFORMATION).run {
                init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(TAG_LENGTH_BITS, iv))
                doFinal(ciphertext)
            }
        } catch (failure: Exception) {
            // A missing/invalidated key or modified ciphertext must never trigger
            // a plaintext fallback.
            clear()
            return null
        } finally {
            iv.fill(0)
            ciphertext.fill(0)
            payload.fill(0)
        }

        return try {
            DataInputStream(ByteArrayInputStream(plain)).use { input ->
                val usernameLength = input.readInt()
                require(usernameLength in 1..MAX_FIELD_BYTES)
                val usernameBytes = ByteArray(usernameLength)
                input.readFully(usernameBytes)
                val passwordLength = input.readInt()
                require(passwordLength in 1..MAX_FIELD_BYTES)
                val passwordBytes = ByteArray(passwordLength)
                input.readFully(passwordBytes)
                require(input.available() == 0)
                val username = try {
                    String(usernameBytes, StandardCharsets.UTF_8)
                } finally {
                    usernameBytes.fill(0)
                }
                val password = try {
                    val decoded = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(passwordBytes))
                    CharArray(decoded.remaining()).also { decoded.get(it) }.also {
                        if (decoded.hasArray()) decoded.array().fill('\u0000')
                    }
                } finally {
                    passwordBytes.fill(0)
                }
                Credentials(username, password)
            }
        } catch (failure: Exception) {
            clear()
            null
        } finally {
            plain.fill(0)
        }
    }

    @Synchronized
    fun clear() {
        preferences().edit().remove(KEY_PAYLOAD).commit()
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) keyStore.deleteEntry(KEY_ALIAS)
    }

    private fun encodePassword(password: CharArray): ByteArray {
        val encoded = StandardCharsets.UTF_8.newEncoder().encode(CharBuffer.wrap(password))
        return ByteArray(encoded.remaining()).also { bytes -> encoded.get(bytes) }
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEY_STORE
        )
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setKeySize(256)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    private fun preferences() = appContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    private companion object {
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val KEY_ALIAS = "studia_online_credentials_aes_gcm"
        const val PREFERENCES = "studia_online_secure_credentials"
        const val KEY_PAYLOAD = "encrypted_credentials_v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_LENGTH = 12
        const val TAG_LENGTH_BITS = 128
        const val MAX_FIELD_BYTES = 16 * 1024
    }
}
