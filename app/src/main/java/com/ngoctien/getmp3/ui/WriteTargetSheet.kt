package com.ngoctien.getmp3.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.lyrics.LibrarySongCandidate
import com.ngoctien.getmp3.lyrics.LyricsSearchResult
import com.ngoctien.getmp3.ui.components.AppActionButton
import com.ngoctien.getmp3.ui.components.AppActionVisualState
import com.ngoctien.getmp3.ui.components.AppEmptyState
import com.ngoctien.getmp3.ui.components.AppErrorNotice
import com.ngoctien.getmp3.ui.components.AppLoadingPanel
import com.ngoctien.getmp3.ui.design.LocalAppDesign
import com.ngoctien.getmp3.viewmodel.WriteTargetPickerState


@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
internal fun WriteTargetSheet(
    state: WriteTargetPickerState,
    selectedLyrics: LyricsSearchResult?,
    currentSong: LibrarySongCandidate?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSelect: (LibrarySongCandidate) -> Unit,
    onConfirm: () -> Unit
) {
    val design =
        LocalAppDesign.current

    val colors =
        MaterialTheme.colorScheme

    var filterQuery by
        remember(
            state.isVisible,
            selectedLyrics?.id
        ) {
            mutableStateOf(
                ""
            )
        }

    val filteredCandidates =
        remember(
            state.candidates,
            filterQuery
        ) {
            val needle =
                filterQuery
                    .trim()
                    .lowercase()

            if (needle.isBlank()) {
                state.candidates
            }
            else {
                state.candidates.filter {
                        candidate ->

                    candidate.title
                        .lowercase()
                        .contains(
                            needle
                        ) ||
                        candidate.artist
                            .lowercase()
                            .contains(
                                needle
                            ) ||
                        candidate.displayName
                            .lowercase()
                            .contains(
                                needle
                            )
                }
            }
        }

    val sourceLabel =
        selectedLyrics
            ?.let {
                    result ->

                listOf(
                    result.trackName,
                    result.artistName
                )
                    .filter(
                        String::isNotBlank
                    )
                    .joinToString(
                        " · "
                    )
            }
            .orEmpty()
            .ifBlank {
                "Chọn bài trong Library"
            }

    val busy =
        state.isLoading ||
            state.isChecking ||
            isSaving

    ModalBottomSheet(
        onDismissRequest =
            onDismiss,

        modifier =
            Modifier.fillMaxHeight(
                0.94f
            ),

        shape =
            RoundedCornerShape(
                topStart =
                    design.shapes.sheetCorner,

                topEnd =
                    design.shapes.sheetCorner
            ),

        containerColor =
            colors.surface
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(
                        start =
                            design.spacing.medium,

                        end =
                            design.spacing.medium,

                        bottom =
                            design.spacing.medium
                    ),

            verticalArrangement =
                Arrangement.spacedBy(
                    design.spacing.medium
                )
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically,

                horizontalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {
                Surface(
                    modifier =
                        Modifier.size(
                            52.dp
                        ),

                    shape =
                        RoundedCornerShape(
                            16.dp
                        ),

                    color =
                        colors
                            .secondaryContainer
                ) {
                    Box(
                        modifier =
                            Modifier.fillMaxSize(),

                        contentAlignment =
                            Alignment.Center
                    ) {
                        Icon(
                            imageVector =
                                Icons.Rounded.Save,

                            contentDescription =
                                null,

                            modifier =
                                Modifier.size(
                                    27.dp
                                ),

                            tint =
                                colors.secondary
                        )
                    }
                }

                Column(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            2.dp
                        )
                ) {
                    Text(
                        text =
                            "Ghi lời vào bài hát",

                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,

                        fontWeight =
                            FontWeight.Black
                    )

                    Text(
                        text =
                            sourceLabel,

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
                }

                Surface(
                    shape =
                        RoundedCornerShape(
                            14.dp
                        ),

                    color =
                        colors
                            .surfaceVariant
                            .copy(
                                alpha = 0.48f
                            )
                ) {
                    IconButton(
                        onClick =
                            onDismiss,

                        enabled =
                            !busy
                    ) {
                        Icon(
                            imageVector =
                                Icons.Rounded.Close,

                            contentDescription =
                                "Đóng"
                        )
                    }
                }
            }

            OutlinedTextField(
                value =
                    filterQuery,

                onValueChange = {
                    filterQuery =
                        it
                },

                modifier =
                    Modifier.fillMaxWidth(),

                singleLine =
                    true,

                shape =
                    RoundedCornerShape(
                        18.dp
                    ),

                leadingIcon = {
                    Icon(
                        imageVector =
                            Icons.Rounded.Search,

                        contentDescription =
                            null
                    )
                },

                trailingIcon = {
                    if (filterQuery.isNotBlank()) {
                        IconButton(
                            onClick = {
                                filterQuery =
                                    ""
                            }
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Rounded.Clear,

                                contentDescription =
                                    "Xóa tìm kiếm"
                            )
                        }
                    }
                },

                placeholder = {
                    Text(
                        text =
                            "Tìm Title, Artist hoặc tên file"
                    )
                }
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text =
                        "Bài hát phù hợp",

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
                        colors
                            .primary
                            .copy(
                                alpha = 0.16f
                            )
                ) {
                    Text(
                        text =
                            "${filteredCandidates.size} bài",

                        modifier =
                            Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 5.dp
                            ),

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            colors.primary
                    )
                }
            }

            when {
                state.isLoading -> {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(
                                    1f
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {
                        AppLoadingPanel(
                            text =
                                "Đang tìm file MP3 phù hợp..."
                        )
                    }
                }

                state.candidates.isEmpty() -> {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(
                                    1f
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {
                        AppEmptyState(
                            icon =
                                Icons.Rounded.LibraryMusic,

                            title =
                                "Chưa tìm thấy bài phù hợp",

                            description =
                                state.errorMessage
                                    ?: "Không có file MP3 phù hợp trong Library."
                        )
                    }
                }

                filteredCandidates.isEmpty() -> {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(
                                    1f
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {
                        AppEmptyState(
                            icon =
                                Icons.Rounded.Search,

                            title =
                                "Không có bài khớp",

                            description =
                                "Thử tên bài, Artist hoặc tên file khác."
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(
                                    1f
                                ),

                        contentPadding =
                            PaddingValues(
                                vertical = 2.dp
                            ),

                        verticalArrangement =
                            Arrangement.spacedBy(
                                10.dp
                            )
                    ) {
                        items(
                            items =
                                filteredCandidates,

                            key =
                                LibrarySongCandidate::uri
                        ) {
                                candidate ->

                            WriteTargetCandidateRow(
                                candidate =
                                    candidate,

                                selected =
                                    state.selectedTarget
                                        ?.uri ==
                                        candidate.uri,

                                currentSong =
                                    currentSong
                                        ?.uri
                                        ?.takeIf(
                                            String::isNotBlank
                                        ) ==
                                        candidate.uri,

                                enabled =
                                    !busy,

                                onSelect = {
                                    onSelect(
                                        candidate
                                    )
                                }
                            )
                        }
                    }
                }
            }

            if (
                !state.errorMessage
                    .isNullOrBlank() &&
                state.candidates
                    .isNotEmpty()
            ) {
                AppErrorNotice(
                    text =
                        state.errorMessage
                            .orEmpty()
                )
            }

            AppActionButton(
                label =
                    "Ghi lời vào bài đã chọn",

                loadingLabel =
                    if (isSaving) {
                        "Đang ghi lyrics..."
                    }
                    else {
                        "Đang kiểm tra file..."
                    },

                leadingIcon =
                    Icons.Rounded.Save,

                onClick =
                    onConfirm,

                modifier =
                    Modifier.fillMaxWidth(),

                enabled =
                    state.selectedTarget !=
                        null &&
                        !state.isLoading,

                state =
                    if (
                        state.isChecking ||
                        isSaving
                    ) {
                        AppActionVisualState
                            .LOADING
                    }
                    else {
                        AppActionVisualState
                            .IDLE
                    },

                haptic =
                    true
            )
        }
    }
}