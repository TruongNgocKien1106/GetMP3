package com.ngoctien.getmp3.ui

import com.ngoctien.getmp3.data.DownloadJobEntity
import com.ngoctien.getmp3.note.ReferenceSongMatch
import com.ngoctien.getmp3.viewmodel.SearchDownloadSection
import com.ngoctien.getmp3.youtube.YouTubeSearchResult

internal sealed interface SearchDownloadAction {

    data class QueryChanged(
        val value: String
    ) : SearchDownloadAction

    object Search :
        SearchDownloadAction

    object ClearSearch :
        SearchDownloadAction

    object LoadMore :
        SearchDownloadAction

    data class SelectSection(
        val value: SearchDownloadSection
    ) : SearchDownloadAction

    data class PasteAndDownload(
        val value: CharSequence?
    ) : SearchDownloadAction

    data class DownloadResult(
        val value: YouTubeSearchResult
    ) : SearchDownloadAction

    data class OpenReferenceSong(
        val value: ReferenceSongMatch
    ) : SearchDownloadAction

    data class CancelJob(
        val id: String
    ) : SearchDownloadAction

    data class RetryJob(
        val job: DownloadJobEntity
    ) : SearchDownloadAction

    object ClearHistory :
        SearchDownloadAction

    object RetryFfmpeg :
        SearchDownloadAction
}
