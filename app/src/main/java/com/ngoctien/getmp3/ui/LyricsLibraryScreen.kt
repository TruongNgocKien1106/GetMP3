package com.ngoctien.getmp3.ui

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.LibraryMusic
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
import com.ngoctien.getmp3.lyrics.LibrarySongCandidate
import com.ngoctien.getmp3.ui.components.AppActionButton
import com.ngoctien.getmp3.ui.components.AppAnimatedStatusText
import com.ngoctien.getmp3.ui.components.AppCard
import com.ngoctien.getmp3.ui.components.AppEmptyState
import com.ngoctien.getmp3.ui.components.AppErrorNotice
import com.ngoctien.getmp3.ui.components.AppIconActionButton
import com.ngoctien.getmp3.ui.design.LocalAppDesign
import kotlin.math.roundToInt

@Composable
internal fun LyricsLibraryScreen(
    state: com.ngoctien.getmp3.viewmodel.LyricsUiState,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
    onSelectSong: (LibrarySongCandidate) -> Unit
) {
    val design =
        LocalAppDesign.current

    val focusManager =
        LocalFocusManager.current

    val keyboardController =
        LocalSoftwareKeyboardController.current

    fun submit() {
        if (state.query.isBlank()) {
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
                    design.spacing.extraLarge
            ),

        verticalArrangement =
            Arrangement.spacedBy(
                design.spacing.medium
            )
    ) {
        item(
            key = "lyrics-search"
        ) {
            AppCard(
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(
                            design.spacing.medium
                        )
                ) {
                    Column(
                        verticalArrangement =
                            Arrangement.spacedBy(
                                design.spacing.tiny
                            )
                    ) {
                        Text(
                            text =
                                "Tìm lyrics",

                            style =
                                MaterialTheme
                                    .typography
                                    .titleMedium,

                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            text =
                                "Nhập tên bài hát hoặc Artist. Bạn vẫn tự chọn đúng bài trước khi mở lyrics.",

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

                    StableOutlinedTextField(
                        value =
                            state.query,

                        onValueChange =
                            onQueryChange,

                        modifier =
                            Modifier.fillMaxWidth(),

                        singleLine =
                            true,

                        shape =
                            RoundedCornerShape(
                                design
                                    .shapes
                                    .controlCorner
                            ),

                        label = {
                            Text(
                                "Tên bài hát hoặc Artist"
                            )
                        },

                        placeholder = {
                            Text(
                                "Ví dụ: Nơi Này Có Anh"
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
                                state
                                    .isLoadingSuggestions -> {

                                    CircularProgressIndicator(
                                        modifier =
                                            Modifier.size(
                                                20.dp
                                            ),

                                        strokeWidth =
                                            2.dp
                                    )
                                }

                                state.query
                                    .isNotEmpty() -> {

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
                            OutlinedTextFieldDefaults.colors(
                                focusedContainerColor =
                                    MaterialTheme
                                        .colorScheme
                                        .surfaceVariant
                                        .copy(
                                            alpha = 0.20f
                                        ),

                                unfocusedContainerColor =
                                    MaterialTheme
                                        .colorScheme
                                        .surfaceVariant
                                        .copy(
                                            alpha = 0.12f
                                        )
                            )
                    )

                    AppActionButton(
                        label =
                            "Tra lời bài hát",

                        onClick = {
                            submit()
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        enabled =
                            state.query.isNotBlank(),

                        leadingIcon =
                            Icons.Rounded.Search,

                        haptic =
                            true
                    )
                }
            }
        }

        state.errorMessage
            ?.takeIf(
                String::isNotBlank
            )
            ?.let { message ->

                item(
                    key = "library-error"
                ) {
                    AppErrorNotice(
                        text = message
                    )
                }
            }

        when {
            state.suggestions
                .isNotEmpty() -> {

                item(
                    key =
                        "library-suggestion-header"
                ) {
                    LyricsSectionHeader(
                        title =
                            "Gợi ý từ Library",

                        subtitle =
                            "Chạm đúng bài để tiếp tục",

                        badge =
                            "${state.suggestions.size} bài"
                    )
                }

                items(
                    items =
                        state.suggestions,

                    key =
                        LibrarySongCandidate::uri
                ) { song ->

                    LibrarySongCard(
                        song =
                            song,

                        onClick = {
                            onSelectSong(
                                song
                            )
                        }
                    )
                }
            }

            state.isLoadingSuggestions -> {

                item(
                    key =
                        "library-loading"
                ) {
                    AppAnimatedStatusText(
                        text =
                            "Đang tìm bài phù hợp trong Library...",

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }
            }

            state.query.length < 2 -> {

                item(
                    key =
                        "library-empty-start"
                ) {
                    AppEmptyState(
                        icon =
                            Icons.Rounded
                                .LibraryMusic,

                        title =
                            "Tìm bài trong Library",

                        description =
                            "Nhập ít nhất 2 ký tự để xem đề xuất. App không tự chọn bài thay bạn."
                    )
                }
            }

            else -> {

                item(
                    key =
                        "library-empty-result"
                ) {
                    AppEmptyState(
                        icon =
                            Icons.Rounded
                                .LibraryMusic,

                        title =
                            "Không có bài phù hợp",

                        description =
                            "Bạn vẫn có thể nhấn Tra lời bài hát để tìm trực tiếp bằng tên đang nhập."
                    )
                }
            }
        }
    }
}



@Composable
private fun LibrarySongCard(
    song: LibrarySongCandidate,
    onClick: () -> Unit
) {
    val design =
        LocalAppDesign.current

    AppCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .animateContentSize(),

        onClick =
            onClick,

        haptic =
            true,

        contentPadding =
            PaddingValues(
                horizontal =
                    design.spacing.medium,

                vertical =
                    design.spacing.small
            )
    ) {
        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Surface(
                modifier =
                    Modifier.size(
                        46.dp
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
                            design.spacing.medium
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
                    Modifier.weight(
                        1f
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        design.spacing.tiny
                    )
            ) {
                Text(
                    text =
                        song.title,

                    maxLines =
                        2,

                    overflow =
                        TextOverflow.Ellipsis,

                    fontWeight =
                        FontWeight.SemiBold
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
                        design.spacing.small
                    )
            )

            Surface(
                shape =
                    CircleShape,

                color =
                    MaterialTheme
                        .colorScheme
                        .secondaryContainer,

                contentColor =
                    MaterialTheme
                        .colorScheme
                        .onSecondaryContainer
            ) {
                Text(
                    text =
                        "${
                            (
                                song.score *
                                    100
                            ).roundToInt()
                        }%",

                    modifier =
                        Modifier.padding(
                            horizontal =
                                design.spacing.small,

                            vertical =
                                design.spacing.tiny
                        ),

                    style =
                        MaterialTheme
                            .typography
                            .labelMedium,

                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}
