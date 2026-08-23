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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    state: com.ngoctien.getmp3.viewmodel.LyricsUiState,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
    onSelectResult:
        (LyricsSearchResult) -> Unit,
    onRetry: () -> Unit
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
                design
                    .spacing
                    .medium
            )
    ) {
        item(
            key = "lyrics-search"
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically,

                horizontalArrangement =
                    Arrangement.spacedBy(
                        design.spacing.small
                    )
            ) {
                StableOutlinedTextField(
                    value =
                        state.query,

                    onValueChange =
                        onQueryChange,

                    modifier =
                        Modifier.weight(1f),

                    singleLine =
                        true,

                    shape =
                        RoundedCornerShape(
                            18.dp
                        ),

                    placeholder = {
                        Text(
                            text =
                                "Tên bài hát hoặc Artist",

                            maxLines =
                                1,

                            overflow =
                                TextOverflow.Ellipsis
                        )
                    },

                    leadingIcon = {
                        Icon(
                            imageVector =
                                Icons.Rounded.Search,

                            contentDescription =
                                null
                        )
                    },

                    trailingIcon = {
                        when {
                            state.isSearchingLyrics -> {
                                CircularProgressIndicator(
                                    modifier =
                                        Modifier.size(
                                            20.dp
                                        ),

                                    strokeWidth =
                                        2.dp
                                )
                            }

                            state.query.isNotEmpty() -> {
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
                    },

                    keyboardOptions =
                        KeyboardOptions(
                            imeAction =
                                ImeAction.Search
                        ),

                    keyboardActions =
                        KeyboardActions(
                            onSearch = {
                                submit()
                            }
                        ),

                    colors =
                        OutlinedTextFieldDefaults
                            .colors(
                                focusedContainerColor =
                                    MaterialTheme
                                        .colorScheme
                                        .surfaceVariant
                                        .copy(
                                            alpha =
                                                0.18f
                                        ),

                                unfocusedContainerColor =
                                    MaterialTheme
                                        .colorScheme
                                        .surfaceVariant
                                        .copy(
                                            alpha =
                                                0.12f
                                        ),

                                focusedBorderColor =
                                    MaterialTheme
                                        .colorScheme
                                        .primary
                                        .copy(
                                            alpha =
                                                0.72f
                                        ),

                                unfocusedBorderColor =
                                    MaterialTheme
                                        .colorScheme
                                        .outline
                                        .copy(
                                            alpha =
                                                0.28f
                                        )
                            )
                )

                AppIconActionButton(
                    icon =
                        Icons.Rounded
                            .ArrowForward,

                    contentDescription =
                        "Tìm lyrics",

                    onClick = {
                        submit()
                    },

                    enabled =
                        state.query
                            .isNotBlank() &&
                            !state
                                .isSearchingLyrics,

                    haptic =
                        true
                )
            }
        }

        when {
            state.isSearchingLyrics -> {
                item(
                    key =
                        "lyrics-online-loading"
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
                        "lyrics-online-header"
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
                                        alpha =
                                            0.12f
                                    ),

                            border =
                                BorderStroke(
                                    1.dp,

                                    MaterialTheme
                                        .colorScheme
                                        .primary
                                        .copy(
                                            alpha =
                                                0.24f
                                        )
                                )
                        ) {
                            Text(
                                text =
                                    "${state.searchResults.size} kết quả",

                                modifier =
                                    Modifier.padding(
                                        horizontal =
                                            9.dp,

                                        vertical =
                                            5.dp
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

                items(
                    items =
                        state.searchResults,

                    key = { result ->
                        if (result.id > 0L) {
                            result.id.toString()
                        }
                        else {
                            buildString {
                                append(
                                    result.trackName
                                )

                                append('|')

                                append(
                                    result.artistName
                                )

                                append('|')

                                append(
                                    result.albumName
                                )
                            }
                        }
                    }
                ) { result ->

                    LyricsOnlineResultCard(
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
                        "lyrics-online-error"
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
                        "lyrics-start-hint"
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = 24.dp
                                ),

                        horizontalAlignment =
                            Alignment.CenterHorizontally,

                        verticalArrangement =
                            Arrangement.spacedBy(
                                8.dp
                            )
                    ) {
                        Surface(
                            modifier =
                                Modifier.size(
                                    54.dp
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
                                        alpha =
                                            0.14f
                                    )
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Rounded
                                        .MusicNote,

                                contentDescription =
                                    null,

                                modifier =
                                    Modifier.padding(
                                        15.dp
                                    ),

                                tint =
                                    MaterialTheme
                                        .colorScheme
                                        .primary
                            )
                        }

                        Text(
                            text =
                                "Tìm lời để hát",

                            style =
                                MaterialTheme
                                    .typography
                                    .titleMedium,

                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            text =
                                "Nhập tên bài hát hoặc Artist rồi chọn đúng phiên bản lyrics.",

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

            else -> {
                item(
                    key =
                        "lyrics-ready-hint"
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
private fun LyricsOnlineResultCard(
    result: LyricsSearchResult,
    onClick: () -> Unit
) {
    val design =
        LocalAppDesign.current

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

            if (
                result.formattedDuration
                    .isNotBlank() &&
                result.formattedDuration !=
                    "--:--"
            ) {
                append(" · ")

                append(
                    result.formattedDuration
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
            MaterialTheme
                .colorScheme
                .surface
                .copy(
                    alpha = 0.82f
                ),

        contentColor =
            MaterialTheme
                .colorScheme
                .onSurface,

        border =
            BorderStroke(
                1.dp,

                MaterialTheme
                    .colorScheme
                    .primary
                    .copy(
                        alpha = 0.24f
                    )
            )
    ) {
        Row(
            modifier =
                Modifier.padding(
                    12.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Surface(
                modifier =
                    Modifier.size(
                        64.dp
                    ),

                shape =
                    RoundedCornerShape(
                        17.dp
                    ),

                color =
                    MaterialTheme
                        .colorScheme
                        .primaryContainer,

                contentColor =
                    MaterialTheme
                        .colorScheme
                        .onPrimaryContainer
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded
                            .MusicNote,

                    contentDescription =
                        null,

                    modifier =
                        Modifier.padding(
                            19.dp
                        )
                )
            }

            Spacer(
                modifier =
                    Modifier.width(
                        design.spacing.medium
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
                        MaterialTheme
                            .colorScheme
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