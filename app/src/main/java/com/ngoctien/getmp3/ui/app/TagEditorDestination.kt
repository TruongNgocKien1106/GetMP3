package com.ngoctien.getmp3.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ngoctien.getmp3.ui.AppDestination
import com.ngoctien.getmp3.ui.TagEditorAction
import com.ngoctien.getmp3.ui.TagEditorRoute
import com.ngoctien.getmp3.ui.design.UiStyle
import com.ngoctien.getmp3.viewmodel.LyricsViewModel
import com.ngoctien.getmp3.viewmodel.TagEditorViewModel

@Composable
internal fun TagEditorDestination(
    tagEditorViewModel: TagEditorViewModel,
    lyricsViewModel: LyricsViewModel,
    uiStyle: UiStyle,
    modifier: Modifier,
    onNavigate:
        (AppDestination) -> Unit
) {
    val state by
        tagEditorViewModel
            .uiState
            .collectAsStateWithLifecycle()

    TagEditorRoute(
        state =
            state,

        uiStyle =
            uiStyle,

        modifier =
            modifier,

        onAction = { action ->

            when (action) {

                is TagEditorAction.TitleChanged -> {
                    tagEditorViewModel
                        .setTitle(
                            action.value
                        )
                }

                TagEditorAction.ClearTitle -> {
                    tagEditorViewModel
                        .clearTitle()
                }

                is TagEditorAction.ArtistChanged -> {
                    tagEditorViewModel
                        .setArtist(
                            action.value
                        )
                }

                TagEditorAction.ClearArtist -> {
                    tagEditorViewModel
                        .clearArtist()
                }

                is TagEditorAction.ArtistCaseModeChanged -> {
                    tagEditorViewModel
                        .setArtistCaseMode(
                            action.mode
                        )
                }

                is TagEditorAction.AlbumChanged -> {
                    tagEditorViewModel
                        .setAlbum(
                            action.value
                        )
                }

                is TagEditorAction.YearChanged -> {
                    tagEditorViewModel
                        .setYear(
                            action.value
                        )
                }

                TagEditorAction.QuickFormat -> {
                    tagEditorViewModel
                        .quickFormat()
                }

                is TagEditorAction.SelectArtist -> {
                    tagEditorViewModel
                        .selectArtistCandidate(
                            action.value
                        )
                }

                TagEditorAction.DismissArtists -> {
                    tagEditorViewModel
                        .dismissArtistCandidates()
                }

                TagEditorAction.RefreshFiles -> {
                    tagEditorViewModel
                        .refreshFileList()
                }

                is TagEditorAction.SelectFile -> {
                    tagEditorViewModel
                        .selectFile(
                            action.index
                        )
                }

                TagEditorAction.Skip -> {
                    tagEditorViewModel
                        .skip()
                }

                TagEditorAction.Save -> {
                    tagEditorViewModel
                        .saveAndNext()
                }

                TagEditorAction.Delete -> {
                    tagEditorViewModel
                        .deleteCurrentSong()
                }

                TagEditorAction.EditLyrics -> {

                    state
                        .currentSong
                        ?.let { song ->

                            lyricsViewModel
                                .openEditorForFile(
                                    uri =
                                        song
                                            .file
                                            .uri,

                                    displayName =
                                        song
                                            .file
                                            .displayName,

                                    title =
                                        state.title,

                                    artist =
                                        state
                                            .effectiveArtist
                                )

                            onNavigate(
                                AppDestination
                                    .LYRICS
                            )
                        }
                }
            }
        }
    )
}
