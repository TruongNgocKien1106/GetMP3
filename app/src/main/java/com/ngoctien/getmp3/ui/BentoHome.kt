package com.ngoctien.getmp3.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.ui.design.appPressable
import com.ngoctien.getmp3.ui.theme.BrandBlue
import com.ngoctien.getmp3.ui.theme.BrandCyan
import com.ngoctien.getmp3.ui.theme.BrandViolet
import java.time.LocalTime

@Composable
internal fun BentoHome(
    inboxFolderName: String,
    librarySongCount: Int,
    attentionCount: Int,
    duplicateCount: Int?,
    isLibrarySyncing: Boolean,
    modifier: Modifier = Modifier,
    onOpenInbox: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenTagEditor: () -> Unit,
    onOpenCompare: () -> Unit,
    onOpenSettings: () -> Unit,
    onSyncLibrary: () -> Unit,
    onQuickDownloadFromClipboard: () -> Unit
) {
    val greeting =
        remember {
            greetingForHour(
                LocalTime.now().hour
            )
        }

    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .statusBarsPadding(),
        contentPadding =
            PaddingValues(
                start = 16.dp,
                top = 14.dp,
                end = 16.dp,
                bottom = 28.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {
        item {
            HomeHeader(
                greeting = greeting,
                onOpenSettings = onOpenSettings
            )
        }

        item {
            DownloadHero(
                inboxFolderName = inboxFolderName,
                onOpenInbox = onOpenInbox,
                onQuickDownloadFromClipboard =
                    onQuickDownloadFromClipboard
            )
        }

        item {
            HomeSectionTitle(
                text = "Khám phá"
            )
        }

        item {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    ExploreCard(
                        title = "LIBRARY",
                        metric =
                            librarySongCount.toString(),
                        metricLabel =
                            "Bài hát",
                        icon =
                            Icons.Rounded.LibraryMusic,
                        accent =
                            Color(0xFF5FB2FF),
                        background =
                            listOf(
                                Color(0xFF174B82),
                                Color(0xFF15345F),
                                Color(0xFF122C51)
                            ),
                        modifier =
                            Modifier.weight(1f),
                        onClick =
                            onOpenLibrary
                    )

                    ExploreCard(
                        title = "LYRICS",
                        metric = null,
                        metricLabel = null,
                        icon =
                            Icons.Rounded.MusicNote,
                        accent =
                            Color(0xFFC27BFF),
                        background =
                            listOf(
                                Color(0xFF5B2D83),
                                Color(0xFF422267),
                                Color(0xFF352054)
                            ),
                        modifier =
                            Modifier.weight(1f),
                        onClick =
                            onOpenLyrics
                    )
                }

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    ExploreCard(
                        title = "TAGS",
                        metric =
                            attentionCount.toString(),
                        metricLabel =
                            "Cần sửa",
                        icon =
                            Icons.Rounded.Edit,
                        accent =
                            Color(0xFFFFC45C),
                        background =
                            listOf(
                                Color(0xFF263D5E),
                                Color(0xFF202F49),
                                Color(0xFF19283E)
                            ),
                        modifier =
                            Modifier.weight(1f),
                        onClick =
                            onOpenTagEditor
                    )

                    ExploreCard(
                        title = "DUPLICATES",
                        metric =
                            duplicateCount
                                ?.toString()
                                ?: "—",
                        metricLabel =
                            "Bài trùng",
                        icon =
                            Icons.Rounded.Sync,
                        accent =
                            Color(0xFFE77CFF),
                        background =
                            listOf(
                                Color(0xFF54306F),
                                Color(0xFF3F255B),
                                Color(0xFF342047)
                            ),
                        modifier =
                            Modifier.weight(1f),
                        onClick =
                            onOpenCompare
                    )
                }
            }
        }

        item {
            HomeSectionTitle(
                text = "Công cụ nhanh"
            )
        }

        item {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                QuickToolCard(
                    title =
                        if (isLibrarySyncing) {
                            "Đang đồng bộ"
                        } else {
                            "Đồng bộ"
                        },
                    subtitle =
                        if (isLibrarySyncing) {
                            "Đang quét Library"
                        } else {
                            "Quét lại Library"
                        },
                    icon =
                        Icons.Rounded.Sync,
                    accent =
                        BrandBlue,
                    enabled =
                        !isLibrarySyncing,
                    modifier =
                        Modifier.weight(1f),
                    onClick =
                        onSyncLibrary
                )

                QuickToolCard(
                    title =
                        "Cài đặt",
                    subtitle =
                        "Theme · bitrate · storage",
                    icon =
                        Icons.Rounded.Settings,
                    accent =
                        Color(0xFFFFA95C),
                    enabled =
                        true,
                    modifier =
                        Modifier.weight(1f),
                    onClick =
                        onOpenSettings
                )
            }
        }
    }
}

@Composable
private fun HomeHeader(
    greeting: String,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween,
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement =
                Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text =
                    greeting,
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

            Text(
                text =
                    "GetMP3",
                style =
                    MaterialTheme
                        .typography
                        .headlineLarge,
                fontWeight =
                    FontWeight.Black,
                color =
                    MaterialTheme
                        .colorScheme
                        .onBackground
            )
        }

        Surface(
            modifier =
                Modifier
                    .size(46.dp)
                    .appPressable(
                        haptic = true,
                        onClick =
                            onOpenSettings
                    ),
            shape =
                CircleShape,
            color =
                MaterialTheme
                    .colorScheme
                    .surface
                    .copy(
                        alpha = 0.88f
                    ),
            border =
                BorderStroke(
                    1.dp,
                    MaterialTheme
                        .colorScheme
                        .outline
                        .copy(
                            alpha = 0.16f
                        )
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
                        Icons.Rounded.Settings,
                    contentDescription =
                        "Cài đặt",
                    modifier =
                        Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun DownloadHero(
    inboxFolderName: String,
    onOpenInbox: () -> Unit,
    onQuickDownloadFromClipboard: () -> Unit
) {
    val shape =
        RoundedCornerShape(28.dp)

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(
                    min = 144.dp
                )
                .appPressable(
                    pressedScale = 0.985f,
                    haptic = true,
                    onClick =
                        onOpenInbox
                ),
        shape =
            shape,
        color =
            Color.Transparent,
        border =
            BorderStroke(
                1.dp,
                Color(0xFF6BA7FF)
                    .copy(
                        alpha = 0.34f
                    )
            )
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF102B55),
                                Color(0xFF182A55),
                                Color(0xFF312259)
                            )
                        ),
                        shape
                    )
                    .padding(
                        horizontal = 16.dp,
                        vertical = 16.dp
                    )
        ) {
            HomeHeroWave(
                modifier =
                    Modifier
                        .align(
                            Alignment.BottomEnd
                        )
                        .padding(
                            end = 50.dp,
                            bottom = 8.dp
                        )
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(14.dp),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column(
                    modifier =
                        Modifier.weight(1f),
                    verticalArrangement =
                        Arrangement.spacedBy(7.dp)
                ) {
                    Surface(
                        modifier =
                            Modifier.size(36.dp),
                        shape =
                            RoundedCornerShape(12.dp),
                        color =
                            Color.White.copy(
                                alpha = 0.10f
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
                                    Icons.Rounded.Download,
                                contentDescription =
                                    null,
                                tint =
                                    Color(0xFFDCEAFF),
                                modifier =
                                    Modifier.size(20.dp)
                            )
                        }
                    }

                    Text(
                        text =
                            "QUICK ACTION",
                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,
                        fontWeight =
                            FontWeight.Bold,
                        color =
                            Color(0xFFABC9FF)
                    )

                    Text(
                        text =
                            "Tải nhạc",
                        style =
                            MaterialTheme
                                .typography
                                .headlineSmall,
                        fontWeight =
                            FontWeight.Black,
                        color =
                            Color.White
                    )

                    Text(
                        text =
                            "Search YouTube hoặc dùng URL vừa copy.",
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,
                        color =
                            Color.White.copy(
                                alpha = 0.78f
                            ),
                        maxLines =
                            2,
                        overflow =
                            TextOverflow.Ellipsis
                    )

                    Text(
                        text =
                            inboxFolderName
                                .ifBlank {
                                    "Music/Inbox"
                                },
                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,
                        color =
                            Color.White.copy(
                                alpha = 0.54f
                            ),
                        maxLines =
                            1,
                        overflow =
                            TextOverflow.Ellipsis
                    )
                }

                Surface(
                    modifier =
                        Modifier
                            .size(50.dp)
                            .appPressable(
                                pressedScale = 0.92f,
                                haptic = true,
                                onClick =
                                    onQuickDownloadFromClipboard
                            ),
                    shape =
                        RoundedCornerShape(17.dp),
                    color =
                        Color(0xFF5F86FF)
                            .copy(
                                alpha = 0.92f
                            ),
                    border =
                        BorderStroke(
                            1.dp,
                            Color.White.copy(
                                alpha = 0.22f
                            )
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
                                Icons.Rounded.Download,
                            contentDescription =
                                "Tải URL đã sao chép",
                            tint =
                                Color.White,
                            modifier =
                                Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeroWave(
    modifier: Modifier = Modifier
) {
    val transition =
        rememberInfiniteTransition(
            label =
                "home-prototype-wave"
        )

    val minScales =
        listOf(
            0.30f,
            0.52f,
            0.72f,
            0.44f,
            0.82f,
            0.58f
        )

    Row(
        modifier =
            modifier.graphicsLayer {
                alpha =
                    0.56f
            },
        horizontalArrangement =
            Arrangement.spacedBy(4.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        minScales.forEachIndexed {
                index,
                minScale ->

            val scale by
                transition.animateFloat(
                    initialValue =
                        minScale,
                    targetValue =
                        1f,
                    animationSpec =
                        infiniteRepeatable(
                            animation =
                                tween(
                                    durationMillis =
                                        560 +
                                            index * 90,
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
                        .width(4.dp)
                        .height(42.dp)
                        .graphicsLayer {
                            scaleY =
                                scale

                            transformOrigin =
                                TransformOrigin(
                                    0.5f,
                                    0.5f
                                )
                        }
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    BrandCyan,
                                    BrandBlue,
                                    BrandViolet
                                )
                            ),
                            CircleShape
                        )
            )
        }
    }
}

@Composable
private fun HomeSectionTitle(
    text: String
) {
    Text(
        text =
            text,
        style =
            MaterialTheme
                .typography
                .titleMedium,
        fontWeight =
            FontWeight.Bold,
        color =
            MaterialTheme
                .colorScheme
                .onBackground
    )
}

@Composable
private fun ExploreCard(
    title: String,
    metric: String?,
    metricLabel: String?,
    icon: ImageVector,
    accent: Color,
    background: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape =
        RoundedCornerShape(24.dp)

    Surface(
        modifier =
            modifier
                .heightIn(
                    min = 196.dp
                )
                .appPressable(
                    pressedScale = 0.97f,
                    haptic = true,
                    onClick =
                        onClick
                ),
        shape =
            shape,
        color =
            Color.Transparent,
        border =
            BorderStroke(
                1.dp,
                accent.copy(
                    alpha = 0.30f
                )
            )
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            background
                        ),
                        shape
                    )
                    .padding(18.dp)
        ) {
            Box(
                modifier =
                    Modifier
                        .size(118.dp)
                        .align(
                            Alignment.TopEnd
                        )
                        .graphicsLayer {
                            translationX =
                                42f

                            translationY =
                                -58f

                            alpha =
                                0.22f
                        }
                        .background(
                            accent,
                            CircleShape
                        )
            )

            Column(
                modifier =
                    Modifier.fillMaxSize(),
                verticalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Surface(
                        modifier =
                            Modifier.size(46.dp),
                        shape =
                            RoundedCornerShape(15.dp),
                        color =
                            accent.copy(
                                alpha = 0.18f
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
                                    icon,
                                contentDescription =
                                    null,
                                tint =
                                    Color.White,
                                modifier =
                                    Modifier.size(23.dp)
                            )
                        }
                    }

                    Text(
                        text =
                            "→",
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                        fontWeight =
                            FontWeight.Bold,
                        color =
                            Color.White.copy(
                                alpha = 0.70f
                            )
                    )
                }

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text =
                            title,
                        style =
                            MaterialTheme
                                .typography
                                .labelLarge,
                        fontWeight =
                            FontWeight.Black,
                        color =
                            Color.White
                    )

                    if (metric != null) {
                        AnimatedContent(
                            targetState =
                                metric,
                            transitionSpec = {
                                fadeIn(
                                    animationSpec =
                                        tween(180)
                                )
                                    .togetherWith(
                                        fadeOut(
                                            animationSpec =
                                                tween(120)
                                        )
                                    )
                            },
                            label =
                                "home-metric-$title"
                        ) { value ->
                            Text(
                                text =
                                    value,
                                style =
                                    MaterialTheme
                                        .typography
                                        .headlineMedium,
                                fontWeight =
                                    FontWeight.Black,
                                color =
                                    Color.White
                            )
                        }
                    }

                    if (
                        !metricLabel
                            .isNullOrBlank()
                    ) {
                        Text(
                            text =
                                metricLabel,
                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall,
                            color =
                                Color.White.copy(
                                    alpha = 0.66f
                                ),
                            maxLines =
                                1,
                            overflow =
                                TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape =
        RoundedCornerShape(22.dp)

    Surface(
        modifier =
            modifier
                .heightIn(
                    min = 116.dp
                )
                .appPressable(
                    enabled =
                        enabled,
                    pressedScale =
                        0.97f,
                    haptic =
                        true,
                    onClick =
                        onClick
                ),
        shape =
            shape,
        color =
            MaterialTheme
                .colorScheme
                .surface
                .copy(
                    alpha =
                        if (enabled) {
                            0.94f
                        } else {
                            0.72f
                        }
                ),
        border =
            BorderStroke(
                1.dp,
                accent.copy(
                    alpha = 0.20f
                )
            )
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Surface(
                    modifier =
                        Modifier.size(38.dp),
                    shape =
                        RoundedCornerShape(13.dp),
                    color =
                        accent.copy(
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
                                icon,
                            contentDescription =
                                null,
                            tint =
                                accent,
                            modifier =
                                Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text =
                        "→",
                    style =
                        MaterialTheme
                            .typography
                            .labelLarge,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                            .copy(
                                alpha =
                                    if (enabled) {
                                        0.72f
                                    } else {
                                        0.38f
                                    }
                            )
                )
            }

            Spacer(
                modifier =
                    Modifier.height(2.dp)
            )

            Text(
                text =
                    title,
                style =
                    MaterialTheme
                        .typography
                        .titleSmall,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    subtitle,
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
                maxLines =
                    2,
                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

private fun greetingForHour(
    hour: Int
): String =
    when (hour) {
        in 5..11 ->
            "GOOD MORNING"

        in 12..16 ->
            "GOOD AFTERNOON"

        in 17..21 ->
            "GOOD EVENING"

        else ->
            "GOOD NIGHT"
    }