package com.ngoctien.getmp3.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun AppLoadingPanel(
    text: String,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier =
            modifier.fillMaxWidth()
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = 138.dp
                    ),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.spacedBy(
                    12.dp,
                    Alignment.CenterVertically
                )
        ) {
            CircularProgressIndicator(
                modifier =
                    Modifier.size(
                        30.dp
                    ),

                strokeWidth =
                    3.dp
            )

            Text(
                text =
                    text,

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}
