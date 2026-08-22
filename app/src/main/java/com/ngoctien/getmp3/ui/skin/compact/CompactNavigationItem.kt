package com.ngoctien.getmp3.ui.skin.compact

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.ui.AppDestination
import com.ngoctien.getmp3.ui.AppMotion
import com.ngoctien.getmp3.ui.design.LocalAppDesign
import com.ngoctien.getmp3.ui.design.appPressable

@Composable
internal fun CompactNavigationItem(
    destination: AppDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val design =
        LocalAppDesign.current

    val containerColor by
        animateColorAsState(
            targetValue =
                if (selected) {
                    MaterialTheme
                        .colorScheme
                        .primaryContainer
                }
                else {
                    MaterialTheme
                        .colorScheme
                        .surfaceContainer
                        .copy(
                            alpha =
                                0f
                        )
                },

            animationSpec =
                tween(
                    durationMillis =
                        AppMotion.ContentMillis
                ),

            label =
                "compact-nav-container"
        )

    val contentColor by
        animateColorAsState(
            targetValue =
                if (selected) {
                    MaterialTheme
                        .colorScheme
                        .onPrimaryContainer
                }
                else {
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
                },

            animationSpec =
                tween(
                    durationMillis =
                        AppMotion.ContentMillis
                ),

            label =
                "compact-nav-content"
        )

    Surface(
        modifier =
            modifier
                .appPressable(
                    haptic =
                        true,

                    onClick =
                        onClick
                ),

        color =
            containerColor,

        contentColor =
            contentColor,

        shape =
            RoundedCornerShape(
                design.shapes.controlCorner
            )
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal =
                        if (selected) {
                            12.dp
                        }
                        else {
                            10.dp
                        },

                    vertical =
                        9.dp
                ),

            horizontalArrangement =
                Arrangement.spacedBy(
                    design.spacing.small
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Icon(
                imageVector =
                    destination.icon,

                contentDescription =
                    destination.title,

                modifier =
                    Modifier.size(
                        21.dp
                    )
            )

            AnimatedVisibility(
                visible =
                    selected,

                enter =
                    fadeIn(
                        tween(
                            AppMotion.ContentMillis
                        )
                    ) +
                        expandHorizontally(
                            tween(
                                AppMotion.ContentMillis
                            )
                        ),

                exit =
                    fadeOut(
                        tween(
                            AppMotion.ContentMillis
                        )
                    ) +
                        shrinkHorizontally(
                            tween(
                                AppMotion.ContentMillis
                            )
                        )
            ) {
                Text(
                    text =
                        destination.title,

                    style =
                        MaterialTheme
                            .typography
                            .labelMedium,

                    fontWeight =
                        FontWeight.SemiBold,

                    maxLines =
                        1
                )
            }
        }
    }
}
