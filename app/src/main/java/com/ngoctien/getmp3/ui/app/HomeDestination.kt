package com.ngoctien.getmp3.ui.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ngoctien.getmp3.settings.AppThemeMode
import com.ngoctien.getmp3.ui.AppDestination
import com.ngoctien.getmp3.ui.BentoHome
import com.ngoctien.getmp3.ui.LibrarySyncProgressScreen
import com.ngoctien.getmp3.ui.design.UiStyle
import com.ngoctien.getmp3.ui.skin.AppSkinContent
import com.ngoctien.getmp3.ui.skin.compact.CompactHome
import com.ngoctien.getmp3.viewmodel.CompareViewModel
import com.ngoctien.getmp3.viewmodel.DownloadViewModel
import com.ngoctien.getmp3.viewmodel.MetadataRepairViewModel
import com.ngoctien.getmp3.viewmodel.SettingsViewModel
import com.ngoctien.getmp3.viewmodel.TagEditorViewModel

@Composable
internal fun HomeDestination(
    downloadViewModel: DownloadViewModel,
    tagEditorViewModel: TagEditorViewModel,
    compareViewModel: CompareViewModel,
    metadataRepairViewModel: MetadataRepairViewModel,
    settingsViewModel: SettingsViewModel,
    uiStyle: UiStyle,
    modifier: Modifier,
    onQuickDownloadFromClipboard: () -> Unit,
    onNavigate: (AppDestination) -> Unit
) {
    var showLibrarySyncProgress by
        rememberSaveable {
            mutableStateOf(
                false
            )
        }

    val systemDarkTheme =
        isSystemInDarkTheme()

    val downloadState by
        downloadViewModel
            .uiState
            .collectAsStateWithLifecycle()

    val compareState by
        compareViewModel
            .uiState
            .collectAsStateWithLifecycle()

    val repairState by
        metadataRepairViewModel
            .uiState
            .collectAsStateWithLifecycle()

    val settings by
        settingsViewModel
            .uiState
            .collectAsStateWithLifecycle()

    val libraryIndexState by
        settingsViewModel
            .libraryIndexState
            .collectAsStateWithLifecycle()

    val isDarkTheme =
        when (settings.themeMode) {
            AppThemeMode.SYSTEM ->
                systemDarkTheme

            AppThemeMode.LIGHT ->
                false

            AppThemeMode.DARK ->
                true
        }

    LaunchedEffect(
        metadataRepairViewModel
    ) {
        metadataRepairViewModel
            .ensureLoaded()
    }

    val openInbox: () -> Unit = {
        onNavigate(
            AppDestination.DOWNLOAD
        )
    }

    val openLibrary: () -> Unit = {
        onNavigate(
            if (
                settings
                    .hasLibraryFolder
            ) {
                AppDestination.LIBRARY
            } else {
                AppDestination.SETTINGS
            }
        )
    }

    val openLyrics: () -> Unit = {
        onNavigate(
            AppDestination.LYRICS
        )
    }

    val openTagEditor: () -> Unit = {
        tagEditorViewModel
            .ensureLoaded()

        onNavigate(
            AppDestination.EDIT_TAG
        )
    }

    val openCompare: () -> Unit = {
        compareViewModel
            .ensureLoaded()

        metadataRepairViewModel
            .ensureLoaded()

        onNavigate(
            AppDestination.COMPARE
        )
    }

    val openSettings: () -> Unit = {
        onNavigate(
            AppDestination.SETTINGS
        )
    }

    val toggleTheme: () -> Unit = {
        settingsViewModel
            .setThemeMode(
                if (isDarkTheme) {
                    AppThemeMode.LIGHT
                } else {
                    AppThemeMode.DARK
                }
            )
    }

    val syncLibrary: () -> Unit = {
        if (
            settings
                .hasLibraryFolder
        ) {
            showLibrarySyncProgress =
                true

            /*
             * When a scan is already running, tapping Sync only
             * reopens the progress screen. It must not restart it.
             */
            if (
                !libraryIndexState
                    .isScanning
            ) {
                settingsViewModel
                    .rebuildLibraryIndex()
            }
        } else {
            openSettings()
        }
    }

    val duplicateCount =
        compareState
            .totalPairCount
            .takeIf {
                compareState.hasLoaded
            }

    AppSkinContent(
        style =
            uiStyle,

        bento = {
            if (
                showLibrarySyncProgress
            ) {
                LibrarySyncProgressScreen(
                    state =
                        libraryIndexState,

                    libraryFolderName =
                        settings.libraryFolderName,

                    modifier =
                        modifier,

                    onClose = {
                        showLibrarySyncProgress =
                            false
                    },

                    onRetry = {
                        settingsViewModel
                            .rebuildLibraryIndex()
                    }
                )
            } else {
                BentoHome(
                    inboxFolderName =
                        settings.inboxFolderName,

                    librarySongCount =
                        libraryIndexState
                            .totalFiles,

                    attentionCount =
                        repairState
                            .attentionCount,

                    duplicateCount =
                        duplicateCount,

                    isLibrarySyncing =
                        libraryIndexState
                            .isScanning,

                    isDarkTheme =
                        isDarkTheme,

                    modifier =
                        modifier,

                    onOpenInbox =
                        openInbox,

                    onOpenLibrary =
                        openLibrary,

                    onOpenLyrics =
                        openLyrics,

                    onOpenTagEditor =
                        openTagEditor,

                    onOpenCompare =
                        openCompare,

                    onOpenSettings =
                        openSettings,

                    onToggleTheme =
                        toggleTheme,

                    onSyncLibrary =
                        syncLibrary,

                    onQuickDownloadFromClipboard =
                        onQuickDownloadFromClipboard
                )
            }
        },

        compact = {
            CompactHome(
                inboxFolderName =
                    settings.inboxFolderName,

                libraryFolderName =
                    settings.libraryFolderName,

                activeDownloadCount =
                    downloadState
                        .activeJobs
                        .size,

                librarySongCount =
                    libraryIndexState
                        .totalFiles,

                attentionCount =
                    repairState
                        .attentionCount,

                duplicateCount =
                    duplicateCount,

                modifier =
                    modifier,

                onOpenInbox =
                    openInbox,

                onOpenLyrics =
                    openLyrics,

                onOpenTagEditor =
                    openTagEditor,

                onOpenCompare =
                    openCompare,

                onOpenSettings =
                    openSettings
            )
        }
    )
}