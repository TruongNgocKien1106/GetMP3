package com.ngoctien.getmp3.ui.design

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.ui.AppMotion

/*
 * Interaction primitive dùng chung cho toàn app.
 *
 * Feature UI không cần biết:
 * - spring nào đang dùng
 * - pressed scale bao nhiêu
 * - haptic được triển khai như thế nào
 *
 * Skin quyết định phần visual.
 */
internal fun Modifier.appPressable(
    enabled: Boolean = true,
    pressedScale: Float? = null,
    haptic: Boolean = false,
    onClick: () -> Unit
): Modifier =
    composed {

        val design =
            LocalAppDesign.current

        val hapticFeedback =
            LocalHapticFeedback.current

        val interactionSource =
            remember {
                MutableInteractionSource()
            }

        val pressed by
            interactionSource
                .collectIsPressedAsState()

        val targetScale =
            if (
                enabled &&
                pressed
            ) {
                pressedScale
                    ?: design
                        .pressedScale
            }
            else {
                1f
            }

        val scale by
            animateFloatAsState(
                targetValue =
                    targetScale,

                animationSpec =
                    AppMotion
                        .pressSpring,

                label =
                    "app-press-scale"
            )

        val density =
            LocalDensity.current

        val targetDepth =
            if (
                enabled &&
                pressed
            ) {
                with(density) {
                    2.5.dp.toPx()
                }
            }
            else {
                0f
            }

        val depth by
            animateFloatAsState(
                targetValue =
                    targetDepth,

                animationSpec =
                    AppMotion
                        .pressSpring,

                label =
                    "app-press-depth"
            )

        graphicsLayer {

            scaleX =
                scale

            scaleY =
                scale

            translationY =
                depth
        }
            .clickable(
                enabled =
                    enabled,

                interactionSource =
                    interactionSource,

                indication =
                    LocalIndication.current,

                onClick = {

                    if (haptic) {

                        hapticFeedback
                            .performHapticFeedback(
                                HapticFeedbackType
                                    .LongPress
                            )
                    }

                    onClick()
                }
            )
    }
