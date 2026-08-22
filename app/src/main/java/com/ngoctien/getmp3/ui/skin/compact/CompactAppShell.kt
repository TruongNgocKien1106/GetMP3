package com.ngoctien.getmp3.ui.skin.compact

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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

/*
 * Independent COMPACT application shell.
 *
 * Responsibilities:
 * - compact navigation dock;
 * - compact backdrop;
 * - destination transition;
 * - snackbar host;
 * - application chrome only.
 *
 * No ViewModel and no business logic belong here.
 */
@Composable
internal fun CompactAppShell(
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
        },

        bottomBar = {
            CompactBottomDock(
                selected =
                    selectedDestination,

                onSelected =
                    onDestinationSelected
            )
        }
    ) { innerPadding ->

        CompactBackdrop(
            modifier =
                Modifier.fillMaxSize()
        ) {
            AnimatedContent(
                targetState =
                    selectedDestination,

                modifier =
                    Modifier.fillMaxSize(),

                transitionSpec = {

                    val forward =
                        targetState.ordinal >=
                            initialState.ordinal

                    val enterDirection =
                        if (forward) {
                            1
                        }
                        else {
                            -1
                        }

                    val exitDirection =
                        -enterDirection

                    (
                        fadeIn(
                            animationSpec =
                                tween(
                                    durationMillis =
                                        AppMotion
                                            .EnterMillis
                                )
                        ) +
                            slideInHorizontally(
                                animationSpec =
                                    tween(
                                        durationMillis =
                                            AppMotion
                                                .EnterMillis
                                    ),

                                initialOffsetX = {
                                    fullWidth ->

                                    (
                                        fullWidth /
                                            12
                                    ) *
                                        enterDirection
                                }
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
                                slideOutHorizontally(
                                    animationSpec =
                                        tween(
                                            durationMillis =
                                                AppMotion
                                                    .ExitMillis
                                        ),

                                    targetOffsetX = {
                                        fullWidth ->

                                        (
                                            fullWidth /
                                                16
                                        ) *
                                            exitDirection
                                    }
                                )
                        )
                },

                label =
                    "compact-destination"
            ) { destination ->

                content(
                    destination,
                    innerPadding
                )
            }
        }
    }
}
