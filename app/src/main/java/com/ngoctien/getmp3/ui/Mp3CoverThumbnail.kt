package com.ngoctien.getmp3.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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


@Composable
internal fun Mp3CoverThumbnail(
    file: MediaSongFile,
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    cornerRadius: Dp = 16.dp
) {
    IndexedMp3CoverThumbnail(
        uri =
            file.uri,

        displayName =
            file.displayName,

        versionKey =
            file.dateModifiedSeconds,

        modifier =
            modifier,

        size =
            size,

        cornerRadius =
            cornerRadius
    )
}


@Composable
internal fun Mp3CoverThumbnail(
    uri: String,
    displayName: String,
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    cornerRadius: Dp = 16.dp
) {
    IndexedMp3CoverThumbnail(
        uri =
            uri,

        displayName =
            displayName,

        versionKey =
            null,

        modifier =
            modifier,

        size =
            size,

        cornerRadius =
            cornerRadius
    )
}


@Composable
private fun IndexedMp3CoverThumbnail(
    uri: String,
    displayName: String,
    versionKey: Any?,
    modifier: Modifier,
    size: Dp,
    cornerRadius: Dp
) {
    val context =
        LocalContext.current

    val coverPath =
        produceState<String?>(
            initialValue =
                null,

            key1 =
                uri,

            key2 =
                versionKey
        ) {
            value =
                runCatching {
                    MediaIndexRepository(
                        context
                    )
                        .getByUri(
                            uri
                        )
                        ?.coverPath
                        ?.trim()
                        ?.takeIf(
                            String::isNotBlank
                        )
                }
                    .getOrNull()
        }.value

    val coverFile =
        coverPath
            ?.let(
                ::File
            )
            ?.takeIf {
                it.isFile
            }

    val shape =
        RoundedCornerShape(
            cornerRadius
        )

    Box(
        modifier =
            modifier
                .size(
                    size
                )
                .clip(
                    shape
                )
                .background(
                    MaterialTheme
                        .colorScheme
                        .surfaceVariant
                ),

        contentAlignment =
            Alignment.Center
    ) {
        if (coverFile != null) {
            AsyncImage(
                model =
                    coverFile,

                contentDescription =
                    "Ảnh bìa $displayName",

                modifier =
                    Modifier
                        .fillMaxSize(),

                contentScale =
                    ContentScale.Crop
            )
        }
        else {
            Icon(
                imageVector =
                    Icons.Rounded
                        .MusicNote,

                contentDescription =
                    null,

                tint =
                    MaterialTheme
                        .colorScheme
                        .primary,

                modifier =
                    Modifier.size(
                        size *
                            0.46f
                    )
            )
        }
    }
}