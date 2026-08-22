package com.ngoctien.getmp3.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ngoctien.getmp3.data.IndexedMediaEntity
import com.ngoctien.getmp3.note.ReferenceSongMatch
import com.ngoctien.getmp3.ui.AppDestination
import com.ngoctien.getmp3.ui.LibraryScreen
import com.ngoctien.getmp3.viewmodel.LibraryViewModel
import com.ngoctien.getmp3.viewmodel.LyricsViewModel
import com.ngoctien.getmp3.viewmodel.SettingsViewModel
import com.ngoctien.getmp3.viewmodel.TagEditorViewModel
import com.ngoctien.getmp3.viewmodel.YouTubeSearchViewModel

@Composable
internal fun LibraryDestination(
    libraryViewModel: LibraryViewModel,
    settingsViewModel: SettingsViewModel,
    tagEditorViewModel: TagEditorViewModel,
    lyricsViewModel: LyricsViewModel,
    youtubeSearchViewModel: YouTubeSearchViewModel,
    modifier: Modifier,
    onNavigate: (AppDestination) -> Unit
) {
    val state by libraryViewModel.uiState.collectAsStateWithLifecycle()
    val indexState by settingsViewModel.libraryIndexState.collectAsStateWithLifecycle()

    LaunchedEffect(indexState.isScanning, indexState.updatedAt) {
        if (!indexState.isScanning) {
            libraryViewModel.refresh()
        }
    }

    LibraryScreen(
        state = state,
        indexIsScanning = indexState.isScanning,
        indexProgress = indexState.progressFraction,
        modifier = modifier,
        onQueryChange = libraryViewModel::setQuery,
        onSortChange = libraryViewModel::setSortMode,
        onFilterChange = libraryViewModel::setFilterMode,
        onRefresh = libraryViewModel::refresh,
        onRescan = settingsViewModel::rebuildLibraryIndex,
        onOpenSettings = { onNavigate(AppDestination.SETTINGS) },
        onSearchYouTube = { query ->
            youtubeSearchViewModel.setQuery(query)
            youtubeSearchViewModel.search()
            onNavigate(AppDestination.DOWNLOAD)
        },
        onOpenLyrics = { song ->
            openLyrics(song, lyricsViewModel)
            onNavigate(AppDestination.LYRICS)
        },
        onEditTag = { song ->
            openTagEditor(song, tagEditorViewModel)
            onNavigate(AppDestination.EDIT_TAG)
        }
    )
}

private fun openLyrics(
    song: IndexedMediaEntity,
    viewModel: LyricsViewModel
) {
    viewModel.openEditorForFile(
        uri = song.uri,
        displayName = song.displayName,
        title = song.title,
        artist = song.artist
    )
}

private fun openTagEditor(
    song: IndexedMediaEntity,
    viewModel: TagEditorViewModel
) {
    val treeUri = song.treeUri

    if (treeUri.isNullOrBlank()) {
        viewModel.ensureLoaded()
        return
    }

    viewModel.openReferenceSong(
        ReferenceSongMatch(
            uri = song.uri,
            treeUri = treeUri,
            displayName = song.displayName,
            title = song.title,
            artist = song.artist,
            score = 1.0,
            coverPath = song.coverPath,
            durationSeconds = song.durationMs
                .takeIf { it > 0L }
                ?.div(1000L)
        )
    )
}
