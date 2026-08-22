package com.ngoctien.getmp3.ui.skin.compact

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun CompactArtistCandidatesDialog(
    candidates: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest =
            onDismiss,

        icon = {
            Icon(
                imageVector =
                    Icons.Rounded.Person,

                contentDescription =
                    null
            )
        },

        title = {
            Text(
                text =
                    "Chọn Artist",

                fontWeight =
                    FontWeight.Bold
            )
        },

        text = {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(
                            max = 380.dp
                        )
            ) {
                items(
                    items =
                        candidates,

                    key = {
                        it
                    }
                ) { artist ->

                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(
                                        artist
                                    )
                                }
                                .padding(
                                    vertical =
                                        12.dp
                                )
                    ) {
                        Text(
                            text =
                                artist,

                            style =
                                MaterialTheme
                                    .typography
                                    .bodyLarge,

                            fontWeight =
                                FontWeight.Medium
                        )
                    }
                }
            }
        },

        confirmButton = {
            TextButton(
                onClick =
                    onDismiss
            ) {
                Text(
                    "Tự nhập"
                )
            }
        }
    )
}
