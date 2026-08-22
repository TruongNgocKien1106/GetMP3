package com.ngoctien.getmp3.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.ui.AppMotion

@Composable
internal fun AppDestructiveButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    loadingLabel: String = "Đang xử lý...",
    leadingIcon: ImageVector? = null,
    haptic: Boolean = true
) {
    val hapticFeedback =
        LocalHapticFeedback.current

    Button(
        onClick = {
            if (haptic) {
                hapticFeedback
                    .performHapticFeedback(
                        HapticFeedbackType.LongPress
                    )
            }

            onClick()
        },

        modifier =
            modifier,

        enabled =
            enabled &&
                !loading,

        colors =
            ButtonDefaults.buttonColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .errorContainer,

                contentColor =
                    MaterialTheme
                        .colorScheme
                        .onErrorContainer,

                disabledContainerColor =
                    MaterialTheme
                        .colorScheme
                        .errorContainer
                        .copy(
                            alpha = 0.38f
                        ),

                disabledContentColor =
                    MaterialTheme
                        .colorScheme
                        .onSurface
                        .copy(
                            alpha = 0.38f
                        )
            )
    ) {
        AnimatedContent(
            targetState =
                loading,

            transitionSpec = {
                (
                    fadeIn(
                        AppMotion
                            .feedbackSpring
                    ) +
                        scaleIn(
                            animationSpec =
                                AppMotion
                                    .feedbackSpring,

                            initialScale =
                                0.88f
                        )
                )
                    .togetherWith(
                        fadeOut(
                            AppMotion
                                .feedbackSpring
                        ) +
                            scaleOut(
                                animationSpec =
                                    AppMotion
                                        .feedbackSpring,

                                targetScale =
                                    0.90f
                            )
                    )
            },

            label =
                "destructive-button-state"
        ) { isLoading ->

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(
                                18.dp
                            ),

                        strokeWidth =
                            2.dp,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onErrorContainer
                    )
                }
                else {
                    leadingIcon
                        ?.let { icon ->

                            Icon(
                                imageVector =
                                    icon,

                                contentDescription =
                                    null,

                                modifier =
                                    Modifier.size(
                                        19.dp
                                    )
                            )
                        }
                }

                if (
                    isLoading ||
                    leadingIcon != null
                ) {
                    Spacer(
                        modifier =
                            Modifier.width(
                                6.dp
                            )
                    )
                }

                Text(
                    if (isLoading) {
                        loadingLabel
                    }
                    else {
                        label
                    }
                )
            }
        }
    }
}
