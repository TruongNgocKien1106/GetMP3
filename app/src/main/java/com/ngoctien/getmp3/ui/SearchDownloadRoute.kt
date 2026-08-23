package com.ngoctien.getmp3.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ngoctien.getmp3.data.DownloadJobEntity
import com.ngoctien.getmp3.note.ReferenceSongMatch
import com.ngoctien.getmp3.settings.AppSettings
import com.ngoctien.getmp3.ui.design.UiStyle
import com.ngoctien.getmp3.ui.skin.AppSkinContent
import com.ngoctien.getmp3.ui.skin.compact.CompactSearchDownloadScreen
import com.ngoctien.getmp3.viewmodel.DownloadScreenUiState
import com.ngoctien.getmp3.viewmodel.SearchDownloadSection
import com.ngoctien.getmp3.viewmodel.YouTubeSearchUiState
import com.ngoctien.getmp3.youtube.YouTubeSearchResult

/*
 * Route owns semantic action mapping.
 *
 * Bento and Compact provide independent screen chrome while the results
 * list and download queue are intentionally shared inner content.
 */
@Composable
internal fun SearchDownloadRoute(
    searchState: YouTubeSearchUiState,
    downloadState: DownloadScreenUiState,
    settings: AppSettings,
    uiStyle: UiStyle,
    modifier: Modifier = Modifier,
    onAction: (SearchDownloadAction) -> Unit
) {
    val queryChanged:
        (String) -> Unit = {
            value ->

            onAction(
                SearchDownloadAction
                    .QueryChanged(
                        value
                    )
            )
        }

    val search:
        () -> Unit = {
            onAction(
                SearchDownloadAction
                    .Search
            )
        }

    val clearSearch:
        () -> Unit = {
            onAction(
                SearchDownloadAction
                    .ClearSearch
            )
        }

    val loadMore:
        () -> Unit = {
            onAction(
                SearchDownloadAction
                    .LoadMore
            )
        }

    val selectSection:
        (SearchDownloadSection) -> Unit = {
            value ->

            onAction(
                SearchDownloadAction
                    .SelectSection(
                        value
                    )
            )
        }

    val pasteAndDownload:
        (CharSequence?) -> Unit = {
            value ->

            onAction(
                SearchDownloadAction
                    .PasteAndDownload(
                        value
                    )
            )
        }

    val downloadResult:
        (YouTubeSearchResult) -> Unit = {
            value ->

            onAction(
                SearchDownloadAction
                    .DownloadResult(
                        value
                    )
            )
        }

    val openReferenceSong:
        (ReferenceSongMatch) -> Unit = {
            value ->

            onAction(
                SearchDownloadAction
                    .OpenReferenceSong(
                        value
                    )
            )
        }

    val cancelJob:
        (String) -> Unit = {
            value ->

            onAction(
                SearchDownloadAction
                    .CancelJob(
                        value
                    )
            )
        }

    val retryJob:
        (DownloadJobEntity) -> Unit = {
            value ->

            onAction(
                SearchDownloadAction
                    .RetryJob(
                        value
                    )
            )
        }

    val clearHistory:
        () -> Unit = {
            onAction(
                SearchDownloadAction
                    .ClearHistory
            )
        }

    val retryFfmpeg:
        () -> Unit = {
            onAction(
                SearchDownloadAction
                    .RetryFfmpeg
            )
        }

    AppSkinContent(
        style =
            uiStyle,

        bento = {
            BentoSearchDownloadScreen(
                searchState =
                    searchState,

                downloadState =
                    downloadState,

                modifier =
                    modifier,

                onQueryChange =
                    queryChanged,

                onSearch =
                    search,

                onClearSearch =
                    clearSearch,

                onLoadMore =
                    loadMore,

                onSelectSection =
                    selectSection,

                onPasteAndDownload =
                    pasteAndDownload,

                onDownloadResult =
                    downloadResult,

                onCancelJob =
                    cancelJob,

                onRetryJob =
                    retryJob,

                onClearHistory =
                    clearHistory,

                onRetryFfmpeg =
                    retryFfmpeg
            )
        },

        compact = {
            CompactSearchDownloadScreen(
                searchState =
                    searchState,

                downloadState =
                    downloadState,

                settings =
                    settings,

                modifier =
                    modifier,

                onQueryChange =
                    queryChanged,

                onSearch =
                    search,

                onClearSearch =
                    clearSearch,

                onLoadMore =
                    loadMore,

                onSelectSection =
                    selectSection,

                onPasteAndDownload =
                    pasteAndDownload,

                onDownloadResult =
                    downloadResult,

                onOpenReferenceSong =
                    openReferenceSong,

                onCancelJob =
                    cancelJob,

                onRetryJob =
                    retryJob,

                onClearHistory =
                    clearHistory,

                onRetryFfmpeg =
                    retryFfmpeg
            )
        }
    )
}
