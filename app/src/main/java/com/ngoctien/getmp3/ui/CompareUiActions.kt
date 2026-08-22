package com.ngoctien.getmp3.ui

import com.ngoctien.getmp3.compare.ComparePair
import com.ngoctien.getmp3.compare.CompareSide
import com.ngoctien.getmp3.data.IndexedMediaEntity
import com.ngoctien.getmp3.viewmodel.MetadataRepairFilter

/*
 * Semantic actions exposed by Compare UI.
 *
 * No ViewModel instance is exposed to the screen/skin.
 * Skin only knows what the user can do.
 */
internal data class CompareUiActions(
    val refresh:
        () -> Unit,

    val ensureRepair:
        () -> Unit,

    val refreshRepair:
        () -> Unit,

    val changeRepairFilter:
        (MetadataRepairFilter) -> Unit,

    val downloadRepairReplacement:
        (IndexedMediaEntity) -> Unit,

    val selectRepair:
        (IndexedMediaEntity) -> Unit,

    val dismissRepair:
        () -> Unit,

    val changeRepairTitle:
        (String) -> Unit,

    val changeRepairArtist:
        (String) -> Unit,

    val changeRepairAlbum:
        (String) -> Unit,

    val changeRepairYear:
        (String) -> Unit,

    val lookupRepairYear:
        () -> Unit,

    val saveRepair:
        () -> Unit,

    val openPair:
        (ComparePair) -> Unit,

    val closePair:
        () -> Unit,

    val togglePreview:
        (CompareSide) -> Unit,

    val keepCurrent:
        () -> Unit,

    val keepReference:
        () -> Unit,

    val keepBoth:
        () -> Unit
)
