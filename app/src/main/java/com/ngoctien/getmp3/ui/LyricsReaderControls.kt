package com.ngoctien.getmp3.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ngoctien.getmp3.viewmodel.LyricsUiState
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


private data class ReaderSpeedOption(
    val stateValue: Int,
    val label: String
)


private val ReaderSpeedOptions =
    listOf(
        ReaderSpeedOption(
            stateValue = 1,
            label = "0.50×"
        ),
        ReaderSpeedOption(
            stateValue = 2,
            label = "0.75×"
        ),
        ReaderSpeedOption(
            stateValue = 3,
            label = "1.00×"
        ),
        ReaderSpeedOption(
            stateValue = 4,
            label = "1.25×"
        ),
        ReaderSpeedOption(
            stateValue = 5,
            label = "1.50×"
        ),
        ReaderSpeedOption(
            stateValue = 6,
            label = "1.75×"
        ),
        ReaderSpeedOption(
            stateValue = 7,
            label = "2.00×"
        )
    )


@Suppress("UNUSED_PARAMETER")
@Composable
internal fun ReaderControlBar(
    state: LyricsUiState,
    onIncreaseFont: () -> Unit,
    onDecreaseFont: () -> Unit,
    onToggleAutoScroll: () -> Unit,
    onScrollSpeedChange: (Int) -> Unit,
    onWriteLyrics: () -> Unit
) {
    val design =
        com.ngoctien.getmp3.ui.design
            .LocalAppDesign
            .current

    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                design.spacing.small
            )
    ) {
        AnimatedVisibility(
            visible =
                state.autoScrollEnabled,

            enter =
                fadeIn(
                    animationSpec =
                        tween(
                            durationMillis = 220
                        )
                ) +
                    expandVertically(
                        animationSpec =
                            tween(
                                durationMillis = 260
                            )
                    ),

            exit =
                fadeOut(
                    animationSpec =
                        tween(
                            durationMillis = 160
                        )
                ) +
                    shrinkVertically(
                        animationSpec =
                            tween(
                                durationMillis = 220
                            )
                    )
        ) {
            CameraStyleSpeedDial(
                speed =
                    state.autoScrollSpeed,

                onSpeedChange =
                    onScrollSpeedChange
            )
        }

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(
                    design.spacing.small
                )
        ) {
            AutoScrollAction(
                enabled =
                    state.autoScrollEnabled,

                speedLabel =
                    readerSpeedText(
                        state.autoScrollSpeed
                    ),

                modifier =
                    Modifier.weight(
                        1f
                    ),

                onClick =
                    onToggleAutoScroll
            )

            WriteLyricsAction(
                isSaving =
                    state.isSaving,

                onClick =
                    onWriteLyrics
            )
        }
    }
}


@Composable
private fun AutoScrollAction(
    enabled: Boolean,
    speedLabel: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val colors =
        MaterialTheme.colorScheme

    Surface(
        modifier =
            modifier
                .height(
                    78.dp
                )
                .bouncyClickable(
                    onClick =
                        onClick
                ),

        shape =
            RoundedCornerShape(
                21.dp
            ),

        color =
            colors
                .primary
                .copy(
                    alpha =
                        if (enabled) {
                            0.31f
                        }
                        else {
                            0.16f
                        }
                ),

        contentColor =
            colors.onSurface,

        border =
            BorderStroke(
                width =
                    1.dp,

                color =
                    colors
                        .primary
                        .copy(
                            alpha =
                                if (enabled) {
                                    0.64f
                                }
                                else {
                                    0.34f
                                }
                        )
            ),

        tonalElevation =
            if (enabled) {
                8.dp
            }
            else {
                3.dp
            },

        shadowElevation =
            if (enabled) {
                7.dp
            }
            else {
                2.dp
            }
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 13.dp,
                        vertical = 10.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.spacedBy(
                    11.dp
                )
        ) {
            Surface(
                modifier =
                    Modifier.size(
                        43.dp
                    ),

                shape =
                    RoundedCornerShape(
                        14.dp
                    ),

                color =
                    colors
                        .primary
                        .copy(
                            alpha = 0.22f
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
                            if (enabled) {
                                Icons.Rounded.Pause
                            }
                            else {
                                Icons.Rounded.PlayArrow
                            },

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(
                                23.dp
                            ),

                        tint =
                            colors.primary
                    )
                }
            }

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {
                Text(
                    text =
                        "TỰ CUỘN",

                    style =
                        MaterialTheme
                            .typography
                            .labelLarge,

                    fontWeight =
                        FontWeight.Black
                )

                Text(
                    text =
                        if (enabled) {
                            "Đang chạy"
                        }
                        else {
                            "Chạm để bắt đầu"
                        },

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    color =
                        colors
                            .onSurfaceVariant
                )
            }

            Surface(
                shape =
                    RoundedCornerShape(
                        99.dp
                    ),

                color =
                    if (enabled) {
                        colors
                            .tertiary
                            .copy(
                                alpha = 0.18f
                            )
                    }
                    else {
                        colors
                            .surfaceVariant
                            .copy(
                                alpha = 0.38f
                            )
                    },

                border =
                    BorderStroke(
                        width =
                            1.dp,

                        color =
                            if (enabled) {
                                colors
                                    .tertiary
                                    .copy(
                                        alpha = 0.38f
                                    )
                            }
                            else {
                                colors
                                    .outline
                                    .copy(
                                        alpha = 0.16f
                                    )
                            }
                    )
            ) {
                Text(
                    text =
                        speedLabel,

                    modifier =
                        Modifier.padding(
                            horizontal = 9.dp,
                            vertical = 5.dp
                        ),

                    style =
                        MaterialTheme
                            .typography
                            .labelMedium,

                    fontWeight =
                        FontWeight.Black,

                    color =
                        if (enabled) {
                            colors.tertiary
                        }
                        else {
                            colors
                                .onSurfaceVariant
                        }
                )
            }
        }
    }
}


@Composable
private fun WriteLyricsAction(
    isSaving: Boolean,
    onClick: () -> Unit
) {
    val colors =
        MaterialTheme.colorScheme

    Surface(
        modifier =
            Modifier
                .width(
                    94.dp
                )
                .height(
                    78.dp
                )
                .bouncyClickable(
                    enabled =
                        !isSaving,

                    onClick =
                        onClick
                ),

        shape =
            RoundedCornerShape(
                21.dp
            ),

        color =
            colors
                .secondary
                .copy(
                    alpha = 0.19f
                ),

        border =
            BorderStroke(
                width =
                    1.dp,

                color =
                    colors
                        .secondary
                        .copy(
                            alpha = 0.46f
                        )
            ),

        tonalElevation =
            4.dp,

        shadowElevation =
            3.dp
    ) {
        Column(
            modifier =
                Modifier.fillMaxSize(),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.Center
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.Save,

                contentDescription =
                    null,

                modifier =
                    Modifier.size(
                        25.dp
                    ),

                tint =
                    colors.secondary
            )

            Spacer(
                modifier =
                    Modifier.height(
                        5.dp
                    )
            )

            Text(
                text =
                    if (isSaving) {
                        "Đang ghi"
                    }
                    else {
                        "Ghi"
                    },

                style =
                    MaterialTheme
                        .typography
                        .labelLarge,

                fontWeight =
                    FontWeight.Black
            )
        }
    }
}


@Composable
private fun CameraStyleSpeedDial(
    speed: Int,
    onSpeedChange: (Int) -> Unit
) {
    val colors =
        MaterialTheme.colorScheme

    val selectedIndex =
        ReaderSpeedOptions
            .indexOfFirst {
                it.stateValue ==
                    speed.coerceIn(
                        1,
                        7
                    )
            }
            .coerceAtLeast(
                0
            )

    val animatedIndex by
        animateFloatAsState(
            targetValue =
                selectedIndex.toFloat(),

            animationSpec =
                tween(
                    durationMillis = 190
                ),

            label =
                "lyrics-camera-speed-marker"
        )

    var controlWidthPx by
        remember {
            mutableFloatStateOf(
                1f
            )
        }

    fun selectFromX(
        x: Float
    ) {
        if (controlWidthPx <= 1f) {
            return
        }

        val normalized =
            (
                x /
                    controlWidthPx
            )
                .coerceIn(
                    0f,
                    1f
                )

        val index =
            (
                normalized *
                    ReaderSpeedOptions
                        .lastIndex
            )
                .roundToInt()
                .coerceIn(
                    0,
                    ReaderSpeedOptions
                        .lastIndex
                )

        onSpeedChange(
            ReaderSpeedOptions[index]
                .stateValue
        )
    }

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    184.dp
                ),

        shape =
            RoundedCornerShape(
                24.dp
            ),

        color =
            colors
                .surface
                .copy(
                    alpha = 0.96f
                ),

        contentColor =
            colors.onSurface,

        border =
            BorderStroke(
                width =
                    1.dp,

                color =
                    colors
                        .primary
                        .copy(
                            alpha = 0.25f
                        )
            ),

        tonalElevation =
            7.dp,

        shadowElevation =
            8.dp
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        start = 9.dp,
                        top = 10.dp,
                        end = 9.dp,
                        bottom = 8.dp
                    ),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text =
                    readerSpeedText(
                        speed
                    ),

                style =
                    MaterialTheme
                        .typography
                        .titleLarge,

                fontWeight =
                    FontWeight.Black,

                fontSize =
                    20.sp,

                color =
                    colors.tertiary
            )

            Text(
                text =
                    "Tốc độ tự cuộn",

                style =
                    MaterialTheme
                        .typography
                        .labelSmall,

                color =
                    colors
                        .onSurfaceVariant
            )

            Spacer(
                modifier =
                    Modifier.height(
                        1.dp
                    )
            )

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(
                            1f
                        )
                        .onSizeChanged {
                                size ->

                            controlWidthPx =
                                size.width
                                    .toFloat()
                        }
                        .pointerInput(
                            controlWidthPx
                        ) {
                            detectTapGestures {
                                    offset ->

                                selectFromX(
                                    offset.x
                                )
                            }
                        }
                        .pointerInput(
                            controlWidthPx
                        ) {
                            detectDragGestures(
                                onDragStart = {
                                        offset ->

                                    selectFromX(
                                        offset.x
                                    )
                                },

                                onDrag = {
                                        change,
                                        _ ->

                                    change.consume()

                                    selectFromX(
                                        change
                                            .position
                                            .x
                                    )
                                }
                            )
                        }
            ) {
                Canvas(
                    modifier =
                        Modifier.fillMaxSize()
                ) {
                    val startAngle =
                        200f

                    val sweepAngle =
                        140f

                    val center =
                        Offset(
                            x =
                                size.width /
                                    2f,

                            y =
                                size.height *
                                    1.09f
                        )

                    val radius =
                        minOf(
                            size.width *
                                0.43f,

                            size.height *
                                0.88f
                        )

                    drawArc(
                        color =
                            colors
                                .outline
                                .copy(
                                    alpha = 0.38f
                                ),

                        startAngle =
                            startAngle,

                        sweepAngle =
                            sweepAngle,

                        useCenter =
                            false,

                        topLeft =
                            Offset(
                                x =
                                    center.x -
                                        radius,

                                y =
                                    center.y -
                                        radius
                            ),

                        size =
                            Size(
                                width =
                                    radius * 2f,

                                height =
                                    radius * 2f
                            ),

                        style =
                            Stroke(
                                width =
                                    1.5.dp
                                        .toPx(),

                                cap =
                                    StrokeCap.Round
                            )
                    )

                    val totalTicks =
                        24

                    for (
                        tick in
                        0..totalTicks
                    ) {
                        val fraction =
                            tick.toFloat() /
                                totalTicks
                                    .toFloat()

                        val angle =
                            startAngle +
                                sweepAngle *
                                    fraction

                        val radians =
                            Math.toRadians(
                                angle.toDouble()
                            )

                        val major =
                            tick % 4 ==
                                0

                        val tickLength =
                            if (major) {
                                13.dp.toPx()
                            }
                            else {
                                7.dp.toPx()
                            }

                        val outerRadius =
                            radius

                        val innerRadius =
                            radius -
                                tickLength

                        val cosValue =
                            cos(
                                radians
                            ).toFloat()

                        val sinValue =
                            sin(
                                radians
                            ).toFloat()

                        val outer =
                            Offset(
                                x =
                                    center.x +
                                        cosValue *
                                            outerRadius,

                                y =
                                    center.y +
                                        sinValue *
                                            outerRadius
                            )

                        val inner =
                            Offset(
                                x =
                                    center.x +
                                        cosValue *
                                            innerRadius,

                                y =
                                    center.y +
                                        sinValue *
                                            innerRadius
                            )

                        drawLine(
                            color =
                                colors
                                    .onSurfaceVariant
                                    .copy(
                                        alpha =
                                            if (major) {
                                                0.72f
                                            }
                                            else {
                                                0.34f
                                            }
                                    ),

                            start =
                                inner,

                            end =
                                outer,

                            strokeWidth =
                                if (major) {
                                    1.7.dp
                                        .toPx()
                                }
                                else {
                                    1.dp
                                        .toPx()
                                },

                            cap =
                                StrokeCap.Round
                        )
                    }

                    val selectedFraction =
                        animatedIndex /
                            ReaderSpeedOptions
                                .lastIndex
                                .toFloat()

                    val selectedAngle =
                        startAngle +
                            sweepAngle *
                                selectedFraction

                    val selectedRadians =
                        Math.toRadians(
                            selectedAngle.toDouble()
                        )

                    val selectedCos =
                        cos(
                            selectedRadians
                        ).toFloat()

                    val selectedSin =
                        sin(
                            selectedRadians
                        ).toFloat()

                    val markerOuter =
                        Offset(
                            x =
                                center.x +
                                    selectedCos *
                                        (
                                            radius +
                                                2.dp
                                                    .toPx()
                                        ),

                            y =
                                center.y +
                                    selectedSin *
                                        (
                                            radius +
                                                2.dp
                                                    .toPx()
                                        )
                        )

                    val markerInner =
                        Offset(
                            x =
                                center.x +
                                    selectedCos *
                                        (
                                            radius -
                                                20.dp
                                                    .toPx()
                                        ),

                            y =
                                center.y +
                                    selectedSin *
                                        (
                                            radius -
                                                20.dp
                                                    .toPx()
                                        )
                        )

                    drawLine(
                        color =
                            colors
                                .tertiary
                                .copy(
                                    alpha = 0.18f
                                ),

                        start =
                            markerInner,

                        end =
                            markerOuter,

                        strokeWidth =
                            10.dp.toPx(),

                        cap =
                            StrokeCap.Round
                    )

                    drawLine(
                        color =
                            colors.tertiary,

                        start =
                            markerInner,

                        end =
                            markerOuter,

                        strokeWidth =
                            3.2.dp.toPx(),

                        cap =
                            StrokeCap.Round
                    )

                    drawCircle(
                        color =
                            colors
                                .tertiary
                                .copy(
                                    alpha = 0.20f
                                ),

                        radius =
                            7.dp.toPx(),

                        center =
                            markerOuter
                    )

                    drawCircle(
                        color =
                            colors.tertiary,

                        radius =
                            2.7.dp.toPx(),

                        center =
                            markerOuter
                    )
                }

                Row(
                    modifier =
                        Modifier
                            .align(
                                Alignment.BottomCenter
                            )
                            .fillMaxWidth()
                            .padding(
                                horizontal = 8.dp,
                                vertical = 2.dp
                            ),

                    verticalAlignment =
                        Alignment.CenterVertically,

                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {
                    ReaderSpeedOptions
                        .forEachIndexed {
                                index,
                                option ->

                            val selected =
                                index ==
                                    selectedIndex

                            Text(
                                text =
                                    option.label,

                                style =
                                    MaterialTheme
                                        .typography
                                        .labelSmall,

                                fontSize =
                                    8.sp,

                                fontWeight =
                                    if (selected) {
                                        FontWeight.Black
                                    }
                                    else {
                                        FontWeight.Medium
                                    },

                                color =
                                    if (selected) {
                                        colors.tertiary
                                    }
                                    else {
                                        colors
                                            .onSurfaceVariant
                                            .copy(
                                                alpha = 0.62f
                                            )
                                    }
                            )
                        }
                }
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 5.dp
                        ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text =
                        "Chậm",

                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,

                    color =
                        colors
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )

                Text(
                    text =
                        "Kéo trái / phải",

                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        colors
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )

                Text(
                    text =
                        "Nhanh",

                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,

                    color =
                        colors
                            .onSurfaceVariant
                )
            }
        }
    }
}


private fun readerSpeedText(
    speed: Int
): String {
    return ReaderSpeedOptions[
        (
            speed.coerceIn(
                1,
                7
            ) -
                1
            )
    ].label
}
