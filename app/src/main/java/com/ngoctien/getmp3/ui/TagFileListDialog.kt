package com.ngoctien.getmp3.ui

import com.ngoctien.getmp3.ui.design.UiStyle
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.ngoctien.getmp3.data.DownloadJobEntity
import com.ngoctien.getmp3.model.DownloadStatus
import com.ngoctien.getmp3.settings.AppSettings
import com.ngoctien.getmp3.settings.AppThemeMode
import com.ngoctien.getmp3.tag.MediaSongFile
import com.ngoctien.getmp3.tag.Mp3ListMetadata
import com.ngoctien.getmp3.tag.Mp3ListMetadataReader
import com.ngoctien.getmp3.ui.theme.BrandBlue
import com.ngoctien.getmp3.ui.theme.BrandCyan
import com.ngoctien.getmp3.ui.theme.BrandPink
import com.ngoctien.getmp3.ui.theme.BrandSky
import com.ngoctien.getmp3.ui.theme.BrandViolet
import com.ngoctien.getmp3.viewmodel.ArtistCaseMode
import com.ngoctien.getmp3.viewmodel.CompareIndexUiState
import com.ngoctien.getmp3.viewmodel.CompareViewModel
import com.ngoctien.getmp3.viewmodel.DownloadScreenUiState
import com.ngoctien.getmp3.viewmodel.DownloadViewModel
import com.ngoctien.getmp3.viewmodel.FfmpegReadyState
import com.ngoctien.getmp3.viewmodel.LyricsViewModel
import com.ngoctien.getmp3.viewmodel.MetadataRepairViewModel
import com.ngoctien.getmp3.viewmodel.SettingsViewModel
import com.ngoctien.getmp3.viewmodel.SearchDownloadSection
import com.ngoctien.getmp3.viewmodel.TagEditorUiState
import com.ngoctien.getmp3.viewmodel.TagEditorViewModel
import com.ngoctien.getmp3.viewmodel.YouTubeSearchViewModel
import java.io.File

@Composable
internal fun FileListDialog(
    files: List<MediaSongFile>,
    selectedIndex: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    val context =
        LocalContext.current

    val safeSelectedIndex =
        if (files.isEmpty()) {
            0
        } else {
            selectedIndex.coerceIn(
                0,
                files.lastIndex
            )
        }

    val listState =
        rememberLazyListState(
            initialFirstVisibleItemIndex =
                safeSelectedIndex
        )

    var searchQuery by
        rememberSaveable {
            mutableStateOf("")
        }

    /*
     * null = tất cả
     * ""   = chưa có Album
     * khác = tên Album cần lọc
     */
    var selectedAlbumFilter by
        rememberSaveable {
            mutableStateOf<String?>(
                null
            )
        }

    val metadataByKey =
        remember {
            mutableStateMapOf<
                String,
                Mp3ListMetadata
            >()
        }

    /*
     * Hiện Title / Artist ngay từ filename.
     *
     * Album được đọc nền từng file từ content URI.
     * Không copy toàn bộ MP3 vào cache.
     */
    LaunchedEffect(files) {

        val activeKeys =
            files
                .map {
                    Mp3ListMetadataReader
                        .cacheKey(it)
                }
                .toSet()

        metadataByKey
            .keys
            .toList()
            .filterNot {
                it in activeKeys
            }
            .forEach {
                metadataByKey.remove(it)
            }

        files.forEach { file ->

            val key =
                Mp3ListMetadataReader
                    .cacheKey(file)

            if (
                metadataByKey[key] == null
            ) {
                metadataByKey[key] =
                    Mp3ListMetadataReader
                        .fromFileName(file)
            }
        }

        /*
         * Đọc tuần tự để tránh mở quá nhiều
         * MediaMetadataRetriever cùng lúc.
         */
        files.forEach { file ->

            val key =
                Mp3ListMetadataReader
                    .cacheKey(file)

            metadataByKey[key] =
                Mp3ListMetadataReader
                    .load(
                        context = context,
                        file = file
                    )
        }
    }

    val query =
        searchQuery.trim()

    /*
     * Album filter được tạo từ metadata thật của các file
     * trong thư mục hiện tại.
     */
    val availableAlbums =
        metadataByKey
            .values
            .asSequence()
            .filter {
                it.isAlbumLoaded
            }
            .map {
                it.album.trim()
            }
            .filter {
                it.isNotBlank()
            }
            .distinctBy {
                it.lowercase()
            }
            .sortedBy {
                it.lowercase()
            }
            .toList()

    val missingAlbumCount =
        metadataByKey
            .values
            .count {
                it.isAlbumLoaded &&
                    it.album.isBlank()
            }

    val filteredFiles =
        files
            .mapIndexed {
                    index,
                    file ->

                index to file
            }
            .filter {
                    (_, file) ->

                val key =
                    Mp3ListMetadataReader
                        .cacheKey(file)

                val metadata =
                    metadataByKey[key]
                        ?: Mp3ListMetadataReader
                            .fromFileName(file)

                val matchesQuery =
                    query.isBlank() ||
                        file.displayName.contains(
                            query,
                            ignoreCase = true
                        ) ||
                        metadata.title.contains(
                            query,
                            ignoreCase = true
                        ) ||
                        metadata.artist.contains(
                            query,
                            ignoreCase = true
                        ) ||
                        metadata.album.contains(
                            query,
                            ignoreCase = true
                        )

                val matchesAlbumFilter =
                    when (
                        val albumFilter =
                            selectedAlbumFilter
                    ) {
                        null ->
                            true

                        "" ->
                            metadata.isAlbumLoaded &&
                                metadata.album.isBlank()

                        else ->
                            metadata.isAlbumLoaded &&
                                metadata.album.equals(
                                    albumFilter,
                                    ignoreCase = true
                                )
                    }

                matchesQuery &&
                    matchesAlbumFilter
            }

    val loadedMetadataCount =
        files.count { file ->

            val key =
                Mp3ListMetadataReader
                    .cacheKey(file)

            metadataByKey[key]
                ?.isAlbumLoaded ==
                true
        }

    Dialog(
        onDismissRequest =
            onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(
                    max = 680.dp
                ),

            shape =
                RoundedCornerShape(
                    26.dp
                ),

            color =
                MaterialTheme
                    .colorScheme
                    .surface
        ) {
            Column {

                /*
                 * Header
                 */
                Row(
                    modifier =
                        Modifier.padding(
                            start = 18.dp,
                            top = 16.dp,
                            end = 18.dp,
                            bottom = 10.dp
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
                                "Danh sách MP3",

                            style =
                                MaterialTheme
                                    .typography
                                    .titleLarge,

                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            text =
                                if (
                                    filteredFiles.size ==
                                    files.size
                                ) {
                                    "${files.size} file"
                                } else {
                                    "${filteredFiles.size} / ${files.size} file"
                                },

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

                    Surface(
                        shape =
                            CircleShape,

                        color =
                            MaterialTheme
                                .colorScheme
                                .primaryContainer
                    ) {
                        Text(
                            text =
                                filteredFiles
                                    .size
                                    .toString(),

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onPrimaryContainer,

                            fontWeight =
                                FontWeight.Bold,

                            modifier =
                                Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 4.dp
                                )
                        )
                    }
                }


                /*
                 * Search
                 */
                StableOutlinedTextField(
                    value =
                        searchQuery,

                    onValueChange = {
                        searchQuery = it
                    },

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal =
                                    14.dp
                            ),

                    singleLine = true,

                    label = {
                        Text(
                            "Tìm Title, Artist, Album"
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
                            searchQuery.isNotEmpty()
                        ) {
                            IconButton(
                                onClick = {
                                    searchQuery = ""
                                }
                            ) {
                                Icon(
                                    imageVector =
                                        Icons.Rounded.Clear,

                                    contentDescription =
                                        "Xóa tìm kiếm"
                                )
                            }
                        }
                    }
                )


                /*
                 * Album filters.
                 *
                 * Cuộn ngang vì số Album có thể nhiều.
                 */
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(
                                rememberScrollState()
                            )
                            .padding(
                                horizontal = 14.dp,
                                vertical = 6.dp
                            ),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    FilterChip(
                        selected =
                            selectedAlbumFilter ==
                                null,

                        onClick = {
                            selectedAlbumFilter =
                                null
                        },

                        label = {
                            Text(
                                "Tất cả ${files.size}"
                            )
                        }
                    )

                    FilterChip(
                        selected =
                            selectedAlbumFilter ==
                                "",

                        onClick = {
                            selectedAlbumFilter =
                                ""
                        },

                        label = {
                            Text(
                                "Chưa có $missingAlbumCount"
                            )
                        }
                    )

                    availableAlbums
                        .forEach { album ->

                            val albumCount =
                                metadataByKey
                                    .values
                                    .count {
                                        it.isAlbumLoaded &&
                                            it.album.equals(
                                                album,
                                                ignoreCase =
                                                    true
                                            )
                                    }

                            FilterChip(
                                selected =
                                    selectedAlbumFilter
                                        ?.equals(
                                            album,
                                            ignoreCase =
                                                true
                                        ) ==
                                        true,

                                onClick = {
                                    selectedAlbumFilter =
                                        album
                                },

                                label = {
                                    Text(
                                        "$album $albumCount"
                                    )
                                }
                            )
                        }
                }

                /*
                 * Trạng thái đọc metadata.
                 */
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 6.dp
                            ),

                    horizontalArrangement =
                        Arrangement.End,

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    if (
                        loadedMetadataCount <
                        files.size
                    ) {
                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(
                                    14.dp
                                ),

                            strokeWidth =
                                2.dp
                        )

                        Spacer(
                            modifier =
                                Modifier.width(
                                    6.dp
                                )
                        )
                    }

                    Text(
                        text =
                            "$loadedMetadataCount/${files.size} đã đọc",

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

                HorizontalDivider()


                if (
                    filteredFiles.isEmpty()
                ) {

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical =
                                        36.dp,
                                    horizontal =
                                        20.dp
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            text =
                                if (
                                    selectedAlbumFilter !=
                                    null
                                ) {
                                    "Không có file thuộc Album này"
                                } else {
                                    "Không tìm thấy bài hát"
                                },

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }

                } else {

                    LazyColumn(
                        state =
                            listState,

                        modifier =
                            Modifier.heightIn(
                                max = 530.dp
                            )
                    ) {

                        itemsIndexed(
                            items =
                                filteredFiles,

                            key = {
                                    _,
                                    entry ->

                                val file =
                                    entry.second

                                "${file.uri}|${file.dateModifiedSeconds}"
                            }
                        ) {
                                filteredIndex,
                                entry ->

                            val originalIndex =
                                entry.first

                            val file =
                                entry.second

                            val isSelected =
                                originalIndex ==
                                    selectedIndex

                            val key =
                                Mp3ListMetadataReader
                                    .cacheKey(
                                        file
                                    )

                            val metadata =
                                metadataByKey[key]
                                    ?: Mp3ListMetadataReader
                                        .fromFileName(
                                            file
                                        )

                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .bouncyClickable {
                                            onSelect(
                                                originalIndex
                                            )
                                        }
                                        .background(
                                            if (
                                                isSelected
                                            ) {
                                                MaterialTheme
                                                    .colorScheme
                                                    .primaryContainer
                                            } else {
                                                Color.Transparent
                                            }
                                        )
                                        .padding(
                                            horizontal =
                                                14.dp,
                                            vertical =
                                                10.dp
                                        ),

                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                Mp3CoverThumbnail(
                                    file =
                                        file,

                                    size =
                                        58.dp,

                                    cornerRadius =
                                        14.dp
                                )

                                Spacer(
                                    modifier =
                                        Modifier.width(
                                            12.dp
                                        )
                                )

                                Column(
                                    modifier =
                                        Modifier.weight(
                                            1f
                                        )
                                ) {

                                    /*
                                     * Title
                                     */
                                    Text(
                                        text =
                                            metadata.title,

                                        maxLines = 1,

                                        overflow =
                                            TextOverflow
                                                .Ellipsis,

                                        style =
                                            MaterialTheme
                                                .typography
                                                .bodyLarge,

                                        fontWeight =
                                            FontWeight
                                                .SemiBold,

                                        color =
                                            if (
                                                isSelected
                                            ) {
                                                MaterialTheme
                                                    .colorScheme
                                                    .onPrimaryContainer
                                            } else {
                                                MaterialTheme
                                                    .colorScheme
                                                    .onSurface
                                            }
                                    )


                                    /*
                                     * Artist
                                     */
                                    Text(
                                        text =
                                            "Artist: ${
                                                metadata.artist
                                                    .ifBlank {
                                                        "Chưa xác định"
                                                    }
                                            }",

                                        maxLines = 1,

                                        overflow =
                                            TextOverflow
                                                .Ellipsis,

                                        style =
                                            MaterialTheme
                                                .typography
                                                .bodySmall,

                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .onSurfaceVariant
                                    )


                                    /*
                                     * Album
                                     */
                                    val albumText =
                                        when {
                                            !metadata
                                                .isAlbumLoaded ->

                                                "Đang đọc..."

                                            metadata.album
                                                .isBlank() ->

                                                "Chưa có Album"

                                            else ->
                                                metadata.album
                                        }

                                    Text(
                                        text =
                                            "Album: $albumText",

                                        maxLines = 1,

                                        overflow =
                                            TextOverflow
                                                .Ellipsis,

                                        style =
                                            MaterialTheme
                                                .typography
                                                .bodySmall,

                                        color =
                                            if (
                                                metadata
                                                    .isAlbumLoaded &&
                                                metadata.album
                                                    .isBlank()
                                            ) {
                                                MaterialTheme
                                                    .colorScheme
                                                    .error
                                            } else {
                                                MaterialTheme
                                                    .colorScheme
                                                    .primary
                                            }
                                    )


                                    /*
                                     * Filename để đối chiếu.
                                     */
                                    Text(
                                        text =
                                            file.displayName,

                                        maxLines = 1,

                                        overflow =
                                            TextOverflow
                                                .Ellipsis,

                                        style =
                                            MaterialTheme
                                                .typography
                                                .labelSmall,

                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .onSurfaceVariant
                                                .copy(
                                                    alpha =
                                                        0.72f
                                                )
                                    )
                                }

                                if (
                                    isSelected
                                ) {
                                    Spacer(
                                        modifier =
                                            Modifier.width(
                                                8.dp
                                            )
                                    )

                                    Icon(
                                        imageVector =
                                            Icons.Rounded
                                                .Check,

                                        contentDescription =
                                            "Đang chọn",

                                        tint =
                                            MaterialTheme
                                                .colorScheme
                                                .primary
                                    )
                                }
                            }

                            if (
                                filteredIndex <
                                filteredFiles
                                    .lastIndex
                            ) {
                                HorizontalDivider(
                                    modifier =
                                        Modifier.padding(
                                            start =
                                                84.dp
                                        ),

                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .outlineVariant
                                            .copy(
                                                alpha =
                                                    0.45f
                                            )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
