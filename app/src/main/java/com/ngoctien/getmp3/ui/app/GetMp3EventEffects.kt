package com.ngoctien.getmp3.ui.app

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.ngoctien.getmp3.viewmodel.CompareViewModel
import com.ngoctien.getmp3.viewmodel.DownloadViewModel
import com.ngoctien.getmp3.viewmodel.MetadataRepairViewModel
import com.ngoctien.getmp3.viewmodel.TagEditorViewModel

/*
 * Application-level event effects.
 *
 * This file renders no UI and contains no business rules.
 * It only converts one-shot ViewModel events into snackbar messages.
 */
@Composable
internal fun GetMp3EventEffects(
    downloadViewModel: DownloadViewModel,
    tagEditorViewModel: TagEditorViewModel,
    compareViewModel: CompareViewModel,
    metadataRepairViewModel: MetadataRepairViewModel,
    snackbarHostState: SnackbarHostState
) {
    LaunchedEffect(
        downloadViewModel,
        snackbarHostState
    ) {
        downloadViewModel
            .events
            .collect { event ->
                snackbarHostState.showSnackbar(
                    event.message
                )
            }
    }

    LaunchedEffect(
        tagEditorViewModel,
        snackbarHostState
    ) {
        tagEditorViewModel
            .events
            .collect { event ->
                snackbarHostState.showSnackbar(
                    event.message
                )
            }
    }

    LaunchedEffect(
        compareViewModel,
        snackbarHostState
    ) {
        compareViewModel
            .events
            .collect { event ->
                snackbarHostState.showSnackbar(
                    event.message
                )
            }
    }

    LaunchedEffect(
        metadataRepairViewModel,
        snackbarHostState
    ) {
        metadataRepairViewModel
            .events
            .collect { event ->
                snackbarHostState.showSnackbar(
                    event.message
                )
            }
    }
}
