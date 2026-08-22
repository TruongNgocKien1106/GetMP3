package com.ngoctien.getmp3.ui.skin.compact

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.ui.AppDestination
import com.ngoctien.getmp3.ui.DockDestinations
import com.ngoctien.getmp3.ui.design.LocalAppDesign

@Composable
internal fun CompactBottomDock(
    selected: AppDestination,
    onSelected:
        (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val design =
        LocalAppDesign.current

    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal =
                        design
                            .spacing
                            .screenHorizontal,

                    vertical =
                        design
                            .spacing
                            .small
                ),

        shape =
            RoundedCornerShape(
                design.shapes.dockCorner
            ),

        color =
            MaterialTheme
                .colorScheme
                .surfaceContainerHigh,

        tonalElevation =
            3.dp,

        shadowElevation =
            8.dp,

        border =
            BorderStroke(
                width =
                    1.dp,

                color =
                    MaterialTheme
                        .colorScheme
                        .outlineVariant
                        .copy(
                            alpha =
                                0.55f
                        )
            )
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal =
                            design.spacing.small,

                        vertical =
                            design.spacing.tiny
                    ),

            horizontalArrangement =
                Arrangement.SpaceEvenly,

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            DockDestinations
                .forEach { destination ->

                    CompactNavigationItem(
                        destination =
                            destination,

                        selected =
                            destination ==
                                selected,

                        onClick = {
                            onSelected(
                                destination
                            )
                        }
                    )
                }
        }
    }
}
