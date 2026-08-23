package com.ngoctien.getmp3.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.lyrics.LibrarySongCandidate
import com.ngoctien.getmp3.lyrics.LyricsScreen
import com.ngoctien.getmp3.lyrics.LyricsSearchResult
import com.ngoctien.getmp3.viewmodel.LyricsUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
internal fun LyricsTab(
    state: LyricsUiState,
    actions: LyricsUiActions,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    var showOverwriteDialog by
        remember {
            mutableStateOf(false)
        }

    if (state.writeTargetPicker.isVisible) {
        WriteTargetSheet(
            state =
                state.writeTargetPicker,
            selectedLyrics =
                state.selectedResult,
            currentSong =
                state.selectedSong,
            isSaving =
                state.isSaving,
            onDismiss =
                actions.dismissWriteTargetPicker,
            onSelect =
                actions.selectWriteTarget,
            onConfirm =
                actions.confirmWriteTarget
        )
    }

    if (
        state.showDirectOverwriteDialog
    ) {
        AlertDialog(
            onDismissRequest =
                actions.dismissDirectOverwrite,

            containerColor =
                MaterialTheme
                    .colorScheme
                    .surface,

            iconContentColor =
                MaterialTheme
                    .colorScheme
                    .primary,

            titleContentColor =
                MaterialTheme
                    .colorScheme
                    .onSurface,

            textContentColor =
                MaterialTheme
                    .colorScheme
                    .onSurface,

            icon = {
                Icon(
                    imageVector =
                        Icons.Rounded.Save,

                    contentDescription =
                        null
                )
            },

            title = {
                Text(
                    "Bài hát này đã có lời"
                )
            },

            text = {
                val oldLines =
                    state.pendingExistingLyrics
                        .lineSequence()
                        .count()

                val newLines =
                    state.pendingWriteLyrics
                        .lineSequence()
                        .count()

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        state.pendingWriteTarget
                            ?.displayName
                            .orEmpty()
                    )

                    Text(
                        "Lời hiện tại: $oldLines dòng"
                    )

                    Text(
                        "Lời mới: $newLines dòng"
                    )

                    Text(
                        "Ghi lời mới sẽ thay thế lời hiện tại."
                    )
                }
            },

            confirmButton = {
                TextButton(
                    onClick =
                        actions.confirmDirectOverwrite
                ) {
                    Text("Thay thế lời")
                }
            },

            dismissButton = {
                TextButton(
                    onClick =
                        actions.dismissDirectOverwrite
                ) {
                    Text("Hủy")
                }
            }
        )
    }
    if (showOverwriteDialog) {
        AlertDialog(
            onDismissRequest = {
                showOverwriteDialog =
                    false
            },

            containerColor =
                MaterialTheme
                    .colorScheme
                    .surface,

            iconContentColor =
                MaterialTheme
                    .colorScheme
                    .primary,

            titleContentColor =
                MaterialTheme
                    .colorScheme
                    .onSurface,

            textContentColor =
                MaterialTheme
                    .colorScheme
                    .onSurface,

            icon = {
                Icon(
                    imageVector =
                        Icons.Rounded.Save,

                    contentDescription =
                        null
                )
            },

            title = {
                Text(
                    "Thay lời hiện tại?"
                )
            },

            text = {
                val oldLines =
                    state.existingLyrics
                        .lineSequence()
                        .count()

                val newLines =
                    state.editorLyrics
                        .lineSequence()
                        .count()

                Text(
                    "File đã có $oldLines dòng lời. Nội dung mới có $newLines dòng. Lời cũ sẽ được thay bằng lời mới."
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showOverwriteDialog =
                            false

                        actions.saveLyrics()
                    }
                ) {
                    Text("Thay lời")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showOverwriteDialog =
                            false
                    }
                ) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        modifier =
            modifier.fillMaxSize(),

        contentWindowInsets =
            WindowInsets(
                0,
                0,
                0,
                0
            ),

        containerColor =
            androidx.compose.ui.graphics
                .Color.Transparent,

        snackbarHost = {
            SnackbarHost(
                hostState =
                    snackbarHostState
            )
        }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    scaffoldPadding
                )
                .statusBarsPadding()
                .padding(
                    start = 16.dp,
                    top = 10.dp,
                    end = 16.dp
                )
        ) {
            if (state.screen != LyricsScreen.READER) {
                LyricsHeader(
                    state = state,
                    onBack =
                        actions.back
                )

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )
            }

            when (state.screen) {
                LyricsScreen.LIBRARY -> {
                    LyricsLibraryScreen(
                        state = state,

                        onQueryChange =
                            actions.setQuery,

                        onClear =
                            actions.clearQuery,

                        onSubmit =
                            actions.submitSearch,

                        onSelectResult =
                            actions.selectLyricsResult,

                        onRetry =
                            actions.retryLyricsSearch
                    )
                }

                LyricsScreen.RESULTS -> {
                    LyricsResultsScreen(
                        state = state,

                        onSelectResult =
                            actions.selectLyricsResult,

                        onRetry =
                            actions.retryLyricsSearch
                    )
                }

                LyricsScreen.READER -> {
                    LyricsReaderScreen(
                        state = state,
                        onBack =
                            actions.back,
                        onIncreaseFont =
                            actions.increaseFontSize,
                        onDecreaseFont =
                            actions.decreaseFontSize,
                        onToggleAutoScroll =
                            actions.toggleAutoScroll,
                        onScrollSpeedChange =
                            actions.setAutoScrollSpeed,
                        onEditLyrics =
                            actions.editCurrentLyrics,
                        onWriteLyrics =
                            actions.openWriteTargetPicker
                    )
                }

                LyricsScreen.EDITOR -> {
                    LyricsEditorScreen(
                        state = state,

                        onLyricsChange =
                            actions.setEditorLyrics,

                        onPasteLyrics =
                            actions.pasteEditorLyrics,

                        onClear =
                            actions.clearEditorLyrics,

                        onRestoreFoundLyrics =
                            actions.restoreFoundLyrics,

                        onRestoreStoredLyrics =
                            actions.restoreStoredLyrics,

                        onSave = {
                            val hasExistingLyrics =
                                state.existingLyrics
                                    .isNotBlank()

                            val changed =
                                state.existingLyrics
                                    .trim() !=
                                    state.editorLyrics
                                        .trim()

                            if (
                                hasExistingLyrics &&
                                changed
                            ) {
                                showOverwriteDialog =
                                    true
                            } else {
                                actions.saveLyrics()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LyricsHeader(
    state: LyricsUiState,
    onBack: () -> Unit
) {
    val subtitle =
        when (state.screen) {
            LyricsScreen.LIBRARY ->
                "Tìm lời • karaoke • ghi khi cần"

            LyricsScreen.RESULTS ->
                state.selectedSong
                    ?.title
                    ?: "Kết quả tìm lyrics"

            LyricsScreen.READER ->
                state.selectedResult
                    ?.artistName
                    ?.ifBlank {
                        "Đang đọc lyrics"
                    }
                    ?: "Đang đọc lyrics"

            LyricsScreen.EDITOR ->
                state.editorTarget
                    ?.displayName
                    ?: "Ghi lyrics vào MP3"
        }

    AppPageHeader(
        title =
            "Lyrics",

        subtitle =
            subtitle,

        icon =
            Icons.Rounded
                .LibraryMusic,

        action =
            if (
                state.screen !=
                LyricsScreen.LIBRARY
            ) {
                {
                    AppHeaderActionButton(
                        icon =
                            Icons.Rounded
                                .ArrowBack,

                        contentDescription =
                            "Quay lại",

                        onClick =
                            onBack
                    )
                }
            } else {
                null
            }
    )
}
