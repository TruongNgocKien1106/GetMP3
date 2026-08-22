package com.ngoctien.getmp3.ui.skin.compact

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ngoctien.getmp3.settings.AppSettings
import com.ngoctien.getmp3.settings.AppThemeMode
import com.ngoctien.getmp3.settings.AppUiStyle
import com.ngoctien.getmp3.ui.AppPageHeader
import com.ngoctien.getmp3.ui.components.AppErrorNotice
import com.ngoctien.getmp3.ui.design.LocalAppDesign
import com.ngoctien.getmp3.viewmodel.CompareIndexUiState
import com.ngoctien.getmp3.viewmodel.DownloadScreenUiState
import com.ngoctien.getmp3.viewmodel.FfmpegReadyState

@Composable
internal fun CompactSettingsScreen(
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

    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
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
                design.spacing.sectionGap
            )
    ) {
        item {
            AppPageHeader(
                title =
                    "Cài đặt",

                subtitle =
                    "Chất lượng • thư mục • giao diện",

                icon =
                    Icons.Rounded.Settings,

                action =
                    null
            )
        }

        item {
            CompactSettingsGeneralCard(
                settings =
                    settings,

                onBitrateChange =
                    onBitrateChange,

                onThemeChange =
                    onThemeChange,

                onUiStyleChange =
                    onUiStyleChange,

                onChooseInboxFolder =
                    onChooseInboxFolder,

                onUseDefaultInboxFolder =
                    onUseDefaultInboxFolder
            )
        }

        item {
            CompactSettingsLibraryCard(
                settings =
                    settings,

                libraryIndexState =
                    libraryIndexState,

                onChooseLibraryFolder =
                    onChooseLibraryFolder,

                onClearLibraryFolder =
                    onClearLibraryFolder,

                onRebuildLibraryIndex =
                    onRebuildLibraryIndex
            )
        }

        item {
            CompactSettingsFiltersCard(
                settings =
                    settings,

                onSaveFilters =
                    onSaveFilters
            )
        }

        item {
            AnimatedVisibility(
                visible =
                    downloadState.ffmpegState ==
                        FfmpegReadyState.FAILED
            ) {
                AppErrorNotice(
                    text =
                        downloadState.ffmpegMessage,

                    actionText =
                        "Thử lại",

                    onAction =
                        onRetryFfmpeg,

                    modifier =
                        Modifier.padding(
                            bottom =
                                design.spacing.small
                        )
                )
            }
        }
    }
}
