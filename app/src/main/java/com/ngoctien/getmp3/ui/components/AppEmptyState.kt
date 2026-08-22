package com.ngoctien.getmp3.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.ui.design.LocalAppDesign

@Composable
internal fun AppEmptyState(
    icon: ImageVector,
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier
) {

    val design =
        LocalAppDesign.current

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    vertical =
                        design
                            .spacing
                            .extraLarge
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector =
                icon,

            contentDescription =
                null,

            modifier =
                Modifier.size(
                    44.dp
                ),

            tint =
                MaterialTheme
                    .colorScheme
                    .primary
        )

        Spacer(
            Modifier.height(
                design
                    .spacing
                    .medium
            )
        )

        Text(
            text =
                title,

            fontWeight =
                FontWeight.SemiBold
        )

        if (
            !description
                .isNullOrBlank()
        ) {

            Spacer(
                Modifier.height(
                    design
                        .spacing
                        .tiny
                )
            )

            Text(
                text =
                    description,

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
    }
}
