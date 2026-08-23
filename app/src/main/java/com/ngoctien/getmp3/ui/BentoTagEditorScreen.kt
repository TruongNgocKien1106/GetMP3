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

@Suppress("UNUSED_PARAMETER")
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

    if (showFileList) {
        FileListDialog(
            files =
                state.files,

            selectedIndex =
                state.currentIndex,

            onDismiss = {
                showFileList =
                    false
            },

            onSelect = {
                    index ->

                showFileList =
                    false

                onSelectFile(
                    index
                )
            }
        )
    }

    if (state.showArtistCandidates) {
        ArtistCandidateDialog(
            artists =
                state.artistCandidates,

            onSelect =
                onSelectArtist,

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
                        Icons.Rounded
                            .Delete,

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

    AppScreenBackdrop(
        modifier =
            modifier
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(
                        start = 12.dp,
                        top = 7.dp,
                        end = 12.dp,
                        bottom = 7.dp
                    )
                    .pointerInput(
                        Unit
                    ) {
                        detectTapGestures(
                            onTap = {
                                focusManager
                                    .clearFocus(
                                        force =
                                            true
                                    )
                            }
                        )
                    },

            verticalArrangement =
                Arrangement.spacedBy(
                    7.dp
                )
        ) {
            PremiumTagEditorHeader(
                displayIndex =
                    state.displayIndex,

                totalFiles =
                    state.totalFiles,

                enabled =
                    !state.isScanning &&
                        !state.isLoadingSong &&
                        !state.isSaving &&
                        !state.isDeleting,

                onOpenFileList = {
                    onRefreshFiles()

                    showFileList =
                        true
                }
            )

            when {
                state.isScanning ||
                    state.isLoadingSong -> {

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(
                                    1f
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {
                        AppLoadingPanel(
                            text =
                                if (
                                    state.isScanning
                                ) {
                                    "Đang quét file MP3..."
                                }
                                else {
                                    "Đang đọc metadata..."
                                }
                        )
                    }
                }

                state.files.isEmpty() -> {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(
                                    1f
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {
                        AppEmptyPanel(
                            text =
                                "Không có MP3 trong thư mục đã chọn"
                        )
                    }
                }

                else -> {
                    state.currentSong?.let {
                            song ->

                        SongPreviewCard(
                            fileName =
                                state.previewFileName,

                            album =
                                state.selectedAlbum,

                            coverPath =
                                song.coverPath
                        )

                        MetadataEditorCard(
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

                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        )

                        AnimatedVisibility(
                            visible =
                                !state.errorMessage
                                    .isNullOrBlank(),

                            enter =
                                fadeIn(),

                            exit =
                                fadeOut()
                        ) {
                            state.errorMessage
                                ?.let {
                                        message ->

                                    Text(
                                        text =
                                            message,

                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    horizontal =
                                                        4.dp
                                                ),

                                        maxLines =
                                            1,

                                        overflow =
                                            TextOverflow
                                                .Ellipsis,

                                        style =
                                            MaterialTheme
                                                .typography
                                                .labelSmall,

                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .error
                                    )
                                }
                        }

                        TagEditorActionDock(
                            knownArtistCount =
                                state.knownArtistCount,

                            busy =
                                state.isSaving ||
                                    state.isDeleting,

                            saving =
                                state.isSaving,

                            deleting =
                                state.isDeleting,

                            onQuickFormat =
                                onQuickFormat,

                            onSave =
                                onSave,

                            onSkip =
                                onSkip,

                            onDelete = {
                                showDeleteConfirmation =
                                    true
                            }
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

    modifier: Modifier =
        Modifier
) {
    var showAlbumPicker by
        rememberSaveable {
            mutableStateOf(
                false
            )
        }

    var showYearPicker by
        rememberSaveable {
            mutableStateOf(
                false
            )
        }

    if (showAlbumPicker) {
        TagAlbumPickerDialog(
            currentValue =
                state.selectedAlbum,

            options =
                state.albumOptions,

            onSelected = {
                    value ->

                onAlbumChange(
                    value
                )

                showAlbumPicker =
                    false
            },

            onDismiss = {
                showAlbumPicker =
                    false
            }
        )
    }

    if (showYearPicker) {
        TagYearPickerDialog(
            currentValue =
                state.year,

            onSelected = {
                    value ->

                onYearChange(
                    value
                )

                showYearPicker =
                    false
            },

            onClear = {
                onYearChange(
                    ""
                )

                showYearPicker =
                    false
            },

            onDismiss = {
                showYearPicker =
                    false
            }
        )
    }

    val colors =
        MaterialTheme.colorScheme

    Surface(
        modifier =
            modifier
                .fillMaxWidth(),

        shape =
            RoundedCornerShape(
                23.dp
            ),

        color =
            colors
                .surface
                .copy(
                    alpha =
                        0.88f
                ),

        border =
            BorderStroke(
                width =
                    1.dp,

                color =
                    colors
                        .outline
                        .copy(
                            alpha =
                                0.24f
                        )
            ),

        tonalElevation =
            5.dp,

        shadowElevation =
            7.dp
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        10.dp
                    ),

            verticalArrangement =
                Arrangement.spacedBy(
                    6.dp
                )
        ) {
            StableOutlinedTextField(
                value =
                    state.title,

                onValueChange =
                    onTitleChange,

                modifier =
                    Modifier
                        .fillMaxWidth(),

                label = {
                    Text(
                        "Title"
                    )
                },

                placeholder = {
                    Text(
                        "Nhập Title"
                    )
                },

                singleLine =
                    true,

                shape =
                    RoundedCornerShape(
                        16.dp
                    ),

                keyboardOptions =
                    KeyboardOptions(
                        capitalization =
                            KeyboardCapitalization
                                .Words
                    ),

                trailingIcon = {
                    if (
                        state.title
                            .isNotEmpty()
                    ) {
                        IconButton(
                            onClick =
                                onClearTitle
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Rounded
                                        .Clear,

                                contentDescription =
                                    "Xóa Title"
                            )
                        }
                    }
                }
            )

            StableOutlinedTextField(
                value =
                    state.artist,

                onValueChange =
                    onArtistChange,

                modifier =
                    Modifier
                        .fillMaxWidth(),

                label = {
                    Text(
                        "Artist"
                    )
                },

                placeholder = {
                    Text(
                        "Nhập Artist"
                    )
                },

                singleLine =
                    true,

                shape =
                    RoundedCornerShape(
                        16.dp
                    ),

                trailingIcon = {
                    if (
                        state.artist
                            .isNotEmpty()
                    ) {
                        IconButton(
                            onClick =
                                onClearArtist
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Rounded
                                        .Clear,

                                contentDescription =
                                    "Xóa Artist"
                            )
                        }
                    }
                }
            )

            ArtistCaseSegmentedControl(
                selectedMode =
                    state.artistCaseMode,

                onSelected =
                    onArtistCaseModeChange
            )

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {
                CompactMetadataPicker(
                    label =
                        "Album",

                    value =
                        state.selectedAlbum
                            .ifBlank {
                                "Chưa phân loại"
                            },

                    modifier =
                        Modifier.weight(
                            1.55f
                        ),

                    onClick = {
                        showAlbumPicker =
                            true
                    }
                )

                CompactMetadataPicker(
                    label =
                        "Year",

                    value =
                        state.year
                            .ifBlank {
                                "Chọn năm"
                            },

                    modifier =
                        Modifier.weight(
                            0.85f
                        ),

                    onClick = {
                        showYearPicker =
                            true
                    }
                )
            }
        }
    }
}

@Composable
private fun SongPreviewCard(
    fileName: String,
    album: String,
    coverPath: String?
) {
    val colors =
        MaterialTheme.colorScheme

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    72.dp
                ),

        shape =
            RoundedCornerShape(
                20.dp
            ),

        color =
            colors
                .surface
                .copy(
                    alpha =
                        0.84f
                ),

        border =
            BorderStroke(
                width =
                    1.dp,

                color =
                    colors
                        .outline
                        .copy(
                            alpha =
                                0.24f
                        )
            ),

        tonalElevation =
            4.dp,

        shadowElevation =
            6.dp
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        8.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            AppMediaCover(
                model =
                    coverPath
                        ?.let(
                            ::File
                        ),

                size =
                    56
            )

            Spacer(
                modifier =
                    Modifier.width(
                        10.dp
                    )
            )

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    ),

                verticalArrangement =
                    Arrangement.Center
            ) {
                Text(
                    text =
                        fileName,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow
                            .Ellipsis,

                    style =
                        MaterialTheme
                            .typography
                            .titleSmall,

                    fontWeight =
                        FontWeight.Black
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            4.dp
                        )
                )

                Surface(
                    shape =
                        RoundedCornerShape(
                            99.dp
                        ),

                    color =
                        BrandBlue
                            .copy(
                                alpha =
                                    0.18f
                            ),

                    border =
                        BorderStroke(
                            width =
                                1.dp,

                            color =
                                BrandCyan
                                    .copy(
                                        alpha =
                                            0.22f
                                    )
                        )
                ) {
                    Text(
                        text =
                            album.ifBlank {
                                "Chưa phân loại"
                            },

                        modifier =
                            Modifier.padding(
                                horizontal =
                                    8.dp,

                                vertical =
                                    2.dp
                            ),

                        maxLines =
                            1,

                        overflow =
                            TextOverflow
                                .Ellipsis,

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            colors
                                .onSurface
                    )
                }
            }
        }
    }
}


@Composable
private fun PremiumTagEditorHeader(
    displayIndex: Int,
    totalFiles: Int,
    enabled: Boolean,
    onOpenFileList: () -> Unit
) {
    val colors =
        MaterialTheme.colorScheme

    val transition =
        rememberInfiniteTransition(
            label =
                "tag-editor-header-motion"
        )

    val pulse by
        transition.animateFloat(
            initialValue =
                0.96f,

            targetValue =
                1.06f,

            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(
                            durationMillis =
                                2400
                        ),

                    repeatMode =
                        RepeatMode.Reverse
                ),

            label =
                "tag-editor-icon-pulse"
        )

    val tilt by
        transition.animateFloat(
            initialValue =
                -3f,

            targetValue =
                3f,

            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(
                            durationMillis =
                                3300
                        ),

                    repeatMode =
                        RepeatMode.Reverse
                ),

            label =
                "tag-editor-icon-tilt"
        )

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    60.dp
                ),

        shape =
            RoundedCornerShape(
                22.dp
            ),

        color =
            colors
                .surface
                .copy(
                    alpha =
                        0.84f
                ),

        border =
            BorderStroke(
                width =
                    1.dp,

                color =
                    BrandSky
                        .copy(
                            alpha =
                                0.32f
                        )
            ),

        tonalElevation =
            5.dp,

        shadowElevation =
            8.dp
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal =
                            9.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Box(
                modifier =
                    Modifier
                        .size(
                            44.dp
                        )
                        .graphicsLayer {
                            scaleX =
                                pulse

                            scaleY =
                                pulse

                            rotationZ =
                                tilt
                        }
                        .clip(
                            RoundedCornerShape(
                                14.dp
                            )
                        )
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    BrandCyan,
                                    BrandBlue,
                                    BrandViolet
                                )
                            )
                        ),

                contentAlignment =
                    Alignment.Center
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded
                            .Tag,

                    contentDescription =
                        null,

                    modifier =
                        Modifier.size(
                            25.dp
                        ),

                    tint =
                        Color.White
                )
            }

            Spacer(
                modifier =
                    Modifier.width(
                        10.dp
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
                        "Sửa thẻ",

                    style =
                        MaterialTheme
                            .typography
                            .titleLarge,

                    fontWeight =
                        FontWeight.Black
                )

                Text(
                    text =
                        "Metadata MP3",

                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,

                    color =
                        colors
                            .onSurfaceVariant
                )
            }

            Surface(
                shape =
                    RoundedCornerShape(
                        99.dp
                    ),

                color =
                    colors
                        .surfaceVariant
                        .copy(
                            alpha =
                                0.62f
                        ),

                border =
                    BorderStroke(
                        width =
                            1.dp,

                        color =
                            colors
                                .outline
                                .copy(
                                    alpha =
                                        0.20f
                                )
                    )
            ) {
                Text(
                    text =
                        if (
                            totalFiles > 0
                        ) {
                            "$displayIndex / $totalFiles"
                        }
                        else {
                            "0 / 0"
                        },

                    modifier =
                        Modifier.padding(
                            horizontal =
                                9.dp,

                            vertical =
                                5.dp
                        ),

                    style =
                        MaterialTheme
                            .typography
                            .labelMedium,

                    fontWeight =
                        FontWeight.Black
                )
            }

            Spacer(
                modifier =
                    Modifier.width(
                        6.dp
                    )
            )

            Surface(
                modifier =
                    Modifier
                        .size(
                            42.dp
                        )
                        .bouncyClickable(
                            enabled =
                                enabled
                        ) {
                            onOpenFileList()
                        },

                shape =
                    CircleShape,

                color =
                    colors
                        .primaryContainer,

                contentColor =
                    colors
                        .onPrimaryContainer,

                tonalElevation =
                    5.dp,

                shadowElevation =
                    6.dp
            ) {
                Box(
                    modifier =
                        Modifier.fillMaxSize(),

                    contentAlignment =
                        Alignment.Center
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded
                                .List,

                        contentDescription =
                            "Danh sách file",

                        modifier =
                            Modifier.size(
                                21.dp
                            )
                    )
                }
            }
        }
    }
}


@Composable
private fun ArtistCaseSegmentedControl(
    selectedMode: ArtistCaseMode,
    onSelected: (ArtistCaseMode) -> Unit
) {
    val colors =
        MaterialTheme.colorScheme

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    38.dp
                ),

        shape =
            RoundedCornerShape(
                14.dp
            ),

        color =
            colors
                .surfaceVariant
                .copy(
                    alpha =
                        0.64f
                ),

        border =
            BorderStroke(
                width =
                    1.dp,

                color =
                    colors
                        .outline
                        .copy(
                            alpha =
                                0.20f
                        )
            )
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        3.dp
                    ),

            horizontalArrangement =
                Arrangement.spacedBy(
                    3.dp
                )
        ) {
            ArtistCaseSegment(
                text =
                    "Viết hoa chữ đầu",

                selected =
                    selectedMode ==
                        ArtistCaseMode
                            .CAPITALIZE_WORDS,

                modifier =
                    Modifier.weight(
                        1.25f
                    ),

                onClick = {
                    onSelected(
                        ArtistCaseMode
                            .CAPITALIZE_WORDS
                    )
                }
            )

            ArtistCaseSegment(
                text =
                    "Giữ nguyên",

                selected =
                    selectedMode ==
                        ArtistCaseMode
                            .KEEP_ORIGINAL,

                modifier =
                    Modifier.weight(
                        0.85f
                    ),

                onClick = {
                    onSelected(
                        ArtistCaseMode
                            .KEEP_ORIGINAL
                    )
                }
            )
        }
    }
}


@Composable
private fun ArtistCaseSegment(
    text: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier =
            modifier
                .fillMaxSize()
                .bouncyClickable(
                    pressedScale =
                        0.975f,

                    onClick =
                        onClick
                ),

        shape =
            RoundedCornerShape(
                11.dp
            ),

        color =
            if (selected) {
                BrandViolet
                    .copy(
                        alpha =
                            0.46f
                    )
            }
            else {
                Color.Transparent
            },

        contentColor =
            MaterialTheme
                .colorScheme
                .onSurface,

        tonalElevation =
            if (selected) {
                4.dp
            }
            else {
                0.dp
            }
    ) {
        Box(
            modifier =
                Modifier.fillMaxSize(),

            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    text,

                style =
                    MaterialTheme
                        .typography
                        .labelMedium,

                fontWeight =
                    if (selected) {
                        FontWeight.Black
                    }
                    else {
                        FontWeight.Medium
                    }
            )
        }
    }
}


@Composable
private fun CompactMetadataPicker(
    label: String,
    value: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val colors =
        MaterialTheme.colorScheme

    Surface(
        modifier =
            modifier
                .height(
                    54.dp
                )
                .bouncyClickable(
                    pressedScale =
                        0.98f,

                    onClick =
                        onClick
                ),

        shape =
            RoundedCornerShape(
                16.dp
            ),

        color =
            colors
                .surfaceVariant
                .copy(
                    alpha =
                        0.64f
                ),

        border =
            BorderStroke(
                width =
                    1.dp,

                color =
                    colors
                        .outline
                        .copy(
                            alpha =
                                0.25f
                        )
            ),

        tonalElevation =
            2.dp
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal =
                            11.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Column(
                modifier =
                    Modifier.weight(
                        1f
                    ),

                verticalArrangement =
                    Arrangement.Center
            ) {
                Text(
                    text =
                        label,

                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,

                    color =
                        colors
                            .onSurfaceVariant
                )

                Text(
                    text =
                        value,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow
                            .Ellipsis,

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    fontWeight =
                        FontWeight.Bold
                )
            }

            Icon(
                imageVector =
                    Icons.Rounded
                        .ExpandMore,

                contentDescription =
                    null,

                modifier =
                    Modifier.size(
                        19.dp
                    ),

                tint =
                    colors
                        .onSurfaceVariant
            )
        }
    }
}


@Composable
private fun TagEditorActionDock(
    knownArtistCount: Int,
    busy: Boolean,
    saving: Boolean,
    deleting: Boolean,
    onQuickFormat: () -> Unit,
    onSave: () -> Unit,
    onSkip: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier =
            Modifier.fillMaxWidth(),

        verticalArrangement =
            Arrangement.spacedBy(
                6.dp
            )
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {
            TagEditor3DButton(
                label =
                    "Format nhanh",

                subtitle =
                    if (
                        knownArtistCount > 0
                    ) {
                        "$knownArtistCount ca sĩ"
                    }
                    else {
                        null
                    },

                icon =
                    Icons.Rounded
                        .Tune,

                gradient =
                    listOf(
                        BrandViolet,
                        BrandPink
                    ),

                enabled =
                    !busy,

                modifier =
                    Modifier.weight(
                        0.92f
                    ),

                onClick =
                    onQuickFormat
            )

            TagEditor3DButton(
                label =
                    if (saving) {
                        "Đang lưu..."
                    }
                    else {
                        "Lưu & tiếp"
                    },

                subtitle =
                    null,

                icon =
                    Icons.Rounded
                        .Save,

                gradient =
                    listOf(
                        BrandSky,
                        BrandBlue,
                        BrandViolet
                    ),

                enabled =
                    !busy,

                modifier =
                    Modifier.weight(
                        1.08f
                    ),

                onClick =
                    onSave
            )
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(
                        38.dp
                    ),

            horizontalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {
            TagEditorSubtleAction(
                text =
                    "Bỏ qua",

                icon =
                    Icons.Rounded
                        .SkipNext,

                enabled =
                    !busy,

                modifier =
                    Modifier.weight(
                        1f
                    ),

                destructive =
                    false,

                onClick =
                    onSkip
            )

            TagEditorSubtleAction(
                text =
                    if (deleting) {
                        "Đang xóa..."
                    }
                    else {
                        "Xóa file"
                    },

                icon =
                    Icons.Rounded
                        .Delete,

                enabled =
                    !busy,

                modifier =
                    Modifier.weight(
                        1f
                    ),

                destructive =
                    true,

                onClick =
                    onDelete
            )
        }
    }
}


@Composable
private fun TagEditor3DButton(
    label: String,
    subtitle: String?,
    icon: ImageVector,
    gradient: List<Color>,
    enabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val shape =
        RoundedCornerShape(
            17.dp
        )

    val transition =
        rememberInfiniteTransition(
            label =
                "tag-editor-3d-button"
        )

    val iconPulse by
        transition.animateFloat(
            initialValue =
                0.96f,

            targetValue =
                1.06f,

            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(
                            durationMillis =
                                2100
                        ),

                    repeatMode =
                        RepeatMode.Reverse
                ),

            label =
                "tag-editor-action-icon"
        )

    val sheen by
        transition.animateFloat(
            initialValue =
                -1f,

            targetValue =
                1f,

            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(
                            durationMillis =
                                5200
                        ),

                    repeatMode =
                        RepeatMode.Restart
                ),

            label =
                "tag-editor-action-sheen"
        )

    val density =
        LocalDensity.current

    val sheenTravel =
        with(density) {
            180.dp.toPx()
        }

    Surface(
        modifier =
            modifier
                .height(
                    52.dp
                )
                .shadow(
                    elevation =
                        if (enabled) {
                            9.dp
                        }
                        else {
                            2.dp
                        },

                    shape =
                        shape
                )
                .bouncyClickable(
                    enabled =
                        enabled,

                    pressedScale =
                        0.965f,

                    onClick =
                        onClick
                ),

        shape =
            shape,

        color =
            Color.Transparent
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            gradient.map {
                                    color ->

                                if (enabled) {
                                    color
                                }
                                else {
                                    color.copy(
                                        alpha =
                                            0.34f
                                    )
                                }
                            }
                        )
                    )
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX =
                                sheen *
                                    sheenTravel
                        }
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.White
                                        .copy(
                                            alpha =
                                                if (enabled) {
                                                    0.12f
                                                }
                                                else {
                                                    0f
                                                }
                                        ),
                                    Color.Transparent
                                )
                            )
                        )
            )

            Box(
                modifier =
                    Modifier
                        .align(
                            Alignment.TopCenter
                        )
                        .fillMaxWidth()
                        .height(
                            1.dp
                        )
                        .background(
                            Color.White
                                .copy(
                                    alpha =
                                        0.34f
                                )
                        )
            )

            Box(
                modifier =
                    Modifier
                        .align(
                            Alignment.BottomCenter
                        )
                        .fillMaxWidth()
                        .height(
                            4.dp
                        )
                        .background(
                            Color.Black
                                .copy(
                                    alpha =
                                        0.16f
                                )
                        )
            )

            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal =
                                12.dp
                        ),

                verticalAlignment =
                    Alignment.CenterVertically,

                horizontalArrangement =
                    Arrangement.Center
            ) {
                Icon(
                    imageVector =
                        icon,

                    contentDescription =
                        null,

                    modifier =
                        Modifier
                            .size(
                                20.dp
                            )
                            .graphicsLayer {
                                scaleX =
                                    iconPulse

                                scaleY =
                                    iconPulse
                            },

                    tint =
                        Color.White
                )

                Spacer(
                    modifier =
                        Modifier.width(
                            8.dp
                        )
                )

                Column(
                    verticalArrangement =
                        Arrangement.Center
                ) {
                    Text(
                        text =
                            label,

                        style =
                            MaterialTheme
                                .typography
                                .labelLarge,

                        fontWeight =
                            FontWeight.Black,

                        color =
                            Color.White
                    )

                    if (
                        !subtitle
                            .isNullOrBlank()
                    ) {
                        Text(
                            text =
                                subtitle,

                            style =
                                MaterialTheme
                                    .typography
                                    .labelSmall,

                            color =
                                Color.White
                                    .copy(
                                        alpha =
                                            0.74f
                                    )
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun TagEditorSubtleAction(
    text: String,
    icon: ImageVector,
    enabled: Boolean,
    modifier: Modifier,
    destructive: Boolean,
    onClick: () -> Unit
) {
    val colors =
        MaterialTheme.colorScheme

    val actionColor =
        if (destructive) {
            colors.error
        }
        else {
            colors
                .onSurfaceVariant
        }

    Surface(
        modifier =
            modifier
                .fillMaxSize()
                .bouncyClickable(
                    enabled =
                        enabled,

                    pressedScale =
                        0.975f,

                    onClick =
                        onClick
                ),

        shape =
            RoundedCornerShape(
                14.dp
            ),

        color =
            if (destructive) {
                colors
                    .errorContainer
                    .copy(
                        alpha =
                            0.16f
                    )
            }
            else {
                colors
                    .surfaceVariant
                    .copy(
                        alpha =
                            0.42f
                    )
            },

        border =
            BorderStroke(
                width =
                    1.dp,

                color =
                    actionColor
                        .copy(
                            alpha =
                                0.35f
                        )
            )
    ) {
        Row(
            modifier =
                Modifier.fillMaxSize(),

            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.Center
        ) {
            Icon(
                imageVector =
                    icon,

                contentDescription =
                    null,

                modifier =
                    Modifier.size(
                        17.dp
                    ),

                tint =
                    actionColor
            )

            Spacer(
                modifier =
                    Modifier.width(
                        6.dp
                    )
            )

            Text(
                text =
                    text,

                style =
                    MaterialTheme
                        .typography
                        .labelMedium,

                fontWeight =
                    FontWeight.Bold,

                color =
                    actionColor
            )
        }
    }
}


@Composable
private fun TagAlbumPickerDialog(
    currentValue: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedIndex =
        options
            .indexOf(
                currentValue
            )
            .coerceAtLeast(
                0
            )

    val listState =
        rememberLazyListState(
            initialFirstVisibleItemIndex =
                selectedIndex
        )

    Dialog(
        onDismissRequest =
            onDismiss
    ) {
        Surface(
            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(
                    26.dp
                ),

            color =
                MaterialTheme
                    .colorScheme
                    .surface,

            tonalElevation =
                12.dp,

            shadowElevation =
                18.dp
        ) {
            Column(
                modifier =
                    Modifier.padding(
                        16.dp
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {
                Text(
                    text =
                        "Chọn Album",

                    style =
                        MaterialTheme
                            .typography
                            .titleLarge,

                    fontWeight =
                        FontWeight.Black
                )

                Text(
                    text =
                        "Chọn nhóm phù hợp cho bài hát.",

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                LazyColumn(
                    modifier =
                        Modifier.heightIn(
                            max =
                                390.dp
                        ),

                    state =
                        listState,

                    verticalArrangement =
                        Arrangement.spacedBy(
                            7.dp
                        )
                ) {
                    itemsIndexed(
                        options
                    ) {
                            _,
                            option ->

                        val selected =
                            option ==
                                currentValue

                        Surface(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .bouncyClickable {
                                        onSelected(
                                            option
                                        )
                                    },

                            shape =
                                RoundedCornerShape(
                                    15.dp
                                ),

                            color =
                                if (selected) {
                                    MaterialTheme
                                        .colorScheme
                                        .primaryContainer
                                }
                                else {
                                    MaterialTheme
                                        .colorScheme
                                        .surfaceVariant
                                        .copy(
                                            alpha =
                                                0.44f
                                        )
                                },

                            border =
                                BorderStroke(
                                    width =
                                        1.dp,

                                    color =
                                        if (selected) {
                                            MaterialTheme
                                                .colorScheme
                                                .primary
                                        }
                                        else {
                                            MaterialTheme
                                                .colorScheme
                                                .outline
                                                .copy(
                                                    alpha =
                                                        0.18f
                                                )
                                        }
                                )
                        ) {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal =
                                                14.dp,

                                            vertical =
                                                12.dp
                                        ),

                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {
                                Text(
                                    text =
                                        option,

                                    modifier =
                                        Modifier.weight(
                                            1f
                                        ),

                                    fontWeight =
                                        if (selected) {
                                            FontWeight.Black
                                        }
                                        else {
                                            FontWeight.Medium
                                        }
                                )

                                if (selected) {
                                    Icon(
                                        imageVector =
                                            Icons.Rounded
                                                .Check,

                                        contentDescription =
                                            null,

                                        tint =
                                            MaterialTheme
                                                .colorScheme
                                                .primary
                                    )
                                }
                            }
                        }
                    }
                }

                TextButton(
                    onClick =
                        onDismiss,

                    modifier =
                        Modifier.align(
                            Alignment.End
                        )
                ) {
                    Text(
                        "Đóng"
                    )
                }
            }
        }
    }
}


@Composable
private fun TagYearPickerDialog(
    currentValue: String,
    onSelected: (String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val currentYear =
        remember {
            java.util.Calendar
                .getInstance()
                .get(
                    java.util.Calendar.YEAR
                )
        }

    val oldestYear =
        currentYear -
            199

    val years =
        remember(
            currentYear
        ) {
            (
                currentYear downTo
                    oldestYear
                )
                .toList()
        }

    val rows =
        remember(
            years
        ) {
            years.chunked(
                4
            )
        }

    val selectedYear =
        currentValue
            .trim()
            .toIntOrNull()

    val initialRow =
        selectedYear
            ?.takeIf {
                it in
                    oldestYear..currentYear
            }
            ?.let {
                (
                    currentYear -
                        it
                    ) /
                    4
            }
            ?: 0

    val listState =
        rememberLazyListState(
            initialFirstVisibleItemIndex =
                initialRow
                    .coerceIn(
                        0,
                        (
                            rows.size -
                                1
                            )
                            .coerceAtLeast(
                                0
                            )
                    )
        )

    Dialog(
        onDismissRequest =
            onDismiss
    ) {
        Surface(
            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(
                    28.dp
                ),

            color =
                MaterialTheme
                    .colorScheme
                    .surface,

            border =
                BorderStroke(
                    width =
                        1.dp,

                    color =
                        BrandViolet
                            .copy(
                                alpha =
                                    0.32f
                            )
                ),

            tonalElevation =
                14.dp,

            shadowElevation =
                20.dp
        ) {
            Column(
                modifier =
                    Modifier.padding(
                        16.dp
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Column(
                        modifier =
                            Modifier.weight(
                                1f
                            )
                    ) {
                        Text(
                            text =
                                "Chọn năm",

                            style =
                                MaterialTheme
                                    .typography
                                    .titleLarge,

                            fontWeight =
                                FontWeight.Black
                        )

                        Text(
                            text =
                                "$oldestYear — $currentYear · 200 năm",

                            style =
                                MaterialTheme
                                    .typography
                                    .labelSmall,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }

                    TextButton(
                        onClick =
                            onClear
                    ) {
                        Text(
                            "Không rõ"
                        )
                    }
                }

                HorizontalDivider(
                    color =
                        MaterialTheme
                            .colorScheme
                            .outline
                            .copy(
                                alpha =
                                    0.16f
                            )
                )

                LazyColumn(
                    modifier =
                        Modifier.heightIn(
                            max =
                                390.dp
                        ),

                    state =
                        listState,

                    verticalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        )
                ) {
                    itemsIndexed(
                        rows
                    ) {
                            _,
                            rowYears ->

                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    8.dp
                                )
                        ) {
                            rowYears.forEach {
                                    year ->

                                YearCell(
                                    year =
                                        year,

                                    selected =
                                        year ==
                                            selectedYear,

                                    modifier =
                                        Modifier.weight(
                                            1f
                                        ),

                                    onClick = {
                                        onSelected(
                                            year.toString()
                                        )
                                    }
                                )
                            }

                            repeat(
                                4 -
                                    rowYears.size
                            ) {
                                Spacer(
                                    modifier =
                                        Modifier.weight(
                                            1f
                                        )
                                )
                            }
                        }
                    }
                }

                Text(
                    text =
                        "Chỉ chọn năm phát hành, không có tháng hoặc ngày.",

                    modifier =
                        Modifier.fillMaxWidth(),

                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }
        }
    }
}


@Composable
private fun YearCell(
    year: Int,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val colors =
        MaterialTheme.colorScheme

    Surface(
        modifier =
            modifier
                .height(
                    43.dp
                )
                .bouncyClickable(
                    pressedScale =
                        0.94f,

                    onClick =
                        onClick
                ),

        shape =
            RoundedCornerShape(
                14.dp
            ),

        color =
            if (selected) {
                BrandViolet
                    .copy(
                        alpha =
                            0.58f
                    )
            }
            else {
                colors
                    .surfaceVariant
                    .copy(
                        alpha =
                            0.54f
                    )
            },

        border =
            BorderStroke(
                width =
                    if (selected) {
                        1.5.dp
                    }
                    else {
                        1.dp
                    },

                color =
                    if (selected) {
                        BrandSky
                    }
                    else {
                        colors
                            .outline
                            .copy(
                                alpha =
                                    0.17f
                            )
                    }
            ),

        tonalElevation =
            if (selected) {
                6.dp
            }
            else {
                1.dp
            }
    ) {
        Box(
            modifier =
                Modifier.fillMaxSize(),

            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    year.toString(),

                style =
                    MaterialTheme
                        .typography
                        .labelLarge,

                fontWeight =
                    if (selected) {
                        FontWeight.Black
                    }
                    else {
                        FontWeight.Medium
                    },

                color =
                    if (selected) {
                        Color.White
                    }
                    else {
                        colors
                            .onSurface
                    }
            )
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
