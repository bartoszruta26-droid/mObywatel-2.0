// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.tor

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.IOException
import java.security.MessageDigest

/**
 * Persists a completed material in the user-selected Storage Access Framework tree.
 * An incomplete .part file is never treated as a completed download.
 */
class StudiaOnlineSafStore(private val context: Context) {
    @Throws(IOException::class)
    fun save(treeUri: Uri, item: StudiaOnlineDownloadItem, bytes: ByteArray): String {
        require(item.url.startsWith("https://studia-online.pl//pobierz-material/")) {
            "Do archiwum można zapisywać wyłącznie materiały z dozwolonego katalogu."
        }
        require(bytes.isNotEmpty()) { "Nie można zapisać pustego materiału." }
        val tree = DocumentFile.fromTreeUri(context, treeUri)
            ?: throw IOException("Wybrany katalog nie jest dostępny.")
        if (!tree.canWrite()) throw IOException("Brak uprawnień do zapisu w wybranym katalogu.")

        val fileName = safeFileName(item.fileName) + extensionFor(item.fileName)
        val existing = tree.findFile(fileName)
        val tempName = ".${safeFileName(item.fileName)}.part"
        tree.findFile(tempName)?.let {
            if (!it.delete()) throw IOException("Nie można usunąć poprzedniego pliku tymczasowego.")
        }
        val temporary = tree.createFile("application/octet-stream", tempName)
            ?: throw IOException("Nie można utworzyć pliku tymczasowego.")
        try {
            context.contentResolver.openOutputStream(temporary.uri, "w")?.use { output ->
                output.write(bytes)
                output.flush()
            } ?: throw IOException("Nie można otworzyć strumienia zapisu.")

            val checksum = MessageDigest.getInstance("SHA-256")
                .digest(bytes).joinToString("") { "%02x".format(it) }

            if (existing != null && !existing.delete()) {
                throw IOException("Nie można zastąpić istniejącego pliku $fileName.")
            }
            val committed = temporary.renameTo(fileName)
            if (!committed) throw IOException("Nie można zatwierdzić pliku $fileName.")
            return checksum
        } catch (failure: Exception) {
            temporary.delete()
            if (failure is IOException) throw failure
            throw IOException("Nie udało się zapisać materiału.", failure)
        }
    }

    private fun safeFileName(value: String): String =
        value.substringBeforeLast('.', value)
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .trim('.', '_')
            .take(100)
            .ifBlank { "material" }

    private fun extensionFor(value: String): String {
        val ext = value.substringAfterLast('.', "").lowercase()
        return if (ext.matches(Regex("[a-z0-9]{1,8}"))) ".$ext" else ".bin"
    }
}
