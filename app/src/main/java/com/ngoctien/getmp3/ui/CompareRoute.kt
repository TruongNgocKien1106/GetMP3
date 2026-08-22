package com.ngoctien.getmp3.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ngoctien.getmp3.viewmodel.CompareUiState
import com.ngoctien.getmp3.viewmodel.MetadataRepairUiState

/*
 * Pure Compare feature route.
 *
 * Receives immutable UI state + semantic actions.
 * It never receives a ViewModel instance.
 *
 * Compare currently has one shared presentation for every skin.
 * The AppShell already provides the active AppDesign scope.
 */
@Composable
internal fun CompareRoute(
    state: CompareUiState,
    repairState: MetadataRepairUiState,
    actions: CompareUiActions,
    modifier: Modifier = Modifier
) {
    CompareTab(
        state =
            state,

        repairState =
            repairState,

        modifier =
            modifier,

        onRefresh =
            actions.refresh,

        onEnsureRepair =
            actions.ensureRepair,

        onRefreshRepair =
            actions.refreshRepair,

        onRepairFilterChange =
            actions.changeRepairFilter,

        onDownloadRepairReplacement =
            actions.downloadRepairReplacement,

        onRepairYearChange =
            actions.changeRepairYear,

        onLookupRepairYear =
            actions.lookupRepairYear,

        onSelectRepair =
            actions.selectRepair,

        onDismissRepair =
            actions.dismissRepair,

        onRepairTitleChange =
            actions.changeRepairTitle,

        onRepairArtistChange =
            actions.changeRepairArtist,

        onRepairAlbumChange =
            actions.changeRepairAlbum,

        onSaveRepair =
            actions.saveRepair,

        onOpenPair =
            actions.openPair,

        onClosePair =
            actions.closePair,

        onTogglePreview =
            actions.togglePreview,

        onKeepCurrent =
            actions.keepCurrent,

        onKeepReference =
            actions.keepReference,

        onKeepBoth =
            actions.keepBoth
    )
}
