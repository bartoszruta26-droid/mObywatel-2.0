// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.tor

import java.io.IOException
import java.math.BigInteger
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/** User-selected materials hosted at the exact studia-online.pl path supplied in the request. */
object StudiaOnlineDownloadCatalog {
    private const val BASE = "https://studia-online.pl//pobierz-material/"

    fun fromRange(first: Int, last: Int): List<StudiaOnlineDownloadItem> {
        require(first > 0 && last >= first) { "Zakres numerów materiałów jest nieprawidłowy." }
        val count = last.toLong() - first.toLong() + 1L
        require(count <= MAX_RANGE_SIZE) { "Zakres przekracza limit $MAX_RANGE_SIZE materiałów na jedno zadanie." }
        return (first..last).map(::item)
    }

    fun fromIds(ids: List<Int>): List<StudiaOnlineDownloadItem> {
        require(ids.isNotEmpty()) { "Podaj przynajmniej jeden numer materiału." }
        require(ids.all { it > 0 }) { "Numery materiałów muszą być dodatnie." }
        val unique = ids.distinct()
        require(unique.size <= MAX_RANGE_SIZE) { "Lista przekracza limit $MAX_RANGE_SIZE materiałów." }
        return unique.map(::item)
    }

    private fun item(id: Int) = StudiaOnlineDownloadItem(
        id = id,
        url = "$BASE$id"
    )

    const val MAX_RANGE_SIZE = 2_000
}

data class StudiaOnlineDownloadItem(
    val id: Int,
    val url: String,
    val fileName: String = "studia-online-material-$id.bin"
)

data class StudiaOnlineDownloadProgress(
    val completed: Int,
    val total: Int,
    val currentId: Int?,
    val succeeded: List<Int>,
    val failed: Map<Int, String>,
    val paused: Boolean,
    val finished: Boolean,
    val sha256ById: Map<Int, String> = emptyMap(),
    val phase: String = "Oczekiwanie",
    val downloadedBytes: Long = 0L,
    val expectedBytes: Long? = null
) {
    val remaining: Int get() = (total - completed).coerceAtLeast(0)
}

/**
 * Sequential, rate-limited downloader using the app's verified Tor client pool.
 * It never starts more than one request at a time. Pause during a transfer closes
 * that response and restarts the same item from byte zero after resume.
 */
class StudiaOnlineBulkDownloader(
    private val downloads: TorDownloadService,
    private val sleeper: (Long) -> Unit = { Thread.sleep(it) },
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val cookieHeaderProvider: () -> String? = { null }
) {
    @Volatile private var stopRequested = false
    @Volatile private var pauseRequested = false

    fun pause() { pauseRequested = true }
    fun resume() { pauseRequested = false }
    fun stop() { stopRequested = true; pauseRequested = false }

    @Throws(IOException::class)
    fun run(
        items: List<StudiaOnlineDownloadItem>,
        cooldownMs: Long,
        save: (StudiaOnlineDownloadItem, ByteArray) -> String,
        onProgress: (StudiaOnlineDownloadProgress) -> Unit = {}
    ): StudiaOnlineDownloadProgress {
        require(items.isNotEmpty()) { "Lista materiałów jest pusta." }
        require(items.size <= StudiaOnlineDownloadCatalog.MAX_RANGE_SIZE) { "Przekroczono limit materiałów." }
        require(cooldownMs in MIN_COOLDOWN_MS..MAX_COOLDOWN_MS) {
            "Przerwa musi wynosić od 2 do 300 sekund."
        }

        stopRequested = false
        pauseRequested = false
        val success = mutableListOf<Int>()
        val failed = linkedMapOf<Int, String>()
        val sha256ById = linkedMapOf<Int, String>()
        var completed = 0
        var previousRequestAt: Long? = null
        var retryAfterUntilMs: Long? = null

        fun emit(
            currentId: Int?,
            isPaused: Boolean,
            finished: Boolean = false,
            phase: String = if (isPaused) "Wstrzymano" else "Oczekiwanie",
            downloadedBytes: Long = 0L,
            expectedBytes: Long? = null
        ) {
            onProgress(
                StudiaOnlineDownloadProgress(
                    completed, items.size, currentId, success.toList(), failed.toMap(), isPaused, finished,
                    sha256ById.toMap(), phase, downloadedBytes, expectedBytes
                )
            )
        }

        for (item in items) {
            if (stopRequested) break

            var downloaded: ByteArray? = null
            var lastFailure: Exception? = null
            var transientRetryCount = 0
            while (downloaded == null && !stopRequested) {
                waitWhilePaused(item.id, ::emit)
                if (stopRequested) break

                // Cooldown is based on the previous request start and survives
                // pause/resume. Retry-After is treated as a minimum wait.
                val earliestNextRequest = previousRequestAt?.plus(cooldownMs)
                val remainingCooldown = earliestNextRequest?.let { (it - clock()).coerceAtLeast(0L) } ?: 0L
                val retryDeadline = retryAfterUntilMs
                val remainingRetryAfter = remainingDelayMillis(retryDeadline, clock())
                if (remainingRetryAfter == 0L) retryAfterUntilMs = null
                val waitMs = maxOf(remainingCooldown, remainingRetryAfter)
                if (waitMs > 0L) {
                    sleepInterruptibly(minOf(waitMs, PAUSE_POLL_MS))
                    continue
                }
                if (stopRequested || pauseRequested) continue

                emit(item.id, false, phase = "Łączenie — ${downloads.selectedMode.label}…")
                try {
                    previousRequestAt = clock()
                    downloaded = downloadWholeItem(item) { bytes, expected ->
                        emit(item.id, false, phase = "Pobieranie danych — ${downloads.selectedMode.label}", downloadedBytes = bytes, expectedBytes = expected)
                    }
                } catch (failure: Exception) {
                    if (stopRequested) break
                    if (pauseRequested || failure is InterruptedIOException) {
                        // The response is closed by use(); do not issue a new
                        // request until the user resumes.
                        if (pauseRequested) {
                            waitWhilePaused(item.id, ::emit)
                            continue
                        }
                        break
                    }
                    lastFailure = failure
                    val httpFailure = failure as? TorHttpException
                    val retryDelay = retryAfterDelayMillis(httpFailure?.retryAfter, clock())
                    if (retryDelay > 0L) {
                        retryAfterUntilMs = clock() + retryDelay
                    }
                    // Retry rate-limited or temporarily unavailable resources, but
                    // cap retries so a persistent server-side failure cannot loop forever.
                    if (isRetryableHttpStatus(httpFailure?.httpCode) &&
                        retryDelay > 0L && transientRetryCount < MAX_TRANSIENT_RETRIES
                    ) {
                        transientRetryCount++
                        continue
                    }
                    break
                }
            }
            if (stopRequested) break

            if (downloaded != null) {
                try {
                    emit(item.id, false, phase = "Zapisywanie pobranego pliku…", downloadedBytes = downloaded.size.toLong())
                    val namedItem = item.copy(fileName = item.fileName.substringBeforeLast('.') + "." + detectedFileExtension(downloaded))
                    sha256ById[item.id] = save(namedItem, downloaded)
                    success += item.id
                } catch (failure: Exception) {
                    failed[item.id] = describeFailure(failure)
                }
            } else if (lastFailure != null) {
                failed[item.id] = describeFailure(lastFailure)
            } else {
                break
            }

            completed++
            emit(items.getOrNull(completed)?.id, false)
        }

        val finished = !stopRequested && completed == items.size
        emit(null, false, finished)
        return StudiaOnlineDownloadProgress(
            completed, items.size, null, success.toList(), failed.toMap(), false, finished,
            sha256ById.toMap()
        )
    }

    private fun waitWhilePaused(
        itemId: Int,
        emit: (Int?, Boolean, Boolean) -> Unit
    ) {
        while (pauseRequested && !stopRequested) {
            emit(itemId, true, false)
            try {
                sleeper(PAUSE_POLL_MS)
            } catch (interrupted: InterruptedException) {
                Thread.currentThread().interrupt()
                stopRequested = true
            }
        }
    }

    @Throws(IOException::class)
    private fun downloadWholeItem(
        item: StudiaOnlineDownloadItem,
        onBytes: (downloaded: Long, expected: Long?) -> Unit
    ): ByteArray =
        downloads.open(item.url, TorDownloadKind.ARCHIVE, cookieHeader = cookieHeaderProvider()).use { response ->
            if (response.statusCode !in 200..299) {
                throw IOException("Nieoczekiwany status HTTP ${response.statusCode}.")
            }
            if (response.contentLength != null && response.contentLength!! > MAX_FILE_BYTES) {
                throw IOException("Materiał przekracza limit 64 MB.")
            }
            val output = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var total = 0L
            var lastProgressBytes = 0L
            var lastProgressAtMs = clock()
            onBytes(0L, response.contentLength)
            while (true) {
                if (stopRequested) throw InterruptedIOException("Pobieranie zatrzymano.")
                if (pauseRequested) throw InterruptedIOException("Pobieranie wstrzymano.")
                val read = try {
                    response.body.read(buffer)
                } catch (failure: IOException) {
                    if (stopRequested || pauseRequested) {
                        throw InterruptedIOException("Bieżące pobieranie zostało wstrzymane lub zatrzymane.")
                    }
                    throw failure
                }
                if (read < 0) break
                total += read
                if (total > MAX_FILE_BYTES) throw IOException("Materiał przekracza limit 64 MB.")
                output.write(buffer, 0, read)
                val now = clock()
                if (total - lastProgressBytes >= 128L * 1024L || now - lastProgressAtMs >= 500L) {
                    lastProgressBytes = total
                    lastProgressAtMs = now
                    onBytes(total, response.contentLength)
                }
            }
            onBytes(total, response.contentLength)
            output.toByteArray().also {
                if (it.isEmpty()) throw IOException("Serwer zwrócił pusty plik.")
                materialFormatErrorMessage(response.contentType, it)?.let { message -> throw IOException(message) }
            }
        }

    private fun sleepInterruptibly(totalMs: Long) {
        var remaining = totalMs.coerceAtLeast(0L)
        while (remaining > 0L && !stopRequested && !pauseRequested) {
            val slice = minOf(remaining, PAUSE_POLL_MS)
            try {
                sleeper(slice)
            } catch (interrupted: InterruptedException) {
                Thread.currentThread().interrupt()
                stopRequested = true
                return
            }
            remaining -= slice
        }
    }

    companion object {
        internal fun htmlResponseErrorMessage(contentType: String?, bytes: ByteArray): String? {
            val prefix = bytes.copyOfRange(0, minOf(bytes.size, 32 * 1024))
                .toString(Charsets.UTF_8)
                .trimStart()
            val isHtml = contentType?.contains("text/html", ignoreCase = true) == true ||
                prefix.startsWith("<html", ignoreCase = true) ||
                prefix.startsWith("<!doctype html", ignoreCase = true)
            if (!isHtml) return null
            val page = prefix.lowercase(java.util.Locale.ROOT)
            val missingPhrases = listOf(
                "nie znaleziono",
                "nie istnieje",
                "materiał nie jest dostępny",
                "material not found",
                "file not found",
                "not found"
            )
            if (missingPhrases.any(page::contains)) {
                return "Materiał nie został znaleziony — serwis zwrócił stronę błędu zamiast pliku."
            }
            val authPhrases = listOf("zaloguj się", "zaloguj sie", "logowanie", "login", "sign in", "authentication required")
            if (authPhrases.any(page::contains)) {
                return "Serwis zwrócił stronę logowania zamiast pliku. Sprawdź wymagane uprawnienia lub sesję."
            }
            return "Serwis zwrócił stronę HTML zamiast pliku. Materiał nie został zapisany."
        }

        internal fun materialFormatErrorMessage(contentType: String?, bytes: ByteArray): String? {
            htmlResponseErrorMessage(contentType, bytes)?.let { return it }
            if (contentType?.contains("application/pdf", ignoreCase = true) == true) {
                val headerWindow = bytes.copyOfRange(0, minOf(bytes.size, 1024)).toString(Charsets.ISO_8859_1)
                if (headerWindow.indexOf("%PDF-") !in 0..1024) {
                    return "Serwer zadeklarował PDF, ale pobrana zawartość nie ma prawidłowej sygnatury PDF. Plik nie został zapisany."
                }
            }
            return null
        }

        internal fun detectedFileExtension(bytes: ByteArray): String = when {
            bytes.size >= 5 && bytes.copyOfRange(0, 5).contentEquals("%PDF-".toByteArray(Charsets.US_ASCII)) -> "pdf"
            bytes.size >= 4 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4b.toByte() &&
                bytes[2] in byteArrayOf(0x03, 0x05, 0x07) && bytes[3] in byteArrayOf(0x04, 0x06, 0x08) -> "zip"
            bytes.size >= 8 && bytes.copyOfRange(0, 8).contentEquals(byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a)) -> "png"
            bytes.size >= 3 && bytes[0] == 0xff.toByte() && bytes[1] == 0xd8.toByte() && bytes[2] == 0xff.toByte() -> "jpg"
            else -> "bin"
        }

        internal fun describeFailure(failure: Exception): String = when ((failure as? TorHttpException)?.httpCode) {
            404 -> "Materiał nie został znaleziony (HTTP 404)."
            410 -> "Materiał został usunięty z serwisu (HTTP 410)."
            401, 403 -> {
                val http = failure as TorHttpException
                buildString {
                    append("Serwis odmówił dostępu (HTTP ${http.httpCode}). ")
                    append("Sprawdź aktywną sesję, uprawnienia materiału i ograniczenia antyautomatyczne serwisu.")
                    http.server?.let { append(" Serwer: ").append(it).append(".") }
                    http.cfRay?.let { append(" Identyfikator CF-RAY: ").append(it).append(".") }
                    http.responseContentType?.let { append(" Typ odpowiedzi: ").append(it).append(".") }
                }
            }
            else -> failure.message ?: failure.javaClass.simpleName
        }

        internal fun remainingDelayMillis(deadlineMs: Long?, nowMs: Long): Long =
            deadlineMs?.let { (it - nowMs).coerceAtLeast(0L) } ?: 0L

        internal fun isRetryableHttpStatus(statusCode: Int?): Boolean =
            statusCode == 429 || statusCode == 503

        internal fun retryAfterDelayMillis(value: String?, nowMs: Long): Long {
            if (value.isNullOrBlank()) return 0L
            value.trim().toBigIntegerOrNull()?.let { seconds ->
                if (seconds.signum() > 0) {
                    val cappedSeconds = seconds.min(BigInteger.valueOf(MAX_RETRY_WAIT_MS / 1_000L))
                    return cappedSeconds.toLong() * 1_000L
                }
                return 0L
            }
            val targetMs = runCatching {
                ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME)
                    .toInstant().toEpochMilli()
            }.getOrNull() ?: return 0L
            return (targetMs - nowMs).coerceAtLeast(0L).coerceAtMost(MAX_RETRY_WAIT_MS)
        }

        const val MIN_COOLDOWN_MS = 2_000L
        const val MAX_COOLDOWN_MS = 300_000L
        const val DEFAULT_COOLDOWN_MS = 15_000L
        private const val PAUSE_POLL_MS = 250L
        private const val MAX_RETRY_WAIT_MS = 300_000L
        private const val MAX_TRANSIENT_RETRIES = 2
        private const val MAX_FILE_BYTES = 64L * 1024L * 1024L
    }

    private class InterruptedIOException(message: String) : IOException(message)
}
