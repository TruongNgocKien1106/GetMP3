package com.ngoctien.getmp3.ui

import com.ngoctien.getmp3.viewmodel.ArtistCaseMode

/*
 * Contract giữa UI và presentation.
 *
 * Bento / Compact / skin tương lai đều phát cùng một bộ action.
 * ViewModel và core không biết skin nào đang được dùng.
 */
internal sealed interface TagEditorAction {

    data class TitleChanged(
        val value: String
    ) : TagEditorAction

    object ClearTitle :
        TagEditorAction

    data class ArtistChanged(
        val value: String
    ) : TagEditorAction

    object ClearArtist :
        TagEditorAction

    data class ArtistCaseModeChanged(
        val mode: ArtistCaseMode
    ) : TagEditorAction

    data class AlbumChanged(
        val value: String
    ) : TagEditorAction

    data class YearChanged(
        val value: String
    ) : TagEditorAction

    object QuickFormat :
        TagEditorAction

    data class SelectArtist(
        val value: String
    ) : TagEditorAction

    object DismissArtists :
        TagEditorAction

    object RefreshFiles :
        TagEditorAction

    data class SelectFile(
        val index: Int
    ) : TagEditorAction

    object Skip :
        TagEditorAction

    object Save :
        TagEditorAction

    object Delete :
        TagEditorAction

    object EditLyrics :
        TagEditorAction
}
