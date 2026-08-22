package com.ngoctien.getmp3.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ngoctien.getmp3.compare.CompareFile
import com.ngoctien.getmp3.compare.CompareMatchKind
import com.ngoctien.getmp3.compare.ComparePair
import com.ngoctien.getmp3.compare.CompareSide
import com.ngoctien.getmp3.data.IndexedMediaEntity
import com.ngoctien.getmp3.viewmodel.CompareUiState
import com.ngoctien.getmp3.viewmodel.MetadataRepairFilter
import com.ngoctien.getmp3.viewmodel.MetadataRepairUiState
import java.io.File
import kotlin.math.roundToInt

private enum class PendingCompareAction {
    KEEP_CURRENT,
    KEEP_REFERENCE
}

private enum class CompareSection {
    DUPLICATES,
    METADATA_ERRORS
}

@Composable
fun CompareTab(
    state: CompareUiState,
    repairState: MetadataRepairUiState,
    modifier: Modifier = Modifier,

    onRefresh: () -> Unit,

    onEnsureRepair: () -> Unit,
    onRefreshRepair: () -> Unit,

    onRepairFilterChange:
        (MetadataRepairFilter) -> Unit,

    onDownloadRepairReplacement:
        (IndexedMediaEntity) -> Unit,

    onRepairYearChange:
        (String) -> Unit,

    onLookupRepairYear:
        () -> Unit,

    onSelectRepair:
        (IndexedMediaEntity) -> Unit,

    onDismissRepair: () -> Unit,

    onRepairTitleChange:
        (String) -> Unit,

    onRepairArtistChange:
        (String) -> Unit,

    onRepairAlbumChange:
        (String) -> Unit,

    onSaveRepair: () -> Unit,

    onOpenPair:
        (ComparePair) -> Unit,

    onClosePair: () -> Unit,

    onTogglePreview:
        (CompareSide) -> Unit,

    onKeepCurrent: () -> Unit,
    onKeepReference: () -> Unit,
    onKeepBoth: () -> Unit
) {
    val selected =
        state.selectedPair

    if (selected != null) {
        CompareDetail(
            state = state,

            modifier =
                modifier,

            onBack =
                onClosePair,

            onTogglePreview =
                onTogglePreview,

            onKeepCurrent =
                onKeepCurrent,

            onKeepReference =
                onKeepReference,

            onKeepBoth =
                onKeepBoth
        )

        return
    }

    var selectedSection by
        rememberSaveable {
            mutableStateOf(
                CompareSection
                    .DUPLICATES
            )
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .statusBarsPadding()
    ) {
        CompareSectionTabs(
            selected =
                selectedSection,

            errorCount =
                repairState
                    .errorCount,

            onSelected = {
                selectedSection =
                    it

                if (
                    it ==
                    CompareSection
                        .METADATA_ERRORS
                ) {
                    onEnsureRepair()
                }
            }
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(
                        1f
                    )
        ) {
            when (
                selectedSection
            ) {
                CompareSection
                    .DUPLICATES -> {

                    CompareList(
                        state =
                            state,

                        modifier =
                            Modifier
                                .fillMaxSize(),

                        onRefresh =
                            onRefresh,

                        onOpenPair =
                            onOpenPair
                    )
                }

                CompareSection
                    .METADATA_ERRORS -> {

                    MetadataRepairTab(
                        state =
                            repairState,

                        modifier =
                            Modifier
                                .fillMaxSize(),

                        onRefresh =
                            onRefreshRepair,

                        onFilterChange =
                            onRepairFilterChange,

                        onDownloadReplacement =
                            onDownloadRepairReplacement,

                        onYearChange =
                            onRepairYearChange,

                        onLookupYear =
                            onLookupRepairYear,

                        onSelect =
                            onSelectRepair,

                        onDismissEditor =
                            onDismissRepair,

                        onTitleChange =
                            onRepairTitleChange,

                        onArtistChange =
                            onRepairArtistChange,

                        onAlbumChange =
                            onRepairAlbumChange,

                        onSave =
                            onSaveRepair
                    )
                }
            }
        }
    }
}

@Composable
private fun CompareSectionTabs(
    selected: CompareSection,
    errorCount: Int,

    onSelected:
        (CompareSection) -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = 12.dp,
                    top = 8.dp,
                    end = 12.dp,
                    bottom = 2.dp
                )
                .background(
                    color =
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant
                            .copy(
                                alpha = 0.46f
                            ),

                    shape =
                        RoundedCornerShape(22.dp)
                )
                .padding(
                    4.dp
                ),

        horizontalArrangement =
            Arrangement
                .spacedBy(
                    4.dp
                )
    ) {
        val duplicateSelected =
            selected ==
                CompareSection
                    .DUPLICATES

        Row(
            modifier =
                Modifier
                    .weight(
                        1f
                    )
                    .clip(
                        RoundedCornerShape(
                            14.dp
                        )
                    )
                    .background(
                        if (
                            duplicateSelected
                        ) {
                            MaterialTheme
                                .colorScheme
                                .primaryContainer
                        } else {
                            Color.Transparent
                        }
                    )
                    .bouncyClickable {
                        onSelected(
                            CompareSection
                                .DUPLICATES
                        )
                    }
                    .padding(
                        horizontal =
                            10.dp,

                        vertical =
                            10.dp
                    ),

            horizontalArrangement =
                Arrangement.Center,

            verticalAlignment =
                Alignment
                    .CenterVertically
        ) {
            Text(
                text =
                    "So trùng",

                fontWeight =
                    if (
                        duplicateSelected
                    ) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    },

                color =
                    if (
                        duplicateSelected
                    ) {
                        MaterialTheme
                            .colorScheme
                            .onPrimaryContainer
                    } else {
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                    }
            )
        }

        val errorSelected =
            selected ==
                CompareSection
                    .METADATA_ERRORS

        Row(
            modifier =
                Modifier
                    .weight(
                        1f
                    )
                    .clip(
                        RoundedCornerShape(
                            14.dp
                        )
                    )
                    .background(
                        if (
                            errorSelected
                        ) {
                            MaterialTheme
                                .colorScheme
                                .errorContainer
                        } else {
                            Color.Transparent
                        }
                    )
                    .bouncyClickable {
                        onSelected(
                            CompareSection
                                .METADATA_ERRORS
                        )
                    }
                    .padding(
                        horizontal =
                            10.dp,

                        vertical =
                            10.dp
                    ),

            horizontalArrangement =
                Arrangement.Center,

            verticalAlignment =
                Alignment
                    .CenterVertically
        ) {
            Text(
                text =
                    if (
                        errorCount >
                        0
                    ) {
                        "File lỗi $errorCount"
                    } else {
                        "File lỗi"
                    },

                fontWeight =
                    if (
                        errorSelected
                    ) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    },

                color =
                    if (
                        errorSelected
                    ) {
                        MaterialTheme
                            .colorScheme
                            .onErrorContainer
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
private fun CompareList(
    state: CompareUiState,
    modifier: Modifier,
    onRefresh: () -> Unit,
    onOpenPair: (ComparePair) -> Unit
) {
    var nearCollapsed by
        rememberSaveable {
            mutableStateOf(true)
        }

    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding =
            androidx.compose.foundation.layout
                .PaddingValues(
                    start = 12.dp,
                    top = 8.dp,
                    end = 12.dp,
                    bottom = 14.dp
                ),
        verticalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {
                    Text(
                        text = "Đối chiếu",
                        style =
                            MaterialTheme
                                .typography
                                .headlineSmall,
                        fontWeight =
                            FontWeight.ExtraBold
                    )

                    Text(
                        text =
                            "${state.exactPairs.size} trùng hoàn toàn · " +
                                "${state.nearPairs.size} gần trùng ≥90%",
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
                    onClick = onRefresh,
                    enabled =
                        !state.isLoading &&
                            !state.isWorking
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(17.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector =
                                Icons.Rounded.Refresh,
                            contentDescription = null,
                            modifier =
                                Modifier.size(17.dp)
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.width(6.dp)
                    )

                    Text("Đối chiếu lại")
                }
            }
        }

        if (state.isLoading) {
            item {
                Surface(
                    modifier =
                        Modifier.fillMaxWidth(),
                    shape =
                        RoundedCornerShape(16.dp),
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier =
                            Modifier.padding(12.dp),
                        verticalAlignment =
                            Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(9.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )

                        Text(
                            text =
                                "Đang so thư mục tải với dữ liệu đã chuẩn bị...",
                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall
                        )
                    }
                }
            }
        }

        state.errorMessage
            ?.let { message ->
                item {
                    Surface(
                        modifier =
                            Modifier.fillMaxWidth(),
                        shape =
                            RoundedCornerShape(16.dp),
                        color =
                            MaterialTheme
                                .colorScheme
                                .errorContainer
                    ) {
                        Column(
                            modifier =
                                Modifier.padding(13.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = message,
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onErrorContainer
                            )

                            Text(
                                text =
                                    "Nếu chưa có data, vào Cài đặt → Chuẩn bị dữ liệu.",
                                style =
                                    MaterialTheme
                                        .typography
                                        .labelSmall
                            )
                        }
                    }
                }
            }

        if (
            state.hasLoaded &&
            !state.isLoading &&
            state.totalPairCount == 0 &&
            state.errorMessage == null
        ) {
            item {
                Surface(
                    modifier =
                        Modifier.fillMaxWidth(),
                    shape =
                        RoundedCornerShape(20.dp),
                    tonalElevation = 3.dp
                ) {
                    Column(
                        modifier =
                            Modifier.padding(20.dp),
                        horizontalAlignment =
                            Alignment.CenterHorizontally,
                        verticalArrangement =
                            Arrangement.spacedBy(7.dp)
                    ) {
                        Icon(
                            imageVector =
                                Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier =
                                Modifier.size(34.dp)
                        )

                        Text(
                            text =
                                "Không có bài cần xử lý",
                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            text =
                                "Nút Đối chiếu lại chỉ cập nhật thư mục tải; kho đối chiếu không bị quét lại.",
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

        if (state.exactPairs.isNotEmpty()) {
            item {
                CompareSectionHeader(
                    title = "Trùng hoàn toàn",
                    count =
                        state.exactPairs.size,
                    subtitle =
                        "Tên file chuẩn hóa giống 100%",
                    expanded = true,
                    onClick = null
                )
            }

            items(
                items = state.exactPairs,
                key = {
                    "exact-${it.id}"
                }
            ) { pair ->
                ComparePairRow(
                    pair = pair,
                    onClick = {
                        onOpenPair(pair)
                    }
                )
            }
        }

        if (state.nearPairs.isNotEmpty()) {
            item {
                CompareSectionHeader(
                    title = "Có thể trùng ≥ 90%",
                    count =
                        state.nearPairs.size,
                    subtitle =
                        if (nearCollapsed) {
                            "Nhấn để xem"
                        } else {
                            "Nhấn để thu gọn"
                        },
                    expanded =
                        !nearCollapsed,
                    onClick = {
                        nearCollapsed =
                            !nearCollapsed
                    }
                )
            }

            if (!nearCollapsed) {
                items(
                    items = state.nearPairs,
                    key = {
                        "near-${it.id}"
                    }
                ) { pair ->
                    ComparePairRow(
                        pair = pair,
                        onClick = {
                            onOpenPair(pair)
                        }
                    )
                }
            }
        }

        if (state.ignoredPairCount > 0) {
            item {
                Text(
                    text =
                        "Đã ẩn ${state.ignoredPairCount} cặp được chọn Giữ cả hai.",
                    modifier =
                        Modifier.padding(
                            horizontal = 3.dp,
                            vertical = 4.dp
                        ),
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
}

@Composable
private fun CompareSectionHeader(
    title: String,
    count: Int,
    subtitle: String,
    expanded: Boolean,
    onClick: (() -> Unit)?
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) {
                        Modifier.bouncyClickable(
                            onClick = onClick
                        )
                    } else {
                        Modifier
                    }
                ),
        shape =
            RoundedCornerShape(16.dp),
        color =
            MaterialTheme
                .colorScheme
                .primaryContainer
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = 13.dp,
                    vertical = 9.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Column(
                modifier =
                    Modifier.weight(1f)
            ) {
                Text(
                    text = "$title · $count",
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text = subtitle,
                    style =
                        MaterialTheme
                            .typography
                            .labelSmall
                )
            }

            if (onClick != null) {
                Text(
                    if (expanded) "▲" else "▼"
                )
            }
        }
    }
}

@Composable
private fun ComparePairRow(
    pair: ComparePair,
    onClick: () -> Unit
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .bouncyClickable(onClick = onClick),
        shape =
            RoundedCornerShape(18.dp),
        tonalElevation = 3.dp,
        border =
            BorderStroke(
                width = 1.dp,
                color =
                    MaterialTheme
                        .colorScheme
                        .outlineVariant
            )
    ) {
        Row(
            modifier =
                Modifier.padding(9.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            IndexedCover(
                coverPath =
                    pair.current.coverPath,
                title =
                    pair.current.title,
                size = 58
            )

            Spacer(
                modifier =
                    Modifier.width(10.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text =
                        pair.current.title,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        pair.current.artist
                            .ifBlank {
                                "Chưa xác định Artist"
                            },
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

                Text(
                    text =
                        "Album: ${pair.current.album.ifBlank { "Chưa có" }} · " +
                            "Year: ${pair.current.year.ifBlank { "—" }}",
                    maxLines = 1,
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

            Surface(
                shape =
                    RoundedCornerShape(12.dp),
                color =
                    MaterialTheme
                        .colorScheme
                        .secondaryContainer
            ) {
                Text(
                    text =
                        if (
                            pair.kind ==
                            CompareMatchKind.EXACT
                        ) {
                            "100%"
                        } else {
                            "${(pair.score * 100).roundToInt()}%"
                        },
                    modifier =
                        Modifier.padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        ),
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun CompareDetail(
    state: CompareUiState,
    modifier: Modifier,
    onBack: () -> Unit,
    onTogglePreview: (CompareSide) -> Unit,
    onKeepCurrent: () -> Unit,
    onKeepReference: () -> Unit,
    onKeepBoth: () -> Unit
) {
    val pair =
        state.selectedPair
            ?: return

    var pendingAction by
        remember {
            mutableStateOf<PendingCompareAction?>(
                null
            )
        }

    pendingAction
        ?.let { action ->
            val keepNew =
                action ==
                    PendingCompareAction
                        .KEEP_CURRENT

            AlertDialog(
                onDismissRequest = {
                    pendingAction = null
                },
                title = {
                    Text(
                        if (keepNew) {
                            "Giữ bản mới?"
                        } else {
                            "Giữ bản cũ?"
                        }
                    )
                },
                text = {
                    Text(
                        if (keepNew) {
                            "Bản mới sẽ thay file trong kho đối chiếu. App copy + kiểm tra đủ byte trước khi bỏ bản cũ."
                        } else {
                            "Bản mới trong thư mục tải sẽ bị xóa; bản trong kho giữ nguyên."
                        }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            pendingAction = null

                            if (keepNew) {
                                onKeepCurrent()
                            } else {
                                onKeepReference()
                            }
                        }
                    ) {
                        Text("Xác nhận")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            pendingAction = null
                        }
                    ) {
                        Text("Hủy")
                    }
                }
            )
        }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(
                horizontal = 12.dp,
                vertical = 4.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(7.dp)
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBack,
                enabled =
                    !state.isWorking
            ) {
                Text("←")
            }

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {
                Text(
                    text =
                        pair.current.title,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis,
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        if (
                            pair.kind ==
                            CompareMatchKind.EXACT
                        ) {
                            "${pair.current.artist} · Trùng hoàn toàn"
                        } else {
                            "${pair.current.artist} · ${(pair.score * 100).roundToInt()}%"
                        },
                    maxLines = 1,
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

        VersionCard(
            label = "BẢN MỚI",
            file = pair.current,
            playing =
                state.playingSide ==
                    CompareSide.CURRENT,
            enabled =
                !state.isWorking,
            onPlay = {
                onTogglePreview(
                    CompareSide.CURRENT
                )
            }
        )

        VersionCard(
            label = "BẢN CŨ / ĐỐI CHIẾU",
            file = pair.reference,
            playing =
                state.playingSide ==
                    CompareSide.REFERENCE,
            enabled =
                !state.isWorking,
            onPlay = {
                onTogglePreview(
                    CompareSide.REFERENCE
                )
            }
        )

        Spacer(
            modifier =
                Modifier.weight(1f)
        )

        Button(
            onClick = {
                val next =
                    if (
                        state.playingSide ==
                        CompareSide.CURRENT
                    ) {
                        CompareSide.REFERENCE
                    } else {
                        CompareSide.CURRENT
                    }

                onTogglePreview(next)
            },
            enabled =
                !state.isWorking,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(40.dp)
        ) {
            Text("⇆ Đổi nhanh A / B")
        }

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(7.dp)
        ) {
            Button(
                onClick = {
                    pendingAction =
                        PendingCompareAction
                            .KEEP_CURRENT
                },
                enabled =
                    !state.isWorking,
                modifier =
                    Modifier
                        .weight(1f)
                        .height(42.dp)
            ) {
                Text("Giữ bản mới")
            }

            OutlinedButton(
                onClick = {
                    pendingAction =
                        PendingCompareAction
                            .KEEP_REFERENCE
                },
                enabled =
                    !state.isWorking,
                modifier =
                    Modifier
                        .weight(1f)
                        .height(42.dp)
            ) {
                Text("Giữ bản cũ")
            }
        }

        OutlinedButton(
            onClick = onKeepBoth,
            enabled =
                !state.isWorking,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(40.dp)
        ) {
            Text("Giữ cả hai · Không hiện lại")
        }

        if (state.isWorking) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.Center,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.size(15.dp),
                    strokeWidth = 2.dp
                )

                Spacer(
                    modifier =
                        Modifier.width(6.dp)
                )

                Text(
                    text = "Đang xử lý...",
                    style =
                        MaterialTheme
                            .typography
                            .labelSmall
                )
            }
        }
    }
}

@Composable
private fun VersionCard(
    label: String,
    file: CompareFile,
    playing: Boolean,
    enabled: Boolean,
    onPlay: () -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(18.dp),
        tonalElevation = 4.dp,
        border =
            BorderStroke(
                1.dp,
                MaterialTheme
                    .colorScheme
                    .outlineVariant
            )
    ) {
        Row(
            modifier =
                Modifier.padding(9.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            IndexedCover(
                coverPath = file.coverPath,
                title = file.title,
                size = 74
            )

            Spacer(
                modifier =
                    Modifier.width(10.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = label,
                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,
                    color =
                        MaterialTheme
                            .colorScheme
                            .primary,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text = file.title,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        file.artist.ifBlank {
                            "Chưa xác định Artist"
                        },
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis,
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall
                )

                Text(
                    text =
                        "Album: ${file.album.ifBlank { "Chưa có" }} · " +
                            "Year: ${file.year.ifBlank { "—" }}",
                    maxLines = 1,
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

                Text(
                    text =
                        buildString {
                            append(
                                file.durationSeconds
                                    ?.let(::formatDuration)
                                    ?: "--:--"
                            )
                            append(" · ")
                            append(
                                formatBytes(
                                    file.sizeBytes
                                )
                            )
                            append(" · ")
                            append(
                                file.bitrateKbps
                                    ?.let {
                                        "$it kbps"
                                    }
                                    ?: "? kbps"
                            )
                        },
                    maxLines = 1,
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

            OutlinedButton(
                onClick = onPlay,
                enabled = enabled
            ) {
                Text(
                    if (playing) "Ⅱ" else "▶"
                )
            }
        }
    }
}

@Composable
private fun IndexedCover(
    coverPath: String?,
    title: String,
    size: Int
) {
    val path =
        coverPath
            ?.takeIf {
                it.isNotBlank()
            }

    Surface(
        modifier =
            Modifier.size(size.dp),
        shape =
            RoundedCornerShape(14.dp),
        color =
            MaterialTheme
                .colorScheme
                .surfaceVariant
    ) {
        if (path != null) {
            AsyncImage(
                model = File(path),
                contentDescription = title,
                modifier =
                    Modifier.fillMaxSize(),
                contentScale =
                    ContentScale.Crop
            )
        } else {
            Column(
                modifier =
                    Modifier.fillMaxSize(),
                horizontalAlignment =
                    Alignment.CenterHorizontally,
                verticalArrangement =
                    Arrangement.Center
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.MusicNote,
                    contentDescription = null,
                    modifier =
                        Modifier.size(
                            (size / 2).dp
                        )
                )
            }
        }
    }
}

private fun formatDuration(
    seconds: Long
): String {
    val safe =
        seconds.coerceAtLeast(0L)

    return "%d:%02d".format(
        safe / 60L,
        safe % 60L
    )
}

private fun formatBytes(
    bytes: Long
): String {
    if (bytes <= 0L) {
        return "? MB"
    }

    val megabytes =
        bytes /
            (1024.0 * 1024.0)

    return "%.1f MB".format(
        megabytes
    )
}
