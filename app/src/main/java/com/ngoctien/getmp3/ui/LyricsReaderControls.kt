package com.ngoctien.getmp3.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ngoctien.getmp3.viewmodel.LyricsUiState
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


private const val ReaderSpeedMinState =
    5

private const val ReaderSpeedMaxState =
    50


private data class ReaderSpeedDialLabel(
    val stateValue: Int,
    val label: String
)


private val ReaderSpeedDialLabels =
    listOf(
        ReaderSpeedDialLabel(
            stateValue = 5,
            label = "0.50×"
        ),
        ReaderSpeedDialLabel(
            stateValue = 10,
            label = "1.00×"
        ),
        ReaderSpeedDialLabel(
            stateValue = 20,
            label = "2.00×"
        ),
        ReaderSpeedDialLabel(
            stateValue = 30,
            label = "3.00×"
        ),
        ReaderSpeedDialLabel(
            stateValue = 40,
            label = "4.00×"
        ),
        ReaderSpeedDialLabel(
            stateValue = 50,
            label = "5.00×"
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

    var isAdjustingSpeed by
        remember {
            mutableStateOf(
                false
            )
        }

    /*
     * The dial alpha is animated independently from layout.
     *
     * The dial always exists as the second Layout child,
     * therefore showing it never changes the measured height
     * of the bottom controls.
     */
    val dialAlpha by
        animateFloatAsState(
            targetValue =
                if (isAdjustingSpeed) {
                    1f
                }
                else {
                    0f
                },

            animationSpec =
                tween(
                    durationMillis =
                        if (isAdjustingSpeed) {
                            130
                        }
                        else {
                            100
                        }
                ),

            label =
                "lyrics-speed-dial-visibility"
        )

    Layout(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    78.dp
                ),

        content = {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            78.dp
                        ),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        design.spacing.small
                    )
            ) {
                AutoScrollAction(
                    enabled =
                        state.autoScrollEnabled,

                    speed =
                        state.autoScrollSpeed,

                    adjusting =
                        isAdjustingSpeed,

                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    onClick =
                        onToggleAutoScroll,

                    onSpeedChange =
                        onScrollSpeedChange,

                    onAdjustingChange = {
                            adjusting ->

                        isAdjustingSpeed =
                            adjusting
                    }
                )

                WriteLyricsAction(
                    isSaving =
                        state.isSaving,

                    onClick =
                        onWriteLyrics
                )
            }

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .alpha(
                            dialAlpha
                        )
            ) {
                CameraStyleSpeedDial(
                    speed =
                        state.autoScrollSpeed
                )
            }
        }
    ) {
            measurables,
            constraints ->

        if (measurables.size != 2) {
            error(
                "ReaderControlBar requires exactly two layout children."
            )
        }

        val requestedControlHeight =
            78.dp.roundToPx()

        val controlHeight =
            requestedControlHeight.coerceIn(
                constraints.minHeight,
                constraints.maxHeight
            )

        val dialGap =
            12.dp.roundToPx()

        val controlPlaceable =
            measurables[0].measure(
                constraints.copy(
                    minHeight =
                        controlHeight,

                    maxHeight =
                        controlHeight
                )
            )

        /*
         * The dial may use its natural 184dp height.
         *
         * Crucially, this height is NOT returned by layout().
         */
        val dialPlaceable =
            measurables[1].measure(
                constraints.copy(
                    minHeight =
                        0,

                    maxHeight =
                        Constraints.Infinity
                )
            )

        val layoutWidth =
            if (
                constraints.hasBoundedWidth
            ) {
                constraints.maxWidth
            }
            else {
                maxOf(
                    controlPlaceable.width,
                    dialPlaceable.width
                )
            }

        layout(
            width =
                layoutWidth,

            height =
                controlHeight
        ) {
            controlPlaceable.placeRelative(
                x =
                    0,

                y =
                    0
            )

            /*
             * Keep the bottom of the dial exactly 12dp above
             * the fixed control row.
             *
             * Hidden and visible states use the same geometry.
             */
            dialPlaceable.placeRelative(
                x =
                    0,

                y =
                    -(
                        dialPlaceable.height +
                            dialGap
                        )
            )
        }
    }
}
@Composable
private fun AutoScrollAction(
    enabled: Boolean,
    speed: Int,
    adjusting: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
    onSpeedChange: (Int) -> Unit,
    onAdjustingChange: (Boolean) -> Unit
) {
    val colors =
        MaterialTheme.colorScheme

    val density =
        LocalDensity.current

    /*
     * 4dp horizontal movement = 0.10x.
     *
     * The speed state uses tenths:
     *
     *  5 = 0.50x
     * 10 = 1.00x
     * 50 = 5.00x
     */
    val scrubStepPx =
        with(density) {
            4.dp.toPx()
        }

    /*
     * Gesture keys stay stable while speed changes.
     *
     * This prevents Compose from cancelling the active
     * long-press gesture after each speed update.
     */
    val currentSpeed by
        rememberUpdatedState(
            speed
        )

    val currentOnSpeedChange by
        rememberUpdatedState(
            onSpeedChange
        )

    val currentOnAdjustingChange by
        rememberUpdatedState(
            onAdjustingChange
        )

    Surface(
        modifier =
            modifier
                .height(
                    78.dp
                )
                .pointerInput(
                    scrubStepPx
                ) {
                    var gestureSpeed =
                        currentSpeed.coerceIn(
                            ReaderSpeedMinState,
                            ReaderSpeedMaxState
                        )

                    var residualDragPx =
                        0f

                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            gestureSpeed =
                                currentSpeed.coerceIn(
                                    ReaderSpeedMinState,
                                    ReaderSpeedMaxState
                                )

                            residualDragPx =
                                0f

                            currentOnAdjustingChange(
                                true
                            )
                        },

                        onDragCancel = {
                            residualDragPx =
                                0f

                            currentOnAdjustingChange(
                                false
                            )
                        },

                        onDragEnd = {
                            residualDragPx =
                                0f

                            currentOnAdjustingChange(
                                false
                            )
                        },

                        onDrag = {
                                change,
                                dragAmount ->

                            change.consume()

                            residualDragPx +=
                                dragAmount.x

                            while (
                                residualDragPx >=
                                    scrubStepPx &&
                                gestureSpeed <
                                    ReaderSpeedMaxState
                            ) {
                                gestureSpeed +=
                                    1

                                residualDragPx -=
                                    scrubStepPx

                                currentOnSpeedChange(
                                    gestureSpeed
                                )
                            }

                            while (
                                residualDragPx <=
                                    -scrubStepPx &&
                                gestureSpeed >
                                    ReaderSpeedMinState
                            ) {
                                gestureSpeed -=
                                    1

                                residualDragPx +=
                                    scrubStepPx

                                currentOnSpeedChange(
                                    gestureSpeed
                                )
                            }

                            if (
                                gestureSpeed ==
                                    ReaderSpeedMaxState &&
                                residualDragPx >
                                    0f
                            ) {
                                residualDragPx =
                                    0f
                            }

                            if (
                                gestureSpeed ==
                                    ReaderSpeedMinState &&
                                residualDragPx <
                                    0f
                            ) {
                                residualDragPx =
                                    0f
                            }
                        }
                    )
                }
                .clickable(
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
                        when {
                            adjusting ->
                                0.39f

                            enabled ->
                                0.31f

                            else ->
                                0.16f
                        }
                ),

        contentColor =
            colors.onSurface,

        border =
            BorderStroke(
                width =
                    if (adjusting) {
                        1.5.dp
                    }
                    else {
                        1.dp
                    },

                color =
                    colors
                        .primary
                        .copy(
                            alpha =
                                when {
                                    adjusting ->
                                        0.92f

                                    enabled ->
                                        0.64f

                                    else ->
                                        0.34f
                                }
                        )
            ),

        tonalElevation =
            when {
                adjusting ->
                    11.dp

                enabled ->
                    8.dp

                else ->
                    3.dp
            },

        shadowElevation =
            when {
                adjusting ->
                    10.dp

                enabled ->
                    7.dp

                else ->
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
                            alpha =
                                if (adjusting) {
                                    0.34f
                                }
                                else {
                                    0.22f
                                }
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

            Text(
                text =
                    "TỰ CUỘN",

                modifier =
                    Modifier.weight(
                        1f
                    ),

                style =
                    MaterialTheme
                        .typography
                        .labelLarge,

                fontWeight =
                    FontWeight.Black
            )

            Surface(
                shape =
                    RoundedCornerShape(
                        99.dp
                    ),

                color =
                    if (
                        adjusting ||
                        enabled
                    ) {
                        colors
                            .tertiary
                            .copy(
                                alpha = 0.20f
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
                            if (
                                adjusting ||
                                enabled
                            ) {
                                colors
                                    .tertiary
                                    .copy(
                                        alpha = 0.46f
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
                        readerSpeedText(
                            speed
                        ),

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
                        if (
                            adjusting ||
                            enabled
                        ) {
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
    speed: Int
) {
    val colors =
        MaterialTheme.colorScheme

    val safeSpeed =
        speed.coerceIn(
            ReaderSpeedMinState,
            ReaderSpeedMaxState
        )

    val selectedFraction =
        (
            safeSpeed -
                ReaderSpeedMinState
            ).toFloat() /
            (
                ReaderSpeedMaxState -
                    ReaderSpeedMinState
                ).toFloat()

    val animatedFraction by
        animateFloatAsState(
            targetValue =
                selectedFraction,

            animationSpec =
                tween(
                    durationMillis =
                        55
                ),

            label =
                "lyrics-camera-speed-marker"
        )

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
                    alpha = 0.98f
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
                            alpha = 0.32f
                        )
            ),

        tonalElevation =
            10.dp,

        shadowElevation =
            12.dp
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
                        safeSpeed
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
                    "Giữ nút Tự cuộn và kéo trái / phải",

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
                                    radius *
                                        2f,

                                height =
                                    radius *
                                        2f
                            ),

                        style =
                            Stroke(
                                width =
                                    1.5.dp.toPx(),

                                cap =
                                    StrokeCap.Round
                            )
                    )

                    val totalTicks =
                        ReaderSpeedMaxState -
                            ReaderSpeedMinState

                    for (
                        tick in
                        0..totalTicks
                    ) {
                        val fraction =
                            tick.toFloat() /
                                totalTicks.toFloat()

                        val angle =
                            startAngle +
                                sweepAngle *
                                    fraction

                        val radians =
                            Math.toRadians(
                                angle.toDouble()
                            )

                        val major =
                            tick % 5 ==
                                0

                        val outerRadius =
                            radius

                        val innerRadius =
                            radius -
                                if (major) {
                                    13.dp.toPx()
                                }
                                else {
                                    6.dp.toPx()
                                }

                        val cosValue =
                            cos(
                                radians
                            ).toFloat()

                        val sinValue =
                            sin(
                                radians
                            ).toFloat()

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
                                                0.28f
                                            }
                                    ),

                            start =
                                Offset(
                                    x =
                                        center.x +
                                            cosValue *
                                                innerRadius,

                                    y =
                                        center.y +
                                            sinValue *
                                                innerRadius
                                ),

                            end =
                                Offset(
                                    x =
                                        center.x +
                                            cosValue *
                                                outerRadius,

                                    y =
                                        center.y +
                                            sinValue *
                                                outerRadius
                                ),

                            strokeWidth =
                                if (major) {
                                    1.6.dp.toPx()
                                }
                                else {
                                    0.9.dp.toPx()
                                },

                            cap =
                                StrokeCap.Round
                        )
                    }

                    val markerAngle =
                        startAngle +
                            sweepAngle *
                                animatedFraction

                    val markerRadians =
                        Math.toRadians(
                            markerAngle.toDouble()
                        )

                    val markerCos =
                        cos(
                            markerRadians
                        ).toFloat()

                    val markerSin =
                        sin(
                            markerRadians
                        ).toFloat()

                    val markerOuter =
                        Offset(
                            x =
                                center.x +
                                    markerCos *
                                        (
                                            radius +
                                                2.dp.toPx()
                                            ),

                            y =
                                center.y +
                                    markerSin *
                                        (
                                            radius +
                                                2.dp.toPx()
                                            )
                        )

                    val markerInner =
                        Offset(
                            x =
                                center.x +
                                    markerCos *
                                        (
                                            radius -
                                                20.dp.toPx()
                                            ),

                            y =
                                center.y +
                                    markerSin *
                                        (
                                            radius -
                                                20.dp.toPx()
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
                                horizontal = 5.dp,
                                vertical = 2.dp
                            ),

                    verticalAlignment =
                        Alignment.CenterVertically,

                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {
                    ReaderSpeedDialLabels.forEach {
                            option ->

                        Text(
                            text =
                                option.label,

                            style =
                                MaterialTheme
                                    .typography
                                    .labelSmall,

                            fontSize =
                                7.sp,

                            fontWeight =
                                if (
                                    option.stateValue ==
                                        safeSpeed
                                ) {
                                    FontWeight.Black
                                }
                                else {
                                    FontWeight.Medium
                                },

                            color =
                                if (
                                    option.stateValue ==
                                        safeSpeed
                                ) {
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
                        "Giữ + kéo",

                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        colors.tertiary
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
    val safeSpeed =
        speed.coerceIn(
            ReaderSpeedMinState,
            ReaderSpeedMaxState
        )

    val whole =
        safeSpeed /
            10

    val decimal =
        safeSpeed %
            10

    return "$whole.${decimal}0×"
}