// SPDX-FileCopyrightText: 2026 bartoszruta26-droid
// SPDX-License-Identifier: CC0-1.0
package pl.gov.obywatel.android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/**
 * Downloads original raster graphics only when a caller explicitly needs them.
 *
 * The bundled thumbnail manifest is the allow-list: callers can request only a
 * resource name present in that manifest. A SHA-256 digest from the manifest is
 * checked before a file becomes visible in the cache. Downloaded originals are
 * stored outside APK resources and reused offline on subsequent requests.
 */
internal object RemoteGraphicRepository {
    private const val MANIFEST_ASSET = "graphic-thumbnails/manifest.json"
    private const val CACHE_DIR = "remote-graphics"
    private const val MAX_DOWNLOAD_BYTES = 20L * 1024L * 1024L
    private const val MAX_PIXELS = 32_000_000L
    private const val RAW_BASE =
        "https://raw.githubusercontent.com/bartoszruta26-droid/mObywatel-2.0/main/"

    private val http by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .callTimeout(45, TimeUnit.SECONDS)
            .followRedirects(false)
            .build()
    }

    private data class Entry(
        val resourceName: String,
        val fullPath: String,
        val sha256: String,
        val fullBytes: Long,
        val thumbnailAssetPath: String
    )

    private fun manifest(context: Context): Map<String, Entry> {
        val json = context.assets.open(MANIFEST_ASSET).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(json)
        require(root.optInt("schema_version") == 1) { "Nieobsługiwana wersja manifestu grafik." }
        val items = root.getJSONArray("items")
        val result = LinkedHashMap<String, Entry>(items.length())
        for (index in 0 until items.length()) {
            val item = items.getJSONObject(index)
            val name = item.getString("resource_name")
            val path = item.getString("full_path")
            val digest = item.getString("full_sha256").lowercase()
            val bytes = item.getLong("full_bytes")
            val thumbnailPath = item.getString("thumbnail_asset_path")
            require(name.matches(Regex("[A-Za-z0-9_]+"))) { "Nieprawidłowa nazwa zasobu." }
            require(path.startsWith("obywatel-android/app/src/main/res/") &&
                path.split('/').none { it == ".." } &&
                path.matches(Regex("[A-Za-z0-9_./-]+"))) { "Niedozwolona ścieżka zasobu." }
            require(thumbnailPath.startsWith("obywatel-android/app/src/main/assets/graphic-thumbnails/") && thumbnailPath.matches(Regex("[A-Za-z0-9_./-]+")) && thumbnailPath.split('/').none { it == ".." }) { "Nieprawidłowa ścieżka miniatury." }
            require(digest.matches(Regex("[a-f0-9]{64}"))) { "Nieprawidłowy skrót SHA-256." }
            require(bytes in 1..MAX_DOWNLOAD_BYTES) { "Nieprawidłowy rozmiar grafiki." }
            require(result.put(name, Entry(name, path, digest, bytes, thumbnailPath.removePrefix("obywatel-android/app/src/main/assets/"))) == null) {
                "Powielona nazwa zasobu w manifeście: $name"
            }
        }
        return result
    }

    suspend fun fullGraphicFile(context: Context, resourceName: String): File =
        withContext(Dispatchers.IO) {
            require(resourceName.matches(Regex("[A-Za-z0-9_]+"))) { "Nieprawidłowa nazwa grafiki." }
            val entry = manifest(context)[resourceName]
                ?: throw IllegalArgumentException("Grafika nie jest dostępna w katalogu zdalnym: $resourceName")
            val directory = File(context.noBackupFilesDir, CACHE_DIR).apply { mkdirs() }
            val destination = File(directory, "${entry.sha256}.webp")
            if (destination.isFile && destination.length() == entry.fullBytes &&
                sha256(destination) == entry.sha256) return@withContext destination
            destination.delete()

            val url = RAW_BASE + entry.fullPath
            val request = Request.Builder().url(url).header("Accept", "image/webp,image/*;q=0.8").build()
            http.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "Pobranie grafiki nie powiodło się (HTTP ${response.code})." }
                val body = response.body ?: error("Serwer zwrócił pustą odpowiedź.")
                val declaredLength = body.contentLength()
                require(declaredLength == -1L || declaredLength == entry.fullBytes) {
                    "Rozmiar pobranej grafiki nie zgadza się z manifestem."
                }
                val temporary = File(directory, "${entry.sha256}.part")
                try {
                    body.byteStream().use { input ->
                        FileOutputStream(temporary).use { output ->
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            var total = 0L
                            while (true) {
                                val read = input.read(buffer)
                                if (read < 0) break
                                total += read
                                require(total <= MAX_DOWNLOAD_BYTES && total <= entry.fullBytes) {
                                    "Grafika przekracza dozwolony rozmiar."
                                }
                                output.write(buffer, 0, read)
                            }
                            output.fd.sync()
                        }
                    }
                    require(temporary.length() == entry.fullBytes) { "Niepełny plik grafiki." }
                    require(sha256(temporary) == entry.sha256) { "Suma kontrolna grafiki jest niezgodna." }
                    check(temporary.renameTo(destination)) { "Nie można zapisać grafiki w pamięci podręcznej." }
                } finally {
                    temporary.delete()
                }
            }
            destination
        }

    fun remoteResourceNames(context: Context): Set<String> =
        runCatching { manifest(context).keys }.getOrDefault(emptySet())

    fun isRemoteResource(context: Context, resourceName: String): Boolean =
        resourceName in remoteResourceNames(context)

    fun thumbnailBitmap(context: Context, resourceName: String): Bitmap? = runCatching {
        val entry = manifest(context)[resourceName] ?: return null
        context.assets.open(entry.thumbnailAssetPath).use { BitmapFactory.decodeStream(it) }
    }.getOrNull()

    fun cachedGraphicFile(context: Context, resourceName: String): File? = runCatching {
        val entry = manifest(context)[resourceName] ?: return null
        File(File(context.noBackupFilesDir, CACHE_DIR), "${entry.sha256}.webp")
            .takeIf { it.isFile && it.length() == entry.fullBytes && sha256(it) == entry.sha256 }
    }.getOrNull()

    suspend fun fullGraphicBitmap(
        context: Context,
        resourceName: String,
        maxEdgePx: Int = 2400
    ): Bitmap = withContext(Dispatchers.IO) {
        require(maxEdgePx in 128..4096) { "Nieprawidłowy rozmiar docelowy." }
        val file = fullGraphicFile(context, resourceName)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0 &&
            bounds.outWidth.toLong() * bounds.outHeight <= MAX_PIXELS) {
            "Grafika ma nieprawidłowe lub zbyt duże wymiary."
        }
        var sample = 1
        while (bounds.outWidth / sample > maxEdgePx || bounds.outHeight / sample > maxEdgePx) {
            sample *= 2
        }
        BitmapFactory.decodeFile(
            file.absolutePath,
            BitmapFactory.Options().apply { inSampleSize = sample; inPreferredConfig = Bitmap.Config.ARGB_8888 }
        ) ?: error("Nie można zdekodować pobranej grafiki.")
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
