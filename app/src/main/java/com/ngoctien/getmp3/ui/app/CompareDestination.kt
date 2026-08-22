package com.ngoctien.getmp3.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ngoctien.getmp3.ui.AppDestination
import com.ngoctien.getmp3.ui.CompareRoute
import com.ngoctien.getmp3.ui.CompareUiActions
import com.ngoctien.getmp3.viewmodel.CompareViewModel
import com.ngoctien.getmp3.viewmodel.MetadataRepairViewModel
import com.ngoctien.getmp3.viewmodel.YouTubeSearchViewModel

/*
 * Compare presentation boundary.
 *
 * Only this layer knows the concrete ViewModels.
 * CompareRoute / CompareTab / future skins receive state + actions.
 */
@Composable
internal fun CompareDestination(
    compareViewModel: CompareViewModel,
    metadataRepairViewModel: MetadataRepairViewModel,
    youtubeSearchViewModel: YouTubeSearchViewModel,
    modifier: Modifier,
    onNavigate:
        (AppDestination) -> Unit
) {
    val compareState by
        compareViewModel
            .uiState
            .collectAsStateWithLifecycle()

    val repairState by
        metadataRepairViewModel
            .uiState
            .collectAsStateWithLifecycle()

    val actions =
        CompareUiActions(
            refresh =
                compareViewModel::refresh,

            ensureRepair =
                metadataRepairViewModel::
                    ensureLoaded,

            refreshRepair =
                metadataRepairViewModel::
                    refresh,

            changeRepairFilter =
                metadataRepairViewModel::
                    setFilter,

            downloadRepairReplacement = { item ->

                val query =
                    listOf(
                        item.fileTitle
                            .ifBlank {
                                item.title
                            }
                            .trim(),

                        item.fileArtist
                            .ifBlank {
                                item.artist
                            }
                            .trim()
                    )
                        .filter {
                            it.isNotBlank()
                        }
                        .joinToString(
                            separator =
                                " - "
                        )

                youtubeSearchViewModel
                    .setQuery(
                        query
                    )

                if (
                    query.isNotBlank()
                ) {
                    youtubeSearchViewModel
                        .search()
                }

                onNavigate(
                    AppDestination.DOWNLOAD
                )
            },

            selectRepair =
                metadataRepairViewModel::
                    select,

            dismissRepair =
                metadataRepairViewModel::
                    dismissEditor,

            changeRepairTitle =
                metadataRepairViewModel::
                    setTitle,

            changeRepairArtist =
                metadataRepairViewModel::
                    setArtist,

            changeRepairAlbum =
                metadataRepairViewModel::
                    setAlbum,

            changeRepairYear =
                metadataRepairViewModel::
                    setYear,

            lookupRepairYear =
                metadataRepairViewModel::
                    lookupYear,

            saveRepair =
                metadataRepairViewModel::
                    save,

            openPair =
                compareViewModel::
                    openPair,

            closePair =
                compareViewModel::
                    closePair,

            togglePreview =
                compareViewModel::
                    togglePreview,

            keepCurrent =
                compareViewModel::
                    keepCurrent,

            keepReference =
                compareViewModel::
                    keepReference,

            keepBoth =
                compareViewModel::
                    keepBoth
        )

    CompareRoute(
        state =
            compareState,

        repairState =
            repairState,

        actions =
            actions,

        modifier =
            modifier
    )
}
