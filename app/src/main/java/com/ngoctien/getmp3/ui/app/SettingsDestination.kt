package com.ngoctien.getmp3.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ngoctien.getmp3.ui.SettingsAction
import com.ngoctien.getmp3.ui.SettingsRoute
import com.ngoctien.getmp3.ui.design.UiStyle
import com.ngoctien.getmp3.viewmodel.DownloadViewModel
import com.ngoctien.getmp3.viewmodel.SettingsViewModel
import com.ngoctien.getmp3.viewmodel.TagEditorViewModel

@Composable
internal fun SettingsDestination(
    settingsViewModel: SettingsViewModel,
    downloadViewModel: DownloadViewModel,
    tagEditorViewModel: TagEditorViewModel,
    uiStyle: UiStyle,
    modifier: Modifier,
    onChooseInboxFolder: () -> Unit,
    onChooseLibraryFolder: () -> Unit
) {
    val settings by
        settingsViewModel
            .uiState
            .collectAsStateWithLifecycle()

    val libraryIndexState by
        settingsViewModel
            .libraryIndexState
            .collectAsStateWithLifecycle()

    val downloadState by
        downloadViewModel
            .uiState
            .collectAsStateWithLifecycle()

    SettingsRoute(
        settings =
            settings,

        libraryIndexState =
            libraryIndexState,

        downloadState =
            downloadState,

        uiStyle =
            uiStyle,

        modifier =
            modifier,

        onAction = { action ->

            when (action) {

                is SettingsAction.BitrateChanged -> {
                    settingsViewModel
                        .setBitrate(
                            action.value
                        )
                }

                is SettingsAction.ThemeChanged -> {
                    settingsViewModel
                        .setThemeMode(
                            action.value
                        )
                }

                is SettingsAction.UiStyleChanged -> {
                    settingsViewModel
                        .setUiStyle(
                            action.value
                        )
                }

                SettingsAction.ChooseInboxFolder -> {
                    onChooseInboxFolder()
                }

                SettingsAction.UseDefaultInboxFolder -> {
                    settingsViewModel
                        .useDefaultInboxFolder()

                    tagEditorViewModel
                        .refresh()
                }

                SettingsAction.ChooseLibraryFolder -> {
                    onChooseLibraryFolder()
                }

                SettingsAction.ClearLibraryFolder -> {
                    settingsViewModel
                        .clearLibraryFolder()
                }

                SettingsAction.RebuildLibraryIndex -> {
                    settingsViewModel
                        .rebuildLibraryIndex()
                }

                is SettingsAction.SaveFilters -> {
                    settingsViewModel
                        .setTitleFilters(
                            action.terms,
                            action.symbols
                        )
                }

                SettingsAction.RetryFfmpeg -> {
                    downloadViewModel
                        .checkFfmpeg()
                }
            }
        }
    )
}
