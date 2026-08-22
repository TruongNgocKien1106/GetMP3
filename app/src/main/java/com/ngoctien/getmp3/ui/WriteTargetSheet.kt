package com.ngoctien.getmp3.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.ngoctien.getmp3.lyrics.LibrarySongCandidate
import com.ngoctien.getmp3.lyrics.LyricsSearchResult
import com.ngoctien.getmp3.ui.components.AppActionButton
import com.ngoctien.getmp3.ui.components.AppActionVisualState
import com.ngoctien.getmp3.ui.components.AppEmptyState
import com.ngoctien.getmp3.ui.components.AppErrorNotice
import com.ngoctien.getmp3.ui.components.AppLoadingPanel
import com.ngoctien.getmp3.ui.design.LocalAppDesign
import com.ngoctien.getmp3.viewmodel.WriteTargetPickerState

private const val InitialWriteTargetCount =
    8

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

    var showAll by
        remember(
            state.candidates
        ) {
            mutableStateOf(
                false
            )
        }

    val visibleCandidates =
        if (showAll) {
            state.candidates
        }
        else {
            state.candidates.take(
                InitialWriteTargetCount
            )
        }

    val busy =
        state.isLoading ||
            state.isChecking ||
            isSaving

    val subtitle =
        selectedLyrics
            ?.let { result ->
                buildString {
                    append(
                        result.trackName
                    )

                    result.artistName
                        .takeIf(
                            String::isNotBlank
                        )
                        ?.let { artist ->
                            append(
                                " · "
                            )

                            append(
                                artist
                            )
                        }
                }
            }
            .orEmpty()

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
            MaterialTheme
                .colorScheme
                .surface
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(
                        start =
                            design.spacing.large,

                        end =
                            design.spacing.large,

                        bottom =
                            design.spacing.medium
                    ),

            verticalArrangement =
                Arrangement.spacedBy(
                    design.spacing.medium
                )
        ) {
            LyricsSectionHeader(
                title =
                    "Ghi lời vào bài hát",

                subtitle =
                    subtitle
                        .ifBlank {
                            "Chọn file MP3 đích"
                        },

                badge =
                    state.candidates
                        .takeIf(
                            List<LibrarySongCandidate>::isNotEmpty
                        )
                        ?.let {
                            "${it.size} bài"
                        }
            )

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
                                Icons.Rounded
                                    .LibraryMusic,

                            title =
                                "Chưa tìm thấy bài phù hợp",

                            description =
                                state.errorMessage
                                    ?: "Không có file MP3 phù hợp trong Library."
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
                                vertical =
                                    design.spacing.tiny
                            ),

                        verticalArrangement =
                            Arrangement.spacedBy(
                                design.spacing.small
                            )
                    ) {
                        items(
                            items =
                                visibleCandidates,

                            key =
                                LibrarySongCandidate::uri
                        ) { candidate ->

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

                        if (
                            !showAll &&
                            state.candidates.size >
                            InitialWriteTargetCount
                        ) {
                            item(
                                key =
                                    "show-all-write-targets"
                            ) {
                                TextButton(
                                    modifier =
                                        Modifier.fillMaxWidth(),

                                    onClick = {
                                        showAll =
                                            true
                                    }
                                ) {
                                    Text(
                                        text =
                                            "Xem tất cả ${state.candidates.size} bài"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible =
                    !state.errorMessage
                        .isNullOrBlank() &&
                        state.candidates
                            .isNotEmpty(),

                enter =
                    expandVertically() +
                        fadeIn(),

                exit =
                    shrinkVertically() +
                        fadeOut()
            ) {
                state.errorMessage
                    ?.let { message ->
                        AppErrorNotice(
                            text =
                                message
                        )
                    }
            }

            HorizontalDivider()

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
