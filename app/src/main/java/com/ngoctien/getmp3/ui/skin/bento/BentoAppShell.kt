package com.ngoctien.getmp3.ui.skin.bento

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ngoctien.getmp3.ui.AppDestination
import com.ngoctien.getmp3.ui.AppMotion
import com.ngoctien.getmp3.ui.AppScreenBackdrop

/*
 * Bento-specific application shell.
 *
 * Home là dashboard điều hướng chính.
 * Màn con dùng system back / back gesture, không cần nút Back cố định.
 *
 * Business state is deliberately absent from this file.
 */
@Composable
internal fun BentoAppShell(
    selectedDestination: AppDestination,
    snackbarHostState: SnackbarHostState,
    onDestinationSelected:
        (AppDestination) -> Unit,
    content:
        @Composable (
            destination: AppDestination,
            innerPadding: PaddingValues
        ) -> Unit
) {
    Scaffold(
        containerColor =
            Color.Transparent,

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

        AppScreenBackdrop(
            modifier =
                Modifier.fillMaxSize()
        ) {
            Box(
                modifier =
                    Modifier.fillMaxSize()
            ) {
                AnimatedContent(
                    targetState =
                        selectedDestination,

                    modifier =
                        Modifier.fillMaxSize(),

                    transitionSpec = {
                        (
                            fadeIn(
                                animationSpec =
                                    tween(
                                        durationMillis =
                                            AppMotion
                                                .EnterMillis
                                    )
                            ) +
                                scaleIn(
                                    initialScale =
                                        0.985f,

                                    animationSpec =
                                        tween(
                                            durationMillis =
                                                AppMotion
                                                    .EnterMillis
                                        )
                                )
                        )
                            .togetherWith(
                                fadeOut(
                                    animationSpec =
                                        tween(
                                            durationMillis =
                                                AppMotion
                                                    .ExitMillis
                                        )
                                ) +
                                    scaleOut(
                                        targetScale =
                                            0.985f,

                                        animationSpec =
                                            tween(
                                                durationMillis =
                                                    AppMotion
                                                        .ExitMillis
                                            )
                                    )
                            )
                    },

                    label =
                        "app-shell-destination"
                ) { destination ->
                    content(
                        destination,
                        innerPadding
                    )
                }
            }
        }
    }
}
