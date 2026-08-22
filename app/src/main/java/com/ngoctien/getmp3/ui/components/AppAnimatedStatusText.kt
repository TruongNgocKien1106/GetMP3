package com.ngoctien.getmp3.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.ngoctien.getmp3.ui.AppMotion

@Composable
internal fun AppAnimatedStatusText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color =
        LocalContentColor.current,
    style: TextStyle =
        MaterialTheme
            .typography
            .bodySmall
) {
    AnimatedContent(
        targetState =
            text,

        transitionSpec = {
            (
                fadeIn(
                    animationSpec =
                        tween(
                            AppMotion
                                .ContentMillis
                        )
                ) +
                    slideInVertically(
                        animationSpec =
                            tween(
                                AppMotion
                                    .ContentMillis
                            ),
                        initialOffsetY = {
                            height ->
                            height / 3
                        }
                    )
            )
                .togetherWith(
                    fadeOut(
                        animationSpec =
                            tween(
                                AppMotion
                                    .QuickMillis
                            )
                    ) +
                        slideOutVertically(
                            animationSpec =
                                tween(
                                    AppMotion
                                        .QuickMillis
                                ),
                            targetOffsetY = {
                                height ->
                                -height / 4
                            }
                        )
                )
        },

        label =
            "app-status-text"
    ) { currentText ->

        Text(
            text =
                currentText,

            modifier =
                modifier,

            color =
                color,

            style =
                style
        )
    }
}
