package com.ngoctien.getmp3.ui.skin.compact

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Download
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import com.ngoctien.getmp3.data.DownloadJobEntity
import com.ngoctien.getmp3.note.ReferenceSongMatch
import com.ngoctien.getmp3.settings.AppSettings
import com.ngoctien.getmp3.ui.AppHeaderActionButton
import com.ngoctien.getmp3.ui.AppMotion
import com.ngoctien.getmp3.ui.AppPageHeader
import com.ngoctien.getmp3.ui.DownloadQueueContent
import com.ngoctien.getmp3.ui.SearchResultsContent
import com.ngoctien.getmp3.ui.design.LocalAppDesign
import com.ngoctien.getmp3.viewmodel.DownloadScreenUiState
import com.ngoctien.getmp3.viewmodel.FfmpegReadyState
import com.ngoctien.getmp3.viewmodel.SearchDownloadSection
import com.ngoctien.getmp3.viewmodel.YouTubeSearchUiState
import com.ngoctien.getmp3.youtube.YouTubeSearchResult

@Suppress(
    "DEPRECATION"
)
@Composable
internal fun CompactSearchDownloadScreen(
    searchState: YouTubeSearchUiState,
    downloadState: DownloadScreenUiState,
    settings: AppSettings,
    modifier: Modifier = Modifier,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearSearch: () -> Unit,
    onLoadMore: () -> Unit,
    onSelectSection:
        (SearchDownloadSection) -> Unit,
    onPasteAndDownload:
        (CharSequence?) -> Unit,
    onDownloadResult:
        (YouTubeSearchResult) -> Unit,
    onOpenReferenceSong:
        (ReferenceSongMatch) -> Unit,
    onCancelJob: (String) -> Unit,
    onRetryJob:
        (DownloadJobEntity) -> Unit,
    onClearHistory: () -> Unit,
    onRetryFfmpeg: () -> Unit
) {
    val design =
        LocalAppDesign.current

    val clipboard =
        LocalClipboardManager.current

    var showReferenceMatches by
        remember {
            mutableStateOf(
                false
            )
        }

    LaunchedEffect(
        searchState.query
    ) {
        showReferenceMatches =
            false
    }

    val downloadEnabled =
        downloadState.ffmpegState ==
            FfmpegReadyState.READY &&
            !downloadState
                .isPreparingDownload

    val queueCount =
        downloadState
            .activeJobs
            .size +
            downloadState
                .recentJobs
                .size

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(
                    start =
                        design
                            .spacing
                            .screenHorizontal,

                    top =
                        design
                            .spacing
                            .screenVertical,

                    end =
                        design
                            .spacing
                            .screenHorizontal
                )
    ) {
        AppPageHeader(
            title =
                "Inbox",

            subtitle =
                "Tìm • tải • xử lý nhạc mới • ${settings.bitrateKbps} kbps",

            icon =
                Icons.Rounded.Download,

            action = {
                AppHeaderActionButton(
                    icon =
                        Icons.Rounded
                            .ContentPaste,

                    contentDescription =
                        "Dán liên kết và tải",

                    enabled =
                        downloadEnabled,

                    onClick = {
                        onPasteAndDownload(
                            clipboard
                                .getText()
                                ?.text
                        )
                    }
                )
            }
        )

        Spacer(
            modifier =
                Modifier.height(
                    design.spacing.small
                )
        )

        CompactSearchTopPanel(
            query =
                searchState.query,

            isSearching =
                searchState.isSearching,

            isSearchingReference =
                searchState
                    .isSearchingReference,

            isPreparingDownload =
                downloadState
                    .isPreparingDownload,

            ffmpegState =
                downloadState
                    .ffmpegState,

            ffmpegMessage =
                downloadState
                    .ffmpegMessage,

            referenceMessage =
                searchState
                    .referenceMessage,

            onQueryChange =
                onQueryChange,

            onSearch =
                onSearch,

            onClear =
                onClearSearch,

            onRetryFfmpeg =
                onRetryFfmpeg
        )

        Spacer(
            modifier =
                Modifier.height(
                    design.spacing.small
                )
        )

        CompactSearchSectionBar(
            selected =
                searchState
                    .selectedSection,

            resultCount =
                searchState
                    .results
                    .size,

            queueCount =
                queueCount,

            referenceCount =
                searchState
                    .referenceMatches
                    .size,

            onSelected =
                onSelectSection,

            onOpenReferenceMatches = {
                if (
                    searchState
                        .referenceMatches
                        .isNotEmpty()
                ) {
                    showReferenceMatches =
                        true
                }
            }
        )

        Spacer(
            modifier =
                Modifier.height(
                    design.spacing.small
                )
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
        ) {
            AnimatedContent(
                targetState =
                    searchState
                        .selectedSection,

                modifier =
                    Modifier.fillMaxSize(),

                transitionSpec = {
                    (
                        slideInHorizontally(
                            animationSpec =
                                tween(
                                    AppMotion
                                        .EnterMillis
                                ),

                            initialOffsetX = {
                                    width ->
                                    width / 10
                                }
                        ) +
                            fadeIn(
                                animationSpec =
                                    tween(
                                        AppMotion
                                            .ContentMillis
                                    )
                            )
                        ) togetherWith
                        (
                            slideOutHorizontally(
                                animationSpec =
                                    tween(
                                        AppMotion
                                            .ExitMillis
                                    ),

                                targetOffsetX = {
                                    width ->
                                    -width / 10
                                }
                            ) +
                                fadeOut(
                                    animationSpec =
                                        tween(
                                            AppMotion
                                                .ExitMillis
                                        )
                                )
                            )
                },

                label =
                    "compact-search-section"
            ) { section ->

                when (section) {
                    SearchDownloadSection.RESULTS -> {
                        SearchResultsContent(
                            state =
                                searchState,

                            downloadEnabled =
                                downloadEnabled,

                            onSearch =
                                onSearch,

                            onLoadMore =
                                onLoadMore,

                            onDownload =
                                onDownloadResult
                        )
                    }

                    SearchDownloadSection.QUEUE -> {
                        DownloadQueueContent(
                            state =
                                downloadState,

                            onCancelJob =
                                onCancelJob,

                            onRetryJob =
                                onRetryJob,

                            onClearHistory =
                                onClearHistory
                        )
                    }
                }
            }
        }
    }

    if (
        showReferenceMatches &&
        searchState
            .referenceMatches
            .isNotEmpty()
    ) {
        CompactReferenceMatchesSheet(
            query =
                searchState.query,

            matches =
                searchState
                    .referenceMatches,

            onDismiss = {
                showReferenceMatches =
                    false
            },

            onOpenReferenceSong =
                onOpenReferenceSong
        )
    }
}
