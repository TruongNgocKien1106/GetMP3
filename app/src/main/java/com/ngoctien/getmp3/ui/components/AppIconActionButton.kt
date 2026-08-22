package com.ngoctien.getmp3.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.ui.AppMotion
import com.ngoctien.getmp3.ui.design.appPressable

@Composable
internal fun AppIconActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    haptic: Boolean = true,
    state: AppActionVisualState =
        AppActionVisualState.IDLE
) {
    val working =
        state ==
            AppActionVisualState.LOADING

    Surface(
        modifier =
            modifier
                .size(40.dp)
                .appPressable(
                    enabled =
                        enabled &&
                            !working,

                    haptic =
                        haptic,

                    onClick =
                        onClick
                ),

        shape =
            CircleShape,

        color =
            MaterialTheme
                .colorScheme
                .secondaryContainer,

        contentColor =
            MaterialTheme
                .colorScheme
                .onSecondaryContainer,

        tonalElevation =
            1.dp
    ) {
        Box(
            contentAlignment =
                Alignment.Center
        ) {
            AnimatedContent(
                targetState =
                    state,

                transitionSpec = {
                    fadeIn(
                        tween(
                            AppMotion.ContentMillis
                        )
                    )
                        .togetherWith(
                            fadeOut(
                                tween(
                                    AppMotion.QuickMillis
                                )
                            )
                        )
                },

                label =
                    "app-icon-action-state"
            ) { visualState ->

                when (visualState) {
                    AppActionVisualState.IDLE -> {
                        Icon(
                            imageVector =
                                icon,

                            contentDescription =
                                contentDescription,

                            modifier =
                                Modifier.size(
                                    20.dp
                                )
                        )
                    }

                    AppActionVisualState.LOADING -> {
                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(
                                    19.dp
                                ),

                            strokeWidth =
                                2.dp,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSecondaryContainer
                        )
                    }

                    AppActionVisualState.SUCCESS -> {
                        Icon(
                            imageVector =
                                Icons.Rounded.Check,

                            contentDescription =
                                contentDescription,

                            modifier =
                                Modifier.size(
                                    20.dp
                                )
                        )
                    }

                    AppActionVisualState.ERROR -> {
                        Icon(
                            imageVector =
                                Icons.Rounded
                                    .ErrorOutline,

                            contentDescription =
                                contentDescription,

                            modifier =
                                Modifier.size(
                                    20.dp
                                )
                        )
                    }
                }
            }
        }
    }
}
