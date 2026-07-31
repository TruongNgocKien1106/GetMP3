package com.ngoctien.getmp3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ngoctien.getmp3.settings.AppSettings
import com.ngoctien.getmp3.settings.AppSettingsRepository
import com.ngoctien.getmp3.settings.AppThemeMode
import com.ngoctien.getmp3.settings.CompareLibraryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CompareIndexUiState(
    val isScanning: Boolean = false,
    val scannedFiles: Int = 0,
    val message: String? = null,
    val errorMessage: String? = null
)

class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        AppSettingsRepository(application)

    private val compareLibraryRepository =
        CompareLibraryRepository(
            application
        )

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

    /*
     * Chỉ cho phép một lượt quét tồn tại.
     */
    private var compareScanJob: Job? =
        null

    init {
        ensureCompareIndex()
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
        repository.setThemeMode(mode)
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
        /*
         * Người dùng chọn thư mục khác thì dừng ngay
         * lượt quét cũ.
         */
        compareScanJob?.cancel()
        compareScanJob = null

        repository.setCompareFolder(
            treeUri = treeUri,
            displayName = displayName
        )

        mutableCompareIndexState.value =
            CompareIndexUiState(
                message =
                    "Đã chọn thư mục. Đang chuẩn bị quét..."
            )

        rebuildCompareIndex()
    }

    fun clearCompareFolder() {
        compareScanJob?.cancel()
        compareScanJob = null

        repository.clearCompareFolder()

        mutableCompareIndexState.value =
            CompareIndexUiState(
                message =
                    "Đã bỏ thư mục đối chiếu"
            )
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

    fun rebuildCompareIndex() {
        val settings =
            repository.getSettings()

        val treeUri =
            settings.compareTreeUri

        if (treeUri.isNullOrBlank()) {
            mutableCompareIndexState.value =
                CompareIndexUiState(
                    errorMessage =
                        "Chưa chọn thư mục đối chiếu"
                )

            return
        }

        /*
         * Nhấn Quét lại thì hủy lượt cũ và chạy
         * lượt mới, không để hai tác vụ tranh nhau.
         */
        compareScanJob?.cancel()

        compareScanJob =
            viewModelScope.launch {
                mutableCompareIndexState.value =
                    CompareIndexUiState(
                        isScanning = true,

                        message =
                            "Đang đọc file MP3 trực tiếp trong thư mục..."
                    )

                try {
                    val result =
                        compareLibraryRepository.scan(
                            treeUriText =
                                treeUri,

                            onProgress = {
                                mutableCompareIndexState.value =
                                    CompareIndexUiState(
                                        isScanning = true,

                                        scannedFiles = it,

                                        message =
                                            "Đã đọc $it file MP3..."
                                    )
                            }
                        )

                    /*
                     * Lưu JSON lớn ra SharedPreferences
                     * trên IO, không chặn UI.
                     */
                    withContext(
                        Dispatchers.IO
                    ) {
                        repository.saveCompareIndex(
                            sourceUri =
                                treeUri,

                            artists =
                                result.artists,

                            albums =
                                result.albums
                        )
                    }

                    mutableCompareIndexState.value =
                        CompareIndexUiState(
                            isScanning = false,

                            scannedFiles =
                                result.scannedFiles,

                            message =
                                "Hoàn tất • " +
                                    "${result.scannedFiles} file • " +
                                    "${result.artists.size} Artist"
                        )
                } catch (
                    exception: CancellationException
                ) {
                    /*
                     * Hủy lượt cũ là hành vi bình thường.
                     * Không biến nó thành thông báo lỗi.
                     */
                    throw exception
                } catch (
                    exception: Exception
                ) {
                    mutableCompareIndexState.value =
                        CompareIndexUiState(
                            isScanning = false,

                            errorMessage =
                                exception.message
                                    ?.takeIf {
                                        it.isNotBlank()
                                    }
                                    ?: "Không quét được thư mục đối chiếu"
                        )
                }
            }
    }

    private fun ensureCompareIndex() {
        val settings =
            repository.getSettings()

        val compareUri =
            settings.compareTreeUri

        if (compareUri.isNullOrBlank()) {
            return
        }

        val hasValidIndex =
            settings.compareIndexSourceUri ==
                compareUri &&
                settings.compareIndexGeneratedAt >
                0L &&
                settings.indexedArtists
                    .isNotEmpty()

        if (!hasValidIndex) {
            rebuildCompareIndex()
        }
    }

    override fun onCleared() {
        compareScanJob?.cancel()
        compareScanJob = null

        super.onCleared()
    }
}