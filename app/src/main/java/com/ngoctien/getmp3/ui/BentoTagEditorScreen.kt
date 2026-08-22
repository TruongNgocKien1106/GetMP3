package com.ngoctien.getmp3.ui

import com.ngoctien.getmp3.ui.components.AppMediaCover

import com.ngoctien.getmp3.ui.components.AppLoadingPanel

import com.ngoctien.getmp3.ui.components.AppErrorNotice

import com.ngoctien.getmp3.ui.components.AppEmptyPanel

import com.ngoctien.getmp3.ui.components.AppDestructiveButton

import com.ngoctien.getmp3.ui.components.AppCard

import com.ngoctien.getmp3.ui.components.AppActionVisualState

import com.ngoctien.getmp3.ui.components.AppActionButton

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.ngoctien.getmp3.data.DownloadJobEntity
import com.ngoctien.getmp3.model.DownloadStatus
import com.ngoctien.getmp3.settings.AppSettings
import com.ngoctien.getmp3.settings.AppThemeMode
import com.ngoctien.getmp3.tag.MediaSongFile
import com.ngoctien.getmp3.tag.Mp3ListMetadata
import com.ngoctien.getmp3.tag.Mp3ListMetadataReader
import com.ngoctien.getmp3.ui.theme.BrandBlue
import com.ngoctien.getmp3.ui.theme.BrandCyan
import com.ngoctien.getmp3.ui.theme.BrandPink
import com.ngoctien.getmp3.ui.theme.BrandSky
import com.ngoctien.getmp3.ui.theme.BrandViolet
import com.ngoctien.getmp3.viewmodel.ArtistCaseMode
import com.ngoctien.getmp3.viewmodel.CompareIndexUiState
import com.ngoctien.getmp3.viewmodel.CompareViewModel
import com.ngoctien.getmp3.viewmodel.DownloadScreenUiState
import com.ngoctien.getmp3.viewmodel.DownloadViewModel
import com.ngoctien.getmp3.viewmodel.FfmpegReadyState
import com.ngoctien.getmp3.viewmodel.LyricsViewModel
import com.ngoctien.getmp3.viewmodel.MetadataRepairViewModel
import com.ngoctien.getmp3.viewmodel.SettingsViewModel
import com.ngoctien.getmp3.viewmodel.SearchDownloadSection
import com.ngoctien.getmp3.viewmodel.TagEditorUiState
import com.ngoctien.getmp3.viewmodel.TagEditorViewModel
import com.ngoctien.getmp3.viewmodel.YouTubeSearchViewModel
import java.io.File

@Composable
internal fun BentoTagEditorScreen(
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
    val focusManager =
        LocalFocusManager.current

    var showFileList by
        remember {
            mutableStateOf(false)
        }

    var showDeleteConfirmation by
        remember {
            mutableStateOf(false)
        }

    if (showFileList) {
        FileListDialog(
            files = state.files,

            selectedIndex =
                state.currentIndex,

            onDismiss = {
                showFileList = false
            },

            onSelect = {
                showFileList = false
                onSelectFile(it)
            }
        )
    }

    if (state.showArtistCandidates) {
        ArtistCandidateDialog(
            artists =
                state.artistCandidates,

            onSelect =
                onSelectArtist,

            /*
             * Đóng hộp này không thay đổi Artist.
             * Người dùng tiếp tục nhập tên thủ công.
             */
            onDismiss =
                onDismissArtists
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

            icon = {
                Icon(
                    imageVector =
                        Icons.Rounded.Delete,

                    contentDescription =
                        null,

                    tint =
                        MaterialTheme
                            .colorScheme
                            .error
                )
            },

            title = {
                Text(
                    text =
                        "Xóa file MP3?"
                )
            },

            text = {
                Text(
                    text =
                        state.currentSong
                            ?.file
                            ?.displayName
                            ?.let {
                                "File sẽ bị xóa khỏi điện thoại:`n`n$it"
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
                        text = "Xóa",

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
                    Text("Hủy")
                }
            }
        )
    }

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier
            .statusBarsPadding()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        focusManager.clearFocus(
                            force = true
                        )
                    }
                )
            },

        contentPadding =
            PaddingValues(
                start = 10.dp,
                top = 4.dp,
                end = 10.dp,
                bottom = 4.dp
            ),

        userScrollEnabled =
            false,

        verticalArrangement =
            Arrangement.spacedBy(6.dp)
    ) {
        item {
            AppPageHeader(
                title = "Sửa thẻ",

                subtitle =
                    if (
                        state.totalFiles > 0
                    ) {
                        "${state.displayIndex} / ${state.totalFiles} file MP3"
                    } else {
                        "Không có file MP3"
                    },

                icon =
                    Icons.Rounded.Tag,

                action = {
                    AppHeaderActionButton(
                        icon =
                            Icons.Rounded.List,

                        contentDescription =
                            "Danh sách file",

                        enabled =
                            !state.isScanning &&
                                !state.isLoadingSong &&
                                !state.isSaving &&
                                !state.isDeleting,

                        onClick = {
                            onRefreshFiles()
                            showFileList = true
                        }
                    )
                }
            )
        }
        if (
            state.isScanning ||
            state.isLoadingSong
        ) {
            item {
                AppLoadingPanel(
                    text =
                        if (state.isScanning) {
                            "Đang quét file MP3..."
                        } else {
                            "Đang đọc metadata..."
                        }
                )
            }
        } else if (
            state.files.isEmpty()
        ) {
            item {
                AppEmptyPanel(
                    text =
                        "Không có MP3 trong thư mục đã chọn"
                )
            }
        } else {
            state.currentSong?.let {
                    song ->

                item {
                    SongPreviewCard(
                        fileName =
                            state.previewFileName,

                        album =
                            state.selectedAlbum,

                        coverPath =
                            song.coverPath
                    )
                }

                item {
                    MetadataEditorCard(
                        state = state,

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
                    OutlinedButton(
                        onClick =
                            onEditLyrics,

                        enabled =
                            state.currentSong != null &&
                                !state.isSaving &&
                                !state.isDeleting,

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Icon(
                            imageVector =
                                Icons.Rounded.LibraryMusic,

                            contentDescription =
                                null
                        )

                        Spacer(
                            modifier =
                                Modifier.width(8.dp)
                        )

                        Text("Lời bài hát")
                    }
                }

                state.errorMessage?.let {
                    item {
                        AppErrorNotice(
                            text = it
                        )
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
                        AppDestructiveButton(
                            label =
                                "Xóa",

                            onClick = {
                                showDeleteConfirmation =
                                    true
                            },

                            enabled =
                                !state.isSaving &&
                                    !state.isDeleting,

                            loading =
                                state.isDeleting,

                            loadingLabel =
                                "Đang xóa...",

                            leadingIcon =
                                Icons.Rounded
                                    .Delete,

                            haptic =
                                true,

                            modifier =
                                Modifier.height(
                                    42.dp
                                )
                        )

                        OutlinedButton(
                            onClick = onSkip,

                            enabled =
                                !state.isSaving &&
                                    !state.isDeleting,

                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),

                            contentPadding =
                                PaddingValues(
                                    horizontal =
                                        10.dp
                                )
                        ) {
                            Text("Bỏ qua")
                        }

                        AppActionButton(
                            label =
                                "Lưu & xử lý",

                            onClick =
                                onSave,

                            enabled =
                                !state.isSaving &&
                                    !state.isDeleting,

                            modifier =
                                Modifier
                                    .weight(
                                        1.35f
                                    )
                                    .height(
                                        42.dp
                                    ),

                            state =
                                when {
                                    state.isSaving &&
                                        !state.isDeleting ->
                                        AppActionVisualState
                                            .LOADING

                                    !state.errorMessage
                                        .isNullOrBlank() ->
                                        AppActionVisualState
                                            .ERROR

                                    else ->
                                        AppActionVisualState
                                            .IDLE
                                },

                            loadingLabel =
                                "Đang xử lý...",

                            errorLabel =
                                "Thử lưu lại",

                            leadingIcon =
                                Icons.Rounded
                                    .Save,

                            haptic =
                                true
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun MetadataEditorCard(
    state: TagEditorUiState,

    onTitleChange: (String) -> Unit,
    onClearTitle: () -> Unit,

    onArtistChange: (String) -> Unit,
    onClearArtist: () -> Unit,

    onArtistCaseModeChange:
        (ArtistCaseMode) -> Unit,

    onAlbumChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onQuickFormat: () -> Unit
) {
    AppCard(
        modifier =
            Modifier.fillMaxWidth(),

        contentPadding =
            PaddingValues()
    ) {
        Column(
            modifier =
                Modifier.padding(10.dp),

            verticalArrangement =
                Arrangement.spacedBy(6.dp)
        ) {
            StableOutlinedTextField(
                value = state.title,

                onValueChange =
                    onTitleChange,

                modifier =
                    Modifier.fillMaxWidth(),

                label = {
                    Text("Title")
                },

                singleLine = true,

                keyboardOptions =
                    KeyboardOptions(
                        capitalization =
                            KeyboardCapitalization.Words
                    ),

                trailingIcon = {
                    if (state.title.isNotEmpty()) {
                        IconButton(
                            onClick =
                                onClearTitle
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Rounded.Clear,

                                contentDescription =
                                    "Xóa Title"
                            )
                        }
                    }
                }
            )

            StableOutlinedTextField(
                value = state.artist,

                onValueChange =
                    onArtistChange,

                modifier =
                    Modifier.fillMaxWidth(),

                label = {
                    Text("Artist")
                },

                singleLine = true,

                trailingIcon = {
                    if (state.artist.isNotEmpty()) {
                        IconButton(
                            onClick =
                                onClearArtist
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Rounded.Clear,

                                contentDescription =
                                    "Xóa Artist"
                            )
                        }
                    }
                }
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected =
                        state.artistCaseMode ==
                            ArtistCaseMode
                                .CAPITALIZE_WORDS,

                    onClick = {
                        onArtistCaseModeChange(
                            ArtistCaseMode
                                .CAPITALIZE_WORDS
                        )
                    },

                    label = {
                        Text("Viết hoa chữ đầu")
                    }
                )

                FilterChip(
                    selected =
                        state.artistCaseMode ==
                            ArtistCaseMode
                                .KEEP_ORIGINAL,

                    onClick = {
                        onArtistCaseModeChange(
                            ArtistCaseMode
                                .KEEP_ORIGINAL
                        )
                    },

                    label = {
                        Text("Giữ nguyên")
                    }
                )
            }

            OutlinedButton(
                onClick =
                    onQuickFormat,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.Tune,

                    contentDescription =
                        null
                )

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Text(
                    text =
                        "Format nhanh",

                    fontWeight =
                        FontWeight.SemiBold
                )

                if (
                    state.knownArtistCount > 0
                ) {
                    Spacer(
                        modifier =
                            Modifier.width(6.dp)
                    )

                    Text(
                        text =
                            "• ${state.knownArtistCount} ca sĩ",

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }
            }

            AnimatedSelectorField(
                label = "Album",

                value =
                    state.selectedAlbum,

                options =
                    state.albumOptions,

                onSelected =
                    onAlbumChange
            )

            StableOutlinedTextField(
                value =
                    state.year,

                onValueChange =
                    onYearChange,

                modifier =
                    Modifier
                        .fillMaxWidth(),

                label = {
                    Text(
                        "Year"
                    )
                },

                singleLine =
                    true,

                supportingText = {
                    Text(
                        "4 chữ số · năm phát hành của đúng bản thu/version"
                    )
                }
            )
        }
    }
}

@Composable
private fun SongPreviewCard(
    fileName: String,
    album: String,
    coverPath: String?
) {
    AppCard(
        modifier =
            Modifier.fillMaxWidth(),

        contentPadding =
            PaddingValues()
    ) {
        Row(
            modifier =
                Modifier.padding(8.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            AppMediaCover(
                model =
                    coverPath?.let(::File),

                size = 60
            )

            Spacer(
                modifier =
                    Modifier.width(8.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {
                Text(
                    text = fileName,

                    maxLines = 2,

                    overflow =
                        TextOverflow.Ellipsis,

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,

                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(2.dp)
                )

                Surface(
                    shape = CircleShape,

                    color =
                        MaterialTheme
                            .colorScheme
                            .primaryContainer
                ) {
                    Text(
                        text = album,

                        modifier =
                            Modifier.padding(
                                horizontal = 8.dp,
                                vertical = 2.dp
                            ),

                        style =
                            MaterialTheme
                                .typography
                                .labelMedium,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtistCandidateDialog(
    artists: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest =
            onDismiss
    ) {
        Surface(
            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(28.dp),

            color =
                MaterialTheme
                    .colorScheme
                    .surface
        ) {
            Column(
                modifier =
                    Modifier.padding(
                        vertical = 18.dp
                    )
            ) {
                Text(
                    text =
                        "Chọn Artist phù hợp",

                    modifier =
                        Modifier.padding(
                            horizontal = 20.dp
                        ),

                    style =
                        MaterialTheme
                            .typography
                            .titleLarge,

                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )

                Text(
                    text =
                        "Kết quả được tìm từ Library.",

                    modifier =
                        Modifier.padding(
                            horizontal = 20.dp
                        ),

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                Column(
                    modifier = Modifier
                        .heightIn(
                            max = 420.dp
                        )
                        .verticalScroll(
                            rememberScrollState()
                        )
                ) {
                    artists.forEach { artist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .bouncyClickable {
                                    onSelect(
                                        artist
                                    )
                                }
                                .padding(
                                    horizontal =
                                        20.dp,

                                    vertical =
                                        15.dp
                                ),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Rounded
                                        .MusicNote,

                                contentDescription =
                                    null,

                                tint =
                                    MaterialTheme
                                        .colorScheme
                                        .primary
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(
                                        12.dp
                                    )
                            )

                            Text(
                                text = artist,

                                modifier =
                                    Modifier.weight(
                                        1f
                                    ),

                                fontWeight =
                                    FontWeight.Medium
                            )
                        }

                        HorizontalDivider(
                            modifier =
                                Modifier.padding(
                                    start = 56.dp
                                )
                        )
                    }
                }

                TextButton(
                    onClick = onDismiss,

                    modifier =
                        Modifier.align(
                            Alignment.End
                        )
                ) {
                    Text(
                        "Không dùng, tự nhập"
                    )
                }
            }
        }
    }
}
