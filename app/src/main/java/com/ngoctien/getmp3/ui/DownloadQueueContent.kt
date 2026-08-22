package com.ngoctien.getmp3.ui

import androidx.compose.material.icons.automirrored.rounded.List

import com.ngoctien.getmp3.ui.components.AppExpandableCard

import com.ngoctien.getmp3.ui.components.AppIconActionButton

import com.ngoctien.getmp3.ui.components.AppCard

import com.ngoctien.getmp3.ui.components.AppAnimatedStatusText

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
internal fun DownloadQueueContent(
    state: DownloadScreenUiState,
    onCancelJob: (String) -> Unit,
    onRetryJob:
        (DownloadJobEntity) -> Unit,
    onClearHistory: () -> Unit
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
            state.activeJobs.isEmpty() &&
            state.recentJobs.isEmpty()
        ) {
            item {
                AppEmptyState(
                    icon =
                        Icons.AutoMirrored.Rounded.List,

                    title =
                        "Hàng đợi đang trống",

                    description =
                        "Tìm một video hoặc dán liên kết để tải."
                )
            }

            return@LazyColumn
        }

        if (
            state.activeJobs.isNotEmpty()
        ) {
            item {
                QueueSectionHeader(
                    title = "Đang tải",
                    count =
                        state.activeJobs
                            .size
                )
            }

            items(
                items =
                    state.activeJobs,

                key = {
                    it.id
                }
            ) { job ->
                ActiveQueueItem(
                    job = job,
                    onCancel = {
                        onCancelJob(
                            job.id
                        )
                    }
                )
            }
        }

        if (
            state.recentJobs.isNotEmpty()
        ) {
            item {
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    QueueSectionHeader(
                        title = "Gần đây",
                        count =
                            state.recentJobs
                                .size
                    )

                    Spacer(
                        modifier =
                            Modifier.weight(1f)
                    )

                    TextButton(
                        onClick =
                            onClearHistory
                    ) {
                        Icon(
                            imageVector =
                                Icons.Rounded
                                    .DeleteSweep,

                            contentDescription =
                                null,

                            modifier =
                                Modifier.size(18.dp)
                        )

                        Spacer(
                            modifier =
                                Modifier.width(5.dp)
                        )

                        Text("Xóa")
                    }
                }
            }

            items(
                items =
                    state.recentJobs
                        .take(20),

                key = {
                    it.id
                }
            ) { job ->
                RecentQueueItem(
                    job = job,
                    onRetry = {
                        onRetryJob(job)
                    }
                )
            }
        }
    }
}

@Composable
private fun QueueSectionHeader(
    title: String,
    count: Int
) {
    Row(
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Text(
            text = title,

            style =
                MaterialTheme
                    .typography
                    .titleLarge,

            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.width(8.dp)
        )

        Surface(
            shape = CircleShape,

            color =
                MaterialTheme
                    .colorScheme
                    .primaryContainer
        ) {
            Text(
                text =
                    count.toString(),

                modifier =
                    Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 3.dp
                    )
            )
        }
    }
}

@Composable
private fun ActiveQueueItem(
    job: DownloadJobEntity,
    onCancel: () -> Unit
) {
    AppCard(
        modifier =
            Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                QueueCover(
                    job
                )

                Spacer(
                    modifier =
                        Modifier.width(
                            11.dp
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
                            job.title,

                        maxLines =
                            2,

                        overflow =
                            TextOverflow
                                .Ellipsis,

                        fontWeight =
                            FontWeight
                                .SemiBold
                    )

                    AppAnimatedStatusText(
                        text =
                            job.statusMessage
                                ?.takeIf(
                                    String::isNotBlank
                                )
                                ?: job.artist,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }

                Text(
                    text =
                        "${job.overallProgress.coerceIn(0, 100)}%",

                    color =
                        MaterialTheme
                            .colorScheme
                            .primary,

                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.width(
                            6.dp
                        )
                )

                AppIconActionButton(
                    icon =
                        Icons.Rounded.Close,

                    contentDescription =
                        "Hủy tải",

                    onClick =
                        onCancel,

                    haptic =
                        true
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )

            LinearProgressIndicator(
                progress = {
                    job.overallProgress
                        .coerceIn(
                            0,
                            100
                        ) /
                        100f
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            7.dp
                        )
                        .clip(
                            CircleShape
                        )
            )
        }
    }
}

@Composable
private fun RecentQueueItem(
    job: DownloadJobEntity,
    onRetry: () -> Unit
) {
    val context =
        LocalContext.current

    AppExpandableCard(
        modifier =
            Modifier.fillMaxWidth(),

        haptic =
            false,

        header = {
            Row(
                modifier =
                    Modifier.padding(
                        12.dp
                    ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                QueueCover(
                    job
                )

                Spacer(
                    modifier =
                        Modifier.width(
                            11.dp
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
                            job.title,

                        maxLines =
                            1,

                        overflow =
                            TextOverflow
                                .Ellipsis,

                        fontWeight =
                            FontWeight
                                .SemiBold
                    )

                    Text(
                        text =
                            job.artist,

                        maxLines =
                            1,

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
                                .onSurfaceVariant
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                3.dp
                            )
                    )

                    AppAnimatedStatusText(
                        text =
                            job.statusMessage
                                ?.takeIf(
                                    String::isNotBlank
                                )
                                ?: statusText(
                                    job.status
                                ),

                        color =
                            statusColor(
                                job.status
                            ),

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall
                    )
                }

                when (
                    job.status
                ) {

                    DownloadStatus.COMPLETED -> {
                        AppIconActionButton(
                            icon =
                                Icons.Rounded
                                    .PlayArrow,

                            contentDescription =
                                "Mở bằng trình phát mặc định",

                            enabled =
                                !job.outputUri
                                    .isNullOrBlank(),

                            onClick = {
                                openAudioWithDefaultPlayer(
                                    context =
                                        context,

                                    outputUri =
                                        job.outputUri
                                )
                            }
                        )
                    }

                    DownloadStatus.FAILED,
                    DownloadStatus.CANCELLED -> {
                        AppIconActionButton(
                            icon =
                                Icons.Rounded
                                    .Refresh,

                            contentDescription =
                                "Tải lại",

                            onClick =
                                onRetry
                        )
                    }

                    else ->
                        Unit
                }
            }
        },

        expandedContent = {

            Column(
                modifier =
                    Modifier.padding(
                        start =
                            12.dp,

                        end =
                            12.dp,

                        bottom =
                            12.dp
                    )
            ) {
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

                job.warningMessage
                    ?.takeIf(
                        String::isNotBlank
                    )
                    ?.let {
                            warning ->

                        Text(
                            text =
                                warning,

                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .tertiary
                        )

                        Spacer(
                            modifier =
                                Modifier.height(
                                    5.dp
                                )
                        )
                    }

                job.errorMessage
                    ?.takeIf(
                        String::isNotBlank
                    )
                    ?.let {
                            error ->

                        Text(
                            text =
                                error,

                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .error
                        )
                    }

                if (
                    job.warningMessage
                        .isNullOrBlank() &&
                    job.errorMessage
                        .isNullOrBlank()
                ) {
                    Text(
                        text =
                            "Chạm lại thẻ để thu gọn.",

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
    )
}

@Composable
private fun QueueCover(
    job: DownloadJobEntity
) {
    val thumbnail =
        job.thumbnailUrl
            ?.takeIf {
                it.isNotBlank()
            }

    if (thumbnail != null) {
        AsyncImage(
            model = thumbnail,

            contentDescription =
                "Ảnh bìa ${job.title}",

            modifier = Modifier
                .size(58.dp)
                .clip(
                    RoundedCornerShape(
                        15.dp
                    )
                ),

            contentScale =
                ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(
                    RoundedCornerShape(
                        15.dp
                    )
                )
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme
                                .colorScheme
                                .primaryContainer,

                            MaterialTheme
                                .colorScheme
                                .secondaryContainer
                        )
                    )
                ),

            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                imageVector =
                    Icons.Rounded
                        .MusicNote,

                contentDescription =
                    null
            )
        }
    }
}

private fun statusText(
    status: DownloadStatus
): String {
    return when (status) {
        DownloadStatus.QUEUED ->
            "Đang chờ"

        DownloadStatus.EXTRACTING ->
            "Đang đọc thông tin"

        DownloadStatus.DOWNLOADING ->
            "Đang tải"

        DownloadStatus.CONVERTING ->
            "Đang chuyển MP3"

        DownloadStatus.TAGGING ->
            "Đang ghi metadata"

        DownloadStatus.SAVING ->
            "Đang lưu"

        DownloadStatus.COMPLETED ->
            "Hoàn tất"

        DownloadStatus.FAILED ->
            "Tải thất bại"

        DownloadStatus.CANCELLED ->
            "Đã hủy"
    }
}

@Composable
private fun statusColor(
    status: DownloadStatus
): Color {
    return when (status) {
        DownloadStatus.COMPLETED ->
            MaterialTheme
                .colorScheme
                .primary

        DownloadStatus.FAILED ->
            MaterialTheme
                .colorScheme
                .error

        DownloadStatus.CANCELLED ->
            MaterialTheme
                .colorScheme
                .onSurfaceVariant

        else ->
            MaterialTheme
                .colorScheme
                .secondary
    }
}
