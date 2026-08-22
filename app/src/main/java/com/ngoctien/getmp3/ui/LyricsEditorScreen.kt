package com.ngoctien.getmp3.ui

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.ui.components.AppActionButton
import com.ngoctien.getmp3.ui.components.AppActionVisualState
import com.ngoctien.getmp3.ui.components.AppAnimatedStatusText
import com.ngoctien.getmp3.ui.components.AppCard
import com.ngoctien.getmp3.ui.components.AppErrorNotice
import com.ngoctien.getmp3.ui.components.AppIconActionButton
import com.ngoctien.getmp3.ui.components.AppLoadingPanel
import com.ngoctien.getmp3.ui.design.LocalAppDesign
import com.ngoctien.getmp3.viewmodel.LyricsUiState

@Composable
internal fun LyricsEditorScreen(
    state: LyricsUiState,
    onLyricsChange: (String) -> Unit,
    onPasteLyrics: (String?) -> Unit,
    onClear: () -> Unit,
    onRestoreFoundLyrics: () -> Unit,
    onRestoreStoredLyrics: () -> Unit,
    onSave: () -> Unit
) {
    val design =
        LocalAppDesign.current

    val clipboard =
        LocalClipboardManager.current

    val target =
        state.editorTarget

    val lineCount =
        state.editorLyrics
            .lineSequence()
            .count()

    val hasUnsavedChanges =
        state.editorLyrics
            .trim() !=
            state.existingLyrics
                .trim()

    var showEditorMenu by
        remember {
            mutableStateOf(
                false
            )
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
        target
            ?.let { song ->

                item(
                    key =
                        "editor-target"
                ) {
                    EditorTargetCard(
                        title =
                            song.title,

                        artist =
                            song.artist,

                        displayName =
                            song.displayName
                    )
                }
            }

        if (state.isLoadingFile) {
            item(
                key =
                    "editor-loading"
            ) {
                AppLoadingPanel(
                    text =
                        "Đang đọc lyrics hiện tại trong MP3..."
                )
            }

            return@LazyColumn
        }

        if (
            state.existingLyrics
                .isNotBlank()
        ) {
            item(
                key =
                    "existing-lyrics-info"
            ) {
                LyricsInfoCard(
                    icon =
                        Icons.Rounded
                            .LibraryMusic,

                    title =
                        "File đã có lyrics",

                    message =
                        "${
                            state.existingLyrics
                                .lineSequence()
                                .count()
                        } dòng hiện tại. Khi ghi nội dung mới, app vẫn hỏi xác nhận trước khi thay thế."
                )
            }
        }

        item(
            key =
                "editor-heading"
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
                        ),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            design.spacing.tiny
                        )
                ) {
                    Text(
                        text =
                            "Chỉnh lyrics",

                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            "Lyrics được nạp từ file hoặc kết quả đã chọn. Chỉ chỉnh khi cần.",

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

                Box {
                    AppIconActionButton(
                        icon =
                            Icons.Rounded
                                .MoreVert,

                        contentDescription =
                            "Tùy chọn chỉnh lyrics",

                        onClick = {
                            showEditorMenu =
                                true
                        },

                        haptic =
                            false
                    )

                    DropdownMenu(
                        expanded =
                            showEditorMenu,

                        onDismissRequest = {
                            showEditorMenu =
                                false
                        }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Dán từ clipboard"
                                )
                            },

                            leadingIcon = {
                                Icon(
                                    imageVector =
                                        Icons.Rounded
                                            .ContentPaste,

                                    contentDescription =
                                        null
                                )
                            },

                            onClick = {
                                showEditorMenu =
                                    false

                                onPasteLyrics(
                                    clipboard
                                        .getText()
                                        ?.text
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Khôi phục lyrics đã tìm"
                                )
                            },

                            leadingIcon = {
                                Icon(
                                    imageVector =
                                        Icons.Rounded
                                            .Refresh,

                                    contentDescription =
                                        null
                                )
                            },

                            enabled =
                                state.selectedResult
                                    ?.readableLyrics
                                    ?.isNotBlank() ==
                                    true,

                            onClick = {
                                showEditorMenu =
                                    false

                                onRestoreFoundLyrics()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Khôi phục lyrics trong file"
                                )
                            },

                            leadingIcon = {
                                Icon(
                                    imageVector =
                                        Icons.Rounded
                                            .LibraryMusic,

                                    contentDescription =
                                        null
                                )
                            },

                            enabled =
                                state.existingLyrics
                                    .isNotBlank(),

                            onClick = {
                                showEditorMenu =
                                    false

                                onRestoreStoredLyrics()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Xóa nội dung"
                                )
                            },

                            leadingIcon = {
                                Icon(
                                    imageVector =
                                        Icons.Rounded
                                            .Clear,

                                    contentDescription =
                                        null
                                )
                            },

                            enabled =
                                state.editorLyrics
                                    .isNotBlank(),

                            onClick = {
                                showEditorMenu =
                                    false

                                onClear()
                            }
                        )
                    }
                }
            }
        }

        item(
            key =
                "editor-field"
        ) {
            StableOutlinedTextField(
                value =
                    state.editorLyrics,

                onValueChange =
                    onLyricsChange,

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(
                            min = 360.dp
                        ),

                minLines =
                    16,

                shape =
                    RoundedCornerShape(
                        design
                            .shapes
                            .cardCorner
                    ),

                label = {
                    Text(
                        "Nội dung lyrics"
                    )
                },

                placeholder = {
                    Text(
                        "Lyrics sẽ xuất hiện ở đây..."
                    )
                },

                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedContainerColor =
                            MaterialTheme
                                .colorScheme
                                .surfaceVariant
                                .copy(
                                    alpha = 0.14f
                                ),

                        unfocusedContainerColor =
                            MaterialTheme
                                .colorScheme
                                .surfaceVariant
                                .copy(
                                    alpha = 0.08f
                                )
                    )
            )
        }

        item(
            key =
                "editor-stats"
        ) {
            AppAnimatedStatusText(
                text =
                    "${state.editorLyrics.length} ký tự • $lineCount dòng",

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }

        state.successMessage
            ?.takeIf(
                String::isNotBlank
            )
            ?.let { message ->

                item(
                    key =
                        "editor-success"
                ) {
                    LyricsInfoCard(
                        icon =
                            Icons.Rounded
                                .CheckCircle,

                        title =
                            "Ghi thành công",

                        message =
                            message,

                        emphasized =
                            true
                    )
                }
            }

        if (
            state.successMessage ==
                null &&
            hasUnsavedChanges &&
            state.editorLyrics
                .isNotBlank()
        ) {
            item(
                key =
                    "editor-unsaved"
            ) {
                LyricsInfoCard(
                    icon =
                        Icons.Rounded
                            .Edit,

                    title =
                        "Có thay đổi chưa lưu",

                    message =
                        "Kiểm tra nội dung rồi nhấn Ghi vào MP3.",

                    emphasized =
                        false
                )
            }
        }

        state.errorMessage
            ?.takeIf(
                String::isNotBlank
            )
            ?.let { message ->

                item(
                    key =
                        "editor-error"
                ) {
                    AppErrorNotice(
                        text =
                            message
                    )
                }
            }

        item(
            key =
                "editor-save"
        ) {
            val visualState =
                when {
                    state.isSaving ->
                        AppActionVisualState
                            .LOADING

                    state.successMessage !=
                        null &&
                    !hasUnsavedChanges ->
                        AppActionVisualState
                            .SUCCESS

                    else ->
                        AppActionVisualState
                            .IDLE
                }

            AppActionButton(
                label =
                    "Ghi vào MP3",

                onClick =
                    onSave,

                modifier =
                    Modifier.fillMaxWidth(),

                enabled =
                    state.editorLyrics
                        .isNotBlank() &&
                    (
                        hasUnsavedChanges ||
                        visualState ==
                            AppActionVisualState.SUCCESS
                    ),

                state =
                    visualState,

                loadingLabel =
                    "Đang ghi lyrics...",

                successLabel =
                    "Đã ghi vào MP3",

                leadingIcon =
                    Icons.Rounded.Save,

                haptic =
                    true
            )
        }
    }
}

@Composable
private fun EditorTargetCard(
    title: String,
    artist: String,
    displayName: String
) {
    val design =
        LocalAppDesign.current

    AppCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .animateContentSize()
    ) {
        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Surface(
                modifier =
                    Modifier.size(
                        48.dp
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
                        .secondaryContainer,

                contentColor =
                    MaterialTheme
                        .colorScheme
                        .onSecondaryContainer
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.Edit,

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
                        title,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis,

                    fontWeight =
                        FontWeight.Bold
                )

                if (artist.isNotBlank()) {
                    Text(
                        text =
                            artist,

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

                Text(
                    text =
                        displayName,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis,

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
}
