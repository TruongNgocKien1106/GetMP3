package com.ngoctien.getmp3.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ngoctien.getmp3.data.IndexedMediaEntity
import com.ngoctien.getmp3.data.MediaMetadataStatus
import com.ngoctien.getmp3.ui.theme.BrandBlue
import com.ngoctien.getmp3.ui.theme.BrandCyan
import com.ngoctien.getmp3.ui.theme.BrandViolet
import com.ngoctien.getmp3.viewmodel.LibraryFilterMode
import com.ngoctien.getmp3.viewmodel.LibrarySortMode
import com.ngoctien.getmp3.viewmodel.LibraryUiState
import java.io.File

@Composable
internal fun LibraryScreen(
    state: LibraryUiState,
    indexIsScanning: Boolean,
    indexProgress: Float,
    modifier: Modifier = Modifier,
    onQueryChange: (String) -> Unit,
    onSortChange: (LibrarySortMode) -> Unit,
    onFilterChange: (LibraryFilterMode) -> Unit,
    onRefresh: () -> Unit,
    onRescan: () -> Unit,
    onOpenSettings: () -> Unit,
    onSearchYouTube: (String) -> Unit,
    onOpenLyrics: (IndexedMediaEntity) -> Unit,
    onEditTag: (IndexedMediaEntity) -> Unit
) {
    val context = LocalContext.current
    var infoSong by remember { mutableStateOf<IndexedMediaEntity?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AppPageHeader(
                title = "Library",
                subtitle = "${state.totalCount} bài • Nghe • tìm • quản lý",
                icon = Icons.Rounded.LibraryMusic,
                action = {
                    if (state.isConfigured) {
                        AppHeaderActionButton(
                            icon = Icons.Rounded.Sync,
                            contentDescription = "Đồng bộ lại Library",
                            enabled = !indexIsScanning,
                            onClick = onRescan
                        )
                    }
                }
            )
        }

        if (!state.isConfigured) {
            item {
                LibraryMessageCard(
                    title = "Chưa cài Library",
                    body = "Chọn thư mục Music trong Cài đặt rồi bấm Cài & đồng bộ.",
                    actionLabel = "Cài thư mục",
                    onAction = onOpenSettings
                )
            }
        } else {
            item {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Tìm Title hoặc Artist") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (state.query.isNotBlank()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = "Xóa từ khóa"
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(22.dp)
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        LibraryFilterChip(
                            "Mới thêm",
                            state.sortMode == LibrarySortMode.NEWEST
                        ) { onSortChange(LibrarySortMode.NEWEST) }
                    }
                    item {
                        LibraryFilterChip(
                            "A-Z",
                            state.sortMode == LibrarySortMode.TITLE
                        ) { onSortChange(LibrarySortMode.TITLE) }
                    }
                    item {
                        LibraryFilterChip(
                            "Artist",
                            state.sortMode == LibrarySortMode.ARTIST
                        ) { onSortChange(LibrarySortMode.ARTIST) }
                    }
                    item {
                        LibraryFilterChip(
                            "Cần xử lý",
                            state.filterMode == LibraryFilterMode.NEEDS_ATTENTION
                        ) {
                            onFilterChange(
                                if (state.filterMode == LibraryFilterMode.NEEDS_ATTENTION) {
                                    LibraryFilterMode.ALL
                                } else {
                                    LibraryFilterMode.NEEDS_ATTENTION
                                }
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = buildString {
                        append("${state.totalCount} bài")
                        if (state.newCount > 0) append(" • mới ${state.newCount}")
                        if (state.attentionCount > 0) {
                            append(" • cần xử lý ${state.attentionCount}")
                        }
                        if (state.query.isNotBlank()) {
                            append(" • ${state.songs.size} kết quả")
                        }
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (indexIsScanning) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LinearProgressIndicator(
                            progress = { indexProgress.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            "Đang đồng bộ Library...",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            when {
                state.isLoading -> item {
                    LibraryMessageCard(
                        "Đang mở Library",
                        "Đang đọc dữ liệu đã index."
                    )
                }

                state.errorMessage != null -> item {
                    LibraryMessageCard(
                        "Không đọc được Library",
                        state.errorMessage,
                        "Thử lại",
                        onRefresh
                    )
                }

                state.totalCount == 0 -> item {
                    LibraryMessageCard(
                        "Library chưa có bài",
                        "Đồng bộ để GetMP3 đọc nhạc trong thư mục Library.",
                        "Đồng bộ ngay",
                        onRescan
                    )
                }

                state.songs.isEmpty() -> item {
                    LibraryMessageCard(
                        "Không có kết quả phù hợp",
                        if (state.query.isNotBlank()) {
                            "Không tìm thấy bài khớp từ khóa hiện tại."
                        } else {
                            "Không có bài nào khớp bộ lọc hiện tại."
                        }
                    )
                }

                else -> items(state.songs, key = { it.uri }) { song ->
                    LibrarySongCard(
                        song = song,
                        onPlay = { playExternal(context, song) },
                        onLyrics = { onOpenLyrics(song) },
                        onEditTag = { onEditTag(song) },
                        onInfo = { infoSong = song }
                    )
                }
            }

            if (state.query.trim().isNotBlank() && !state.isLoading) {
                item {
                    LibraryYouTubeCta(
                        query = state.query,
                        localResultCount = state.songs.size
                    ) {
                        onSearchYouTube(state.query)
                    }
                }
            }
        }
    }

    infoSong?.let { song ->
        AlertDialog(
            onDismissRequest = { infoSong = null },
            title = { Text(song.title.ifBlank { song.displayName }) },
            text = {
                Text(
                    buildString {
                        appendLine("Artist: ${song.artist.ifBlank { "—" }}")
                        appendLine("Album: ${song.album.ifBlank { "—" }}")
                        appendLine("Year: ${song.year.ifBlank { "—" }}")
                        appendLine(
                            "Bitrate: ${
                                if (song.bitrateKbps > 0) {
                                    "${song.bitrateKbps} kbps"
                                } else {
                                    "—"
                                }
                            }"
                        )
                        append("File: ${song.displayName}")
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { infoSong = null }) {
                    Text("Đóng")
                }
            }
        )
    }
}

@Composable
private fun LibraryFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}

@Composable
private fun LibrarySongCard(
    song: IndexedMediaEntity,
    onPlay: () -> Unit,
    onLyrics: () -> Unit,
    onEditTag: () -> Unit,
    onInfo: () -> Unit
) {
    var menuExpanded by remember(song.uri) { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onPlay),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 5.dp,
        shadowElevation = 7.dp,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LibraryCover(song)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    song.title.ifBlank { song.displayName },
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    song.artist.ifBlank { "Chưa có Artist" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    libraryMetadataLabel(song),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (song.needsLibraryAttention()) {
                        BrandViolet
                    } else {
                        BrandCyan
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Rounded.MoreVert,
                        contentDescription = "Thao tác với bài hát"
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    LibraryMenuItem("Nghe") {
                        menuExpanded = false
                        onPlay()
                    }
                    LibraryMenuItem("Lyrics") {
                        menuExpanded = false
                        onLyrics()
                    }
                    LibraryMenuItem("Sửa thẻ") {
                        menuExpanded = false
                        onEditTag()
                    }
                    LibraryMenuItem("Thông tin") {
                        menuExpanded = false
                        onInfo()
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryMenuItem(
    text: String,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text) },
        onClick = onClick
    )
}

@Composable
private fun LibraryCover(song: IndexedMediaEntity) {
    val shape = RoundedCornerShape(18.dp)

    if (!song.coverPath.isNullOrBlank()) {
        AsyncImage(
            model = File(song.coverPath.orEmpty()),
            contentDescription = "Cover ${song.title}",
            modifier = Modifier.size(64.dp).clip(shape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(BrandCyan, BrandBlue, BrandViolet)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

private fun libraryMetadataLabel(song: IndexedMediaEntity): String = when {
    MediaMetadataStatus.isError(song.metadataStatus) -> "Metadata lỗi"
    song.tagTitle.isBlank() || song.tagArtist.isBlank() || song.album.isBlank() ->
        "Thiếu tag"
    song.year.isBlank() -> "Thiếu Year"
    song.coverPath.isNullOrBlank() -> "Thiếu cover"
    else -> listOfNotNull(
        song.year.takeIf { it.isNotBlank() },
        song.bitrateKbps.takeIf { it > 0 }?.let { "$it kbps" }
    ).joinToString(" • ").ifBlank { "Metadata OK" }
}

private fun IndexedMediaEntity.needsLibraryAttention(): Boolean =
    MediaMetadataStatus.isError(metadataStatus) ||
        tagTitle.isBlank() ||
        tagArtist.isBlank() ||
        album.isBlank() ||
        year.isBlank()

@Composable
private fun LibraryYouTubeCta(
    query: String,
    localResultCount: Int,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = BrandBlue.copy(alpha = 0.10f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.30f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                if (localResultCount == 0) {
                    "Không tìm thấy trong Library?"
                } else {
                    "Đã hết kết quả local"
                },
                fontWeight = FontWeight.Bold
            )
            Text(
                "Tìm tiếp \"$query\" trên YouTube.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) {
                Icon(Icons.Rounded.Download, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Tìm trên YouTube")
            }
        }
    }
}

@Composable
private fun LibraryMessageCard(
    title: String,
    body: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, BrandCyan.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = BrandCyan.copy(alpha = 0.16f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.LibraryMusic,
                            contentDescription = null,
                            tint = BrandCyan
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold)
                    Text(
                        body,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (actionLabel != null && onAction != null) {
                Button(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

private fun playExternal(
    context: Context,
    song: IndexedMediaEntity
) {
    val mediaUri = Uri.parse(song.uri)
    val mimeType = song.mimeType
        .takeIf { it.startsWith("audio/") }
        ?: "audio/*"

    val playIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(mediaUri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val chooser = Intent.createChooser(playIntent, "Phát bằng")

    runCatching {
        context.startActivity(chooser)
    }.onFailure {
        Toast.makeText(
            context,
            "Không tìm thấy ứng dụng phát nhạc phù hợp",
            Toast.LENGTH_SHORT
        ).show()
    }
}
