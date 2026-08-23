package com.ngoctien.getmp3.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.lyrics.LibrarySongCandidate
import com.ngoctien.getmp3.ui.design.LocalAppDesign
import com.ngoctien.getmp3.ui.design.appPressable
import kotlin.math.roundToInt


@Composable
internal fun WriteTargetCandidateRow(
    candidate: LibrarySongCandidate,
    selected: Boolean,
    currentSong: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit
) {
    val design =
        LocalAppDesign.current

    val colors =
        MaterialTheme.colorScheme

    val containerColor by
        animateColorAsState(
            targetValue =
                if (selected) {
                    colors.primaryContainer
                }
                else {
                    colors.surface
                },

            animationSpec =
                tween(
                    AppMotion.ContentMillis
                ),

            label =
                "write-target-container"
        )

    val borderColor by
        animateColorAsState(
            targetValue =
                if (selected) {
                    colors.primary
                }
                else {
                    colors
                        .outline
                        .copy(
                            alpha = 0.28f
                        )
                },

            animationSpec =
                tween(
                    AppMotion.ContentMillis
                ),

            label =
                "write-target-border"
        )

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .appPressable(
                    enabled =
                        enabled,

                    haptic =
                        true,

                    onClick =
                        onSelect
                ),

        shape =
            RoundedCornerShape(
                design.shapes.cardCorner
            ),

        color =
            containerColor,

        contentColor =
            if (selected) {
                colors.onPrimaryContainer
            }
            else {
                colors.onSurface
            },

        border =
            BorderStroke(
                width =
                    if (selected) {
                        1.5.dp
                    }
                    else {
                        1.dp
                    },

                color =
                    borderColor
            ),

        tonalElevation =
            if (selected) {
                4.dp
            }
            else {
                1.dp
            }
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        11.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.spacedBy(
                    11.dp
                )
        ) {
            Mp3CoverThumbnail(
                uri =
                    candidate.uri,

                displayName =
                    candidate.displayName,

                size =
                    64.dp,

                cornerRadius =
                    17.dp
            )

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        3.dp
                    )
            ) {
                Text(
                    text =
                        candidate.title
                            .ifBlank {
                                candidate.displayName
                            },

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis,

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    fontWeight =
                        FontWeight.Black
                )

                Text(
                    text =
                        candidate.artist
                            .ifBlank {
                                "Không rõ Artist"
                            },

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis,

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    color =
                        colors
                            .onSurfaceVariant
                )

                Text(
                    text =
                        candidate.displayName,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis,

                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,

                    color =
                        colors
                            .onSurfaceVariant
                            .copy(
                                alpha = 0.68f
                            )
                )

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            7.dp
                        )
                ) {
                    LyricsPill(
                        text =
                            "Khớp ${
                                (
                                    candidate.score *
                                        100
                                    )
                                    .roundToInt()
                                    .coerceIn(
                                        0,
                                        100
                                    )
                            }%",

                        emphasized =
                            selected
                    )

                    if (currentSong) {
                        LyricsPill(
                            text =
                                "Đang mở",

                            emphasized =
                                true
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.width(
                        2.dp
                    )
            )

            RadioButton(
                selected =
                    selected,

                onClick =
                    onSelect,

                enabled =
                    enabled
            )
        }
    }
}