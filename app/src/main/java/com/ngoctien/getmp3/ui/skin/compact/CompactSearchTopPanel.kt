package com.ngoctien.getmp3.ui.skin.compact

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.ui.AppMotion
import com.ngoctien.getmp3.ui.StableOutlinedTextField
import com.ngoctien.getmp3.ui.design.LocalAppDesign
import com.ngoctien.getmp3.viewmodel.FfmpegReadyState

private enum class CompactSearchStatus {
    READY,
    SEARCHING,
    SEARCHING_REFERENCE,
    PREPARING,
    FFMPEG_CHECKING,
    FFMPEG_FAILED
}

@Composable
internal fun CompactSearchTopPanel(
    query: String,
    isSearching: Boolean,
    isSearchingReference: Boolean,
    isPreparingDownload: Boolean,
    ffmpegState: FfmpegReadyState,
    ffmpegMessage: String,
    referenceMessage: String?,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onRetryFfmpeg: () -> Unit
) {
    val design =
        LocalAppDesign.current

    val focusManager =
        LocalFocusManager.current

    val keyboardController =
        LocalSoftwareKeyboardController.current

    val status =
        when {
            isPreparingDownload ->
                CompactSearchStatus.PREPARING

            ffmpegState ==
                FfmpegReadyState.FAILED ->
                CompactSearchStatus.FFMPEG_FAILED

            ffmpegState ==
                FfmpegReadyState.CHECKING ->
                CompactSearchStatus.FFMPEG_CHECKING

            isSearching ->
                CompactSearchStatus.SEARCHING

            isSearchingReference ->
                CompactSearchStatus.SEARCHING_REFERENCE

            else ->
                CompactSearchStatus.READY
        }

    fun submitSearch() {
        if (
            query.isBlank() ||
            isSearching
        ) {
            return
        }

        focusManager.clearFocus(
            force = true
        )

        keyboardController?.hide()

        onSearch()
    }

    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(
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
                    horizontal =
                        design.spacing.cardPadding,

                    vertical =
                        design.spacing.medium
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    design.spacing.small
                )
        ) {
            StableOutlinedTextField(
                value =
                    query,

                onValueChange =
                    onQueryChange,

                modifier =
                    Modifier.fillMaxWidth(),

                singleLine =
                    true,

                shape =
                    androidx.compose.foundation.shape
                        .RoundedCornerShape(
                            design.shapes.controlCorner
                        ),

                label = {
                    Text(
                        "Tên bài hát hoặc Artist"
                    )
                },

                placeholder = {
                    Text(
                        "Ví dụ: Nơi Này Có Anh Sơn Tùng M-TP"
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
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick =
                                onClear
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Rounded.Close,

                                contentDescription =
                                    "Xóa nội dung tìm kiếm"
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
                            submitSearch()
                        }
                    )
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        design.spacing.small
                    ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        submitSearch()
                    },

                    enabled =
                        query.isNotBlank() &&
                            !isSearching,

                    modifier =
                        Modifier
                            .weight(1f)
                            .height(46.dp)
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(17.dp),

                            strokeWidth =
                                2.dp
                        )

                        Spacer(
                            modifier =
                                Modifier.size(7.dp)
                        )
                    }
                    else {
                        Icon(
                            imageVector =
                                Icons.Rounded.Search,

                            contentDescription =
                                null,

                            modifier =
                                Modifier.size(18.dp)
                        )

                        Spacer(
                            modifier =
                                Modifier.size(7.dp)
                        )
                    }

                    Text(
                        text =
                            if (isSearching) {
                                "Đang tìm"
                            }
                            else {
                                "Tìm"
                            },

                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }

            AnimatedContent(
                targetState =
                    status,

                transitionSpec = {
                    fadeIn(
                        animationSpec =
                            tween(
                                AppMotion.ContentMillis
                            )
                    ) togetherWith
                        fadeOut(
                            animationSpec =
                                tween(
                                    AppMotion.ExitMillis
                                )
                        )
                },

                label =
                    "compact-search-status"
            ) { currentStatus ->

                when (currentStatus) {
                    CompactSearchStatus.READY -> {
                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Rounded
                                        .CheckCircle,

                                contentDescription =
                                    null,

                                modifier =
                                    Modifier.size(17.dp),

                                tint =
                                    MaterialTheme
                                        .colorScheme
                                        .primary
                            )

                            Spacer(
                                modifier =
                                    Modifier.size(7.dp)
                            )

                            Text(
                                text =
                                    "Sẵn sàng tìm và tải",

                                style =
                                    MaterialTheme
                                        .typography
                                        .labelMedium,

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onSurfaceVariant
                            )
                        }
                    }

                    CompactSearchStatus.SEARCHING -> {
                        CompactProgressStatus(
                            text =
                                "Đang tìm trên YouTube"
                        )
                    }

                    CompactSearchStatus.SEARCHING_REFERENCE -> {
                        CompactProgressStatus(
                            text =
                                "Đang đối chiếu Library"
                        )
                    }

                    CompactSearchStatus.PREPARING -> {
                        CompactProgressStatus(
                            text =
                                "Đang chuẩn bị file tải"
                        )
                    }

                    CompactSearchStatus.FFMPEG_CHECKING -> {
                        CompactProgressStatus(
                            text =
                                ffmpegMessage
                                    .ifBlank {
                                        "Đang kiểm tra bộ chuyển đổi"
                                    }
                        )
                    }

                    CompactSearchStatus.FFMPEG_FAILED -> {
                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Rounded
                                        .ErrorOutline,

                                contentDescription =
                                    null,

                                modifier =
                                    Modifier.size(18.dp),

                                tint =
                                    MaterialTheme
                                        .colorScheme
                                        .error
                            )

                            Spacer(
                                modifier =
                                    Modifier.size(7.dp)
                            )

                            Text(
                                text =
                                    ffmpegMessage
                                        .ifBlank {
                                            "Bộ chuyển đổi MP3 chưa sẵn sàng"
                                        },

                                modifier =
                                    Modifier.weight(1f),

                                style =
                                    MaterialTheme
                                        .typography
                                        .bodySmall,

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .error
                            )

                            TextButton(
                                onClick =
                                    onRetryFfmpeg
                            ) {
                                Text(
                                    "Thử lại"
                                )
                            }
                        }
                    }
                }
            }

            referenceMessage
                ?.takeIf {
                    it.isNotBlank() &&
                        !isSearchingReference
                }
                ?.let { message ->

                    Text(
                        text =
                            message,

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

@Composable
private fun CompactProgressStatus(
    text: String
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                6.dp
            )
    ) {
        Text(
            text =
                text,

            style =
                MaterialTheme
                    .typography
                    .labelMedium,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        LinearProgressIndicator(
            modifier =
                Modifier.fillMaxWidth()
        )
    }
}