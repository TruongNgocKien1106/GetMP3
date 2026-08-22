package com.ngoctien.getmp3.ui.skin.compact

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.tag.MediaSongFile
import com.ngoctien.getmp3.ui.Mp3CoverThumbnail
import com.ngoctien.getmp3.ui.design.LocalAppDesign

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
internal fun CompactTagFileSheet(
    files: List<MediaSongFile>,
    selectedIndex: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    val design =
        LocalAppDesign.current

    ModalBottomSheet(
        onDismissRequest =
            onDismiss
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal =
                            design.spacing.screenHorizontal
                    ),

            verticalArrangement =
                Arrangement.spacedBy(
                    design.spacing.small
                )
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {
                    Text(
                        text =
                            "Danh sách MP3",

                        style =
                            MaterialTheme
                                .typography
                                .titleLarge,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            "${files.size} file trong Inbox",

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
            }

            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(
                            max = 520.dp
                        )
            ) {
                itemsIndexed(
                    items =
                        files,

                    key = {
                            _,
                            file ->

                        "${file.uri}|${file.dateModifiedSeconds}"
                    }
                ) {
                        index,
                        file ->

                    val selected =
                        index ==
                            selectedIndex

                    Surface(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(
                                        index
                                    )

                                    onDismiss()
                                },

                        shape =
                            RoundedCornerShape(
                                design
                                    .shapes
                                    .controlCorner
                            ),

                        color =
                            if (selected) {
                                MaterialTheme
                                    .colorScheme
                                    .primaryContainer
                            }
                            else {
                                MaterialTheme
                                    .colorScheme
                                    .surface
                            }
                    ) {
                        Row(
                            modifier =
                                Modifier.padding(
                                    horizontal =
                                        8.dp,

                                    vertical =
                                        8.dp
                                ),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Mp3CoverThumbnail(
                                file =
                                    file,

                                size =
                                    48.dp,

                                cornerRadius =
                                    12.dp
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(
                                        10.dp
                                    )
                            )

                            Column(
                                modifier =
                                    Modifier.weight(
                                        1f
                                    )
                            ) {
                                Text(
                                    text =
                                        file.displayName,

                                    maxLines =
                                        2,

                                    overflow =
                                        TextOverflow.Ellipsis,

                                    fontWeight =
                                        if (selected) {
                                            FontWeight.SemiBold
                                        }
                                        else {
                                            FontWeight.Normal
                                        }
                                )

                                Text(
                                    text =
                                        formatCompactBytes(
                                            file.sizeBytes
                                        ),

                                    style =
                                        MaterialTheme
                                            .typography
                                            .labelSmall,

                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onSurfaceVariant
                                )
                            }

                            if (selected) {
                                Icon(
                                    imageVector =
                                        Icons.Rounded.Check,

                                    contentDescription =
                                        "Đang chọn",

                                    tint =
                                        MaterialTheme
                                            .colorScheme
                                            .primary
                                )
                            }
                        }
                    }

                    if (
                        index <
                        files.lastIndex
                    ) {
                        HorizontalDivider(
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .outlineVariant
                                    .copy(
                                        alpha =
                                            0.35f
                                    )
                        )
                    }
                }
            }
        }
    }
}

private fun formatCompactBytes(
    bytes: Long
): String {
    if (bytes <= 0L) {
        return "0 B"
    }

    val mb =
        bytes.toDouble() /
            (1024.0 * 1024.0)

    return if (mb >= 1.0) {
        String.format(
            "%.1f MB",
            mb
        )
    }
    else {
        String.format(
            "%.0f KB",
            bytes.toDouble() /
                1024.0
        )
    }
}
