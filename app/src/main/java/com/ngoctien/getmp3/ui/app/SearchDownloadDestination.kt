package com.ngoctien.getmp3.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ngoctien.getmp3.ui.AppDestination
import com.ngoctien.getmp3.ui.SearchDownloadAction
import com.ngoctien.getmp3.ui.SearchDownloadRoute
import com.ngoctien.getmp3.ui.design.UiStyle
import com.ngoctien.getmp3.viewmodel.DownloadViewModel
import com.ngoctien.getmp3.viewmodel.SearchDownloadSection
import com.ngoctien.getmp3.viewmodel.SettingsViewModel
import com.ngoctien.getmp3.viewmodel.TagEditorViewModel
import com.ngoctien.getmp3.viewmodel.YouTubeSearchViewModel

@Composable
internal fun SearchDownloadDestination(
    downloadViewModel: DownloadViewModel,
    tagEditorViewModel: TagEditorViewModel,
    settingsViewModel: SettingsViewModel,
    youtubeSearchViewModel: YouTubeSearchViewModel,
    uiStyle: UiStyle,
    modifier: Modifier,
    onRequestNotificationPermission: () -> Unit,
    onNavigate:
        (AppDestination) -> Unit
) {
    val searchState by
        youtubeSearchViewModel
            .uiState
            .collectAsStateWithLifecycle()

    val downloadState by
        downloadViewModel
            .uiState
            .collectAsStateWithLifecycle()

    val settings by
        settingsViewModel
            .uiState
            .collectAsStateWithLifecycle()

    SearchDownloadRoute(
        searchState =
            searchState,

        downloadState =
            downloadState,

        settings =
            settings,

        uiStyle =
            uiStyle,

        modifier =
            modifier,

        onAction = { action ->

            when (action) {

                is SearchDownloadAction.QueryChanged -> {
                    youtubeSearchViewModel
                        .setQuery(
                            action.value
                        )
                }

                SearchDownloadAction.Search -> {
                    youtubeSearchViewModel
                        .search()
                }

                SearchDownloadAction.ClearSearch -> {
                    youtubeSearchViewModel
                        .clearSearch()
                }

                SearchDownloadAction.LoadMore -> {
                    youtubeSearchViewModel
                        .loadMore()
                }

                is SearchDownloadAction.SelectSection -> {
                    youtubeSearchViewModel
                        .selectSection(
                            action.value
                        )
                }

                is SearchDownloadAction.PasteAndDownload -> {

                    onRequestNotificationPermission()

                    downloadViewModel
                        .pasteAndDownload(
                            clipboardText =
                                action.value
                        )

                    youtubeSearchViewModel
                        .selectSection(
                            SearchDownloadSection
                                .QUEUE
                        )
                }

                is SearchDownloadAction.DownloadResult -> {

                    onRequestNotificationPermission()

                    downloadViewModel
                        .pasteAndDownload(
                            clipboardText =
                                action
                                    .value
                                    .webpageUrl
                        )

                    youtubeSearchViewModel
                        .showQueue()
                }

                is SearchDownloadAction.OpenReferenceSong -> {

                    tagEditorViewModel
                        .openReferenceSong(
                            action.value
                        )

                    onNavigate(
                        AppDestination.EDIT_TAG
                    )
                }

                is SearchDownloadAction.CancelJob -> {
                    downloadViewModel
                        .cancelJob(
                            action.id
                        )
                }

                is SearchDownloadAction.RetryJob -> {
                    downloadViewModel
                        .retryJob(
                            action.job
                        )
                }

                SearchDownloadAction.ClearHistory -> {
                    downloadViewModel
                        .clearHistory()
                }

                SearchDownloadAction.RetryFfmpeg -> {
                    downloadViewModel
                        .checkFfmpeg()
                }
            }
        }
    )
}
