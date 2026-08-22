package com.ngoctien.getmp3.ui

import com.ngoctien.getmp3.ui.components.AppCard

import com.ngoctien.getmp3.ui.components.AppActionVisualState

import com.ngoctien.getmp3.ui.components.AppActionButton

import com.ngoctien.getmp3.ui.components.AppFeedbackKind

import com.ngoctien.getmp3.ui.components.AppFeedbackBanner

import com.ngoctien.getmp3.ui.components.AppEmptyState

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
import androidx.compose.material.icons.rounded.ContentPaste
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
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

/*
 * Shared inner content for Bento and Compact Search/Download screens.
 * Skin-specific chrome stays in each screen implementation.
 */
@Composable
internal fun SearchResultsContent(
    state: YouTubeSearchUiState,
    downloadEnabled: Boolean,
    onSearch: () -> Unit,
    onLoadMore: () -> Unit,
    onDownload:
        (YouTubeSearchResult) -> Unit
) {
    LazyColumn(
        modifier =
            Modifier.fillMaxSize(),

        contentPadding =
            PaddingValues(
                bottom = 26.dp
            ),

        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        if (
            state.isSearching &&
            state.results.isNotEmpty()
        ) {
            item(
                key = "search-progress"
            ) {
                LinearProgressIndicator(
                    modifier =
                        Modifier.fillMaxWidth()
                )
            }
        }

        if (state.query.isNotBlank()) {
            item(
                key = "youtube-header"
            ) {
                YouTubeResultsHeader(
                    count = state.results.size
                )
            }
        }

        when {
            state.isSearching &&
                state.results.isEmpty() -> {

                item(
                    key = "loading"
                ) {
                    SearchLoadingPanel()
                }
            }

            state.results.isNotEmpty() -> {
                items(
                    items =
                        state.results,

                    key = {
                        it.videoId
                    }
                ) { result ->
                    YouTubeResultCard(
                        result =
                            result,

                        downloadEnabled =
                            downloadEnabled,

                        onDownload = {
                            onDownload(result)
                        }
                    )
                }

                if (
                    state.hasMore ||
                    state.isLoadingMore ||
                    state.loadMoreError !=
                    null
                ) {
                    item(
                        key =
                            "load-more-" +
                                state.nextOffset
                    ) {
                        LoadMoreFooter(
                            triggerKey =
                                state.nextOffset,

                            hasMore =
                                state.hasMore,

                            isLoading =
                                state.isLoadingMore,

                            errorMessage =
                                state.loadMoreError,

                            onLoadMore =
                                onLoadMore
                        )
                    }
                } else {
                    item(
                        key = "end-results"
                    ) {
                        Text(
                            text =
                                "Đã hiển thị toàn bộ kết quả tìm được",

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical = 14.dp
                                ),

                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onBackground
                                    .copy(
                                        alpha = 0.82f
                                    )
                        )
                    }
                }
            }

            state.errorMessage != null -> {
                item(
                    key = "error"
                ) {
                    SearchErrorPanel(
                        message =
                            state.errorMessage,

                        onRetry =
                            onSearch
                    )
                }
            }

            else -> {
                item(
                    key = "empty"
                ) {
                    SearchEmptyPanel()
                }
            }
        }
    }
}

@Composable
private fun LoadMoreFooter(
    triggerKey: Int,
    hasMore: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onLoadMore: () -> Unit
) {
    LaunchedEffect(
        triggerKey,
        hasMore,
        isLoading,
        errorMessage
    ) {
        if (
            hasMore &&
            !isLoading &&
            errorMessage == null
        ) {
            onLoadMore()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp),

        contentAlignment =
            Alignment.Center
    ) {
        when {
            isLoading -> {
                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(22.dp),

                        strokeWidth = 2.dp
                    )

                    Spacer(
                        modifier =
                            Modifier.width(10.dp)
                    )

                    Text(
                        text =
                            "Đang tải thêm 10 kết quả..."
                    )
                }
            }

            errorMessage != null -> {
                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Text(
                        text =
                            errorMessage,

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,

                        color =
                            MaterialTheme
                                .colorScheme
                                .error
                    )

                    TextButton(
                        onClick =
                            onLoadMore
                    ) {
                        Icon(
                            imageVector =
                                Icons.Rounded
                                    .Refresh,

                            contentDescription =
                                null
                        )

                        Spacer(
                            modifier =
                                Modifier.width(6.dp)
                        )

                        Text("Tải tiếp")
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchLoadingPanel() {
    AppLoadingSkeleton(
        text =
            "Đang tìm video trên YouTube."
    )
}

@Composable
private fun SearchEmptyPanel() {

    AppEmptyState(
        icon =
            Icons.Rounded.Search,

        title =
            "Nhập tên bài hát để bắt đầu tìm",

        description =
            "Cuộn xuống cuối để tự động tải thêm kết quả."
    )
}

@Composable
private fun SearchErrorPanel(
    message: String,
    onRetry: () -> Unit
) {

    AppFeedbackBanner(
        message =
            message,

        kind =
            AppFeedbackKind.ERROR,

        modifier =
            Modifier.padding(
                vertical = 12.dp
            ),

        actionLabel =
            "Thử lại",

        onAction =
            onRetry
    )
}

@Composable
private fun YouTubeResultCard(
    result: YouTubeSearchResult,
    downloadEnabled: Boolean,
    onDownload: () -> Unit
) {
    val context =
        LocalContext.current

    val previewManager =
        remember(
            context
        ) {
            PreviewAppManager(
                context
            )
        }

    var showPreviewChooser by
        remember(
            result.webpageUrl
        ) {
            mutableStateOf(
                false
            )
        }

    if (showPreviewChooser) {
        PreviewAppChooserDialog(
            url =
                result.webpageUrl,

            onDismiss = {
                showPreviewChooser =
                    false
            }
        )
    }

    AppCard(
        modifier =
            Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                verticalAlignment =
                    Alignment.Top
            ) {
                Box(
                    modifier =
                        Modifier
                            .width(
                                138.dp
                            )
                            .aspectRatio(
                                16f / 9f
                            )
                            .clip(
                                RoundedCornerShape(
                                    15.dp
                                )
                            )
                            .background(
                                MaterialTheme
                                    .colorScheme
                                    .surfaceVariant
                            )
                ) {
                    AsyncImage(
                        model =
                            result
                                .effectiveThumbnailUrl,

                        contentDescription =
                            "Thumbnail ${result.title}",

                        modifier =
                            Modifier.fillMaxSize(),

                        contentScale =
                            ContentScale.Crop
                    )

                    Surface(
                        modifier =
                            Modifier
                                .align(
                                    Alignment.BottomEnd
                                )
                                .padding(
                                    5.dp
                                ),

                        shape =
                            RoundedCornerShape(
                                7.dp
                            ),

                        color =
                            Color.Black
                                .copy(
                                    alpha =
                                        0.78f
                                ),

                        contentColor =
                            Color.White
                    ) {
                        Text(
                            text =
                                result
                                    .formattedDuration,

                            modifier =
                                Modifier.padding(
                                    horizontal =
                                        6.dp,

                                    vertical =
                                        3.dp
                                ),

                            style =
                                MaterialTheme
                                    .typography
                                    .labelSmall,

                            color =
                                Color.White
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.width(
                            12.dp
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
                            result.title,

                        maxLines =
                            3,

                        overflow =
                            TextOverflow
                                .Ellipsis,

                        style =
                            MaterialTheme
                                .typography
                                .titleSmall,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurface
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                6.dp
                            )
                    )

                    Text(
                        text =
                            result.channel
                                .ifBlank {
                                    "Không rõ kênh"
                                },

                        maxLines =
                            2,

                        overflow =
                            TextOverflow
                                .Ellipsis,

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurface
                                .copy(
                                    alpha = 0.80f
                                )
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )

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

            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )

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

                        val opened =
                            previewManager
                                .openSaved(
                                    result.webpageUrl
                                )

                        if (!opened) {
                            showPreviewChooser =
                                true
                        }
                    },

                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded
                                .PlayArrow,

                        contentDescription =
                            null
                    )

                    Spacer(
                        modifier =
                            Modifier.width(
                                6.dp
                            )
                    )

                    Text(
                        "Nghe thử"
                    )
                }

                AppActionButton(
                    label =
                        "Tải",

                    onClick =
                        onDownload,

                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    enabled =
                        downloadEnabled,

                    state =
                        AppActionVisualState
                            .IDLE,

                    leadingIcon =
                        Icons.Rounded
                            .Download,

                    haptic =
                        true
                )
            }
        }
    }
}
