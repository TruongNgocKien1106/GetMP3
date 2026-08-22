package com.ngoctien.getmp3.ui.app

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ngoctien.getmp3.ui.LyricsRoute
import com.ngoctien.getmp3.ui.LyricsUiActions
import com.ngoctien.getmp3.viewmodel.LyricsEvent
import com.ngoctien.getmp3.viewmodel.LyricsViewModel

/*
 * Lyrics presentation boundary.
 *
 * Đây là lớp duy nhất của feature UI biết LyricsViewModel:
 *
 * ViewModel
 *   -> StateFlow / Event
 *   -> Destination
 *   -> LyricsUiState + LyricsUiActions
 *   -> Route
 *   -> Skin / LyricsTab
 */
@Composable
internal fun LyricsDestination(
    viewModel: LyricsViewModel,
    modifier: Modifier
) {
    val state by
        viewModel
            .uiState
            .collectAsStateWithLifecycle()

    val snackbarHostState =
        remember {
            SnackbarHostState()
        }

    LaunchedEffect(
        viewModel,
        snackbarHostState
    ) {
        viewModel
            .events
            .collect { event ->

                when (event) {
                    is LyricsEvent.Message -> {
                        snackbarHostState
                            .showSnackbar(
                                event.text
                            )
                    }
                }
            }
    }

    val actions =
        remember(
            viewModel
        ) {
            LyricsUiActions(
                setQuery =
                    viewModel::setQuery,

                clearQuery =
                    viewModel::clearQuery,

                submitSearch =
                    viewModel::submitSearch,

                selectSong =
                    viewModel::selectSong,

                retryLyricsSearch =
                    viewModel::retryLyricsSearch,

                selectLyricsResult =
                    viewModel::selectLyricsResult,

                dismissWriteTargetPicker =
                    viewModel::dismissWriteTargetPicker,

                selectWriteTarget =
                    viewModel::selectWriteTarget,

                confirmWriteTarget =
                    viewModel::confirmWriteTarget,

                dismissDirectOverwrite =
                    viewModel::dismissDirectOverwrite,

                confirmDirectOverwrite =
                    viewModel::confirmDirectOverwrite,

                back =
                    viewModel::back,

                increaseFontSize =
                    viewModel::increaseFontSize,

                decreaseFontSize =
                    viewModel::decreaseFontSize,

                toggleAutoScroll =
                    viewModel::toggleAutoScroll,

                setAutoScrollSpeed =
                    viewModel::setAutoScrollSpeed,

                editCurrentLyrics =
                    viewModel::editCurrentLyrics,

                openWriteTargetPicker =
                    viewModel::openWriteTargetPicker,

                setEditorLyrics =
                    viewModel::setEditorLyrics,

                pasteEditorLyrics =
                    viewModel::pasteEditorLyrics,

                clearEditorLyrics =
                    viewModel::clearEditorLyrics,

                restoreFoundLyrics =
                    viewModel::restoreFoundLyrics,

                restoreStoredLyrics =
                    viewModel::restoreStoredLyrics,

                saveLyrics =
                    viewModel::saveLyrics
            )
        }

    LyricsRoute(
        state =
            state,

        actions =
            actions,

        snackbarHostState =
            snackbarHostState,

        modifier =
            modifier
    )
}
