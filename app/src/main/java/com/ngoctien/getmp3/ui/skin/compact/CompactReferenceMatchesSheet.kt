package com.ngoctien.getmp3.ui.skin.compact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.ngoctien.getmp3.note.ReferenceSongMatch
import com.ngoctien.getmp3.ui.design.appPressable
import com.ngoctien.getmp3.ui.design.LocalAppDesign

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
internal fun CompactReferenceMatchesSheet(
    query: String,
    matches: List<ReferenceSongMatch>,
    onDismiss: () -> Unit,
    onOpenReferenceSong:
        (ReferenceSongMatch) -> Unit
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
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded
                            .LibraryMusic,

                    contentDescription =
                        null,

                    tint =
                        MaterialTheme
                            .colorScheme
                            .primary
                )

                Spacer(
                    modifier =
                        Modifier.size(9.dp)
                )

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {
                    Text(
                        text =
                            "Đã có trong Library",

                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            "${matches.size} kết quả cho “${query.ifBlank { "bài hát" }}”",

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant,

                        maxLines =
                            1,

                        overflow =
                            TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text =
                    "Chọn đúng file để mở. App không tự chọn kết quả đầu tiên.",

                style =
                    MaterialTheme
                        .typography
                        .labelSmall,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(
                            max = 480.dp
                        ),

                contentPadding =
                    PaddingValues(
                        vertical =
                            8.dp
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        6.dp
                    )
            ) {
                items(
                    items =
                        matches,

                    key = {
                        it.uri
                    }
                ) { match ->

                    Surface(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .appPressable(
                                    haptic =
                                        true,

                                    onClick = {
                                        onDismiss()

                                        onOpenReferenceSong(
                                            match
                                        )
                                    }
                                ),

                        shape =
                            RoundedCornerShape(
                                design
                                    .shapes
                                    .controlCorner
                            ),

                        color =
                            MaterialTheme
                                .colorScheme
                                .surfaceVariant
                                .copy(
                                    alpha = 0.42f
                                )
                    ) {
                        Row(
                            modifier =
                                Modifier.padding(
                                    horizontal =
                                        12.dp,

                                    vertical =
                                        11.dp
                                ),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Column(
                                modifier =
                                    Modifier.weight(
                                        1f
                                    )
                            ) {
                                Text(
                                    text =
                                        match.displayName,

                                    maxLines =
                                        2,

                                    overflow =
                                        TextOverflow.Ellipsis,

                                    fontWeight =
                                        FontWeight.Medium
                                )

                                Text(
                                    text =
                                        "Mở trong Sửa thẻ",

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

                            Spacer(
                                modifier =
                                    Modifier.size(
                                        8.dp
                                    )
                            )

                            Icon(
                                imageVector =
                                    Icons.Rounded
                                        .OpenInNew,

                                contentDescription =
                                    null,

                                modifier =
                                    Modifier.size(
                                        18.dp
                                    ),

                                tint =
                                    MaterialTheme
                                        .colorScheme
                                        .primary
                            )
                        }
                    }
                }
            }
        }
    }
}
