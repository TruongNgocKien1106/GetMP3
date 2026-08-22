package com.ngoctien.getmp3.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.ngoctien.getmp3.ui.components.AppAnimatedStatusText
import com.ngoctien.getmp3.ui.components.AppCard
import com.ngoctien.getmp3.ui.design.LocalAppDesign
import com.ngoctien.getmp3.viewmodel.LyricsUiState
import kotlin.math.roundToInt

@Composable
internal fun ReaderControlBar(
    state: LyricsUiState,
    onIncreaseFont: () -> Unit,
    onDecreaseFont: () -> Unit,
    onToggleAutoScroll: () -> Unit,
    onScrollSpeedChange: (Int) -> Unit
) {
    val design =
        LocalAppDesign.current

    AppCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .animateContentSize()
    ) {
        Column(
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

                FilterChip(
                    selected =
                        state.autoScrollEnabled,

                    onClick =
                        onToggleAutoScroll,

                    label = {
                        AppAnimatedStatusText(
                            text =
                                if (
                                    state.autoScrollEnabled
                                ) {
                                    "Tạm dừng"
                                }
                                else {
                                    "Tự cuộn"
                                }
                        )
                    },

                    leadingIcon = {
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
                                null
                        )
                    },

                    colors =
                        FilterChipDefaults
                            .filterChipColors(
                                selectedContainerColor =
                                    MaterialTheme
                                        .colorScheme
                                        .primary,

                                selectedLabelColor =
                                    MaterialTheme
                                        .colorScheme
                                        .onPrimary,

                                selectedLeadingIconColor =
                                    MaterialTheme
                                        .colorScheme
                                        .onPrimary
                            )
                )
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
                    AppAnimatedStatusText(
                        text =
                            "Tốc độ ${state.autoScrollSpeed}/10",

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
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
                                    MaterialTheme
                                        .colorScheme
                                        .primary,

                                thumbColor =
                                    MaterialTheme
                                        .colorScheme
                                        .primary
                            )
                    )
                }
            }
        }
    }
}
