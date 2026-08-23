package com.ngoctien.getmp3.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.lyrics.LyricsSearchResult
import com.ngoctien.getmp3.ui.components.AppAnimatedStatusText
import com.ngoctien.getmp3.ui.components.AppErrorNotice
import com.ngoctien.getmp3.ui.components.AppIconActionButton
import com.ngoctien.getmp3.ui.design.LocalAppDesign


@Composable
internal fun LyricsLibraryScreen(
    state:
        com.ngoctien.getmp3.viewmodel.LyricsUiState,
    onQueryChange:
        (String) -> Unit,
    onClear:
        () -> Unit,
    onSubmit:
        () -> Unit,
    onSelectResult:
        (LyricsSearchResult) -> Unit,
    onRetry:
        () -> Unit
) {
    val design =
        LocalAppDesign.current

    val focusManager =
        LocalFocusManager.current

    val keyboardController =
        LocalSoftwareKeyboardController.current

    fun submit() {
        if (
            state.query.isBlank() ||
            state.isSearchingLyrics
        ) {
            return
        }

        focusManager.clearFocus(
            force = true
        )

        keyboardController?.hide()

        onSubmit()
    }

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
                14.dp
            )
    ) {
        item(
            key = "lyrics-search"
        ) {
            LyricsSearchBar(
                value =
                    state.query,

                loading =
                    state.isSearchingLyrics,

                onValueChange =
                    onQueryChange,

                onClear =
                    onClear,

                onSubmit = {
                    submit()
                }
            )
        }

        when {
            state.isSearchingLyrics -> {
                item(
                    key =
                        "lyrics-loading"
                ) {
                    AppAnimatedStatusText(
                        text =
                            "Đang tìm lời bài hát...",

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }
            }

            state.searchResults
                .isNotEmpty() -> {

                item(
                    key =
                        "lyrics-results-header"
                ) {
                    LyricsResultsHeader(
                        count =
                            state.searchResults
                                .size
                    )
                }

                itemsIndexed(
                    items =
                        state.searchResults,

                    key = {
                            index,
                            result ->

                        buildString {
                            append(result.id)
                            append('|')
                            append(
                                result.trackName
                            )
                            append('|')
                            append(
                                result.artistName
                            )
                            append('|')
                            append(index)
                        }
                    }
                ) {
                        _,
                        result ->

                    LyricsResultCard(
                        result =
                            result,

                        onClick = {
                            onSelectResult(
                                result
                            )
                        }
                    )
                }
            }

            state.errorMessage
                ?.isNotBlank() == true -> {

                item(
                    key =
                        "lyrics-error"
                ) {
                    AppErrorNotice(
                        text =
                            state.errorMessage
                                .orEmpty(),

                        actionText =
                            "Thử lại",

                        onAction =
                            onRetry
                    )
                }
            }

            state.query.isBlank() -> {
                item(
                    key =
                        "lyrics-start"
                ) {
                    LyricsStartHint()
                }
            }

            else -> {
                item(
                    key =
                        "lyrics-ready"
                ) {
                    Text(
                        text =
                            "Nhấn mũi tên hoặc Search trên bàn phím để tìm lời.",

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
        }
    }
}


@Composable
private fun LyricsSearchBar(
    value: String,
    loading: Boolean,
    onValueChange:
        (String) -> Unit,
    onClear:
        () -> Unit,
    onSubmit:
        () -> Unit
) {
    val colors =
        MaterialTheme.colorScheme

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(
                    min = 60.dp
                ),

        shape =
            RoundedCornerShape(
                18.dp
            ),

        color =
            colors
                .surfaceVariant
                .copy(
                    alpha = 0.12f
                ),

        border =
            BorderStroke(
                1.dp,

                colors
                    .primary
                    .copy(
                        alpha = 0.30f
                    )
            )
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 14.dp,
                        top = 6.dp,
                        end = 7.dp,
                        bottom = 6.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.Search,

                contentDescription =
                    null,

                modifier =
                    Modifier.size(
                        22.dp
                    ),

                tint =
                    colors
                        .onSurfaceVariant
            )

            BasicTextField(
                value =
                    value,

                onValueChange =
                    onValueChange,

                modifier =
                    Modifier
                        .weight(1f)
                        .padding(
                            horizontal = 11.dp,
                            vertical = 10.dp
                        ),

                singleLine =
                    true,

                textStyle =
                    MaterialTheme
                        .typography
                        .bodyLarge
                        .copy(
                            color =
                                colors
                                    .onSurface
                        ),

                cursorBrush =
                    SolidColor(
                        colors.primary
                    ),

                keyboardOptions =
                    KeyboardOptions(
                        imeAction =
                            ImeAction.Search
                    ),

                keyboardActions =
                    KeyboardActions(
                        onSearch = {
                            onSubmit()
                        }
                    ),

                decorationBox = {
                        innerTextField ->

                    Box(
                        contentAlignment =
                            Alignment.CenterStart
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text =
                                    "Tên bài hát hoặc Artist",

                                maxLines =
                                    1,

                                overflow =
                                    TextOverflow.Ellipsis,

                                style =
                                    MaterialTheme
                                        .typography
                                        .bodyLarge,

                                color =
                                    colors
                                        .onSurfaceVariant
                                        .copy(
                                            alpha = 0.78f
                                        )
                            )
                        }

                        innerTextField()
                    }
                }
            )

            when {
                loading -> {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(
                                21.dp
                            ),

                        strokeWidth =
                            2.dp
                    )

                    Spacer(
                        modifier =
                            Modifier.width(
                                7.dp
                            )
                    )
                }

                value.isNotEmpty() -> {
                    AppIconActionButton(
                        icon =
                            Icons.Rounded.Clear,

                        contentDescription =
                            "Xóa tìm kiếm",

                        onClick =
                            onClear,

                        haptic =
                            false
                    )
                }
            }

            AppIconActionButton(
                icon =
                    Icons.Rounded
                        .ArrowForward,

                contentDescription =
                    "Tìm lyrics",

                onClick =
                    onSubmit,

                enabled =
                    value.isNotBlank() &&
                    !loading,

                haptic =
                    true
            )
        }
    }
}


@Composable
private fun LyricsResultsHeader(
    count: Int
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Row(
            modifier =
                Modifier.weight(1f),

            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {
            Surface(
                modifier =
                    Modifier.size(
                        7.dp
                    ),

                shape =
                    RoundedCornerShape(
                        99.dp
                    ),

                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            ) {}

            Text(
                text =
                    "Kết quả",

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.Black
            )
        }

        Surface(
            shape =
                RoundedCornerShape(
                    99.dp
                ),

            color =
                MaterialTheme
                    .colorScheme
                    .primary
                    .copy(
                        alpha = 0.12f
                    ),

            border =
                BorderStroke(
                    1.dp,

                    MaterialTheme
                        .colorScheme
                        .primary
                        .copy(
                            alpha = 0.22f
                        )
                )
        ) {
            Text(
                text =
                    "$count kết quả",

                modifier =
                    Modifier.padding(
                        horizontal = 9.dp,
                        vertical = 5.dp
                    ),

                style =
                    MaterialTheme
                        .typography
                        .labelSmall,

                fontWeight =
                    FontWeight.Bold,

                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )
        }
    }
}


@Composable
private fun LyricsResultCard(
    result: LyricsSearchResult,
    onClick: () -> Unit
) {
    val colors =
        MaterialTheme.colorScheme

    val metadata =
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
                append(" · ")

                append(
                    result.albumName
                )
            }
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
                20.dp
            ),

        color =
            colors
                .surface
                .copy(
                    alpha = 0.74f
                ),

        contentColor =
            colors.onSurface,

        border =
            BorderStroke(
                1.dp,

                colors
                    .primary
                    .copy(
                        alpha = 0.24f
                    )
            )
    ) {
        Row(
            modifier =
                Modifier.padding(
                    11.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            LyricsArtwork(
                result =
                    result,

                size =
                    64
            )

            Spacer(
                modifier =
                    Modifier.width(
                        11.dp
                    )
            )

            Column(
                modifier =
                    Modifier.weight(1f),

                verticalArrangement =
                    Arrangement.spacedBy(
                        4.dp
                    )
            ) {
                Text(
                    text =
                        result.trackName,

                    maxLines =
                        2,

                    overflow =
                        TextOverflow.Ellipsis,

                    style =
                        MaterialTheme
                            .typography
                            .titleSmall,

                    fontWeight =
                        FontWeight.Black
                )

                Text(
                    text =
                        metadata,

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

            Spacer(
                modifier =
                    Modifier.width(
                        8.dp
                    )
            )

            AppIconActionButton(
                icon =
                    Icons.Rounded
                        .ArrowForward,

                contentDescription =
                    "Mở lyrics",

                onClick =
                    onClick,

                haptic =
                    true
            )
        }
    }
}


@Composable
private fun LyricsStartHint() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    top = 150.dp
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.spacedBy(
                9.dp
            )
    ) {
        Surface(
            modifier =
                Modifier.size(
                    56.dp
                ),

            shape =
                RoundedCornerShape(
                    17.dp
                ),

            color =
                MaterialTheme
                    .colorScheme
                    .primary
                    .copy(
                        alpha = 0.15f
                    ),

            border =
                BorderStroke(
                    1.dp,

                    MaterialTheme
                        .colorScheme
                        .primary
                        .copy(
                            alpha = 0.25f
                        )
                )
        ) {
            Box(
                modifier =
                    Modifier.fillMaxSize(),

                contentAlignment =
                    Alignment.Center
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.Search,

                    contentDescription =
                        null,

                    modifier =
                        Modifier.size(
                            29.dp
                        ),

                    tint =
                        MaterialTheme
                            .colorScheme
                            .primary
                )
            }
        }

        Text(
            text =
                "Tìm lời bài hát",

            style =
                MaterialTheme
                    .typography
                    .titleMedium,

            fontWeight =
                FontWeight.Bold
        )

        Text(
            text =
                "Nhập tên bài hoặc Artist để bắt đầu",

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