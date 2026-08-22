package com.ngoctien.getmp3.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.ngoctien.getmp3.lyrics.LyricsSearchResult
import com.ngoctien.getmp3.ui.design.LocalAppDesign
import kotlin.math.roundToInt

@Composable
internal fun LyricsSourceInfoDialog(
    result: LyricsSearchResult,
    onDismiss: () -> Unit
) {
    val design =
        LocalAppDesign.current

    AlertDialog(
        onDismissRequest =
            onDismiss,

        icon = {
            Icon(
                imageVector =
                    Icons.Rounded.Info,

                contentDescription =
                    null
            )
        },

        title = {
            Text(
                text =
                    "Thông tin nguồn lyrics"
            )
        },

        text = {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(
                        design.spacing.small
                    )
            ) {
                LyricsSourceRow(
                    label =
                        "Bài hát",

                    value =
                        result.trackName
                )

                LyricsSourceRow(
                    label =
                        "Artist",

                    value =
                        result.artistName
                            .ifBlank {
                                "Không rõ"
                            }
                )

                result.albumName
                    .takeIf(
                        String::isNotBlank
                    )
                    ?.let { album ->

                        LyricsSourceRow(
                            label =
                                "Album",

                            value =
                                album
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
                    LyricsPill(
                        text =
                            lyricsSourceType(
                                result
                            ),

                        emphasized =
                            true
                    )

                    LyricsPill(
                        text =
                            "${
                                (
                                    result.score *
                                        100
                                ).roundToInt()
                            }%"
                    )
                }
            }
        },

        confirmButton = {
            TextButton(
                onClick =
                    onDismiss
            ) {
                Text(
                    text =
                        "Đóng"
                )
            }
        }
    )
}

@Composable
private fun LyricsSourceRow(
    label: String,
    value: String
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {
        Text(
            text =
                label,

            style =
                MaterialTheme
                    .typography
                    .bodySmall,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        Text(
            text =
                value,

            modifier =
                Modifier.weight(1f),

            fontWeight =
                FontWeight.SemiBold
        )
    }
}

internal fun lyricsReaderSubtitle(
    result: LyricsSearchResult
): String {
    return buildString {
        append(
            result.artistName
                .ifBlank {
                    "Không rõ Artist"
                }
        )

        append(" · ")

        append(
            when {
                result.id ==
                    Long.MIN_VALUE ->
                    "Trong file MP3"

                result.syncedLyrics
                    .orEmpty()
                    .isNotBlank() ->
                    "Đồng bộ"

                else ->
                    "Lời thường"
            }
        )

        append(" · ")

        append(
            (
                result.score *
                    100
            ).roundToInt()
        )

        append('%')
    }
}

private fun lyricsSourceType(
    result: LyricsSearchResult
): String {
    return when {
        result.id ==
            Long.MIN_VALUE ->
            "Lyrics nhúng trong MP3"

        result.syncedLyrics
            .orEmpty()
            .isNotBlank() ->
            "Lyrics đồng bộ"

        else ->
            "Lyrics thường"
    }
}
