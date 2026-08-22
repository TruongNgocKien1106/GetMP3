package com.ngoctien.getmp3.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ngoctien.getmp3.ui.components.AppActionVisualState
import com.ngoctien.getmp3.ui.components.AppCard
import com.ngoctien.getmp3.ui.components.AppErrorNotice
import com.ngoctien.getmp3.ui.components.AppIconActionButton
import com.ngoctien.getmp3.ui.design.LocalAppDesign
import com.ngoctien.getmp3.viewmodel.LyricsUiState
import kotlinx.coroutines.delay

@Composable
internal fun LyricsReaderScreen(
    state: LyricsUiState,
    onBack: () -> Unit,
    onIncreaseFont: () -> Unit,
    onDecreaseFont: () -> Unit,
    onToggleAutoScroll: () -> Unit,
    onScrollSpeedChange: (Int) -> Unit,
    onEditLyrics: () -> Unit,
    onWriteLyrics: () -> Unit
) {
    val result =
        state.selectedResult
            ?: return

    val design =
        LocalAppDesign.current

    val scrollState =
        rememberScrollState()

    val view =
        LocalView.current

    val clipboard =
        LocalClipboardManager.current

    val selectedTarget =
        state.selectedSong
            ?.takeIf {
                it.uri.isNotBlank()
            }

    var showReaderMenu by
        remember {
            mutableStateOf(false)
        }

    var showSourceInfo by
        remember {
            mutableStateOf(false)
        }

    DisposableEffect(view) {
        val previous =
            view.keepScreenOn

        view.keepScreenOn =
            true

        onDispose {
            view.keepScreenOn =
                previous
        }
    }

    LaunchedEffect(
        state.autoScrollEnabled,
        state.autoScrollSpeed,
        result.id
    ) {
        if (!state.autoScrollEnabled) {
            return@LaunchedEffect
        }

        val pixelsPerTick =
            autoScrollPixelsPerTick(
                state.autoScrollSpeed
            )

        while (true) {
            val next =
                (
                    scrollState.value +
                        pixelsPerTick
                ).coerceAtMost(
                    scrollState.maxValue
                )

            if (
                next <=
                scrollState.value
            ) {
                break
            }

            scrollState.scrollTo(
                next
            )

            delay(
                24L
            )
        }
    }

    if (showSourceInfo) {
        LyricsSourceInfoDialog(
            result =
                result,

            onDismiss = {
                showSourceInfo =
                    false
            }
        )
    }

    val saveState =
        when {
            state.isSaving ->
                AppActionVisualState.LOADING

            state.recentlySavedTargetUri !=
                null ->
                AppActionVisualState.SUCCESS

            else ->
                AppActionVisualState.IDLE
        }

    Column(
        modifier =
            Modifier.fillMaxSize(),

        verticalArrangement =
            Arrangement.spacedBy(
                design.spacing.small
            )
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
            AppIconActionButton(
                icon =
                    Icons.Rounded.ArrowBack,

                contentDescription =
                    "Quay lại",

                onClick =
                    onBack,

                haptic =
                    false
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {
                Text(
                    text =
                        result.trackName,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis,

                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        lyricsReaderSubtitle(
                            result
                        ),

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

            AppIconActionButton(
                icon =
                    Icons.Rounded.Save,

                contentDescription =
                    "Ghi lời vào file MP3",

                onClick =
                    onWriteLyrics,

                enabled =
                    !state
                        .writeTargetPicker
                        .isLoading,

                state =
                    saveState,

                haptic =
                    true
            )

            Box {
                AppIconActionButton(
                    icon =
                        Icons.Rounded.MoreVert,

                    contentDescription =
                        "Tùy chọn lyrics",

                    onClick = {
                        showReaderMenu =
                            true
                    },

                    haptic =
                        false
                )

                DropdownMenu(
                    expanded =
                        showReaderMenu,

                    onDismissRequest = {
                        showReaderMenu =
                            false
                    }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Sửa lyrics"
                            )
                        },

                        leadingIcon = {
                            Icon(
                                imageVector =
                                    Icons.Rounded.Edit,

                                contentDescription =
                                    null
                            )
                        },

                        enabled =
                            selectedTarget !=
                                null,

                        onClick = {
                            showReaderMenu =
                                false

                            onEditLyrics()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                "Thông tin nguồn"
                            )
                        },

                        leadingIcon = {
                            Icon(
                                imageVector =
                                    Icons.Rounded.Info,

                                contentDescription =
                                    null
                            )
                        },

                        onClick = {
                            showReaderMenu =
                                false

                            showSourceInfo =
                                true
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                "Sao chép lyrics"
                            )
                        },

                        leadingIcon = {
                            Icon(
                                imageVector =
                                    Icons.Rounded
                                        .ContentCopy,

                                contentDescription =
                                    null
                            )
                        },

                        onClick = {
                            showReaderMenu =
                                false

                            clipboard.setText(
                                AnnotatedString(
                                    result
                                        .readableLyrics
                                )
                            )
                        }
                    )
                }
            }
        }

        state.errorMessage
            ?.takeIf(
                String::isNotBlank
            )
            ?.let { message ->

                AppErrorNotice(
                    text =
                        message
                )
            }

        AppCard(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),

            contentPadding =
                PaddingValues(
                    0.dp
                )
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(
                            scrollState
                        )
                        .padding(
                            start =
                                design.spacing.large,

                            top =
                                design.spacing.large,

                            end =
                                design.spacing.large,

                            bottom =
                                design.spacing.extraLarge
                        )
            ) {
                Text(
                    text =
                        result.readableLyrics,

                    fontSize =
                        state.fontSizeSp.sp,

                    lineHeight =
                        (
                            state.fontSizeSp *
                                1.52f
                        ).sp,

                    textAlign =
                        TextAlign.Start,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurface
                )
            }
        }

        ReaderControlBar(
            state =
                state,

            onIncreaseFont =
                onIncreaseFont,

            onDecreaseFont =
                onDecreaseFont,

            onToggleAutoScroll =
                onToggleAutoScroll,

            onScrollSpeedChange =
                onScrollSpeedChange
        )
    }
}

private fun autoScrollPixelsPerTick(
    speed: Int
): Int {
    return when (speed) {
        1 -> 1
        2 -> 2
        3 -> 3
        4 -> 5
        5 -> 8
        6 -> 12
        7 -> 17
        8 -> 23
        9 -> 30
        10 -> 40
        else -> 3
    }
}
