package com.ngoctien.getmp3.ui.skin.compact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.ui.StableOutlinedTextField
import com.ngoctien.getmp3.ui.design.LocalAppDesign
import com.ngoctien.getmp3.viewmodel.ArtistCaseMode
import com.ngoctien.getmp3.viewmodel.TagEditorUiState

@Composable
internal fun CompactTagEditorForm(
    state: TagEditorUiState,
    onTitleChange: (String) -> Unit,
    onClearTitle: () -> Unit,
    onArtistChange: (String) -> Unit,
    onClearArtist: () -> Unit,
    onArtistCaseModeChange:
        (ArtistCaseMode) -> Unit,
    onAlbumChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onQuickFormat: () -> Unit
) {
    val design =
        LocalAppDesign.current

    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                design.shapes.cardCorner
            ),

        color =
            MaterialTheme
                .colorScheme
                .surface,

        tonalElevation =
            1.dp
    ) {
        Column(
            modifier =
                Modifier.padding(
                    design.spacing.cardPadding
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    design.spacing.small
                )
        ) {
            Text(
                text =
                    "Metadata",

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.Bold
            )

            StableOutlinedTextField(
                value =
                    state.title,

                onValueChange =
                    onTitleChange,

                modifier =
                    Modifier.fillMaxWidth(),

                label = {
                    Text("Title")
                },

                singleLine =
                    true,

                keyboardOptions =
                    KeyboardOptions(
                        capitalization =
                            KeyboardCapitalization.Words
                    ),

                trailingIcon = {
                    if (state.title.isNotEmpty()) {
                        IconButton(
                            onClick =
                                onClearTitle
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Rounded.Clear,

                                contentDescription =
                                    "Xóa Title"
                            )
                        }
                    }
                }
            )

            StableOutlinedTextField(
                value =
                    state.artist,

                onValueChange =
                    onArtistChange,

                modifier =
                    Modifier.fillMaxWidth(),

                label = {
                    Text("Artist")
                },

                singleLine =
                    true,

                trailingIcon = {
                    if (state.artist.isNotEmpty()) {
                        IconButton(
                            onClick =
                                onClearArtist
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Rounded.Clear,

                                contentDescription =
                                    "Xóa Artist"
                            )
                        }
                    }
                }
            )

            LazyRow(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        6.dp
                    )
            ) {
                item {
                    FilterChip(
                        selected =
                            state.artistCaseMode ==
                                ArtistCaseMode
                                    .CAPITALIZE_WORDS,

                        onClick = {
                            onArtistCaseModeChange(
                                ArtistCaseMode
                                    .CAPITALIZE_WORDS
                            )
                        },

                        label = {
                            Text(
                                "Viết hoa chữ đầu"
                            )
                        }
                    )
                }

                item {
                    FilterChip(
                        selected =
                            state.artistCaseMode ==
                                ArtistCaseMode
                                    .KEEP_ORIGINAL,

                        onClick = {
                            onArtistCaseModeChange(
                                ArtistCaseMode
                                    .KEEP_ORIGINAL
                            )
                        },

                        label = {
                            Text(
                                "Giữ nguyên"
                            )
                        }
                    )
                }
            }

            OutlinedButton(
                onClick =
                    onQuickFormat,

                modifier =
                    Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded
                            .AutoFixHigh,

                    contentDescription =
                        null
                )

                Spacer(
                    modifier =
                        Modifier.width(
                            8.dp
                        )
                )

                Text(
                    text =
                        if (
                            state.knownArtistCount > 0
                        ) {
                            "Format nhanh • ${state.knownArtistCount} Artist"
                        }
                        else {
                            "Format nhanh"
                        },

                    fontWeight =
                        FontWeight.SemiBold
                )
            }

            Text(
                text =
                    "Album",

                style =
                    MaterialTheme
                        .typography
                        .labelLarge,

                fontWeight =
                    FontWeight.SemiBold
            )

            LazyRow(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        6.dp
                    )
            ) {
                items(
                    items =
                        state.albumOptions,

                    key = {
                        it
                    }
                ) { album ->

                    FilterChip(
                        selected =
                            album ==
                                state.selectedAlbum,

                        onClick = {
                            onAlbumChange(
                                album
                            )
                        },

                        label = {
                            Text(
                                album
                            )
                        }
                    )
                }
            }

            StableOutlinedTextField(
                value =
                    state.year,

                onValueChange = {
                    value ->

                    onYearChange(
                        value
                            .filter(
                                Char::isDigit
                            )
                            .take(4)
                    )
                },

                modifier =
                    Modifier.fillMaxWidth(),

                label = {
                    Text("Year")
                },

                singleLine =
                    true,

                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Number
                    ),

                supportingText = {
                    Text(
                        "4 chữ số • năm phát hành của đúng bản thu/version"
                    )
                }
            )
        }
    }
}
