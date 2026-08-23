package com.ngoctien.getmp3.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.ui.components.AppAnimatedStatusText
import com.ngoctien.getmp3.ui.design.LocalAppDesign
import com.ngoctien.getmp3.viewmodel.LyricsUiState
import java.util.Locale
import kotlin.math.roundToInt


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
        LocalAppDesign.current

    val colors =
        MaterialTheme.colorScheme

    val speedText =
        readerSpeedText(
            state.autoScrollSpeed
        )

    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                design.spacing.small
            )
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(
                    design.spacing.small
                )
        ) {
            Surface(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(
                            76.dp
                        )
                        .bouncyClickable(
                            onClick =
                                onToggleAutoScroll
                        ),

                shape =
                    RoundedCornerShape(
                        20.dp
                    ),

                color =
                    colors
                        .primary
                        .copy(
                            alpha =
                                if (
                                    state.autoScrollEnabled
                                ) {
                                    0.26f
                                }
                                else {
                                    0.14f
                                }
                        ),

                border =
                    BorderStroke(
                        1.dp,

                        colors
                            .primary
                            .copy(
                                alpha = 0.38f
                            )
                    )
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
                                42.dp
                            ),

                        shape =
                            RoundedCornerShape(
                                14.dp
                            ),

                        color =
                            colors
                                .primary
                                .copy(
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
                                    if (
                                        state.autoScrollEnabled
                                    ) {
                                        Icons.Rounded.Pause
                                    }
                                    else {
                                        Icons.Rounded.PlayArrow
                                    },

                                contentDescription =
                                    null,

                                tint =
                                    colors.primary
                            )
                        }
                    }

                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {
                        Text(
                            text =
                                "TỰ CUỘN",

                            fontWeight =
                                FontWeight.Black,

                            style =
                                MaterialTheme
                                    .typography
                                    .labelLarge
                        )

                        AppAnimatedStatusText(
                            text =
                                if (
                                    state.autoScrollEnabled
                                ) {
                                    "$speedText • đang chạy"
                                }
                                else {
                                    "$speedText • chạm để chạy"
                                },

                            color =
                                colors
                                    .onSurfaceVariant
                        )
                    }
                }
            }

            Surface(
                modifier =
                    Modifier
                        .width(
                            96.dp
                        )
                        .height(
                            76.dp
                        )
                        .bouncyClickable(
                            enabled =
                                !state.isSaving,

                            onClick =
                                onWriteLyrics
                        ),

                shape =
                    RoundedCornerShape(
                        20.dp
                    ),

                color =
                    colors
                        .secondary
                        .copy(
                            alpha = 0.18f
                        ),

                border =
                    BorderStroke(
                        1.dp,

                        colors
                            .secondary
                            .copy(
                                alpha = 0.42f
                            )
                    )
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
                                24.dp
                            ),

                        tint =
                            colors.secondary
                    )

                    Text(
                        text =
                            if (
                                state.isSaving
                            ) {
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

        Surface(
            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(
                    18.dp
                ),

            color =
                colors
                    .surfaceVariant
                    .copy(
                        alpha = 0.20f
                    ),

            border =
                BorderStroke(
                    1.dp,

                    colors
                        .outline
                        .copy(
                            alpha = 0.18f
                        )
                )
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 8.dp,
                            vertical = 3.dp
                        ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                TextButton(
                    onClick =
                        onDecreaseFont
                ) {
                    Text(
                        text =
                            "A−",

                        fontWeight =
                            FontWeight.Bold
                    )
                }

                LyricsPill(
                    text =
                        "${state.fontSizeSp.roundToInt()}sp",

                    emphasized =
                        true
                )

                TextButton(
                    onClick =
                        onIncreaseFont
                ) {
                    Text(
                        text =
                            "A+",

                        fontWeight =
                            FontWeight.Bold
                    )
                }

                Spacer(
                    modifier =
                        Modifier.weight(1f)
                )

                Text(
                    text =
                        speedText,

                    style =
                        MaterialTheme
                            .typography
                            .labelMedium,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        colors
                            .onSurfaceVariant
                )
            }
        }

        AnimatedVisibility(
            visible =
                state.autoScrollEnabled,

            enter =
                expandVertically() +
                    fadeIn(),

            exit =
                shrinkVertically() +
                    fadeOut()
        ) {
            Surface(
                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(
                        18.dp
                    ),

                color =
                    colors
                        .surfaceVariant
                        .copy(
                            alpha = 0.16f
                        )
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 14.dp,
                                vertical = 5.dp
                            ),

                    verticalAlignment =
                        Alignment.CenterVertically,

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        )
                ) {
                    Text(
                        text =
                            "Tốc độ",

                        style =
                            MaterialTheme
                                .typography
                                .labelMedium,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Slider(
                        value =
                            state.autoScrollSpeed
                                .toFloat(),

                        onValueChange = {
                            onScrollSpeedChange(
                                it.roundToInt()
                            )
                        },

                        modifier =
                            Modifier.weight(1f),

                        valueRange =
                            1f..10f,

                        steps =
                            8,

                        colors =
                            SliderDefaults.colors(
                                activeTrackColor =
                                    colors.primary,

                                thumbColor =
                                    colors.primary
                            )
                    )
                }
            }
        }
    }
}


private fun readerSpeedText(
    speed: Int
): String {
    val multiplier =
        when (
            speed.coerceIn(
                1,
                10
            )
        ) {
            1 -> 0.60
            2 -> 0.80
            3 -> 1.00
            4 -> 1.15
            5 -> 1.30
            6 -> 1.50
            7 -> 1.75
            8 -> 2.00
            9 -> 2.25
            else -> 2.50
        }

    return String.format(
        Locale.US,
        "%.2fx",
        multiplier
    )
}