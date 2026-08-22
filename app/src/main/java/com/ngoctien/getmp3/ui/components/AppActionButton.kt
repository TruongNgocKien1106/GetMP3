package com.ngoctien.getmp3.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.ui.AppMotion
import com.ngoctien.getmp3.ui.design.LocalAppDesign

internal enum class AppActionVisualState {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR
}

@Composable
internal fun AppActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    state: AppActionVisualState =
        AppActionVisualState.IDLE,
    loadingLabel: String = "Đang xử lý...",
    successLabel: String = "Hoàn tất",
    errorLabel: String = "Thử lại",
    leadingIcon: ImageVector? = null,
    haptic: Boolean = false
) {
    val design =
        LocalAppDesign.current

    val hapticFeedback =
        LocalHapticFeedback.current

    val working =
        state ==
            AppActionVisualState.LOADING

    val pulseTransition =
        rememberInfiniteTransition(
            label =
                "app-action-pulse"
        )

    val pulse by
        pulseTransition.animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec =
                infiniteRepeatable(
                    animation =
                        keyframes {
                            durationMillis =
                                AppMotion
                                    .ActionPulseMillis

                            0f at 0
                            0f at 4_150
                            1f at 4_350
                            0f at 4_600
                            0f at AppMotion.ActionPulseMillis
                        }
                ),
            label =
                "app-action-pulse-value"
        )

    val attentionAmount =
        if (
            enabled &&
            !working &&
            state ==
            AppActionVisualState.IDLE
        ) {
            pulse
        }
        else {
            0f
        }

    Button(
        onClick = {

            if (haptic) {
                hapticFeedback
                    .performHapticFeedback(
                        HapticFeedbackType
                            .LongPress
                    )
            }

            onClick()
        },

        enabled =
            enabled &&
                !working,

        modifier =
            modifier
                .graphicsLayer {
                    val scale =
                        1f +
                            (
                                0.014f *
                                    attentionAmount
                                )

                    scaleX =
                        scale

                    scaleY =
                        scale

                    alpha =
                        1f -
                            (
                                0.055f *
                                    attentionAmount
                                )
                },

        shape =
            RoundedCornerShape(
                design
                    .shapes
                    .controlCorner
            )
    ) {
        AnimatedContent(
            targetState =
                state,

            transitionSpec = {
                (
                    fadeIn(
                        tween(
                            AppMotion
                                .ContentMillis
                        )
                    ) +
                        scaleIn(
                            initialScale =
                                0.90f,

                            animationSpec =
                                AppMotion
                                    .feedbackSpring
                        )
                )
                    .togetherWith(
                        fadeOut(
                            tween(
                                AppMotion
                                    .QuickMillis
                            )
                        ) +
                            scaleOut(
                                targetScale =
                                    0.92f,

                                animationSpec =
                                    tween(
                                        AppMotion
                                            .QuickMillis
                                    )
                            )
                    )
            },

            label =
                "app-action-button-state"
        ) { visualState ->

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                when (visualState) {

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
                                    .onPrimary
                        )
                    }

                    AppActionVisualState.SUCCESS -> {
                        Icon(
                            imageVector =
                                Icons.Rounded.Check,

                            contentDescription =
                                null,

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
                                null,

                            modifier =
                                Modifier.size(
                                    20.dp
                                )
                        )
                    }

                    AppActionVisualState.IDLE -> {
                        leadingIcon
                            ?.let { icon ->

                                Icon(
                                    imageVector =
                                        icon,

                                    contentDescription =
                                        null,

                                    modifier =
                                        Modifier.size(
                                            20.dp
                                        )
                                )
                            }
                    }
                }

                if (
                    visualState !=
                        AppActionVisualState.IDLE ||
                    leadingIcon != null
                ) {
                    Spacer(
                        Modifier.width(
                            7.dp
                        )
                    )
                }

                Text(
                    text =
                        when (visualState) {
                            AppActionVisualState.IDLE ->
                                label

                            AppActionVisualState.LOADING ->
                                loadingLabel

                            AppActionVisualState.SUCCESS ->
                                successLabel

                            AppActionVisualState.ERROR ->
                                errorLabel
                        }
                )
            }
        }
    }
}
