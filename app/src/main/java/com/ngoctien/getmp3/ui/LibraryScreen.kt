package com.ngoctien.getmp3.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.Dp
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

private const val RECENT_WINDOW_MS =
    7L *
        24L *
        60L *
        60L *
        1000L

private enum class LibraryBrowseMode {
    SONGS,
    ARTISTS,
    ALBUMS,
    YEARS
}

private data class LibraryGroupRowData(
    val label: String,
    val searchValue: String,
    val count: Int,
    val coverPath: String?
)

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
    val context =
        LocalContext.current

    var browseMode by
        remember {
            mutableStateOf(
                LibraryBrowseMode.SONGS
            )
        }

    var recentOnly by
        remember {
            mutableStateOf(
                false
            )
        }

    val recentThreshold =
        System.currentTimeMillis() -
            RECENT_WINDOW_MS

    val visibleSongs =
        if (recentOnly) {
            state.songs
                .filter {
                    it.indexedAt >=
                        recentThreshold
                }
        } else {
            state.songs
        }

    val groupRows =
        remember(
            visibleSongs,
            browseMode
        ) {
            buildGroupRows(
                songs =
                    visibleSongs,

                mode =
                    browseMode
            )
        }

    AppScreenBackdrop(
        modifier =
            modifier
    ) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
            contentPadding =
                PaddingValues(
                    start =
                        16.dp,

                    end =
                        16.dp,

                    top =
                        16.dp,

                    bottom =
                        28.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {
            item {
                LibraryPrototypeHeader(
                    indexIsScanning =
                        indexIsScanning,

                    onRescan =
                        onRescan
                )
            }

            if (
                !state.isConfigured
            ) {
                item {
                    LibraryMessageCard(
                        title =
                            "Chưa cài Library",

                        body =
                            "Chọn thư mục Music trong Cài đặt rồi đồng bộ Library.",

                        actionLabel =
                            "Cài thư mục",

                        onAction =
                            onOpenSettings
                    )
                }

                return@LazyColumn
            }

            item {
                LibrarySearchField(
                    query =
                        state.query,

                    onQueryChange =
                        onQueryChange
                )
            }

            item {
                LibraryPrimaryTabs(
                    selectedMode =
                        browseMode,

                    onSelect = {
                        browseMode =
                            it
                    }
                )
            }

            item {
                LibraryFilterStrip(
                    state =
                        state,

                    recentOnly =
                        recentOnly,

                    onAll = {
                        recentOnly =
                            false

                        onFilterChange(
                            LibraryFilterMode.ALL
                        )
                    },

                    onAttention = {
                        recentOnly =
                            false

                        onFilterChange(
                            LibraryFilterMode
                                .NEEDS_ATTENTION
                        )
                    },

                    onRecent = {
                        recentOnly =
                            true

                        onFilterChange(
                            LibraryFilterMode.ALL
                        )

                        onSortChange(
                            LibrarySortMode.NEWEST
                        )
                    },

                    onSort = {
                        onSortChange(
                            if (
                                browseMode ==
                                LibraryBrowseMode.ARTISTS
                            ) {
                                LibrarySortMode.ARTIST
                            } else {
                                LibrarySortMode.TITLE
                            }
                        )
                    }
                )
            }

            if (
                indexIsScanning
            ) {
                item {
                    LibrarySyncIndicator(
                        progress =
                            indexProgress
                    )
                }
            }

            item {
                LibrarySectionHeader(
                    mode =
                        browseMode,

                    count =
                        if (
                            browseMode ==
                            LibraryBrowseMode.SONGS
                        ) {
                            visibleSongs.size
                        } else {
                            groupRows.size
                        }
                )
            }

            when {
                state.isLoading -> {
                    item {
                        LibraryMessageCard(
                            title =
                                "Đang mở Library",

                            body =
                                "Đang đọc dữ liệu đã index."
                        )
                    }
                }

                state.errorMessage != null -> {
                    item {
                        LibraryMessageCard(
                            title =
                                "Không đọc được Library",

                            body =
                                state.errorMessage,

                            actionLabel =
                                "Thử lại",

                            onAction =
                                onRefresh
                        )
                    }
                }

                state.totalCount == 0 -> {
                    item {
                        LibraryMessageCard(
                            title =
                                "Library chưa có bài",

                            body =
                                "Đồng bộ để GetMP3 đọc nhạc trong thư mục Library.",

                            actionLabel =
                                "Đồng bộ ngay",

                            onAction =
                                onRescan
                        )
                    }
                }

                browseMode ==
                    LibraryBrowseMode.SONGS &&
                    visibleSongs.isEmpty() -> {

                    item {
                        LibraryMessageCard(
                            title =
                                "Không có kết quả phù hợp",

                            body =
                                if (
                                    recentOnly
                                ) {
                                    "Không có bài mới trong 7 ngày gần đây."
                                } else if (
                                    state.query
                                        .isNotBlank()
                                ) {
                                    "Không tìm thấy bài khớp từ khóa hiện tại."
                                } else {
                                    "Không có bài nào khớp bộ lọc hiện tại."
                                }
                        )
                    }
                }

                browseMode !=
                    LibraryBrowseMode.SONGS &&
                    groupRows.isEmpty() -> {

                    item {
                        LibraryMessageCard(
                            title =
                                "Chưa có dữ liệu",

                            body =
                                "Không có dữ liệu phù hợp với nhóm đang chọn."
                        )
                    }
                }

                browseMode ==
                    LibraryBrowseMode.SONGS -> {

                    items(
                        items =
                            visibleSongs,

                        key = {
                            it.uri
                        }
                    ) { song ->

                        LibraryPrototypeSongCard(
                            song =
                                song,

                            onPlay = {
                                playExternal(
                                    context =
                                        context,

                                    song =
                                        song
                                )
                            },

                            onEditTag = {
                                onEditTag(
                                    song
                                )
                            }
                        )
                    }
                }

                else -> {
                    items(
                        items =
                            groupRows,

                        key = {
                            "${browseMode.name}:${it.label}"
                        }
                    ) { group ->

                        LibraryGroupCard(
                            item =
                                group,

                            onClick = {
                                if (
                                    group.searchValue
                                        .isNotBlank()
                                ) {
                                    onQueryChange(
                                        group.searchValue
                                    )

                                    browseMode =
                                        LibraryBrowseMode.SONGS
                                }
                            }
                        )
                    }
                }
            }

            if (
                state.query
                    .trim()
                    .isNotBlank() &&
                visibleSongs.isEmpty() &&
                !state.isLoading
            ) {
                item {
                    LibraryYouTubeCta(
                        query =
                            state.query,

                        onClick = {
                            onSearchYouTube(
                                state.query
                            )
                        }
                    )
                }
            }
        }
    }

    // Preserve the route contract. The prototype Library list does
    // not expose a separate Lyrics action on each song card.
    @Suppress("UNUSED_VARIABLE")
    val lyricsRouteContract =
        onOpenLyrics
}


@Composable
private fun LibraryPrototypeHeader(
    indexIsScanning: Boolean,
    onRescan: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    top =
                        4.dp,

                    bottom =
                        8.dp
                ),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {
        Text(
            text =
                "Library",

            style =
                MaterialTheme
                    .typography
                    .headlineLarge,

            fontWeight =
                FontWeight.Black,

            color =
                MaterialTheme
                    .colorScheme
                    .onBackground
        )

        Surface(
            modifier =
                Modifier
                    .size(
                        58.dp
                    )
                    .bouncyClickable(
                        enabled =
                            !indexIsScanning,

                        pressedScale =
                            0.90f,

                        onClick =
                            onRescan
                    ),
            shape =
                RoundedCornerShape(
                    20.dp
                ),
            color =
                MaterialTheme
                    .colorScheme
                    .surface
                    .copy(
                        alpha =
                            0.90f
                    ),
            tonalElevation =
                7.dp,
            shadowElevation =
                9.dp,
            border =
                BorderStroke(
                    1.dp,
                    BrandBlue.copy(
                        alpha =
                            0.40f
                    )
                )
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    BrandBlue.copy(
                                        alpha =
                                            0.18f
                                    ),
                                    Color.Transparent,
                                    BrandViolet.copy(
                                        alpha =
                                            0.12f
                                    )
                                )
                            )
                        ),
                contentAlignment =
                    Alignment.Center
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.Sync,

                    contentDescription =
                        "Đồng bộ Library",

                    modifier =
                        Modifier.size(
                            25.dp
                        ),

                    tint =
                        if (
                            indexIsScanning
                        ) {
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                        } else {
                            MaterialTheme
                                .colorScheme
                                .primary
                        }
                )
            }
        }
    }
}


@Composable
private fun LibrarySearchField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value =
            query,

        onValueChange =
            onQueryChange,

        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(
                    min =
                        62.dp
                ),

        singleLine =
            true,

        placeholder = {
            Text(
                "Title, Artist, Album hoặc Filename"
            )
        },

        leadingIcon = {
            Icon(
                imageVector =
                    Icons.Rounded.Search,

                contentDescription =
                    null
            )
        },

        trailingIcon = {
            if (
                query.isNotBlank()
            ) {
                IconButton(
                    onClick = {
                        onQueryChange(
                            ""
                        )
                    }
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Close,

                        contentDescription =
                            "Xóa từ khóa"
                    )
                }
            }
        },

        shape =
            RoundedCornerShape(
                24.dp
            )
    )
}


@Composable
private fun LibraryPrimaryTabs(
    selectedMode: LibraryBrowseMode,
    onSelect: (LibraryBrowseMode) -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                22.dp
            ),
        color =
            MaterialTheme
                .colorScheme
                .surface
                .copy(
                    alpha =
                        0.68f
                ),
        border =
            BorderStroke(
                1.dp,
                MaterialTheme
                    .colorScheme
                    .outlineVariant
            )
    ) {
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        4.dp
                    )
        ) {
            val gap =
                5.dp

            val tabWidth =
                (
                    maxWidth -
                        gap *
                        3f
                    ) /
                    4f

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        gap
                    )
            ) {
                LibraryPrimaryTab(
                    text =
                        "Bài hát",

                    selected =
                        selectedMode ==
                            LibraryBrowseMode.SONGS,

                    modifier =
                        Modifier.width(
                            tabWidth
                        ),

                    onClick = {
                        onSelect(
                            LibraryBrowseMode.SONGS
                        )
                    }
                )

                LibraryPrimaryTab(
                    text =
                        "Artist",

                    selected =
                        selectedMode ==
                            LibraryBrowseMode.ARTISTS,

                    modifier =
                        Modifier.width(
                            tabWidth
                        ),

                    onClick = {
                        onSelect(
                            LibraryBrowseMode.ARTISTS
                        )
                    }
                )

                LibraryPrimaryTab(
                    text =
                        "Album",

                    selected =
                        selectedMode ==
                            LibraryBrowseMode.ALBUMS,

                    modifier =
                        Modifier.width(
                            tabWidth
                        ),

                    onClick = {
                        onSelect(
                            LibraryBrowseMode.ALBUMS
                        )
                    }
                )

                LibraryPrimaryTab(
                    text =
                        "Năm",

                    selected =
                        selectedMode ==
                            LibraryBrowseMode.YEARS,

                    modifier =
                        Modifier.width(
                            tabWidth
                        ),

                    onClick = {
                        onSelect(
                            LibraryBrowseMode.YEARS
                        )
                    }
                )
            }
        }
    }
}


@Composable
private fun LibraryPrimaryTab(
    text: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier =
            modifier
                .height(
                    46.dp
                )
                .bouncyClickable(
                    pressedScale =
                        0.95f,

                    onClick =
                        onClick
                ),
        shape =
            RoundedCornerShape(
                17.dp
            ),
        color =
            if (
                selected
            ) {
                Color.Transparent
            } else {
                MaterialTheme
                    .colorScheme
                    .surfaceVariant
                    .copy(
                        alpha =
                            0.34f
                    )
            },
        tonalElevation =
            if (
                selected
            ) {
                7.dp
            } else {
                0.dp
            },
        shadowElevation =
            if (
                selected
            ) {
                7.dp
            } else {
                0.dp
            }
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        if (
                            selected
                        ) {
                            Brush.linearGradient(
                                listOf(
                                    BrandCyan,
                                    BrandBlue,
                                    BrandViolet
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Transparent
                                )
                            )
                        }
                    ),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    text,

                fontWeight =
                    if (
                        selected
                    ) {
                        FontWeight.Black
                    } else {
                        FontWeight.Bold
                    },

                color =
                    if (
                        selected
                    ) {
                        Color.White
                    } else {
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                    }
            )
        }
    }
}


@Composable
private fun LibraryFilterStrip(
    state: LibraryUiState,
    recentOnly: Boolean,
    onAll: () -> Unit,
    onAttention: () -> Unit,
    onRecent: () -> Unit,
    onSort: () -> Unit
) {
    BoxWithConstraints(
        modifier =
            Modifier.fillMaxWidth()
    ) {
        val sortWidth =
            58.dp

        val gap =
            8.dp

        val filterWidth =
            maxWidth -
                sortWidth -
                gap

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(
                    gap
                )
        ) {
            Surface(
                modifier =
                    Modifier
                        .width(
                            filterWidth
                        )
                        .height(
                            64.dp
                        ),
                shape =
                    RoundedCornerShape(
                        22.dp
                    ),
                color =
                    MaterialTheme
                        .colorScheme
                        .surface
                        .copy(
                            alpha =
                                0.72f
                        ),
                border =
                    BorderStroke(
                        1.dp,
                        MaterialTheme
                            .colorScheme
                            .outlineVariant
                    )
            ) {
                BoxWithConstraints(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                4.dp
                            )
                ) {
                    val itemWidth =
                        maxWidth /
                            3f

                    Row {
                        LibraryCounterFilter(
                            label =
                                "Tất cả",

                            count =
                                state.totalCount,

                            selected =
                                state.filterMode ==
                                    LibraryFilterMode.ALL &&
                                    !recentOnly,

                            modifier =
                                Modifier.width(
                                    itemWidth
                                ),

                            onClick =
                                onAll
                        )

                        LibraryCounterFilter(
                            label =
                                "Cần xử lý",

                            count =
                                state.attentionCount,

                            selected =
                                state.filterMode ==
                                    LibraryFilterMode
                                        .NEEDS_ATTENTION &&
                                    !recentOnly,

                            modifier =
                                Modifier.width(
                                    itemWidth
                                ),

                            onClick =
                                onAttention
                        )

                        LibraryCounterFilter(
                            label =
                                "Mới thêm",

                            count =
                                state.newCount,

                            selected =
                                recentOnly,

                            modifier =
                                Modifier.width(
                                    itemWidth
                                ),

                            onClick =
                                onRecent
                        )
                    }
                }
            }

            Surface(
                modifier =
                    Modifier
                        .width(
                            sortWidth
                        )
                        .height(
                            64.dp
                        )
                        .bouncyClickable(
                            pressedScale =
                                0.93f,

                            onClick =
                                onSort
                        ),
                shape =
                    RoundedCornerShape(
                        20.dp
                    ),
                color =
                    MaterialTheme
                        .colorScheme
                        .surface
                        .copy(
                            alpha =
                                0.82f
                        ),
                tonalElevation =
                    5.dp,
                shadowElevation =
                    6.dp,
                border =
                    BorderStroke(
                        1.dp,
                        BrandBlue.copy(
                            alpha =
                                0.38f
                        )
                    )
            ) {
                Column(
                    modifier =
                        Modifier.fillMaxSize(),
                    horizontalAlignment =
                        Alignment.CenterHorizontally,
                    verticalArrangement =
                        Arrangement.Center
                ) {
                    Text(
                        text =
                            "AZ",

                        style =
                            MaterialTheme
                                .typography
                                .labelMedium,

                        fontWeight =
                            FontWeight.Black
                    )

                    Text(
                        text =
                            "↓",

                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,

                        color =
                            BrandCyan
                    )
                }
            }
        }
    }
}


@Composable
private fun LibraryCounterFilter(
    label: String,
    count: Int,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier =
            modifier
                .fillMaxSize()
                .bouncyClickable(
                    pressedScale =
                        0.95f,

                    onClick =
                        onClick
                ),
        shape =
            RoundedCornerShape(
                17.dp
            ),
        color =
            if (
                selected
            ) {
                MaterialTheme
                    .colorScheme
                    .primary
                    .copy(
                        alpha =
                            0.22f
                    )
            } else {
                Color.Transparent
            }
    ) {
        Column(
            modifier =
                Modifier.fillMaxSize(),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.Center
        ) {
            Text(
                text =
                    label,

                style =
                    MaterialTheme
                        .typography
                        .labelSmall,

                fontWeight =
                    if (
                        selected
                    ) {
                        FontWeight.Black
                    } else {
                        FontWeight.SemiBold
                    },

                maxLines =
                    1
            )

            Spacer(
                modifier =
                    Modifier.height(
                        2.dp
                    )
            )

            Surface(
                shape =
                    CircleShape,
                color =
                    if (
                        selected
                    ) {
                        Color.White.copy(
                            alpha =
                                0.92f
                        )
                    } else {
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant
                    }
            ) {
                Text(
                    text =
                        count.toString(),

                    modifier =
                        Modifier.padding(
                            horizontal =
                                7.dp,

                            vertical =
                                2.dp
                        ),

                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        if (
                            selected
                        ) {
                            MaterialTheme
                                .colorScheme
                                .primary
                        } else {
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                        }
                )
            }
        }
    }
}


@Composable
private fun LibrarySyncIndicator(
    progress: Float
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        2.dp
                ),
        verticalArrangement =
            Arrangement.spacedBy(
                5.dp
            )
    ) {
        LinearProgressIndicator(
            progress = {
                progress.coerceIn(
                    0f,
                    1f
                )
            },
            modifier =
                Modifier.fillMaxWidth()
        )

        Text(
            text =
                "Đang đồng bộ Library...",

            style =
                MaterialTheme
                    .typography
                    .labelSmall,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )
    }
}


@Composable
private fun LibrarySectionHeader(
    mode: LibraryBrowseMode,
    count: Int
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    top =
                        2.dp
                ),
        horizontalArrangement =
            Arrangement.SpaceBetween,
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Text(
            text =
                when (
                    mode
                ) {
                    LibraryBrowseMode.SONGS ->
                        "Bài hát"

                    LibraryBrowseMode.ARTISTS ->
                        "Artist"

                    LibraryBrowseMode.ALBUMS ->
                        "Album"

                    LibraryBrowseMode.YEARS ->
                        "Năm"
                },

            style =
                MaterialTheme
                    .typography
                    .titleMedium,

            fontWeight =
                FontWeight.Black
        )

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


@Composable
private fun LibraryPrototypeSongCard(
    song: IndexedMediaEntity,
    onPlay: () -> Unit,
    onEditTag: () -> Unit
) {
    val issues =
        libraryAttentionLabels(
            song
        )

    val needsAttention =
        issues.isNotEmpty()

    val borderColor =
        if (
            needsAttention
        ) {
            MaterialTheme
                .colorScheme
                .error
                .copy(
                    alpha =
                        0.50f
                )
        } else {
            MaterialTheme
                .colorScheme
                .outline
                .copy(
                    alpha =
                        0.30f
                )
        }

    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                27.dp
            ),
        color =
            MaterialTheme
                .colorScheme
                .surface
                .copy(
                    alpha =
                        0.91f
                ),
        tonalElevation =
            5.dp,
        shadowElevation =
            8.dp,
        border =
            BorderStroke(
                1.dp,
                borderColor
            )
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                BrandBlue.copy(
                                    alpha =
                                        0.08f
                                ),
                                Color.Transparent,
                                if (
                                    needsAttention
                                ) {
                                    MaterialTheme
                                        .colorScheme
                                        .error
                                        .copy(
                                            alpha =
                                                0.06f
                                        )
                                } else {
                                    BrandViolet.copy(
                                        alpha =
                                            0.04f
                                    )
                                }
                            )
                        )
                    )
                    .padding(
                        14.dp
                    )
        ) {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            13.dp
                        ),
                    verticalAlignment =
                        Alignment.Top
                ) {
                    LibraryCover(
                        song =
                            song,

                        size =
                            88.dp
                    )

                    Column(
                        modifier =
                            Modifier.weight(
                                1f
                            ),
                        verticalArrangement =
                            Arrangement.spacedBy(
                                3.dp
                            )
                    ) {
                        Text(
                            text =
                                song.title
                                    .ifBlank {
                                        song.displayName
                                            .removeSuffix(
                                                ".mp3"
                                            )
                                    },

                            style =
                                MaterialTheme
                                    .typography
                                    .titleMedium,

                            fontWeight =
                                FontWeight.Black,

                            maxLines =
                                2,

                            overflow =
                                TextOverflow.Ellipsis
                        )

                        Text(
                            text =
                                song.artist
                                    .ifBlank {
                                        "Chưa có Artist"
                                    },

                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurface
                                    .copy(
                                        alpha =
                                            0.83f
                                    ),

                            maxLines =
                                1,

                            overflow =
                                TextOverflow.Ellipsis
                        )

                        Text(
                            text =
                                "Album: ${
                                    song.album
                                        .ifBlank {
                                            "Chưa phân loại"
                                        }
                                }",

                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant,

                            maxLines =
                                1,

                            overflow =
                                TextOverflow.Ellipsis
                        )

                        if (
                            song.year
                                .isNotBlank()
                        ) {
                            Text(
                                text =
                                    song.year,

                                style =
                                    MaterialTheme
                                        .typography
                                        .bodySmall,

                                color =
                                    BrandCyan
                            )
                        }
                    }
                }

                Text(
                    text =
                        song.displayName,

                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis
                )

                if (
                    needsAttention
                ) {
                    LibraryIssueLine(
                        issues =
                            issues
                    )
                }

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.End,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    LibrarySongAction(
                        text =
                            "Nghe thử",

                        icon =
                            Icons.Rounded.PlayArrow,

                        accent =
                            BrandBlue,

                        onClick =
                            onPlay
                    )

                    if (
                        needsAttention
                    ) {
                        Spacer(
                            modifier =
                                Modifier.width(
                                    8.dp
                                )
                        )

                        LibrarySongAction(
                            text =
                                "Sửa",

                            icon =
                                Icons.Rounded.Edit,

                            accent =
                                MaterialTheme
                                    .colorScheme
                                    .error,

                            onClick =
                                onEditTag
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun LibraryIssueLine(
    issues: List<String>
) {
    val displayed =
        buildString {
            append(
                issues
                    .take(
                        2
                    )
                    .joinToString(
                        " • "
                    )
            )

            if (
                issues.size >
                2
            ) {
                append(
                    " • +${
                        issues.size -
                            2
                    }"
                )
            }
        }

    Row(
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(
                6.dp
            )
    ) {
        Icon(
            imageVector =
                Icons.Rounded.WarningAmber,

            contentDescription =
                null,

            modifier =
                Modifier.size(
                    17.dp
                ),

            tint =
                MaterialTheme
                    .colorScheme
                    .error
        )

        Text(
            text =
                displayed,

            style =
                MaterialTheme
                    .typography
                    .labelMedium,

            fontWeight =
                FontWeight.Bold,

            color =
                MaterialTheme
                    .colorScheme
                    .error,

            maxLines =
                2,

            overflow =
                TextOverflow.Ellipsis
        )
    }
}


@Composable
private fun LibrarySongAction(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier =
            Modifier
                .height(
                    44.dp
                )
                .bouncyClickable(
                    pressedScale =
                        0.92f,

                    onClick =
                        onClick
                ),
        shape =
            RoundedCornerShape(
                15.dp
            ),
        color =
            accent.copy(
                alpha =
                    0.18f
            ),
        tonalElevation =
            5.dp,
        shadowElevation =
            5.dp,
        border =
            BorderStroke(
                1.dp,
                accent.copy(
                    alpha =
                        0.50f
                )
            )
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal =
                        14.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(
                    6.dp
                )
        ) {
            Icon(
                imageVector =
                    icon,

                contentDescription =
                    null,

                modifier =
                    Modifier.size(
                        17.dp
                    ),

                tint =
                    accent
            )

            Text(
                text =
                    text,

                style =
                    MaterialTheme
                        .typography
                        .labelMedium,

                fontWeight =
                    FontWeight.Black,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface
            )
        }
    }
}


@Composable
private fun LibraryGroupCard(
    item: LibraryGroupRowData,
    onClick: () -> Unit
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .bouncyClickable(
                    enabled =
                        item.searchValue
                            .isNotBlank(),

                    pressedScale =
                        0.97f,

                    onClick =
                        onClick
                ),
        shape =
            RoundedCornerShape(
                24.dp
            ),
        color =
            MaterialTheme
                .colorScheme
                .surface
                .copy(
                    alpha =
                        0.90f
                ),
        tonalElevation =
            4.dp,
        shadowElevation =
            6.dp,
        border =
            BorderStroke(
                1.dp,
                MaterialTheme
                    .colorScheme
                    .outline
                    .copy(
                        alpha =
                            0.28f
                    )
            )
    ) {
        Row(
            modifier =
                Modifier.padding(
                    12.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {
            LibraryGroupCover(
                coverPath =
                    item.coverPath
            )

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {
                Text(
                    text =
                        item.label,

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,

                    fontWeight =
                        FontWeight.Black,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            3.dp
                        )
                )

                Text(
                    text =
                        "${item.count} bài",

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }

            Text(
                text =
                    "›",

                style =
                    MaterialTheme
                        .typography
                        .headlineSmall,

                color =
                    BrandCyan
            )
        }
    }
}


@Composable
private fun LibraryGroupCover(
    coverPath: String?
) {
    val shape =
        RoundedCornerShape(
            18.dp
        )

    if (
        !coverPath
            .isNullOrBlank()
    ) {
        AsyncImage(
            model =
                File(
                    coverPath
                ),

            contentDescription =
                null,

            modifier =
                Modifier
                    .size(
                        70.dp
                    )
                    .clip(
                        shape
                    ),

            contentScale =
                ContentScale.Crop
        )
    } else {
        Box(
            modifier =
                Modifier
                    .size(
                        70.dp
                    )
                    .clip(
                        shape
                    )
                    .background(
                        Brush.linearGradient(
                            listOf(
                                BrandBlue,
                                BrandViolet
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
                    Color.White
            )
        }
    }
}


@Composable
private fun LibraryCover(
    song: IndexedMediaEntity,
    size: Dp
) {
    val shape =
        RoundedCornerShape(
            20.dp
        )

    if (
        !song.coverPath
            .isNullOrBlank()
    ) {
        AsyncImage(
            model =
                File(
                    song.coverPath
                        .orEmpty()
                ),

            contentDescription =
                "Cover ${song.title}",

            modifier =
                Modifier
                    .size(
                        size
                    )
                    .clip(
                        shape
                    ),

            contentScale =
                ContentScale.Crop
        )
    } else {
        Box(
            modifier =
                Modifier
                    .size(
                        size
                    )
                    .clip(
                        shape
                    )
                    .background(
                        Brush.linearGradient(
                            listOf(
                                BrandCyan.copy(
                                    alpha =
                                        0.72f
                                ),
                                BrandBlue,
                                BrandViolet
                            )
                        )
                    ),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    "COVER",

                style =
                    MaterialTheme
                        .typography
                        .labelSmall,

                fontWeight =
                    FontWeight.Black,

                color =
                    Color.White.copy(
                        alpha =
                            0.90f
                    )
            )
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
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                26.dp
            ),
        color =
            MaterialTheme
                .colorScheme
                .surface
                .copy(
                    alpha =
                        0.88f
                ),
        tonalElevation =
            4.dp,
        shadowElevation =
            5.dp,
        border =
            BorderStroke(
                1.dp,
                MaterialTheme
                    .colorScheme
                    .outlineVariant
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    18.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {
            Text(
                text =
                    title,

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.Black
            )

            Text(
                text =
                    body,

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            if (
                actionLabel != null &&
                onAction != null
            ) {
                Button(
                    onClick =
                        onAction,

                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    Text(
                        actionLabel
                    )
                }
            }
        }
    }
}


@Composable
private fun LibraryYouTubeCta(
    query: String,
    onClick: () -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                24.dp
            ),
        color =
            BrandBlue.copy(
                alpha =
                    0.11f
            ),
        border =
            BorderStroke(
                1.dp,
                BrandBlue.copy(
                    alpha =
                        0.32f
                )
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    16.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {
            Text(
                text =
                    "Không tìm thấy trong Library?",

                fontWeight =
                    FontWeight.Black
            )

            Text(
                text =
                    "Tìm tiếp \"$query\" trên YouTube.",

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Button(
                onClick =
                    onClick,

                modifier =
                    Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.Search,

                    contentDescription =
                        null
                )

                Spacer(
                    modifier =
                        Modifier.width(
                            7.dp
                        )
                )

                Text(
                    "Tìm trên YouTube"
                )
            }
        }
    }
}


private fun buildGroupRows(
    songs: List<IndexedMediaEntity>,
    mode: LibraryBrowseMode
): List<LibraryGroupRowData> {

    if (
        mode ==
        LibraryBrowseMode.SONGS
    ) {
        return emptyList()
    }

    val groups:
        Map<String, List<IndexedMediaEntity>> =
        when (
            mode
        ) {
            LibraryBrowseMode.ARTISTS ->
                songs.groupBy {
                    it.artist.trim()
                }

            LibraryBrowseMode.ALBUMS ->
                songs.groupBy {
                    it.album.trim()
                }

            LibraryBrowseMode.YEARS ->
                songs.groupBy {
                    it.year.trim()
                }

            LibraryBrowseMode.SONGS ->
                emptyMap()
        }

    val rows =
        groups.map {
            (
                rawValue,
                groupSongs
            ) ->

            val label =
                when {
                    rawValue.isNotBlank() ->
                        rawValue

                    mode ==
                        LibraryBrowseMode.ARTISTS ->
                        "Chưa có Artist"

                    mode ==
                        LibraryBrowseMode.ALBUMS ->
                        "Chưa có Album"

                    else ->
                        "Chưa có năm"
                }

            LibraryGroupRowData(
                label =
                    label,

                searchValue =
                    rawValue,

                count =
                    groupSongs.size,

                coverPath =
                    groupSongs
                        .firstOrNull {
                            !it.coverPath
                                .isNullOrBlank()
                        }
                        ?.coverPath
            )
        }

    return when (
        mode
    ) {
        LibraryBrowseMode.YEARS ->
            rows.sortedByDescending {
                it.searchValue
                    .toIntOrNull()
                    ?: Int.MIN_VALUE
            }

        else ->
            rows.sortedBy {
                it.label.lowercase()
            }
    }
}


private fun libraryAttentionLabels(
    song: IndexedMediaEntity
): List<String> {

    val issues =
        linkedSetOf<String>()

    if (
        MediaMetadataStatus
            .isError(
                song.metadataStatus
            )
    ) {
        issues +=
            "Metadata lỗi"
    }

    val fields =
        song.metadataErrorFields
            .orEmpty()
            .split(',')
            .map {
                it.trim()
            }
            .filter {
                it.isNotBlank()
            }

    fields.forEach {
        field ->

        issues +=
            when (
                field
            ) {
                "fileType" ->
                    "Sai định dạng file"

                "file" ->
                    "File rỗng"

                "metadata" ->
                    "Metadata lỗi"

                "id3Version" ->
                    "ID3 không phải v2.3"

                "title" ->
                    "Thiếu Title"

                "artist" ->
                    "Thiếu Artist"

                "album" ->
                    "Thiếu Album"

                "year" ->
                    "Year không đúng chuẩn"

                "cover" ->
                    "Thiếu cover"

                "filename" ->
                    "Sai chuẩn tên file"

                else ->
                    field
            }
    }

    if (
        song.tagTitle
            .isBlank()
    ) {
        issues +=
            "Thiếu Title"
    }

    if (
        song.tagArtist
            .isBlank()
    ) {
        issues +=
            "Thiếu Artist"
    }

    if (
        song.album
            .isBlank()
    ) {
        issues +=
            "Thiếu Album"
    }

    if (
        song.year.length !=
        4 ||
        song.year.any {
            !it.isDigit()
        }
    ) {
        issues +=
            "Year không đúng chuẩn"
    }

    if (
        song.coverPath
            .isNullOrBlank()
    ) {
        issues +=
            "Thiếu cover"
    }

    return issues.toList()
}


private fun playExternal(
    context: Context,
    song: IndexedMediaEntity
) {
    val uri =
        runCatching {
            Uri.parse(
                song.uri
            )
        }
            .getOrNull()

    if (
        uri == null
    ) {
        Toast.makeText(
            context,
            "Không mở được file nhạc.",
            Toast.LENGTH_SHORT
        ).show()

        return
    }

    val intent =
        Intent(
            Intent.ACTION_VIEW
        ).apply {
            setDataAndType(
                uri,
                song.mimeType
                    .ifBlank {
                        "audio/mpeg"
                    }
            )

            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

    runCatching {
        context.startActivity(
            intent
        )
    }
        .onFailure {
            Toast.makeText(
                context,
                "Không tìm thấy ứng dụng phát nhạc phù hợp.",
                Toast.LENGTH_SHORT
            ).show()
        }
}