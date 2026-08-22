package com.ngoctien.getmp3.ui

import androidx.compose.material.icons.automirrored.rounded.List

import com.ngoctien.getmp3.ui.design.LocalAppDesign

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ngoctien.getmp3.data.DownloadJobEntity
import com.ngoctien.getmp3.model.DownloadStatus
import com.ngoctien.getmp3.note.ReferenceSongMatch
import com.ngoctien.getmp3.settings.AppSettings
import com.ngoctien.getmp3.ui.theme.BrandBlue
import com.ngoctien.getmp3.ui.theme.BrandCyan
import com.ngoctien.getmp3.ui.theme.BrandViolet
import com.ngoctien.getmp3.viewmodel.DownloadScreenUiState
import com.ngoctien.getmp3.viewmodel.FfmpegReadyState
import com.ngoctien.getmp3.viewmodel.SearchDownloadSection
import com.ngoctien.getmp3.viewmodel.YouTubeSearchUiState
import com.ngoctien.getmp3.youtube.YouTubeSearchResult

@Composable
internal fun BentoSearchDownloadScreen(
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

    val downloadEnabled =
        downloadState.ffmpegState ==
            FfmpegReadyState.READY &&
            !downloadState
                .isPreparingDownload

    var showReferenceMatches by
        remember {
            mutableStateOf(false)
        }

    LaunchedEffect(searchState.query) {
        showReferenceMatches = false
    }

    Column(
        modifier = modifier
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
            title = "Inbox",

            subtitle =
                "Tìm, tải và xử lý nhạc mới • ${settings.bitrateKbps} kbps",

            icon =
                Icons.Rounded.Download
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        SearchHeroPanel(
            searchState =
                searchState,

            downloadState =
                downloadState,

            onQueryChange =
                onQueryChange,

            onSearch =
                onSearch,


            onClearSearch =
                onClearSearch,

            onSelectSection =
                onSelectSection,

            onOpenReferenceMatches = {
                showReferenceMatches = true
            },

            onRetryFfmpeg =
                onRetryFfmpeg
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        when (
            searchState
                .selectedSection
        ) {
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

    if (
        showReferenceMatches &&
        searchState.referenceMatches.isNotEmpty()
    ) {
        ReferenceMatchesSheet(
            query =
                searchState.query,

            matches =
                searchState.referenceMatches,

            onDismiss = {
                showReferenceMatches = false
            },

            onOpenReferenceSong =
                onOpenReferenceSong
        )
    }
}

@Composable
private fun SearchHeroPanel(
    searchState:
        YouTubeSearchUiState,

    downloadState:
        DownloadScreenUiState,

    onQueryChange: (String) -> Unit,

    onSearch: () -> Unit,

    onClearSearch: () -> Unit,

    onSelectSection:
        (SearchDownloadSection) -> Unit,

    onOpenReferenceMatches: () -> Unit,

    onRetryFfmpeg: () -> Unit
) {
    val shape =
        RoundedCornerShape(28.dp)

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = shape,
                    clip = false
                ),

        shape =
            shape,

        color =
            MaterialTheme
                .colorScheme
                .surface
                .copy(alpha = 0.96f),

        contentColor =
            MaterialTheme
                .colorScheme
                .onSurface,

        tonalElevation = 5.dp,
        shadowElevation = 5.dp,

        border =
            BorderStroke(
                width = 1.dp,

                color =
                    MaterialTheme
                        .colorScheme
                        .primary
                        .copy(alpha = 0.24f)
            )
    ) {
        Column(
            modifier =
                Modifier.padding(14.dp)
        ) {
            SearchField(
                query =
                    searchState.query,

                isSearching =
                    searchState.isSearching ||
                        searchState
                            .isSearchingReference,

                onQueryChange =
                    onQueryChange,

                onSearch =
                    onSearch,

                onClear =
                    onClearSearch
            )


            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            SearchSectionSelector(
                selected =
                    searchState
                        .selectedSection,

                resultCount =
                    searchState.results.size,

                queueCount =
                    downloadState
                        .activeJobs
                        .size,

                referenceMatches =
                    searchState
                        .referenceMatches,

                onSelected =
                    onSelectSection,

                onOpenReferenceMatches =
                    onOpenReferenceMatches
            )

            if (
                downloadState
                    .isPreparingDownload
            ) {
                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                PreparingDownloadNotice(
                    message =
                        downloadState
                            .preparingMessage
                            ?: "Đang chuẩn bị tải..."
                )
            }

            if (
                downloadState.ffmpegState ==
                FfmpegReadyState.FAILED
            ) {
                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                FfmpegErrorNotice(
                    message =
                        downloadState
                            .ffmpegMessage,

                    onRetry =
                        onRetryFfmpeg
                )
            }
        }
    }
}
@Composable
private fun SearchField(
    query: String,
    isSearching: Boolean,
    onQueryChange:
        (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit
) {
    val focusManager =
        LocalFocusManager.current

    val keyboardController =
        LocalSoftwareKeyboardController
            .current

    fun submitSearch() {
        if (
            query.isBlank() ||
            isSearching
        ) {
            return
        }

        focusManager.clearFocus(
            force = true
        )

        keyboardController?.hide()

        onSearch()
    }

    StableOutlinedTextField(
        value =
            query,

        onValueChange =
            onQueryChange,

        modifier =
            Modifier.fillMaxWidth(),

        singleLine = true,

        shape =
            RoundedCornerShape(18.dp),

        label = {
            Text(
                "Tên bài hát hoặc Artist"
            )
        },

        placeholder = {
            Text(
                "Ví dụ: Nơi Này Có Anh Sơn Tùng M-TP"
            )
        },

        leadingIcon = {
            Icon(
                imageVector =
                    Icons.Rounded.Search,

                contentDescription =
                    null
            )
        },

        trailingIcon = {
            when {
                isSearching -> {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(22.dp),

                        strokeWidth = 2.dp
                    )
                }

                query.isNotEmpty() -> {
                    IconButton(
                        onClick = {
                            onClear()
                        }
                    ) {
                        Icon(
                            imageVector =
                                Icons.Rounded.Close,

                            contentDescription =
                                "Xóa nội dung tìm kiếm"
                        )
                    }
                }
            }
        },

        keyboardOptions =
            KeyboardOptions(
                imeAction =
                    ImeAction.Search
            ),

        keyboardActions =
            KeyboardActions(
                onSearch = {
                    submitSearch()
                }
            ),

        colors =
            OutlinedTextFieldDefaults
                .colors(
                    focusedTextColor =
                        MaterialTheme
                            .colorScheme
                            .onSurface,

                    unfocusedTextColor =
                        MaterialTheme
                            .colorScheme
                            .onSurface,

                    cursorColor =
                        MaterialTheme
                            .colorScheme
                            .primary,

                    focusedBorderColor =
                        MaterialTheme
                            .colorScheme
                            .primary,

                    unfocusedBorderColor =
                        MaterialTheme
                            .colorScheme
                            .outline,

                    focusedLabelColor =
                        MaterialTheme
                            .colorScheme
                            .primary,

                    unfocusedLabelColor =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,

                    focusedPlaceholderColor =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,

                    unfocusedPlaceholderColor =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,

                    focusedLeadingIconColor =
                        MaterialTheme
                            .colorScheme
                            .primary,

                    unfocusedLeadingIconColor =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,

                    focusedTrailingIconColor =
                        MaterialTheme
                            .colorScheme
                            .primary,

                    unfocusedTrailingIconColor =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
    )
}
@Composable
private fun SearchSectionSelector(
    selected: SearchDownloadSection,
    resultCount: Int,
    queueCount: Int,
    referenceMatches:
        List<ReferenceSongMatch>,
    onSelected:
        (SearchDownloadSection) -> Unit,
    onOpenReferenceMatches: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(
                rememberScrollState()
            ),

        horizontalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected =
                selected ==
                    SearchDownloadSection
                        .RESULTS,

            onClick = {
                onSelected(
                    SearchDownloadSection
                        .RESULTS
                )
            },

            label = {
                Text(
                    if (resultCount > 0) {
                        "Kết quả $resultCount"
                    } else {
                        "Kết quả"
                    }
                )
            },

            leadingIcon = {
                Icon(
                    imageVector =
                        Icons.Rounded.Search,

                    contentDescription = null,

                    modifier =
                        Modifier.size(18.dp)
                )
            }
        )

        FilterChip(
            selected =
                selected ==
                    SearchDownloadSection
                        .QUEUE,

            onClick = {
                onSelected(
                    SearchDownloadSection
                        .QUEUE
                )
            },

            label = {
                Text(
                    if (queueCount > 0) {
                        "Đang tải $queueCount"
                    } else {
                        "Tải xuống"
                    }
                )
            },

            leadingIcon = {
                Icon(
                    imageVector =
                        Icons.AutoMirrored.Rounded.List,

                    contentDescription = null,

                    modifier =
                        Modifier.size(18.dp)
                )
            }
        )

        if (referenceMatches.isNotEmpty()) {
            ReferenceMatchChip(
                matches = referenceMatches,
                onClick =
                    onOpenReferenceMatches
            )
        }
    }
}

@Composable
private fun PreparingDownloadNotice(
    message: String
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(16.dp),

        color =
            MaterialTheme
                .colorScheme
                .primaryContainer
    ) {
        Column(
            modifier =
                Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.size(19.dp),

                    strokeWidth = 2.dp
                )

                Spacer(
                    modifier =
                        Modifier.width(9.dp)
                )

                Text(
                    text = message,
                    fontWeight =
                        FontWeight.Medium
                )
            }

            Spacer(
                modifier =
                    Modifier.height(9.dp)
            )

            LinearProgressIndicator(
                modifier =
                    Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FfmpegErrorNotice(
    message: String,
    onRetry: () -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(16.dp),

        color =
            MaterialTheme
                .colorScheme
                .errorContainer
    ) {
        Row(
            modifier =
                Modifier.padding(12.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Icon(
                imageVector =
                    Icons.Rounded
                        .ErrorOutline,

                contentDescription =
                    null,

                tint =
                    MaterialTheme
                        .colorScheme
                        .error
            )

            Spacer(
                modifier =
                    Modifier.width(10.dp)
            )

            Text(
                text = message,

                modifier =
                    Modifier.weight(1f),

                style =
                    MaterialTheme
                        .typography
                        .bodySmall
            )

            TextButton(
                onClick = onRetry
            ) {
                Text("Thử lại")
            }
        }
    }
}
