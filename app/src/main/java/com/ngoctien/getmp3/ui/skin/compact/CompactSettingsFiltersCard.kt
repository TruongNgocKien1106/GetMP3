package com.ngoctien.getmp3.ui.skin.compact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.ngoctien.getmp3.settings.AppSettings
import com.ngoctien.getmp3.ui.components.AppActionButton
import com.ngoctien.getmp3.ui.components.AppSettingsCard
import com.ngoctien.getmp3.ui.design.LocalAppDesign

@Composable
internal fun CompactSettingsFiltersCard(
    settings: AppSettings,
    onSaveFilters:
        (List<String>, String) -> Unit
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

    val savedTermsText =
        settings
            .titleFilterTerms
            .joinToString("\n")

    val hasChanges =
        filterTermsText !=
            savedTermsText ||
            filterSymbolsText !=
            settings.titleFilterSymbols

    AppSettingsCard {
        Column(
            verticalArrangement =
                Arrangement.spacedBy(
                    design.spacing.small
                )
        ) {
            Text(
                text =
                    "Format nhanh",

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    "Các từ và ký hiệu cần bỏ khỏi Title khi chuẩn hóa metadata",

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            OutlinedTextField(
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
                        "Từ cần loại bỏ"
                    )
                },

                supportingText = {
                    Text(
                        "Mỗi dòng một từ hoặc cụm từ"
                    )
                },

                minLines =
                    3,

                maxLines =
                    7
            )

            OutlinedTextField(
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
                        "Ký hiệu cần loại bỏ"
                    )
                },

                singleLine =
                    true
            )

            AppActionButton(
                label =
                    if (hasChanges) {
                        "Lưu bộ lọc"
                    }
                    else {
                        "Bộ lọc đã lưu"
                    },

                onClick = {
                    val terms =
                        filterTermsText
                            .lineSequence()
                            .map {
                                it.trim()
                            }
                            .filter {
                                it.isNotBlank()
                            }
                            .distinct()
                            .toList()

                    onSaveFilters(
                        terms,
                        filterSymbolsText
                    )
                },

                enabled =
                    hasChanges,

                leadingIcon =
                    Icons.Rounded.Save,

                haptic =
                    true,

                modifier =
                    Modifier.fillMaxWidth()
            )
        }
    }
}
