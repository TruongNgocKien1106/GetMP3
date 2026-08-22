package com.ngoctien.getmp3.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.ui.AppMotion
import com.ngoctien.getmp3.ui.design.LocalAppDesign

internal enum class AppFeedbackKind {
    INFO,
    LOADING,
    SUCCESS,
    ERROR
}

@Composable
internal fun AppFeedbackBanner(
    message: String,
    kind: AppFeedbackKind,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val design =
        LocalAppDesign.current

    val colors =
        MaterialTheme.colorScheme

    val containerColor =
        when (kind) {
            AppFeedbackKind.INFO ->
                colors.surfaceVariant

            AppFeedbackKind.LOADING ->
                colors.primaryContainer

            AppFeedbackKind.SUCCESS ->
                colors.tertiaryContainer

            AppFeedbackKind.ERROR ->
                colors.errorContainer
        }

    Surface(
        modifier =
            modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                design
                    .shapes
                    .controlCorner
            ),

        color =
            containerColor
    ) {
        Row(
            modifier =
                Modifier.padding(
                    design
                        .spacing
                        .medium
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            AnimatedContent(
                targetState =
                    kind,

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
                                    0.82f,

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
                                        0.86f
                                )
                        )
                },

                label =
                    "feedback-kind"
            ) { currentKind ->

                when (currentKind) {
                    AppFeedbackKind.LOADING ->
                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(
                                    20.dp
                                ),

                            strokeWidth =
                                2.dp
                        )

                    AppFeedbackKind.SUCCESS ->
                        Icon(
                            imageVector =
                                Icons.Rounded.Check,

                            contentDescription =
                                null
                        )

                    AppFeedbackKind.ERROR ->
                        Icon(
                            imageVector =
                                Icons.Rounded
                                    .ErrorOutline,

                            contentDescription =
                                null
                        )

                    AppFeedbackKind.INFO ->
                        Icon(
                            imageVector =
                                Icons.Rounded.Info,

                            contentDescription =
                                null
                        )
                }
            }

            Spacer(
                Modifier.width(
                    10.dp
                )
            )

            Text(
                text =
                    message,

                modifier =
                    Modifier.weight(
                        1f
                    ),

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium
            )

            if (
                actionLabel != null &&
                onAction != null
            ) {
                TextButton(
                    onClick =
                        onAction
                ) {
                    Text(
                        actionLabel
                    )
                }
            }
        }
    }
}
