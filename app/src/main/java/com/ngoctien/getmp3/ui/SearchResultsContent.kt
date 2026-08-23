package com.ngoctien.getmp3.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ngoctien.getmp3.ui.design.appPressable
import com.ngoctien.getmp3.viewmodel.YouTubeSearchUiState
import com.ngoctien.getmp3.youtube.YouTubeSearchResult


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
                bottom = 28.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(14.dp)
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
                        Modifier
                            .fillMaxWidth()
                            .clip(
                                CircleShape
                            )
                )
            }
        }

        if (state.query.isNotBlank()) {
            item(
                key = "result-header"
            ) {
                PrototypeResultsHeader(
                    count =
                        state.results.size
                )
            }
        }

        when {
            state.isSearching &&
                state.results.isEmpty() -> {

                item(
                    key = "loading"
                ) {
                    PrototypeSearchMessage(
                        title =
                            "Đang tìm trên YouTube",
                        description =
                            "GetMP3 đang lấy các kết quả phù hợp nhất."
                    )
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
                    PrototypeYouTubeResultCard(
                        result =
                            result,
                        downloadEnabled =
                            downloadEnabled,
                        onDownload = {
                            onDownload(
                                result
                            )
                        }
                    )
                }

                if (
                    state.hasMore ||
                    state.isLoadingMore ||
                    state.loadMoreError != null
                ) {
                    item(
                        key =
                            "load-more-" +
                                state.nextOffset
                    ) {
                        PrototypeLoadMoreFooter(
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
                }
            }

            state.errorMessage != null -> {
                item(
                    key = "error"
                ) {
                    PrototypeSearchError(
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
                    PrototypeSearchMessage(
                        title =
                            "Tìm bài hát",
                        description =
                            "Nhập tên bài hát, Artist hoặc từ khóa để bắt đầu."
                    )
                }
            }
        }
    }
}


@Composable
private fun PrototypeResultsHeader(
    count: Int
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Column(
            modifier =
                Modifier.weight(1f)
        ) {
            Text(
                text =
                    "Kết quả phù hợp nhất",
                style =
                    MaterialTheme
                        .typography
                        .titleSmall,
                fontWeight =
                    FontWeight.Black,
                color =
                    MaterialTheme
                        .colorScheme
                        .onBackground
            )

            Text(
                text =
                    "YouTube",
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }

        Surface(
            shape =
                RoundedCornerShape(
                    99.dp
                ),
            color =
                MaterialTheme
                    .colorScheme
                    .primary
                    .copy(
                        alpha = 0.13f
                    )
        ) {
            Text(
                text =
                    "$count kết quả",
                modifier =
                    Modifier.padding(
                        horizontal = 9.dp,
                        vertical = 5.dp
                    ),
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                fontWeight =
                    FontWeight.Bold,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )
        }
    }
}


@Composable
private fun PrototypeYouTubeResultCard(
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
            mutableStateOf(false)
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

    val shape =
        RoundedCornerShape(24.dp)

    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            shape,
        color =
            MaterialTheme
                .colorScheme
                .surface
                .copy(
                    alpha = 0.92f
                ),
        border =
            BorderStroke(
                1.dp,
                MaterialTheme
                    .colorScheme
                    .primary
                    .copy(
                        alpha = 0.30f
                    )
            )
    ) {
        Column {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(
                            16f / 9f
                        )
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF163C6C),
                                    Color(0xFF202D5C),
                                    Color(0xFF3A275F)
                                )
                            )
                        )
            ) {
                AsyncImage(
                    model =
                        result
                            .effectiveThumbnailUrl,
                    contentDescription =
                        "Ảnh ${result.title}",
                    modifier =
                        Modifier.fillMaxSize(),
                    contentScale =
                        ContentScale.Crop
                )

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(66.dp)
                            .align(
                                Alignment.BottomCenter
                            )
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.Black.copy(
                                            alpha = 0.64f
                                        )
                                    )
                                )
                            )
                )

                Surface(
                    modifier =
                        Modifier
                            .align(
                                Alignment.TopStart
                            )
                            .padding(10.dp),
                    shape =
                        RoundedCornerShape(
                            9.dp
                        ),
                    color =
                        Color.Black.copy(
                            alpha = 0.52f
                        ),
                    border =
                        BorderStroke(
                            1.dp,
                            Color.White.copy(
                                alpha = 0.15f
                            )
                        )
                ) {
                    Text(
                        text =
                            "YOUTUBE",
                        modifier =
                            Modifier.padding(
                                horizontal = 8.dp,
                                vertical = 4.dp
                            ),
                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,
                        fontWeight =
                            FontWeight.Black,
                        color =
                            Color.White
                    )
                }

                if (
                    result.formattedDuration
                        .isNotBlank()
                ) {
                    Surface(
                        modifier =
                            Modifier
                                .align(
                                    Alignment.BottomEnd
                                )
                                .padding(10.dp),
                        shape =
                            RoundedCornerShape(
                                8.dp
                            ),
                        color =
                            Color.Black.copy(
                                alpha = 0.76f
                            )
                    ) {
                        Text(
                            text =
                                result
                                    .formattedDuration,
                            modifier =
                                Modifier.padding(
                                    horizontal = 7.dp,
                                    vertical = 4.dp
                                ),
                            style =
                                MaterialTheme
                                    .typography
                                    .labelSmall,
                            fontWeight =
                                FontWeight.Bold,
                            color =
                                Color.White
                        )
                    }
                }
            }

            Column(
                modifier =
                    Modifier.padding(
                        start = 14.dp,
                        top = 13.dp,
                        end = 14.dp,
                        bottom = 14.dp
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(13.dp)
            ) {
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {
                        Text(
                            text =
                                result.title,
                            maxLines =
                                2,
                            overflow =
                                TextOverflow.Ellipsis,
                            style =
                                MaterialTheme
                                    .typography
                                    .titleMedium,
                            fontWeight =
                                FontWeight.Black,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurface
                        )

                        Spacer(
                            modifier =
                                Modifier.height(3.dp)
                        )

                        Text(
                            text =
                                result.channel
                                    .ifBlank {
                                        "Không rõ kênh"
                                    },
                            maxLines =
                                1,
                            overflow =
                                TextOverflow.Ellipsis,
                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.width(11.dp)
                    )

                    Surface(
                        modifier =
                            Modifier
                                .height(42.dp)
                                .appPressable(
                                    pressedScale =
                                        0.93f,
                                    haptic =
                                        true,
                                    onClick = {
                                        val opened =
                                            previewManager
                                                .openSaved(
                                                    result
                                                        .webpageUrl
                                                )

                                        if (!opened) {
                                            showPreviewChooser =
                                                true
                                        }
                                    }
                                ),
                        shape =
                            RoundedCornerShape(
                                13.dp
                            ),
                        color =
                            MaterialTheme
                                .colorScheme
                                .primary
                                .copy(
                                    alpha = 0.14f
                                ),
                        border =
                            BorderStroke(
                                1.dp,
                                MaterialTheme
                                    .colorScheme
                                    .primary
                                    .copy(
                                        alpha = 0.42f
                                    )
                            )
                    ) {
                        Row(
                            modifier =
                                Modifier.padding(
                                    horizontal = 13.dp
                                ),
                            horizontalArrangement =
                                Arrangement.spacedBy(5.dp),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Rounded
                                        .PlayArrow,
                                contentDescription =
                                    null,
                                modifier =
                                    Modifier.size(18.dp),
                                tint =
                                    MaterialTheme
                                        .colorScheme
                                        .primary
                            )

                            Text(
                                text =
                                    "Nghe",
                                style =
                                    MaterialTheme
                                        .typography
                                        .labelMedium,
                                fontWeight =
                                    FontWeight.Black
                            )
                        }
                    }
                }

                PrototypeDownloadButton(
                    enabled =
                        downloadEnabled,
                    onClick =
                        onDownload
                )
            }
        }
    }
}


@Composable
private fun PrototypeDownloadButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape =
        RoundedCornerShape(14.dp)

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(50.dp)
                .graphicsLayer {
                    alpha =
                        if (enabled) {
                            1f
                        } else {
                            0.44f
                        }
                }
                .clip(shape)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF62C9FF),
                            Color(0xFF73B1FF),
                            Color(0xFF9B82FF)
                        )
                    )
                )
                .appPressable(
                    enabled =
                        enabled,
                    pressedScale =
                        0.982f,
                    haptic =
                        true,
                    onClick =
                        onClick
                ),
        contentAlignment =
            Alignment.Center
    ) {
        Row(
            horizontalArrangement =
                Arrangement.spacedBy(8.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.Download,
                contentDescription =
                    null,
                modifier =
                    Modifier.size(19.dp),
                tint =
                    Color(0xFF071526)
            )

            Text(
                text =
                    "Tải MP3",
                style =
                    MaterialTheme
                        .typography
                        .labelLarge,
                fontWeight =
                    FontWeight.Black,
                color =
                    Color(0xFF071526)
            )
        }
    }
}

@Composable
private fun PrototypeLoadMoreFooter(
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
        modifier =
            Modifier
                .fillMaxWidth()
                .height(76.dp),
        contentAlignment =
            Alignment.Center
    ) {
        when {
            isLoading -> {
                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(9.dp),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(20.dp),
                        strokeWidth =
                            2.dp
                    )

                    Text(
                        text =
                            "Đang tải thêm..."
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
                                Icons.Rounded.Refresh,
                            contentDescription =
                                null
                        )

                        Text(
                            text =
                                "Tải tiếp"
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun PrototypeSearchError(
    message: String,
    onRetry: () -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(20.dp),
        color =
            MaterialTheme
                .colorScheme
                .errorContainer
                .copy(
                    alpha = 0.54f
                ),
        border =
            BorderStroke(
                1.dp,
                MaterialTheme
                    .colorScheme
                    .error
                    .copy(
                        alpha = 0.28f
                    )
            )
    ) {
        Column(
            modifier =
                Modifier.padding(16.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text =
                    message,
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onErrorContainer
            )

            TextButton(
                onClick =
                    onRetry
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.Refresh,
                    contentDescription =
                        null
                )

                Text(
                    text =
                        "Thử lại"
                )
            }
        }
    }
}


@Composable
private fun PrototypeSearchMessage(
    title: String,
    description: String
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(22.dp),
        color =
            MaterialTheme
                .colorScheme
                .surface
                .copy(
                    alpha = 0.60f
                ),
        border =
            BorderStroke(
                1.dp,
                MaterialTheme
                    .colorScheme
                    .outline
                    .copy(
                        alpha = 0.20f
                    )
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    horizontal = 18.dp,
                    vertical = 26.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier =
                    Modifier.size(46.dp),
                shape =
                    RoundedCornerShape(
                        15.dp
                    ),
                color =
                    MaterialTheme
                        .colorScheme
                        .primary
                        .copy(
                            alpha = 0.14f
                        )
            ) {
                Box(
                    modifier =
                        Modifier.fillMaxSize(),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Search,
                        contentDescription =
                            null,
                        tint =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }
            }

            Text(
                text =
                    title,
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                fontWeight =
                    FontWeight.Black
            )

            Text(
                text =
                    description,
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}