// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.tor

import java.io.IOException
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
    val sha256ById: Map<Int, String> = emptyMap()
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
    private val clock: () -> Long = { System.currentTimeMillis() }
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
        var retryAfterMs = 0L

        fun emit(currentId: Int?, isPaused: Boolean, finished: Boolean = false) {
            onProgress(
                StudiaOnlineDownloadProgress(
                    completed, items.size, currentId, success.toList(), failed.toMap(), isPaused, finished,
                    sha256ById.toMap()
                )
            )
        }

        for (item in items) {
            if (stopRequested) break

            var downloaded: ByteArray? = null
            var lastFailure: Exception? = null
            while (downloaded == null && !stopRequested) {
                waitWhilePaused(item.id, ::emit)
                if (stopRequested) break

                // Cooldown is based on the previous request start and survives
                // pause/resume. Retry-After is treated as a minimum wait.
                val earliestNextRequest = previousRequestAt?.plus(cooldownMs)
                val remainingCooldown = earliestNextRequest?.let { (it - clock()).coerceAtLeast(0L) } ?: 0L
                val remainingRetryAfter = retryAfterMs.coerceAtLeast(0L)
                val waitMs = maxOf(remainingCooldown, remainingRetryAfter)
                if (waitMs > 0L) {
                    sleepInterruptibly(minOf(waitMs, PAUSE_POLL_MS))
                    continue
                }
                if (stopRequested || pauseRequested) continue
                retryAfterMs = 0L

                emit(item.id, false)
                try {
                    previousRequestAt = clock()
                    downloaded = downloadWholeItem(item)
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
                    val retryAfter = (failure as? TorHttpException)?.retryAfter
                    retryAfterMs = retryAfterDelayMillis(retryAfter, clock())
                    break
                }
            }
            if (stopRequested) break

            if (downloaded != null) {
                try {
                    sha256ById[item.id] = save(item, downloaded)
                    success += item.id
                } catch (failure: Exception) {
                    failed[item.id] = failure.message ?: failure.javaClass.simpleName
                }
            } else if (lastFailure != null) {
                failed[item.id] = lastFailure.message ?: lastFailure.javaClass.simpleName
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
    private fun downloadWholeItem(item: StudiaOnlineDownloadItem): ByteArray =
        downloads.open(item.url, TorDownloadKind.ARCHIVE).use { response ->
            if (response.statusCode !in 200..299) {
                throw IOException("Nieoczekiwany status HTTP ${response.statusCode}.")
            }
            if (response.contentLength != null && response.contentLength!! > MAX_FILE_BYTES) {
                throw IOException("Materiał przekracza limit 64 MB.")
            }
            val output = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var total = 0L
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
            }
            output.toByteArray().also {
                if (it.isEmpty()) throw IOException("Serwer zwrócił pusty plik.")
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
        internal fun retryAfterDelayMillis(value: String?, nowMs: Long): Long {
            if (value.isNullOrBlank()) return 0L
            value.trim().toLongOrNull()?.let { seconds ->
                if (seconds > 0L) return (seconds * 1_000L).coerceAtMost(MAX_RETRY_WAIT_MS)
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
        private const val MAX_FILE_BYTES = 64L * 1024L * 1024L
    }

    private class InterruptedIOException(message: String) : IOException(message)
}
