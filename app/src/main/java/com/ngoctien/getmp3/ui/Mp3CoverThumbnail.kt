package com.ngoctien.getmp3.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ngoctien.getmp3.library.MediaIndexRepository
import com.ngoctien.getmp3.tag.MediaSongFile
import java.io.File

/**
 * Cover thumbnail backed by the shared media index.
 *
 * The MP3 itself is no longer opened by every visible row. Heavy embedded-art
 * extraction is performed once by MediaIndexRepository and persisted in the
 * app's media-index cover directory.
 */
@Composable
internal fun Mp3CoverThumbnail(
    file: MediaSongFile,
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    cornerRadius: Dp = 16.dp
) {
    val context =
        LocalContext.current

    val coverPath =
        produceState<String?>(
            initialValue = null,
            key1 = file.uri,
            key2 = file.dateModifiedSeconds
        ) {
            value =
                MediaIndexRepository(
                    context
                )
                    .getByUri(
                        file.uri
                    )
                    ?.coverPath
                    ?.takeIf {
                        it.isNotBlank()
                    }
        }.value

    val shape =
        RoundedCornerShape(
            cornerRadius
        )

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                MaterialTheme
                    .colorScheme
                    .surfaceVariant
            ),
        contentAlignment =
            Alignment.Center
    ) {
        if (
            !coverPath.isNullOrBlank()
        ) {
            AsyncImage(
                model =
                    File(coverPath),
                contentDescription =
                    "Ảnh bìa ${file.displayName}",
                modifier =
                    Modifier.matchParentSize(),
                contentScale =
                    ContentScale.Crop
            )
        } else {
            Icon(
                imageVector =
                    Icons.Rounded.MusicNote,
                contentDescription =
                    null,
                tint =
                    MaterialTheme
                        .colorScheme
                        .primary,
                modifier =
                    Modifier.size(
                        size * 0.46f
                    )
            )
        }
    }
}
