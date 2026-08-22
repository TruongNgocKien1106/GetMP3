package com.ngoctien.getmp3.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ngoctien.getmp3.ui.design.LocalAppDesign

@Composable
internal fun AppSettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val design =
        LocalAppDesign.current

    AppCard(
        modifier =
            modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement =
                Arrangement.spacedBy(
                    design
                        .spacing
                        .medium
                )
        ) {
            content()
        }
    }
}
