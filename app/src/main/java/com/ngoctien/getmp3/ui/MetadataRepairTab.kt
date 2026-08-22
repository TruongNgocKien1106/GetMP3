package com.ngoctien.getmp3.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ngoctien.getmp3.data.IndexedMediaEntity
import com.ngoctien.getmp3.data.MediaIndexSource
import com.ngoctien.getmp3.data.MediaMetadataStatus
import com.ngoctien.getmp3.metadata.ReleaseYearSuggestion
import com.ngoctien.getmp3.viewmodel.MetadataRepairFilter
import com.ngoctien.getmp3.viewmodel.MetadataRepairUiState
import com.ngoctien.getmp3.viewmodel.hasMetadataError
import com.ngoctien.getmp3.viewmodel.hasMissingCover
import com.ngoctien.getmp3.viewmodel.hasMissingTags
import java.io.File

@Composable
fun MetadataRepairTab(
    state: MetadataRepairUiState,

    modifier: Modifier =
        Modifier,

    onRefresh: () -> Unit,

    onFilterChange:
        (MetadataRepairFilter) -> Unit,

    onDownloadReplacement:
        (IndexedMediaEntity) -> Unit,

    onSelect:
        (IndexedMediaEntity) -> Unit,

    onDismissEditor:
        () -> Unit,

    onTitleChange:
        (String) -> Unit,

    onArtistChange:
        (String) -> Unit,

    onAlbumChange:
        (String) -> Unit,

    onYearChange:
        (String) -> Unit,

    onLookupYear:
        () -> Unit,

    onSave:
        () -> Unit
) {

    state.selected
        ?.let { item ->

            MetadataRepairDialog(
                item =
                    item,

                title =
                    state.title,

                artist =
                    state.artist,

                album =
                    state.album,

                year =
                    state.year,

                yearSuggestions =
                    state.yearSuggestions,

                isLookingUpYear =
                    state.isLookingUpYear,

                yearLookupError =
                    state.yearLookupError,

                isSaving =
                    state.isSaving,

                errorMessage =
                    state.errorMessage,

                onDismiss =
                    onDismissEditor,

                onTitleChange =
                    onTitleChange,

                onArtistChange =
                    onArtistChange,

                onAlbumChange =
                    onAlbumChange,

                onYearChange =
                    onYearChange,

                onLookupYear =
                    onLookupYear,

                onSave =
                    onSave
            )
        }


    LazyColumn(
        modifier =
            modifier
                .fillMaxSize(),

        contentPadding =
            PaddingValues(
                start = 12.dp,
                top = 8.dp,
                end = 12.dp,
                bottom = 18.dp
            ),

        verticalArrangement =
            Arrangement.spacedBy(
                10.dp
            )
    ) {

        item {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth(),

                verticalAlignment =
                    Alignment
                        .CenterVertically
            ) {

                Column(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {

                    Text(
                        text =
                            "Cần xử lý",

                        style =
                            MaterialTheme
                                .typography
                                .headlineSmall,

                        fontWeight =
                            FontWeight
                                .ExtraBold
                    )


                    Text(
                        text =
                            "${state.attentionCount} file · dữ liệu lấy trực tiếp từ Media Index",

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


                OutlinedButton(
                    onClick =
                        onRefresh,

                    enabled =
                        !state.isLoading &&
                            !state.isSaving
                ) {

                    if (
                        state.isLoading
                    ) {

                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(
                                    17.dp
                                ),

                            strokeWidth =
                                2.dp
                        )

                    } else {

                        Icon(
                            imageVector =
                                Icons
                                    .Rounded
                                    .Refresh,

                            contentDescription =
                                null,

                            modifier =
                                Modifier.size(
                                    17.dp
                                )
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.width(
                                6.dp
                            )
                    )


                    Text(
                        "Làm mới"
                    )
                }
            }
        }


        item {

            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth(),

                shape =
                    RoundedCornerShape(
                        18.dp
                    ),

                color =
                    MaterialTheme
                        .colorScheme
                        .primaryContainer
                        .copy(
                            alpha =
                                0.55f
                        )
            ) {

                Column(
                    modifier =
                        Modifier.padding(
                            13.dp
                        ),

                    verticalArrangement =
                        Arrangement
                            .spacedBy(
                                5.dp
                            )
                ) {

                    Text(
                        text =
                            "Luồng xử lý",

                        fontWeight =
                            FontWeight.Bold
                    )


                    Text(
                        text =
                            "Thiếu cover → Tải bản thay thế → So trùng → Giữ bản mới.",

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )


                    Text(
                        text =
                            "Thiếu hoặc lỗi Title / Artist / Album / Year → Ghi lại thẻ trực tiếp.",

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )


                    Text(
                        text =
                            "Màn này chỉ query Room DB, không quét lại thư mục.",

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
        }


        item {

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth(),

                horizontalArrangement =
                    Arrangement
                        .spacedBy(
                            7.dp
                        )
            ) {

                RepairCounter(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    value =
                        state
                            .metadataErrorCount,

                    label =
                        "Lỗi metadata"
                )


                RepairCounter(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    value =
                        state
                            .missingCoverCount,

                    label =
                        "Thiếu cover"
                )


                RepairCounter(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    value =
                        state
                            .missingTagCount,

                    label =
                        "Thiếu thẻ"
                )
            }
        }


        item {

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(
                            rememberScrollState()
                        ),

                horizontalArrangement =
                    Arrangement
                        .spacedBy(
                            7.dp
                        )
            ) {

                RepairFilterChip(
                    selected =
                        state.filter ==
                            MetadataRepairFilter.ALL,

                    text =
                        "Tất cả ${state.attentionCount}",

                    onClick = {
                        onFilterChange(
                            MetadataRepairFilter.ALL
                        )
                    }
                )


                RepairFilterChip(
                    selected =
                        state.filter ==
                            MetadataRepairFilter.METADATA,

                    text =
                        "Metadata ${state.metadataErrorCount}",

                    onClick = {
                        onFilterChange(
                            MetadataRepairFilter.METADATA
                        )
                    }
                )


                RepairFilterChip(
                    selected =
                        state.filter ==
                            MetadataRepairFilter.COVER,

                    text =
                        "Thiếu cover ${state.missingCoverCount}",

                    onClick = {
                        onFilterChange(
                            MetadataRepairFilter.COVER
                        )
                    }
                )


                RepairFilterChip(
                    selected =
                        state.filter ==
                            MetadataRepairFilter.TAGS,

                    text =
                        "Thiếu thẻ ${state.missingTagCount}",

                    onClick = {
                        onFilterChange(
                            MetadataRepairFilter.TAGS
                        )
                    }
                )
            }
        }


        if (
            state.isLoading &&
            !state.hasLoaded
        ) {

            item {

                RepairInfoCard(
                    title =
                        "Đang đọc Media Index",

                    message =
                        "Không quét lại toàn Library.",

                    loading =
                        true
                )
            }
        }


        state.errorMessage
            ?.takeIf {
                state.selected ==
                    null
            }
            ?.let { message ->

                item {

                    Surface(
                        modifier =
                            Modifier
                                .fillMaxWidth(),

                        shape =
                            RoundedCornerShape(
                                16.dp
                            ),

                        color =
                            MaterialTheme
                                .colorScheme
                                .errorContainer
                    ) {

                        Row(
                            modifier =
                                Modifier.padding(
                                    12.dp
                                ),

                            verticalAlignment =
                                Alignment
                                    .CenterVertically,

                            horizontalArrangement =
                                Arrangement
                                    .spacedBy(
                                        9.dp
                                    )
                        ) {

                            Icon(
                                imageVector =
                                    Icons
                                        .Rounded
                                        .ErrorOutline,

                                contentDescription =
                                    null
                            )


                            Text(
                                text =
                                    message,

                                style =
                                    MaterialTheme
                                        .typography
                                        .bodySmall
                            )
                        }
                    }
                }
            }


        if (
            state.hasLoaded &&
            !state.isLoading &&
            state.filteredItems
                .isEmpty() &&
            state.errorMessage ==
            null
        ) {

            item {

                RepairInfoCard(
                    title =
                        "Không có mục nào",

                    message =
                        "Không có file cần xử lý trong bộ lọc này.",

                    loading =
                        false
                )
            }
        }


        items(
            items =
                state.filteredItems,

            key =
                IndexedMediaEntity::uri
        ) { item ->

            AttentionMediaCard(
                item =
                    item,

                enabled =
                    !state.isSaving,

                onDownloadReplacement = {
                    onDownloadReplacement(
                        item
                    )
                },

                onEdit = {
                    onSelect(
                        item
                    )
                }
            )
        }
    }
}


@Composable
private fun AttentionMediaCard(
    item: IndexedMediaEntity,

    enabled: Boolean,

    onDownloadReplacement:
        () -> Unit,

    onEdit:
        () -> Unit
) {

    val metadataError =
        item.hasMetadataError()

    val missingCover =
        item.hasMissingCover()

    val missingTags =
        item.hasMissingTags()

    val issueSummary =
        buildList {
            if (
                metadataError
            ) {
                add(
                    when (
                        item.metadataStatus
                    ) {
                        MediaMetadataStatus
                            .BROKEN_METADATA ->
                            "Metadata hỏng"

                        MediaMetadataStatus
                            .UNREADABLE_FILE ->
                            "Không đọc được"

                        else ->
                            "Lỗi metadata"
                    }
                )
            }

            if (
                missingCover
            ) {
                add(
                    "Thiếu cover"
                )
            }

            if (
                item.tagTitle
                    .isBlank()
            ) {
                add(
                    "Thiếu Title"
                )
            }

            if (
                item.tagArtist
                    .isBlank()
            ) {
                add(
                    "Thiếu Artist"
                )
            }

            if (
                item.album
                    .isBlank()
            ) {
                add(
                    "Thiếu Album"
                )
            }
        }
            .joinToString(
                separator =
                    " · "
            )

    val unreadable =
        item.metadataStatus ==
            MediaMetadataStatus
                .UNREADABLE_FILE


    val canEditTags =
        !unreadable &&
            (
                metadataError ||
                    missingTags
                )


    val shouldDownload =
        missingCover ||
            unreadable


    val sourceLabel =
        when (
            item.source
        ) {

            MediaIndexSource.REFERENCE ->
                "Library"

            MediaIndexSource.DOWNLOAD ->
                "Inbox"

            else ->
                item.source
        }


    Surface(
        modifier =
            Modifier
                .fillMaxWidth(),

        shape =
            RoundedCornerShape(
                18.dp
            ),

        tonalElevation =
            2.dp,

        border =
            BorderStroke(
                width =
                    1.dp,

                color =
                    if (
                        metadataError
                    ) {
                        MaterialTheme
                            .colorScheme
                            .error
                            .copy(
                                alpha =
                                    0.30f
                            )
                    } else {
                        MaterialTheme
                            .colorScheme
                            .outlineVariant
                    }
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    11.dp
                ),

            verticalArrangement =
                Arrangement
                    .spacedBy(
                        9.dp
                    )
        ) {

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth(),

                verticalAlignment =
                    Alignment
                        .CenterVertically
            ) {

                CoverPreview(
                    item =
                        item
                )


                Spacer(
                    modifier =
                        Modifier.width(
                            11.dp
                        )
                )


                Column(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    verticalArrangement =
                        Arrangement
                            .spacedBy(
                                3.dp
                            )
                ) {

                    Text(
                        text =
                            item.fileTitle
                                .ifBlank {
                                    item.title
                                }
                                .ifBlank {
                                    item.displayName
                                },

                        maxLines =
                            2,

                        overflow =
                            TextOverflow
                                .Ellipsis,

                        fontWeight =
                            FontWeight
                                .Bold
                    )


                    Text(
                        text =
                            item.fileArtist
                                .ifBlank {
                                    item.artist
                                }
                                .ifBlank {
                                    "Chưa có Artist"
                                },

                        maxLines =
                            1,

                        overflow =
                            TextOverflow
                                .Ellipsis,

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )


                    Text(
                        text =
                            buildString {
                                append(
                                    if (
                                        item.album
                                            .isBlank()
                                    ) {
                                        "Album: —"
                                    } else {
                                        "Album: ${item.album}"
                                    }
                                )

                                append(
                                    " · Year: "
                                )

                                append(
                                    item.year
                                        .ifBlank {
                                            "—"
                                        }
                                )
                            },

                        maxLines =
                            1,

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


                    Text(
                        text =
                            sourceLabel,

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        color =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }
            }


            Text(
                text =
                    "Tình trạng: $issueSummary",

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                fontWeight =
                    FontWeight
                        .SemiBold,

                color =
                    if (
                        metadataError
                    ) {
                        MaterialTheme
                            .colorScheme
                            .error
                    } else {
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                    }
            )

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(
                            rememberScrollState()
                        ),

                horizontalArrangement =
                    Arrangement
                        .spacedBy(
                            6.dp
                        )
            ) {

                if (
                    metadataError
                ) {

                    ReasonBadge(
                        text =
                            when (
                                item.metadataStatus
                            ) {

                                MediaMetadataStatus
                                    .BROKEN_METADATA ->
                                    "Metadata hỏng"

                                MediaMetadataStatus
                                    .UNREADABLE_FILE ->
                                    "Không đọc được"

                                else ->
                                    "Lỗi metadata"
                            },

                        error =
                            true
                    )
                }


                if (
                    missingCover
                ) {

                    ReasonBadge(
                        text =
                            "Thiếu cover"
                    )
                }


                if (
                    item.tagTitle
                        .isBlank()
                ) {

                    ReasonBadge(
                        text =
                            "Thiếu Title"
                    )
                }


                if (
                    item.tagArtist
                        .isBlank()
                ) {

                    ReasonBadge(
                        text =
                            "Thiếu Artist"
                    )
                }


                if (
                    item.album
                        .isBlank()
                ) {

                    ReasonBadge(
                        text =
                            "Thiếu Album"
                    )
                }

                if (
                    item.year
                        .isBlank()
                ) {

                    ReasonBadge(
                        text =
                            "Thiếu Year"
                    )
                }
            }


            if (
                !item.metadataErrorMessage
                    .isNullOrBlank()
            ) {

                Text(
                    text =
                        item.metadataErrorMessage,

                    maxLines =
                        2,

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
                            .error
                )
            }


            if (
                shouldDownload ||
                canEditTags
            ) {

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement
                            .spacedBy(
                                7.dp
                            )
                ) {

                    if (
                        shouldDownload
                    ) {

                        FilledTonalButton(
                            onClick =
                                onDownloadReplacement,

                            enabled =
                                enabled,

                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        ) {

                            Icon(
                                imageVector =
                                    Icons
                                        .Rounded
                                        .Download,

                                contentDescription =
                                    null,

                                modifier =
                                    Modifier.size(
                                        18.dp
                                    )
                            )


                            Spacer(
                                modifier =
                                    Modifier.width(
                                        6.dp
                                    )
                            )


                            Text(
                                if (
                                    unreadable
                                ) {
                                    "Tải bản thay thế"
                                } else {
                                    "Tải bản khác"
                                }
                            )
                        }
                    }


                    if (
                        canEditTags
                    ) {

                        OutlinedButton(
                            onClick =
                                onEdit,

                            enabled =
                                enabled,

                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        ) {

                            Icon(
                                imageVector =
                                    Icons
                                        .Rounded
                                        .Edit,

                                contentDescription =
                                    null,

                                modifier =
                                    Modifier.size(
                                        18.dp
                                    )
                            )


                            Spacer(
                                modifier =
                                    Modifier.width(
                                        6.dp
                                    )
                            )


                            Text(
                                "Ghi lại thẻ"
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun CoverPreview(
    item: IndexedMediaEntity
) {

    val coverPath =
        item.coverPath
            ?.takeIf {
                it.isNotBlank()
            }


    if (
        coverPath !=
        null
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
                        72.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            13.dp
                        )
                    ),

            contentScale =
                ContentScale.Crop
        )

        return
    }


    Box(
        modifier =
            Modifier
                .size(
                    72.dp
                )
                .clip(
                    RoundedCornerShape(
                        13.dp
                    )
                )
                .background(
                    MaterialTheme
                        .colorScheme
                        .surfaceVariant
                ),

        contentAlignment =
            Alignment.Center
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.spacedBy(
                    3.dp
                )
        ) {

            Icon(
                imageVector =
                    Icons
                        .Rounded
                        .LibraryMusic,

                contentDescription =
                    null,

                tint =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )


            Text(
                text =
                    "No cover",

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
}


@Composable
private fun ReasonBadge(
    text: String,

    error: Boolean =
        false
) {

    Surface(
        shape =
            RoundedCornerShape(
                50
            ),

        color =
            if (
                error
            ) {
                MaterialTheme
                    .colorScheme
                    .errorContainer
            } else {
                MaterialTheme
                    .colorScheme
                    .secondaryContainer
            }
    ) {

        Text(
            text =
                text,

            modifier =
                Modifier.padding(
                    horizontal =
                        9.dp,

                    vertical =
                        4.dp
                ),

            style =
                MaterialTheme
                    .typography
                    .labelSmall,

            fontWeight =
                FontWeight
                    .SemiBold
        )
    }
}


@Composable
private fun RepairCounter(
    modifier: Modifier,
    value: Int,
    label: String
) {

    Surface(
        modifier =
            modifier,

        shape =
            RoundedCornerShape(
                14.dp
            ),

        color =
            MaterialTheme
                .colorScheme
                .surfaceVariant
    ) {

        Column(
            modifier =
                Modifier.padding(
                    9.dp
                ),

            horizontalAlignment =
                Alignment
                    .CenterHorizontally
        ) {

            Text(
                text =
                    value.toString(),

                fontWeight =
                    FontWeight
                        .ExtraBold,

                style =
                    MaterialTheme
                        .typography
                        .titleMedium
            )


            Text(
                text =
                    label,

                style =
                    MaterialTheme
                        .typography
                        .labelSmall,

                maxLines =
                    1
            )
        }
    }
}


@Composable
private fun RepairFilterChip(
    selected: Boolean,
    text: String,
    onClick: () -> Unit
) {

    FilterChip(
        selected =
            selected,

        onClick =
            onClick,

        label = {
            Text(
                text
            )
        }
    )
}


@Composable
private fun MetadataRepairDialog(
    item: IndexedMediaEntity,

    title: String,
    artist: String,
    album: String,
    year: String,

    yearSuggestions:
        List<ReleaseYearSuggestion>,

    isLookingUpYear: Boolean,
    yearLookupError: String?,

    isSaving: Boolean,

    errorMessage: String?,

    onDismiss: () -> Unit,

    onTitleChange:
        (String) -> Unit,

    onArtistChange:
        (String) -> Unit,

    onAlbumChange:
        (String) -> Unit,

    onYearChange:
        (String) -> Unit,

    onLookupYear:
        () -> Unit,

    onSave:
        () -> Unit
) {

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) {
                onDismiss()
            }
        },

        icon = {
            Icon(
                imageVector =
                    Icons
                        .Rounded
                        .Edit,

                contentDescription =
                    null
            )
        },

        title = {
            Text(
                "Ghi lại metadata"
            )
        },

        text = {

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(
                            max =
                                430.dp
                        )
                        .verticalScroll(
                            rememberScrollState()
                        ),

                verticalArrangement =
                    Arrangement
                        .spacedBy(
                            10.dp
                        )
            ) {

                Text(
                    text =
                        item.displayName,

                    maxLines =
                        2,

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


                Surface(
                    shape =
                        RoundedCornerShape(
                            12.dp
                        ),

                    color =
                        MaterialTheme
                            .colorScheme
                            .secondaryContainer
                ) {

                    Text(
                        text =
                            "Title / Artist / Album / Year sẽ được ghi đè vào file hiện tại. Cover không thay đổi.",

                        modifier =
                            Modifier.padding(
                                10.dp
                            ),

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )
                }


                OutlinedTextField(
                    value =
                        title,

                    onValueChange =
                        onTitleChange,

                    modifier =
                        Modifier.fillMaxWidth(),

                    enabled =
                        !isSaving,

                    singleLine =
                        true,

                    label = {
                        Text(
                            "Title"
                        )
                    }
                )


                OutlinedTextField(
                    value =
                        artist,

                    onValueChange =
                        onArtistChange,

                    modifier =
                        Modifier.fillMaxWidth(),

                    enabled =
                        !isSaving,

                    singleLine =
                        true,

                    label = {
                        Text(
                            "Artist"
                        )
                    }
                )


                OutlinedTextField(
                    value =
                        album,

                    onValueChange =
                        onAlbumChange,

                    modifier =
                        Modifier.fillMaxWidth(),

                    enabled =
                        !isSaving,

                    singleLine =
                        true,

                    label = {
                        Text(
                            "Album"
                        )
                    }
                )


                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value =
                            year,

                        onValueChange =
                            onYearChange,

                        modifier =
                            Modifier.weight(
                                1f
                            ),

                        enabled =
                            !isSaving,

                        singleLine =
                            true,

                        label = {
                            Text(
                                "Year"
                            )
                        },

                        supportingText = {
                            Text(
                                "4 chữ số · đúng bản thu/version"
                            )
                        }
                    )


                    OutlinedButton(
                        onClick =
                            onLookupYear,

                        enabled =
                            !isSaving &&
                                !isLookingUpYear &&
                                title.isNotBlank() &&
                                artist.isNotBlank(),

                        contentPadding =
                            PaddingValues(
                                horizontal =
                                    12.dp,

                                vertical =
                                    10.dp
                            )
                    ) {
                        if (
                            isLookingUpYear
                        ) {
                            CircularProgressIndicator(
                                modifier =
                                    Modifier.size(
                                        16.dp
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
                            if (
                                isLookingUpYear
                            ) {
                                "Đang tra"
                            }
                            else {
                                "Tra năm"
                            }
                        )
                    }
                }


                yearLookupError
                    ?.takeIf(
                        String::isNotBlank
                    )
                    ?.let { message ->

                        Text(
                            text =
                                message,

                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .error
                        )
                    }


                if (
                    yearSuggestions.isNotEmpty()
                ) {
                    Text(
                        text =
                            "Đề xuất",

                        style =
                            MaterialTheme
                                .typography
                                .labelLarge,

                        fontWeight =
                            FontWeight.SemiBold
                    )


                    yearSuggestions.forEach {
                            suggestion ->

                        ReleaseYearSuggestionButton(
                            suggestion =
                                suggestion,

                            enabled =
                                !isSaving,

                            onSelect = {
                                onYearChange(
                                    suggestion.year
                                )
                            }
                        )
                    }
                }


                errorMessage
                    ?.takeIf(
                        String::isNotBlank
                    )
                    ?.let { message ->

                        Text(
                            text =
                                message,

                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .error
                        )
                    }


                Text(
                    text =
                        "Lưu xong chỉ cập nhật đúng file này. Nếu file Inbox đã đạt chuẩn, app tự đưa sang Library.",

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
        },

        dismissButton = {

            TextButton(
                onClick =
                    onDismiss,

                enabled =
                    !isSaving
            ) {

                Text(
                    "Hủy"
                )
            }
        },

        confirmButton = {

            Button(
                onClick =
                    onSave,

                enabled =
                    !isSaving &&
                        title.isNotBlank() &&
                        artist.isNotBlank()
            ) {

                if (
                    isSaving
                ) {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(
                                17.dp
                            ),

                        strokeWidth =
                            2.dp
                    )


                    Spacer(
                        modifier =
                            Modifier.width(
                                7.dp
                            )
                    )
                }


                Text(
                    if (
                        isSaving
                    ) {
                        "Đang ghi"
                    } else {
                        "Ghi đè thẻ"
                    }
                )
            }
        }
    )
}


@Composable
private fun ReleaseYearSuggestionButton(
    suggestion: ReleaseYearSuggestion,
    enabled: Boolean,
    onSelect: () -> Unit
) {
    OutlinedButton(
        onClick =
            onSelect,

        modifier =
            Modifier.fillMaxWidth(),

        enabled =
            enabled,

        contentPadding =
            PaddingValues(
                horizontal =
                    12.dp,

                vertical =
                    10.dp
            )
    ) {
        Column(
            modifier =
                Modifier.fillMaxWidth(),

            verticalArrangement =
                Arrangement.spacedBy(
                    2.dp
                ),

            horizontalAlignment =
                Alignment.Start
        ) {
            Text(
                text =
                    buildString {
                        append(
                            suggestion.year
                        )

                        suggestion.versionLabel
                            ?.takeIf(
                                String::isNotBlank
                            )
                            ?.let { version ->
                                append(
                                    " · "
                                )

                                append(
                                    version
                                )
                            }
                    },

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,

                fontWeight =
                    FontWeight.SemiBold
            )


            Text(
                text =
                    listOf(
                        suggestion.title,
                        suggestion.artist
                    )
                        .filter(
                            String::isNotBlank
                        )
                        .joinToString(
                            separator =
                                " · "
                        ),

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis,

                style =
                    MaterialTheme
                        .typography
                        .bodySmall
            )


            Text(
                text =
                    listOf(
                        suggestion.releaseDate,
                        suggestion.album
                    )
                        .filter(
                            String::isNotBlank
                        )
                        .joinToString(
                            separator =
                                " · "
                        ),

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis,

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
}


@Composable
private fun RepairInfoCard(
    title: String,
    message: String,
    loading: Boolean
) {

    Surface(
        modifier =
            Modifier
                .fillMaxWidth(),

        shape =
            RoundedCornerShape(24.dp),

        tonalElevation =
            2.dp
    ) {

        Row(
            modifier =
                Modifier.padding(
                    14.dp
                ),

            verticalAlignment =
                Alignment
                    .CenterVertically,

            horizontalArrangement =
                Arrangement
                    .spacedBy(
                        10.dp
                    )
        ) {

            if (
                loading
            ) {

                CircularProgressIndicator(
                    modifier =
                        Modifier.size(
                            20.dp
                        ),

                    strokeWidth =
                        2.dp
                )

            } else {

                Icon(
                    imageVector =
                        Icons
                            .Rounded
                            .CheckCircle,

                    contentDescription =
                        null,

                    tint =
                        MaterialTheme
                            .colorScheme
                            .primary
                )
            }


            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {

                Text(
                    text =
                        title,

                    fontWeight =
                        FontWeight
                            .Bold
                )


                Text(
                    text =
                        message,

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
        }
    }
}
