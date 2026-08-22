package com.ngoctien.getmp3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ngoctien.getmp3.compare.CompareActionResult
import com.ngoctien.getmp3.compare.CompareAudioPreview
import com.ngoctien.getmp3.compare.ComparePair
import com.ngoctien.getmp3.compare.CompareRepository
import com.ngoctien.getmp3.compare.CompareSide
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CompareUiState(
    val exactPairs: List<ComparePair> =
        emptyList(),

    val nearPairs: List<ComparePair> =
        emptyList(),

    val ignoredPairCount: Int = 0,

    val selectedPair: ComparePair? =
        null,

    val playingSide: CompareSide? =
        null,

    val isLoading: Boolean =
        false,

    val isWorking: Boolean =
        false,

    val hasLoaded: Boolean =
        false,

    val errorMessage: String? =
        null
) {
    val totalPairCount: Int
        get() =
            exactPairs.size +
                nearPairs.size
}

data class CompareEvent(
    val message: String
)

class CompareViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        CompareRepository(application)

    private val mutableUiState =
        MutableStateFlow(
            CompareUiState()
        )

    val uiState:
        StateFlow<CompareUiState> =
        mutableUiState.asStateFlow()

    private val mutableEvents =
        MutableSharedFlow<CompareEvent>(
            extraBufferCapacity = 8
        )

    val events:
        SharedFlow<CompareEvent> =
        mutableEvents.asSharedFlow()

    private var scanJob: Job? =
        null

    private val audioPreview =
        CompareAudioPreview(
            context = application,
            onPlayingChanged = { uri ->
                val selected =
                    mutableUiState
                        .value
                        .selectedPair

                val side =
                    when (uri) {
                        selected
                            ?.current
                            ?.uri ->
                            CompareSide.CURRENT

                        selected
                            ?.reference
                            ?.uri ->
                            CompareSide.REFERENCE

                        else -> null
                    }

                mutableUiState.update {
                    it.copy(
                        playingSide = side
                    )
                }
            },
            onError = { message ->
                mutableEvents.tryEmit(
                    CompareEvent(message)
                )

                mutableUiState.update {
                    it.copy(
                        playingSide = null
                    )
                }
            }
        )

    /**
     * Entering the tab is cheap after the first load: do not rerun matching
     * every time the user switches tabs.
     */
    fun ensureLoaded() {
        if (
            mutableUiState.value.hasLoaded ||
            scanJob?.isActive == true
        ) {
            return
        }

        runCompare()
    }

    /**
     * Manual button in Compare. It only syncs the small download folder and
     * compares it with the already prepared shared reference DB.
     */
    fun refresh() {
        if (scanJob?.isActive == true) {
            return
        }

        runCompare()
    }

    private fun runCompare() {
        if (mutableUiState.value.isWorking) {
            return
        }

        scanJob =
            viewModelScope.launch {
                mutableUiState.update {
                    it.copy(
                        isLoading = true,
                        errorMessage = null
                    )
                }

                try {
                    val result =
                        repository.scan()

                    mutableUiState.update {
                        it.copy(
                            exactPairs =
                                result.exactPairs,
                            nearPairs =
                                result.nearPairs,
                            ignoredPairCount =
                                result.ignoredPairCount,
                            isLoading = false,
                            hasLoaded = true,
                            errorMessage = null
                        )
                    }
                } catch (
                    exception: CancellationException
                ) {
                    throw exception
                } catch (
                    exception: Exception
                ) {
                    mutableUiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage =
                                exception.message
                                    ?.takeIf {
                                        message ->
                                        message.isNotBlank()
                                    }
                                    ?: "Không đối chiếu được dữ liệu"
                        )
                    }
                }
            }
    }

    fun openPair(
        pair: ComparePair
    ) {
        audioPreview.stop()

        mutableUiState.update {
            it.copy(
                selectedPair = pair,
                playingSide = null
            )
        }
    }

    fun closePair() {
        audioPreview.stop()

        mutableUiState.update {
            it.copy(
                selectedPair = null,
                playingSide = null
            )
        }
    }

    fun togglePreview(
        side: CompareSide
    ) {
        val pair =
            mutableUiState
                .value
                .selectedPair
                ?: return

        val uri =
            when (side) {
                CompareSide.CURRENT ->
                    pair.current.uri

                CompareSide.REFERENCE ->
                    pair.reference.uri
            }

        audioPreview.toggle(uri)
    }

    fun keepCurrent() {
        val pair =
            mutableUiState
                .value
                .selectedPair
                ?: return

        performAction {
            repository.keepCurrent(pair)
        }
    }

    fun keepReference() {
        val pair =
            mutableUiState
                .value
                .selectedPair
                ?: return

        performAction {
            repository.keepReference(pair)
        }
    }

    fun keepBoth() {
        val pair =
            mutableUiState
                .value
                .selectedPair
                ?: return

        performAction {
            repository.keepBoth(pair)
        }
    }

    private fun performAction(
        action:
            suspend () -> CompareActionResult
    ) {
        if (mutableUiState.value.isWorking) {
            return
        }

        viewModelScope.launch {
            audioPreview.stop()

            mutableUiState.update {
                it.copy(
                    isWorking = true,
                    errorMessage = null
                )
            }

            try {
                val result =
                    action()

                mutableEvents.emit(
                    CompareEvent(
                        result.message
                    )
                )

                result.warning
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let { warning ->
                        mutableEvents.emit(
                            CompareEvent(
                                warning
                            )
                        )
                    }

                mutableUiState.update {
                    it.copy(
                        selectedPair = null,
                        playingSide = null,
                        isWorking = false
                    )
                }

                /* Only re-sync the small working folder + DB matcher. */
                refresh()
            } catch (
                exception: CancellationException
            ) {
                throw exception
            } catch (
                exception: Exception
            ) {
                mutableUiState.update {
                    it.copy(
                        isWorking = false
                    )
                }

                mutableEvents.emit(
                    CompareEvent(
                        exception.message
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: "Không xử lý được file"
                    )
                )
            }
        }
    }

    override fun onCleared() {
        scanJob?.cancel()
        audioPreview.release()
        super.onCleared()
    }
}
