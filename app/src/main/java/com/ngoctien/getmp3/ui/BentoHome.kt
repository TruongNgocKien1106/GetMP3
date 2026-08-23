package com.ngoctien.getmp3.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.LightMode
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
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    onOpenInbox: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenTagEditor: () -> Unit,
    onOpenCompare: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleTheme: () -> Unit,
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
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }

        item {
            DownloadHero(
                inboxFolderName =
                    inboxFolderName,
                onOpenInbox =
                    onOpenInbox,
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
                        motion =
                            ExploreMotion.LIBRARY,
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
                        motion =
                            ExploreMotion.LYRICS,
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
                        motion =
                            ExploreMotion.TAGS,
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
                        motion =
                            ExploreMotion.DUPLICATES,
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
                        Color(0xFF6495FF),
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
                        Color(0xFFFFA75B),
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
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
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
                        pressedScale = 0.92f,
                        haptic = true,
                        onClick =
                            onToggleTheme
                    ),
            shape =
                RoundedCornerShape(15.dp),
            color =
                MaterialTheme
                    .colorScheme
                    .surface,
            border =
                BorderStroke(
                    1.dp,
                    MaterialTheme
                        .colorScheme
                        .outline
                        .copy(
                            alpha = 0.58f
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
                        if (isDarkTheme) {
                            Icons.Rounded.LightMode
                        } else {
                            Icons.Rounded.DarkMode
                        },
                    contentDescription =
                        if (isDarkTheme) {
                            "Chuyển sang giao diện sáng"
                        } else {
                            "Chuyển sang giao diện tối"
                        },
                    modifier =
                        Modifier.size(21.dp),
                    tint =
                        MaterialTheme
                            .colorScheme
                            .onSurface
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
        ) {
            PrototypeCardSheen(
                delayMillis =
                    3_400,
                modifier =
                    Modifier.fillMaxSize()
            )

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
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

                HeroWave(
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
}
@Composable
private fun HeroWave(
    modifier: Modifier = Modifier
) {
    val transition =
        rememberInfiniteTransition(
            label =
                "hero-wave"
        )

    val barScales =
        listOf(
            0.36f,
            0.58f,
            0.82f,
            0.48f,
            0.70f,
            0.43f,
            0.64f
        )

    Row(
        modifier =
            modifier,
        horizontalArrangement =
            Arrangement.spacedBy(3.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        barScales.forEachIndexed {
                index,
                minimumScale ->

            val scale by
                transition.animateFloat(
                    initialValue =
                        minimumScale,
                    targetValue =
                        1f,
                    animationSpec =
                        infiniteRepeatable(
                            animation =
                                tween(
                                    durationMillis =
                                        560 +
                                            index * 70,
                                    easing =
                                        FastOutSlowInEasing
                                ),
                            repeatMode =
                                RepeatMode.Reverse
                        ),
                    label =
                        "hero-bar-$index"
                )

            Box(
                modifier =
                    Modifier
                        .width(3.dp)
                        .height(31.dp)
                        .graphicsLayer {
                            scaleY =
                                scale

                            transformOrigin =
                                TransformOrigin(
                                    0.5f,
                                    1f
                                )
                        }
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF6DDCFF),
                                    Color(0xFF6495FF),
                                    Color(0xFF9A76FF)
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
private enum class ExploreMotion {
    LIBRARY,
    LYRICS,
    TAGS,
    DUPLICATES
}

@Composable
private fun ExploreCard(
    title: String,
    metric: String?,
    metricLabel: String?,
    icon: ImageVector,
    accent: Color,
    background: List<Color>,
    motion: ExploreMotion,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape =
        RoundedCornerShape(21.dp)

    val metricColor =
        when (motion) {
            ExploreMotion.TAGS ->
                Color(0xFFFFC84C)

            ExploreMotion.DUPLICATES ->
                Color(0xFFFF86A6)

            else ->
                HomeTextPrimary
        }

    val sheenDelay =
        when (motion) {
            ExploreMotion.LIBRARY ->
                2_900

            ExploreMotion.LYRICS ->
                3_900

            ExploreMotion.TAGS ->
                3_400

            ExploreMotion.DUPLICATES ->
                4_600
        }

    val ambientTransition =
        rememberInfiniteTransition(
            label =
                "ambient-$title"
        )

    val ambientScale by
        ambientTransition.animateFloat(
            initialValue =
                0.94f,
            targetValue =
                1.04f,
            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(
                            durationMillis =
                                when (motion) {
                                    ExploreMotion.LIBRARY ->
                                        5_200

                                    ExploreMotion.LYRICS ->
                                        6_100

                                    ExploreMotion.TAGS ->
                                        5_700

                                    ExploreMotion.DUPLICATES ->
                                        6_500
                                },
                            easing =
                                FastOutSlowInEasing
                        ),
                    repeatMode =
                        RepeatMode.Reverse
                ),
            label =
                "ambient-scale-$title"
        )

    val ambientAlpha by
        ambientTransition.animateFloat(
            initialValue =
                0.15f,
            targetValue =
                0.25f,
            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(
                            durationMillis =
                                4_300,
                            easing =
                                FastOutSlowInEasing
                        ),
                    repeatMode =
                        RepeatMode.Reverse
                ),
            label =
                "ambient-alpha-$title"
        )

    Surface(
        modifier =
            modifier
                .height(174.dp)
                .appPressable(
                    pressedScale = 0.975f,
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
        ) {
            Box(
                modifier =
                    Modifier
                        .size(108.dp)
                        .align(
                            Alignment.TopEnd
                        )
                        .offset(
                            x = 35.dp,
                            y = (-53).dp
                        )
                        .graphicsLayer {
                            scaleX =
                                ambientScale

                            scaleY =
                                ambientScale

                            alpha =
                                ambientAlpha
                        }
                        .background(
                            accent,
                            CircleShape
                        )
            )

            PrototypeCardSheen(
                delayMillis =
                    sheenDelay,
                modifier =
                    Modifier.fillMaxSize()
            )

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(14.dp)
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .align(
                                Alignment.TopStart
                            ),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Surface(
                        modifier =
                            Modifier.size(41.dp),
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
                                alpha = 0.78f
                            )
                    )
                }

                Text(
                    text =
                        title,
                    modifier =
                        Modifier
                            .align(
                                Alignment.TopStart
                            )
                            .padding(
                                top = 68.dp
                            ),
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
                    Row(
                        modifier =
                            Modifier.align(
                                Alignment.BottomStart
                            ),
                        horizontalArrangement =
                            Arrangement.spacedBy(5.dp),
                        verticalAlignment =
                            Alignment.Bottom
                    ) {
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
                                        .headlineSmall,
                                fontWeight =
                                    FontWeight.Black,
                                color =
                                    metricColor
                            )
                        }

                        if (
                            !metricLabel
                                .isNullOrBlank()
                        ) {
                            Text(
                                text =
                                    metricLabel,
                                modifier =
                                    Modifier.padding(
                                        bottom = 3.dp
                                    ),
                                style =
                                    MaterialTheme
                                        .typography
                                        .labelSmall,
                                fontWeight =
                                    FontWeight.Bold,
                                color =
                                    HomeTextSecondary,
                                maxLines =
                                    1
                            )
                        }
                    }
                }

                ExploreCardMotion(
                    motion =
                        motion,
                    accent =
                        accent,
                    modifier =
                        Modifier
                            .align(
                                Alignment.BottomEnd
                            )
                            .size(
                                width = 44.dp,
                                height = 28.dp
                            )
                )
            }
        }
    }
}

@Composable
private fun PrototypeCardSheen(
    delayMillis: Int,
    modifier: Modifier = Modifier
) {
    val transition =
        rememberInfiniteTransition(
            label =
                "prototype-sheen-$delayMillis"
        )

    val progress by
        transition.animateFloat(
            initialValue =
                0f,
            targetValue =
                1f,
            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(
                            durationMillis =
                                860,
                            delayMillis =
                                delayMillis,
                            easing =
                                FastOutSlowInEasing
                        ),
                    repeatMode =
                        RepeatMode.Restart
                ),
            label =
                "prototype-sheen-progress-$delayMillis"
        )

    BoxWithConstraints(
        modifier =
            modifier
    ) {
        val travelPx =
            constraints
                .maxWidth
                .toFloat() +
                220f

        Box(
            modifier =
                Modifier
                    .width(70.dp)
                    .fillMaxHeight()
                    .graphicsLayer {
                        translationX =
                            -150f +
                                travelPx *
                                    progress

                        rotationZ =
                            -14f

                        alpha =
                            0.92f
                    }
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(
                                    alpha = 0.018f
                                ),
                                Color.White.copy(
                                    alpha = 0.075f
                                ),
                                Color.White.copy(
                                    alpha = 0.22f
                                ),
                                Color.White.copy(
                                    alpha = 0.07f
                                ),
                                Color.Transparent
                            )
                        )
                    )
        )
    }
}
@Composable
private fun ExploreCardMotion(
    motion: ExploreMotion,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val transition =
        rememberInfiniteTransition(
            label =
                "card-motion-$motion"
        )

    when (motion) {
        ExploreMotion.LIBRARY -> {
            val heights =
                listOf(
                    8.dp,
                    16.dp,
                    11.dp,
                    22.dp,
                    14.dp
                )

            Row(
                modifier =
                    modifier,
                horizontalArrangement =
                    Arrangement.spacedBy(3.dp),
                verticalAlignment =
                    Alignment.Bottom
            ) {
                heights.forEachIndexed {
                        index,
                        barHeight ->

                    val scale by
                        transition.animateFloat(
                            initialValue =
                                0.42f +
                                    index * 0.07f,
                            targetValue =
                                1f,
                            animationSpec =
                                infiniteRepeatable(
                                    animation =
                                        tween(
                                            durationMillis =
                                                620 +
                                                    index * 110,
                                            easing =
                                                FastOutSlowInEasing
                                        ),
                                    repeatMode =
                                        RepeatMode.Reverse
                                ),
                            label =
                                "library-motion-$index"
                        )

                    Box(
                        modifier =
                            Modifier
                                .width(3.dp)
                                .height(
                                    barHeight
                                )
                                .graphicsLayer {
                                    scaleY =
                                        scale

                                    transformOrigin =
                                        TransformOrigin(
                                            0.5f,
                                            1f
                                        )
                                }
                                .background(
                                    accent.copy(
                                        alpha = 0.82f
                                    ),
                                    CircleShape
                                )
                    )
                }
            }
        }

        ExploreMotion.LYRICS -> {
            val lineScales =
                listOf(
                    0.74f,
                    0.52f,
                    0.88f,
                    0.62f
                )

            Column(
                modifier =
                    modifier,
                horizontalAlignment =
                    Alignment.End,
                verticalArrangement =
                    Arrangement.spacedBy(3.dp)
            ) {
                lineScales.forEachIndexed {
                        index,
                        minimumScale ->

                    val scale by
                        transition.animateFloat(
                            initialValue =
                                minimumScale,
                            targetValue =
                                1f,
                            animationSpec =
                                infiniteRepeatable(
                                    animation =
                                        tween(
                                            durationMillis =
                                                880 +
                                                    index * 130,
                                            easing =
                                                FastOutSlowInEasing
                                        ),
                                    repeatMode =
                                        RepeatMode.Reverse
                                ),
                            label =
                                "lyrics-motion-$index"
                        )

                    Box(
                        modifier =
                            Modifier
                                .width(
                                    when (index) {
                                        0 -> 22.dp
                                        1 -> 17.dp
                                        2 -> 25.dp
                                        else -> 19.dp
                                    }
                                )
                                .height(2.dp)
                                .graphicsLayer {
                                    scaleX =
                                        scale

                                    transformOrigin =
                                        TransformOrigin(
                                            1f,
                                            0.5f
                                        )
                                }
                                .background(
                                    accent.copy(
                                        alpha = 0.82f
                                    ),
                                    CircleShape
                                )
                    )
                }
            }
        }

        ExploreMotion.TAGS -> {
            val rotation by
                transition.animateFloat(
                    initialValue =
                        -4f,
                    targetValue =
                        4f,
                    animationSpec =
                        infiniteRepeatable(
                            animation =
                                tween(
                                    durationMillis =
                                        1800,
                                    easing =
                                        FastOutSlowInEasing
                                ),
                            repeatMode =
                                RepeatMode.Reverse
                        ),
                    label =
                        "tag-wobble"
                )

            val sparkleRotation by
                transition.animateFloat(
                    initialValue =
                        0f,
                    targetValue =
                        360f,
                    animationSpec =
                        infiniteRepeatable(
                            animation =
                                tween(
                                    durationMillis =
                                        4800,
                                    easing =
                                        LinearEasing
                                ),
                            repeatMode =
                                RepeatMode.Restart
                        ),
                    label =
                        "tag-sparkle"
                )

            Box(
                modifier =
                    modifier
            ) {
                Box(
                    modifier =
                        Modifier
                            .width(23.dp)
                            .height(13.dp)
                            .align(
                                Alignment.BottomEnd
                            )
                            .offset(
                                x = (-3).dp,
                                y = (-2).dp
                            )
                            .graphicsLayer {
                                rotationZ =
                                    rotation
                            }
                            .background(
                                accent.copy(
                                    alpha = 0.28f
                                ),
                                RoundedCornerShape(
                                    topStart = 4.dp,
                                    topEnd = 7.dp,
                                    bottomEnd = 7.dp,
                                    bottomStart = 4.dp
                                )
                            )
                            .border(
                                1.dp,
                                accent.copy(
                                    alpha = 0.72f
                                ),
                                RoundedCornerShape(
                                    topStart = 4.dp,
                                    topEnd = 7.dp,
                                    bottomEnd = 7.dp,
                                    bottomStart = 4.dp
                                )
                            )
                )

                Box(
                    modifier =
                        Modifier
                            .size(5.dp)
                            .align(
                                Alignment.TopEnd
                            )
                            .graphicsLayer {
                                rotationZ =
                                    sparkleRotation
                            }
                            .background(
                                accent,
                                CircleShape
                            )
                )
            }
        }

        ExploreMotion.DUPLICATES -> {
            val shift by
                transition.animateFloat(
                    initialValue =
                        -1.5f,
                    targetValue =
                        1.5f,
                    animationSpec =
                        infiniteRepeatable(
                            animation =
                                tween(
                                    durationMillis =
                                        1400,
                                    easing =
                                        FastOutSlowInEasing
                                ),
                            repeatMode =
                                RepeatMode.Reverse
                        ),
                    label =
                        "duplicate-shift"
                )

            Box(
                modifier =
                    modifier
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(15.dp)
                            .align(
                                Alignment.CenterEnd
                            )
                            .offset(
                                x = (-12).dp
                            )
                            .graphicsLayer {
                                translationX =
                                    shift
                            }
                            .border(
                                2.dp,
                                accent.copy(
                                    alpha = 0.82f
                                ),
                                CircleShape
                            )
                )

                Box(
                    modifier =
                        Modifier
                            .size(15.dp)
                            .align(
                                Alignment.CenterEnd
                            )
                            .offset(
                                x = (-1).dp
                            )
                            .graphicsLayer {
                                translationX =
                                    -shift
                            }
                            .border(
                                2.dp,
                                accent.copy(
                                    alpha = 0.82f
                                ),
                                CircleShape
                            )
                )
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
                    enabled =
                        enabled,
                    pressedScale =
                        0.965f,
                    haptic =
                        true,
                    onClick =
                        onClick
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