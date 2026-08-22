package com.ngoctien.getmp3.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.ui.design.appPressable
import com.ngoctien.getmp3.ui.theme.BrandBlue
import com.ngoctien.getmp3.ui.theme.BrandCyan
import com.ngoctien.getmp3.ui.theme.BrandPink
import com.ngoctien.getmp3.ui.theme.BrandViolet

@Composable
internal fun BentoHome(
    inboxFolderName: String,
    librarySongCount: Int,
    attentionCount: Int,
    duplicateCount: Int?,
    modifier: Modifier = Modifier,
    onOpenInbox: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenTagEditor: () -> Unit,
    onOpenCompare: () -> Unit,
    onOpenSettings: () -> Unit,
    onQuickDownloadFromClipboard:
        () -> Unit
) {
    LazyVerticalGrid(
        columns =
            GridCells.Fixed(2),
        modifier =
            modifier
                .fillMaxSize()
                .statusBarsPadding(),
        contentPadding =
            PaddingValues(16.dp),
        horizontalArrangement =
            Arrangement.spacedBy(12.dp),
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        item(
            span = {
                GridItemSpan(
                    maxLineSpan
                )
            }
        ) {
            AppPageHeader(
                title =
                    "GetMP3",
                subtitle =
                    "Music • Inbox • Library",
                icon =
                    Icons.Rounded.GraphicEq
            )
        }

        item(
            span = {
                GridItemSpan(
                    maxLineSpan
                )
            }
        ) {
            BentoTile(
                title =
                    "Tải nhạc",
                subtitle =
                    inboxFolderName
                        .ifBlank {
                            "Tìm và tải MP3 vào Music/Inbox"
                        },
                metric =
                    "",
                metricLabel =
                    "",
                icon =
                    Icons.Rounded.MusicNote,
                accent =
                    BrandBlue,
                minHeight =
                    154.dp,
                onClick =
                    onOpenInbox,
                showWaveformDecoration =
                    true,
                trailingContent = {
                    BentoQuickDownloadAction(
                        accent =
                            BrandBlue,
                        onClick =
                            onQuickDownloadFromClipboard
                    )
                }
            )
        }

        item {
            BentoTile(
                title =
                    "Library",
                subtitle =
                    "Nghe • tìm • quản lý",
                metric =
                    librarySongCount
                        .toString(),
                metricLabel =
                    "",
                icon =
                    Icons.Rounded.LibraryMusic,
                accent =
                    BrandCyan,
                minHeight =
                    164.dp,
                onClick =
                    onOpenLibrary,
                showWaveformDecoration =
                    true
            )
        }

        item {
            BentoTile(
                title =
                    "Lyrics",
                subtitle =
                    "Tìm, đọc và chèn lời",
                metric =
                    "LRC",
                metricLabel =
                    "local + online",
                icon =
                    Icons.Rounded.LibraryMusic,
                accent =
                    BrandViolet,
                minHeight =
                    164.dp,
                onClick =
                    onOpenLyrics
            )
        }

        item {
            BentoTile(
                title =
                    "Sửa thẻ",
                subtitle =
                    "Title • Artist • Album • Year",
                metric =
                    "ID3",
                metricLabel =
                    "chuẩn hóa metadata",
                icon =
                    Icons.Rounded.Edit,
                accent =
                    BrandPink,
                minHeight =
                    164.dp,
                onClick =
                    onOpenTagEditor
            )
        }

        item {
            val duplicateText =
                duplicateCount
                    ?.toString()
                    ?: "—"

            BentoTile(
                title =
                    "Sửa lỗi",
                subtitle =
                    "$duplicateText cặp trùng • $attentionCount file lỗi",
                metric =
                    duplicateCount
                        ?.let {
                            (
                                it +
                                    attentionCount
                                )
                                .toString()
                        }
                        ?: attentionCount
                            .toString(),
                metricLabel =
                    "mục cần sửa",
                icon =
                    Icons.Rounded.Sync,
                accent =
                    BrandViolet,
                minHeight =
                    164.dp,
                onClick =
                    onOpenCompare
            )
        }

        item(
            span = {
                GridItemSpan(
                    maxLineSpan
                )
            }
        ) {
            BentoTile(
                title =
                    "Cài đặt",
                subtitle =
                    "Bitrate • thư mục • giao diện • index",
                metric =
                    "→",
                metricLabel =
                    "tùy chỉnh GetMP3",
                icon =
                    Icons.Rounded.Settings,
                accent =
                    BrandBlue,
                minHeight =
                    116.dp,
                onClick =
                    onOpenSettings
            )
        }
    }
}

@Composable
private fun WorkflowStrip() {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(24.dp),
        color =
            MaterialTheme
                .colorScheme
                .surface
                .copy(
                    alpha = 0.94f
                ),
        border =
            BorderStroke(
                1.dp,
                MaterialTheme
                    .colorScheme
                    .outline
                    .copy(
                        alpha = 0.14f
                    )
            )
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            horizontalArrangement =
                Arrangement.spacedBy(8.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            WorkflowStep(
                text = "Inbox",
                accent = BrandBlue,
                modifier =
                    Modifier.weight(1f)
            )

            Text(
                "→",
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            WorkflowStep(
                text = "Chuẩn hóa",
                accent = BrandViolet,
                modifier =
                    Modifier.weight(1f)
            )

            Text(
                "→",
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            WorkflowStep(
                text = "Library",
                accent = BrandCyan,
                modifier =
                    Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WorkflowStep(
    text: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier =
            modifier,
        shape =
            CircleShape,
        color =
            accent.copy(
                alpha = 0.12f
            )
    ) {
        Text(
            text = text,
            modifier =
                Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 8.dp
                ),
            style =
                MaterialTheme
                    .typography
                    .labelMedium,
            fontWeight =
                FontWeight.SemiBold,
            maxLines =
                1
        )
    }
}

private val HomeWaveformMinScales =
    listOf(
        0.24f,
        0.40f,
        0.56f,
        0.34f,
        0.62f,
        0.42f,
        0.26f
    )

private val HomeWaveformMaxScales =
    listOf(
        0.62f,
        0.88f,
        1.00f,
        0.78f,
        0.96f,
        0.86f,
        0.58f
    )

@Composable
private fun HomeWaveformDecoration(
    accent: Color,
    modifier: Modifier = Modifier
) {
    val transition =
        rememberInfiniteTransition(
            label =
                "home-music-wave"
        )

    Row(
        modifier =
            modifier.graphicsLayer {
                alpha =
                    0.78f
            },
        horizontalArrangement =
            Arrangement.spacedBy(
                4.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        HomeWaveformMinScales
            .indices
            .forEach { index ->
                val scale by
                    transition.animateFloat(
                        initialValue =
                            HomeWaveformMinScales[
                                index
                            ],
                        targetValue =
                            HomeWaveformMaxScales[
                                index
                            ],
                        animationSpec =
                            infiniteRepeatable(
                                animation =
                                    tween(
                                        durationMillis =
                                            520 +
                                                (
                                                    index *
                                                        85
                                                    ),
                                        easing =
                                            FastOutSlowInEasing
                                    ),
                                repeatMode =
                                    RepeatMode.Reverse
                            ),
                        label =
                            "home-wave-$index"
                    )

                Box(
                    modifier =
                        Modifier
                            .width(
                                5.dp
                            )
                            .height(
                                42.dp
                            )
                            .graphicsLayer {
                                scaleY =
                                    scale

                                transformOrigin =
                                    TransformOrigin(
                                        pivotFractionX =
                                            0.5f,
                                        pivotFractionY =
                                            0.5f
                                    )
                            }
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        BrandCyan.copy(
                                            alpha =
                                                0.92f
                                        ),
                                        accent.copy(
                                            alpha =
                                                0.96f
                                        ),
                                        BrandViolet.copy(
                                            alpha =
                                                0.58f
                                        )
                                    )
                                ),
                                CircleShape
                            )
                )
            }
    }
}

@Composable
private fun BentoQuickDownloadAction(
    accent: Color,
    onClick: () -> Unit
) {
    val transition =
        rememberInfiniteTransition(
            label =
                "home-quick-download-pulse"
        )

    val pulse by
        transition.animateFloat(
            initialValue =
                0f,
            targetValue =
                0f,
            animationSpec =
                infiniteRepeatable(
                    animation =
                        keyframes {
                            durationMillis =
                                AppMotion
                                    .ActionPulseMillis

                            0f at 0
                            0f at 4_000
                            1f at 4_140
                            0f at 4_300
                            0.72f at 4_450
                            0f at 4_620
                            0f at AppMotion.ActionPulseMillis
                        }
                ),
            label =
                "home-quick-download-pulse-value"
        )

    val shape =
        CircleShape

    Surface(
        modifier =
            Modifier
                .size(48.dp)
                .graphicsLayer {
                    val scale =
                        1f +
                            (
                                0.10f *
                                    pulse
                                )

                    scaleX =
                        scale

                    scaleY =
                        scale

                    alpha =
                        1f -
                            (
                                0.03f *
                                    pulse
                                )
                }
                .appPressable(
                    haptic =
                        true,
                    onClick =
                        onClick
                ),
        shape =
            shape,
        color =
            accent.copy(
                alpha = 0.90f
            ),
        contentColor =
            Color.White,
        tonalElevation =
            8.dp,
        shadowElevation =
            6.dp,
        border =
            BorderStroke(
                1.dp,
                Color.White.copy(
                    alpha = 0.34f
                )
            )
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color.White.copy(
                                    alpha = 0.22f
                                ),
                                accent.copy(
                                    alpha = 0.78f
                                ),
                                BrandViolet.copy(
                                    alpha = 0.36f
                                )
                            )
                        ),
                        CircleShape
                    ),
            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.Download,
                contentDescription =
                    "Tải nhanh link đã sao chép",
                modifier =
                    Modifier.size(26.dp),
                tint =
                    Color.White
            )
        }
    }
}

@Composable
private fun BentoTile(
    title: String,
    subtitle: String,
    metric: String,
    metricLabel: String,
    icon: ImageVector,
    accent: Color,
    minHeight: Dp,
    onClick: () -> Unit,
    showWaveformDecoration: Boolean = false,
    trailingContent:
        (@Composable () -> Unit)? = null
) {
    val shape =
        RoundedCornerShape(34.dp)

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(
                    min = minHeight
                )
                .shadow(
                    elevation = 20.dp,
                    shape = shape,
                    clip = false
                )
                .bouncyClickable(
                    pressedScale =
                        0.965f,
                    onClick =
                        onClick
                ),
        shape =
            shape,
        color =
            MaterialTheme
                .colorScheme
                .surface,
        contentColor =
            MaterialTheme
                .colorScheme
                .onSurface,
        tonalElevation =
            8.dp,
        shadowElevation =
            6.dp,
        border =
            BorderStroke(
                1.dp,
                accent.copy(
                    alpha = 0.48f
                )
            )
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                accent.copy(
                                    alpha = 0.30f
                                ),
                                MaterialTheme
                                    .colorScheme
                                    .surfaceVariant
                                    .copy(
                                        alpha = 0.14f
                                    ),
                                Color.Transparent
                            )
                        ),
                        RoundedCornerShape(
                            28.dp
                        )
                    )
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(
                                    alpha = 0.12f
                                ),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(
                                    alpha = 0.22f
                                )
                            )
                        ),
                        RoundedCornerShape(
                            28.dp
                        )
                    )
                    .padding(16.dp)
        ) {
            if (showWaveformDecoration) {
                HomeWaveformDecoration(
                    accent =
                        accent,
                    modifier =
                        Modifier
                            .align(
                                Alignment.BottomEnd
                            )
                            .padding(
                                end = 4.dp,
                                bottom = 3.dp
                            )
                )
            }

            Column(
                Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Surface(
                        modifier =
                            Modifier
                                .size(48.dp)
                                .shadow(
                                    elevation = 10.dp,
                                    shape = RoundedCornerShape(17.dp),
                                    clip = false
                                ),
                        shape =
                            RoundedCornerShape(
                                17.dp
                            ),
                        color =
                            accent.copy(
                                alpha = 0.18f
                            ),
                        tonalElevation =
                            6.dp,
                        shadowElevation =
                            5.dp,
                        border =
                            BorderStroke(
                                1.dp,
                                accent.copy(
                                    alpha = 0.55f
                                )
                            )
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                Color.White.copy(
                                                    alpha = 0.12f
                                                ),
                                                accent.copy(
                                                    alpha = 0.50f
                                                ),
                                                accent.copy(
                                                    alpha = 0.12f
                                                )
                                            )
                                        )
                                    ),
                            contentAlignment =
                                Alignment.Center
                        ) {
                            Icon(
                                imageVector =
                                    icon,
                                contentDescription =
                                    null,
                                tint =
                                    accent,
                                modifier =
                                    Modifier.size(23.dp)
                            )
                        }
                    }

                    Spacer(
                        Modifier.weight(1f)
                    )

                    if (trailingContent != null) {
                        trailingContent()
                    }
                    else {
                        AnimatedContent(
                            targetState =
                                metric,
                            transitionSpec = {
                                fadeIn(
                                    tween(
                                        AppMotion
                                            .ContentMillis
                                    )
                                )
                                    .togetherWith(
                                        fadeOut(
                                            tween(
                                                AppMotion
                                                    .QuickMillis
                                            )
                                        )
                                    )
                            },
                            label =
                                "metric-$title"
                        ) { value ->

                            Surface(
                                shape =
                                    CircleShape,
                                color =
                                    accent.copy(
                                        alpha = 0.14f
                                    ),
                                tonalElevation =
                                    3.dp,
                                shadowElevation =
                                    4.dp,
                                border =
                                    BorderStroke(
                                        1.dp,
                                        accent.copy(
                                            alpha = 0.34f
                                        )
                                    )
                            ) {
                                Text(
                                    text =
                                        value,
                                    modifier =
                                        Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 5.dp
                                        ),
                                    style =
                                        MaterialTheme
                                            .typography
                                            .titleMedium,
                                    fontWeight =
                                        FontWeight.Bold,
                                    color =
                                        accent,
                                    maxLines =
                                        1
                                )
                            }
                        }
                    }
                }

                Spacer(
                    Modifier.height(14.dp)
                )

                Text(
                    text =
                        title,
                    style =
                        MaterialTheme
                            .typography
                            .titleLarge,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurface,
                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    Modifier.height(4.dp)
                )

                Text(
                    text =
                        subtitle,
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurface
                            .copy(
                                alpha = 0.94f
                            ),
                    maxLines =
                        2,
                    overflow =
                        TextOverflow.Ellipsis
                )

                if (metricLabel.isNotBlank()) {
                    Spacer(
                        Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            metricLabel,
                        style =
                            MaterialTheme
                                .typography
                                .labelMedium,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurface
                                .copy(
                                    alpha = 0.84f
                                )
                    )
                }
            }
        }
    }
}
