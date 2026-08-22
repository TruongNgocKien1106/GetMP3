package com.ngoctien.getmp3.ui.skin.compact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.settings.AppSettings
import com.ngoctien.getmp3.settings.AppThemeMode
import com.ngoctien.getmp3.settings.AppUiStyle
import com.ngoctien.getmp3.ui.AnimatedSelectorField
import com.ngoctien.getmp3.ui.components.AppFolderField
import com.ngoctien.getmp3.ui.components.AppSettingsCard
import com.ngoctien.getmp3.ui.design.LocalAppDesign

@Composable
internal fun CompactSettingsGeneralCard(
    settings: AppSettings,
    onBitrateChange: (Int) -> Unit,
    onThemeChange: (AppThemeMode) -> Unit,
    onUiStyleChange: (AppUiStyle) -> Unit,
    onChooseInboxFolder: () -> Unit,
    onUseDefaultInboxFolder: () -> Unit
) {
    val design =
        LocalAppDesign.current

    AppSettingsCard {
        Column(
            verticalArrangement =
                Arrangement.spacedBy(
                    design.spacing.small
                )
        ) {
            Text(
                text =
                    "Ứng dụng",

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    "Âm thanh, giao diện và thư mục Inbox",

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                modifier =
                    Modifier.height(
                        2.dp
                    )
            )

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

            AnimatedSelectorField(
                label =
                    "Theme",

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

                    val mode =
                        when (selected) {
                            "Sáng" ->
                                AppThemeMode.LIGHT

                            "Tối" ->
                                AppThemeMode.DARK

                            else ->
                                AppThemeMode.SYSTEM
                        }

                    onThemeChange(
                        mode
                    )
                }
            )

            Text(
                text =
                    "Kiểu giao diện",

                style =
                    MaterialTheme
                        .typography
                        .labelLarge,

                fontWeight =
                    FontWeight.SemiBold
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {
                FilterChip(
                    selected =
                        settings.uiStyle ==
                            AppUiStyle.BENTO,

                    onClick = {
                        onUiStyleChange(
                            AppUiStyle.BENTO
                        )
                    },

                    label = {
                        Text(
                            "Bento"
                        )
                    }
                )

                FilterChip(
                    selected =
                        settings.uiStyle ==
                            AppUiStyle.COMPACT,

                    onClick = {
                        onUiStyleChange(
                            AppUiStyle.COMPACT
                        )
                    },

                    label = {
                        Text(
                            "Compact"
                        )
                    }
                )
            }

            AppFolderField(
                label =
                    "Inbox",

                value =
                    settings.downloadFolderName,

                onClick =
                    onChooseInboxFolder,

                modifier =
                    Modifier.fillMaxWidth()
            )

            if (
                !settings
                    .downloadTreeUri
                    .isNullOrBlank()
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
        }
    }
}
