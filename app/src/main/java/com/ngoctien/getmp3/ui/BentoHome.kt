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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

private val HomeTextPrimary =
    Color(0xFFF6F8FF)

private val HomeTextSecondary =
    Color(0xFFBAC6DA)

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
                start = 18.dp,
                top = 10.dp,
                end = 18.dp,
                bottom = 28.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(14.dp)
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
                    Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
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
                            Color(0xFF62B7FF),
                        background =
                            listOf(
                                Color(0xFF0F4279),
                                Color(0xFF11345F),
                                Color(0xFF102846)
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
                                Color(0xFF51267A),
                                Color(0xFF3C1E60),
                                Color(0xFF2C183F)
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
                        Arrangement.spacedBy(10.dp)
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
                            Color(0xFFFFC84C),
                        background =
                            listOf(
                                Color(0xFF1A304C),
                                Color(0xFF17283F),
                                Color(0xFF142235)
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
                            Color(0xFFFF77A8),
                        background =
                            listOf(
                                Color(0xFF4C245B),
                                Color(0xFF3A1D47),
                                Color(0xFF2D1738)
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
                    Arrangement.spacedBy(10.dp)
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
                Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = greeting,
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                fontWeight =
                    FontWeight.Bold,
                color =
                    Color(0xFF82A9FF)
            )

            Text(
                text = "GetMP3",
                style =
                    MaterialTheme
                        .typography
                        .headlineLarge,
                fontWeight =
                    FontWeight.Black,
                color =
                    HomeTextPrimary
            )
        }

        Surface(
            modifier =
                Modifier
                    .size(46.dp)
                    .appPressable(
                        pressedScale = 0.94f,
                        haptic = true,
                        onClick = onOpenSettings
                    ),
            shape =
                RoundedCornerShape(15.dp),
            color =
                Color(0xFF0D1A2D),
            border =
                BorderStroke(
                    1.dp,
                    Color(0xFF6090E8)
                        .copy(
                            alpha = 0.35f
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
                        Modifier.size(21.dp),
                    tint =
                        Color(0xFFDDE8FF)
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
        RoundedCornerShape(25.dp)

    val targetFolder =
        inboxFolderName
            .ifBlank {
                "Music/Inbox"
            }

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(142.dp)
                .appPressable(
                    pressedScale = 0.982f,
                    haptic = true,
                    onClick = onOpenInbox
                ),
        shape =
            shape,
        color =
            Color.Transparent,
        border =
            BorderStroke(
                1.dp,
                Color(0xFF739BFF)
                    .copy(
                        alpha = 0.58f
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
                                Color(0xFF143A78),
                                Color(0xFF243C86),
                                Color(0xFF392270)
                            )
                        ),
                        shape
                    )
                    .padding(
                        horizontal = 17.dp,
                        vertical = 15.dp
                    )
        ) {
            Column(
                modifier =
                    Modifier.align(
                        Alignment.TopStart
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text =
                        "QUICK DOWNLOAD",
                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        Color(0xFFB3CCFF)
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
                        "Dán link YouTube để tải.",
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,
                    color =
                        Color.White.copy(
                            alpha = 0.86f
                        )
                )
            }

            HomeHeroWave(
                modifier =
                    Modifier
                        .align(
                            Alignment.BottomStart
                        )
                        .padding(
                            start = 8.dp,
                            bottom = 3.dp
                        )
            )

            Surface(
                modifier =
                    Modifier
                        .size(50.dp)
                        .align(
                            Alignment.CenterEnd
                        )
                        .appPressable(
                            pressedScale = 0.92f,
                            haptic = true,
                            onClick =
                                onQuickDownloadFromClipboard
                        ),
                shape =
                    RoundedCornerShape(16.dp),
                color =
                    Color(0xFF3857A2),
                border =
                    BorderStroke(
                        1.dp,
                        Color(0xFF9AB9FF)
                            .copy(
                                alpha = 0.52f
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
                            "Tải vào $targetFolder",
                        modifier =
                            Modifier.size(23.dp),
                        tint =
                            Color.White
                    )
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
        text = text,
        style =
            MaterialTheme
                .typography
                .titleMedium,
        fontWeight =
            FontWeight.Bold,
        color =
            HomeTextPrimary
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
        RoundedCornerShape(22.dp)

    val metricColor =
        when (title) {
            "TAGS" ->
                Color(0xFFFFC84C)

            "DUPLICATES" ->
                Color(0xFFFF77A8)

            else ->
                HomeTextPrimary
        }

    Surface(
        modifier =
            modifier
                .height(170.dp)
                .appPressable(
                    pressedScale = 0.965f,
                    haptic = true,
                    onClick = onClick
                ),
        shape =
            shape,
        color =
            Color.Transparent,
        border =
            BorderStroke(
                1.dp,
                accent.copy(
                    alpha = 0.38f
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
                    .padding(14.dp)
        ) {
            Box(
                modifier =
                    Modifier
                        .size(80.dp)
                        .align(
                            Alignment.TopEnd
                        )
                        .graphicsLayer {
                            translationX = 27f
                            translationY = -35f
                            alpha = 0.14f
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
                            Modifier.size(39.dp),
                        shape =
                            RoundedCornerShape(13.dp),
                        color =
                            accent.copy(
                                alpha = 0.16f
                            ),
                        border =
                            BorderStroke(
                                1.dp,
                                Color.White.copy(
                                    alpha = 0.09f
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
                                    icon,
                                contentDescription =
                                    null,
                                modifier =
                                    Modifier.size(20.dp),
                                tint =
                                    Color.White
                            )
                        }
                    }

                    Text(
                        text =
                            "›",
                        style =
                            MaterialTheme
                                .typography
                                .titleLarge,
                        color =
                            Color.White.copy(
                                alpha = 0.74f
                            )
                    )
                }

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(3.dp)
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
                            HomeTextPrimary,
                        maxLines =
                            1,
                        overflow =
                            TextOverflow.Ellipsis
                    )

                    if (metric != null) {
                        AnimatedContent(
                            targetState =
                                metric,
                            transitionSpec = {
                                fadeIn(
                                    tween(170)
                                )
                                    .togetherWith(
                                        fadeOut(
                                            tween(110)
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
                                    metricColor
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
                                HomeTextSecondary,
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
        RoundedCornerShape(21.dp)

    Surface(
        modifier =
            modifier
                .height(108.dp)
                .appPressable(
                    enabled = enabled,
                    pressedScale = 0.965f,
                    haptic = true,
                    onClick = onClick
                ),
        shape =
            shape,
        color =
            if (enabled) {
                Color(0xFF0C1828)
            } else {
                Color(0xFF09131F)
            },
        border =
            BorderStroke(
                1.dp,
                accent.copy(
                    alpha =
                        if (enabled) {
                            0.28f
                        } else {
                            0.14f
                        }
                )
            )
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(13.dp),
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
                        Modifier.size(36.dp),
                    shape =
                        RoundedCornerShape(12.dp),
                    color =
                        accent.copy(
                            alpha = 0.15f
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
                            modifier =
                                Modifier.size(19.dp),
                            tint =
                                accent
                        )
                    }
                }

                Text(
                    text =
                        "›",
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    color =
                        HomeTextSecondary
                )
            }

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text =
                        title,
                    style =
                        MaterialTheme
                            .typography
                            .titleSmall,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        HomeTextPrimary
                )

                Text(
                    text =
                        subtitle,
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,
                    color =
                        HomeTextSecondary,
                    maxLines =
                        1,
                    overflow =
                        TextOverflow.Ellipsis
                )
            }
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