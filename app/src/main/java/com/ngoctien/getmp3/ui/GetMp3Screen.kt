package com.ngoctien.getmp3.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ngoctien.getmp3.ui.design.UiStyle
import com.ngoctien.getmp3.ui.shell.AppShell

/*
 * Pure application screen.
 *
 * Không biết ViewModel.
 * Không collect StateFlow.
 * Không gọi repository/business logic.
 * Không quyết định feature action.
 *
 * Screen chỉ nối:
 *
 * route state
 *      ↓
 * application shell
 *      ↓
 * destination content
 *
 * Vì vậy có thể đổi Bento / Compact / skin khác mà không
 * cần sửa ViewModel hoặc business layer.
 */
@Composable
internal fun GetMp3Screen(
    uiStyle: UiStyle,
    selectedDestination: AppDestination,
    snackbarHostState: SnackbarHostState,
    onDestinationSelected:
        (AppDestination) -> Unit,
    content:
        @Composable (
            destination: AppDestination,
            modifier: Modifier
        ) -> Unit
) {
    AppShell(
        style =
            uiStyle,

        selectedDestination =
            selectedDestination,

        snackbarHostState =
            snackbarHostState,

        onDestinationSelected =
            onDestinationSelected
    ) { destination, innerPadding ->

        content(
            destination,
            Modifier
                .fillMaxSize()
                .padding(
                    innerPadding
                )
        )
    }
}
