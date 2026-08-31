package com.example.engine

import android.net.Network
import com.example.util.NetworkRoutingMode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import javax.net.SocketFactory
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

enum class StressMode {
    DOWNLOAD,
    UPLOAD,
    BI_DIRECTIONAL
}

data class DataUsagePoint(
    val timestampSec: Long,
    val totalBytes: Long,
    val downloadBytes: Long,
    val uploadBytes: Long,
    val speedMbps: Double
)

data class EngineStats(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val currentDownloadSpeedMbps: Double = 0.0,
    val currentUploadSpeedMbps: Double = 0.0,
    val currentTotalSpeedMbps: Double = 0.0,
    val peakDownloadSpeedMbps: Double = 0.0,
    val peakUploadSpeedMbps: Double = 0.0,
    val peakTotalSpeedMbps: Double = 0.0,
    val avgSpeedMbps: Double = 0.0,
    val totalBytesDownloaded: Long = 0L,
    val totalBytesUploaded: Long = 0L,
    val totalBytesBurned: Long = 0L,
    val elapsedSeconds: Long = 0L,
    val targetDurationSeconds: Long = 0L,
    val remainingDurationSeconds: Long = 0L,
    val targetLimitBytes: Long = 0L,
    val currentPingMs: Double = 0.0,
    val minPingMs: Double = 0.0,
    val maxPingMs: Double = 0.0,
    val avgPingMs: Double = 0.0,
    val jitterMs: Double = 0.0,
    val packetLossPercent: Double = 0.0,
    val stabilityScorePercent: Double = 100.0,
    val loadedLatencyDeltaMs: Double = 0.0,
    val activeThreads: Int = 0,
    val routingMode: NetworkRoutingMode = NetworkRoutingMode.AUTO_ALL,
    val speedHistory: List<Float> = emptyList(),
    val dataUsageHistory: List<DataUsagePoint> = emptyList(),
    val limitReached: Boolean = false,
    val durationReached: Boolean = false,
    val stopReason: String? = null,
    val errorMessage: String? = null
)

class DataKillerEngine(private val scope: CoroutineScope) {

    private val _stats = MutableStateFlow(EngineStats())
    val stats: StateFlow<EngineStats> = _stats.asStateFlow()

    private val downloadedBytesAtomic = AtomicLong(0L)
    private val uploadedBytesAtomic = AtomicLong(0L)
    private val isPausedAtomic = AtomicBoolean(false)

    // Packet loss tracking
    private val totalProbesAtomic = AtomicInteger(0)
    private val failedProbesAtomic = AtomicInteger(0)

    private var workerJobs = mutableListOf<Job>()
    private var tickerJob: Job? = null
    private var pingJob: Job? = null

    private var sessionStartTime = 0L
    private var peakDownSpeed = 0.0
    private var peakUpSpeed = 0.0
    private var peakTotalSpeed = 0.0
    private var idleBaselinePing = 0.0

    private val pingReadings = mutableListOf<Double>()
    private val speedHistoryBuffer = mutableListOf<Float>()
    private val speedSamplesWindow = mutableListOf<Double>()
    private val dataUsagePoints = mutableListOf<DataUsagePoint>()

    private var boundNetwork: Network? = null
    private var activeHttpClient: OkHttpClient = createHttpClient(null)

    // High-capacity global CDN endpoints for raw stress throughput
    private val downloadEndpoints = listOf(
        "https://speed.cloudflare.com/__down?bytes=100000000",
        "https://speed.cloudflare.com/__down?bytes=50000000",
        "https://speed.cloudflare.com/__down?bytes=25000000",
        "https://proof.ovh.net/files/100Mb.dat",
        "https://speedtest.tele2.net/100MB.zip",
        "https://speedtest.tele2.net/50MB.zip"
    )

    private val uploadEndpoints = listOf(
        "https://speed.cloudflare.com/__up",
        "https://httpbin.org/post"
    )

    private fun createHttpClient(network: Network?): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)

        if (network != null) {
            builder.socketFactory(network.socketFactory)
        }

        return builder.build()
    }

    fun start(
        mode: StressMode = StressMode.DOWNLOAD,
        concurrency: Int = 8,
        targetLimitBytes: Long = 0L,
        targetDurationSeconds: Long = 0L,
        routingMode: NetworkRoutingMode = NetworkRoutingMode.AUTO_ALL,
        targetNetwork: Network? = null
    ) {
        if (_stats.value.isRunning) return

        downloadedBytesAtomic.set(0L)
        uploadedBytesAtomic.set(0L)
        totalProbesAtomic.set(0)
        failedProbesAtomic.set(0)
        isPausedAtomic.set(false)
        pingReadings.clear()
        speedHistoryBuffer.clear()
        speedSamplesWindow.clear()
        dataUsagePoints.clear()
        peakDownSpeed = 0.0
        peakUpSpeed = 0.0
        peakTotalSpeed = 0.0
        idleBaselinePing = 0.0
        sessionStartTime = System.currentTimeMillis()

        boundNetwork = targetNetwork
        activeHttpClient = createHttpClient(targetNetwork)

        _stats.value = EngineStats(
            isRunning = true,
            isPaused = false,
            activeThreads = concurrency,
            targetLimitBytes = targetLimitBytes,
            targetDurationSeconds = targetDurationSeconds,
            remainingDurationSeconds = targetDurationSeconds,
            routingMode = routingMode
        )

        // Launch workers based on mode
        val downloadThreads = when (mode) {
            StressMode.DOWNLOAD -> concurrency
            StressMode.UPLOAD -> 0
            StressMode.BI_DIRECTIONAL -> max(1, concurrency / 2)
        }

        val uploadThreads = when (mode) {
            StressMode.DOWNLOAD -> 0
            StressMode.UPLOAD -> concurrency
            StressMode.BI_DIRECTIONAL -> max(1, concurrency - downloadThreads)
        }

        workerJobs.clear()

        // Launch download streams
        for (i in 0 until downloadThreads) {
            val job = scope.launch(Dispatchers.IO) {
                runDownloadWorker(i, targetLimitBytes, targetDurationSeconds)
            }
            workerJobs.add(job)
        }

        // Launch upload streams
        for (i in 0 until uploadThreads) {
            val job = scope.launch(Dispatchers.IO) {
                runUploadWorker(i, targetLimitBytes, targetDurationSeconds)
            }
            workerJobs.add(job)
        }

        // Launch ticker for real-time speed calculation, stability scoring and time graph tracking
        startTicker(targetLimitBytes, targetDurationSeconds)

        // Launch continuous ping and packet loss monitor
        startPingMonitor()
    }

    fun pause() {
        if (!_stats.value.isRunning || _stats.value.isPaused) return
        isPausedAtomic.set(true)
        _stats.value = _stats.value.copy(isPaused = true)
    }

    fun resume() {
        if (!_stats.value.isRunning || !_stats.value.isPaused) return
        isPausedAtomic.set(false)
        _stats.value = _stats.value.copy(isPaused = false)
    }

    fun stop(reason: String = "MANUAL_STOP"): EngineStats {
        val finalState = _stats.value
        workerJobs.forEach { it.cancel() }
        workerJobs.clear()
        tickerJob?.cancel()
        tickerJob = null
        pingJob?.cancel()
        pingJob = null

        val stoppedState = finalState.copy(
            isRunning = false,
            isPaused = false,
            currentDownloadSpeedMbps = 0.0,
            currentUploadSpeedMbps = 0.0,
            currentTotalSpeedMbps = 0.0,
            stopReason = reason
        )
        _stats.value = stoppedState
        return stoppedState
    }

    private suspend fun runDownloadWorker(
        workerIndex: Int,
        targetLimitBytes: Long,
        targetDurationSeconds: Long
    ) {
        val buffer = ByteArray(128 * 1024) // 128 KB buffer

        while (scope.isActive) {
            if (isPausedAtomic.get()) {
                delay(200)
                continue
            }

            val elapsedSec = (System.currentTimeMillis() - sessionStartTime) / 1000L
            if (targetDurationSeconds > 0 && elapsedSec >= targetDurationSeconds) {
                break
            }

            if (targetLimitBytes > 0 && (downloadedBytesAtomic.get() + uploadedBytesAtomic.get()) >= targetLimitBytes) {
                break
            }

            val endpoint = downloadEndpoints[workerIndex % downloadEndpoints.size]
            val cacheBuster = "?nocache=${System.currentTimeMillis()}_${Random.nextInt(100000)}"
            val urlStr = if (endpoint.contains("?")) "$endpoint&cb=$cacheBuster" else "$endpoint$cacheBuster"

            try {
                val request = Request.Builder()
                    .url(urlStr)
                    .addHeader("Cache-Control", "no-cache, no-store")
                    .addHeader("Pragma", "no-cache")
                    .build()

                val response = activeHttpClient.newCall(request).execute()
                response.use { resp ->
                    if (resp.isSuccessful) {
                        val stream: InputStream = resp.body?.byteStream() ?: return@use
                        var bytesRead: Int
                        while (scope.isActive && !isPausedAtomic.get()) {
                            bytesRead = stream.read(buffer)
                            if (bytesRead == -1) break
                            downloadedBytesAtomic.addAndGet(bytesRead.toLong())

                            val curElapsed = (System.currentTimeMillis() - sessionStartTime) / 1000L
                            if (targetDurationSeconds > 0 && curElapsed >= targetDurationSeconds) {
                                return@use
                            }

                            if (targetLimitBytes > 0 &&
                                (downloadedBytesAtomic.get() + uploadedBytesAtomic.get()) >= targetLimitBytes
                            ) {
                                return@use
                            }
                        }
                    } else {
                        failedProbesAtomic.incrementAndGet()
                    }
                }
            } catch (_: CancellationException) {
                break
            } catch (_: Exception) {
                failedProbesAtomic.incrementAndGet()
                delay(300)
            }
        }
    }

    private suspend fun runUploadWorker(
        workerIndex: Int,
        targetLimitBytes: Long,
        targetDurationSeconds: Long
    ) {
        val chunkSize = 512 * 1024 // 512 KB upload payloads
        val dummyData = ByteArray(chunkSize)
        Random.nextBytes(dummyData)

        val customBody = object : RequestBody() {
            override fun contentType() = "application/octet-stream".toMediaType()
            override fun contentLength() = chunkSize.toLong()

            override fun writeTo(sink: BufferedSink) {
                var offset = 0
                val subChunk = 64 * 1024
                while (offset < chunkSize && scope.isActive && !isPausedAtomic.get()) {
                    val len = min(subChunk, chunkSize - offset)
                    sink.write(dummyData, offset, len)
                    offset += len
                    uploadedBytesAtomic.addAndGet(len.toLong())
                }
            }
        }

        while (scope.isActive) {
            if (isPausedAtomic.get()) {
                delay(200)
                continue
            }

            val elapsedSec = (System.currentTimeMillis() - sessionStartTime) / 1000L
            if (targetDurationSeconds > 0 && elapsedSec >= targetDurationSeconds) {
                break
            }

            if (targetLimitBytes > 0 && (downloadedBytesAtomic.get() + uploadedBytesAtomic.get()) >= targetLimitBytes) {
                break
            }

            val endpoint = uploadEndpoints[workerIndex % uploadEndpoints.size]

            try {
                val request = Request.Builder()
                    .url(endpoint)
                    .post(customBody)
                    .addHeader("Cache-Control", "no-cache")
                    .build()

                val response = activeHttpClient.newCall(request).execute()
                response.close()
            } catch (_: CancellationException) {
                break
            } catch (_: Exception) {
                failedProbesAtomic.incrementAndGet()
                delay(300)
            }
        }
    }

    private fun startTicker(targetLimitBytes: Long, targetDurationSeconds: Long) {
        tickerJob = scope.launch(Dispatchers.Default) {
            var lastDownBytes = 0L
            var lastUpBytes = 0L
            var lastTime = System.currentTimeMillis()
            var smoothDownSpeed = 0.0
            var smoothUpSpeed = 0.0
            var lastDataPointSec = -1L

            while (isActive) {
                delay(350)
                if (isPausedAtomic.get()) continue

                val now = System.currentTimeMillis()
                val dtSec = max(0.001, (now - lastTime) / 1000.0)
                val currentDownBytes = downloadedBytesAtomic.get()
                val currentUpBytes = uploadedBytesAtomic.get()

                val deltaDown = max(0L, currentDownBytes - lastDownBytes)
                val deltaUp = max(0L, currentUpBytes - lastUpBytes)

                val rawDownMbps = (deltaDown * 8.0) / (dtSec * 1_000_000.0)
                val rawUpMbps = (deltaUp * 8.0) / (dtSec * 1_000_000.0)

                // EMA smoothing
                smoothDownSpeed = if (smoothDownSpeed == 0.0) rawDownMbps else (smoothDownSpeed * 0.4 + rawDownMbps * 0.6)
                smoothUpSpeed = if (smoothUpSpeed == 0.0) rawUpMbps else (smoothUpSpeed * 0.4 + rawUpMbps * 0.6)
                val totalCurrentSpeed = smoothDownSpeed + smoothUpSpeed

                if (smoothDownSpeed > peakDownSpeed) peakDownSpeed = smoothDownSpeed
                if (smoothUpSpeed > peakUpSpeed) peakUpSpeed = smoothUpSpeed
                if (totalCurrentSpeed > peakTotalSpeed) peakTotalSpeed = totalCurrentSpeed

                val totalBytes = currentDownBytes + currentUpBytes
                val elapsedSec = max(1L, (now - sessionStartTime) / 1000L)
                val avgSpeed = (totalBytes * 8.0) / (elapsedSec * 1_000_000.0)

                // Record rolling speed samples for Consistency / Stability Calculation
                speedSamplesWindow.add(totalCurrentSpeed)
                if (speedSamplesWindow.size > 40) {
                    speedSamplesWindow.removeAt(0)
                }

                // Stability score = 100% - (StdDev / Mean * 100%)
                val stabilityScore = calculateStabilityScore(speedSamplesWindow, avgSpeed)

                // Add to waveform buffer
                speedHistoryBuffer.add(totalCurrentSpeed.toFloat())
                if (speedHistoryBuffer.size > 60) {
                    speedHistoryBuffer.removeAt(0)
                }

                // Add to Data Consumed Over Time points every second
                if (elapsedSec != lastDataPointSec) {
                    lastDataPointSec = elapsedSec
                    dataUsagePoints.add(
                        DataUsagePoint(
                            timestampSec = elapsedSec,
                            totalBytes = totalBytes,
                            downloadBytes = currentDownBytes,
                            uploadBytes = currentUpBytes,
                            speedMbps = totalCurrentSpeed
                        )
                    )
                    if (dataUsagePoints.size > 120) {
                        dataUsagePoints.removeAt(0)
                    }
                }

                // Packet loss
                val totalP = totalProbesAtomic.get()
                val failedP = failedProbesAtomic.get()
                val lossPercent = if (totalP > 0) ((failedP.toDouble() / totalP.toDouble()) * 100.0).coerceIn(0.0, 100.0) else 0.0

                // Remaining duration & limit checks
                val remainingSec = if (targetDurationSeconds > 0) max(0L, targetDurationSeconds - elapsedSec) else 0L
                val limitReached = targetLimitBytes > 0 && totalBytes >= targetLimitBytes
                val durationReached = targetDurationSeconds > 0 && elapsedSec >= targetDurationSeconds

                val currentPing = _stats.value.currentPingMs
                val loadedDelta = if (idleBaselinePing > 0.0 && currentPing > 0.0) {
                    max(0.0, currentPing - idleBaselinePing)
                } else 0.0

                _stats.value = _stats.value.copy(
                    currentDownloadSpeedMbps = smoothDownSpeed,
                    currentUploadSpeedMbps = smoothUpSpeed,
                    currentTotalSpeedMbps = totalCurrentSpeed,
                    peakDownloadSpeedMbps = peakDownSpeed,
                    peakUploadSpeedMbps = peakUpSpeed,
                    peakTotalSpeedMbps = peakTotalSpeed,
                    avgSpeedMbps = avgSpeed,
                    totalBytesDownloaded = currentDownBytes,
                    totalBytesUploaded = currentUpBytes,
                    totalBytesBurned = totalBytes,
                    elapsedSeconds = elapsedSec,
                    remainingDurationSeconds = remainingSec,
                    speedHistory = speedHistoryBuffer.toList(),
                    dataUsageHistory = dataUsagePoints.toList(),
                    packetLossPercent = lossPercent,
                    stabilityScorePercent = stabilityScore,
                    loadedLatencyDeltaMs = loadedDelta,
                    limitReached = limitReached,
                    durationReached = durationReached,
                    stopReason = when {
                        limitReached -> "QUOTA_REACHED"
                        durationReached -> "DURATION_REACHED"
                        else -> null
                    }
                )

                if (limitReached || durationReached) {
                    stop(reason = if (limitReached) "QUOTA_REACHED" else "DURATION_REACHED")
                    break
                }

                lastDownBytes = currentDownBytes
                lastUpBytes = currentUpBytes
                lastTime = now
            }
        }
    }

    private fun calculateStabilityScore(samples: List<Double>, mean: Double): Double {
        if (samples.size < 4 || mean <= 0.1) return 100.0
        val variance = samples.sumOf { (it - mean).pow(2.0) } / samples.size
        val stdDev = sqrt(variance)
        val coeffOfVariation = stdDev / mean
        val score = (1.0 - coeffOfVariation) * 100.0
        return score.coerceIn(5.0, 100.0)
    }

    private fun startPingMonitor() {
        pingJob = scope.launch(Dispatchers.IO) {
            val pingTargets = listOf(
                Pair("1.1.1.1", 53),
                Pair("8.8.8.8", 53),
                Pair("speed.cloudflare.com", 443)
            )
            var targetIndex = 0

            while (isActive) {
                if (!isPausedAtomic.get()) {
                    val (host, port) = pingTargets[targetIndex % pingTargets.size]
                    targetIndex++

                    totalProbesAtomic.incrementAndGet()
                    val pingMs = measureSocketPing(host, port)
                    if (pingMs > 0) {
                        if (idleBaselinePing == 0.0) {
                            idleBaselinePing = pingMs
                        }
                        pingReadings.add(pingMs)
                        if (pingReadings.size > 60) {
                            pingReadings.removeAt(0)
                        }

                        val minP = pingReadings.minOrNull() ?: pingMs
                        val maxP = pingReadings.maxOrNull() ?: pingMs
                        val avgP = pingReadings.average()

                        // Jitter = Mean deviation of consecutive measurements
                        var jitter = 0.0
                        if (pingReadings.size > 1) {
                            var diffSum = 0.0
                            for (i in 1 until pingReadings.size) {
                                diffSum += abs(pingReadings[i] - pingReadings[i - 1])
                            }
                            jitter = diffSum / (pingReadings.size - 1)
                        }

                        _stats.value = _stats.value.copy(
                            currentPingMs = pingMs,
                            minPingMs = minP,
                            maxPingMs = maxP,
                            avgPingMs = avgP,
                            jitterMs = jitter
                        )
                    } else {
                        failedProbesAtomic.incrementAndGet()
                    }
                }
                delay(800)
            }
        }
    }

    private fun measureSocketPing(host: String, port: Int): Double {
        return try {
            val start = System.nanoTime()
            val socket = if (boundNetwork != null) {
                boundNetwork!!.socketFactory.createSocket()
            } else {
                Socket()
            }
            socket.connect(InetSocketAddress(host, port), 2000)
            val durationNs = System.nanoTime() - start
            socket.close()
            durationNs / 1_000_000.0
        } catch (_: Exception) {
            try {
                val start = System.nanoTime()
                val url = URL("https://1.1.1.1/cdn-cgi/trace")
                val conn = (if (boundNetwork != null) boundNetwork!!.openConnection(url) else url.openConnection()) as HttpURLConnection
                conn.connectTimeout = 2000
                conn.readTimeout = 2000
                conn.requestMethod = "HEAD"
                conn.responseCode
                conn.disconnect()
                (System.nanoTime() - start) / 1_000_000.0
            } catch (_: Exception) {
                -1.0
            }
        }
    }
}
