package com.ngoctien.getmp3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ngoctien.getmp3.library.MediaIndexProgress
import com.ngoctien.getmp3.library.MediaIndexRepository
import com.ngoctien.getmp3.settings.AppSettings
import com.ngoctien.getmp3.settings.AppSettingsRepository
import com.ngoctien.getmp3.settings.AppThemeMode
import com.ngoctien.getmp3.settings.AppUiStyle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI state for the shared reference-media index.
 *
 * The data itself lives in Room. This state only mirrors progress/stats.
 */
data class CompareIndexUiState(
    val isScanning: Boolean = false,
    val totalFiles: Int = 0,
    val processedFiles: Int = 0,
    val newFiles: Int = 0,
    val changedFiles: Int = 0,
    val skippedFiles: Int = 0,
    val failedFiles: Int = 0,
    val compliantFiles: Int = 0,
    val normalizationFiles: Int = 0,
    val coverFiles: Int = 0,
    val artistCount: Int = 0,
    val albumCount: Int = 0,
    val updatedAt: Long = 0L,
    val sourceUri: String? = null,
    val currentFileName: String = "",
    val message: String? = null,
    val errorMessage: String? = null
) {
    val progressFraction: Float
        get() =
            if (totalFiles <= 0) {
                0f
            } else {
                processedFiles
                    .toFloat()
                    .div(totalFiles.toFloat())
                    .coerceIn(0f, 1f)
            }

    val hasIndex: Boolean
        get() =
            sourceUri
                .isNullOrBlank()
                .not() &&
                totalFiles > 0
}

class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        AppSettingsRepository(application)

    private val mediaIndexRepository =
        MediaIndexRepository(application)

    val uiState: StateFlow<AppSettings> =
        repository.observeSettings()
            .stateIn(
                scope = viewModelScope,
                started =
                    SharingStarted
                        .WhileSubscribed(
                            5_000L
                        ),
                initialValue =
                    repository.getSettings()
            )

    private val mutableCompareIndexState =
        MutableStateFlow(
            CompareIndexUiState()
        )

    val compareIndexState:
        StateFlow<CompareIndexUiState> =
        mutableCompareIndexState
            .asStateFlow()

    private var compareScanJob:
        Job? =
        null

    init {
        refreshStoredIndexState()
    }

    // ========================================================
    // DOMAIN API - INBOX / LIBRARY
    // ========================================================

    val libraryIndexState:
        StateFlow<CompareIndexUiState>
        get() =
            compareIndexState

    fun setInboxFolder(
        treeUri: String,
        displayName: String
    ) {
        setDownloadFolder(
            treeUri = treeUri,
            displayName = displayName
        )
    }

    fun useDefaultInboxFolder() {
        useDefaultFolder()
    }

    fun setLibraryFolder(
        treeUri: String,
        displayName: String
    ) {
        setCompareFolder(
            treeUri = treeUri,
            displayName = displayName
        )
    }

    fun clearLibraryFolder() {
        clearCompareFolder()
    }

    fun rebuildLibraryIndex() {
        rebuildCompareIndex()
    }
    fun setBitrate(
        bitrateKbps: Int
    ) {
        repository.setBitrate(
            bitrateKbps
        )
    }

    fun setThemeMode(
        mode: AppThemeMode
    ) {
        repository.setThemeMode(
            mode
        )
    }
    fun setUiStyle(
        style: AppUiStyle
    ) {
        repository
            .setUiStyle(
                style
            )
    }

fun setDownloadFolder(
        treeUri: String,
        displayName: String
    ) {
        repository.setDownloadFolder(
            treeUri = treeUri,
            displayName = displayName
        )
    }

    fun useDefaultFolder() {
        repository.useDefaultFolder()
    }

    fun setCompareFolder(
        treeUri: String,
        displayName: String
    ) {
        val oldUri =
            repository
                .getSettings()
                .compareTreeUri

        compareScanJob?.cancel()
        compareScanJob = null

        repository.setCompareFolder(
            treeUri = treeUri,
            displayName = displayName
        )

        if (oldUri != treeUri) {
            viewModelScope.launch {
                mediaIndexRepository
                    .clearReferenceLibrary()

                mutableCompareIndexState.value =
                    CompareIndexUiState(
                        sourceUri = treeUri,
                        message =
                            "Đã chọn thư mục. Nhấn Cài & đồng bộ để lập chỉ mục."
                    )
            }
        } else {
            refreshStoredIndexState()
        }
    }

    fun clearCompareFolder() {
        compareScanJob?.cancel()
        compareScanJob = null

        repository.clearCompareFolder()

        viewModelScope.launch {
            mediaIndexRepository
                .clearReferenceLibrary()

            mutableCompareIndexState.value =
                CompareIndexUiState(
                    message =
                        "Đã bỏ Library"
                )
        }
    }

    fun setTitleFilters(
        terms: List<String>,
        symbols: String
    ) {
        repository.setTitleFilters(
            terms = terms,
            symbols = symbols
        )
    }

    /**
     * Incremental scan:
     * - inventory is always checked;
     * - unchanged files reuse Room data;
     * - metadata/cover are read only for new or changed files.
     */
    fun rebuildCompareIndex() {
        val settings =
            repository.getSettings()

        val treeUri =
            settings.compareTreeUri

        if (treeUri.isNullOrBlank()) {
            mutableCompareIndexState.value =
                CompareIndexUiState(
                    errorMessage =
                        "Chưa chọn Library"
                )

            return
        }

        compareScanJob?.cancel()

        compareScanJob =
            viewModelScope.launch {
                mutableCompareIndexState.value =
                    mutableCompareIndexState
                        .value
                        .copy(
                            isScanning = true,
                            sourceUri = treeUri,
                            processedFiles = 0,
                            newFiles = 0,
                            changedFiles = 0,
                            skippedFiles = 0,
                            failedFiles = 0,
                            compliantFiles = 0,
                            normalizationFiles = 0,
                            currentFileName = "",
                            message =
                                "Đang kiểm tra Library...",
                            errorMessage = null
                        )

                try {
                    val summary =
                        mediaIndexRepository
                            .scanReferenceLibrary(
                                treeUriText = treeUri,
                                forceReadMetadata = false,
                                onProgress =
                                    ::onIndexProgress
                            )

                    /*
                     * Keep only the legacy invalidation timestamp for the
                     * still-unmigrated Search tab. Artist/Album data itself
                     * lives exclusively in Room.
                     */
                    repository.saveCompareIndex(
                        sourceUri = treeUri,
                        artists = emptyList(),
                        albums = emptyList()
                    )

                    mutableCompareIndexState.value =
                        mutableCompareIndexState
                            .value
                            .copy(
                                isScanning = false,
                                totalFiles =
                                    summary.totalFiles,
                                processedFiles =
                                    summary.totalFiles,
                                failedFiles =
                                    summary.failedFiles,
                                coverFiles =
                                    summary.coverFiles,
                                artistCount =
                                    summary.artistCount,
                                albumCount =
                                    summary.albumCount,
                                updatedAt =
                                    summary.updatedAt,
                                sourceUri =
                                    summary.treeUri,
                                currentFileName = "",
                                message =
                                    "Hoàn tất • ${summary.totalFiles} bài • " +
                                        "${summary.artistCount} Artist • " +
                                        "${summary.albumCount} Album • " +
                                        "${summary.coverFiles} cover",
                                errorMessage = null
                            )
                } catch (
                    exception:
                        CancellationException
                ) {
                    throw exception
                } catch (
                    exception: Exception
                ) {
                    mutableCompareIndexState.value =
                        mutableCompareIndexState
                            .value
                            .copy(
                                isScanning = false,
                                currentFileName = "",
                                errorMessage =
                                    exception.message
                                        ?.takeIf {
                                            it.isNotBlank()
                                        }
                                        ?: "Không chuẩn bị được dữ liệu thư viện"
                            )
                }
            }
    }

    private fun onIndexProgress(
        progress: MediaIndexProgress
    ) {
        mutableCompareIndexState.value =
            mutableCompareIndexState
                .value
                .copy(
                    isScanning = true,
                    totalFiles =
                        progress.totalFiles,
                    processedFiles =
                        progress.processedFiles,
                    newFiles =
                        progress.newFiles,
                    changedFiles =
                        progress.changedFiles,
                    skippedFiles =
                        progress.skippedFiles,
                    failedFiles =
                        progress.failedFiles,
                    compliantFiles =
                        progress.compliantFiles,
                    normalizationFiles =
                        progress.normalizationFiles,
                    currentFileName =
                        progress.currentFileName,
                    message =
                        if (
                            progress.totalFiles > 0
                        ) {
                            "${progress.processedFiles}/${progress.totalFiles} • " +
                                "mới ${progress.newFiles} • " +
                                "đổi ${progress.changedFiles} • " +
                                "bỏ qua ${progress.skippedFiles}"
                        } else {
                            "Đang đọc danh sách file..."
                        },
                    errorMessage = null
                )
    }

    private fun refreshStoredIndexState() {
        viewModelScope.launch {
            val settings =
                repository.getSettings()

            val summary =
                mediaIndexRepository
                    .referenceSummary()

            val complianceSummary =
                mediaIndexRepository
                    .referenceComplianceSummary()

            mutableCompareIndexState.value =
                if (
                    summary != null &&
                    summary.treeUri ==
                    settings.compareTreeUri
                ) {
                    CompareIndexUiState(
                        totalFiles =
                            summary.totalFiles,
                        processedFiles =
                            summary.totalFiles,
                        failedFiles =
                            complianceSummary
                                .brokenFiles,
                        compliantFiles =
                            complianceSummary
                                .compliantFiles,
                        normalizationFiles =
                            complianceSummary
                                .normalizationFiles,
                        coverFiles =
                            summary.coverFiles,
                        artistCount =
                            summary.artistCount,
                        albumCount =
                            summary.albumCount,
                        updatedAt =
                            summary.updatedAt,
                        sourceUri =
                            summary.treeUri,
                        message =
                            "${summary.totalFiles} bài đã có dữ liệu"
                    )
                } else {
                    CompareIndexUiState(
                        sourceUri =
                            settings.compareTreeUri,
                        message =
                            if (
                                settings.hasLibraryFolder
                            ) {
                                "Chưa có dữ liệu cho thư mục này"
                            } else {
                                null
                            }
                    )
                }
        }
    }

    override fun onCleared() {
        compareScanJob?.cancel()
        compareScanJob = null

        super.onCleared()
    }
}
