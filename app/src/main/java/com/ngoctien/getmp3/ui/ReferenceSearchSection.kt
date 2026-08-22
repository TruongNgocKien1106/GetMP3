package com.ngoctien.getmp3.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ngoctien.getmp3.note.ReferenceSongMatch
import com.ngoctien.getmp3.ui.theme.BrandBlue
import com.ngoctien.getmp3.ui.theme.BrandCyan
import com.ngoctien.getmp3.ui.theme.BrandViolet
import java.io.File

@Composable
internal fun ReferenceMatchChip(
    matches: List<ReferenceSongMatch>,
    onClick: () -> Unit
) {
    if (matches.isEmpty()) {
        return
    }

    val scale =
        remember {
            Animatable(1f)
        }

    val resultSignature =
        matches.joinToString(
            separator = "|"
        ) {
            "${it.uri}:${it.score}"
        }

    LaunchedEffect(
        resultSignature
    ) {
        repeat(2) {
            scale.animateTo(
                targetValue = 1.06f,
                animationSpec =
                    tween(150)
            )

            scale.animateTo(
                targetValue = 1f,
                animationSpec =
                    tween(170)
            )
        }
    }

    FilterChip(
        selected = false,
        onClick = onClick,
        modifier =
            Modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            },
        label = {
            Text(
                text =
                    "Có thể trùng ${matches.size}",
                fontWeight =
                    FontWeight.Bold
            )
        },
        leadingIcon = {
            Icon(
                imageVector =
                    Icons.Rounded.FolderOpen,
                contentDescription = null,
                modifier =
                    Modifier.size(18.dp)
            )
        },
        colors =
            FilterChipDefaults
                .filterChipColors(
                    containerColor =
                        BrandViolet.copy(
                            alpha = 0.14f
                        ),
                    labelColor =
                        BrandViolet,
                    iconColor =
                        BrandViolet
                )
    )
}

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
internal fun ReferenceMatchesSheet(
    query: String,
    matches: List<ReferenceSongMatch>,
    onDismiss: () -> Unit,
    onOpenReferenceSong:
        (ReferenceSongMatch) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest =
            onDismiss,
        modifier =
            Modifier.fillMaxHeight(
                0.9f
            ),
        containerColor =
            MaterialTheme
                .colorScheme
                .surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 20.dp,
                        top = 18.dp,
                        end = 10.dp,
                        bottom = 8.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {
                    Text(
                        text =
                            "Bài có thể đã tồn tại",
                        style =
                            MaterialTheme
                                .typography
                                .titleLarge,
                        fontWeight =
                            FontWeight.Black
                    )

                    Text(
                        text =
                            "${matches.size} kết quả gần giống với “$query”",
                        maxLines = 2,
                        overflow =
                            TextOverflow.Ellipsis,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }

                IconButton(
                    onClick =
                        onDismiss
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Close,
                        contentDescription =
                            "Đóng danh sách đối chiếu"
                    )
                }
            }

            HorizontalDivider(
                color =
                    MaterialTheme
                        .colorScheme
                        .outline
                        .copy(alpha = 0.22f)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding =
                    PaddingValues(16.dp),
                verticalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {
                items(
                    items =
                        matches,
                    key =
                        ReferenceSongMatch::uri
                ) { match ->
                    ReferenceMatchCard(
                        match = match,
                        onOpenEditor = {
                            onDismiss()
                            onOpenReferenceSong(
                                match
                            )
                        }
                    )
                }

                item(
                    key =
                        "continue-youtube"
                ) {
                    OutlinedButton(
                        onClick =
                            onDismiss,
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Tiếp tục xem kết quả YouTube"
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun YouTubeResultsHeader(
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Icon(
            imageVector =
                Icons.Rounded.MusicNote,
            contentDescription =
                null,
            tint =
                MaterialTheme
                    .colorScheme
                    .primary
        )

        Spacer(
            modifier =
                Modifier.width(8.dp)
        )

        Text(
            text =
                "Kết quả YouTube",
            modifier =
                Modifier.weight(1f),
            style =
                MaterialTheme
                    .typography
                    .titleMedium,
            fontWeight =
                FontWeight.ExtraBold
        )

        if (count > 0) {
            Text(
                text =
                    "$count kết quả",
                style =
                    MaterialTheme
                        .typography
                        .labelMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReferenceMatchCard(
    match: ReferenceSongMatch,
    onOpenEditor: () -> Unit
) {
    val context =
        LocalContext.current

    val percent =
        (match.score * 100.0)
            .toInt()
            .coerceIn(
                0,
                100
            )

    val badge =
        when {
            match.score >= 0.97 ->
                "Đã có"

            match.score >= 0.84 ->
                "Rất giống"

            else ->
                "Có thể trùng"
        }

    val accent =
        when {
            match.score >= 0.97 ->
                BrandCyan

            match.score >= 0.84 ->
                BrandBlue

            else ->
                BrandViolet
        }

    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(24.dp),
        color =
            MaterialTheme
                .colorScheme
                .surface
                .copy(alpha = 0.97f),
        shadowElevation = 3.dp,
        tonalElevation = 2.dp,
        border =
            BorderStroke(
                1.dp,
                accent.copy(
                    alpha = 0.30f
                )
            )
    ) {
        Column(
            modifier =
                Modifier.padding(15.dp),
            verticalArrangement =
                Arrangement.spacedBy(
                    11.dp
                )
        ) {
            Row(
                verticalAlignment =
                    Alignment.Top
            ) {
                ReferenceArtwork(
                    match = match,
                    accent = accent
                )

                Spacer(
                    modifier =
                        Modifier.width(12.dp)
                )

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {
                    Text(
                        text =
                            match.title,
                        maxLines = 2,
                        overflow =
                            TextOverflow.Ellipsis,
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                        fontWeight =
                            FontWeight.ExtraBold
                    )

                    Text(
                        text =
                            match.artist.ifBlank {
                                "Chưa xác định Artist"
                            },
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant,
                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium
                    )

                    Text(
                        text =
                            buildString {
                                append(
                                    match.displayName
                                )

                                match.durationSeconds
                                    ?.takeIf {
                                        it > 0L
                                    }
                                    ?.let {
                                        append(
                                            " • "
                                        )

                                        append(
                                            formatReferenceDuration(
                                                it
                                            )
                                        )
                                    }
                            },
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                                .copy(alpha = 0.78f),
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )
                }

                Surface(
                    shape =
                        CircleShape,
                    color =
                        accent.copy(
                            alpha = 0.16f
                        ),
                    border =
                        BorderStroke(
                            1.dp,
                            accent.copy(
                                alpha = 0.45f
                            )
                        )
                ) {
                    Column(
                        modifier =
                            Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 7.dp
                            ),
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {
                        Text(
                            text =
                                "$percent%",
                            color =
                                accent,
                            fontWeight =
                                FontWeight.Black,
                            style =
                                MaterialTheme
                                    .typography
                                    .labelLarge
                        )

                        Text(
                            text =
                                badge,
                            color =
                                accent,
                            style =
                                MaterialTheme
                                    .typography
                                    .labelSmall
                        )
                    }
                }
            }

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        9.dp
                    )
            ) {
                OutlinedButton(
                    onClick = {
                        openReferenceAudio(
                            context = context,
                            uriText =
                                match.uri
                        )
                    },
                    modifier =
                        Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded
                                .PlayArrow,
                        contentDescription =
                            null,
                        modifier =
                            Modifier.size(18.dp)
                    )

                    Spacer(
                        modifier =
                            Modifier.width(5.dp)
                    )

                    Text("Nghe thử")
                }

                Button(
                    onClick =
                        onOpenEditor,
                    modifier =
                        Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Edit,
                        contentDescription =
                            null,
                        modifier =
                            Modifier.size(18.dp)
                    )

                    Spacer(
                        modifier =
                            Modifier.width(5.dp)
                    )

                    Text("Sửa thẻ")
                }
            }
        }
    }
}

@Composable
private fun ReferenceArtwork(
    match: ReferenceSongMatch,
    accent: Color
) {
    val coverFile =
        match.coverPath
            ?.let(::File)
            ?.takeIf {
                it.isFile &&
                    it.length() > 0L
            }

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(
                RoundedCornerShape(
                    18.dp
                )
            )
            .background(
                Brush.linearGradient(
                    listOf(
                        accent.copy(
                            alpha = 0.95f
                        ),
                        BrandBlue.copy(
                            alpha = 0.9f
                        ),
                        BrandViolet.copy(
                            alpha = 0.88f
                        )
                    )
                )
            ),
        contentAlignment =
            Alignment.Center
    ) {
        Icon(
            imageVector =
                Icons.Rounded.MusicNote,
            contentDescription =
                null,
            tint =
                Color.White,
            modifier =
                Modifier.size(27.dp)
        )

        if (coverFile != null) {
            AsyncImage(
                model =
                    coverFile,
                contentDescription =
                    "Ảnh bìa ${match.title}",
                modifier =
                    Modifier.fillMaxSize(),
                contentScale =
                    ContentScale.Crop
            )
        }
    }
}

private fun formatReferenceDuration(
    durationSeconds: Long
): String {
    val safeSeconds =
        durationSeconds
            .coerceAtLeast(0L)

    return "%d:%02d".format(
        safeSeconds / 60L,
        safeSeconds % 60L
    )
}

private fun openReferenceAudio(
    context: Context,
    uriText: String
) {
    val intent =
        Intent(
            Intent.ACTION_VIEW
        ).apply {
            setDataAndType(
                Uri.parse(uriText),
                "audio/mpeg"
            )

            addFlags(
                Intent
                    .FLAG_GRANT_READ_URI_PERMISSION
            )

            addFlags(
                Intent
                    .FLAG_ACTIVITY_NEW_TASK
            )
        }

    try {
        context.startActivity(
            intent
        )
    }
    catch (
        _: ActivityNotFoundException
    ) {
        val fallback =
            Intent.createChooser(
                intent,
                "Chọn ứng dụng nghe thử"
            ).addFlags(
                Intent
                    .FLAG_ACTIVITY_NEW_TASK
            )

        runCatching {
            context.startActivity(
                fallback
            )
        }
    }
    catch (
        _: SecurityException
    ) {
        Unit
    }
}
