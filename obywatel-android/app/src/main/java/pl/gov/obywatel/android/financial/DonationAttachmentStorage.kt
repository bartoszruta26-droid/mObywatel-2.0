// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android.financial

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import pl.gov.obywatel.android.DonationAttachment
import java.io.File
import java.io.FileOutputStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID

/** Kopiuje wybrane przez SAF dokumenty do prywatnego katalogu aplikacji. */
class DonationAttachmentStorage(context: Context) {
    private val resolver = context.applicationContext.contentResolver
    private val root = File(context.applicationContext.filesDir, "donation-attachments")

    fun import(donationId: String, uri: Uri): DonationAttachment {
        require(donationId.isNotBlank()) { "Identyfikator darowizny jest wymagany." }
        val mimeType = resolver.getType(uri)?.lowercase()
            ?: throw IllegalArgumentException("Nie można ustalić typu pliku.")
        require(mimeType in DonationAttachment.ALLOWED_MIME_TYPES) {
            "Dozwolone są wyłącznie pliki PDF, JPEG i PNG."
        }
        val displayName = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }?.takeIf { it.isNotBlank() } ?: "zalacznik"
        val safeName = displayName.substringAfterLast('/').substringAfterLast('\\')
            .filter { it.isLetterOrDigit() || it in " ._-()" }
            .take(180).ifBlank { "zalacznik" }
        val extension = when (mimeType) {
            "application/pdf" -> "pdf"
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            else -> error("Nieobsługiwany typ pliku.")
        }
        if (!root.exists() && !root.mkdirs()) throw IllegalStateException("Nie można utworzyć prywatnego katalogu załączników.")
        val id = UUID.randomUUID().toString()
        val storedName = "$id.$extension"
        val target = File(root, storedName)
        val digest = MessageDigest.getInstance("SHA-256")
        var total = 0L
        val signature = ByteArrayOutputStream(8)
        try {
            val input = resolver.openInputStream(uri) ?: throw IllegalStateException("Nie można odczytać wybranego pliku.")
            input.use { source ->
                FileOutputStream(target).use { output ->
                    val buffer = ByteArray(8192)
                    while (true) {
                        val count = source.read(buffer)
                        if (count < 0) break
                        total += count
                        require(total <= DonationAttachment.MAX_BYTES) { "Załącznik przekracza limit 10 MB." }
                        digest.update(buffer, 0, count)
                        if (signature.size() < 8) {
                            signature.write(buffer, 0, minOf(count, 8 - signature.size()))
                        }
                        output.write(buffer, 0, count)
                    }
                    output.fd.sync()
                }
            }
            require(total > 0) { "Wybrany plik jest pusty." }
            require(isDonationAttachmentSignatureValid(mimeType, signature.toByteArray())) {
                "Zawartość pliku nie odpowiada zadeklarowanemu typowi PDF/JPEG/PNG."
            }
            return DonationAttachment(
                id = id,
                donationId = donationId,
                fileName = safeName,
                mimeType = mimeType,
                sizeBytes = total,
                sha256 = digest.digest().joinToString("") { "%02x".format(it) },
                storedFileName = storedName,
                addedAt = Instant.now()
            ).also { it.validate() }
        } catch (error: Exception) {
            target.delete()
            throw error
        }
    }

    fun delete(storedFileName: String) {
        require(storedFileName.matches(Regex("[a-f0-9-]{36}\\.(pdf|jpg|png)"))) {
            "Nieprawidłowa nazwa pliku załącznika."
        }
        val file = File(root, storedFileName)
        if (file.exists() && !file.delete()) {
            throw IllegalStateException("Nie udało się usunąć pliku załącznika z pamięci aplikacji.")
        }
    }
}


/** Weryfikuje sygnaturę binarną, aby MIME dostawcy SAF nie był jedynym kryterium typu. */
internal fun isDonationAttachmentSignatureValid(mimeType: String, header: ByteArray): Boolean =
    when (mimeType) {
        "application/pdf" -> header.size >= 5 &&
            header[0] == '%'.code.toByte() &&
            header[1] == 'P'.code.toByte() &&
            header[2] == 'D'.code.toByte() &&
            header[3] == 'F'.code.toByte() &&
            header[4] == '-'.code.toByte()
        "image/jpeg" -> header.size >= 3 &&
            header[0] == 0xFF.toByte() && header[1] == 0xD8.toByte() && header[2] == 0xFF.toByte()
        "image/png" -> header.size >= 8 &&
            header[0] == 0x89.toByte() && header[1] == 0x50.toByte() &&
            header[2] == 0x4E.toByte() && header[3] == 0x47.toByte() &&
            header[4] == 0x0D.toByte() && header[5] == 0x0A.toByte() &&
            header[6] == 0x1A.toByte() && header[7] == 0x0A.toByte()
        else -> false
    }
