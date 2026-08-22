package com.ngoctien.getmp3.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.ngoctien.getmp3.settings.AppSettings
import com.ngoctien.getmp3.settings.AppThemeMode
import com.ngoctien.getmp3.settings.AppUiStyle
import com.ngoctien.getmp3.ui.components.AppActionButton
import com.ngoctien.getmp3.ui.components.AppActionVisualState
import com.ngoctien.getmp3.ui.components.AppAnimatedStatusText
import com.ngoctien.getmp3.ui.components.AppErrorNotice
import com.ngoctien.getmp3.ui.components.AppFolderField
import com.ngoctien.getmp3.ui.components.AppSettingsCard
import com.ngoctien.getmp3.ui.design.LocalAppDesign
import com.ngoctien.getmp3.viewmodel.CompareIndexUiState
import com.ngoctien.getmp3.viewmodel.DownloadScreenUiState
import com.ngoctien.getmp3.viewmodel.FfmpegReadyState

@Composable
internal fun BentoSettingsScreen(
    settings: AppSettings,
    libraryIndexState: CompareIndexUiState,
    downloadState: DownloadScreenUiState,
    modifier: Modifier,

    onBitrateChange: (Int) -> Unit,
    onThemeChange: (AppThemeMode) -> Unit,

    onUiStyleChange: (AppUiStyle) -> Unit,
    onChooseInboxFolder: () -> Unit,
    onUseDefaultInboxFolder: () -> Unit,

    onChooseLibraryFolder: () -> Unit,
    onClearLibraryFolder: () -> Unit,
    onRebuildLibraryIndex: () -> Unit,

    onSaveFilters:
        (List<String>, String) -> Unit,

    onRetryFfmpeg: () -> Unit
) {
    val design =
        LocalAppDesign.current

    var filterTermsText by
        remember(
            settings.titleFilterTerms
        ) {
            mutableStateOf(
                settings
                    .titleFilterTerms
                    .joinToString("\n")
            )
        }

    var filterSymbolsText by
        remember(
            settings.titleFilterSymbols
        ) {
            mutableStateOf(
                settings.titleFilterSymbols
            )
        }

    LazyColumn(
        modifier =
            modifier
                .statusBarsPadding(),

        contentPadding =
            PaddingValues(
                start =
                    design
                        .spacing
                        .screenHorizontal,

                top =
                    design
                        .spacing
                        .screenVertical,

                end =
                    design
                        .spacing
                        .screenHorizontal,

                bottom =
                    design
                        .spacing
                        .extraLarge
            ),

        verticalArrangement =
            Arrangement.spacedBy(
                design
                    .spacing
                    .sectionGap
            )
    ) {
        item {
            AppPageHeader(
                title =
                    "Cài đặt",

                subtitle =
                    "Chất lượng • thư mục • giao diện",

                icon =
                    Icons.Rounded.Settings
            )
        }

        item {
            AppSettingsCard {
                AnimatedSelectorField(
                    label =
                        "Chất lượng MP3",

                    value =
                        "${settings.bitrateKbps} kbps",

                    options =
                        AppSettings
                            .SUPPORTED_BITRATES
                            .map {
                                "$it kbps"
                            },

                    onSelected = {
                            selected ->

                        selected
                            .substringBefore(" ")
                            .toIntOrNull()
                            ?.let(
                                onBitrateChange
                            )
                    }
                )

                AppFolderField(
                    label =
                        "Inbox",

                    value =
                        settings
                            .downloadFolderName,

                    onClick =
                        onChooseInboxFolder
                )

                if (
                    settings
                        .usesCustomInboxFolder
                ) {
                    TextButton(
                        onClick =
                            onUseDefaultInboxFolder
                    ) {
                        Text(
                            "Dùng Inbox mặc định"
                        )
                    }
                }

                AnimatedSelectorField(
                    label = "Chế độ màu",

                    value =
                        when (
                            settings.themeMode
                        ) {
                            AppThemeMode.SYSTEM ->
                                "Theo hệ thống"

                            AppThemeMode.LIGHT ->
                                "Sáng"

                            AppThemeMode.DARK ->
                                "Tối"
                        },

                    options =
                        listOf(
                            "Theo hệ thống",
                            "Sáng",
                            "Tối"
                        ),

                    onSelected = {
                            selected ->

                        onThemeChange(
                            when (selected) {
                                "Sáng" ->
                                    AppThemeMode
                                        .LIGHT

                                "Tối" ->
                                    AppThemeMode
                                        .DARK

                                else ->
                                    AppThemeMode
                                        .SYSTEM
                            }
                        )
                    }
                )
            }
        }

        item {
            AppSettingsCard {
                UiStyleSelector(
                    style =
                        settings.uiStyle,

                    onStyleChange =
                        onUiStyleChange
                )
            }
        }
        item {
            AppSettingsCard {
                Text(
                    text =
                        "Library",

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,

                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        "Cài dữ liệu Library một lần rồi đồng bộ lại khi có file mới. Sửa thẻ, Lyrics và Đối chiếu sẽ dùng chung dữ liệu này.",

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                AppFolderField(
                    label =
                        "Library",

                    value =
                        settings
                            .compareFolderName,

                    onClick =
                        onChooseLibraryFolder
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            design
                                .spacing
                                .medium
                        )
                ) {
                    AppActionButton(
                        label =
                            if (
                                libraryIndexState
                                    .hasIndex
                            ) {
                                "Cập nhật dữ liệu"
                            }
                            else {
                                "Cài & đồng bộ"
                            },

                        onClick =
                            onRebuildLibraryIndex,

                        enabled =
                            settings
                                .hasCompareFolder,

                        state =
                            when {
                                libraryIndexState
                                    .isScanning ->
                                    AppActionVisualState
                                        .LOADING

                                libraryIndexState
                                    .errorMessage != null ->
                                    AppActionVisualState
                                        .ERROR

                                else ->
                                    AppActionVisualState
                                        .IDLE
                            },

                        loadingLabel =
                            "Đang cập nhật...",

                        errorLabel =
                            "Thử lại",

                        leadingIcon =
                            Icons.Rounded.Sync,

                        haptic =
                            true,

                        modifier =
                            Modifier.weight(
                                1f
                            )
                    )

                    if (
                        settings
                            .hasCompareFolder
                    ) {
                        TextButton(
                            onClick =
                                onClearLibraryFolder
                        ) {
                            Text(
                                "Bỏ"
                            )
                        }
                    }
                }

                AppAnimatedStatusText(
                    text =
                        libraryIndexState
                            .errorMessage
                            ?: libraryIndexState
                                .message
                            ?: if (
                                libraryIndexState
                                    .hasIndex
                            ) {
                                "${libraryIndexState.totalFiles} bài đã có dữ liệu"
                            }
                            else {
                                "Chưa có dữ liệu Library"
                            },

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    color =
                        if (
                            libraryIndexState
                                .errorMessage != null
                        ) {
                            MaterialTheme
                                .colorScheme
                                .error
                        }
                        else {
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                        }
                )

                if (
                    libraryIndexState
                        .isScanning
                ) {
                    LinearProgressIndicator(
                        progress = {
                            libraryIndexState
                                .progressFraction
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    Text(
                        text =
                            buildString {
                                append(
                                    "${libraryIndexState.processedFiles}/${libraryIndexState.totalFiles}"
                                )

                                append(
                                    " • mới ${libraryIndexState.newFiles}"
                                )

                                append(
                                    " • đổi ${libraryIndexState.changedFiles}"
                                )

                                append(
                                    " • bỏ qua ${libraryIndexState.skippedFiles}"
                                )
                            },

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )

                    libraryIndexState
                        .currentFileName
                        .takeIf {
                            it.isNotBlank()
                        }
                        ?.let {
                                fileName ->

                            Text(
                                text =
                                    fileName,

                                maxLines =
                                    1,

                                overflow =
                                    TextOverflow
                                        .Ellipsis,

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
                else if (
                    libraryIndexState
                        .hasIndex
                ) {
                    Text(
                        text =
                            "${libraryIndexState.totalFiles} bài • " +
                                "${libraryIndexState.artistCount} Artist • " +
                                "${libraryIndexState.albumCount} Album • " +
                                "${libraryIndexState.coverFiles} cover",

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        color =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }
            }
        }

        item {
            AppSettingsCard {
                Text(
                    text =
                        "Format nhanh Title",

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,

                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        "Mỗi dòng hoặc dấu phẩy là một cụm từ cần xóa.",

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                StableOutlinedTextField(
                    value =
                        filterTermsText,

                    onValueChange = {
                        filterTermsText =
                            it
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    label = {
                        Text(
                            "Cụm từ cần loại bỏ"
                        )
                    },

                    minLines =
                        3,

                    maxLines =
                        6,

                    placeholder = {
                        Text(
                            "Official Video\nLyrics\nVietsub"
                        )
                    }
                )

                StableOutlinedTextField(
                    value =
                        filterSymbolsText,

                    onValueChange = {
                        filterSymbolsText =
                            it
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    label = {
                        Text(
                            "Dấu cần loại bỏ"
                        )
                    },

                    singleLine =
                        true,

                    placeholder = {
                        Text(
                            "| • ~ _"
                        )
                    }
                )

                AppActionButton(
                    label =
                        "Lưu bộ lọc",

                    onClick = {
                        val terms =
                            filterTermsText
                                .split(
                                    '\n',
                                    ',',
                                    ';'
                                )
                                .map {
                                    it.trim()
                                }
                                .filter {
                                    it.isNotBlank()
                                }

                        onSaveFilters(
                            terms,
                            filterSymbolsText
                        )
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    leadingIcon =
                        Icons.Rounded.Save,

                    haptic =
                        true
                )
            }
        }

        if (
            downloadState
                .ffmpegState ==
            FfmpegReadyState.FAILED
        ) {
            item {
                AppErrorNotice(
                    text =
                        downloadState
                            .ffmpegMessage,

                    actionText =
                        "Thử lại",

                    onAction =
                        onRetryFfmpeg
                )
            }
        }
    }
}
