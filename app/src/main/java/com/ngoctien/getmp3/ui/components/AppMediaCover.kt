package com.ngoctien.getmp3.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
internal fun AppMediaCover(
    model: Any?,
    size: Int,
    modifier: Modifier = Modifier
) {
    val shape =
        RoundedCornerShape(
            16.dp
        )

    if (model != null) {
        AsyncImage(
            model =
                model,

            contentDescription =
                "Ảnh bìa",

            modifier =
                modifier
                    .size(
                        size.dp
                    )
                    .clip(
                        shape
                    ),

            contentScale =
                ContentScale.Crop
        )

        return
    }

    Box(
        modifier =
            modifier
                .size(
                    size.dp
                )
                .clip(
                    shape
                )
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme
                                .colorScheme
                                .primaryContainer,

                            MaterialTheme
                                .colorScheme
                                .secondaryContainer
                        )
                    )
                ),

        contentAlignment =
            Alignment.Center
    ) {
        Icon(
            imageVector =
                Icons.Rounded
                    .MusicNote,

            contentDescription =
                null,

            tint =
                MaterialTheme
                    .colorScheme
                    .primary
        )
    }
}
