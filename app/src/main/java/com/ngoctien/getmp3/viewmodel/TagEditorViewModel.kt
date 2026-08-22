package com.ngoctien.getmp3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ngoctien.getmp3.library.InboxWorkflowRepository
import com.ngoctien.getmp3.library.LibraryAdmissionPolicy
import com.ngoctien.getmp3.library.MediaIndexRepository
import com.ngoctien.getmp3.note.ReferenceSongMatch
import com.ngoctien.getmp3.settings.AppSettingsRepository
import com.ngoctien.getmp3.tag.EditableSong
import com.ngoctien.getmp3.tag.MediaSongFile
import com.ngoctien.getmp3.tag.TagEditorRepository
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
import java.text.Normalizer
import java.util.Locale

enum class ArtistCaseMode {
    KEEP_ORIGINAL,
    CAPITALIZE_WORDS
}

private val DefaultAlbumOptions =
    listOf(
        "Nhạc Việt",
        "US-UK",
        "Nhạc Trung",
        "Nhạc Hàn",
        "Nhạc Nhật",
        "Remix",
        "Lofi",
        "Speed Up",
        "Instrumental"
    )

data class TagEditorUiState(
    val files: List<MediaSongFile> =
        emptyList(),

    val currentIndex: Int = 0,

    val currentSong: EditableSong? =
        null,

    val title: String = "",

    val artist: String = "",

    val artistCaseMode: ArtistCaseMode =
        ArtistCaseMode.CAPITALIZE_WORDS,

    val selectedAlbum: String =
        DefaultAlbumOptions.first(),

    val year: String = "",

    val albumOptions: List<String> =
        DefaultAlbumOptions,

    val knownArtistCount: Int = 0,

    val artistCandidates: List<String> =
        emptyList(),

    val showArtistCandidates: Boolean =
        false,

    val isScanning: Boolean = false,

    val isLoadingSong: Boolean = false,

    val isSaving: Boolean = false,

    val isDeleting: Boolean = false,

    val errorMessage: String? = null
) {
    val totalFiles: Int
        get() = files.size

    val displayIndex: Int
        get() =
            if (files.isEmpty()) {
                0
            } else {
                currentIndex + 1
            }

    val canGoNext: Boolean
        get() =
            files.isNotEmpty() &&
                currentIndex < files.lastIndex

    /*
     * Title của tên file luôn dùng dạng viết hoa chữ đầu.
     *
     * Không ép lại state trong lúc người dùng đang gõ để tránh
     * làm cursor nhảy khi sửa ở giữa chuỗi.
     */
    val effectiveTitle: String
        get() =
            capitalizeWords(
                normalizeSpaces(title)
            )

    val effectiveArtist: String
        get() =
            formatArtistForSave(
                value = artist,
                mode = artistCaseMode
            )

    val previewFileName: String
        get() =
            buildPreviewFileName(
                title = effectiveTitle,
                artist = effectiveArtist
            )
}

data class TagEditorEvent(
    val message: String
)

class TagEditorViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        TagEditorRepository(application)

    private val settingsRepository =
        AppSettingsRepository(application)

    private val mediaIndexRepository =
        MediaIndexRepository(application)

    private val inboxWorkflowRepository =
        InboxWorkflowRepository(
            application
        )
    private val mutableUiState =
        MutableStateFlow(
            TagEditorUiState()
        )

    val uiState: StateFlow<TagEditorUiState> =
        mutableUiState.asStateFlow()

    private val mutableEvents =
        MutableSharedFlow<TagEditorEvent>(
            extraBufferCapacity = 8
        )

    val events: SharedFlow<TagEditorEvent> =
        mutableEvents.asSharedFlow()

    private var hasLoadedOnce =
        false

    /**
     * Switching back to Sửa thẻ should not rescan/reset the working list.
     * The list button still calls refreshFileList() when the user wants a
     * fresh inventory.
     */
    fun ensureLoaded() {
        if (hasLoadedOnce) {
            return
        }

        refresh()
    }

    fun refresh() {
        val state =
            mutableUiState.value

        if (
            state.isScanning ||
            state.isSaving ||
            state.isDeleting
        ) {
            return
        }

        viewModelScope.launch {
            val libraryAlbums =
                mediaIndexRepository
                    .getLibraryAlbums()

            val knownArtists =
                mediaIndexRepository
                    .getLibraryArtists()

            val albumOptions =
                (
                    DefaultAlbumOptions +
                        libraryAlbums
                    )
                    .map(::normalizeSpaces)
                    .filter {
                        it.isNotBlank()
                    }
                    .distinctBy {
                        normalizeText(it)
                    }

            val finalAlbumOptions =
                albumOptions.ifEmpty {
                    DefaultAlbumOptions
                }

            val selectedAlbum =
                state.selectedAlbum
                    .takeIf {
                        it in finalAlbumOptions
                    }
                    ?: finalAlbumOptions.first()

            mutableUiState.update {
                it.copy(
                    isScanning = true,
                    errorMessage = null,

                    albumOptions =
                        finalAlbumOptions,

                    selectedAlbum =
                        selectedAlbum,

                    knownArtistCount =
                        knownArtists.size,

                    artistCandidates =
                        emptyList(),

                    showArtistCandidates =
                        false
                )
            }

            try {
                val files =
                    mediaIndexRepository
                        .syncInboxLibrary()

                hasLoadedOnce =
                    true

                mutableUiState.update {
                    it.copy(
                        files = files,

                        currentIndex = 0,

                        currentSong = null,

                        title = "",

                        artist = "",

                        year = "",

                        isScanning = false,

                        isLoadingSong = false,

                        artistCandidates =
                            emptyList(),

                        showArtistCandidates =
                            false
                    )
                }

                if (files.isNotEmpty()) {
                    loadSongAt(0)
                }
            } catch (exception: Exception) {
                mutableUiState.update {
                    it.copy(
                        isScanning = false,

                        isLoadingSong = false,

                        errorMessage =
                            exception.message
                                ?.takeIf(String::isNotBlank)
                                ?: "Không quét được thư mục MP3"
                    )
                }
            }
        }
    }

    fun openReferenceSong(
        match: ReferenceSongMatch
    ) {
        val state =
            mutableUiState.value

        if (
            state.isScanning ||
            state.isLoadingSong ||
            state.isSaving ||
            state.isDeleting
        ) {
            mutableEvents.tryEmit(
                TagEditorEvent(
                    "Sửa thẻ đang bận. Hãy thử lại sau."
                )
            )

            return
        }

        val referenceFile =
            MediaSongFile(
                id =
                    match.uri.hashCode()
                        .toLong(),
                uri = match.uri,
                displayName =
                    match.displayName,
                sizeBytes = 0L,
                dateModifiedSeconds = 0L,
                treeUri = match.treeUri
            )

        viewModelScope.launch {
            val latestFiles =
                mutableUiState.value.files
                    .toMutableList()

            val existingIndex =
                latestFiles.indexOfFirst {
                    it.uri == match.uri
                }

            val targetIndex =
                if (existingIndex >= 0) {
                    existingIndex
                } else {
                    latestFiles.add(
                        index = 0,
                        element = referenceFile
                    )

                    0
                }

            mutableUiState.update {
                it.copy(
                    files = latestFiles,
                    currentIndex =
                        targetIndex,
                    currentSong = null,
                    title = "",
                    artist = "",

                    year = "",
                    isLoadingSong = false,
                    errorMessage = null,
                    artistCandidates =
                        emptyList(),
                    showArtistCandidates =
                        false
                )
            }

            loadSongAt(targetIndex)
        }
    }
    fun refreshFileList() {
        val state =
            mutableUiState.value

        if (
            state.isScanning ||
            state.isSaving ||
            state.isDeleting ||
            state.isLoadingSong
        ) {
            return
        }

        viewModelScope.launch {
            mutableUiState.update {
                it.copy(
                    isScanning = true,
                    errorMessage = null
                )
            }

            try {
                val files =
                    mediaIndexRepository
                        .syncInboxLibrary()

                if (files.isEmpty()) {
                    mutableUiState.update {
                        it.copy(
                            files = emptyList(),
                            currentIndex = 0,
                            currentSong = null,
                            title = "",
                            artist = "",

                            year = "",
                            isScanning = false,
                            isLoadingSong = false,
                            errorMessage = null,
                            artistCandidates =
                                emptyList(),
                            showArtistCandidates =
                                false
                        )
                    }

                    return@launch
                }

                val currentSong =
                    state.currentSong

                val matchingIndex =
                    currentSong
                        ?.file
                        ?.let { currentFile ->
                            files.indexOfFirst {
                                    candidate ->

                                candidate.uri ==
                                    currentFile.uri ||
                                    candidate.id ==
                                    currentFile.id ||
                                    candidate.displayName.equals(
                                        currentFile.displayName,
                                        ignoreCase = true
                                    )
                            }
                        }
                        ?: -1

                if (
                    currentSong != null &&
                    matchingIndex >= 0
                ) {
                    val refreshedFile =
                        files[matchingIndex]

                    mutableUiState.update {
                        it.copy(
                            files = files,

                            currentIndex =
                                matchingIndex,

                            currentSong =
                                currentSong.copy(
                                    file =
                                        refreshedFile
                                ),

                            isScanning = false,
                            isLoadingSong = false,
                            errorMessage = null,

                            artistCandidates =
                                emptyList(),

                            showArtistCandidates =
                                false
                        )
                    }
                } else {
                    val targetIndex =
                        state.currentIndex.coerceIn(
                            0,
                            files.lastIndex
                        )

                    mutableUiState.update {
                        it.copy(
                            files = files,

                            currentIndex =
                                targetIndex,

                            currentSong = null,

                            isScanning = false,
                            isLoadingSong = false,
                            errorMessage = null,

                            artistCandidates =
                                emptyList(),

                            showArtistCandidates =
                                false
                        )
                    }

                    loadSongAt(
                        targetIndex
                    )
                }
            } catch (exception: Exception) {
                val message =
                    exception.message
                        ?.takeIf(
                            String::isNotBlank
                        )
                        ?: "Kh\u00f4ng qu\u00e9t l\u1ea1i \u0111\u01b0\u1ee3c danh s\u00e1ch MP3"

                mutableUiState.update {
                    it.copy(
                        isScanning = false,
                        isLoadingSong = false,
                        errorMessage = message
                    )
                }

                mutableEvents.emit(
                    TagEditorEvent(
                        message
                    )
                )
            }
        }
    }
    fun setTitle(
        value: String
    ) {
        mutableUiState.update {
            it.copy(
                title = value
            )
        }
    }

    fun clearTitle() {
        mutableUiState.update {
            it.copy(
                title = ""
            )
        }
    }

    fun setArtist(
        value: String
    ) {
        mutableUiState.update {
            it.copy(
                artist = value
            )
        }
    }

    fun clearArtist() {
        mutableUiState.update {
            it.copy(
                artist = ""
            )
        }
    }

    fun setArtistCaseMode(
        mode: ArtistCaseMode
    ) {
        mutableUiState.update {
            it.copy(
                artistCaseMode = mode
            )
        }
    }

    fun setYear(
        value: String
    ) {
        mutableUiState.update {
            it.copy(
                year =
                    value
                        .filter(
                            Char::isDigit
                        )
                        .take(
                            4
                        )
            )
        }
    }

    fun setAlbum(
        album: String
    ) {
        val state =
            mutableUiState.value

        if (album !in state.albumOptions) {
            return
        }

        mutableUiState.update {
            it.copy(
                selectedAlbum = album
            )
        }
    }

    fun quickFormat() {
        val state =
            mutableUiState.value

        if (
            state.isScanning ||
            state.isLoadingSong ||
            state.isSaving ||
            state.isDeleting
        ) {
            return
        }

        val settings =
            settingsRepository.getSettings()

        if (!settings.hasCompareFolder) {
            mutableEvents.tryEmit(
                TagEditorEvent(
                    "Hãy chọn thư mục đối chiếu trong Cài đặt trước"
                )
            )

            return
        }

        viewModelScope.launch {
            try {
                val knownArtists =
                    mediaIndexRepository
                        .getLibraryArtists()

                if (knownArtists.isEmpty()) {
                    mutableEvents.emit(
                        TagEditorEvent(
                            "Chưa có dữ liệu thư viện. Hãy vào Cài đặt → Chuẩn bị dữ liệu."
                        )
                    )

                    return@launch
                }

                val result =
                    withContext(
                        Dispatchers.Default
                    ) {
                        val cleanTitle =
                            QuickFormatEngine.cleanTitle(
                                title = state.title,
                                filterTerms =
                                    settings.titleFilterTerms,
                                filterSymbols =
                                    settings.titleFilterSymbols
                            )

                        val cleanArtist =
                            QuickFormatEngine.cleanArtist(
                                state.artist
                            )

                        val candidates =
                            QuickFormatEngine
                                .findArtistCandidates(
                                    rawArtist =
                                        cleanArtist,
                                    rawTitle =
                                        cleanTitle,
                                    knownArtists =
                                        knownArtists
                                )

                        QuickFormatResult(
                            title = cleanTitle,
                            artist = cleanArtist,
                            candidates = candidates
                        )
                    }

                mutableUiState.update {
                    it.copy(
                        title =
                            capitalizeWords(
                                result.title
                            ),
                        artist =
                            result.artist,
                        artistCandidates =
                            result.candidates,
                        showArtistCandidates =
                            result.candidates
                                .isNotEmpty(),
                        errorMessage = null
                    )
                }

                if (result.candidates.isEmpty()) {
                    mutableEvents.emit(
                        TagEditorEvent(
                            "Đã format Title nhưng chưa tìm thấy Artist phù hợp. Hãy tự nhập."
                        )
                    )
                }
            } catch (exception: Exception) {
                mutableEvents.emit(
                    TagEditorEvent(
                        exception.message
                            ?.takeIf(String::isNotBlank)
                            ?: "Không thể Format nhanh"
                    )
                )
            }
        }
    }

    fun selectArtistCandidate(
        artist: String
    ) {
        val state =
            mutableUiState.value

        val settings =
            settingsRepository.getSettings()

        val cleanArtist =
            QuickFormatEngine.cleanArtist(
                artist
            )

        val finalArtist =
            when (state.artistCaseMode) {
                ArtistCaseMode.KEEP_ORIGINAL ->
                    cleanArtist

                ArtistCaseMode.CAPITALIZE_WORDS ->
                    capitalizeWords(
                        cleanArtist
                    )
            }

        val titleWithoutArtist =
            QuickFormatEngine
                .removeArtistFromTitle(
                    title = state.title,
                    artist = cleanArtist
                )

        val finalTitle =
            QuickFormatEngine.cleanTitle(
                title = titleWithoutArtist,

                filterTerms =
                    settings.titleFilterTerms,

                filterSymbols =
                    settings.titleFilterSymbols
            )

        mutableUiState.update {
            it.copy(
                title =
                    capitalizeWords(
                        finalTitle
                    ),

                artist =
                    finalArtist,

                artistCandidates =
                    emptyList(),

                showArtistCandidates =
                    false
            )
        }
    }

    fun dismissArtistCandidates() {
        mutableUiState.update {
            it.copy(
                artistCandidates =
                    emptyList(),

                showArtistCandidates =
                    false
            )
        }
    }

    fun skip() {
        val state =
            mutableUiState.value

        if (
            state.isSaving ||
            state.isDeleting ||
            state.isLoadingSong
        ) {
            return
        }

        if (state.canGoNext) {
            viewModelScope.launch {
                loadSongAt(
                    state.currentIndex + 1
                )
            }
        } else {
            mutableEvents.tryEmit(
                TagEditorEvent(
                    "Đây là file cuối cùng"
                )
            )
        }
    }

    fun selectFile(
        index: Int
    ) {
        val state =
            mutableUiState.value

        if (
            state.isSaving ||
            state.isDeleting ||
            state.isLoadingSong ||
            index !in state.files.indices
        ) {
            return
        }

        viewModelScope.launch {
            loadSongAt(index)
        }
    }

    fun saveAndNext() {

        val state =
            mutableUiState.value

        val currentSong =
            state.currentSong
                ?: return

        if (
            state.isSaving ||
            state.isDeleting ||
            state.isLoadingSong
        ) {
            return
        }

        val cleanTitle =
            capitalizeWords(
                normalizeSpaces(
                    state.title
                )
            )

        val cleanArtist =
            formatArtistForSave(
                value =
                    state.artist,

                mode =
                    state.artistCaseMode
            )

        val cleanAlbum =
            normalizeSpaces(
                state.selectedAlbum
            )

        val cleanYear =
            state.year.trim()

        if (
            !Regex("""\d{4}""")
                .matches(
                    cleanYear
                )
        ) {
            mutableEvents.tryEmit(
                TagEditorEvent(
                    "Year phải gồm đúng 4 chữ số"
                )
            )

            return
        }

        if (cleanTitle.isBlank()) {
            mutableEvents.tryEmit(
                TagEditorEvent(
                    "Title không được để trống"
                )
            )

            return
        }

        if (cleanArtist.isBlank()) {
            mutableEvents.tryEmit(
                TagEditorEvent(
                    "Artist không được để trống"
                )
            )

            return
        }

        if (cleanAlbum.isBlank()) {
            mutableEvents.tryEmit(
                TagEditorEvent(
                    "Hãy chọn Album"
                )
            )

            return
        }

        viewModelScope.launch {

            mutableUiState.update {
                it.copy(
                    isSaving =
                        true,

                    errorMessage =
                        null
                )
            }

            try {

                val updatedSong =
                    repository.saveSong(
                        song =
                            currentSong,

                        title =
                            cleanTitle,

                        artist =
                            cleanArtist,

                        album =
                            cleanAlbum,

                        year =
                            cleanYear
                    )

                /*
                 * Cập nhật đúng một file.
                 * Không scan lại toàn Inbox.
                 */
                val indexed =
                    mediaIndexRepository
                        .refreshEditedFile(
                            oldUri =
                                currentSong
                                    .file
                                    .uri,

                            updatedFile =
                                updatedSong
                                    .file
                        )

                val admission =
                    LibraryAdmissionPolicy
                        .evaluate(
                            indexed
                        )

                val promotion =
                    if (
                        admission.allowed
                    ) {

                        runCatching {
                            inboxWorkflowRepository
                                .promoteToLibrary(
                                    updatedSong
                                        .file
                                        .uri
                                )
                        }
                            .getOrNull()

                    }
                    else {

                        null
                    }

                val promoted =
                    promotion != null

                val latestState =
                    mutableUiState.value

                val actualIndex =
                    latestState
                        .files
                        .indexOfFirst {
                            it.uri ==
                                currentSong
                                    .file
                                    .uri ||
                                it.uri ==
                                updatedSong
                                    .file
                                    .uri
                        }
                        .takeIf {
                            it >= 0
                        }
                        ?: latestState
                            .currentIndex

                val updatedFiles =
                    latestState
                        .files
                        .toMutableList()
                        .apply {

                            if (
                                actualIndex in
                                indices
                            ) {

                                if (promoted) {
                                    removeAt(
                                        actualIndex
                                    )
                                }
                                else {
                                    this[
                                        actualIndex
                                    ] =
                                        updatedSong
                                            .file
                                }
                            }
                        }

                mutableUiState.update {
                    it.copy(
                        files =
                            updatedFiles,

                        isSaving =
                            false,

                        artistCandidates =
                            emptyList(),

                        showArtistCandidates =
                            false
                    )
                }

                if (promoted) {

                    mutableEvents.emit(
                        TagEditorEvent(
                            "Đã chuẩn hóa và đưa vào Library: " +
                                promotion
                                    ?.displayName
                        )
                    )
                }
                else {

                    val reason =
                        admission
                            .message
                            .takeIf(
                                String::isNotBlank
                            )

                    mutableEvents.emit(
                        TagEditorEvent(
                            if (reason != null) {
                                "Đã lưu. File vẫn ở Inbox: $reason"
                            }
                            else {
                                "Đã lưu ${updatedSong.file.displayName}"
                            }
                        )
                    )
                }

                if (
                    updatedFiles
                        .isEmpty()
                ) {

                    mutableUiState.update {
                        it.copy(
                            files =
                                emptyList(),

                            currentIndex =
                                0,

                            currentSong =
                                null,

                            title =
                                "",

                            artist =
                                "",

                            year =
                                "",

                            isSaving =
                                false,

                            isLoadingSong =
                                false
                        )
                    }

                    return@launch
                }

                val targetIndex =
                    if (promoted) {

                        actualIndex.coerceAtMost(
                            updatedFiles
                                .lastIndex
                        )
                    }
                    else if (
                        actualIndex <
                        updatedFiles.lastIndex
                    ) {

                        actualIndex + 1
                    }
                    else {

                        actualIndex
                    }

                if (
                    !promoted &&
                    targetIndex ==
                    actualIndex &&
                    actualIndex ==
                    updatedFiles
                        .lastIndex
                ) {

                    mutableUiState.update {
                        it.copy(
                            currentIndex =
                                actualIndex,

                            currentSong =
                                updatedSong,

                            title =
                                cleanTitle,

                            artist =
                                cleanArtist,

                            selectedAlbum =
                                cleanAlbum,

                            year =
                                cleanYear,

                            isSaving =
                                false
                        )
                    }

                    mutableEvents.emit(
                        TagEditorEvent(
                            "Đã đến file cuối cùng"
                        )
                    )
                }
                else {

                    mutableUiState.update {
                        it.copy(
                            currentIndex =
                                targetIndex,

                            currentSong =
                                null,

                            title =
                                "",

                            artist =
                                "",

                            year =
                                "",

                            isSaving =
                                false,

                            isLoadingSong =
                                false
                        )
                    }

                    loadSongAt(
                        targetIndex
                    )
                }

            } catch (
                exception: Exception
            ) {

                mutableUiState.update {
                    it.copy(
                        isSaving =
                            false,

                        errorMessage =
                            exception.message
                                ?.takeIf(
                                    String::isNotBlank
                                )
                                ?: "Không xử lý được file"
                    )
                }
            }
        }
    }

    fun deleteCurrentSong() {
        val state =
            mutableUiState.value

        val currentSong =
            state.currentSong
                ?: return

        if (
            state.isScanning ||
            state.isLoadingSong ||
            state.isSaving ||
            state.isDeleting
        ) {
            return
        }

        viewModelScope.launch {
            mutableUiState.update {
                it.copy(
                    isDeleting = true,
                    errorMessage = null
                )
            }

            try {
                repository.deleteSong(
                    currentSong.file
                )


                mediaIndexRepository
                    .deleteUri(
                        currentSong.file.uri
                    )

                val latestState =
                    mutableUiState.value

                val actualIndex =
                    latestState.files
                        .indexOfFirst {
                            it.uri ==
                                currentSong.file.uri
                        }
                        .takeIf {
                            it >= 0
                        }
                        ?: latestState.currentIndex

                val remainingFiles =
                    latestState.files
                        .toMutableList()
                        .apply {
                            if (
                                actualIndex in indices
                            ) {
                                removeAt(actualIndex)
                            }
                        }

                if (remainingFiles.isEmpty()) {
                    mutableUiState.update {
                        it.copy(
                            files =
                                emptyList(),

                            currentIndex = 0,

                            currentSong = null,

                            title = "",

                            artist = "",

                            year = "",

                            artistCandidates =
                                emptyList(),

                            showArtistCandidates =
                                false,

                            isDeleting = false,

                            isSaving = false,

                            isLoadingSong = false
                        )
                    }
                } else {
                    val nextIndex =
                        actualIndex.coerceAtMost(
                            remainingFiles.lastIndex
                        )

                    mutableUiState.update {
                        it.copy(
                            files =
                                remainingFiles,

                            currentIndex =
                                nextIndex,

                            currentSong = null,

                            title = "",

                            artist = "",

                            year = "",

                            artistCandidates =
                                emptyList(),

                            showArtistCandidates =
                                false,

                            isDeleting = false,

                            isSaving = false,

                            isLoadingSong = false
                        )
                    }

                    loadSongAt(nextIndex)
                }

                mutableEvents.emit(
                    TagEditorEvent(
                        "Đã xóa ${currentSong.file.displayName}"
                    )
                )
            } catch (exception: Exception) {
                mutableUiState.update {
                    it.copy(
                        isDeleting = false,

                        isSaving = false,

                        errorMessage =
                            exception.message
                                ?.takeIf(String::isNotBlank)
                                ?: "Không xóa được file"
                    )
                }
            }
        }
    }

    private suspend fun loadSongAt(
        index: Int
    ) {
        val files =
            mutableUiState.value.files

        if (files.isEmpty()) {
            return
        }

        val safeIndex =
            index.coerceIn(
                0,
                files.lastIndex
            )

        mutableUiState.update {
            it.copy(
                currentIndex =
                    safeIndex,

                currentSong = null,

                title = "",

                artist = "",

                year = "",

                isLoadingSong = true,

                errorMessage = null,

                artistCandidates =
                    emptyList(),

                showArtistCandidates =
                    false
            )
        }

        try {
            val file =
                files[safeIndex]

            /*
             * Fast path: the shared media index already contains the fields
             * needed to render the editor. Opening the MP3 and copying it to
             * cache is only a compatibility fallback when the row is missing.
             */
            val indexed =
                mediaIndexRepository
                    .getByUri(
                        file.uri
                    )

            val song =
                if (indexed != null) {
                    EditableSong(
                        file = file,
                        title = indexed.fileTitle
                            .ifBlank {
                                indexed.title
                            },
                        artist = indexed.fileArtist
                            .ifBlank {
                                indexed.artist
                            },
                        album = indexed.album,
                        coverPath = indexed.coverPath,
                        year = indexed.year
                    )
                } else {
                    repository.loadSong(
                        file
                    )
                }

            /*
             * Album phải lấy từ metadata thật của bài đang mở,
             * không được giữ Album của bài trước hoặc Album mặc định.
             */
            val loadedAlbum =
                normalizeSpaces(
                    song.album
                )

            val currentAlbumOptions =
                mutableUiState
                    .value
                    .albumOptions

            val matchingAlbum =
                loadedAlbum
                    .takeIf {
                        it.isNotBlank()
                    }
                    ?.let { album ->
                        currentAlbumOptions
                            .firstOrNull { option ->
                                normalizeText(option) ==
                                    normalizeText(album)
                            }
                    }

            /*
             * Nếu MP3 có Album mà danh sách hiện tại chưa có,
             * thêm Album đó vào dropdown để UI vẫn hiển thị đúng.
             */
            val resolvedAlbumOptions =
                when {
                    loadedAlbum.isBlank() ->
                        currentAlbumOptions

                    matchingAlbum != null ->
                        currentAlbumOptions

                    else ->
                        currentAlbumOptions +
                            loadedAlbum
                }

            val selectedAlbum =
                when {
                    loadedAlbum.isBlank() ->
                        resolvedAlbumOptions
                            .firstOrNull()
                            .orEmpty()

                    matchingAlbum != null ->
                        matchingAlbum

                    else ->
                        loadedAlbum
                }

            mutableUiState.update {
                it.copy(
                    currentSong =
                        song,

                    title =
                        capitalizeWords(
                            normalizeSpaces(
                                song.title
                            )
                        ),

                    artist =
                        normalizeSpaces(
                            song.artist
                        ),

                    albumOptions =
                        resolvedAlbumOptions,

                    selectedAlbum =
                        selectedAlbum,

                    year =
                        song.year,

                    isLoadingSong =
                        false
                )
            }
        } catch (exception: Exception) {
            mutableUiState.update {
                it.copy(
                    isLoadingSong = false,

                    errorMessage =
                        exception.message
                            ?.takeIf(String::isNotBlank)
                            ?: "Không đọc được metadata"
                )
            }
        }
    }
}

private data class QuickFormatResult(
    val title: String,
    val artist: String,
    val candidates: List<String>
)

private object QuickFormatEngine {

    fun cleanTitle(
        title: String,
        filterTerms: List<String>,
        filterSymbols: String
    ): String {
        var result =
            normalizeSpaces(
                stripDecorativeSymbols(
                    title
                )
            )

        result =
            removeGroupedContent(result)

        filterTerms
            .map(::normalizeSpaces)
            .filter {
                it.isNotBlank()
            }
            .distinctBy {
                normalizeText(it)
            }
            .sortedByDescending {
                it.length
            }
            .forEach { term ->
                result =
                    replaceIgnoreCase(
                        source = result,
                        target = term,
                        replacement = " "
                    )
            }

        filterSymbols.forEach { symbol ->
            result =
                result.replace(
                    oldChar = symbol,
                    newChar = ' '
                )
        }

        val separators =
            charArrayOf(
                '-',
                '–',
                '—',
                '_',
                '|',
                '•',
                '~',
                '#',
                '@',
                '/',
                '\\',
                '·',
                '…',
                '"',
                '\'',
                '!',
                '！',
                '?',
                '？'
            )

        separators.forEach { symbol ->
            result =
                result.replace(
                    oldChar = symbol,
                    newChar = ' '
                )
        }

        result =
            removeKnownAudioExtension(
                result
            )

        return normalizeSpaces(result)
            .trim(
                '-',
                '_',
                '|',
                '•',
                '.',
                ',',
                ';',
                ':'
            )
            .let(::normalizeSpaces)
    }

    fun cleanArtist(
        artist: String
    ): String {
        return normalizeSpaces(
            stripDecorativeSymbols(
                artist
            )
        )
    }

    fun removeArtistFromTitle(
        title: String,
        artist: String
    ): String {
        val cleanArtist =
            normalizeSpaces(artist)

        if (cleanArtist.isBlank()) {
            return normalizeSpaces(title)
        }

        return replaceIgnoreCase(
            source = title,
            target = cleanArtist,
            replacement = " "
        )
            .let(::normalizeSpaces)
            .trim(
                '-',
                '_',
                '|',
                '•',
                '.',
                ',',
                ';',
                ':'
            )
            .let(::normalizeSpaces)
    }

    fun findArtistCandidates(
        rawArtist: String,
        rawTitle: String,
        knownArtists: List<String>
    ): List<String> {
        if (knownArtists.isEmpty()) {
            return emptyList()
        }

        val artistQuery =
            normalizeText(rawArtist)

        val titleQuery =
            normalizeText(rawTitle)

        return knownArtists
            .asSequence()
            .map(::normalizeSpaces)
            .filter {
                it.isNotBlank()
            }
            .mapNotNull { knownArtist ->
                val normalizedKnown =
                    normalizeText(
                        knownArtist
                    )

                if (normalizedKnown.isBlank()) {
                    return@mapNotNull null
                }

                val score =
                    scoreCandidate(
                        artistQuery =
                            artistQuery,

                        titleQuery =
                            titleQuery,

                        known =
                            normalizedKnown
                    )

                if (score >= 58.0) {
                    ArtistCandidate(
                        artist =
                            knownArtist,

                        score =
                            score
                    )
                } else {
                    null
                }
            }
            .sortedByDescending {
                it.score
            }
            .distinctBy {
                normalizeText(
                    it.artist
                )
            }
            .take(8)
            .map {
                it.artist
            }
            .toList()
    }

    private fun scoreCandidate(
        artistQuery: String,
        titleQuery: String,
        known: String
    ): Double {
        var score = 0.0

        if (artistQuery.isNotBlank()) {
            score =
                when {
                    artistQuery == known ->
                        100.0

                    artistQuery.contains(known) ->
                        94.0

                    known.contains(artistQuery) &&
                        artistQuery.length >= 3 ->
                        86.0

                    else ->
                        0.0
                }

            score =
                maxOf(
                    score,

                    tokenSimilarity(
                        artistQuery,
                        known
                    ) * 82.0
                )

            if (
                artistQuery.length >= 3 &&
                known.length >= 3
            ) {
                score =
                    maxOf(
                        score,

                        stringSimilarity(
                            artistQuery,
                            known
                        ) * 78.0
                    )
            }
        }

        if (
            titleQuery.isNotBlank() &&
            titleQuery.contains(known)
        ) {
            score =
                maxOf(
                    score,
                    90.0
                )
        }

        return score
    }

    private fun tokenSimilarity(
        left: String,
        right: String
    ): Double {
        val leftTokens =
            left.split(' ')
                .filter(String::isNotBlank)
                .toSet()

        val rightTokens =
            right.split(' ')
                .filter(String::isNotBlank)
                .toSet()

        if (
            leftTokens.isEmpty() ||
            rightTokens.isEmpty()
        ) {
            return 0.0
        }

        val intersection =
            leftTokens
                .intersect(rightTokens)
                .size

        val union =
            leftTokens
                .union(rightTokens)
                .size

        return intersection.toDouble() /
            union.toDouble()
    }

    private fun stringSimilarity(
        left: String,
        right: String
    ): Double {
        val maximumLength =
            maxOf(
                left.length,
                right.length
            )

        if (maximumLength == 0) {
            return 1.0
        }

        val distance =
            levenshteinDistance(
                left,
                right
            )

        return 1.0 -
            distance.toDouble() /
            maximumLength.toDouble()
    }

    private fun levenshteinDistance(
        left: String,
        right: String
    ): Int {
        if (left == right) {
            return 0
        }

        if (left.isEmpty()) {
            return right.length
        }

        if (right.isEmpty()) {
            return left.length
        }

        var previous =
            IntArray(
                right.length + 1
            ) {
                it
            }

        var current =
            IntArray(
                right.length + 1
            )

        left.forEachIndexed {
                leftIndex,
                leftCharacter ->

            current[0] =
                leftIndex + 1

            right.forEachIndexed {
                    rightIndex,
                    rightCharacter ->

                val insert =
                    current[rightIndex] + 1

                val delete =
                    previous[
                        rightIndex + 1
                    ] + 1

                val replace =
                    previous[rightIndex] +
                        if (
                            leftCharacter ==
                            rightCharacter
                        ) {
                            0
                        } else {
                            1
                        }

                current[
                    rightIndex + 1
                ] = minOf(
                    insert,
                    delete,
                    replace
                )
            }

            val temporary =
                previous

            previous =
                current

            current =
                temporary
        }

        return previous[right.length]
    }

    private fun stripDecorativeSymbols(
        value: String
    ): String {
        val output =
            StringBuilder(value.length)

        var index = 0

        while (index < value.length) {
            val codePoint =
                Character.codePointAt(
                    value,
                    index
                )

            index +=
                Character.charCount(
                    codePoint
                )

            val type =
                Character.getType(
                    codePoint
                )

            val shouldRemove =
                type ==
                    Character.OTHER_SYMBOL.toInt() ||
                    type ==
                    Character.MODIFIER_SYMBOL.toInt() ||
                    type ==
                    Character.PRIVATE_USE.toInt() ||
                    type ==
                    Character.SURROGATE.toInt() ||
                    type ==
                    Character.FORMAT.toInt() ||
                    codePoint in
                        0x1F1E6..0x1FAFF ||
                    codePoint in
                        0x2600..0x27BF ||
                    codePoint in
                        0xFE00..0xFE0F

            if (shouldRemove) {
                output.append(' ')
            } else {
                output.appendCodePoint(
                    codePoint
                )
            }
        }

        return output.toString()
    }

    private fun removeGroupedContent(
        value: String
    ): String {
        if (value.isEmpty()) {
            return value
        }

        val removeFlags =
            BooleanArray(
                value.length
            )

        val stack =
            mutableListOf<
                Pair<Char, Int>
                >()

        fun expectedOpening(
            closing: Char
        ): Char? {
            return when (closing) {
                ')' -> '('
                ']' -> '['
                '}' -> '{'
                else -> null
            }
        }

        value.forEachIndexed {
                index,
                character ->

            when (character) {
                '(',
                '[',
                '{' -> {
                    stack.add(
                        character to index
                    )
                }

                ')',
                ']',
                '}' -> {
                    val expected =
                        expectedOpening(
                            character
                        )

                    val latest =
                        stack.lastOrNull()

                    if (
                        latest != null &&
                        latest.first == expected
                    ) {
                        stack.removeAt(
                            stack.lastIndex
                        )

                        for (
                            removeIndex in
                            latest.second..index
                        ) {
                            removeFlags[
                                removeIndex
                            ] = true
                        }
                    }
                }
            }
        }

        val output =
            StringBuilder(
                value.length
            )

        value.indices.forEach { index ->
            if (removeFlags[index]) {
                if (
                    index == 0 ||
                    !removeFlags[index - 1]
                ) {
                    output.append(' ')
                }
            } else {
                output.append(
                    value[index]
                )
            }
        }

        return output.toString()
    }

    private fun removeKnownAudioExtension(
        value: String
    ): String {
        val clean =
            normalizeSpaces(value)

        val extensions =
            listOf(
                ".mp3",
                ".m4a",
                ".webm",
                ".aac",
                ".wav",
                ".flac"
            )

        val extension =
            extensions.firstOrNull {
                clean.endsWith(
                    suffix = it,
                    ignoreCase = true
                )
            } ?: return clean

        return clean
            .dropLast(
                extension.length
            )
            .trim()
    }

    private data class ArtistCandidate(
        val artist: String,
        val score: Double
    )
}

private fun formatArtistForSave(
    value: String,
    mode: ArtistCaseMode
): String {
    val clean =
        QuickFormatEngine.cleanArtist(
            value
        )

    return when (mode) {
        ArtistCaseMode.KEEP_ORIGINAL ->
            clean

        ArtistCaseMode.CAPITALIZE_WORDS ->
            capitalizeWords(clean)
    }
}

private fun capitalizeWords(
    input: String
): String {
    if (input.isBlank()) {
        return input
    }

    val output =
        StringBuilder(
            input.length
        )

    var capitalizeNext = true

    input.forEach { character ->
        when {
            character.isLetter() -> {
                if (capitalizeNext) {
                    output.append(
                        character.titlecase()
                    )
                } else {
                    output.append(
                        character.lowercase()
                    )
                }

                capitalizeNext = false
            }

            character.isDigit() -> {
                output.append(character)

                capitalizeNext = false
            }

            else -> {
                output.append(character)

                capitalizeNext =
                    character.isWhitespace() ||
                        character == '-' ||
                        character == '/' ||
                        character == '(' ||
                        character == '['
            }
        }
    }

    return output.toString()
}

private fun buildPreviewFileName(
    title: String,
    artist: String
): String {
    val cleanTitle =
        normalizeSpaces(title)
            .ifBlank {
                "Title"
            }

    val cleanArtist =
        normalizeSpaces(artist)
            .ifBlank {
                "Artist"
            }

    val rawName =
        "$cleanTitle - $cleanArtist.mp3"

    val output =
        StringBuilder(
            rawName.length
        )

    rawName.forEach { character ->
        when {
            character.code < 32 -> Unit

            character == '\\' ||
                character == '/' ||
                character == ':' ||
                character == '*' ||
                character == '?' ||
                character == '"' ||
                character == '<' ||
                character == '>' ||
                character == '|' -> {
                output.append('_')
            }

            else ->
                output.append(character)
        }
    }

    return normalizeSpaces(
        output.toString()
    )
        .trimEnd(
            '.',
            ' '
        )
        .take(170)
}

private fun normalizeSpaces(
    value: String
): String {
    val output =
        StringBuilder(
            value.length
        )

    var previousWasSpace = true
    var index = 0

    while (index < value.length) {
        val codePoint =
            Character.codePointAt(
                value,
                index
            )

        index +=
            Character.charCount(
                codePoint
            )

        val isInvisible =
            codePoint in
                0x200B..0x200D ||
                codePoint == 0xFEFF

        if (isInvisible) {
            continue
        }

        val isSpace =
            Character.isWhitespace(
                codePoint
            ) ||
                Character.isSpaceChar(
                    codePoint
                )

        if (isSpace) {
            if (
                !previousWasSpace &&
                output.isNotEmpty()
            ) {
                output.append(' ')
            }

            previousWasSpace = true
        } else {
            output.appendCodePoint(
                codePoint
            )

            previousWasSpace = false
        }
    }

    return output
        .toString()
        .trim()
}

private fun normalizeText(
    value: String
): String {
    val prepared =
        value
            .lowercase(
                Locale.ROOT
            )
            .replace(
                oldChar = 'đ',
                newChar = 'd'
            )

    val decomposed =
        Normalizer.normalize(
            prepared,
            Normalizer.Form.NFD
        )

    val output =
        StringBuilder(
            decomposed.length
        )

    var previousWasSpace = true

    decomposed.forEach { character ->
        val type =
            Character.getType(
                character
            )

        val isMark =
            type ==
                Character.NON_SPACING_MARK.toInt() ||
                type ==
                Character.COMBINING_SPACING_MARK.toInt() ||
                type ==
                Character.ENCLOSING_MARK.toInt()

        when {
            isMark -> Unit

            character.isLetterOrDigit() -> {
                output.append(character)
                previousWasSpace = false
            }

            else -> {
                if (
                    !previousWasSpace &&
                    output.isNotEmpty()
                ) {
                    output.append(' ')
                }

                previousWasSpace = true
            }
        }
    }

    return output
        .toString()
        .trim()
}

private fun replaceIgnoreCase(
    source: String,
    target: String,
    replacement: String
): String {
    if (
        source.isEmpty() ||
        target.isEmpty()
    ) {
        return source
    }

    val output =
        StringBuilder(
            source.length
        )

    var searchIndex = 0

    while (searchIndex < source.length) {
        val matchIndex =
            source.indexOf(
                string = target,
                startIndex = searchIndex,
                ignoreCase = true
            )

        if (matchIndex < 0) {
            output.append(
                source,
                searchIndex,
                source.length
            )

            break
        }

        output.append(
            source,
            searchIndex,
            matchIndex
        )

        output.append(replacement)

        searchIndex =
            matchIndex +
                target.length
    }

    return output.toString()
}
