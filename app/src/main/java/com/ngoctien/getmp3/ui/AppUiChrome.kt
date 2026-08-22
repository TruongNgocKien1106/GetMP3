package com.ngoctien.getmp3.ui

import com.ngoctien.getmp3.ui.design.appPressable

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.ui.theme.BrandBlue
import com.ngoctien.getmp3.ui.theme.BrandCyan
import com.ngoctien.getmp3.ui.theme.BrandViolet

fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    pressedScale: Float = 0.965f,
    onClick: () -> Unit
): Modifier =
    appPressable(
        enabled =
            enabled,

        pressedScale =
            pressedScale,

        haptic =
            false,

        onClick =
            onClick
    )


@Composable
fun AppScreenBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val colors =
        MaterialTheme.colorScheme

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colors.background,
                            colors.surfaceVariant
                                .copy(
                                    alpha = 0.18f
                                ),
                            colors.background
                        )
                    )
                )
                .background(
                    Brush.radialGradient(
                        listOf(
                            BrandBlue.copy(
                                alpha = 0.28f
                            ),
                            BrandCyan.copy(
                                alpha = 0.10f
                            ),
                            Color.Transparent
                        )
                    )
                )
                .background(
                    Brush.linearGradient(
                        listOf(
                            BrandViolet.copy(
                                alpha = 0.16f
                            ),
                            Color.Transparent,
                            BrandBlue.copy(
                                alpha = 0.10f
                            )
                        )
                    )
                ),
        content = {
            AmbientMotionLayer()
            content()
        }
    )
}

@Composable
private fun BoxScope.AmbientMotionLayer() {
    val transition =
        rememberInfiniteTransition(
            label =
                "app-ambient-motion"
        )

    val drift by
        transition.animateFloat(
            initialValue =
                -1f,
            targetValue =
                1f,
            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(
                            durationMillis =
                                AppMotion
                                    .AmbientDriftMillis,
                            easing =
                                LinearEasing
                        ),
                    repeatMode =
                        RepeatMode.Reverse
                ),
            label =
                "app-ambient-drift"
        )

    val breathe by
        transition.animateFloat(
            initialValue =
                0.72f,
            targetValue =
                1f,
            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(
                            durationMillis =
                                AppMotion
                                    .AmbientBreatheMillis
                        ),
                    repeatMode =
                        RepeatMode.Reverse
                ),
            label =
                "app-ambient-breathe"
        )

    val density =
        LocalDensity.current

    val horizontalTravel =
        with(density) {
            28.dp.toPx()
        }

    val verticalTravel =
        with(density) {
            18.dp.toPx()
        }

    Box(
        modifier =
            Modifier
                .align(
                    Alignment.TopStart
                )
                .size(320.dp)
                .graphicsLayer {
                    translationX =
                        drift *
                            horizontalTravel

                    translationY =
                        drift *
                            verticalTravel

                    alpha =
                        0.78f +
                            0.22f *
                                breathe
                }
                .background(
                    brush =
                        Brush.radialGradient(
                            listOf(
                                BrandBlue.copy(
                                    alpha = 0.14f
                                ),
                                BrandCyan.copy(
                                    alpha = 0.055f
                                ),
                                Color.Transparent
                            )
                        ),
                    shape =
                        CircleShape
                )
    )

    Box(
        modifier =
            Modifier
                .align(
                    Alignment.BottomEnd
                )
                .size(360.dp)
                .graphicsLayer {
                    translationX =
                        -drift *
                            horizontalTravel

                    translationY =
                        -drift *
                            verticalTravel

                    alpha =
                        0.70f +
                            0.24f *
                                breathe
                }
                .background(
                    brush =
                        Brush.radialGradient(
                            listOf(
                                BrandViolet.copy(
                                    alpha = 0.12f
                                ),
                                BrandBlue.copy(
                                    alpha = 0.045f
                                ),
                                Color.Transparent
                            )
                        ),
                    shape =
                        CircleShape
                )
    )
}

@Composable
fun AppLoadingSkeleton(
    text: String,
    modifier: Modifier = Modifier
) {
    val transition =
        rememberInfiniteTransition(
            label =
                "loading-skeleton"
        )

    val pulse by
        transition.animateFloat(
            initialValue =
                0.32f,
            targetValue =
                0.76f,
            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(
                            AppMotion
                                .SkeletonMillis
                        ),
                    repeatMode =
                        RepeatMode.Reverse
                ),
            label =
                "loading-skeleton-pulse"
        )

    Surface(
        modifier =
            modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(28.dp),
        color =
            MaterialTheme
                .colorScheme
                .surface,
        tonalElevation =
            2.dp,
        border =
            BorderStroke(
                1.dp,
                MaterialTheme
                    .colorScheme
                    .outline
                    .copy(
                        alpha = 0.16f
                    )
            )
    ) {
        Column(
            modifier =
                Modifier.padding(20.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier =
                    Modifier.size(24.dp),
                strokeWidth =
                    2.5.dp
            )

            Spacer(
                Modifier.height(16.dp)
            )

            SkeletonBar(
                widthFraction = 0.82f,
                alpha = pulse
            )

            Spacer(
                Modifier.height(8.dp)
            )

            SkeletonBar(
                widthFraction = 0.58f,
                alpha = pulse * 0.78f
            )

            Spacer(
                Modifier.height(14.dp)
            )

            Text(
                text = text,
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SkeletonBar(
    widthFraction: Float,
    alpha: Float
) {
    Box(
        Modifier
            .fillMaxWidth(
                widthFraction
            )
            .height(11.dp)
            .background(
                MaterialTheme
                    .colorScheme
                    .surfaceVariant
                    .copy(
                        alpha = alpha
                    ),
                CircleShape
            )
    )
}

@Composable
fun AppPageHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(32.dp),
                    clip = false
                ),
        shape =
            RoundedCornerShape(32.dp),
        color =
            MaterialTheme
                .colorScheme
                .surface
                .copy(
                    alpha = 0.98f
                ),
        contentColor =
            MaterialTheme
                .colorScheme
                .onSurface,
        tonalElevation =
            8.dp,
        shadowElevation =
            7.dp,
        border =
            BorderStroke(
                1.dp,
                MaterialTheme
                    .colorScheme
                    .primary
                    .copy(
                        alpha = 0.32f
                    )
            )
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = 84.dp
                    )
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme
                                    .colorScheme
                                    .primary
                                    .copy(
                                        alpha = 0.18f
                                    ),
                                Color.Transparent,
                                BrandViolet.copy(
                                    alpha = 0.10f
                                ),
                                Color.Black.copy(
                                    alpha = 0.14f
                                )
                            )
                        )
                    )
                    .padding(
                        horizontal = 16.dp,
                        vertical = 13.dp
                    ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            HeaderIconBadge(
                icon
            )

            Spacer(
                Modifier.width(13.dp)
            )

            Column(
                Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style =
                        MaterialTheme
                            .typography
                            .headlineSmall,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurface,
                    maxLines =
                        1,
                    overflow =
                        TextOverflow.Ellipsis
                )

                Spacer(
                    Modifier.height(2.dp)
                )

                Text(
                    text = subtitle,
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurface
                            .copy(
                                alpha = 0.88f
                            ),
                    maxLines =
                        1,
                    overflow =
                        TextOverflow.Ellipsis
                )
            }

            action?.let {
                Spacer(
                    Modifier.width(10.dp)
                )

                it()
            }
        }
    }
}

@Composable
fun AppHeaderActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    FilledIconButton(
        onClick =
            onClick,
        enabled =
            enabled,
        modifier =
            modifier.size(48.dp)
    ) {
        Icon(
            imageVector =
                icon,
            contentDescription =
                contentDescription,
            modifier =
                Modifier.size(22.dp)
        )
    }
}

@Composable
private fun HeaderIconBadge(
    icon: ImageVector
) {
    Surface(
        modifier =
            Modifier
                .size(58.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(19.dp),
                    clip = false
                ),
        shape =
            RoundedCornerShape(
                19.dp
            ),
        color =
            BrandBlue.copy(
                alpha = 0.20f
            ),
        tonalElevation =
            7.dp,
        shadowElevation =
            6.dp,
        border =
            BorderStroke(
                1.dp,
                Color.White.copy(
                    alpha = 0.28f
                )
            )
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(
                                    alpha = 0.16f
                                ),
                                BrandCyan,
                                BrandBlue,
                                BrandViolet,
                                Color.Black.copy(
                                    alpha = 0.12f
                                )
                            )
                        )
                    ),
            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                imageVector =
                    icon,
                contentDescription =
                    null,
                modifier =
                    Modifier.size(27.dp),
                tint =
                    Color.White
            )
        }
    }
}
