package com.ngoctien.getmp3.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ngoctien.getmp3.data.AppDatabase
import com.ngoctien.getmp3.data.DownloadJobEntity
import com.ngoctien.getmp3.data.DownloadRepository
import com.ngoctien.getmp3.download.DownloadService
import com.ngoctien.getmp3.download.FfmpegProcessor
import com.ngoctien.getmp3.model.DownloadStatus
import com.ngoctien.getmp3.python.VideoInfoResult
import com.ngoctien.getmp3.python.YtDlpBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

enum class FfmpegReadyState {
    CHECKING,
    READY,
    FAILED
}

data class DownloadScreenUiState(
    val isPreparingDownload: Boolean = false,
    val preparingMessage: String? = null,

    val ffmpegState: FfmpegReadyState =
        FfmpegReadyState.CHECKING,

    val ffmpegMessage: String =
        "Đang kiểm tra bộ chuyển đổi...",

    val activeJobs: List<DownloadJobEntity> =
        emptyList(),

    val recentJobs: List<DownloadJobEntity> =
        emptyList()
)

data class UiEvent(
    val message: String
)

class DownloadViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        DownloadRepository(
            AppDatabase
                .getDatabase(application)
                .downloadJobDao()
        )

    private val bridge by lazy {
        YtDlpBridge(application)
    }

    private val ffmpegProcessor =
        FfmpegProcessor(application)

    private val mutableUiState =
        MutableStateFlow(
            DownloadScreenUiState()
        )

    val uiState: StateFlow<DownloadScreenUiState> =
        mutableUiState.asStateFlow()

    private val mutableEvents =
        MutableSharedFlow<UiEvent>(
            extraBufferCapacity = 8
        )

    val events: SharedFlow<UiEvent> =
        mutableEvents.asSharedFlow()

    init {
        observeJobs()
        checkFfmpeg()
    }

    fun pasteAndDownload(
        clipboardText: CharSequence?
    ) {
        if (
            mutableUiState.value
                .isPreparingDownload
        ) {
            return
        }

        val rawClipboardText =
            clipboardText
                ?.toString()
                ?.trim()
                .orEmpty()

        val normalizedUrl =
            try {
                normalizeYouTubeVideoUrl(
                    rawClipboardText
                )
            } catch (
                exception: Exception
            ) {
                mutableEvents.tryEmit(
                    UiEvent(
                        exception.message
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: "Clipboard không chứa liên kết YouTube hợp lệ"
                    )
                )

                return
            }

        if (
            mutableUiState.value.ffmpegState !=
            FfmpegReadyState.READY
        ) {
            mutableEvents.tryEmit(
                UiEvent(
                    "Bộ chuyển đổi MP3 chưa sẵn sàng"
                )
            )

            return
        }

        viewModelScope.launch {
            mutableUiState.update {
                it.copy(
                    isPreparingDownload =
                        true,

                    preparingMessage =
                        "Đang đọc thông tin bài hát..."
                )
            }

            try {
                val duplicated =
                    withContext(
                        Dispatchers.IO
                    ) {
                        repository
                            .hasActiveJobForUrl(
                                normalizedUrl
                            )
                    }

                if (duplicated) {
                    mutableEvents.emit(
                        UiEvent(
                            "Video này đã có trong hàng đợi"
                        )
                    )

                    return@launch
                }

                val infoResult =
                    withContext(
                        Dispatchers.IO
                    ) {
                        bridge.extractVideoInfo(
                            normalizedUrl
                        )
                    }

                val videoInfo =
                    when (infoResult) {
                        is VideoInfoResult.Success -> {
                            infoResult.info
                        }

                        is VideoInfoResult.Error -> {
                            mutableEvents.emit(
                                UiEvent(
                                    friendlyMetadataError(
                                        infoResult.message
                                    )
                                )
                            )

                            return@launch
                        }
                    }

                mutableUiState.update {
                    it.copy(
                        preparingMessage =
                            "Đang thêm vào hàng đợi..."
                    )
                }

                val now =
                    System.currentTimeMillis()

                val job =
                    DownloadJobEntity(
                        id =
                            UUID.randomUUID()
                                .toString(),

                        /*
                         * Luôn lưu URL chuẩn để:
                         * - loại tham số playlist;
                         * - tránh một video bị thêm nhiều lần;
                         * - bảo đảm yt-dlp chỉ nhận video đơn.
                         */
                        url =
                            normalizedUrl,

                        title =
                            videoInfo.title,

                        artist =
                            videoInfo.artist,

                        thumbnailUrl =
                            videoInfo.thumbnailUrl,

                        status =
                            DownloadStatus.QUEUED,

                        stageProgress = 0,
                        overallProgress = 0,

                        downloadedBytes = 0L,
                        totalBytes = 0L,
                        speedBytesPerSecond = 0L,
                        etaSeconds = 0L,

                        durationSeconds =
                            videoInfo.durationSeconds,

                        processedSeconds = 0L,
                        ffmpegSpeed = 0f,

                        stageStartedAt = now,
                        lastProgressAt = now,

                        statusMessage =
                            "Đang chờ",

                        warningMessage = null,
                        errorMessage = null,

                        outputUri = null,

                        createdAt = now,
                        updatedAt = now
                    )

                withContext(
                    Dispatchers.IO
                ) {
                    repository.insertJob(
                        job
                    )
                }

                startDownloadService()

                mutableEvents.emit(
                    UiEvent(
                        "Đã thêm “${videoInfo.title}”"
                    )
                )
            } catch (
                exception: Exception
            ) {
                mutableEvents.emit(
                    UiEvent(
                        friendlyMetadataError(
                            exception.message
                                ?: "Không thể chuẩn bị tải video"
                        )
                    )
                )
            } finally {
                mutableUiState.update {
                    it.copy(
                        isPreparingDownload =
                            false,

                        preparingMessage =
                            null
                    )
                }
            }
        }
    }
    fun cancelJob(
        jobId: String
    ) {
        val intent = Intent(
            getApplication(),
            DownloadService::class.java
        ).apply {
            action =
                DownloadService.ACTION_CANCEL_JOB

            putExtra(
                DownloadService.EXTRA_JOB_ID,
                jobId
            )
        }

        ContextCompat.startForegroundService(
            getApplication(),
            intent
        )
    }

    fun retryJob(
        originalJob: DownloadJobEntity
    ) {
        if (
            mutableUiState.value.ffmpegState !=
            FfmpegReadyState.READY
        ) {
            mutableEvents.tryEmit(
                UiEvent(
                    "Bộ chuyển đổi MP3 chưa sẵn sàng"
                )
            )

            return
        }

        viewModelScope.launch(
            Dispatchers.IO
        ) {
            if (
                repository.hasActiveJobForUrl(
                    originalJob.url
                )
            ) {
                mutableEvents.emit(
                    UiEvent(
                        "Liên kết này đang được xử lý"
                    )
                )

                return@launch
            }

            val now =
                System.currentTimeMillis()

            val retryJob =
                originalJob.copy(
                    status =
                        DownloadStatus.QUEUED,

                    stageProgress = 0,
                    overallProgress = 0,

                    downloadedBytes = 0L,
                    totalBytes = 0L,
                    speedBytesPerSecond = 0L,
                    etaSeconds = 0L,

                    processedSeconds = 0L,
                    ffmpegSpeed = 0f,

                    stageStartedAt = now,
                    lastProgressAt = now,

                    statusMessage =
                        "Đang chờ",

                    warningMessage = null,
                    errorMessage = null,

                    outputUri = null,

                    createdAt = now,
                    updatedAt = now
                )

            repository.updateJob(retryJob)
            startDownloadService()

            mutableEvents.emit(
                UiEvent(
                    "Đã tải lại “${originalJob.title}”"
                )
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch(
            Dispatchers.IO
        ) {
            repository.deleteFinishedJobs()

            mutableEvents.emit(
                UiEvent(
                    "Đã xóa lịch sử tải"
                )
            )
        }
    }

    fun checkFfmpeg() {
        viewModelScope.launch {
            mutableUiState.update {
                it.copy(
                    ffmpegState =
                        FfmpegReadyState.CHECKING,

                    ffmpegMessage =
                        "Đang kiểm tra bộ chuyển đổi..."
                )
            }

            val result =
                ffmpegProcessor
                    .verifyInstallation()

            mutableUiState.update {
                it.copy(
                    ffmpegState = if (
                        result.ready
                    ) {
                        FfmpegReadyState.READY
                    } else {
                        FfmpegReadyState.FAILED
                    },

                    ffmpegMessage =
                        result.message
                )
            }
        }
    }

    private fun observeJobs() {
        viewModelScope.launch {
            repository.observeAllJobs()
                .collect { allJobs ->
                    val activeStatuses =
                        setOf(
                            DownloadStatus.QUEUED,
                            DownloadStatus.EXTRACTING,
                            DownloadStatus.DOWNLOADING,
                            DownloadStatus.CONVERTING,
                            DownloadStatus.TAGGING,
                            DownloadStatus.SAVING
                        )

                    val activeJobs =
                        allJobs
                            .filter {
                                it.status in
                                    activeStatuses
                            }
                            .sortedWith(
                                compareBy<
                                    DownloadJobEntity
                                    > {
                                    statusPriority(
                                        it.status
                                    )
                                }.thenBy {
                                    it.createdAt
                                }
                            )

                    val recentJobs =
                        allJobs
                            .filter {
                                it.status !in
                                    activeStatuses
                            }
                            .sortedByDescending {
                                it.updatedAt
                            }

                    mutableUiState.update {
                        it.copy(
                            activeJobs =
                                activeJobs,

                            recentJobs =
                                recentJobs
                        )
                    }
                }
        }
    }

    private fun startDownloadService() {
        val intent = Intent(
            getApplication(),
            DownloadService::class.java
        )

        ContextCompat.startForegroundService(
            getApplication(),
            intent
        )
    }

    private fun normalizeYouTubeVideoUrl(
        clipboardValue: String
    ): String {
        if (clipboardValue.isBlank()) {
            throw IllegalArgumentException(
                "Clipboard không có liên kết"
            )
        }

        /*
         * Hỗ trợ clipboard chứa thêm lời giới thiệu,
         * ví dụ: "Xem video này https://youtu.be/..."
         */
        val extractedUrl =
            Regex(
                pattern =
                    """https?://[^\s]+""",

                option =
                    RegexOption.IGNORE_CASE
            )
                .find(
                    clipboardValue
                )
                ?.value
                ?.trim()
                ?.trimEnd(
                    '.',
                    ',',
                    ';',
                    ')',
                    ']',
                    '}',
                    '>',
                    '"',
                    '\''
                )
                ?: clipboardValue
                    .trim()

        val uri =
            try {
                Uri.parse(
                    extractedUrl
                )
            } catch (
                exception: Exception
            ) {
                throw IllegalArgumentException(
                    "Clipboard không chứa URL hợp lệ",

                    exception
                )
            }

        val scheme =
            uri.scheme
                ?.lowercase(
                    Locale.US
                )

        if (
            scheme != "https" &&
            scheme != "http"
        ) {
            throw IllegalArgumentException(
                "Clipboard không chứa URL hợp lệ"
            )
        }

        val host =
            uri.host
                ?.lowercase(
                    Locale.US
                )
                ?.trimEnd('.')
                ?: throw IllegalArgumentException(
                    "URL không có tên miền"
                )

        val shortHost =
            host == "youtu.be" ||
                host == "www.youtu.be"

        val standardHost =
            host == "youtube.com" ||
                host == "www.youtube.com" ||
                host == "m.youtube.com" ||
                host == "music.youtube.com" ||
                host == "youtube-nocookie.com" ||
                host ==
                    "www.youtube-nocookie.com"

        if (
            !shortHost &&
            !standardHost
        ) {
            throw IllegalArgumentException(
                "Clipboard không chứa liên kết YouTube"
            )
        }

        val pathSegments =
            uri.pathSegments
                .map {
                    it.trim()
                }
                .filter {
                    it.isNotBlank()
                }

        val firstSegment =
            pathSegments
                .firstOrNull()
                ?.lowercase(
                    Locale.US
                )
                .orEmpty()

        val playlistId =
            uri.getQueryParameter(
                "list"
            )
                ?.trim()
                .orEmpty()

        val rawVideoId =
            when {
                shortHost -> {
                    pathSegments
                        .firstOrNull()
                }

                firstSegment == "watch" -> {
                    uri.getQueryParameter(
                        "v"
                    )
                }

                firstSegment in
                    setOf(
                        "shorts",
                        "live",
                        "embed",
                        "v"
                    ) -> {
                    pathSegments
                        .getOrNull(1)
                }

                else -> {
                    uri.getQueryParameter(
                        "v"
                    )
                }
            }
                ?.trim()
                .orEmpty()

        if (rawVideoId.isBlank()) {
            if (
                firstSegment ==
                "playlist" ||
                playlistId.isNotBlank()
            ) {
                throw IllegalArgumentException(
                    "Ứng dụng chưa hỗ trợ tải toàn bộ playlist"
                )
            }

            throw IllegalArgumentException(
                "Không tìm thấy mã video trong liên kết YouTube"
            )
        }

        val videoId =
            rawVideoId
                .substringBefore('?')
                .substringBefore('&')
                .substringBefore('#')
                .trim()

        val validVideoId =
            Regex(
                """^[A-Za-z0-9_-]{11}$"""
            ).matches(
                videoId
            )

        if (!validVideoId) {
            throw IllegalArgumentException(
                "Mã video YouTube không hợp lệ"
            )
        }

        /*
         * Chỉ giữ video ID.
         * Các tham số list, index, start_radio, si, t,
         * feature và pp đều được loại bỏ.
         */
        return buildString {
            append(
                "https://www.youtube.com/watch?v="
            )

            append(
                videoId
            )
        }
    }
    private fun friendlyMetadataError(
        rawMessage: String
    ): String {
        val lower =
            rawMessage.lowercase()

        return when {
            "private video" in lower -> {
                "Video đang ở chế độ riêng tư"
            }

            "video unavailable" in lower ||
                "this video is unavailable" in
                lower -> {
                "Video không khả dụng hoặc đã bị xóa"
            }

            "sign in" in lower ||
                "login" in lower -> {
                "Video yêu cầu đăng nhập"
            }

            "age" in lower -> {
                "Video bị giới hạn độ tuổi"
            }

            "region" in lower ||
                "country" in lower -> {
                "Video bị giới hạn khu vực"
            }

            else -> {
                rawMessage.take(300)
            }
        }
    }

    private fun statusPriority(
        status: DownloadStatus
    ): Int {
        return when (status) {
            DownloadStatus.EXTRACTING,
            DownloadStatus.DOWNLOADING,
            DownloadStatus.CONVERTING,
            DownloadStatus.TAGGING,
            DownloadStatus.SAVING -> 0

            DownloadStatus.QUEUED -> 1

            else -> 2
        }
    }
}