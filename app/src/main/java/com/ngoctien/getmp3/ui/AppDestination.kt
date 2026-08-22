package com.ngoctien.getmp3.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.ui.graphics.vector.ImageVector

internal enum class AppDestination(
    val title: String,
    val icon: ImageVector
) {
    DOWNLOAD(
        title = "Inbox",
        icon = Icons.Rounded.Download
    ),

    LYRICS(
        title = "Lyrics",
        icon = Icons.Rounded.LibraryMusic
    ),

    EDIT_TAG(
        title = "Thẻ",
        icon = Icons.Rounded.Edit
    ),

    COMPARE(
        title = "Đối chiếu",
        icon = Icons.Rounded.Sync
    ),

    SETTINGS(
        title = "Cài đặt",
        icon = Icons.Rounded.Settings
    ),

    HOME(
        title = "Home",
        icon = Icons.Rounded.Home
    ),

    LIBRARY(
        title = "Library",
        icon = Icons.Rounded.LibraryMusic
    )
}

internal val DockDestinations =
    listOf(
        AppDestination.HOME,
        AppDestination.DOWNLOAD,
        AppDestination.LYRICS,
        AppDestination.EDIT_TAG,
        AppDestination.COMPARE
    )
