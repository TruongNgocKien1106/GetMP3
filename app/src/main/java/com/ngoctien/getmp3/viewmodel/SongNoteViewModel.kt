package com.ngoctien.getmp3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ngoctien.getmp3.note.SongNoteRepository
import com.ngoctien.getmp3.note.data.DuplicateSongMatch
import com.ngoctien.getmp3.note.data.SongNoteDraft
import com.ngoctien.getmp3.note.data.SongNoteEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SongNoteFilter {
    ALL,
    PENDING,
    COMPLETED
}

data class SongNoteUiState(
    val notes: List<SongNoteEntity> =
        emptyList(),

    val input: String = "",

    val searchQuery: String = "",

    val filter: SongNoteFilter =
        SongNoteFilter.ALL,

    val isAdding: Boolean = false,

    val pendingDraft: SongNoteDraft? =
        null,

    val duplicateMatches:
        List<DuplicateSongMatch> =
            emptyList(),

    val showDuplicateWarning:
        Boolean = false
) {
    val completedCount: Int
        get() =
            notes.count {
                it.isCompleted
            }

    val pendingCount: Int
        get() =
            notes.size -
                completedCount

    val visibleNotes:
        List<SongNoteEntity>
        get() {
            val query =
                searchQuery
                    .trim()

            return notes.filter { note ->
                val matchesFilter =
                    when (filter) {
                        SongNoteFilter.ALL ->
                            true

                        SongNoteFilter.PENDING ->
                            !note.isCompleted

                        SongNoteFilter.COMPLETED ->
                            note.isCompleted
                    }

                val matchesSearch =
                    query.isBlank() ||
                        note.title.contains(
                            query,
                            ignoreCase = true
                        ) ||
                        note.artist.contains(
                            query,
                            ignoreCase = true
                        ) ||
                        note.rawText.contains(
                            query,
                            ignoreCase = true
                        )

                matchesFilter &&
                    matchesSearch
            }
        }
}

data class SongNoteEvent(
    val message: String
)

class SongNoteViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        SongNoteRepository(application)

    private val mutableUiState =
        MutableStateFlow(
            SongNoteUiState()
        )

    val uiState:
        StateFlow<SongNoteUiState> =
            mutableUiState.asStateFlow()

    private val mutableEvents =
        MutableSharedFlow<SongNoteEvent>(
            extraBufferCapacity = 8
        )

    val events:
        SharedFlow<SongNoteEvent> =
            mutableEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            repository
                .observeNotes()
                .collect { notes ->
                    mutableUiState.update {
                        it.copy(
                            notes = notes
                        )
                    }
                }
        }
    }

    fun setInput(
        value: String
    ) {
        mutableUiState.update {
            it.copy(
                input = value
            )
        }
    }

    fun setSearchQuery(
        value: String
    ) {
        mutableUiState.update {
            it.copy(
                searchQuery = value
            )
        }
    }

    fun setFilter(
        filter: SongNoteFilter
    ) {
        mutableUiState.update {
            it.copy(
                filter = filter
            )
        }
    }

    fun addNote() {
        val state =
            mutableUiState.value

        val rawText =
            state.input.trim()

        if (
            rawText.isBlank() ||
            state.isAdding
        ) {
            if (rawText.isBlank()) {
                mutableEvents.tryEmit(
                    SongNoteEvent(
                        "Hãy nhập tên bài hát"
                    )
                )
            }

            return
        }

        viewModelScope.launch {
            mutableUiState.update {
                it.copy(
                    isAdding = true
                )
            }

            try {
                val draft =
                    repository.parseDraft(
                        rawText
                    )

                val matches =
                    repository.findDuplicates(
                        draft
                    )

                if (matches.isNotEmpty()) {
                    mutableUiState.update {
                        it.copy(
                            isAdding = false,

                            pendingDraft =
                                draft,

                            duplicateMatches =
                                matches,

                            showDuplicateWarning =
                                true
                        )
                    }

                    return@launch
                }

                repository.insert(
                    draft
                )

                mutableUiState.update {
                    it.copy(
                        input =
                            if (
                                it.input.trim() ==
                                rawText
                            ) {
                                ""
                            } else {
                                it.input
                            },

                        isAdding = false
                    )
                }

                mutableEvents.emit(
                    SongNoteEvent(
                        "Đã thêm vào Chờ tải"
                    )
                )
            } catch (exception: Exception) {
                mutableUiState.update {
                    it.copy(
                        isAdding = false
                    )
                }

                mutableEvents.emit(
                    SongNoteEvent(
                        exception.message
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: "Không thêm được note"
                    )
                )
            }
        }
    }

    fun confirmAddDespiteDuplicate() {
        val draft =
            mutableUiState.value
                .pendingDraft
                ?: return

        viewModelScope.launch {
            try {
                repository.insert(
                    draft
                )

                mutableUiState.update {
                    it.copy(
                        input = "",

                        pendingDraft = null,

                        duplicateMatches =
                            emptyList(),

                        showDuplicateWarning =
                            false
                    )
                }

                mutableEvents.emit(
                    SongNoteEvent(
                        "Đã thêm dù có bài gần giống"
                    )
                )
            } catch (exception: Exception) {
                mutableEvents.emit(
                    SongNoteEvent(
                        exception.message
                            ?: "Không thêm được note"
                    )
                )
            }
        }
    }

    fun cancelDuplicateWarning() {
        mutableUiState.update {
            it.copy(
                pendingDraft = null,

                duplicateMatches =
                    emptyList(),

                showDuplicateWarning =
                    false
            )
        }
    }

    fun toggleCompleted(
        note: SongNoteEntity
    ) {
        viewModelScope.launch {
            repository.toggleCompleted(
                note
            )
        }
    }

    fun toggleImportant(
        note: SongNoteEntity
    ) {
        viewModelScope.launch {
            repository.toggleImportant(
                note
            )
        }
    }

    fun deleteNote(
        note: SongNoteEntity
    ) {
        viewModelScope.launch {
            val deleted =
                repository.delete(
                    note
                )

            if (deleted > 0) {
                mutableEvents.emit(
                    SongNoteEvent(
                        "Đã xóa note"
                    )
                )
            }
        }
    }

    fun deleteCompletedNotes() {
        viewModelScope.launch {
            val deleted =
                repository
                    .deleteCompleted()

            mutableEvents.emit(
                SongNoteEvent(
                    if (deleted > 0) {
                        "Đã xóa $deleted note đã tải"
                    } else {
                        "Không có note đã tải để xóa"
                    }
                )
            )
        }
    }
}