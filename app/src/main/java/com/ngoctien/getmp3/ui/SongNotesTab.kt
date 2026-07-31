package com.ngoctien.getmp3.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ngoctien.getmp3.note.SongNameMatcher
import com.ngoctien.getmp3.note.data.DuplicateSongMatch
import com.ngoctien.getmp3.note.data.DuplicateSource
import com.ngoctien.getmp3.note.data.SongNoteEntity
import com.ngoctien.getmp3.ui.theme.BrandBlue
import com.ngoctien.getmp3.ui.theme.BrandCyan
import com.ngoctien.getmp3.ui.theme.BrandViolet
import com.ngoctien.getmp3.viewmodel.SongNoteFilter
import com.ngoctien.getmp3.viewmodel.SongNoteUiState
import com.ngoctien.getmp3.viewmodel.SongNoteViewModel
import kotlinx.coroutines.launch

@Composable
fun SongNotesTab(
    viewModel: SongNoteViewModel,

    modifier: Modifier = Modifier,

    onPasteAndDownload:
        (CharSequence?) -> Unit
) {
    /*
     * Đọc Context trong phạm vi Composable.
     * Các callback bên dưới chỉ sử dụng lại biến này.
     */
    val context =
        LocalContext.current
val state by
        viewModel.uiState
            .collectAsStateWithLifecycle()

    val snackbarHostState =
        remember {
            SnackbarHostState()
        }

    val scope =
        rememberCoroutineScope()

    val clipboard =
        LocalClipboardManager.current

    var showClearCompletedDialog by
        remember {
            mutableStateOf(false)
        }


    var searchNote by
        remember {
            mutableStateOf<SongNoteEntity?>(
                null
            )
        }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            snackbarHostState
                .showSnackbar(
                    event.message
                )
        }
    }

    searchNote?.let { note ->
        YouTubeSearchSheet(
            initialQuery =
                buildSearchQuery(
                    note
                ),

            onDismiss = {
                searchNote = null
            },

            onDownload = { result ->
                searchNote = null

                onPasteAndDownload(
                    result.webpageUrl
                )

                scope.launch {
                    snackbarHostState
                        .showSnackbar(
                            "Đã đưa video vào hàng đợi tải"
                        )
                }
            }
        )
    }
    if (state.showDuplicateWarning) {
        DuplicateWarningDialog(
            matches =
                state.duplicateMatches,

            onConfirm =
                viewModel::
                    confirmAddDespiteDuplicate,

            onDismiss =
                viewModel::
                    cancelDuplicateWarning
        )
    }

    if (showClearCompletedDialog) {
        AlertDialog(
            onDismissRequest = {
                showClearCompletedDialog =
                    false
            },

            icon = {
                Icon(
                    imageVector =
                        Icons.Rounded
                            .DeleteSweep,

                    contentDescription =
                        null
                )
            },

            title = {
                Text(
                    "Xóa note đã tải?"
                )
            },

            text = {
                Text(
                    "Sẽ xóa ${state.completedCount} note đã được đánh dấu hoàn tất. File MP3 không bị xóa."
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showClearCompletedDialog =
                            false

                        viewModel
                            .deleteCompletedNotes()
                    }
                ) {
                    Text(
                        text = "Xóa",

                        color =
                            MaterialTheme
                                .colorScheme
                                .error
                    )
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showClearCompletedDialog =
                            false
                    }
                ) {
                    Text("Hủy")
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),

            contentPadding =
                PaddingValues(
                    start = 18.dp,
                    top = 18.dp,
                    end = 18.dp,
                    bottom = 28.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    14.dp
                )
        ) {
            item {
                SongNotesHeader(
                    total =
                        state.notes.size,

                    completed =
                        state.completedCount,

                    onClearCompleted = {
                        showClearCompletedDialog =
                            true
                    }
                )
            }

            item {
                QuickAddNote(
                    state = state,

                    onInputChange =
                        viewModel::setInput,

                    onAdd =
                        viewModel::addNote
                )
            }

            item {
                NoteFilters(
                    selected =
                        state.filter,

                    onSelected =
                        viewModel::setFilter
                )
            }

            if (
                state.visibleNotes
                    .isEmpty()
            ) {
                item {
                    EmptyNotesPanel(
                        hasAnyNote =
                            state.notes
                                .isNotEmpty()
                    )
                }
            } else {
                items(
                    items =
                        state.visibleNotes,

                    key = {
                        it.id
                    }
                ) { note ->
                    SongNoteItem(
                        note = note,

                        onToggleCompleted = {
                            viewModel
                                .toggleCompleted(
                                    note
                                )
                        },

                        onToggleImportant = {
                            viewModel
                                .toggleImportant(
                                    note
                                )
                        },

                        onDelete = {
                            viewModel
                                .deleteNote(
                                    note
                                )
                        },

                        onSearchYouTube = {
                            searchNote = note
                        },

                        onPasteAndDownload = {
                            val link =
                                note.youtubeUrl
                                    ?: clipboard
                                        .getText()
                                        ?.text
                                        ?.toString()
                                        ?.trim()

                            if (
                                link.isNullOrBlank()
                            ) {
                                scope.launch {
                                    snackbarHostState
                                        .showSnackbar(
                                            "Clipboard chưa có liên kết YouTube"
                                        )
                                }

                                return@SongNoteItem
                            }

                            if (
                                !SongNameMatcher
                                    .isYoutubeUrl(
                                        link
                                    )
                            ) {
                                scope.launch {
                                    snackbarHostState
                                        .showSnackbar(
                                            "Clipboard không phải liên kết YouTube"
                                        )
                                }

                                return@SongNoteItem
                            }

                            onPasteAndDownload(
                                link
                            )

                            scope.launch {
                                snackbarHostState
                                    .showSnackbar(
                                        "Đã đưa liên kết vào hàng đợi tải"
                                    )
                            }
                        }
                    )
                }
            }
        }

        SnackbarHost(
            hostState =
                snackbarHostState,

            modifier = Modifier
                .align(
                    Alignment.BottomCenter
                )
                .padding(12.dp)
        )
    }
}

@Composable
private fun SongNotesHeader(
    total: Int,
    completed: Int,
    onClearCompleted: () -> Unit
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(
                    brush =
                        Brush.linearGradient(
                            listOf(
                                BrandBlue,
                                BrandCyan,
                                BrandViolet
                            )
                        ),

                    shape =
                        RoundedCornerShape(
                            18.dp
                        )
                ),

            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.List,

                contentDescription =
                    null,

                tint = Color.White,

                modifier =
                    Modifier.size(28.dp)
            )
        }

        Spacer(
            modifier =
                Modifier.width(13.dp)
        )

        Column(
            modifier =
                Modifier.weight(1f)
        ) {
            Text(
                text = "Chờ tải",

                style =
                    MaterialTheme
                        .typography
                        .headlineMedium,

                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                text =
                    "$total bài • $completed đã tải",

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }

        TextButton(
            onClick =
                onClearCompleted,

            enabled =
                completed > 0
        ) {
            Icon(
                imageVector =
                    Icons.Rounded
                        .DeleteSweep,

                contentDescription =
                    null,

                modifier =
                    Modifier.size(19.dp)
            )

            Spacer(
                modifier =
                    Modifier.width(5.dp)
            )

            Text("Xóa đã tải")
        }
    }
}

@Composable
private fun QuickAddNote(
    state: SongNoteUiState,
    onInputChange: (String) -> Unit,
    onAdd: () -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(22.dp),

        color =
            MaterialTheme
                .colorScheme
                .surface,

        border =
            BorderStroke(
                width = 1.dp,

                color =
                    MaterialTheme
                        .colorScheme
                        .outline
                        .copy(
                            alpha = 0.32f
                        )
            )
    ) {
        OutlinedTextField(
            value =
                state.input,

            onValueChange =
                onInputChange,

            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),

            enabled =
                !state.isAdding,

            singleLine = true,

            label = {
                Text(
                    "Tên bài hát - Artist"
                )
            },

            placeholder = {
                Text(
                    "Ví dụ: Thuộc Nơi Anh - Sơn Tùng M-TP"
                )
            },

            leadingIcon = {
                Icon(
                    imageVector =
                        Icons.Rounded
                            .MusicNote,

                    contentDescription =
                        null
                )
            },

            trailingIcon = {
                if (state.isAdding) {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(22.dp),

                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = onAdd,

                        enabled =
                            state.input
                                .isNotBlank()
                    ) {
                        Icon(
                            imageVector =
                                Icons.Rounded.Add,

                            contentDescription =
                                "Thêm note"
                        )
                    }
                }
            },

            keyboardOptions =
                KeyboardOptions(
                    imeAction =
                        ImeAction.Done
                ),

            keyboardActions =
                KeyboardActions(
                    onDone = {
                        onAdd()
                    }
                )
        )
    }
}

@Composable
private fun NoteFilters(
    selected: SongNoteFilter,
    onSelected: (SongNoteFilter) -> Unit
) {
    Row(
        horizontalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected =
                selected ==
                    SongNoteFilter.ALL,

            onClick = {
                onSelected(
                    SongNoteFilter.ALL
                )
            },

            label = {
                Text("Tất cả")
            }
        )

        FilterChip(
            selected =
                selected ==
                    SongNoteFilter.PENDING,

            onClick = {
                onSelected(
                    SongNoteFilter.PENDING
                )
            },

            label = {
                Text("Chưa tải")
            }
        )

        FilterChip(
            selected =
                selected ==
                    SongNoteFilter.COMPLETED,

            onClick = {
                onSelected(
                    SongNoteFilter.COMPLETED
                )
            },

            label = {
                Text("Đã tải")
            }
        )
    }
}

@Composable
private fun SongNoteItem(
    note: SongNoteEntity,
    onToggleCompleted: () -> Unit,
    onToggleImportant: () -> Unit,
    onDelete: () -> Unit,
    onSearchYouTube: () -> Unit,
    onPasteAndDownload: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(
                if (note.isCompleted) {
                    0.72f
                } else {
                    1f
                }
            ),

        shape =
            RoundedCornerShape(20.dp),

        color =
            MaterialTheme
                .colorScheme
                .surface,

        border =
            BorderStroke(
                width = 1.dp,

                color =
                    MaterialTheme
                        .colorScheme
                        .outline
                        .copy(
                            alpha = 0.28f
                        )
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 10.dp
                )
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Checkbox(
                    checked =
                        note.isCompleted,

                    onCheckedChange = {
                        onToggleCompleted()
                    }
                )

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {
                    Text(
                        text =
                            note.title,

                        maxLines = 2,

                        overflow =
                            TextOverflow
                                .Ellipsis,

                        fontWeight =
                            FontWeight.SemiBold,

                        textDecoration =
                            if (
                                note.isCompleted
                            ) {
                                TextDecoration
                                    .LineThrough
                            } else {
                                TextDecoration
                                    .None
                            }
                    )

                    if (
                        note.artist
                            .isNotBlank()
                    ) {
                        Spacer(
                            modifier =
                                Modifier.height(
                                    3.dp
                                )
                        )

                        Text(
                            text =
                                note.artist,

                            maxLines = 1,

                            overflow =
                                TextOverflow
                                    .Ellipsis,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant,

                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall
                        )
                    }
                }

                IconButton(
                    onClick =
                        onToggleImportant
                ) {
                    Icon(
                        imageVector =
                            if (
                                note.isImportant
                            ) {
                                Icons.Rounded
                                    .Star
                            } else {
                                Icons.Rounded
                                    .StarBorder
                            },

                        contentDescription =
                            "Quan trọng",

                        tint =
                            if (
                                note.isImportant
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

            HorizontalDivider(
                modifier =
                    Modifier.padding(
                        start = 48.dp
                    ),

                color =
                    MaterialTheme
                        .colorScheme
                        .outline
                        .copy(
                            alpha = 0.18f
                        )
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                TextButton(
                    onClick =
                        onSearchYouTube
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded
                                .OpenInNew,

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(18.dp)
                    )

                    Spacer(
                        modifier =
                            Modifier.width(5.dp)
                    )

                    Text("Tìm YouTube")
                }

                TextButton(
                    onClick =
                        onPasteAndDownload
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded
                                .Download,

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(18.dp)
                    )

                    Spacer(
                        modifier =
                            Modifier.width(5.dp)
                    )

                    Text(
                        if (
                            note.youtubeUrl !=
                            null
                        ) {
                            "Tải link"
                        } else {
                            "Dán & tải"
                        }
                    )
                }

                Spacer(
                    modifier =
                        Modifier.weight(1f)
                )

                IconButton(
                    onClick =
                        onDelete
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded
                                .Delete,

                        contentDescription =
                            "Xóa note",

                        tint =
                            MaterialTheme
                                .colorScheme
                                .error
                    )
                }
            }
        }
    }
}

@Composable
private fun DuplicateWarningDialog(
    matches: List<DuplicateSongMatch>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest =
            onDismiss,

        icon = {
            Icon(
                imageVector =
                    Icons.Rounded
                        .Search,

                contentDescription =
                    null
            )
        },

        title = {
            Text(
                "Có thể đã tồn tại"
            )
        },

        text = {
            Column {
                Text(
                    text =
                        "Tên bài vừa nhập giống hoặc gần giống với các mục sau:",

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                Column(
                    modifier = Modifier
                        .heightIn(
                            max = 320.dp
                        )
                        .verticalScroll(
                            rememberScrollState()
                        )
                ) {
                    matches.forEachIndexed {
                            index,
                            match ->

                        DuplicateMatchRow(
                            match = match
                        )

                        if (
                            index !=
                            matches.lastIndex
                        ) {
                            HorizontalDivider(
                                modifier =
                                    Modifier.padding(
                                        vertical =
                                            8.dp
                                    )
                            )
                        }
                    }
                }
            }
        },

        confirmButton = {
            TextButton(
                onClick =
                    onConfirm
            ) {
                Text("Vẫn thêm")
            }
        },

        dismissButton = {
            TextButton(
                onClick =
                    onDismiss
            ) {
                Text("Hủy")
            }
        }
    )
}

@Composable
private fun DuplicateMatchRow(
    match: DuplicateSongMatch
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.Top
    ) {
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
                Modifier.size(21.dp)
        )

        Spacer(
            modifier =
                Modifier.width(10.dp)
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

                fontWeight =
                    FontWeight.SemiBold
            )

            if (
                match.artist
                    .isNotBlank()
            ) {
                Text(
                    text =
                        match.artist,

                    maxLines = 1,

                    overflow =
                        TextOverflow.Ellipsis,

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
                    when (match.source) {
                        DuplicateSource
                            .NOTE_LIST -> {
                            "Đã có trong Chờ tải"
                        }

                        DuplicateSource
                            .COMPARE_FOLDER -> {
                            "Có trong thư mục đối chiếu"
                        }
                    },

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
                    "${(match.score * 100).toInt()}%",

                modifier =
                    Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    ),

                style =
                    MaterialTheme
                        .typography
                        .labelSmall
            )
        }
    }
}

@Composable
private fun EmptyNotesPanel(
    hasAnyNote: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp),

        contentAlignment =
            Alignment.Center
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.List,

                contentDescription =
                    null,

                modifier =
                    Modifier.size(42.dp),

                tint =
                    MaterialTheme
                        .colorScheme
                        .primary
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            Text(
                text =
                    if (hasAnyNote) {
                        "Không có bài phù hợp bộ lọc"
                    } else {
                        "Chưa có bài nào trong Chờ tải"
                    },

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

private fun buildSearchQuery(
    note: SongNoteEntity
): String {
    return buildString {
        append(note.title)

        if (
            note.artist.isNotBlank()
        ) {
            append(' ')
            append(note.artist)
        }
    }.trim()
}

private fun openYouTubeSearch(
    query: String,
    context: android.content.Context
) {
    val encoded =
        Uri.encode(query)

    val uri =
        Uri.parse(
            "https://www.youtube.com/results?search_query=$encoded"
        )

    val intent =
        Intent(
            Intent.ACTION_VIEW,
            uri
        )

    try {
        context.startActivity(
            intent
        )
    } catch (
        _: ActivityNotFoundException
    ) {
        // Thiết bị không có trình duyệt phù hợp.
    }
}