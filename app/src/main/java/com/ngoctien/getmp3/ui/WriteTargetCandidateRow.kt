package com.ngoctien.getmp3.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.lyrics.LibrarySongCandidate
import com.ngoctien.getmp3.ui.components.AppIconActionButton
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
    val context =
        LocalContext.current

    val design =
        LocalAppDesign.current

    val containerColor by
        animateColorAsState(
            targetValue =
                if (selected) {
                    MaterialTheme
                        .colorScheme
                        .primaryContainer
                }
                else {
                    MaterialTheme
                        .colorScheme
                        .surface
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
                    MaterialTheme
                        .colorScheme
                        .primary
                }
                else {
                    MaterialTheme
                        .colorScheme
                        .outline
                        .copy(
                            alpha =
                                0.24f
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
                MaterialTheme
                    .colorScheme
                    .onPrimaryContainer
            }
            else {
                MaterialTheme
                    .colorScheme
                    .onSurface
            },

        border =
            BorderStroke(
                width =
                    1.dp,

                color =
                    borderColor
            ),

        tonalElevation =
            if (selected) {
                2.dp
            }
            else {
                0.dp
            }
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            RadioButton(
                selected =
                    selected,

                onClick =
                    onSelect,

                enabled =
                    enabled
            )

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        design.spacing.tiny
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

                    fontWeight =
                        FontWeight.SemiBold
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
                        if (selected) {
                            MaterialTheme
                                .colorScheme
                                .onPrimaryContainer
                                .copy(
                                    alpha =
                                        0.78f
                                )
                        }
                        else {
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                        }
                )

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            design.spacing.small
                        ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    LyricsPill(
                        text =
                            "Khớp ${
                                (
                                    candidate.score *
                                        100
                                ).roundToInt()
                            }%",

                        emphasized =
                            selected
                    )

                    if (currentSong) {
                        LyricsPill(
                            text =
                                "Bài đang mở",

                            emphasized =
                                true
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.width(
                        design.spacing.small
                    )
            )

            AppIconActionButton(
                icon =
                    Icons.Rounded.PlayArrow,

                contentDescription =
                    "Mở bài để nghe thử",

                onClick = {
                    openCandidateAudio(
                        context =
                            context,

                        uri =
                            candidate.uri
                    )
                },

                enabled =
                    enabled,

                haptic =
                    false
            )
        }
    }
}

private fun openCandidateAudio(
    context: Context,
    uri: String
) {
    val intent =
        Intent(
            Intent.ACTION_VIEW
        ).apply {
            setDataAndType(
                Uri.parse(
                    uri
                ),
                "audio/mpeg"
            )

            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

    try {
        context.startActivity(
            intent
        )
    }
    catch (
        _: ActivityNotFoundException
    ) {
        runCatching {
            context.startActivity(
                Intent.createChooser(
                    intent,
                    "Chọn ứng dụng mở nhạc"
                )
            )
        }
    }
}
