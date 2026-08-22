package com.ngoctien.getmp3.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
internal fun AppEmptyPanel(
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
                    10.dp,
                    Alignment.CenterVertically
                )
        ) {
            Icon(
                imageVector =
                    Icons.Rounded
                        .LibraryMusic,

                contentDescription =
                    null,

                modifier =
                    Modifier.size(
                        30.dp
                    ),

                tint =
                    MaterialTheme
                        .colorScheme
                        .primary
            )

            Text(
                text =
                    text,

                textAlign =
                    TextAlign.Center,

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
