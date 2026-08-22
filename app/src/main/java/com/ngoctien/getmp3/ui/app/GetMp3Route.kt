package com.ngoctien.getmp3.ui.app

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import com.ngoctien.getmp3.ui.AppDestination
import com.ngoctien.getmp3.ui.GetMp3Screen
import com.ngoctien.getmp3.viewmodel.CompareViewModel
import com.ngoctien.getmp3.viewmodel.DownloadViewModel
import com.ngoctien.getmp3.viewmodel.LibraryViewModel
import com.ngoctien.getmp3.viewmodel.LyricsViewModel
import com.ngoctien.getmp3.viewmodel.MetadataRepairViewModel
import com.ngoctien.getmp3.viewmodel.SettingsViewModel
import com.ngoctien.getmp3.viewmodel.TagEditorViewModel
import com.ngoctien.getmp3.viewmodel.YouTubeSearchViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

/*
 * Application coordinator.
 *
 * Đây là nơi duy nhất kết nối:
 *
 * ViewModel
 *    ↓
 * destination coordinator
 *    ↓
 * pure GetMp3Screen
 *
 * Skin không biết ViewModel.
 * Core không biết UI.
 */
@Composable
internal fun GetMp3Route(
    downloadViewModel: DownloadViewModel,
    tagEditorViewModel: TagEditorViewModel,
    compareViewModel: CompareViewModel,
    metadataRepairViewModel: MetadataRepairViewModel,
    settingsViewModel: SettingsViewModel,
    libraryViewModel: LibraryViewModel,
    lyricsViewModel: LyricsViewModel,
    youtubeSearchViewModel: YouTubeSearchViewModel,
    onRequestNotificationPermission: () -> Unit,
    onChooseInboxFolder: () -> Unit,
    onChooseLibraryFolder: () -> Unit
) {
    val liveSettings by
        settingsViewModel
            .uiState
            .collectAsStateWithLifecycle()

    val clipboardManager =
        LocalClipboardManager.current

    val activity =
        LocalContext.current
            .findActivity()

    val scope =
        rememberCoroutineScope()

    val liveUiStyle =
        liveSettings
            .uiStyle
            .toUiStyle()

    val snackbarHostState =
        remember {
            SnackbarHostState()
        }

    var selectedTabIndex by
        rememberSaveable {
            mutableIntStateOf(
                AppDestination
                    .HOME
                    .ordinal
            )
        }

    var destinationHistory by
        rememberSaveable {
            mutableStateOf(
                intArrayOf()
            )
        }

    var lastHomeBackAt by
        remember {
            mutableLongStateOf(
                0L
            )
        }

    fun navigateTo(
        destination: AppDestination
    ) {
        val targetIndex =
            destination.ordinal

        if (
            targetIndex ==
            selectedTabIndex
        ) {
            return
        }

        lastHomeBackAt =
            0L

        if (
            destination ==
            AppDestination.HOME
        ) {
            destinationHistory =
                intArrayOf()
        }
        else {
            destinationHistory =
                destinationHistory +
                    selectedTabIndex
        }

        selectedTabIndex =
            targetIndex
    }

    fun navigateBack() {
        val previousIndex =
            destinationHistory
                .lastOrNull()

        if (previousIndex != null) {
            destinationHistory =
                destinationHistory
                    .copyOf(
                        destinationHistory.size -
                            1
                    )

            selectedTabIndex =
                previousIndex

            lastHomeBackAt =
                0L

            return
        }

        if (
            selectedTabIndex !=
            AppDestination
                .HOME
                .ordinal
        ) {
            selectedTabIndex =
                AppDestination
                    .HOME
                    .ordinal

            lastHomeBackAt =
                0L

            return
        }

        val now =
            SystemClock
                .elapsedRealtime()

        if (
            now -
                lastHomeBackAt <=
            2_200L
        ) {
            activity?.finish()

            return
        }

        lastHomeBackAt =
            now

        scope.launch {
            snackbarHostState
                .currentSnackbarData
                ?.dismiss()

            snackbarHostState
                .showSnackbar(
                    message =
                        "Vuốt thêm lần nữa để thoát",
                    duration =
                        SnackbarDuration.Short
                )
        }
    }

    fun quickDownloadFromClipboard() {
        val clipboardText =
            clipboardManager
                .getText()
                ?.text

        navigateTo(
            AppDestination.DOWNLOAD
        )

        if (!clipboardText.isNullOrBlank()) {
            onRequestNotificationPermission()
        }

        downloadViewModel
            .pasteAndDownload(
                clipboardText =
                    clipboardText
            )

        youtubeSearchViewModel
            .showQueue()
    }

    /*
     * Hành vi khi bấm navigation dock.
     *
     * Giữ nguyên behaviour cũ:
     * - mở Tag -> ensureLoaded
     * - mở Compare -> ensure cả duplicate + repair
     */
    fun selectFromDock(
        destination: AppDestination
    ) {
        when (destination) {

            AppDestination.EDIT_TAG -> {
                tagEditorViewModel
                    .ensureLoaded()
            }

            AppDestination.COMPARE -> {
                compareViewModel
                    .ensureLoaded()

                metadataRepairViewModel
                    .ensureLoaded()
            }

            else ->
                Unit
        }

        navigateTo(
            destination
        )
    }

    GetMp3EventEffects(
        downloadViewModel =
            downloadViewModel,

        tagEditorViewModel =
            tagEditorViewModel,

        compareViewModel =
            compareViewModel,

        metadataRepairViewModel =
            metadataRepairViewModel,

        snackbarHostState =
            snackbarHostState
    )

    val selectedDestination =
        AppDestination
            .entries[
                selectedTabIndex
            ]

    BackHandler(
        enabled =
            true,
        onBack =
            ::navigateBack
    )

    GetMp3Screen(
        uiStyle = liveUiStyle,

        selectedDestination =
            selectedDestination,

        snackbarHostState =
            snackbarHostState,

        onDestinationSelected =
            ::selectFromDock
    ) { destination, modifier ->

        when (destination) {

            AppDestination.HOME -> {

                HomeDestination(
                    downloadViewModel =
                        downloadViewModel,

                    tagEditorViewModel =
                        tagEditorViewModel,

                    compareViewModel =
                        compareViewModel,

                    metadataRepairViewModel =
                        metadataRepairViewModel,

                    settingsViewModel =
                        settingsViewModel,

                    uiStyle =
                        liveUiStyle,

                    modifier =
                        modifier,

                    onQuickDownloadFromClipboard =
                        ::quickDownloadFromClipboard,

                    onNavigate =
                        ::navigateTo
                )
            }

            AppDestination.DOWNLOAD -> {

                SearchDownloadDestination(
                    downloadViewModel =
                        downloadViewModel,

                    tagEditorViewModel =
                        tagEditorViewModel,

                    settingsViewModel =
                        settingsViewModel,

                    youtubeSearchViewModel =
                        youtubeSearchViewModel,

                    uiStyle = liveUiStyle,

                    modifier =
                        modifier,

                    onRequestNotificationPermission =
                        onRequestNotificationPermission,

                    onNavigate =
                        ::navigateTo
                )
            }

            AppDestination.LIBRARY -> {

                LibraryDestination(
                    libraryViewModel =
                        libraryViewModel,

                    settingsViewModel =
                        settingsViewModel,

                    tagEditorViewModel =
                        tagEditorViewModel,

                    lyricsViewModel =
                        lyricsViewModel,

                    youtubeSearchViewModel =
                        youtubeSearchViewModel,

                    modifier =
                        modifier,

                    onNavigate =
                        ::navigateTo
                )
            }

            AppDestination.LYRICS -> {

                LyricsDestination(
                    viewModel =
                        lyricsViewModel,

                    modifier =
                        modifier
                )
            }

            AppDestination.EDIT_TAG -> {

                TagEditorDestination(
                    tagEditorViewModel =
                        tagEditorViewModel,

                    lyricsViewModel =
                        lyricsViewModel,

                    uiStyle = liveUiStyle,

                    modifier =
                        modifier,

                    onNavigate =
                        ::navigateTo
                )
            }

            AppDestination.COMPARE -> {

                CompareDestination(
                    compareViewModel =
                        compareViewModel,

                    metadataRepairViewModel =
                        metadataRepairViewModel,

                    youtubeSearchViewModel =
                        youtubeSearchViewModel,

                    modifier =
                        modifier,

                    onNavigate =
                        ::navigateTo
                )
            }

            AppDestination.SETTINGS -> {

                SettingsDestination(
                    settingsViewModel =
                        settingsViewModel,

                    downloadViewModel =
                        downloadViewModel,

                    tagEditorViewModel =
                        tagEditorViewModel,

                    uiStyle = liveUiStyle,

                    modifier =
                        modifier,

                    onChooseInboxFolder =
                        onChooseInboxFolder,

                    onChooseLibraryFolder =
                        onChooseLibraryFolder
                )
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var currentContext =
        this

    while (
        currentContext is
        ContextWrapper
    ) {
        if (
            currentContext is
            Activity
        ) {
            return currentContext
        }

        currentContext =
            currentContext
                .baseContext
    }

    return currentContext as? Activity
}
