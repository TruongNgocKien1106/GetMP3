package com.ngoctien.getmp3.ui.skin.compact

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.settings.AppSettings
import com.ngoctien.getmp3.ui.components.AppActionButton
import com.ngoctien.getmp3.ui.components.AppActionVisualState
import com.ngoctien.getmp3.ui.components.AppErrorNotice
import com.ngoctien.getmp3.ui.components.AppFolderField
import com.ngoctien.getmp3.ui.components.AppSettingsCard
import com.ngoctien.getmp3.ui.design.LocalAppDesign
import com.ngoctien.getmp3.viewmodel.CompareIndexUiState

@Composable
internal fun CompactSettingsLibraryCard(
    settings: AppSettings,
    libraryIndexState: CompareIndexUiState,
    onChooseLibraryFolder: () -> Unit,
    onClearLibraryFolder: () -> Unit,
    onRebuildLibraryIndex: () -> Unit
) {
    val design =
        LocalAppDesign.current

    val hasLibrary =
        !settings
            .compareTreeUri
            .isNullOrBlank()

    AppSettingsCard {
        Column(
            verticalArrangement =
                Arrangement.spacedBy(
                    design.spacing.small
                )
        ) {
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
                    "Nguồn đối chiếu Artist, Album, lyrics và file trùng",

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            AppFolderField(
                label =
                    "Thư mục Library",

                value =
                    settings.compareFolderName,

                onClick =
                    onChooseLibraryFolder,

                modifier =
                    Modifier.fillMaxWidth()
            )

            if (
                hasLibrary &&
                libraryIndexState.totalFiles > 0
            ) {
                Text(
                    text =
                        buildString {
                            append(
                                libraryIndexState.totalFiles
                            )

                            append(
                                " bài"
                            )

                            append(
                                " • "
                            )

                            append(
                                libraryIndexState.artistCount
                            )

                            append(
                                " Artist"
                            )

                            append(
                                " • "
                            )

                            append(
                                libraryIndexState.albumCount
                            )

                            append(
                                " Album"
                            )
                        },

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    fontWeight =
                        FontWeight.Medium
                )
            }

            AnimatedVisibility(
                visible =
                    libraryIndexState.isScanning
            ) {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(
                            6.dp
                        )
                ) {
                    LinearProgressIndicator(
                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    Text(
                        text =
                            libraryIndexState
                                .currentFileName
                                .takeIf {
                                    it.isNotBlank()
                                }
                                ?.let {
                                    "Đang quét: $it"
                                }
                                ?: "Đang xây dựng Media Index...",

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

                    if (
                        libraryIndexState.totalFiles >
                        0
                    ) {
                        Text(
                            text =
                                "${libraryIndexState.processedFiles}/${libraryIndexState.totalFiles}",

                            style =
                                MaterialTheme
                                    .typography
                                    .labelSmall
                        )
                    }
                }
            }

            libraryIndexState
                .message
                ?.takeIf {
                    it.isNotBlank() &&
                        !libraryIndexState.isScanning
                }
                ?.let {
                    message ->

                    Text(
                        text =
                            message,

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

            libraryIndexState
                .errorMessage
                ?.takeIf(
                    String::isNotBlank
                )
                ?.let {
                    message ->

                    AppErrorNotice(
                        text =
                            message,

                        actionText =
                            if (hasLibrary) {
                                "Quét lại"
                            }
                            else {
                                null
                            },

                        onAction =
                            if (hasLibrary) {
                                onRebuildLibraryIndex
                            }
                            else {
                                null
                            }
                    )
                }

            AppActionButton(
                label =
                    if (
                        libraryIndexState.totalFiles >
                        0
                    ) {
                        "Quét lại Library"
                    }
                    else {
                        "Tạo Media Index"
                    },

                loadingLabel =
                    "Đang quét Library...",

                onClick =
                    onRebuildLibraryIndex,

                enabled =
                    hasLibrary,

                state =
                    if (
                        libraryIndexState.isScanning
                    ) {
                        AppActionVisualState.LOADING
                    }
                    else {
                        AppActionVisualState.IDLE
                    },

                leadingIcon =
                    Icons.Rounded.Refresh,

                haptic =
                    true,

                modifier =
                    Modifier.fillMaxWidth()
            )

            if (hasLibrary) {
                TextButton(
                    onClick =
                        onClearLibraryFolder,

                    enabled =
                        !libraryIndexState
                            .isScanning,

                    modifier =
                        Modifier.padding(
                            top = 2.dp
                        )
                ) {
                    Text(
                        "Bỏ chọn Library"
                    )
                }
            }
        }
    }
}
