package com.ngoctien.getmp3.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.ngoctien.getmp3.ui.theme.BrandBlue
import com.ngoctien.getmp3.ui.theme.BrandCyan
import com.ngoctien.getmp3.ui.theme.BrandPink
import com.ngoctien.getmp3.ui.theme.BrandSky
import com.ngoctien.getmp3.ui.theme.BrandViolet
import com.ngoctien.getmp3.viewmodel.ArtistCaseMode
import com.ngoctien.getmp3.viewmodel.CompareIndexUiState
import com.ngoctien.getmp3.viewmodel.DownloadScreenUiState
import com.ngoctien.getmp3.viewmodel.DownloadViewModel
import com.ngoctien.getmp3.viewmodel.FfmpegReadyState
import com.ngoctien.getmp3.viewmodel.SettingsViewModel
import com.ngoctien.getmp3.viewmodel.SongNoteViewModel
import com.ngoctien.getmp3.viewmodel.TagEditorUiState
import com.ngoctien.getmp3.viewmodel.TagEditorViewModel
import kotlinx.coroutines.launch
import java.io.File

private enum class MainTab(
    val title: String,
    val icon: ImageVector
) {
    DOWNLOAD(
        title = "Tải",
        icon = Icons.Rounded.Download
    ),

    WAITING(
        title = "Chờ tải",
        icon = Icons.Rounded.List
    ),
    EDIT_TAG(
        title = "Sửa thẻ",
        icon = Icons.Rounded.Edit
    ),

    SETTINGS(
        title = "Cài đặt",
        icon = Icons.Rounded.Settings
    )
}

@Composable
fun GetMp3Screen(
    downloadViewModel: DownloadViewModel,
    tagEditorViewModel: TagEditorViewModel,
    settingsViewModel: SettingsViewModel,
    songNoteViewModel: SongNoteViewModel,
    onRequestNotificationPermission: () -> Unit,
    onChooseDownloadFolder: () -> Unit,
    onChooseCompareFolder: () -> Unit
) {
    val downloadState by
        downloadViewModel.uiState
            .collectAsStateWithLifecycle()

    val tagState by
        tagEditorViewModel.uiState
            .collectAsStateWithLifecycle()

    val settings by
        settingsViewModel.uiState
            .collectAsStateWithLifecycle()

    val compareIndexState by
        settingsViewModel
            .compareIndexState
            .collectAsStateWithLifecycle()

    val snackbarHostState =
        remember {
            SnackbarHostState()
        }

    
    val uiScope = rememberCoroutineScope()
var selectedTabIndex by
        rememberSaveable {
            mutableIntStateOf(0)
        }

    LaunchedEffect(downloadViewModel) {
        downloadViewModel.events.collect {
            snackbarHostState.showSnackbar(
                it.message
            )
        }
    }

    LaunchedEffect(tagEditorViewModel) {
        tagEditorViewModel.events.collect {
            snackbarHostState.showSnackbar(
                it.message
            )
        }
    }

    Scaffold(
        containerColor =
            MaterialTheme
                .colorScheme
                .background,

        snackbarHost = {
            SnackbarHost(
                hostState =
                    snackbarHostState
            )
        },

        bottomBar = {
            FloatingBottomTabs(
                selectedIndex =
                    selectedTabIndex,

                onSelected = { index ->
                    selectedTabIndex = index

                    if (
                        MainTab.entries[index] ==
                        MainTab.EDIT_TAG
                    ) {
                        tagEditorViewModel
                            .refresh()
                    }
                }
            )
        }
    ) { innerPadding ->
        when (
            MainTab.entries[
                selectedTabIndex
            ]
        ) {
            MainTab.DOWNLOAD -> {
                DownloadTab(
                    state =
                        downloadState,

                    settings =
                        settings,

                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),

                    onPasteAndDownload = {
                        onRequestNotificationPermission()

                        downloadViewModel
                            .pasteAndDownload(it)
                    },

                    onCancel =
                        downloadViewModel::cancelJob,

                    onRetry =
                        downloadViewModel::retryJob,

                    onClearHistory =
                        downloadViewModel::clearHistory,

                    onRetryFfmpeg =
                        downloadViewModel::checkFfmpeg
                )
            }

            MainTab.WAITING -> {
                SongNotesTab(
                    viewModel = songNoteViewModel,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),

                    onPasteAndDownload = { clipboardText ->
                        onRequestNotificationPermission()

                        downloadViewModel
                            .pasteAndDownload(
                                clipboardText
                            )
                    }
                )
            }
            MainTab.EDIT_TAG -> {
                TagEditorTab(
                    state = tagState,

                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),

                    onTitleChange =
                        tagEditorViewModel::setTitle,

                    onClearTitle =
                        tagEditorViewModel::clearTitle,

                    onArtistChange =
                        tagEditorViewModel::setArtist,

                    onClearArtist =
                        tagEditorViewModel::clearArtist,

                    onArtistCaseModeChange =
                        tagEditorViewModel::
                            setArtistCaseMode,

                    onAlbumChange =
                        tagEditorViewModel::setAlbum,

                    onQuickFormat = {
                        val compareIndexMissing =
                            settings.indexedArtists.isEmpty() ||
                                settings.compareIndexSourceUri !=
                                settings.compareTreeUri

                        when {
                            !settings.hasCompareFolder -> {
                                selectedTabIndex =
                                    MainTab.SETTINGS.ordinal

                                uiScope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Hãy chọn thư mục đối chiếu trước khi Format nhanh"
                                    )
                                }
                            }

                            compareIndexMissing -> {
                                selectedTabIndex =
                                    MainTab.SETTINGS.ordinal

                                settingsViewModel
                                    .rebuildCompareIndex()

                                uiScope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Chưa có dữ liệu ca sĩ. App đang mở Cài đặt để quét thư mục đối chiếu."
                                    )
                                }
                            }

                            else -> {
                                tagEditorViewModel
                                    .quickFormat()
                            }
                        }
                    },
onSelectArtist =
                        tagEditorViewModel::
                            selectArtistCandidate,

                    onDismissArtists =
                        tagEditorViewModel::
                            dismissArtistCandidates,

                    onSelectFile =
                        tagEditorViewModel::selectFile,

                    onSkip =
                        tagEditorViewModel::skip,

                    onSave =
                        tagEditorViewModel::saveAndNext,

                    onDelete =
                        tagEditorViewModel::deleteCurrentSong
                )
            }

            MainTab.SETTINGS -> {
                SettingsTab(
                    settings = settings,

                    compareIndexState =
                        compareIndexState,

                    downloadState =
                        downloadState,

                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),

                    onBitrateChange =
                        settingsViewModel::setBitrate,

                    onThemeChange =
                        settingsViewModel::setThemeMode,

                    onChooseDownloadFolder =
                        onChooseDownloadFolder,

                    onUseDefaultFolder = {
                        settingsViewModel
                            .useDefaultFolder()

                        tagEditorViewModel
                            .refresh()
                    },

                    onChooseCompareFolder =
                        onChooseCompareFolder,

                    onClearCompareFolder =
                        settingsViewModel::
                            clearCompareFolder,

                    onRebuildCompareIndex =
                        settingsViewModel::
                            rebuildCompareIndex,

                    onSaveFilters =
                        settingsViewModel::
                            setTitleFilters,

                    onRetryFfmpeg =
                        downloadViewModel::checkFfmpeg
                )
            }
        }
    }
}

@Composable
private fun FloatingBottomTabs(
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                horizontal = 18.dp,
                vertical = 10.dp
            )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 18.dp,
                    shape =
                        RoundedCornerShape(28.dp)
                ),

            shape =
                RoundedCornerShape(28.dp),

            color =
                MaterialTheme
                    .colorScheme
                    .surface
                    .copy(alpha = 0.94f),

            border =
                BorderStroke(
                    1.dp,
                    MaterialTheme
                        .colorScheme
                        .outline
                        .copy(alpha = 0.3f)
                )
        ) {
            Row(
                modifier =
                    Modifier.padding(7.dp)
            ) {
                MainTab.entries
                    .forEachIndexed {
                            index,
                            tab ->

                        val selected =
                            selectedIndex == index

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(
                                    RoundedCornerShape(
                                        21.dp
                                    )
                                )
                                .background(
                                    if (selected) {
                                        Brush.linearGradient(
                                            listOf(
                                                BrandBlue,
                                                BrandViolet
                                            )
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            listOf(
                                                Color.Transparent,
                                                Color.Transparent
                                            )
                                        )
                                    }
                                )
                                .clickable {
                                    onSelected(index)
                                }
                                .padding(
                                    vertical = 11.dp
                                ),

                            horizontalArrangement =
                                Arrangement.Center,

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector =
                                    tab.icon,

                                contentDescription =
                                    tab.title,

                                tint = if (selected) {
                                    Color.White
                                } else {
                                    MaterialTheme
                                        .colorScheme
                                        .onSurfaceVariant
                                }
                            )

                            if (selected) {
                                Spacer(
                                    modifier =
                                        Modifier.width(7.dp)
                                )

                                Text(
                                    text = tab.title,
                                    color = Color.White,
                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        }
                    }
            }
        }
    }
}

@Composable
private fun DownloadTab(
    state: DownloadScreenUiState,
    settings: AppSettings,
    modifier: Modifier,

    onPasteAndDownload:
        (CharSequence?) -> Unit,

    onCancel: (String) -> Unit,
    onRetry: (DownloadJobEntity) -> Unit,
    onClearHistory: () -> Unit,
    onRetryFfmpeg: () -> Unit
) {
    val clipboard =
        LocalClipboardManager.current

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier
            .statusBarsPadding(),

        contentPadding =
            PaddingValues(
                start = 20.dp,
                top = 18.dp,
                end = 20.dp,
                bottom = 26.dp
            ),

        verticalArrangement =
            Arrangement.spacedBy(18.dp)
    ) {
        item {
            AppLogoHeader(
                title = "GetMP3",
                subtitle =
                    "MP3 • ${settings.bitrateKbps} kbps",

                icon =
                    Icons.Rounded.GraphicEq
            )
        }

        item {
            GradientActionButton(
                loading =
                    state.isPreparingDownload,

                enabled =
                    state.ffmpegState ==
                        FfmpegReadyState.READY,

                loadingText =
                    state.preparingMessage,

                onClick = {
                    onPasteAndDownload(
                        clipboard.getText()
                            ?.text
                    )
                }
            )
        }

        if (
            state.ffmpegState ==
            FfmpegReadyState.FAILED
        ) {
            item {
                ErrorNotice(
                    text =
                        state.ffmpegMessage,

                    actionText =
                        "Thử lại",

                    onAction =
                        onRetryFfmpeg
                )
            }
        }

        if (state.activeJobs.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Đang tải",
                    count =
                        state.activeJobs.size
                )
            }

            items(
                count =
                    state.activeJobs.size,

                key = {
                    state.activeJobs[it].id
                }
            ) { index ->
                ActiveDownloadItem(
                    job =
                        state.activeJobs[index],

                    onCancel = {
                        onCancel(
                            state.activeJobs[
                                index
                            ].id
                        )
                    }
                )
            }
        }

        if (state.recentJobs.isNotEmpty()) {
            item {
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    SectionHeader(
                        title = "Gần đây",
                        count =
                            state.recentJobs.size
                    )

                    Spacer(
                        modifier =
                            Modifier.weight(1f)
                    )

                    TextButton(
                        onClick =
                            onClearHistory
                    ) {
                        Icon(
                            imageVector =
                                Icons.Rounded
                                    .DeleteSweep,

                            contentDescription =
                                null
                        )

                        Spacer(
                            modifier =
                                Modifier.width(4.dp)
                        )

                        Text("Xóa")
                    }
                }
            }

            items(
                count =
                    minOf(
                        state.recentJobs.size,
                        12
                    ),

                key = {
                    state.recentJobs[it].id
                }
            ) { index ->
                RecentDownloadItem(
                    job =
                        state.recentJobs[index],

                    onRetry = {
                        onRetry(
                            state.recentJobs[
                                index
                            ]
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun TagEditorTab(
    state: TagEditorUiState,
    modifier: Modifier,

    onTitleChange: (String) -> Unit,
    onClearTitle: () -> Unit,

    onArtistChange: (String) -> Unit,
    onClearArtist: () -> Unit,

    onArtistCaseModeChange:
        (ArtistCaseMode) -> Unit,

    onAlbumChange: (String) -> Unit,
    onQuickFormat: () -> Unit,

    onSelectArtist: (String) -> Unit,
    onDismissArtists: () -> Unit,

    onSelectFile: (Int) -> Unit,
    onSkip: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
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
            .statusBarsPadding(),

        contentPadding =
            PaddingValues(
                start = 20.dp,
                top = 18.dp,
                end = 20.dp,
                bottom = 28.dp
            ),

        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                GradientIcon(
                    icon =
                        Icons.Rounded.Tag
                )

                Spacer(
                    modifier =
                        Modifier.width(13.dp)
                )

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {
                    Text(
                        text = "Sửa thẻ",

                        style =
                            MaterialTheme
                                .typography
                                .headlineMedium,

                        fontWeight =
                            FontWeight.ExtraBold
                    )

                    Text(
                        text =
                            if (
                                state.totalFiles > 0
                            ) {
                                "${state.displayIndex} / ${state.totalFiles}"
                            } else {
                                "Không có file"
                            },

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        showFileList = true
                    },

                    enabled =
                        state.files.isNotEmpty() &&
                            !state.isDeleting
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.List,

                        contentDescription =
                            "Danh sách file"
                    )
                }
            }
        }

        if (
            state.isScanning ||
            state.isLoadingSong
        ) {
            item {
                LoadingPanel(
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
                EmptyPanel(
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

                        onQuickFormat =
                            onQuickFormat
                    )
                }

                state.errorMessage?.let {
                    item {
                        ErrorNotice(
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
                        OutlinedButton(
                            onClick = {
                                showDeleteConfirmation =
                                    true
                            },

                            enabled =
                                !state.isSaving &&
                                    !state.isDeleting,

                            modifier =
                                Modifier.height(
                                    52.dp
                                ),

                            contentPadding =
                                PaddingValues(
                                    horizontal =
                                        11.dp
                                ),

                            colors =
                                ButtonDefaults
                                    .outlinedButtonColors(
                                        contentColor =
                                            MaterialTheme
                                                .colorScheme
                                                .error
                                    ),

                            border =
                                BorderStroke(
                                    1.dp,

                                    MaterialTheme
                                        .colorScheme
                                        .error
                                        .copy(
                                            alpha = 0.55f
                                        )
                                )
                        ) {
                            if (
                                state.isDeleting
                            ) {
                                CircularProgressIndicator(
                                    modifier =
                                        Modifier.size(
                                            18.dp
                                        ),

                                    strokeWidth =
                                        2.dp
                                )
                            } else {
                                Icon(
                                    imageVector =
                                        Icons.Rounded
                                            .Delete,

                                    contentDescription =
                                        null,

                                    modifier =
                                        Modifier.size(
                                            19.dp
                                        )
                                )

                                Spacer(
                                    modifier =
                                        Modifier.width(
                                            5.dp
                                        )
                                )

                                Text("Xóa")
                            }
                        }

                        OutlinedButton(
                            onClick = onSkip,

                            enabled =
                                !state.isSaving &&
                                    !state.isDeleting,

                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),

                            contentPadding =
                                PaddingValues(
                                    horizontal =
                                        10.dp
                                )
                        ) {
                            Text("Bỏ qua")
                        }

                        Button(
                            onClick = onSave,

                            enabled =
                                !state.isSaving &&
                                    !state.isDeleting,

                            modifier = Modifier
                                .weight(1.35f)
                                .height(52.dp),

                            contentPadding =
                                PaddingValues(
                                    horizontal =
                                        10.dp
                                )
                        ) {
                            if (
                                state.isSaving &&
                                !state.isDeleting
                            ) {
                                CircularProgressIndicator(
                                    modifier =
                                        Modifier.size(
                                            19.dp
                                        ),

                                    strokeWidth =
                                        2.dp
                                )
                            } else {
                                Icon(
                                    imageVector =
                                        Icons.Rounded.Save,

                                    contentDescription =
                                        null,

                                    modifier =
                                        Modifier.size(
                                            19.dp
                                        )
                                )
                            }

                            Spacer(
                                modifier =
                                    Modifier.width(6.dp)
                            )

                            Text("Lưu & tiếp")
                        }
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
    onQuickFormat: () -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(24.dp),

        color =
            MaterialTheme
                .colorScheme
                .surface,

        border =
            BorderStroke(
                1.dp,
                MaterialTheme
                    .colorScheme
                    .outline
                    .copy(alpha = 0.3f)
            )
    ) {
        Column(
            modifier =
                Modifier.padding(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = state.title,

                onValueChange =
                    onTitleChange,

                modifier =
                    Modifier.fillMaxWidth(),

                label = {
                    Text("Title")
                },

                singleLine = true,

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

            OutlinedTextField(
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
            }

            OutlinedButton(
                onClick =
                    onQuickFormat,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
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
        }
    }
}

@Composable
private fun SongPreviewCard(
    fileName: String,
    album: String,
    coverPath: String?
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(24.dp),

        color =
            MaterialTheme
                .colorScheme
                .surface,

        border =
            BorderStroke(
                1.dp,
                MaterialTheme
                    .colorScheme
                    .outline
                    .copy(alpha = 0.3f)
            )
    ) {
        Row(
            modifier =
                Modifier.padding(16.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            CoverImage(
                model =
                    coverPath?.let(::File),

                size = 88
            )

            Spacer(
                modifier =
                    Modifier.width(15.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {
                Text(
                    text = fileName,

                    maxLines = 4,

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
                        Modifier.height(8.dp)
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
                                horizontal = 10.dp,
                                vertical = 4.dp
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
                        "Kết quả được tìm từ thư mục đối chiếu.",

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
                                .clickable {
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

@Composable
private fun SettingsTab(
    settings: AppSettings,
    compareIndexState: CompareIndexUiState,
    downloadState: DownloadScreenUiState,
    modifier: Modifier,

    onBitrateChange: (Int) -> Unit,
    onThemeChange: (AppThemeMode) -> Unit,

    onChooseDownloadFolder: () -> Unit,
    onUseDefaultFolder: () -> Unit,

    onChooseCompareFolder: () -> Unit,
    onClearCompareFolder: () -> Unit,
    onRebuildCompareIndex: () -> Unit,

    onSaveFilters:
        (List<String>, String) -> Unit,

    onRetryFfmpeg: () -> Unit
) {
    var filterTermsText by
        remember(
            settings.titleFilterTerms
        ) {
            mutableStateOf(
                settings
                    .titleFilterTerms
                    .joinToString("\n")
            )
        }

    var filterSymbolsText by
        remember(
            settings.titleFilterSymbols
        ) {
            mutableStateOf(
                settings
                    .titleFilterSymbols
            )
        }

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier
            .statusBarsPadding(),

        contentPadding =
            PaddingValues(
                start = 20.dp,
                top = 18.dp,
                end = 20.dp,
                bottom = 28.dp
            ),

        verticalArrangement =
            Arrangement.spacedBy(18.dp)
    ) {
        item {
            AppLogoHeader(
                title = "Cài đặt",
                subtitle =
                    "Tùy chỉnh GetMP3",

                icon =
                    Icons.Rounded.Settings
            )
        }

        item {
            SettingsCard {
                AnimatedSelectorField(
                    label =
                        "Chất lượng MP3",

                    value =
                        "${settings.bitrateKbps} kbps",

                    options =
                        AppSettings
                            .SUPPORTED_BITRATES
                            .map {
                                "$it kbps"
                            },

                    onSelected = {
                        it.substringBefore(" ")
                            .toIntOrNull()
                            ?.let(
                                onBitrateChange
                            )
                    }
                )

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                FolderPickerField(
                    label =
                        "Thư mục lưu",

                    value =
                        settings
                            .downloadFolderName,

                    onClick =
                        onChooseDownloadFolder
                )

                if (settings.usesCustomFolder) {
                    TextButton(
                        onClick =
                            onUseDefaultFolder
                    ) {
                        Text(
                            "Dùng lại Music/GetMP3"
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                AnimatedSelectorField(
                    label = "Giao diện",

                    value =
                        when (
                            settings.themeMode
                        ) {
                            AppThemeMode.SYSTEM ->
                                "Theo hệ thống"

                            AppThemeMode.LIGHT ->
                                "Sáng"

                            AppThemeMode.DARK ->
                                "Tối"
                        },

                    options =
                        listOf(
                            "Theo hệ thống",
                            "Sáng",
                            "Tối"
                        ),

                    onSelected = {
                        onThemeChange(
                            when (it) {
                                "Sáng" ->
                                    AppThemeMode
                                        .LIGHT

                                "Tối" ->
                                    AppThemeMode
                                        .DARK

                                else ->
                                    AppThemeMode
                                        .SYSTEM
                            }
                        )
                    }
                )
            }
        }

        item {
            SettingsCard {
                Text(
                    text =
                        "Dữ liệu đối chiếu",

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,

                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )

                Text(
                    text =
                        "Chỉ quét các file MP3 nằm trực tiếp trong thư mục. Không đọc thư mục con.",

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )

                FolderPickerField(
                    label =
                        "Thư mục đối chiếu",

                    value =
                        settings
                            .compareFolderName,

                    onClick =
                        onChooseCompareFolder
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        )
                ) {
                    OutlinedButton(
                        onClick =
                            onRebuildCompareIndex,

                        enabled =
                            settings
                                .hasCompareFolder &&
                                !compareIndexState
                                    .isScanning,

                        modifier =
                            Modifier.weight(1f)
                    ) {
                        if (
                            compareIndexState
                                .isScanning
                        ) {
                            CircularProgressIndicator(
                                modifier =
                                    Modifier.size(
                                        18.dp
                                    ),

                                strokeWidth =
                                    2.dp
                            )
                        } else {
                            Icon(
                                imageVector =
                                    Icons.Rounded.Sync,

                                contentDescription =
                                    null
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.width(7.dp)
                        )

                        Text("Quét lại")
                    }

                    if (
                        settings
                            .hasCompareFolder
                    ) {
                        TextButton(
                            onClick =
                                onClearCompareFolder
                        ) {
                            Text("Bỏ")
                        }
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                Text(
                    text =
                        compareIndexState
                            .errorMessage
                            ?: compareIndexState
                                .message
                            ?: (
                                "${settings.indexedArtists.size} ca sĩ • " +
                                    "${settings.indexedAlbums.size} album"
                                ),

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    color =
                        if (
                            compareIndexState
                                .errorMessage != null
                        ) {
                            MaterialTheme
                                .colorScheme
                                .error
                        } else {
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                        }
                )
            }
        }

        item {
            SettingsCard {
                Text(
                    text =
                        "Format nhanh Title",

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,

                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )

                Text(
                    text =
                        "Mỗi dòng hoặc dấu phẩy là một cụm từ cần xóa.",

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value =
                        filterTermsText,

                    onValueChange = {
                        filterTermsText = it
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    label = {
                        Text(
                            "Cụm từ cần loại bỏ"
                        )
                    },

                    minLines = 3,
                    maxLines = 6,

                    placeholder = {
                        Text(
                            "Official Video\nLyrics\nVietsub"
                        )
                    }
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value =
                        filterSymbolsText,

                    onValueChange = {
                        filterSymbolsText = it
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    label = {
                        Text(
                            "Dấu cần loại bỏ"
                        )
                    },

                    singleLine = true,

                    placeholder = {
                        Text("| • ~ _")
                    }
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                Button(
                    onClick = {
                        val terms =
                            filterTermsText
                                .split(
                                    '\n',
                                    ',',
                                    ';'
                                )
                                .map {
                                    it.trim()
                                }
                                .filter {
                                    it.isNotBlank()
                                }

                        onSaveFilters(
                            terms,
                            filterSymbolsText
                        )
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Save,

                        contentDescription =
                            null
                    )

                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )

                    Text("Lưu bộ lọc")
                }
            }
        }

        /*
         * Không hiện dòng FFmpeg "đã sẵn sàng".
         * Chỉ hiện nếu FFmpeg thực sự có lỗi.
         */
        if (
            downloadState.ffmpegState ==
            FfmpegReadyState.FAILED
        ) {
            item {
                ErrorNotice(
                    text =
                        downloadState
                            .ffmpegMessage,

                    actionText =
                        "Thử lại",

                    onAction =
                        onRetryFfmpeg
                )
            }
        }
    }
}

@Composable
private fun AnimatedSelectorField(
    label: String,
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by
        rememberSaveable(
            label
        ) {
            mutableStateOf(false)
        }

    val rotation by
        animateFloatAsState(
            targetValue =
                if (expanded) {
                    180f
                } else {
                    0f
                },

            label =
                "selector_rotation"
        )

    Column {
        Text(
            text = label,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        Spacer(
            modifier =
                Modifier.height(7.dp)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),

            shape =
                RoundedCornerShape(18.dp),

            color =
                MaterialTheme
                    .colorScheme
                    .surfaceVariant,

            border =
                BorderStroke(
                    1.dp,
                    if (expanded) {
                        MaterialTheme
                            .colorScheme
                            .primary
                    } else {
                        MaterialTheme
                            .colorScheme
                            .outline
                            .copy(
                                alpha = 0.22f
                            )
                    }
                )
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expanded =
                                !expanded
                        }
                        .padding(
                            horizontal = 16.dp,
                            vertical = 15.dp
                        ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        text = value,

                        modifier =
                            Modifier.weight(1f),

                        fontWeight =
                            FontWeight.Medium
                    )

                    Icon(
                        imageVector =
                            Icons.Rounded
                                .ExpandMore,

                        contentDescription =
                            null,

                        modifier =
                            Modifier.rotate(
                                rotation
                            )
                    )
                }

                AnimatedVisibility(
                    visible = expanded,

                    enter =
                        expandVertically() +
                            fadeIn(),

                    exit =
                        shrinkVertically() +
                            fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(
                                max = 280.dp
                            )
                            .verticalScroll(
                                rememberScrollState()
                            )
                    ) {
                        HorizontalDivider()

                        options.forEach {
                                option ->

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expanded =
                                            false

                                        onSelected(
                                            option
                                        )
                                    }
                                    .background(
                                        if (
                                            option ==
                                            value
                                        ) {
                                            MaterialTheme
                                                .colorScheme
                                                .primaryContainer
                                        } else {
                                            Color.Transparent
                                        }
                                    )
                                    .padding(
                                        horizontal =
                                            16.dp,

                                        vertical =
                                            14.dp
                                    ),

                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {
                                Text(
                                    text = option,

                                    modifier =
                                        Modifier.weight(
                                            1f
                                        )
                                )

                                if (
                                    option == value
                                ) {
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
            }
        }
    }
}

@Composable
private fun FileListDialog(
    files: List<MediaSongFile>,
    selectedIndex: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    Dialog(
        onDismissRequest =
            onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(
                    max = 560.dp
                ),

            shape =
                RoundedCornerShape(26.dp),

            color =
                MaterialTheme
                    .colorScheme
                    .surface
        ) {
            Column {
                Row(
                    modifier =
                        Modifier.padding(18.dp),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        text =
                            "Danh sách MP3",

                        style =
                            MaterialTheme
                                .typography
                                .titleLarge,

                        fontWeight =
                            FontWeight.Bold,

                        modifier =
                            Modifier.weight(1f)
                    )

                    Text(
                        text =
                            files.size.toString(),

                        color =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }

                HorizontalDivider()

                Column(
                    modifier = Modifier
                        .verticalScroll(
                            rememberScrollState()
                        )
                ) {
                    files.forEachIndexed {
                            index,
                            file ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(index)
                                }
                                .background(
                                    if (
                                        index ==
                                        selectedIndex
                                    ) {
                                        MaterialTheme
                                            .colorScheme
                                            .primaryContainer
                                    } else {
                                        Color.Transparent
                                    }
                                )
                                .padding(
                                    horizontal = 18.dp,
                                    vertical = 14.dp
                                ),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Rounded
                                        .MusicNote,

                                contentDescription =
                                    null
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(12.dp)
                            )

                            Text(
                                text =
                                    file.displayName,

                                maxLines = 2,

                                overflow =
                                    TextOverflow.Ellipsis,

                                modifier =
                                    Modifier.weight(1f)
                            )

                            if (
                                index ==
                                selectedIndex
                            ) {
                                Icon(
                                    imageVector =
                                        Icons.Rounded.Check,

                                    contentDescription =
                                        null
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    content: @Composable () -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(24.dp),

        color =
            MaterialTheme
                .colorScheme
                .surface,

        border =
            BorderStroke(
                1.dp,
                MaterialTheme
                    .colorScheme
                    .outline
                    .copy(alpha = 0.3f)
            )
    ) {
        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun FolderPickerField(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column {
        Text(
            text = label,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        Spacer(
            modifier =
                Modifier.height(7.dp)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onClick
                ),

            shape =
                RoundedCornerShape(18.dp),

            color =
                MaterialTheme
                    .colorScheme
                    .surfaceVariant
        ) {
            Row(
                modifier =
                    Modifier.padding(15.dp),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded
                            .FolderOpen,

                    contentDescription =
                        null
                )

                Spacer(
                    modifier =
                        Modifier.width(11.dp)
                )

                Text(
                    text = value,

                    modifier =
                        Modifier.weight(1f),

                    maxLines = 2,

                    overflow =
                        TextOverflow.Ellipsis,

                    fontWeight =
                        FontWeight.Medium
                )

                Icon(
                    imageVector =
                        Icons.Rounded
                            .OpenInNew,

                    contentDescription =
                        null
                )
            }
        }
    }
}

@Composable
private fun AppLogoHeader(
    title: String,
    subtitle: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(
                    RoundedCornerShape(20.dp)
                )
                .background(
                    Brush.linearGradient(
                        listOf(
                            BrandBlue,
                            BrandSky,
                            BrandCyan,
                            BrandViolet,
                            BrandPink
                        )
                    )
                ),

            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                imageVector = icon,

                contentDescription =
                    null,

                tint = Color.White,

                modifier =
                    Modifier.size(29.dp)
            )
        }

        Spacer(
            modifier =
                Modifier.width(14.dp)
        )

        Column {
            Text(
                text = title,

                style =
                    MaterialTheme
                        .typography
                        .headlineMedium,

                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                text = subtitle,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GradientIcon(
    icon: ImageVector
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(
                RoundedCornerShape(18.dp)
            )
            .background(
                Brush.linearGradient(
                    listOf(
                        BrandBlue,
                        BrandCyan,
                        BrandViolet
                    )
                )
            ),

        contentAlignment =
            Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White
        )
    }
}

@Composable
private fun GradientActionButton(
    loading: Boolean,
    enabled: Boolean,
    loadingText: String?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(
                RoundedCornerShape(24.dp)
            )
            .background(
                Brush.linearGradient(
                    listOf(
                        BrandBlue,
                        BrandSky,
                        BrandViolet
                    )
                )
            )
            .clickable(
                enabled =
                    enabled && !loading,

                onClick = onClick
            ),

        contentAlignment =
            Alignment.Center
    ) {
        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.size(22.dp),

                    color = Color.White,

                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector =
                        Icons.Rounded
                            .ContentPaste,

                    contentDescription =
                        null,

                    tint = Color.White
                )
            }

            Spacer(
                modifier =
                    Modifier.width(10.dp)
            )

            Text(
                text =
                    if (loading) {
                        loadingText
                            ?: "Đang chuẩn bị..."
                    } else {
                        "Dán liên kết & tải"
                    },

                color = Color.White,

                fontWeight =
                    FontWeight.Bold,

                style =
                    MaterialTheme
                        .typography
                        .titleMedium
            )
        }
    }
}

@Composable
private fun ActiveDownloadItem(
    job: DownloadJobEntity,
    onCancel: () -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(22.dp),

        color =
            MaterialTheme
                .colorScheme
                .surface,

        border =
            BorderStroke(
                1.dp,
                MaterialTheme
                    .colorScheme
                    .outline
                    .copy(alpha = 0.3f)
            )
    ) {
        Column(
            modifier =
                Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                CoverImage(
                    model =
                        job.thumbnailUrl,

                    size = 62
                )

                Spacer(
                    modifier =
                        Modifier.width(12.dp)
                )

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {
                    Text(
                        text = job.title,

                        maxLines = 2,

                        overflow =
                            TextOverflow.Ellipsis,

                        fontWeight =
                            FontWeight.SemiBold
                    )

                    Text(
                        text = job.artist,

                        maxLines = 1,

                        overflow =
                            TextOverflow.Ellipsis,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }

                Text(
                    text =
                        "${job.overallProgress.coerceIn(0, 100)}%",

                    color =
                        MaterialTheme
                            .colorScheme
                            .primary,

                    fontWeight =
                        FontWeight.ExtraBold
                )

                IconButton(
                    onClick = onCancel
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded
                                .SkipNext,

                        contentDescription =
                            "Hủy"
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            LinearProgressIndicator(
                progress = {
                    job.overallProgress
                        .coerceIn(0, 100) /
                        100f
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
private fun RecentDownloadItem(
    job: DownloadJobEntity,
    onRetry: () -> Unit
) {
    val context =
        LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 5.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        CoverImage(
            model =
                job.thumbnailUrl,

            size = 54
        )

        Spacer(
            modifier =
                Modifier.width(12.dp)
        )

        Column(
            modifier =
                Modifier.weight(1f)
        ) {
            Text(
                text = job.title,

                maxLines = 1,

                overflow =
                    TextOverflow.Ellipsis,

                fontWeight =
                    FontWeight.SemiBold
            )

            Text(
                text = job.artist,

                maxLines = 1,

                overflow =
                    TextOverflow.Ellipsis,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }

        when (job.status) {
            DownloadStatus.COMPLETED -> {
                IconButton(
                    onClick = {
                        openAudio(
                            context,
                            job.outputUri
                        )
                    }
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded
                                .OpenInNew,

                        contentDescription =
                            "Mở file"
                    )
                }
            }

            DownloadStatus.FAILED,
            DownloadStatus.CANCELLED -> {
                IconButton(
                    onClick = onRetry
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Refresh,

                        contentDescription =
                            "Tải lại"
                    )
                }
            }

            else -> Unit
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int
) {
    Row(
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Text(
            text = title,

            style =
                MaterialTheme
                    .typography
                    .titleLarge,

            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.width(8.dp)
        )

        Surface(
            shape = CircleShape,

            color =
                MaterialTheme
                    .colorScheme
                    .primaryContainer
        ) {
            Text(
                text = count.toString(),

                modifier =
                    Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 3.dp
                    )
            )
        }
    }
}

@Composable
private fun CoverImage(
    model: Any?,
    size: Int
) {
    if (model != null) {
        AsyncImage(
            model = model,

            contentDescription =
                "Ảnh bìa",

            modifier = Modifier
                .size(size.dp)
                .clip(
                    RoundedCornerShape(16.dp)
                ),

            contentScale =
                ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(
                    RoundedCornerShape(16.dp)
                )
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme
                                .colorScheme
                                .primaryContainer,

                            MaterialTheme
                                .colorScheme
                                .secondaryContainer
                        )
                    )
                ),

            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                imageVector =
                    Icons.Rounded
                        .MusicNote,

                contentDescription =
                    null
            )
        }
    }
}

@Composable
private fun LoadingPanel(
    text: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),

        contentAlignment =
            Alignment.Center
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Text(
                text = text,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyPanel(
    text: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),

        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text = text,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorNotice(
    text: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(18.dp),

        color =
            MaterialTheme
                .colorScheme
                .errorContainer
    ) {
        Row(
            modifier =
                Modifier.padding(14.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Icon(
                imageVector =
                    Icons.Rounded
                        .ErrorOutline,

                contentDescription =
                    null
            )

            Spacer(
                modifier =
                    Modifier.width(10.dp)
            )

            Text(
                text = text,

                modifier =
                    Modifier.weight(1f)
            )

            if (
                actionText != null &&
                onAction != null
            ) {
                TextButton(
                    onClick =
                        onAction
                ) {
                    Text(actionText)
                }
            }
        }
    }
}

private fun openAudio(
    context: android.content.Context,
    uriText: String?
) {
    if (uriText.isNullOrBlank()) {
        return
    }

    val intent =
        Intent(
            Intent.ACTION_VIEW
        ).apply {
            setDataAndType(
                Uri.parse(uriText),
                "audio/mpeg"
            )

            addFlags(
                Intent
                    .FLAG_GRANT_READ_URI_PERMISSION
            )
        }

    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // Không có ứng dụng phát MP3.
    }
}