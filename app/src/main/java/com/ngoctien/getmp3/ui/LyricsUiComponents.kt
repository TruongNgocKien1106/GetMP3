package com.ngoctien.getmp3.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.ui.design.LocalAppDesign

@Composable
internal fun LyricsPill(
    text: String,
    emphasized: Boolean = false,
    modifier: Modifier = Modifier
) {
    val design =
        LocalAppDesign.current

    Surface(
        modifier =
            modifier,

        shape =
            CircleShape,

        color =
            if (emphasized) {
                MaterialTheme
                    .colorScheme
                    .primaryContainer
            }
            else {
                MaterialTheme
                    .colorScheme
                    .surfaceVariant
            },

        contentColor =
            if (emphasized) {
                MaterialTheme
                    .colorScheme
                    .onPrimaryContainer
            }
            else {
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
            }
    ) {
        Text(
            text =
                text,

            modifier =
                Modifier.padding(
                    horizontal =
                        design.spacing.small,

                    vertical =
                        design.spacing.tiny
                ),

            style =
                MaterialTheme
                    .typography
                    .labelSmall,

            fontWeight =
                if (emphasized) {
                    FontWeight.SemiBold
                }
                else {
                    FontWeight.Normal
                }
        )
    }
}

@Composable
internal fun LyricsSectionHeader(
    title: String,
    subtitle: String? = null,
    badge: String? = null,
    modifier: Modifier = Modifier
) {
    val design =
        LocalAppDesign.current

    Row(
        modifier =
            modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically,

        horizontalArrangement =
            Arrangement.spacedBy(
                design.spacing.small
            )
    ) {
        Column(
            modifier =
                Modifier.weight(
                    1f
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    design.spacing.tiny
                )
        ) {
            Text(
                text =
                    title,

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.Bold
            )

            subtitle
                ?.takeIf(
                    String::isNotBlank
                )
                ?.let { value ->

                    Text(
                        text =
                            value,

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

        badge
            ?.takeIf(
                String::isNotBlank
            )
            ?.let { value ->

                LyricsPill(
                    text =
                        value,

                    emphasized =
                        true
                )
            }
    }
}

@Composable
internal fun LyricsInfoCard(
    icon: ImageVector,
    title: String,
    message: String,
    emphasized: Boolean = false,
    modifier: Modifier = Modifier
) {
    val design =
        LocalAppDesign.current

    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .animateContentSize(),

        shape =
            RoundedCornerShape(
                design.shapes.cardCorner
            ),

        color =
            if (emphasized) {
                MaterialTheme
                    .colorScheme
                    .tertiaryContainer
            }
            else {
                MaterialTheme
                    .colorScheme
                    .surfaceVariant
                    .copy(
                        alpha =
                            0.55f
                    )
            },

        contentColor =
            if (emphasized) {
                MaterialTheme
                    .colorScheme
                    .onTertiaryContainer
            }
            else {
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
            }
    ) {
        Row(
            modifier =
                Modifier.padding(
                    design.spacing.medium
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Surface(
                modifier =
                    Modifier.size(
                        38.dp
                    ),

                shape =
                    RoundedCornerShape(
                        design.shapes.controlCorner
                    ),

                color =
                    MaterialTheme
                        .colorScheme
                        .primaryContainer,

                contentColor =
                    MaterialTheme
                        .colorScheme
                        .onPrimaryContainer
            ) {
                Icon(
                    imageVector =
                        icon,

                    contentDescription =
                        null,

                    modifier =
                        Modifier.padding(
                            design.spacing.small
                        )
                )
            }

            Spacer(
                modifier =
                    Modifier.width(
                        design.spacing.medium
                    )
            )

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        design.spacing.tiny
                    )
            ) {
                Text(
                    text =
                        title,

                    fontWeight =
                        FontWeight.SemiBold
                )

                Text(
                    text =
                        message,

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall
                )
            }
        }
    }
}
