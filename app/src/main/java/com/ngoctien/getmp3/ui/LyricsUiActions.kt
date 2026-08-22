package com.ngoctien.getmp3.ui

import com.ngoctien.getmp3.lyrics.LibrarySongCandidate
import com.ngoctien.getmp3.lyrics.LyricsSearchResult

/*
 * Semantic actions exposed by the Lyrics feature.
 *
 * UI / skin chỉ biết người dùng có thể làm gì.
 * Không giữ hoặc expose LyricsViewModel.
 */
internal data class LyricsUiActions(
    val setQuery:
        (String) -> Unit,

    val clearQuery:
        () -> Unit,

    val submitSearch:
        () -> Unit,

    val selectSong:
        (LibrarySongCandidate) -> Unit,

    val retryLyricsSearch:
        () -> Unit,

    val selectLyricsResult:
        (LyricsSearchResult) -> Unit,

    val dismissWriteTargetPicker:
        () -> Unit,

    val selectWriteTarget:
        (LibrarySongCandidate) -> Unit,

    val confirmWriteTarget:
        () -> Unit,

    val dismissDirectOverwrite:
        () -> Unit,

    val confirmDirectOverwrite:
        () -> Unit,

    val back:
        () -> Unit,

    val increaseFontSize:
        () -> Unit,

    val decreaseFontSize:
        () -> Unit,

    val toggleAutoScroll:
        () -> Unit,

    val setAutoScrollSpeed:
        (Int) -> Unit,

    val editCurrentLyrics:
        () -> Unit,

    val openWriteTargetPicker:
        () -> Unit,

    val setEditorLyrics:
        (String) -> Unit,

    val pasteEditorLyrics:
        (String?) -> Unit,

    val clearEditorLyrics:
        () -> Unit,

    val restoreFoundLyrics:
        () -> Unit,

    val restoreStoredLyrics:
        () -> Unit,

    val saveLyrics:
        () -> Unit
)
