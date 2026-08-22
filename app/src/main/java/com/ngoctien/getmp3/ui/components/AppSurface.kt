package com.ngoctien.getmp3.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.ui.design.LocalAppDesign
import com.ngoctien.getmp3.ui.design.appPressable

@Composable
internal fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    haptic: Boolean = false,
    contentPadding: PaddingValues? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val design =
        LocalAppDesign.current

    val colorScheme =
        MaterialTheme.colorScheme

    val clickableModifier =
        if (onClick != null) {
            modifier.appPressable(
                haptic =
                    haptic,

                onClick =
                    onClick
            )
        }
        else {
            modifier
        }

    val resolvedPadding =
        contentPadding
            ?: PaddingValues(
                design
                    .spacing
                    .cardPadding
            )

    val shape =
        RoundedCornerShape(
            design
                .shapes
                .cardCorner
        )

    Surface(
        modifier =
            clickableModifier
                .shadow(
                    elevation = 14.dp,
                    shape = shape,
                    clip = false
                ),

        shape =
            shape,

        color =
            colorScheme
                .surface
                .copy(
                    alpha = 0.96f
                ),

        contentColor =
            colorScheme.onSurface,

        tonalElevation =
            6.dp,

        shadowElevation =
            6.dp,

        border =
            BorderStroke(
                width =
                    1.dp,

                color =
                    colorScheme
                        .primary
                        .copy(
                            alpha =
                                0.26f
                        )
            )
    ) {
        Box(
            modifier =
                Modifier
                    .background(
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    colorScheme
                                        .primary
                                        .copy(
                                            alpha =
                                                0.20f
                                        ),
                                    colorScheme
                                        .surfaceVariant
                                        .copy(
                                            alpha =
                                                0.16f
                                        ),
                                    Color.Transparent,
                                    Color.Black.copy(
                                        alpha =
                                            0.12f
                                    )
                                )
                        )
                    )
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color.White.copy(
                                        alpha =
                                            0.075f
                                    ),
                                    Color.Transparent
                                )
                        )
                    )
                    .padding(
                        resolvedPadding
                    ),

            content =
                content
        )
    }
}
