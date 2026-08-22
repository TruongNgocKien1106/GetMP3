package com.ngoctien.getmp3.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ngoctien.getmp3.settings.AppSettings
import com.ngoctien.getmp3.settings.AppThemeMode
import com.ngoctien.getmp3.settings.AppUiStyle
import com.ngoctien.getmp3.ui.design.UiStyle
import com.ngoctien.getmp3.ui.skin.AppSkinContent
import com.ngoctien.getmp3.ui.skin.compact.CompactSettingsScreen
import com.ngoctien.getmp3.viewmodel.CompareIndexUiState
import com.ngoctien.getmp3.viewmodel.DownloadScreenUiState

@Composable
internal fun SettingsRoute(
    settings: AppSettings,
    libraryIndexState: CompareIndexUiState,
    downloadState: DownloadScreenUiState,
    uiStyle: UiStyle,
    modifier: Modifier = Modifier,
    onAction:
        (SettingsAction) -> Unit
) {
    val changeBitrate:
        (Int) -> Unit = {
            value ->

        onAction(
            SettingsAction
                .BitrateChanged(
                    value
                )
        )
    }

    val changeTheme:
        (AppThemeMode) -> Unit = {
            value ->

        onAction(
            SettingsAction
                .ThemeChanged(
                    value
                )
        )
    }

    val changeUiStyle:
        (AppUiStyle) -> Unit = {
            value ->

        onAction(
            SettingsAction
                .UiStyleChanged(
                    value
                )
        )
    }

    val chooseInbox:
        () -> Unit = {

        onAction(
            SettingsAction
                .ChooseInboxFolder
        )
    }

    val useDefaultInbox:
        () -> Unit = {

        onAction(
            SettingsAction
                .UseDefaultInboxFolder
        )
    }

    val chooseLibrary:
        () -> Unit = {

        onAction(
            SettingsAction
                .ChooseLibraryFolder
        )
    }

    val clearLibrary:
        () -> Unit = {

        onAction(
            SettingsAction
                .ClearLibraryFolder
        )
    }

    val rebuildLibrary:
        () -> Unit = {

        onAction(
            SettingsAction
                .RebuildLibraryIndex
        )
    }

    val saveFilters:
        (List<String>, String) -> Unit = {
            terms,
            symbols ->

        onAction(
            SettingsAction
                .SaveFilters(
                    terms =
                        terms,

                    symbols =
                        symbols
                )
        )
    }

    val retryFfmpeg:
        () -> Unit = {

        onAction(
            SettingsAction
                .RetryFfmpeg
        )
    }

    AppSkinContent(
        style =
            uiStyle,

        bento = {
            BentoSettingsScreen(
                settings =
                    settings,

                libraryIndexState =
                    libraryIndexState,

                downloadState =
                    downloadState,

                modifier =
                    modifier,

                onBitrateChange =
                    changeBitrate,

                onThemeChange =
                    changeTheme,

                onUiStyleChange =
                    changeUiStyle,

                onChooseInboxFolder =
                    chooseInbox,

                onUseDefaultInboxFolder =
                    useDefaultInbox,

                onChooseLibraryFolder =
                    chooseLibrary,

                onClearLibraryFolder =
                    clearLibrary,

                onRebuildLibraryIndex =
                    rebuildLibrary,

                onSaveFilters =
                    saveFilters,

                onRetryFfmpeg =
                    retryFfmpeg
            )
        },

        compact = {
            CompactSettingsScreen(
                settings =
                    settings,

                libraryIndexState =
                    libraryIndexState,

                downloadState =
                    downloadState,

                modifier =
                    modifier,

                onBitrateChange =
                    changeBitrate,

                onThemeChange =
                    changeTheme,

                onUiStyleChange =
                    changeUiStyle,

                onChooseInboxFolder =
                    chooseInbox,

                onUseDefaultInboxFolder =
                    useDefaultInbox,

                onChooseLibraryFolder =
                    chooseLibrary,

                onClearLibraryFolder =
                    clearLibrary,

                onRebuildLibraryIndex =
                    rebuildLibrary,

                onSaveFilters =
                    saveFilters,

                onRetryFfmpeg =
                    retryFfmpeg
            )
        }
    )
}
