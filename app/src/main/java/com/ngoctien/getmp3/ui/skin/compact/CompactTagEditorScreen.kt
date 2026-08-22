package com.ngoctien.getmp3.ui.skin.compact

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.ui.AppHeaderActionButton
import com.ngoctien.getmp3.ui.AppMotion
import com.ngoctien.getmp3.ui.AppPageHeader
import com.ngoctien.getmp3.ui.components.AppMediaCover
import com.ngoctien.getmp3.ui.design.LocalAppDesign
import com.ngoctien.getmp3.viewmodel.ArtistCaseMode
import com.ngoctien.getmp3.viewmodel.TagEditorUiState
import java.io.File

@Composable
internal fun CompactTagEditorScreen(
    state: TagEditorUiState,
    modifier: Modifier,
    onTitleChange: (String) -> Unit,
    onClearTitle: () -> Unit,
    onArtistChange: (String) -> Unit,
    onClearArtist: () -> Unit,
    onArtistCaseModeChange:
        (ArtistCaseMode) -> Unit,
    onAlbumChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onQuickFormat: () -> Unit,
    onSelectArtist: (String) -> Unit,
    onDismissArtists: () -> Unit,
    onRefreshFiles: () -> Unit,
    onSelectFile: (Int) -> Unit,
    onSkip: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onEditLyrics: () -> Unit
) {
    val design =
        LocalAppDesign.current

    var showFileSheet by
        remember {
            mutableStateOf(
                false
            )
        }

    var showDeleteConfirmation by
        remember {
            mutableStateOf(
                false
            )
        }

    val busy =
        state.isScanning ||
            state.isLoadingSong ||
            state.isSaving ||
            state.isDeleting

    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .statusBarsPadding(),

        contentPadding =
            PaddingValues(
                start =
                    design.spacing.screenHorizontal,

                top =
                    design.spacing.screenVertical,

                end =
                    design.spacing.screenHorizontal,

                bottom =
                    design.spacing.screenVertical
            ),

        verticalArrangement =
            Arrangement.spacedBy(
                design.spacing.small
            )
    ) {
        item {
            AppPageHeader(
                title =
                    "Sửa thẻ",

                subtitle =
                    if (state.totalFiles > 0) {
                        "${state.displayIndex}/${state.totalFiles} • Inbox → Library"
                    }
                    else {
                        "Inbox → Library"
                    },

                icon =
                    Icons.Rounded.Edit,

                action = {
                    AppHeaderActionButton(
                        icon =
                            Icons.Rounded.Refresh,

                        contentDescription =
                            "Quét lại danh sách MP3",

                        enabled =
                            !busy,

                        onClick =
                            onRefreshFiles
                    )
                }
            )
        }

        item {
            AnimatedVisibility(
                visible =
                    state.isScanning ||
                        state.isLoadingSong
            ) {
                Surface(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(
                            design.shapes.controlCorner
                        ),

                    color =
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant
                            .copy(
                                alpha = 0.55f
                            )
                ) {
                    Row(
                        modifier =
                            Modifier.padding(
                                12.dp
                            ),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(
                                    18.dp
                                ),

                            strokeWidth =
                                2.dp
                        )

                        Spacer(
                            modifier =
                                Modifier.width(
                                    10.dp
                                )
                        )

                        Text(
                            text =
                                if (state.isScanning) {
                                    "Đang quét Inbox..."
                                }
                                else {
                                    "Đang đọc metadata..."
                                },

                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall
                        )
                    }
                }
            }
        }

        state.errorMessage
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let { message ->

                item {
                    Surface(
                        modifier =
                            Modifier.fillMaxWidth(),

                        shape =
                            RoundedCornerShape(
                                design.shapes.controlCorner
                            ),

                        color =
                            MaterialTheme
                                .colorScheme
                                .errorContainer
                    ) {
                        Text(
                            text =
                                message,

                            modifier =
                                Modifier.padding(
                                    12.dp
                                ),

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onErrorContainer,

                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall
                        )
                    }
                }
            }

        item {
            AnimatedContent(
                targetState =
                    state.currentSong,

                transitionSpec = {
                    fadeIn(
                        animationSpec =
                            tween(
                                AppMotion.ContentMillis
                            )
                    ) togetherWith
                        fadeOut(
                            animationSpec =
                                tween(
                                    AppMotion.ExitMillis
                                )
                        )
                },

                label =
                    "compact-tag-song"
            ) { song ->

                Surface(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(
                            design.shapes.cardCorner
                        ),

                    color =
                        MaterialTheme
                            .colorScheme
                            .surface,

                    tonalElevation =
                        1.dp
                ) {
                    if (song == null) {
                        Column(
                            modifier =
                                Modifier.padding(
                                    design.spacing.cardPadding
                                )
                        ) {
                            Text(
                                text =
                                    if (state.files.isEmpty()) {
                                        "Inbox chưa có MP3 cần xử lý"
                                    }
                                    else {
                                        "Chọn một file để sửa thẻ"
                                    },

                                fontWeight =
                                    FontWeight.SemiBold
                            )
                        }
                    }
                    else {
                        Row(
                            modifier =
                                Modifier.padding(
                                    design.spacing.cardPadding
                                ),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            AppMediaCover(
                                model =
                                    song.coverPath
                                        ?.takeIf(
                                            String::isNotBlank
                                        )
                                        ?.let(
                                            ::File
                                        ),

                                size =
                                    64
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(
                                        12.dp
                                    )
                            )

                            Column(
                                modifier =
                                    Modifier.weight(
                                        1f
                                    )
                            ) {
                                Text(
                                    text =
                                        state.previewFileName
                                            .ifBlank {
                                                song.file.displayName
                                            },

                                    maxLines =
                                        2,

                                    overflow =
                                        TextOverflow.Ellipsis,

                                    style =
                                        MaterialTheme
                                            .typography
                                            .titleMedium,

                                    fontWeight =
                                        FontWeight.Bold
                                )

                                Text(
                                    text =
                                        song.file.displayName,

                                    maxLines =
                                        1,

                                    overflow =
                                        TextOverflow.Ellipsis,

                                    style =
                                        MaterialTheme
                                            .typography
                                            .labelSmall,

                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onSurfaceVariant
                                )

                                Text(
                                    text =
                                        buildString {
                                            append(
                                                state.selectedAlbum
                                            )

                                            if (
                                                state.year
                                                    .isNotBlank()
                                            ) {
                                                append(
                                                    " • "
                                                )

                                                append(
                                                    state.year
                                                )
                                            }
                                        },

                                    style =
                                        MaterialTheme
                                            .typography
                                            .labelMedium,

                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .primary
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {
                OutlinedButton(
                    onClick = {
                        showFileSheet =
                            true
                    },

                    enabled =
                        state.files.isNotEmpty() &&
                            !busy,

                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.List,

                        contentDescription =
                            null
                    )

                    Spacer(
                        modifier =
                            Modifier.width(
                                6.dp
                            )
                    )

                    Text(
                        "Danh sách ${state.totalFiles}"
                    )
                }

                OutlinedButton(
                    onClick =
                        onEditLyrics,

                    enabled =
                        state.currentSong != null &&
                            !busy,

                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.LibraryMusic,

                        contentDescription =
                            null
                    )

                    Spacer(
                        modifier =
                            Modifier.width(
                                6.dp
                            )
                    )

                    Text(
                        "Lyrics"
                    )
                }
            }
        }

        item {
            CompactTagEditorForm(
                state =
                    state,

                onTitleChange =
                    onTitleChange,

                onClearTitle =
                    onClearTitle,

                onArtistChange =
                    onArtistChange,

                onClearArtist =
                    onClearArtist,

                onArtistCaseModeChange =
                    onArtistCaseModeChange,

                onAlbumChange =
                    onAlbumChange,

                onYearChange =
                    onYearChange,

                onQuickFormat =
                    onQuickFormat
            )
        }

        item {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {
                OutlinedButton(
                    onClick = {
                        showDeleteConfirmation =
                            true
                    },

                    enabled =
                        state.currentSong != null &&
                            !busy,

                    modifier =
                        Modifier.weight(
                            0.9f
                        )
                ) {
                    if (state.isDeleting) {
                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(
                                    18.dp
                                ),

                            strokeWidth =
                                2.dp
                        )
                    }
                    else {
                        Icon(
                            imageVector =
                                Icons.Rounded
                                    .DeleteOutline,

                            contentDescription =
                                null
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.width(
                                5.dp
                            )
                    )

                    Text(
                        "Xóa"
                    )
                }

                OutlinedButton(
                    onClick =
                        onSkip,

                    enabled =
                        state.canGoNext &&
                            !busy,

                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.SkipNext,

                        contentDescription =
                            null
                    )

                    Spacer(
                        modifier =
                            Modifier.width(
                                5.dp
                            )
                    )

                    Text(
                        "Bỏ qua"
                    )
                }

                Button(
                    onClick =
                        onSave,

                    enabled =
                        state.currentSong != null &&
                            !busy,

                    modifier =
                        Modifier.weight(
                            1.3f
                        )
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(
                                    18.dp
                                ),

                            strokeWidth =
                                2.dp
                        )
                    }
                    else {
                        Icon(
                            imageVector =
                                Icons.Rounded.Save,

                            contentDescription =
                                null
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.width(
                                5.dp
                            )
                    )

                    Text(
                        "Lưu & tiếp"
                    )
                }
            }
        }
    }

    if (showFileSheet) {
        CompactTagFileSheet(
            files =
                state.files,

            selectedIndex =
                state.currentIndex,

            onDismiss = {
                showFileSheet =
                    false
            },

            onSelect = {
                    index ->

                showFileSheet =
                    false

                onSelectFile(
                    index
                )
            }
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = {
                if (!state.isDeleting) {
                    showDeleteConfirmation =
                        false
                }
            },

            title = {
                Text(
                    "Xóa file MP3?"
                )
            },

            text = {
                Text(
                    state.currentSong
                        ?.file
                        ?.displayName
                        ?.let {
                            "File sẽ bị xóa khỏi điện thoại:\n\n$it"
                        }
                        ?: "File sẽ bị xóa khỏi điện thoại."
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation =
                            false

                        onDelete()
                    },

                    enabled =
                        !state.isDeleting
                ) {
                    Text(
                        text =
                            "Xóa",

                        color =
                            MaterialTheme
                                .colorScheme
                                .error
                    )
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation =
                            false
                    },

                    enabled =
                        !state.isDeleting
                ) {
                    Text(
                        "Hủy"
                    )
                }
            }
        )
    }

    if (
        state.showArtistCandidates &&
        state.artistCandidates
            .isNotEmpty()
    ) {
        CompactArtistCandidatesDialog(
            candidates =
                state.artistCandidates,

            onSelect =
                onSelectArtist,

            onDismiss =
                onDismissArtists
        )
    }
}
