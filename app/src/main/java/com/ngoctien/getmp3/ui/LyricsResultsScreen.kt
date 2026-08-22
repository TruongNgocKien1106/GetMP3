package com.ngoctien.getmp3.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.lyrics.LyricsSearchResult
import com.ngoctien.getmp3.ui.components.AppEmptyState
import com.ngoctien.getmp3.ui.components.AppErrorNotice
import com.ngoctien.getmp3.ui.components.AppLoadingPanel
import com.ngoctien.getmp3.ui.design.LocalAppDesign
import com.ngoctien.getmp3.viewmodel.LyricsUiState
import kotlin.math.roundToInt

@Composable
internal fun LyricsResultsScreen(
    state: LyricsUiState,
    onSelectResult:
        (LyricsSearchResult) -> Unit,
    onRetry: () -> Unit
) {
    val design =
        LocalAppDesign.current

    LazyColumn(
        modifier =
            Modifier.fillMaxSize(),

        contentPadding =
            PaddingValues(
                bottom =
                    design
                        .spacing
                        .extraLarge
            ),

        verticalArrangement =
            Arrangement.spacedBy(
                design
                    .spacing
                    .medium
            )
    ) {
        state.selectedSong
            ?.let { song ->

                item(
                    key =
                        "selected-song"
                ) {
                    Surface(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .animateContentSize(),

                        shape =
                            RoundedCornerShape(
                                design
                                    .shapes
                                    .controlCorner
                            ),

                        color =
                            MaterialTheme
                                .colorScheme
                                .secondaryContainer,

                        contentColor =
                            MaterialTheme
                                .colorScheme
                                .onSecondaryContainer
                    ) {
                        Row(
                            modifier =
                                Modifier.padding(
                                    horizontal =
                                        design
                                            .spacing
                                            .medium,

                                    vertical =
                                        design
                                            .spacing
                                            .small
                                ),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Column(
                                modifier =
                                    Modifier.weight(
                                        1f
                                    ),

                                verticalArrangement =
                                    Arrangement.spacedBy(
                                        design
                                            .spacing
                                            .tiny
                                    )
                            ) {
                                Text(
                                    text =
                                        song.title,

                                    maxLines =
                                        1,

                                    overflow =
                                        TextOverflow
                                            .Ellipsis,

                                    fontWeight =
                                        FontWeight
                                            .SemiBold
                                )

                                Text(
                                    text =
                                        song.artist
                                            .ifBlank {
                                                song.displayName
                                            },

                                    maxLines =
                                        1,

                                    overflow =
                                        TextOverflow
                                            .Ellipsis,

                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodySmall,

                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onSecondaryContainer
                                            .copy(
                                                alpha =
                                                    0.80f
                                            )
                                )
                            }

                            LyricsPill(
                                text =
                                    "Từ Library",

                                emphasized =
                                    true
                            )
                        }
                    }
                }
            }

        when {
            state.isSearchingLyrics -> {

                item(
                    key =
                        "lyrics-loading"
                ) {
                    AppLoadingPanel(
                        text =
                            "Đang tìm và xếp hạng lời bài hát..."
                    )
                }
            }

            state.searchResults
                .isNotEmpty() -> {

                item(
                    key =
                        "lyrics-results-header"
                ) {
                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

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
                                    "Kết quả phù hợp",

                                style =
                                    MaterialTheme
                                        .typography
                                        .titleMedium,

                                fontWeight =
                                    FontWeight.Bold
                            )

                            Text(
                                text =
                                    "Chọn đúng phiên bản trước khi mở lyrics",

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

                        LyricsPill(
                            text =
                                "${state.searchResults.size} kết quả",

                            emphasized =
                                false
                        )
                    }
                }

                items(
                    items =
                        state.searchResults,

                    key = {
                        if (it.id > 0L) {
                            it.id.toString()
                        }
                        else {
                            "${it.trackName}|${it.artistName}|${it.id}"
                        }
                    }
                ) { result ->

                    LyricsResultRow(
                        result =
                            result,

                        isBestMatch =
                            result ==
                                state
                                    .searchResults
                                    .firstOrNull(),

                        onClick = {
                            onSelectResult(
                                result
                            )
                        }
                    )
                }
            }

            state.errorMessage != null -> {

                item(
                    key =
                        "lyrics-error"
                ) {
                    AppErrorNotice(
                        text =
                            state.errorMessage,

                        actionText =
                            "Thử lại",

                        onAction =
                            onRetry
                    )
                }
            }

            else -> {

                item(
                    key =
                        "lyrics-empty"
                ) {
                    AppEmptyState(
                        icon =
                            Icons.Rounded
                                .LibraryMusic,

                        title =
                            "Chưa có kết quả lyrics",

                        description =
                            "Quay lại và chọn hoặc tìm một bài hát để tra lời."
                    )
                }
            }
        }
    }
}

@Composable
private fun LyricsResultRow(
    result: LyricsSearchResult,
    isBestMatch: Boolean,
    onClick: () -> Unit
) {
    val design =
        LocalAppDesign.current

    val lyricsType =
        if (
            result.syncedLyrics
                .orEmpty()
                .isNotBlank()
        ) {
            "Đồng bộ"
        }
        else {
            "Lời thường"
        }

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .animateContentSize()
                .bouncyClickable(
                    onClick =
                        onClick
                ),

        shape =
            RoundedCornerShape(
                design
                    .shapes
                    .cardCorner
            ),

        color =
            if (isBestMatch) {
                MaterialTheme
                    .colorScheme
                    .primaryContainer
                    .copy(
                        alpha =
                            0.38f
                    )
            }
            else {
                MaterialTheme
                    .colorScheme
                    .surface
            },

        contentColor =
            MaterialTheme
                .colorScheme
                .onSurface,

        border =
            BorderStroke(
                width =
                    1.dp,

                color =
                    if (isBestMatch) {
                        MaterialTheme
                            .colorScheme
                            .primary
                            .copy(
                                alpha =
                                    0.55f
                            )
                    }
                    else {
                        MaterialTheme
                            .colorScheme
                            .outline
                            .copy(
                                alpha =
                                    0.24f
                            )
                    }
            ),

        tonalElevation =
            if (isBestMatch) {
                2.dp
            }
            else {
                0.dp
            }
    ) {
        Row(
            modifier =
                Modifier.padding(
                    design
                        .spacing
                        .medium
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Column(
                modifier =
                    Modifier.weight(
                        1f
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        design
                            .spacing
                            .tiny
                    )
            ) {
                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        text =
                            result.trackName,

                        modifier =
                            Modifier.weight(
                                1f
                            ),

                        maxLines =
                            1,

                        overflow =
                            TextOverflow
                                .Ellipsis,

                        fontWeight =
                            FontWeight
                                .SemiBold
                    )

                    Spacer(
                        modifier =
                            Modifier.width(
                                design
                                    .spacing
                                    .small
                            )
                    )

                    Text(
                        text =
                            "${
                                (
                                    result.score *
                                        100
                                ).roundToInt()
                            }%",

                        style =
                            MaterialTheme
                                .typography
                                .labelLarge,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }

                Text(
                    text =
                        buildString {
                            append(
                                result.artistName
                                    .ifBlank {
                                        "Không rõ Artist"
                                    }
                            )

                            if (
                                result.albumName
                                    .isNotBlank()
                            ) {
                                append(
                                    " · "
                                )

                                append(
                                    result.albumName
                                )
                            }
                        },

                    maxLines =
                        1,

                    overflow =
                        TextOverflow
                            .Ellipsis,

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            design
                                .spacing
                                .small
                        ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    LyricsPill(
                        text =
                            lyricsType,

                        emphasized =
                            false
                    )

                    if (isBestMatch) {
                        LyricsPill(
                            text =
                                "Khớp cao nhất",

                            emphasized =
                                true
                        )
                    }

                    result.formattedDuration
                        .takeIf {
                            it.isNotBlank()
                        }
                        ?.let { duration ->

                            Text(
                                text =
                                    duration,

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
                }
            }

            Spacer(
                modifier =
                    Modifier.width(
                        design
                            .spacing
                            .small
                    )
            )

            Icon(
                imageVector =
                    Icons.Rounded
                        .ArrowForward,

                contentDescription =
                    "Mở lyrics",

                tint =
                    MaterialTheme
                        .colorScheme
                        .primary
            )
        }
    }
}
