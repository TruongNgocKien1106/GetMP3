package com.ngoctien.getmp3.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ngoctien.getmp3.ui.components.AppCard
import com.ngoctien.getmp3.ui.components.AppErrorNotice
import com.ngoctien.getmp3.ui.components.AppIconActionButton
import com.ngoctien.getmp3.ui.design.LocalAppDesign
import com.ngoctien.getmp3.viewmodel.LyricsUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@Composable
internal fun LyricsReaderScreen(
    state: LyricsUiState,
    onBack: () -> Unit,
    onIncreaseFont: () -> Unit,
    onDecreaseFont: () -> Unit,
    onToggleAutoScroll: () -> Unit,
    onScrollSpeedChange: (Int) -> Unit,
    onEditLyrics: () -> Unit,
    onWriteLyrics: () -> Unit
) {
    val result =
        state.selectedResult
            ?: return

    val design =
        LocalAppDesign.current

    val scrollState =
        rememberScrollState()

    val view =
        LocalView.current

    val clipboard =
        LocalClipboardManager.current

    val selectedTarget =
        state.selectedSong
            ?.takeIf {
                it.uri.isNotBlank()
            }

    val lyricBlocks =
        remember(
            result.id,
            result.readableLyrics
        ) {
            result.readableLyrics
                .trim()
                .split(
                    Regex(
                        """\n\s*\n+"""
                    )
                )
                .map(String::trim)
                .filter(String::isNotBlank)
        }

    var showReaderMenu by
        remember {
            mutableStateOf(false)
        }

    var showSourceInfo by
        remember {
            mutableStateOf(false)
        }

    DisposableEffect(view) {
        val previous =
            view.keepScreenOn

        view.keepScreenOn =
            true

        onDispose {
            view.keepScreenOn =
                previous
        }
    }

    LaunchedEffect(
        result.id
    ) {
        scrollState.scrollTo(
            0
        )
    }

    LaunchedEffect(
        state.autoScrollEnabled,
        state.autoScrollSpeed,
        result.id
    ) {
        if (!state.autoScrollEnabled) {
            return@LaunchedEffect
        }

        val pixelsPerTick =
            autoScrollPixelsPerTick(
                state.autoScrollSpeed
            )

        while (true) {
            val next =
                (
                    scrollState.value +
                        pixelsPerTick
                )
                    .coerceAtMost(
                        scrollState.maxValue
                    )

            if (
                next <=
                scrollState.value
            ) {
                break
            }

            scrollState.scrollTo(
                next
            )

            delay(
                24L
            )
        }
    }

    if (showSourceInfo) {
        LyricsSourceInfoDialog(
            result =
                result,

            onDismiss = {
                showSourceInfo =
                    false
            }
        )
    }

    Column(
        modifier =
            Modifier.fillMaxSize(),

        verticalArrangement =
            Arrangement.spacedBy(
                design.spacing.small
            )
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.spacedBy(
                    design.spacing.small
                )
        ) {
            AppIconActionButton(
                icon =
                    Icons.Rounded.ArrowBack,

                contentDescription =
                    "Quay lại",

                onClick =
                    onBack,

                haptic =
                    false
            )

            LyricsArtwork(
                result =
                    result,

                size =
                    48
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {
                Text(
                    text =
                        result.trackName,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis,

                    fontWeight =
                        FontWeight.Black,

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium
                )

                Text(
                    text =
                        lyricsReaderSubtitle(
                            result
                        ),

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

            Box {
                AppIconActionButton(
                    icon =
                        Icons.Rounded.MoreVert,

                    contentDescription =
                        "Tùy chọn lyrics",

                    onClick = {
                        showReaderMenu =
                            true
                    },

                    haptic =
                        false
                )

                DropdownMenu(
                    expanded =
                        showReaderMenu,

                    onDismissRequest = {
                        showReaderMenu =
                            false
                    }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Sửa lyrics"
                            )
                        },

                        leadingIcon = {
                            Icon(
                                imageVector =
                                    Icons.Rounded.Edit,

                                contentDescription =
                                    null
                            )
                        },

                        enabled =
                            selectedTarget !=
                                null,

                        onClick = {
                            showReaderMenu =
                                false

                            onEditLyrics()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                "Thông tin nguồn"
                            )
                        },

                        leadingIcon = {
                            Icon(
                                imageVector =
                                    Icons.Rounded.Info,

                                contentDescription =
                                    null
                            )
                        },

                        onClick = {
                            showReaderMenu =
                                false

                            showSourceInfo =
                                true
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                "Sao chép lyrics"
                            )
                        },

                        leadingIcon = {
                            Icon(
                                imageVector =
                                    Icons.Rounded
                                        .ContentCopy,

                                contentDescription =
                                    null
                            )
                        },

                        onClick = {
                            showReaderMenu =
                                false

                            clipboard.setText(
                                AnnotatedString(
                                    result
                                        .readableLyrics
                                )
                            )
                        }
                    )
                }
            }
        }

        state.errorMessage
            ?.takeIf(
                String::isNotBlank
            )
            ?.let { message ->

                AppErrorNotice(
                    text =
                        message
                )
            }

        AppCard(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),

            contentPadding =
                PaddingValues(
                    0.dp
                )
        ) {
            Box(
                modifier =
                    Modifier.fillMaxSize()
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(
                                scrollState
                            )
                            .padding(
                                start =
                                    26.dp,

                                top =
                                    28.dp,

                                end =
                                    36.dp,

                                bottom =
                                    80.dp
                            ),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Text(
                        text =
                            "KARAOKE",

                        style =
                            MaterialTheme
                                .typography
                                .labelMedium,

                        fontWeight =
                            FontWeight.Black,

                        color =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                24.dp
                            )
                    )

                    lyricBlocks
                        .forEachIndexed {
                                blockIndex,
                                block ->

                            Text(
                                text =
                                    if (
                                        blockIndex == 0
                                    ) {
                                        "BẮT ĐẦU"
                                    }
                                    else {
                                        "ĐOẠN ${blockIndex + 1}"
                                    },

                                style =
                                    MaterialTheme
                                        .typography
                                        .labelSmall,

                                fontWeight =
                                    FontWeight.Black,

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .secondary
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        15.dp
                                    )
                            )

                            block
                                .lineSequence()
                                .map(String::trim)
                                .filter(
                                    String::isNotBlank
                                )
                                .forEach {
                                        line ->

                                    Text(
                                        text =
                                            line,

                                        modifier =
                                            Modifier
                                                .fillMaxWidth(),

                                        fontSize =
                                            state
                                                .fontSizeSp
                                                .sp,

                                        lineHeight =
                                            (
                                                state
                                                    .fontSizeSp *
                                                    1.38f
                                            ).sp,

                                        textAlign =
                                            TextAlign.Center,

                                        fontWeight =
                                            FontWeight
                                                .SemiBold,

                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .onSurface
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(
                                                7.dp
                                            )
                                    )
                                }

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        26.dp
                                    )
                            )
                        }
                }

                LyricsScrollBar(
                    scrollState =
                        scrollState,

                    modifier =
                        Modifier
                            .align(
                                Alignment.CenterEnd
                            )
                            .padding(
                                top = 14.dp,
                                end = 7.dp,
                                bottom = 14.dp
                            )
                )
            }
        }

        ReaderControlBar(
            state =
                state,

            onIncreaseFont =
                onIncreaseFont,

            onDecreaseFont =
                onDecreaseFont,

            onToggleAutoScroll =
                onToggleAutoScroll,

            onScrollSpeedChange =
                onScrollSpeedChange,

            onWriteLyrics =
                onWriteLyrics
        )
    }
}


@Composable
private fun LyricsScrollBar(
    scrollState:
        androidx.compose.foundation.ScrollState,
    modifier: Modifier = Modifier
) {
    val colors =
        MaterialTheme.colorScheme

    val density =
        LocalDensity.current

    val scope =
        rememberCoroutineScope()

    var trackHeightPx by
        remember {
            mutableFloatStateOf(
                0f
            )
        }

    val maxScrollPx =
        scrollState.maxValue
            .toFloat()

    val minimumThumbPx =
        with(density) {
            42.dp.toPx()
        }

    val contentHeightPx =
        trackHeightPx +
            maxScrollPx

    val calculatedThumbPx =
        if (
            contentHeightPx > 0f &&
            trackHeightPx > 0f
        ) {
            trackHeightPx *
                (
                    trackHeightPx /
                        contentHeightPx
                )
        }
        else {
            0f
        }

    val thumbHeightPx =
        when {
            trackHeightPx <= 0f ->
                0f

            trackHeightPx <=
                minimumThumbPx ->
                trackHeightPx

            else ->
                calculatedThumbPx
                    .coerceIn(
                        minimumThumbPx,
                        trackHeightPx
                    )
        }

    val travelPx =
        (
            trackHeightPx -
                thumbHeightPx
        )
            .coerceAtLeast(
                0f
            )

    val progress =
        if (
            maxScrollPx > 0f
        ) {
            scrollState.value
                .toFloat()
                .div(
                    maxScrollPx
                )
                .coerceIn(
                    0f,
                    1f
                )
        }
        else {
            0f
        }

    val thumbOffsetPx =
        travelPx *
            progress

    Box(
        modifier =
            modifier
                .width(
                    16.dp
                )
                .fillMaxHeight()
                .onSizeChanged {
                        size ->

                    trackHeightPx =
                        size.height
                            .toFloat()
                }
                .pointerInput(
                    maxScrollPx,
                    travelPx,
                    thumbHeightPx
                ) {
                    detectDragGestures(
                        onDragStart = {
                                position ->

                            if (
                                maxScrollPx <= 0f ||
                                travelPx <= 0f
                            ) {
                                return@detectDragGestures
                            }

                            val targetFraction =
                                (
                                    (
                                        position.y -
                                            thumbHeightPx /
                                                2f
                                    ) /
                                        travelPx
                                )
                                    .coerceIn(
                                        0f,
                                        1f
                                    )

                            scope.launch {
                                scrollState.scrollTo(
                                    (
                                        targetFraction *
                                            maxScrollPx
                                    )
                                        .roundToInt()
                                )
                            }
                        },

                        onDrag = {
                                _,
                                dragAmount ->

                            if (
                                maxScrollPx <= 0f ||
                                travelPx <= 0f
                            ) {
                                return@detectDragGestures
                            }

                            val deltaScroll =
                                (
                                    dragAmount.y /
                                        travelPx
                                ) *
                                    maxScrollPx

                            val target =
                                (
                                    scrollState.value +
                                        deltaScroll
                                )
                                    .roundToInt()
                                    .coerceIn(
                                        0,
                                        scrollState.maxValue
                                    )

                            scope.launch {
                                scrollState.scrollTo(
                                    target
                                )
                            }
                        }
                    )
                }
    ) {
        Box(
            modifier =
                Modifier
                    .align(
                        Alignment.Center
                    )
                    .width(
                        3.dp
                    )
                    .fillMaxHeight()
                    .clip(
                        CircleShape
                    )
                    .background(
                        colors
                            .outline
                            .copy(
                                alpha = 0.20f
                            )
                    )
        )

        if (
            maxScrollPx > 0f &&
            thumbHeightPx > 0f
        ) {
            Box(
                modifier =
                    Modifier
                        .align(
                            Alignment.TopCenter
                        )
                        .offset {
                            IntOffset(
                                x = 0,
                                y =
                                    thumbOffsetPx
                                        .roundToInt()
                            )
                        }
                        .width(
                            7.dp
                        )
                        .height(
                            with(density) {
                                thumbHeightPx
                                    .toDp()
                            }
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            colors.primary
                        )
            )
        }
    }
}


private fun autoScrollPixelsPerTick(
    speed: Int
): Int {
    val safeSpeed =
        speed.coerceIn(
            5,
            50
        )

    val multiplier =
        safeSpeed /
            10f

    return (
        multiplier *
            4f
        )
        .roundToInt()
        .coerceAtLeast(
            1
        )
}