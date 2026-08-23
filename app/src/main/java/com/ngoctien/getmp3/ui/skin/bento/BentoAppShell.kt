package com.ngoctien.getmp3.ui.skin.bento

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.ngoctien.getmp3.ui.AppDestination
import com.ngoctien.getmp3.ui.AppMotion

@Composable
internal fun BentoAppShell(
    selectedDestination: AppDestination,
    snackbarHostState: SnackbarHostState,
    onDestinationSelected: (AppDestination) -> Unit,
    content:
        @Composable (
            destination: AppDestination,
            innerPadding: PaddingValues
        ) -> Unit
) {
    val colors =
        MaterialTheme.colorScheme

    Scaffold(
        containerColor =
            colors.background,
        contentWindowInsets =
            WindowInsets(
                0,
                0,
                0,
                0
            ),
        snackbarHost = {
            SnackbarHost(
                hostState =
                    snackbarHostState
            )
        }
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                colors.background,
                                colors.surface,
                                colors.background
                            )
                        )
                    )
        ) {
            AnimatedContent(
                targetState =
                    selectedDestination,
                modifier =
                    Modifier.fillMaxSize(),
                transitionSpec = {
                    (
                        fadeIn(
                            tween(
                                AppMotion.EnterMillis
                            )
                        ) +
                            scaleIn(
                                initialScale =
                                    0.988f,
                                animationSpec =
                                    tween(
                                        AppMotion.EnterMillis
                                    )
                            )
                    )
                        .togetherWith(
                            fadeOut(
                                tween(
                                    AppMotion.ExitMillis
                                )
                            ) +
                                scaleOut(
                                    targetScale =
                                        0.988f,
                                    animationSpec =
                                        tween(
                                            AppMotion.ExitMillis
                                        )
                                )
                        )
                },
                label =
                    "bento-destination"
            ) { destination ->
                content(
                    destination,
                    innerPadding
                )
            }
        }
    }
}