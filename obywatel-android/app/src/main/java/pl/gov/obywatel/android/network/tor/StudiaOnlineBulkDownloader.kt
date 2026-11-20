// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.tor

import java.io.IOException

/**
 * Validated source for an explicitly selected set of files on the user's own
 * studia-online.pl account. The double slash in the supplied URL is preserved
 * as a path separator by URL construction.
 */
object StudiaOnlineDownloadCatalog {
    private const val BASE = "https://studia-online.pl//pobierz-material/"

    fun fromRange(first: Int, last: Int): List<StudiaOnlineDownloadItem> {
        require(first > 0 && last >= first) { "Zakres numerów materiałów jest nieprawidłowy." }
        require(last.toLong() - first.toLong() <= MAX_RANGE_SIZE) {
            "Zakres przekracza limit $MAX_RANGE_SIZE materiałów na jedno zadanie."
        }
        return (first..last).map { item(it) }
    }

    fun fromIds(ids: List<Int>): List<StudiaOnlineDownloadItem> {
        require(ids.isNotEmpty()) { "Podaj przynajmniej jeden numer materiału." }
        require(ids.size <= MAX_RANGE_SIZE) { "Lista przekracza limit $MAX_RANGE_SIZE materiałów." }
        require(ids.all { it > 0 }) { "Numery materiałów muszą być dodatnie." }
        return ids.distinct().map { item(it) }
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
    val fileName: String = "studia-online-material-$id"
)

data class StudiaOnlineDownloadProgress(
    val completed: Int,
    val total: Int,
    val currentId: Int?,
    val succeeded: List<Int>,
    val failed: Map<Int, String>,
    val paused: Boolean,
    val finished: Boolean
) {
    val remaining: Int get() = (total - completed).coerceAtLeast(0)
}

/**
 * Sequential bulk downloader. Every request is sent through TorDownloadService,
 * which verifies and rotates the configured local Tor SOCKS clients. A cooldown
 * is enforced between requests and Retry-After is respected for HTTP 429/503.
 *
 * The destination is supplied by the Android Storage Access Framework; this
 * class never writes outside the user-selected tree.
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
        save: (StudiaOnlineDownloadItem, ByteArray) -> Unit,
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
        var completed = 0
        var previousRequestAt = 0L
        var lastStatus = StudiaOnlineDownloadProgress(0, items.size, null, success, failed, false, false)
        var lastFailureWaitMs = 0L

        fun emit(current: Int?, paused: Boolean, finished: Boolean = false) {
            lastStatus = StudiaOnlineDownloadProgress(
                completed, items.size, current, success.toList(), failed.toMap(), paused, finished
            )
            onProgress(lastStatus)
        }

        for (item in items) {
            if (stopRequested) break
            while (pauseRequested && !stopRequested) {
                emit(item.id, true)
                sleepInterruptibly(PAUSE_POLL_MS)
            }
            if (stopRequested) break

            var wait = (cooldownMs - (clock() - previousRequestAt)).coerceAtLeast(0L)
            while (wait > 0L && !stopRequested && !pauseRequested) {
                sleepInterruptibly(wait)
                wait = (cooldownMs - (clock() - previousRequestAt)).coerceAtLeast(0L)
            }
            if (stopRequested) break
            while (pauseRequested && !stopRequested) {
                emit(item.id, true)
                sleeper(PAUSE_POLL_MS)
            }
            if (stopRequested) break
            if (lastFailureWaitMs > 0L) {
                sleepInterruptibly(lastFailureWaitMs.coerceIn(cooldownMs, MAX_RETRY_WAIT_MS))
                lastFailureWaitMs = 0L
            }
            if (stopRequested) break

            emit(item.id, false)
            previousRequestAt = clock()
            try {
                val bytes = downloads.open(item.url, TorDownloadKind.ARCHIVE).use { response ->
                    if (response.statusCode !in 200..299) {
                        throw IOException("Nieoczekiwany status HTTP ${response.statusCode}.")
                    }
                    val output = java.io.ByteArrayOutputStream()
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var total = 0L
                    while (true) {
                        if (stopRequested || pauseRequested) throw InterruptedIOException("Pobieranie przerwano; można wznowić zadanie.")
                        val read = response.body.read(buffer)
                        if (read < 0) break
                        total += read
                        if (total > MAX_FILE_BYTES) throw IOException("Materiał przekracza limit 256 MB.")
                        output.write(buffer, 0, read)
                    }
                    output.toByteArray()
                }
                if (bytes.isEmpty()) throw IOException("Serwer zwrócił pusty plik.")
                save(item, bytes)
                success += item.id
            } catch (failure: Exception) {
                if (stopRequested) break
                if (pauseRequested) {
                    // Discard the incomplete response; restart this item from byte zero.
                    while (pauseRequested && !stopRequested) {
                        emit(item.id, true)
                        sleepInterruptibly(PAUSE_POLL_MS)
                    }
                    if (stopRequested) break
                    try {
                        val bytes = downloadWholeItem(item)
                        save(item, bytes)
                        success += item.id
                    } catch (retryFailure: Exception) {
                        failed[item.id] = retryFailure.message ?: retryFailure.javaClass.simpleName
                    }
                } else {
                    val retryAfter = (failure as? TorHttpException)?.retryAfter?.toLongOrNull()
                    if (retryAfter != null) lastFailureWaitMs = retryAfter * 1_000L
                    failed[item.id] = failure.message ?: failure.javaClass.simpleName
                }
            }
            completed++
            emit(if (completed < items.size) items[completed].id else null, false)
        }
        emit(null, false, finished = !stopRequested && completed == items.size)
        return lastStatus
    }

    private fun downloadWholeItem(item: StudiaOnlineDownloadItem): ByteArray =
        downloads.open(item.url, TorDownloadKind.ARCHIVE).use { response ->
            if (response.statusCode !in 200..299) {
                throw IOException("Nieoczekiwany status HTTP ${response.statusCode}.")
            }
            val output = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var total = 0L
            while (true) {
                if (stopRequested || pauseRequested) {
                    throw InterruptedIOException("Pobieranie przerwano; można wznowić zadanie.")
                }
                val read = response.body.read(buffer)
                if (read < 0) break
                total += read
                if (total > MAX_FILE_BYTES) throw IOException("Materiał przekracza limit 256 MB.")
                output.write(buffer, 0, read)
            }
            output.toByteArray().also {
                if (it.isEmpty()) throw IOException("Serwer zwrócił pusty plik.")
            }
        }

    private fun sleepInterruptibly(totalMs: Long) {
        var remaining = totalMs.coerceAtLeast(0L)
        while (remaining > 0L && !stopRequested && !pauseRequested) {
            val slice = minOf(remaining, 250L)
            sleeper(slice)
            remaining -= slice
        }
    }

    companion object {
        const val MIN_COOLDOWN_MS = 2_000L
        const val MAX_COOLDOWN_MS = 300_000L
        const val DEFAULT_COOLDOWN_MS = 15_000L
        private const val PAUSE_POLL_MS = 500L
        private const val MAX_RETRY_WAIT_MS = 300_000L
        private const val MAX_FILE_BYTES = 256L * 1024L * 1024L
    }

    private class InterruptedIOException(message: String) : IOException(message)
}
