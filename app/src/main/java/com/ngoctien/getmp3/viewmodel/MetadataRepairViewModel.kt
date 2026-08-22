package com.ngoctien.getmp3.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ngoctien.getmp3.data.IndexedMediaEntity
import com.ngoctien.getmp3.data.MediaMetadataStatus
import com.ngoctien.getmp3.library.InboxWorkflowRepository
import com.ngoctien.getmp3.library.LibraryAdmissionPolicy
import com.ngoctien.getmp3.library.MediaIndexRepository
import com.ngoctien.getmp3.metadata.ReleaseYearRepository
import com.ngoctien.getmp3.metadata.ReleaseYearSuggestion
import com.ngoctien.getmp3.tag.EditableSong
import com.ngoctien.getmp3.tag.MediaSongFile
import com.ngoctien.getmp3.tag.TagEditorRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

enum class MetadataRepairFilter {
    ALL,
    METADATA,
    COVER,
    TAGS
}

data class MetadataRepairUiState(
    val items: List<IndexedMediaEntity> =
        emptyList(),

    val filter:
        MetadataRepairFilter =
        MetadataRepairFilter.ALL,

    val selected: IndexedMediaEntity? =
        null,

    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val year: String = "",

    val yearSuggestions:
        List<ReleaseYearSuggestion> =
        emptyList(),

    val isLookingUpYear: Boolean = false,
    val yearLookupError: String? = null,

    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val hasLoaded: Boolean = false,

    val errorMessage: String? = null
) {
    val attentionCount: Int
        get() =
            items.size

    /*
     * Backward compatible name for CompareTab.
     */
    val errorCount: Int
        get() =
            attentionCount

    val metadataErrorCount: Int
        get() =
            items.count {
                it.hasMetadataError()
            }

    val missingCoverCount: Int
        get() =
            items.count {
                it.hasMissingCover()
            }

    val missingTagCount: Int
        get() =
            items.count {
                it.hasMissingTags()
            }

    val filteredItems:
        List<IndexedMediaEntity>
        get() =
            when (filter) {
                MetadataRepairFilter.ALL ->
                    items

                MetadataRepairFilter.METADATA ->
                    items.filter {
                        it.hasMetadataError()
                    }

                MetadataRepairFilter.COVER ->
                    items.filter {
                        it.hasMissingCover()
                    }

                MetadataRepairFilter.TAGS ->
                    items.filter {
                        it.hasMissingTags()
                    }
            }
}

data class MetadataRepairEvent(
    val message: String
)

class MetadataRepairViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val mediaIndexRepository =
        MediaIndexRepository(
            application
        )

    private val tagEditorRepository =
        TagEditorRepository(
            application
        )

    private val inboxWorkflowRepository =
        InboxWorkflowRepository(
            application
        )

    private val releaseYearRepository =
        ReleaseYearRepository()

    private var yearLookupGeneration =
        0L

    private val mutableUiState =
        MutableStateFlow(
            MetadataRepairUiState()
        )

    val uiState:
        StateFlow<MetadataRepairUiState> =
        mutableUiState.asStateFlow()

    private val mutableEvents =
        MutableSharedFlow<
            MetadataRepairEvent
        >(
            extraBufferCapacity = 8
        )

    val events:
        SharedFlow<MetadataRepairEvent> =
        mutableEvents.asSharedFlow()

    /*
     * This is only a Room query.
     *
     * It does NOT rescan the reference directory.
     * Therefore it is cheap enough to refresh every time
     * the user enters "Cần xử lý".
     */
    fun ensureLoaded() {
        val state =
            mutableUiState.value

        if (
            state.isLoading ||
            state.isSaving
        ) {
            return
        }

        loadItems()
    }

    fun refresh() {
        val state =
            mutableUiState.value

        if (
            state.isLoading ||
            state.isSaving
        ) {
            return
        }

        loadItems()
    }

    fun setFilter(
        filter: MetadataRepairFilter
    ) {
        mutableUiState.update {
            it.copy(
                filter = filter
            )
        }
    }

    fun select(
        item: IndexedMediaEntity
    ) {
        if (
            mutableUiState.value
                .isSaving
        ) {
            return
        }

        invalidateYearLookup()

        mutableUiState.update {
            it.copy(
                selected = item,

                title =
                    preferredTitle(
                        item
                    ),

                artist =
                    preferredArtist(
                        item
                    ),

                album =
                    preferredAlbum(
                        item
                    ),

                year =
                    item.year.trim(),

                yearSuggestions =
                    emptyList(),

                isLookingUpYear =
                    false,

                yearLookupError =
                    null,

                errorMessage =
                    null
            )
        }
    }

    fun dismissEditor() {
        if (
            mutableUiState.value
                .isSaving
        ) {
            return
        }

        invalidateYearLookup()

        mutableUiState.update {
            it.copy(
                selected = null,
                title = "",
                artist = "",
                album = "",
                year = "",
                yearSuggestions = emptyList(),
                isLookingUpYear = false,
                yearLookupError = null,
                errorMessage = null
            )
        }
    }

    fun setTitle(
        value: String
    ) {
        invalidateYearLookup()

        mutableUiState.update {
            it.copy(
                title = value,
                yearSuggestions = emptyList(),
                isLookingUpYear = false,
                yearLookupError = null,
                errorMessage = null
            )
        }
    }

    fun setArtist(
        value: String
    ) {
        invalidateYearLookup()

        mutableUiState.update {
            it.copy(
                artist = value,
                yearSuggestions = emptyList(),
                isLookingUpYear = false,
                yearLookupError = null,
                errorMessage = null
            )
        }
    }

    fun setAlbum(
        value: String
    ) {
        invalidateYearLookup()

        mutableUiState.update {
            it.copy(
                album = value,
                yearSuggestions = emptyList(),
                isLookingUpYear = false,
                yearLookupError = null,
                errorMessage = null
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
                        ),

                yearLookupError =
                    null,

                errorMessage =
                    null
            )
        }
    }

    fun lookupYear() {
        val state =
            mutableUiState.value

        if (
            state.selected == null ||
            state.isSaving ||
            state.isLookingUpYear
        ) {
            return
        }

        val title =
            state.title.trim()

        val artist =
            state.artist.trim()

        val album =
            state.album.trim()

        if (
            title.isBlank() ||
            artist.isBlank()
        ) {
            mutableUiState.update {
                it.copy(
                    yearSuggestions =
                        emptyList(),

                    yearLookupError =
                        "Cần Title và Artist trước khi tra năm"
                )
            }

            return
        }

        val generation =
            nextYearLookupGeneration()

        mutableUiState.update {
            it.copy(
                yearSuggestions =
                    emptyList(),

                isLookingUpYear =
                    true,

                yearLookupError =
                    null
            )
        }

        viewModelScope.launch {
            try {
                val suggestions =
                    releaseYearRepository
                        .lookupSuggestions(
                            title =
                                title,

                            artist =
                                artist,

                            album =
                                album
                        )

                if (
                    generation !=
                    yearLookupGeneration
                ) {
                    return@launch
                }

                mutableUiState.update {
                    it.copy(
                        yearSuggestions =
                            suggestions,

                        isLookingUpYear =
                            false,

                        yearLookupError =
                            if (
                                suggestions.isEmpty()
                            ) {
                                "Không tìm thấy năm phát hành đủ khớp"
                            }
                            else {
                                null
                            }
                    )
                }

            } catch (
                exception:
                    CancellationException
            ) {
                throw exception

            } catch (
                exception: Exception
            ) {
                if (
                    generation !=
                    yearLookupGeneration
                ) {
                    return@launch
                }

                val message =
                    exception.message
                        ?.takeIf(
                            String::isNotBlank
                        )
                        ?: "Không tra được năm phát hành"

                mutableUiState.update {
                    it.copy(
                        yearSuggestions =
                            emptyList(),

                        isLookingUpYear =
                            false,

                        yearLookupError =
                            message
                    )
                }
            }
        }
    }

    /*
     * Rewrite Title / Artist / Album of the current MP3.
     *
     * Artwork is intentionally NOT changed here.
     */
    fun save() {
        val state =
            mutableUiState.value

        val item =
            state.selected
                ?: return

        if (
            state.isSaving ||
            state.isLoading
        ) {
            return
        }

        val title =
            state.title.trim()

        val artist =
            state.artist.trim()

        val album =
            state.album.trim()

        val year =
            state.year.trim()

        if (
            year.isNotBlank() &&
            !Regex("""\d{4}""")
                .matches(
                    year
                )
        ) {
            mutableEvents.tryEmit(
                MetadataRepairEvent(
                    "Year phải gồm đúng 4 chữ số"
                )
            )

            return
        }

        if (title.isBlank()) {
            mutableEvents.tryEmit(
                MetadataRepairEvent(
                    "Title không được để trống"
                )
            )

            return
        }

        if (artist.isBlank()) {
            mutableEvents.tryEmit(
                MetadataRepairEvent(
                    "Artist không được để trống"
                )
            )

            return
        }

        invalidateYearLookup()

        viewModelScope.launch {
            mutableUiState.update {
                it.copy(
                    isSaving = true,
                    yearSuggestions = emptyList(),
                    isLookingUpYear = false,
                    yearLookupError = null,
                    errorMessage = null
                )
            }

            try {
                val updatedSong =
                    tagEditorRepository
                        .saveSong(
                            song =
                                EditableSong(
                                    file =
                                        item
                                            .toMediaSongFile(),

                                    title =
                                        item.title,

                                    artist =
                                        item.artist,

                                    album =
                                        item.album,

                                    coverPath =
                                        item.coverPath,

                                    year =
                                        item.year
                                ),

                            title =
                                title,

                            artist =
                                artist,

                            album =
                                album,

                            year =
                                year
                        )

                /*
                 * Only re-index the edited MP3.
                 * No directory scan.
                 */
                val refreshed =
                    mediaIndexRepository
                        .refreshEditedFile(
                            oldUri =
                                item.uri,

                            updatedFile =
                                updatedSong.file
                        )

                val admission =
                    LibraryAdmissionPolicy
                        .evaluate(
                            refreshed
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

                val remaining =
                    mediaIndexRepository
                        .getMetadataErrors()

                mutableUiState.update {
                    it.copy(
                        items =
                            remaining,

                        selected =
                            null,

                        title = "",
                        artist = "",
                        album = "",
                        year = "",

                        yearSuggestions =
                            emptyList(),

                        isLookingUpYear =
                            false,

                        yearLookupError =
                            null,

                        isSaving =
                            false,

                        hasLoaded =
                            true,

                        errorMessage =
                            null
                    )
                }

                val resultMessage =
                    if (
                        promotion != null
                    ) {

                        "Đã sửa và đưa vào Library: " +
                            promotion
                                .displayName

                    }
                    else if (
                        refreshed.source ==
                        com.ngoctien.getmp3.data
                            .MediaIndexSource
                            .INBOX &&
                        !admission.allowed
                    ) {

                        "Đã lưu. File vẫn ở Inbox: " +
                            admission.message

                    }
                    else {

                        "Đã ghi lại metadata: " +
                            updatedSong
                                .file
                                .displayName
                    }

                mutableEvents.emit(
                    MetadataRepairEvent(
                        resultMessage
                    )
                )

            } catch (
                exception:
                    CancellationException
            ) {
                throw exception

            } catch (
                exception: Exception
            ) {
                val message =
                    exception.message
                        ?.takeIf(
                            String::isNotBlank
                        )
                        ?: "Không ghi lại được metadata"

                mutableUiState.update {
                    it.copy(
                        isSaving =
                            false,

                        errorMessage =
                            message
                    )
                }

                mutableEvents.emit(
                    MetadataRepairEvent(
                        message
                    )
                )
            }
        }
    }

    private fun loadItems() {
        viewModelScope.launch {
            mutableUiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                val items =
                    mediaIndexRepository
                        .getMetadataErrors()

                mutableUiState.update {
                    it.copy(
                        items =
                            items,

                        isLoading =
                            false,

                        hasLoaded =
                            true,

                        errorMessage =
                            null
                    )
                }

            } catch (
                exception:
                    CancellationException
            ) {
                throw exception

            } catch (
                exception: Exception
            ) {
                mutableUiState.update {
                    it.copy(
                        isLoading =
                            false,

                        hasLoaded =
                            true,

                        errorMessage =
                            exception.message
                                ?.takeIf(
                                    String::isNotBlank
                                )
                                ?: "Không đọc được danh sách cần xử lý"
                    )
                }
            }
        }
    }

    private fun invalidateYearLookup() {
        yearLookupGeneration +=
            1L
    }

    private fun nextYearLookupGeneration():
        Long {
        invalidateYearLookup()

        return yearLookupGeneration
    }

    private fun preferredTitle(
        item: IndexedMediaEntity
    ): String {

        return item.fileTitle
            .trim()
            .ifBlank {
                item.rawTagTitle
                    ?.trim()
                    .orEmpty()
            }
            .ifBlank {
                item.title
                    .trim()
            }
    }

    private fun preferredArtist(
        item: IndexedMediaEntity
    ): String {

        return item.fileArtist
            .trim()
            .ifBlank {
                item.rawTagArtist
                    ?.trim()
                    .orEmpty()
            }
            .ifBlank {
                item.artist
                    .trim()
            }
    }

    private fun preferredAlbum(
        item: IndexedMediaEntity
    ): String {

        return item.rawTagAlbum
            ?.trim()
            ?.takeIf(
                String::isNotBlank
            )
            ?: item.album
                .trim()
    }

    private fun IndexedMediaEntity
        .toMediaSongFile():
        MediaSongFile {

        val parsedUri =
            Uri.parse(
                uri
            )

        val id =
            if (
                treeUri.isNullOrBlank()
            ) {
                parsedUri
                    .lastPathSegment
                    ?.toLongOrNull()
                    ?: uri
                        .hashCode()
                        .toLong()
                        .absoluteValue
            } else {
                uri
                    .hashCode()
                    .toLong()
                    .absoluteValue
            }

        return MediaSongFile(
            id =
                id,

            uri =
                uri,

            displayName =
                displayName,

            sizeBytes =
                sizeBytes,

            dateModifiedSeconds =
                lastModifiedMs /
                    1000L,

            treeUri =
                treeUri
        )
    }
}

fun IndexedMediaEntity
    .hasMetadataError():
    Boolean {

    return MediaMetadataStatus
        .isError(
            metadataStatus
        )
}

fun IndexedMediaEntity
    .hasMissingCover():
    Boolean {

    return coverPath
        .isNullOrBlank()
}

fun IndexedMediaEntity
    .hasMissingTags():
    Boolean {

    return tagTitle.isBlank() ||
        tagArtist.isBlank() ||
        album.isBlank() ||
        year.isBlank()
}