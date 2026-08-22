package com.ngoctien.getmp3.ui.shell

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import com.ngoctien.getmp3.ui.AppDestination
import com.ngoctien.getmp3.ui.design.ProvideAppDesign
import com.ngoctien.getmp3.ui.design.UiStyle
import com.ngoctien.getmp3.ui.skin.bento.BentoAppShell
import com.ngoctien.getmp3.ui.skin.compact.CompactAppShell

/*
 * Shell owns application chrome:
 *
 * - bottom navigation
 * - background
 * - screen transitions
 * - snackbar host
 *
 * Feature screens do not need to know how those things are rendered.
 */
@Composable
internal fun AppShell(
    style: UiStyle,
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
    ProvideAppDesign(
        style =
            style
    ) {
        when (style) {
            UiStyle.BENTO -> {
                BentoAppShell(
                    selectedDestination =
                        selectedDestination,

                    snackbarHostState =
                        snackbarHostState,

                    onDestinationSelected =
                        onDestinationSelected,

                    content =
                        content
                )
            }

            UiStyle.COMPACT -> {
                CompactAppShell(
                    selectedDestination =
                        selectedDestination,

                    snackbarHostState =
                        snackbarHostState,

                    onDestinationSelected =
                        onDestinationSelected,

                    content =
                        content
                )
            }
        }
    }
}
