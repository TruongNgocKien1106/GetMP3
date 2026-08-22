package com.ngoctien.getmp3.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ngoctien.getmp3.viewmodel.LyricsUiState

/*
 * Pure Lyrics feature route.
 *
 * Route chỉ nhận immutable state + semantic actions.
 * Không nhận ViewModel.
 * Không collect Flow.
 * Không xử lý business event.
 *
 * Lyrics hiện dùng một presentation chung cho mọi skin.
 * AppShell đã cung cấp AppDesign scope đang hoạt động.
 */
@Composable
internal fun LyricsRoute(
    state: LyricsUiState,
    actions: LyricsUiActions,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    LyricsTab(
        state =
            state,

        actions =
            actions,

        snackbarHostState =
            snackbarHostState,

        modifier =
            modifier
    )
}
