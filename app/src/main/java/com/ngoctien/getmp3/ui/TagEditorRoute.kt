package com.ngoctien.getmp3.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ngoctien.getmp3.ui.design.UiStyle
import com.ngoctien.getmp3.ui.skin.AppSkinContent
import com.ngoctien.getmp3.ui.skin.compact.CompactTagEditorScreen
import com.ngoctien.getmp3.viewmodel.ArtistCaseMode
import com.ngoctien.getmp3.viewmodel.TagEditorUiState

/*
 * Route owns semantic action mapping.
 *
 * Bento / Compact only own presentation.
 */
@Composable
internal fun TagEditorRoute(
    state: TagEditorUiState,
    uiStyle: UiStyle,
    modifier: Modifier = Modifier,
    onAction: (TagEditorAction) -> Unit
) {
    val titleChanged:
        (String) -> Unit = {
            value ->

            onAction(
                TagEditorAction
                    .TitleChanged(
                        value
                    )
            )
        }

    val clearTitle:
        () -> Unit = {
            onAction(
                TagEditorAction
                    .ClearTitle
            )
        }

    val artistChanged:
        (String) -> Unit = {
            value ->

            onAction(
                TagEditorAction
                    .ArtistChanged(
                        value
                    )
            )
        }

    val clearArtist:
        () -> Unit = {
            onAction(
                TagEditorAction
                    .ClearArtist
            )
        }

    val artistCaseChanged:
        (ArtistCaseMode) -> Unit = {
            value ->

            onAction(
                TagEditorAction
                    .ArtistCaseModeChanged(
                        value
                    )
            )
        }

    val albumChanged:
        (String) -> Unit = {
            value ->

            onAction(
                TagEditorAction
                    .AlbumChanged(
                        value
                    )
            )
        }

    val yearChanged:
        (String) -> Unit = {
            value ->

            onAction(
                TagEditorAction
                    .YearChanged(
                        value
                    )
            )
        }

    val quickFormat:
        () -> Unit = {
            onAction(
                TagEditorAction
                    .QuickFormat
            )
        }

    val selectArtist:
        (String) -> Unit = {
            value ->

            onAction(
                TagEditorAction
                    .SelectArtist(
                        value
                    )
            )
        }

    val dismissArtists:
        () -> Unit = {
            onAction(
                TagEditorAction
                    .DismissArtists
            )
        }

    val refreshFiles:
        () -> Unit = {
            onAction(
                TagEditorAction
                    .RefreshFiles
            )
        }

    val selectFile:
        (Int) -> Unit = {
            value ->

            onAction(
                TagEditorAction
                    .SelectFile(
                        value
                    )
            )
        }

    val skip:
        () -> Unit = {
            onAction(
                TagEditorAction
                    .Skip
            )
        }

    val save:
        () -> Unit = {
            onAction(
                TagEditorAction
                    .Save
            )
        }

    val delete:
        () -> Unit = {
            onAction(
                TagEditorAction
                    .Delete
            )
        }

    val editLyrics:
        () -> Unit = {
            onAction(
                TagEditorAction
                    .EditLyrics
            )
        }

    AppSkinContent(
        style =
            uiStyle,

        bento = {
            BentoTagEditorScreen(
                state =
                    state,

                modifier =
                    modifier,

                onTitleChange =
                    titleChanged,

                onClearTitle =
                    clearTitle,

                onArtistChange =
                    artistChanged,

                onClearArtist =
                    clearArtist,

                onArtistCaseModeChange =
                    artistCaseChanged,

                onAlbumChange =
                    albumChanged,

                onYearChange =
                    yearChanged,

                onQuickFormat =
                    quickFormat,

                onSelectArtist =
                    selectArtist,

                onDismissArtists =
                    dismissArtists,

                onRefreshFiles =
                    refreshFiles,

                onSelectFile =
                    selectFile,

                onSkip =
                    skip,

                onSave =
                    save,

                onDelete =
                    delete,

                onEditLyrics =
                    editLyrics
            )
        },

        compact = {
            CompactTagEditorScreen(
                state =
                    state,

                modifier =
                    modifier,

                onTitleChange =
                    titleChanged,

                onClearTitle =
                    clearTitle,

                onArtistChange =
                    artistChanged,

                onClearArtist =
                    clearArtist,

                onArtistCaseModeChange =
                    artistCaseChanged,

                onAlbumChange =
                    albumChanged,

                onYearChange =
                    yearChanged,

                onQuickFormat =
                    quickFormat,

                onSelectArtist =
                    selectArtist,

                onDismissArtists =
                    dismissArtists,

                onRefreshFiles =
                    refreshFiles,

                onSelectFile =
                    selectFile,

                onSkip =
                    skip,

                onSave =
                    save,

                onDelete =
                    delete,

                onEditLyrics =
                    editLyrics
            )
        }
    )
}
