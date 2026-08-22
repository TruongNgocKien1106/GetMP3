package com.ngoctien.getmp3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ngoctien.getmp3.lyrics.LibrarySongCandidate
import com.ngoctien.getmp3.lyrics.LyricsRepository
import com.ngoctien.getmp3.lyrics.LyricsWriteDecision
import com.ngoctien.getmp3.lyrics.decideLyricsWrite
import com.ngoctien.getmp3.lyrics.LyricsScreen
import com.ngoctien.getmp3.lyrics.LyricsSearchResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WriteTargetPickerState(
    val isVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isChecking: Boolean = false,
    val candidates:
        List<LibrarySongCandidate> =
            emptyList(),
    val selectedTarget:
        LibrarySongCandidate? =
            null,
    val errorMessage: String? = null
)

data class LyricsUiState(
    val screen: LyricsScreen =
        LyricsScreen.LIBRARY,
    val readerBackScreen: LyricsScreen =
        LyricsScreen.LIBRARY,
    val query: String = "",
    val suggestions:
        List<LibrarySongCandidate> =
            emptyList(),
    val selectedSong:
        LibrarySongCandidate? =
            null,
    val searchResults:
        List<LyricsSearchResult> =
            emptyList(),
    val selectedResult:
        LyricsSearchResult? =
            null,
    val writeTargetPicker:
        WriteTargetPickerState =
            WriteTargetPickerState(),
    val showDirectOverwriteDialog:
        Boolean = false,
    val pendingWriteTarget:
        LibrarySongCandidate? =
            null,
    val pendingWriteLyrics: String = "",
    val pendingExistingLyrics: String = "",
    val recentlySavedTargetUri:
        String? = null,
    val editorTarget:
        LibrarySongCandidate? =
            null,
    val existingLyrics: String = "",
    val editorLyrics: String = "",
    val isLoadingSuggestions:
        Boolean = false,
    val isSearchingLyrics:
        Boolean = false,
    val isLoadingFile:
        Boolean = false,
    val isSaving:
        Boolean = false,
    val fontSizeSp: Float = 22f,
    val autoScrollEnabled:
        Boolean = false,
    val autoScrollSpeed: Int = 3,
    val successMessage:
        String? = null,
    val errorMessage:
        String? = null
)

sealed interface LyricsEvent {
    data class Message(
        val text: String
    ) : LyricsEvent
}

private enum class LyricsSearchSource {
    QUERY,
    LIBRARY_FILE
}

private data class LyricsSearchRequest(
    val title: String,
    val artist: String,
    val source: LyricsSearchSource,
    val song: LibrarySongCandidate? = null
)

class LyricsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        LyricsRepository(application)

    private val mutableUiState =
        MutableStateFlow(
            LyricsUiState()
        )

    val uiState:
        StateFlow<LyricsUiState> =
            mutableUiState.asStateFlow()

    private val mutableEvents =
        MutableSharedFlow<LyricsEvent>(
            extraBufferCapacity = 8
        )

    val events:
        SharedFlow<LyricsEvent> =
            mutableEvents.asSharedFlow()

    private var suggestionJob: Job? = null
    private var lyricsSearchJob: Job? = null
    private var fileJob: Job? = null
    private var savedIndicatorJob: Job? = null
    private var lastSearchRequest:
        LyricsSearchRequest? = null

    fun setQuery(
        value: String
    ) {
        mutableUiState.update {
            it.copy(
                query = value,
                errorMessage = null
            )
        }

        suggestionJob?.cancel()

        val cleanValue =
            value.trim()

        if (cleanValue.length < 2) {
            mutableUiState.update {
                it.copy(
                    suggestions =
                        emptyList(),
                    isLoadingSuggestions =
                        false
                )
            }

            return
        }

        suggestionJob =
            viewModelScope.launch {
                delay(260L)

                mutableUiState.update {
                    it.copy(
                        isLoadingSuggestions =
                            true
                    )
                }

                try {
                    val suggestions =
                        repository.suggestSongs(
                            cleanValue
                        )

                    mutableUiState.update {
                        it.copy(
                            suggestions =
                                suggestions,
                            isLoadingSuggestions =
                                false,
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
                            suggestions =
                                emptyList(),
                            isLoadingSuggestions =
                                false,
                            errorMessage =
                                exception.message
                                    ?: "Không đọc được thư viện nhạc"
                        )
                    }
                }
            }
    }

    fun clearQuery() {
        suggestionJob?.cancel()

        mutableUiState.update {
            it.copy(
                query = "",
                suggestions =
                    emptyList(),
                errorMessage = null
            )
        }
    }

    fun submitSearch() {
        val rawQuery =
            mutableUiState.value.query

        val candidate =
            repository.candidateFromInput(
                rawQuery
            )

        if (candidate.title.isBlank()) {
            mutableUiState.update {
                it.copy(
                    errorMessage =
                        "Hãy nhập tên bài hát"
                )
            }

            return
        }

        lastSearchRequest =
            LyricsSearchRequest(
                title = candidate.title,
                artist = candidate.artist,
                source =
                    LyricsSearchSource.QUERY
            )

        mutableUiState.update {
            it.copy(
                selectedSong = null,
                selectedResult = null,
                searchResults =
                    emptyList(),
                screen =
                    LyricsScreen.RESULTS,
                readerBackScreen =
                    LyricsScreen.RESULTS,
                successMessage = null,
                errorMessage = null
            )
        }

        searchLyricsOnline(
            title = candidate.title,
            artist = candidate.artist,
            emptyMessage =
                "Không tìm thấy lyrics phù hợp"
        )
    }

    fun selectSong(
        song: LibrarySongCandidate
    ) {
        lastSearchRequest =
            LyricsSearchRequest(
                title = song.title,
                artist = song.artist,
                source =
                    LyricsSearchSource.LIBRARY_FILE,
                song = song
            )

        mutableUiState.update {
            it.copy(
                query =
                    buildSongQuery(song),
                selectedSong = song,
                selectedResult = null,
                searchResults =
                    emptyList(),
                screen =
                    LyricsScreen.RESULTS,
                readerBackScreen =
                    LyricsScreen.LIBRARY,
                successMessage = null,
                errorMessage = null
            )
        }

        searchLyricsForSong(song)
    }

    fun retryLyricsSearch() {
        when (
            val request =
                lastSearchRequest
        ) {
            null -> Unit

            else -> {
                when (request.source) {
                    LyricsSearchSource.QUERY -> {
                        searchLyricsOnline(
                            title = request.title,
                            artist = request.artist,
                            emptyMessage =
                                "Không tìm thấy lyrics phù hợp"
                        )
                    }

                    LyricsSearchSource.LIBRARY_FILE -> {
                        request.song
                            ?.let(
                                ::searchLyricsForSong
                            )
                    }
                }
            }
        }
    }

    fun selectLyricsResult(
        result: LyricsSearchResult
    ) {
        mutableUiState.update {
            it.copy(
                selectedResult = result,
                screen =
                    LyricsScreen.READER,
                readerBackScreen =
                    LyricsScreen.RESULTS,
                autoScrollEnabled = false,
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun editCurrentLyrics() {
        val state =
            mutableUiState.value

        val target =
            state.selectedSong
                ?.takeIf {
                    it.uri.isNotBlank()
                }

        if (target == null) {
            mutableUiState.update {
                it.copy(
                    errorMessage =
                        "Chưa chọn file MP3 để chỉnh"
                )
            }

            return
        }

        openEditorForTarget(
            target = target,
            initialLyrics =
                state.selectedResult
                    ?.readableLyrics
                    .orEmpty()
        )
    }

    fun openWriteTargetPicker() {
        val state =
            mutableUiState.value

        val result =
            state.selectedResult

        val lyrics =
            result
                ?.readableLyrics
                .orEmpty()
                .trim()

        if (
            result == null ||
            lyrics.isBlank()
        ) {
            mutableUiState.update {
                it.copy(
                    errorMessage =
                        "Chưa có lyrics để lưu"
                )
            }

            return
        }

        fileJob?.cancel()

        mutableUiState.update {
            it.copy(
                writeTargetPicker =
                    WriteTargetPickerState(
                        isVisible = true,
                        isLoading = true
                    ),
                successMessage = null,
                errorMessage = null
            )
        }

        fileJob =
            viewModelScope.launch {
                try {
                    val candidates =
                        repository.findWriteCandidates(
                            title =
                                result.trackName,
                            artist =
                                result.artistName,
                            preferredUri =
                                state.selectedSong
                                    ?.uri
                                    ?.takeIf(
                                        String::isNotBlank
                                    ),
                            limit = 12
                        )

                    mutableUiState.update {
                        it.copy(
                            writeTargetPicker =
                                it.writeTargetPicker
                                    .copy(
                                        isLoading = false,
                                        candidates =
                                            candidates,
                                        selectedTarget =
                                            null,
                                        errorMessage =
                                            if (
                                                candidates.isEmpty()
                                            ) {
                                                "Không tìm thấy file MP3 phù hợp"
                                            } else {
                                                null
                                            }
                                    )
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
                            writeTargetPicker =
                                it.writeTargetPicker
                                    .copy(
                                        isLoading = false,
                                        errorMessage =
                                            exception.message
                                                ?: "Không tìm được file MP3 phù hợp"
                                    )
                        )
                    }
                }
            }
    }

    fun dismissWriteTargetPicker() {
        if (mutableUiState.value.isSaving) {
            return
        }

        fileJob?.cancel()

        mutableUiState.update {
            it.copy(
                writeTargetPicker =
                    WriteTargetPickerState()
            )
        }
    }

    fun selectWriteTarget(
        target: LibrarySongCandidate
    ) {
        mutableUiState.update {
            it.copy(
                writeTargetPicker =
                    it.writeTargetPicker
                        .copy(
                            selectedTarget =
                                target,
                            errorMessage = null
                        )
            )
        }
    }

    fun confirmWriteTarget() {
        val state =
            mutableUiState.value

        if (
            state.writeTargetPicker.isLoading ||
            state.writeTargetPicker.isChecking ||
            state.isSaving
        ) {
            return
        }

        val target =
            state.writeTargetPicker
                .selectedTarget

        if (target == null) {
            mutableUiState.update {
                it.copy(
                    writeTargetPicker =
                        it.writeTargetPicker
                            .copy(
                                errorMessage =
                                    "Hãy chọn một file MP3"
                            )
                )
            }

            return
        }

        val lyrics =
            state.selectedResult
                ?.readableLyrics
                .orEmpty()
                .trim()

        prepareDirectWrite(
            target = target,
            lyrics = lyrics
        )
    }

    fun dismissDirectOverwrite() {
        mutableUiState.update {
            it.copy(
                showDirectOverwriteDialog =
                    false,
                pendingWriteTarget = null,
                pendingWriteLyrics = "",
                pendingExistingLyrics = "",
                writeTargetPicker =
                    it.writeTargetPicker
                        .copy(
                            isVisible =
                                it.writeTargetPicker
                                    .candidates
                                    .isNotEmpty(),
                            isChecking = false
                        )
            )
        }
    }

    fun confirmDirectOverwrite() {
        val state =
            mutableUiState.value

        val target =
            state.pendingWriteTarget
                ?: return

        val lyrics =
            state.pendingWriteLyrics
                .trim()

        mutableUiState.update {
            it.copy(
                showDirectOverwriteDialog =
                    false,
                pendingWriteTarget = null,
                pendingWriteLyrics = "",
                pendingExistingLyrics = ""
            )
        }

        fileJob?.cancel()

        fileJob =
            viewModelScope.launch {
                writeDirectLyrics(
                    target = target,
                    lyrics = lyrics
                )
            }
    }

    fun openEditorForFile(
        uri: String,
        displayName: String,
        title: String,
        artist: String
    ) {
        val target =
            LibrarySongCandidate(
                uri = uri,
                treeUri = null,
                displayName =
                    displayName,
                title =
                    title.ifBlank {
                        displayName
                            .substringBeforeLast(
                                "."
                            )
                    },
                artist = artist,
                score = 1.0
            )

        lastSearchRequest =
            LyricsSearchRequest(
                title = target.title,
                artist = target.artist,
                source =
                    LyricsSearchSource.LIBRARY_FILE,
                song = target
            )

        mutableUiState.update {
            it.copy(
                selectedSong = target,
                selectedResult = null,
                searchResults =
                    emptyList(),
                query =
                    buildSongQuery(target),
                errorMessage = null
            )
        }

        openEditorForTarget(
            target = target,
            initialLyrics = ""
        )
    }

    fun setEditorLyrics(
        value: String
    ) {
        mutableUiState.update {
            it.copy(
                editorLyrics = value,
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun clearEditorLyrics() {
        mutableUiState.update {
            it.copy(
                editorLyrics = "",
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun pasteEditorLyrics(
        clipboardText: String?
    ) {
        val text =
            clipboardText
                ?.trim()
                .orEmpty()

        if (text.isBlank()) {
            mutableUiState.update {
                it.copy(
                    successMessage = null,
                    errorMessage =
                        "Clipboard đang trống"
                )
            }

            return
        }

        val isOnlyLink =
            Regex(
                """(?i)^(?:https?://|www\.)\S+$"""
            ).matches(text)

        if (isOnlyLink) {
            mutableUiState.update {
                it.copy(
                    successMessage = null,
                    errorMessage =
                        "Clipboard đang chứa liên kết, không phải lyrics"
                )
            }

            return
        }

        setEditorLyrics(text)
    }

    fun restoreFoundLyrics() {
        val lyrics =
            mutableUiState.value
                .selectedResult
                ?.readableLyrics
                .orEmpty()
                .trim()

        if (lyrics.isBlank()) {
            mutableUiState.update {
                it.copy(
                    successMessage = null,
                    errorMessage =
                        "Không có lyrics đã tìm để khôi phục"
                )
            }

            return
        }

        setEditorLyrics(lyrics)
    }

    fun restoreStoredLyrics() {
        val lyrics =
            mutableUiState.value
                .existingLyrics
                .trim()

        if (lyrics.isBlank()) {
            mutableUiState.update {
                it.copy(
                    successMessage = null,
                    errorMessage =
                        "File chưa có lyrics để khôi phục"
                )
            }

            return
        }

        setEditorLyrics(lyrics)
    }

    fun saveLyrics() {
        val state =
            mutableUiState.value

        val target =
            state.editorTarget
                ?: return

        val lyrics =
            state.editorLyrics
                .trim()

        if (lyrics.isBlank()) {
            mutableUiState.update {
                it.copy(
                    errorMessage =
                        "Nội dung lời bài hát đang trống"
                )
            }

            return
        }

        if (state.isSaving) {
            return
        }

        viewModelScope.launch {
            mutableUiState.update {
                it.copy(
                    isSaving = true,
                    successMessage = null,
                    errorMessage = null
                )
            }

            try {
                repository.writeLyrics(
                    target = target,
                    lyrics = lyrics
                )

                val successText =
                    "Đã ghi lời vào ${target.displayName}"

                mutableUiState.update {
                    it.copy(
                        isSaving = false,
                        selectedSong = target,
                        existingLyrics = lyrics,
                        editorLyrics = lyrics,
                        successMessage =
                            successText,
                        errorMessage = null
                    )
                }

                mutableEvents.emit(
                    LyricsEvent.Message(
                        successText
                    )
                )
            } catch (
                exception: CancellationException
            ) {
                mutableUiState.update {
                    it.copy(
                        isSaving = false
                    )
                }

                throw exception
            } catch (
                exception: Exception
            ) {
                mutableUiState.update {
                    it.copy(
                        isSaving = false,
                        successMessage = null,
                        errorMessage =
                            exception.message
                                ?: "Không ghi được lời vào MP3"
                    )
                }
            }
        }
    }

    fun back() {
        mutableUiState.update {
            when (it.screen) {
                LyricsScreen.LIBRARY ->
                    it

                LyricsScreen.RESULTS -> {
                    it.copy(
                        screen =
                            LyricsScreen.LIBRARY,
                        searchResults =
                            emptyList(),
                        selectedResult = null,
                        isSearchingLyrics =
                            false,
                        errorMessage = null
                    )
                }

                LyricsScreen.READER -> {
                    it.copy(
                        screen =
                            it.readerBackScreen,
                        autoScrollEnabled =
                            false,
                        errorMessage = null
                    )
                }

                LyricsScreen.EDITOR -> {
                    if (
                        it.selectedResult != null
                    ) {
                        it.copy(
                            screen =
                                LyricsScreen.READER,
                            errorMessage = null
                        )
                    } else {
                        it.copy(
                            screen =
                                LyricsScreen.LIBRARY,
                            errorMessage = null
                        )
                    }
                }
            }
        }
    }

    fun increaseFontSize() {
        mutableUiState.update {
            it.copy(
                fontSizeSp =
                    (it.fontSizeSp + 2f)
                        .coerceAtMost(36f)
            )
        }
    }

    fun decreaseFontSize() {
        mutableUiState.update {
            it.copy(
                fontSizeSp =
                    (it.fontSizeSp - 2f)
                        .coerceAtLeast(16f)
            )
        }
    }

    fun toggleAutoScroll() {
        mutableUiState.update {
            it.copy(
                autoScrollEnabled =
                    !it.autoScrollEnabled
            )
        }
    }

    fun setAutoScrollSpeed(
        speed: Int
    ) {
        mutableUiState.update {
            it.copy(
                autoScrollSpeed =
                    speed.coerceIn(
                        minimumValue = 1,
                        maximumValue = 10
                    )
            )
        }
    }

    private fun searchLyricsOnline(
        title: String,
        artist: String,
        emptyMessage: String
    ) {
        lyricsSearchJob?.cancel()

        lyricsSearchJob =
            viewModelScope.launch {
                mutableUiState.update {
                    it.copy(
                        screen =
                            LyricsScreen.RESULTS,
                        isSearchingLyrics =
                            true,
                        searchResults =
                            emptyList(),
                        selectedResult = null,
                        autoScrollEnabled =
                            false,
                        errorMessage = null
                    )
                }

                try {
                    val results =
                        repository.searchLyrics(
                            rawTitle = title,
                            rawArtist = artist,
                            limit = 10
                        )

                    mutableUiState.update {
                        it.copy(
                            screen =
                                LyricsScreen.RESULTS,
                            isSearchingLyrics =
                                false,
                            searchResults =
                                results,
                            selectedResult = null,
                            editorLyrics = "",
                            errorMessage =
                                if (results.isEmpty()) {
                                    emptyMessage
                                } else {
                                    null
                                }
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
                            screen =
                                LyricsScreen.RESULTS,
                            isSearchingLyrics =
                                false,
                            searchResults =
                                emptyList(),
                            selectedResult = null,
                            errorMessage =
                                exception.message
                                    ?.takeIf(
                                        String::isNotBlank
                                    )
                                    ?: "Không tra được lyrics trên mạng"
                        )
                    }
                }
            }
    }

    private fun searchLyricsForSong(
        song: LibrarySongCandidate
    ) {
        lyricsSearchJob?.cancel()

        lyricsSearchJob =
            viewModelScope.launch {
                mutableUiState.update {
                    it.copy(
                        screen =
                            LyricsScreen.RESULTS,
                        isSearchingLyrics =
                            true,
                        searchResults =
                            emptyList(),
                        selectedResult = null,
                        errorMessage = null
                    )
                }

                try {
                    val embeddedLyrics =
                        readEmbeddedLyricsOrEmpty(
                            song
                        )

                    if (embeddedLyrics.isNotBlank()) {
                        val result =
                            LyricsSearchResult(
                                id = Long.MIN_VALUE,
                                trackName =
                                    song.title,
                                artistName =
                                    song.artist,
                                albumName = "",
                                durationSeconds =
                                    null,
                                plainLyrics =
                                    embeddedLyrics,
                                syncedLyrics = null,
                                score = 1.0
                            )

                        mutableUiState.update {
                            it.copy(
                                screen =
                                    LyricsScreen.READER,
                                readerBackScreen =
                                    LyricsScreen.LIBRARY,
                                isSearchingLyrics =
                                    false,
                                searchResults =
                                    emptyList(),
                                selectedResult =
                                    result,
                                autoScrollEnabled =
                                    false,
                                errorMessage = null
                            )
                        }

                        return@launch
                    }

                    val results =
                        repository.searchLyrics(
                            rawTitle =
                                song.title,
                            rawArtist =
                                song.artist,
                            limit = 10
                        )

                    mutableUiState.update {
                        it.copy(
                            screen =
                                LyricsScreen.RESULTS,
                            readerBackScreen =
                                LyricsScreen.RESULTS,
                            isSearchingLyrics =
                                false,
                            searchResults =
                                results,
                            selectedResult = null,
                            editorLyrics = "",
                            errorMessage =
                                if (results.isEmpty()) {
                                    "File chưa có lyrics và không tìm thấy kết quả phù hợp trên mạng"
                                } else {
                                    null
                                }
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
                            screen =
                                LyricsScreen.RESULTS,
                            isSearchingLyrics =
                                false,
                            searchResults =
                                emptyList(),
                            selectedResult = null,
                            errorMessage =
                                exception.message
                                    ?.takeIf(
                                        String::isNotBlank
                                    )
                                    ?: "Không đọc hoặc tra được lyrics"
                        )
                    }
                }
            }
    }

    private fun prepareDirectWrite(
        target: LibrarySongCandidate,
        lyrics: String
    ) {
        val cleanLyrics =
            lyrics.trim()

        if (cleanLyrics.isBlank()) {
            mutableUiState.update {
                it.copy(
                    writeTargetPicker =
                        it.writeTargetPicker
                            .copy(
                                errorMessage =
                                    "Chưa có lyrics để lưu"
                            ),
                    errorMessage =
                        "Chưa có lyrics để lưu"
                )
            }

            return
        }

        if (mutableUiState.value.isSaving) {
            return
        }

        fileJob?.cancel()

        mutableUiState.update {
            it.copy(
                writeTargetPicker =
                    it.writeTargetPicker
                        .copy(
                            isChecking = true,
                            errorMessage = null
                        ),
                successMessage = null,
                errorMessage = null
            )
        }

        fileJob =
            viewModelScope.launch {
                try {
                    val existing =
                        repository.readStoredLyrics(
                            target
                        )
                            .text
                            .trim()

                    when (
                        decideLyricsWrite(
                            existingLyrics = existing,
                            newLyrics = cleanLyrics
                        )
                    ) {
                        LyricsWriteDecision.WRITE_NOW -> {
                            writeDirectLyrics(
                                target = target,
                                lyrics = cleanLyrics
                            )
                        }

                        LyricsWriteDecision.ALREADY_IDENTICAL -> {
                            val message =
                                "File đã có lyrics trùng khớp: ${target.displayName}"

                            mutableUiState.update {
                                it.copy(
                                    selectedSong =
                                        target,
                                    writeTargetPicker =
                                        WriteTargetPickerState(),
                                    successMessage =
                                        message,
                                    errorMessage = null
                                )
                            }

                            mutableEvents.emit(
                                LyricsEvent.Message(
                                    message
                                )
                            )
                        }

                        LyricsWriteDecision.CONFIRM_REPLACE -> {
                            mutableUiState.update {
                                it.copy(
                                    writeTargetPicker =
                                        it.writeTargetPicker
                                            .copy(
                                                isVisible =
                                                    false,
                                                isChecking =
                                                    false
                                            ),
                                    showDirectOverwriteDialog =
                                        true,
                                    pendingWriteTarget =
                                        target,
                                    pendingWriteLyrics =
                                        cleanLyrics,
                                    pendingExistingLyrics =
                                        existing,
                                    successMessage = null,
                                    errorMessage = null
                                )
                            }
                        }
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
                            writeTargetPicker =
                                it.writeTargetPicker
                                    .copy(
                                        isChecking = false,
                                        errorMessage =
                                            exception.message
                                                ?: "Không kiểm tra được lyrics trong file"
                                    ),
                            errorMessage =
                                exception.message
                                    ?: "Không kiểm tra được lyrics trong file"
                        )
                    }
                }
            }
    }

    private suspend fun writeDirectLyrics(
        target: LibrarySongCandidate,
        lyrics: String
    ) {
        mutableUiState.update {
            it.copy(
                writeTargetPicker =
                    it.writeTargetPicker
                        .copy(
                            isChecking = false,
                            errorMessage = null
                        ),
                isSaving = true,
                successMessage = null,
                errorMessage = null
            )
        }

        try {
            repository.writeLyrics(
                target = target,
                lyrics = lyrics
            )

            val successText =
                "Đã ghi lời vào ${target.displayName}"

            mutableUiState.update {
                it.copy(
                    isSaving = false,
                    selectedSong = target,
                    writeTargetPicker =
                        WriteTargetPickerState(),
                    showDirectOverwriteDialog =
                        false,
                    pendingWriteTarget = null,
                    pendingWriteLyrics = "",
                    pendingExistingLyrics = "",
                    recentlySavedTargetUri =
                        target.uri,
                    successMessage =
                        successText,
                    errorMessage = null
                )
            }

            mutableEvents.emit(
                LyricsEvent.Message(
                    successText
                )
            )

            savedIndicatorJob?.cancel()
            savedIndicatorJob =
                viewModelScope.launch {
                    delay(1_800L)

                    mutableUiState.update {
                        if (
                            it.recentlySavedTargetUri ==
                            target.uri
                        ) {
                            it.copy(
                                recentlySavedTargetUri =
                                    null
                            )
                        } else {
                            it
                        }
                    }
                }
        } catch (
            exception: CancellationException
        ) {
            mutableUiState.update {
                it.copy(
                    isSaving = false
                )
            }

            throw exception
        } catch (
            exception: Exception
        ) {
            val message =
                exception.message
                    ?: "Không ghi được lyrics vào MP3"

            mutableUiState.update {
                it.copy(
                    isSaving = false,
                    showDirectOverwriteDialog =
                        false,
                    writeTargetPicker =
                        it.writeTargetPicker
                            .copy(
                                isVisible =
                                    it.writeTargetPicker
                                        .candidates
                                        .isNotEmpty(),
                                isChecking = false,
                                errorMessage =
                                    message
                            ),
                    successMessage = null,
                    errorMessage = message
                )
            }
        }
    }

    private suspend fun readEmbeddedLyricsOrEmpty(
        song: LibrarySongCandidate
    ): String {
        if (song.uri.isBlank()) {
            return ""
        }

        return try {
            repository.readStoredLyrics(song)
                .text
                .trim()
        } catch (
            exception: CancellationException
        ) {
            throw exception
        } catch (_: Exception) {
            ""
        }
    }

    private fun openEditorForTarget(
        target: LibrarySongCandidate,
        initialLyrics: String
    ) {
        fileJob?.cancel()

        fileJob =
            viewModelScope.launch {
                mutableUiState.update {
                    it.copy(
                        editorTarget = target,
                        screen =
                            LyricsScreen.EDITOR,
                        isLoadingFile = true,
                        existingLyrics = "",
                        editorLyrics =
                            initialLyrics,
                        successMessage = null,
                        errorMessage = null
                    )
                }

                try {
                    val storedLyrics =
                        readEmbeddedLyricsOrEmpty(
                            target
                        )

                    val localLyrics =
                        initialLyrics
                            .trim()
                            .ifBlank {
                                storedLyrics
                            }

                    if (localLyrics.isNotBlank()) {
                        mutableUiState.update {
                            it.copy(
                                isLoadingFile = false,
                                existingLyrics =
                                    storedLyrics,
                                editorLyrics =
                                    localLyrics,
                                errorMessage = null
                            )
                        }

                        return@launch
                    }

                    val onlineResults =
                        repository.searchLyrics(
                            rawTitle =
                                target.title,
                            rawArtist =
                                target.artist,
                            limit = 10
                        )

                    mutableUiState.update {
                        it.copy(
                            screen =
                                LyricsScreen.RESULTS,
                            readerBackScreen =
                                LyricsScreen.RESULTS,
                            isLoadingFile = false,
                            searchResults =
                                onlineResults,
                            selectedResult = null,
                            editorLyrics = "",
                            errorMessage =
                                if (
                                    onlineResults.isEmpty()
                                ) {
                                    "File chưa có lyrics và không tìm thấy kết quả phù hợp trên mạng"
                                } else {
                                    null
                                }
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
                            isLoadingFile = false,
                            errorMessage =
                                exception.message
                                    ?.takeIf(
                                        String::isNotBlank
                                    )
                                    ?: "Không đọc hoặc tra được lyrics"
                        )
                    }
                }
            }
    }

    private fun buildSongQuery(
        song: LibrarySongCandidate
    ): String {
        return buildString {
            append(song.title)

            if (song.artist.isNotBlank()) {
                append(" - ")
                append(song.artist)
            }
        }
    }

    override fun onCleared() {
        suggestionJob?.cancel()
        lyricsSearchJob?.cancel()
        fileJob?.cancel()
        savedIndicatorJob?.cancel()

        super.onCleared()
    }
}
