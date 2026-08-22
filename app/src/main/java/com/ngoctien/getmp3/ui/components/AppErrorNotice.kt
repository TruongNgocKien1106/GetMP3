package com.ngoctien.getmp3.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
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

@Composable
internal fun AppErrorNotice(
    text: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState =
            text,

        modifier =
            modifier.fillMaxWidth(),

        transitionSpec = {
            fadeIn(
                AppMotion
                    .feedbackSpring
            )
                .togetherWith(
                    fadeOut(
                        AppMotion
                            .feedbackSpring
                    )
                )
        },

        label =
            "error-notice"
    ) { currentText ->

        Surface(
            modifier =
                Modifier.fillMaxWidth(),

            shape =
                MaterialTheme
                    .shapes
                    .large,

            color =
                MaterialTheme
                    .colorScheme
                    .errorContainer,

            contentColor =
                MaterialTheme
                    .colorScheme
                    .onErrorContainer
        ) {
            Row(
                modifier =
                    Modifier.padding(
                        horizontal = 14.dp,
                        vertical = 11.dp
                    ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
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

                Spacer(
                    modifier =
                        Modifier.width(
                            10.dp
                        )
                )

                Column(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            2.dp
                        )
                ) {
                    Text(
                        text =
                            currentText,

                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium
                    )
                }

                if (
                    actionText != null &&
                    onAction != null
                ) {
                    TextButton(
                        onClick =
                            onAction
                    ) {
                        Text(
                            actionText
                        )
                    }
                }
            }
        }
    }
}
