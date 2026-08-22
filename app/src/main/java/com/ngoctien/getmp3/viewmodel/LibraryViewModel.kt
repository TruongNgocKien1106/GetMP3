package com.ngoctien.getmp3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ngoctien.getmp3.data.IndexedMediaEntity
import com.ngoctien.getmp3.data.MediaMetadataStatus
import com.ngoctien.getmp3.library.MediaIndexRepository
import com.ngoctien.getmp3.note.SongNameMatcher
import com.ngoctien.getmp3.settings.AppSettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal enum class LibrarySortMode { NEWEST, TITLE, ARTIST }
internal enum class LibraryFilterMode { ALL, NEEDS_ATTENTION }

internal data class LibraryUiState(
    val isConfigured: Boolean = false,
    val songs: List<IndexedMediaEntity> = emptyList(),
    val query: String = "",
    val sortMode: LibrarySortMode = LibrarySortMode.NEWEST,
    val filterMode: LibraryFilterMode = LibraryFilterMode.ALL,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val totalCount: Int = 0,
    val newCount: Int = 0,
    val attentionCount: Int = 0
)

internal class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val RECENT_WINDOW_MS = 7L * 24L * 60L * 60L * 1000L
    }

    private val settingsRepository = AppSettingsRepository(application)
    private val mediaIndexRepository = MediaIndexRepository(application)
    private val mutableUiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = mutableUiState.asStateFlow()

    private var allSongs: List<IndexedMediaEntity> = emptyList()
    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            val settings = settingsRepository.getSettings()

            if (!settings.hasLibraryFolder) {
                allSongs = emptyList()
                mutableUiState.value = mutableUiState.value.copy(
                    isConfigured = false,
                    songs = emptyList(),
                    isLoading = false,
                    errorMessage = null,
                    totalCount = 0,
                    newCount = 0,
                    attentionCount = 0
                )
                return@launch
            }

            mutableUiState.value = mutableUiState.value.copy(
                isConfigured = true,
                isLoading = true,
                errorMessage = null
            )

            try {
                allSongs = mediaIndexRepository.getLibrarySongs()
                publish(mutableUiState.value.copy(isLoading = false))
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                allSongs = emptyList()
                mutableUiState.value = mutableUiState.value.copy(
                    isLoading = false,
                    songs = emptyList(),
                    errorMessage = exception.message ?: "Không đọc được Library"
                )
            }
        }
    }

    fun setQuery(value: String) =
        publish(mutableUiState.value.copy(query = value, errorMessage = null))

    fun setSortMode(value: LibrarySortMode) =
        publish(mutableUiState.value.copy(sortMode = value))

    fun setFilterMode(value: LibraryFilterMode) =
        publish(mutableUiState.value.copy(filterMode = value))

    private fun publish(base: LibraryUiState) {
        val query = SongNameMatcher.normalizeText(base.query)

        val filtered = allSongs.asSequence()
            .filter { song ->
                query.isBlank() ||
                    song.normalizedTitle.contains(query) ||
                    song.normalizedArtist.contains(query) ||
                    song.normalizedFileName.contains(query)
            }
            .filter { song ->
                base.filterMode == LibraryFilterMode.ALL ||
                    song.needsLibraryAttention()
            }
            .toList()

        val sorted = when (base.sortMode) {
            LibrarySortMode.NEWEST -> filtered.sortedByDescending { it.indexedAt }
            LibrarySortMode.TITLE ->
                filtered.sortedBy { it.title.ifBlank { it.displayName }.lowercase() }
            LibrarySortMode.ARTIST ->
                filtered.sortedWith(
                    compareBy<IndexedMediaEntity> { it.artist.lowercase() }
                        .thenBy { it.title.lowercase() }
                )
        }

        val recentThreshold = System.currentTimeMillis() - RECENT_WINDOW_MS
        mutableUiState.value = base.copy(
            songs = sorted,
            totalCount = allSongs.size,
            newCount = allSongs.count { it.indexedAt >= recentThreshold },
            attentionCount = allSongs.count { it.needsLibraryAttention() }
        )
    }
}

private fun IndexedMediaEntity.needsLibraryAttention(): Boolean =
    MediaMetadataStatus.isError(metadataStatus) ||
        tagTitle.isBlank() ||
        tagArtist.isBlank() ||
        album.isBlank() ||
        year.isBlank()
