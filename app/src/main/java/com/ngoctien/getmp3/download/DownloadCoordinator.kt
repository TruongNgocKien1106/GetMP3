package com.ngoctien.getmp3.download

import android.content.Context
import android.util.Log
import com.ngoctien.getmp3.data.DownloadJobEntity
import com.ngoctien.getmp3.data.DownloadRepository
import com.ngoctien.getmp3.model.DownloadStatus
import com.ngoctien.getmp3.python.DownloadResult
import com.ngoctien.getmp3.python.PythonProgressCallback
import com.ngoctien.getmp3.python.TagWriteResult
import com.ngoctien.getmp3.python.VideoInfoResult
import com.ngoctien.getmp3.python.YtDlpBridge
import com.ngoctien.getmp3.settings.AppSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class DownloadCoordinator(
    context: Context,
    private val repository: DownloadRepository,
    private val onJobUpdated: (DownloadJobEntity) -> Unit
) {
    companion object {
        private const val TAG = "DownloadCoordinator"
    }

    private val applicationContext =
        context.applicationContext

    private val bridge =
        YtDlpBridge(applicationContext)

    private val ffmpegProcessor =
        FfmpegProcessor(applicationContext)

    private val coverProcessor =
        CoverProcessor()

    private val mediaStoreWriter =
        MediaStoreWriter(applicationContext)

    private val settingsRepository =
        AppSettingsRepository(applicationContext)

    private val cancelRequested =
        AtomicBoolean(false)

    @Volatile
    private var currentJobId: String? = null

    fun cancelJob(jobId: String) {
        if (currentJobId != jobId) {
            return
        }

        cancelRequested.set(true)
        ffmpegProcessor.cancel()
    }

    fun shutdown() {
        cancelRequested.set(true)
        ffmpegProcessor.cancel()
    }

    suspend fun processJob(
        jobId: String
    ): DownloadJobEntity? = coroutineScope {
        val initialJob = repository.getJobById(jobId)
            ?: return@coroutineScope null

        val appSettings =
            settingsRepository.getSettings()

        currentJobId = jobId
        cancelRequested.set(false)

        val jobDirectory = File(
            applicationContext.cacheDir,
            "getmp3_cache/$jobId"
        )

        if (jobDirectory.exists()) {
            jobDirectory.deleteRecursively()
        }

        if (!jobDirectory.mkdirs()) {
            Log.w(
                TAG,
                "Không tạo được thư mục cache mới: ${jobDirectory.absolutePath}"
            )
        }

        var currentJob = initialJob

        val updateMutex = Mutex()
        val warnings = linkedSetOf<String>()

        suspend fun mutateJob(
            transform: (DownloadJobEntity) -> DownloadJobEntity
        ): DownloadJobEntity {
            updateMutex.lock()

            val snapshot: DownloadJobEntity

            try {
                currentJob = transform(currentJob)
                repository.updateJob(currentJob)
                snapshot = currentJob
            } finally {
                updateMutex.unlock()
            }

            runCatching {
                onJobUpdated(snapshot)
            }

            return snapshot
        }

        suspend fun updateStage(
            status: DownloadStatus,
            stageProgress: Int,
            overallProgress: Int,
            message: String
        ): DownloadJobEntity {
            val now = System.currentTimeMillis()

            return mutateJob { job ->
                job.copy(
                    status = status,
                    stageProgress = stageProgress,
                    overallProgress = overallProgress,
                    stageStartedAt = now,
                    lastProgressAt = now,
                    statusMessage = message,
                    errorMessage = null,
                    updatedAt = now
                )
            }
        }

        fun checkCancelled() {
            if (cancelRequested.get()) {
                throw JobCancelledException()
            }
        }

        val downloadProgressChannel =
            Channel<DownloadTelemetry>(
                capacity = Channel.CONFLATED
            )

        val downloadProgressCollector = launch(
            Dispatchers.IO
        ) {
            var lastPersistAt = 0L

            for (telemetry in downloadProgressChannel) {
                val now = System.currentTimeMillis()

                if (
                    telemetry.percent < 100 &&
                    now - lastPersistAt < 400L
                ) {
                    continue
                }

                lastPersistAt = now

                mutateJob { job ->
                    job.copy(
                        stageProgress =
                            telemetry.percent.coerceIn(0, 100),
                        overallProgress = (
                            5 +
                                telemetry.percent
                                    .coerceIn(0, 100) *
                                65 /
                                100
                            ),
                        downloadedBytes =
                            telemetry.downloadedBytes,
                        totalBytes =
                            telemetry.totalBytes,
                        speedBytesPerSecond =
                            telemetry.speedBytesPerSecond,
                        etaSeconds =
                            telemetry.etaSeconds,
                        lastProgressAt = now,
                        statusMessage =
                            "Đang tải âm thanh...",
                        updatedAt = now
                    )
                }
            }
        }

        var resultJob: DownloadJobEntity? = null

        try {
            mutateJob { job ->
                val now = System.currentTimeMillis()

                job.copy(
                    stageStartedAt = now,
                    lastProgressAt = now,
                    warningMessage = null,
                    errorMessage = null,
                    outputUri = null,
                    updatedAt = now
                )
            }

            if (
                currentJob.title.isBlank() ||
                currentJob.title == "Đang lấy thông tin..." ||
                currentJob.title == "Unknown Title"
            ) {
                updateStage(
                    status = DownloadStatus.EXTRACTING,
                    stageProgress = 0,
                    overallProgress = 0,
                    message = "Đang lấy thông tin..."
                )

                val infoResult = withContext(
                    Dispatchers.IO
                ) {
                    bridge.extractVideoInfo(
                        currentJob.url
                    )
                }

                checkCancelled()

                when (infoResult) {
                    is VideoInfoResult.Success -> {
                        mutateJob { job ->
                            job.copy(
                                title =
                                    infoResult.info.title,
                                artist =
                                    infoResult.info.artist,
                                thumbnailUrl =
                                    infoResult.info.thumbnailUrl,
                                durationSeconds =
                                    infoResult.info.durationSeconds,
                                stageProgress = 100,
                                overallProgress = 5,
                                statusMessage =
                                    "Đã lấy thông tin",
                                updatedAt =
                                    System.currentTimeMillis()
                            )
                        }
                    }

                    is VideoInfoResult.Error -> {
                        throw IllegalStateException(
                            "Không lấy được thông tin: " +
                                infoResult.message
                        )
                    }
                }
            }

            checkCancelled()

            updateStage(
                status = DownloadStatus.DOWNLOADING,
                stageProgress = 0,
                overallProgress = 5,
                message = "Đang tải âm thanh..."
            )

            val callback =
                object : PythonProgressCallback {
                    override fun onProgress(
                        percent: Int,
                        downloadedBytes: Long,
                        totalBytes: Long,
                        speed: Long,
                        eta: Long
                    ) {
                        downloadProgressChannel.trySend(
                            DownloadTelemetry(
                                percent = percent,
                                downloadedBytes =
                                    downloadedBytes,
                                totalBytes = totalBytes,
                                speedBytesPerSecond = speed,
                                etaSeconds = eta
                            )
                        )
                    }

                    override fun isCancelled(): Boolean {
                        return cancelRequested.get()
                    }
                }

            val downloadResult = withContext(
                Dispatchers.IO
            ) {
                bridge.downloadAudio(
                    url = currentJob.url,
                    outputDirectory =
                        jobDirectory.absolutePath,
                    jobId = jobId,
                    callback = callback
                )
            }

            downloadProgressChannel.close()
            downloadProgressCollector.join()

            checkCancelled()

            val downloadedAudio: File
            val rawCover: File?

            when (downloadResult) {
                is DownloadResult.Success -> {
                    downloadedAudio = File(
                        downloadResult.audioPath
                    )

                    rawCover =
                        downloadResult.coverPath
                            ?.let(::File)
                            ?.takeIf {
                                it.isFile &&
                                    it.length() > 0L
                            }

                    downloadResult.coverWarning
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?.let(warnings::add)
                }

                is DownloadResult.Error -> {
                    if (
                        downloadResult.message ==
                        "CANCELLED"
                    ) {
                        throw JobCancelledException()
                    }

                    throw IllegalStateException(
                        "Tải âm thanh thất bại: " +
                            downloadResult.message
                    )
                }
            }

            if (
                !downloadedAudio.isFile ||
                downloadedAudio.length() <= 0L
            ) {
                throw IllegalStateException(
                    "File âm thanh tải về không hợp lệ"
                )
            }

            checkCancelled()

            updateStage(
                status = DownloadStatus.CONVERTING,
                stageProgress = 0,
                overallProgress = 70,
                message = "Đang chuyển đổi MP3 ${appSettings.bitrateKbps} kbps..."
            )

            val mp3File = File(
                jobDirectory,
                "output.mp3"
            )

            ffmpegProcessor.convertToMp3(
                inputFile = downloadedAudio,
                outputFile = mp3File,
                durationSeconds =
                    currentJob.durationSeconds,
                bitrateKbps =
                    appSettings.bitrateKbps,
                isCancelled = {
                    cancelRequested.get()
                },
                onProgress = { progress ->
                    val now =
                        System.currentTimeMillis()

                    mutateJob { job ->
                        job.copy(
                            status =
                                DownloadStatus.CONVERTING,
                            stageProgress =
                                progress.percent,
                            overallProgress = (
                                70 +
                                    progress.percent *
                                    22 /
                                    100
                                ),
                            processedSeconds =
                                progress.processedSeconds,
                            ffmpegSpeed =
                                progress.speed,
                            lastProgressAt = now,
                            statusMessage =
                                "Đang chuyển đổi MP3 ${appSettings.bitrateKbps} kbps...",
                            updatedAt = now
                        )
                    }
                }
            )

            checkCancelled()

            if (
                !mp3File.isFile ||
                mp3File.length() <= 0L
            ) {
                throw IllegalStateException(
                    "File MP3 sau chuyển đổi không hợp lệ"
                )
            }

            updateStage(
                status = DownloadStatus.TAGGING,
                stageProgress = 0,
                overallProgress = 92,
                message = "Đang xử lý ảnh bìa..."
            )

            val coverResult =
                coverProcessor.convertToJpeg(
                    sourceFile = rawCover,
                    destinationFile = File(
                        jobDirectory,
                        "cover.jpg"
                    )
                )

            coverResult.warning
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let(warnings::add)

            checkCancelled()

            mutateJob { job ->
                job.copy(
                    statusMessage =
                        "Đang ghi ID3v2.3...",
                    stageProgress = 50,
                    overallProgress = 94,
                    updatedAt =
                        System.currentTimeMillis()
                )
            }

            val tagResult = withContext(
                Dispatchers.IO
            ) {
                bridge.writeId3Tags(
                    mp3Path = mp3File.absolutePath,
                    title = currentJob.title,
                    artist = currentJob.artist,
                    coverPath =
                        coverResult.jpegFile
                            ?.absolutePath
                )
            }

            when (tagResult) {
                is TagWriteResult.Success -> {
                    if (
                        tagResult.id3Version
                            .startsWith("2.3")
                            .not()
                    ) {
                        throw IllegalStateException(
                            "Tag sau khi ghi không phải ID3v2.3"
                        )
                    }

                    val invalidFrames =
                        tagResult.frames.filterNot {
                            it in setOf(
                                "TIT2",
                                "TPE1",
                                "APIC"
                            )
                        }

                    if (invalidFrames.isNotEmpty()) {
                        throw IllegalStateException(
                            "File còn metadata không cho phép: " +
                                invalidFrames.joinToString()
                        )
                    }

                    if (
                        coverResult.jpegFile != null &&
                        !tagResult.coverEmbedded
                    ) {
                        warnings.add(
                            "Không nhúng được ảnh bìa"
                        )
                    }

                    tagResult.warning
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?.let(warnings::add)
                }

                is TagWriteResult.Error -> {
                    Log.e(
                        TAG,
                        "Mutagen error: ${tagResult.technicalDetails}"
                    )

                    throw IllegalStateException(
                        "Không ghi được ID3v2.3: " +
                            tagResult.message
                    )
                }
            }

            checkCancelled()

            updateStage(
                status = DownloadStatus.SAVING,
                stageProgress = 0,
                overallProgress = 96,
                message = "Đang lưu vào Music/GetMP3..."
            )

            val savedAudio =
                mediaStoreWriter.saveMp3(
                    sourceFile = mp3File,
                    title = currentJob.title,
                    artist = currentJob.artist,
                    settings = appSettings
                )

            resultJob = mutateJob { job ->
                val warningText = warnings
                    .takeIf {
                        it.isNotEmpty()
                    }
                    ?.joinToString(" • ")

                job.copy(
                    status = DownloadStatus.COMPLETED,
                    stageProgress = 100,
                    overallProgress = 100,
                    statusMessage = "Hoàn tất",
                    warningMessage = warningText,
                    errorMessage = null,
                    outputUri =
                        savedAudio.uri.toString(),
                    lastProgressAt =
                        System.currentTimeMillis(),
                    updatedAt =
                        System.currentTimeMillis()
                )
            }
        } catch (exception: Exception) {
            val cancelled =
                exception is JobCancelledException ||
                    exception is FfmpegCancelledException ||
                    cancelRequested.get()

            if (cancelled) {
                resultJob = mutateJob { job ->
                    job.copy(
                        status =
                            DownloadStatus.CANCELLED,
                        statusMessage = "Đã hủy",
                        warningMessage = null,
                        errorMessage = null,
                        updatedAt =
                            System.currentTimeMillis()
                    )
                }
            } else {
                val friendlyMessage =
                    friendlyError(exception)

                Log.e(
                    TAG,
                    "Job $jobId failed",
                    exception
                )

                if (
                    exception is FfmpegExecutionException
                ) {
                    Log.e(
                        TAG,
                        exception.technicalDetails
                    )
                }

                resultJob = mutateJob { job ->
                    job.copy(
                        status = DownloadStatus.FAILED,
                        statusMessage = "Thất bại",
                        warningMessage = null,
                        errorMessage = friendlyMessage,
                        updatedAt =
                            System.currentTimeMillis()
                    )
                }
            }
        } finally {
            downloadProgressChannel.close()

            joinAll(
                downloadProgressCollector
            )

            runCatching {
                jobDirectory.deleteRecursively()
            }

            currentJobId = null
            cancelRequested.set(false)
        }

        resultJob
    }

    private fun friendlyError(
        exception: Exception
    ): String {
        val message = exception.message
            ?.trim()
            .orEmpty()

        val lower = message.lowercase()

        return when {
            "private video" in lower ||
                "video is private" in lower -> {
                "Video đang ở chế độ riêng tư"
            }

            "video unavailable" in lower ||
                "this video is unavailable" in lower -> {
                "Video không khả dụng hoặc đã bị xóa"
            }

            "sign in" in lower ||
                "login" in lower -> {
                "Video yêu cầu đăng nhập"
            }

            "geo" in lower ||
                "country" in lower ||
                "region" in lower -> {
                "Video bị giới hạn khu vực"
            }

            "no space" in lower ||
                "enospc" in lower -> {
                "Thiết bị không còn đủ dung lượng"
            }

            message.isBlank() -> {
                "Đã xảy ra lỗi không xác định"
            }

            else -> message.take(300)
        }
    }

    private data class DownloadTelemetry(
        val percent: Int,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val speedBytesPerSecond: Long,
        val etaSeconds: Long
    )

    private class JobCancelledException :
        Exception("Đã hủy")
}