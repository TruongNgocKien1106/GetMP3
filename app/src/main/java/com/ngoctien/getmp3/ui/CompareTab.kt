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
import androidx.compose.runtime.LaunchedEffect
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


@Suppress("UNUSED_PARAMETER")
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

    val firstPendingPair =
        state.exactPairs
            .firstOrNull()
            ?: state.nearPairs
                .firstOrNull()

    /*
     * CompareViewModel normally selects the first pair.
     * This is a defensive UI fallback so the old list
     * never becomes the visible entry screen.
     */
    LaunchedEffect(
        selected?.id,
        firstPendingPair?.id,
        state.isLoading
    ) {
        if (
            selected == null &&
            firstPendingPair != null &&
            !state.isLoading
        ) {
            onOpenPair(
                firstPendingPair
            )
        }
    }

    if (selected != null) {
        CompareDetail(
            state =
                state,

            modifier =
                modifier,

            onSelectPair =
                onOpenPair,

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

    AppScreenBackdrop(
        modifier =
            modifier
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(
                        horizontal =
                            18.dp,

                        vertical =
                            18.dp
                    ),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {
                    Text(
                        text =
                            "DUPLICATE REVIEW",

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        fontWeight =
                            FontWeight.Black,

                        color =
                            com.ngoctien.getmp3.ui.theme
                                .BrandPink
                    )

                    Text(
                        text =
                            if (state.isLoading) {
                                "Đang đối chiếu"
                            }
                            else {
                                "Đã xử lý xong"
                            },

                        style =
                            MaterialTheme
                                .typography
                                .titleLarge,

                        fontWeight =
                            FontWeight.Black
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            if (state.isLoading) {
                CircularProgressIndicator()

                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )

                Text(
                    text =
                        "Đang tìm các cặp có độ trùng từ 90% trở lên.",

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
            else {
                Icon(
                    imageVector =
                        Icons.Rounded
                            .CheckCircle,

                    contentDescription =
                        null,

                    modifier =
                        Modifier.size(
                            44.dp
                        )
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            10.dp
                        )
                )

                Text(
                    text =
                        state.errorMessage
                            ?: "Không còn cặp trùng nào cần xử lý.",

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            14.dp
                        )
                )

                OutlinedButton(
                    onClick =
                        onRefresh
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded
                                .Refresh,

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(
                                17.dp
                            )
                    )

                    Spacer(
                        modifier =
                            Modifier.width(
                                6.dp
                            )
                    )

                    Text(
                        "Đối chiếu lại"
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.weight(
                        1f
                    )
            )
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
            mutableStateOf(
                false
            )
        }

    LazyColumn(
        modifier =
            modifier.fillMaxSize(),

        contentPadding =
            androidx.compose.foundation.layout
                .PaddingValues(
                    start = 12.dp,
                    top = 9.dp,
                    end = 12.dp,
                    bottom = 16.dp
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                9.dp
            )
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
                        Modifier.weight(
                            1f
                        )
                ) {
                    Text(
                        text =
                            "DUPLICATE REVIEW",

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        fontWeight =
                            FontWeight.Black,

                        color =
                            com.ngoctien.getmp3.ui.theme
                                .BrandPink
                    )

                    Text(
                        text =
                            "Đối chiếu",

                        style =
                            MaterialTheme
                                .typography
                                .headlineSmall,

                        fontWeight =
                            FontWeight.Black
                    )

                    Text(
                        text =
                            "Chỉ hiển thị cặp có độ trùng từ 90% trở lên",

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
                    onClick =
                        onRefresh,

                    enabled =
                        !state.isLoading &&
                            !state.isWorking
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(
                                    17.dp
                                ),

                            strokeWidth =
                                2.dp
                        )
                    }
                    else {
                        Icon(
                            imageVector =
                                Icons.Rounded
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
                        "Đối chiếu lại"
                    )
                }
            }
        }

        item {
            val shape =
                RoundedCornerShape(
                    20.dp
                )

            Surface(
                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    shape,

                color =
                    Color.Transparent,

                tonalElevation =
                    4.dp,

                shadowElevation =
                    6.dp
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(
                                shape
                            )
                            .background(
                                androidx.compose.ui.graphics
                                    .Brush
                                    .linearGradient(
                                        listOf(
                                            com.ngoctien.getmp3.ui.theme
                                                .BrandBlue
                                                .copy(
                                                    alpha =
                                                        0.44f
                                                ),

                                            com.ngoctien.getmp3.ui.theme
                                                .BrandViolet
                                                .copy(
                                                    alpha =
                                                        0.34f
                                                ),

                                            com.ngoctien.getmp3.ui.theme
                                                .BrandPink
                                                .copy(
                                                    alpha =
                                                        0.20f
                                                )
                                        )
                                    )
                            )
                            .padding(
                                1.dp
                            )
                            .clip(
                                RoundedCornerShape(
                                    19.dp
                                )
                            )
                            .background(
                                MaterialTheme
                                    .colorScheme
                                    .surface
                                    .copy(
                                        alpha =
                                            0.90f
                                    )
                            )
                            .padding(
                                horizontal =
                                    14.dp,

                                vertical =
                                    11.dp
                            )
                ) {
                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.spacedBy(
                                10.dp
                            )
                    ) {
                        DuplicateCountStat(
                            label =
                                "Trùng hoàn toàn",

                            value =
                                state.exactPairs
                                    .size,

                            accent =
                                com.ngoctien.getmp3.ui.theme
                                    .BrandSky,

                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        )

                        DuplicateCountStat(
                            label =
                                "Gần trùng",

                            value =
                                state.nearPairs
                                    .size,

                            accent =
                                com.ngoctien.getmp3.ui.theme
                                    .BrandPink,

                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        )
                    }
                }
            }
        }

        if (state.isLoading) {
            item {
                Surface(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(
                            17.dp
                        ),

                    color =
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant
                            .copy(
                                alpha =
                                    0.58f
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
                                9.dp
                            )
                    ) {
                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(
                                    18.dp
                                ),

                            strokeWidth =
                                2.dp
                        )

                        Text(
                            text =
                                "Đang đối chiếu Library...",

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
            ?.let {
                    message ->

                item {
                    Surface(
                        modifier =
                            Modifier.fillMaxWidth(),

                        shape =
                            RoundedCornerShape(
                                17.dp
                            ),

                        color =
                            MaterialTheme
                                .colorScheme
                                .errorContainer
                                .copy(
                                    alpha =
                                        0.72f
                                )
                    ) {
                        Text(
                            text =
                                message,

                            modifier =
                                Modifier.padding(
                                    13.dp
                                )
                        )
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
                        RoundedCornerShape(
                            20.dp
                        ),

                    color =
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant
                            .copy(
                                alpha =
                                    0.52f
                            ),

                    tonalElevation =
                        3.dp
                ) {
                    Column(
                        modifier =
                            Modifier.padding(
                                20.dp
                            ),

                        horizontalAlignment =
                            Alignment.CenterHorizontally,

                        verticalArrangement =
                            Arrangement.spacedBy(
                                7.dp
                            )
                    ) {
                        Icon(
                            imageVector =
                                Icons.Rounded
                                    .CheckCircle,

                            contentDescription =
                                null,

                            modifier =
                                Modifier.size(
                                    34.dp
                                )
                        )

                        Text(
                            text =
                                "Không có bài cần xử lý",

                            fontWeight =
                                FontWeight.Black
                        )
                    }
                }
            }
        }

        if (state.exactPairs.isNotEmpty()) {
            item {
                CompareSectionHeader(
                    title =
                        "Trùng hoàn toàn",

                    count =
                        state.exactPairs.size,

                    subtitle =
                        "Tên file chuẩn hóa giống 100%",

                    expanded =
                        true,

                    onClick =
                        null
                )
            }

            items(
                items =
                    state.exactPairs,

                key = {
                    "exact-${it.id}"
                }
            ) {
                    pair ->

                ComparePairRow(
                    pair =
                        pair,

                    onClick = {
                        onOpenPair(
                            pair
                        )
                    }
                )
            }
        }

        if (state.nearPairs.isNotEmpty()) {
            item {
                CompareSectionHeader(
                    title =
                        "Có thể trùng",

                    count =
                        state.nearPairs.size,

                    subtitle =
                        "Độ giống từ 90% đến dưới 100%",

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
                    items =
                        state.nearPairs,

                    key = {
                        "near-${it.id}"
                    }
                ) {
                        pair ->

                    ComparePairRow(
                        pair =
                            pair,

                        onClick = {
                            onOpenPair(
                                pair
                            )
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
                            horizontal =
                                3.dp,

                            vertical =
                                4.dp
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
private fun DuplicateCountStat(
    label: String,
    value: Int,
    accent: Color,
    modifier: Modifier
) {
    Surface(
        modifier =
            modifier,

        shape =
            RoundedCornerShape(
                15.dp
            ),

        color =
            accent
                .copy(
                    alpha =
                        0.13f
                ),

        border =
            BorderStroke(
                width =
                    1.dp,

                color =
                    accent
                        .copy(
                            alpha =
                                0.34f
                        )
            )
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal =
                        11.dp,

                    vertical =
                        9.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {
                Text(
                    text =
                        label,

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
                        value.toString(),

                    style =
                        MaterialTheme
                            .typography
                            .titleLarge,

                    fontWeight =
                        FontWeight.Black,

                    color =
                        accent
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
    val exact =
        title.startsWith(
            "Trùng hoàn toàn"
        )

    val startColor =
        if (exact) {
            com.ngoctien.getmp3.ui.theme
                .BrandBlue
        }
        else {
            com.ngoctien.getmp3.ui.theme
                .BrandViolet
        }

    val endColor =
        if (exact) {
            com.ngoctien.getmp3.ui.theme
                .BrandViolet
        }
        else {
            com.ngoctien.getmp3.ui.theme
                .BrandPink
        }

    val shape =
        RoundedCornerShape(
            18.dp
        )

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) {
                        Modifier.bouncyClickable(
                            onClick =
                                onClick
                        )
                    }
                    else {
                        Modifier
                    }
                ),

        shape =
            shape,

        color =
            Color.Transparent,

        tonalElevation =
            4.dp,

        shadowElevation =
            5.dp
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(
                        shape
                    )
                    .background(
                        androidx.compose.ui.graphics
                            .Brush
                            .linearGradient(
                                listOf(
                                    startColor
                                        .copy(
                                            alpha =
                                                0.78f
                                        ),

                                    endColor
                                        .copy(
                                            alpha =
                                                0.58f
                                        )
                                )
                            )
                    )
                    .padding(
                        horizontal =
                            14.dp,

                        vertical =
                            10.dp
                    )
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
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
                            FontWeight.Black,

                        color =
                            Color.White
                    )

                    Text(
                        text =
                            subtitle,

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        color =
                            Color.White
                                .copy(
                                    alpha =
                                        0.74f
                                )
                    )
                }

                Surface(
                    shape =
                        RoundedCornerShape(
                            99.dp
                        ),

                    color =
                        Color.Black
                            .copy(
                                alpha =
                                    0.20f
                            )
                ) {
                    Text(
                        text =
                            "$count",

                        modifier =
                            Modifier.padding(
                                horizontal =
                                    10.dp,

                                vertical =
                                    5.dp
                            ),

                        fontWeight =
                            FontWeight.Black,

                        color =
                            Color.White
                    )
                }

                if (onClick != null) {
                    Spacer(
                        modifier =
                            Modifier.width(
                                8.dp
                            )
                    )

                    Text(
                        text =
                            if (expanded) {
                                "▲"
                            }
                            else {
                                "▼"
                            },

                        color =
                            Color.White
                    )
                }
            }
        }
    }
}


@Composable
private fun ComparePairRow(
    pair: ComparePair,
    onClick: () -> Unit
) {
    val exact =
        pair.kind ==
            CompareMatchKind.EXACT

    val score =
        if (exact) {
            100
        }
        else {
            (
                pair.score *
                    100
                )
                .roundToInt()
        }

    val accentStart =
        if (exact) {
            com.ngoctien.getmp3.ui.theme
                .BrandBlue
        }
        else {
            com.ngoctien.getmp3.ui.theme
                .BrandViolet
        }

    val accentEnd =
        if (exact) {
            com.ngoctien.getmp3.ui.theme
                .BrandCyan
        }
        else {
            com.ngoctien.getmp3.ui.theme
                .BrandPink
        }

    val shape =
        RoundedCornerShape(
            20.dp
        )

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .bouncyClickable(
                    pressedScale =
                        0.975f,

                    onClick =
                        onClick
                ),

        shape =
            shape,

        color =
            Color.Transparent,

        tonalElevation =
            4.dp,

        shadowElevation =
            6.dp
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(
                        shape
                    )
                    .background(
                        androidx.compose.ui.graphics
                            .Brush
                            .linearGradient(
                                listOf(
                                    accentStart
                                        .copy(
                                            alpha =
                                                0.58f
                                        ),

                                    accentEnd
                                        .copy(
                                            alpha =
                                                0.30f
                                        ),

                                    com.ngoctien.getmp3.ui.theme
                                        .BrandViolet
                                        .copy(
                                            alpha =
                                                0.18f
                                        )
                                )
                            )
                    )
                    .padding(
                        1.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            19.dp
                        )
                    )
                    .background(
                        MaterialTheme
                            .colorScheme
                            .surface
                            .copy(
                                alpha =
                                    0.93f
                            )
                    )
                    .padding(
                        9.dp
                    )
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                IndexedCover(
                    coverPath =
                        pair.current
                            .coverPath,

                    title =
                        pair.current
                            .title,

                    size =
                        60
                )

                Spacer(
                    modifier =
                        Modifier.width(
                            10.dp
                        )
                )

                Column(
                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            2.dp
                        )
                ) {
                    Text(
                        text =
                            pair.current
                                .title
                                .ifBlank {
                                    "Chưa có Title"
                                },

                        maxLines =
                            1,

                        overflow =
                            TextOverflow
                                .Ellipsis,

                        fontWeight =
                            FontWeight.Black
                    )

                    Text(
                        text =
                            pair.current
                                .artist
                                .ifBlank {
                                    "Chưa xác định Artist"
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
                            "Library · " +
                                pair.reference
                                    .title
                                    .ifBlank {
                                        "Chưa có Title"
                                    },

                        maxLines =
                            1,

                        overflow =
                            TextOverflow
                                .Ellipsis,

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        color =
                            accentEnd
                    )
                }

                Surface(
                    shape =
                        RoundedCornerShape(
                            99.dp
                        ),

                    color =
                        accentStart
                            .copy(
                                alpha =
                                    0.20f
                            ),

                    border =
                        BorderStroke(
                            width =
                                1.dp,

                            color =
                                accentEnd
                                    .copy(
                                        alpha =
                                            0.46f
                                    )
                        )
                ) {
                    Text(
                        text =
                            "$score%",

                        modifier =
                            Modifier.padding(
                                horizontal =
                                    10.dp,

                                vertical =
                                    6.dp
                            ),

                        fontWeight =
                            FontWeight.Black,

                        color =
                            accentEnd
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(
                            6.dp
                        )
                )

                Text(
                    text =
                        "›",

                    style =
                        MaterialTheme
                            .typography
                            .headlineSmall,

                    fontWeight =
                        FontWeight.Bold,

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
private fun CompareDetail(
    state: CompareUiState,
    modifier: Modifier,
    onSelectPair: (ComparePair) -> Unit,
    onTogglePreview: (CompareSide) -> Unit,
    onKeepCurrent: () -> Unit,
    onKeepReference: () -> Unit,
    onKeepBoth: () -> Unit
) {
    val pair =
        state.selectedPair
            ?: return

    val exact =
        pair.kind ==
            CompareMatchKind.EXACT

    val similarity =
        if (exact) {
            100
        }
        else {
            (
                pair.score *
                    100
                )
                .roundToInt()
        }


    val pendingPairs =
        remember(
            state.exactPairs,
            state.nearPairs
        ) {
            state.exactPairs +
                state.nearPairs
        }

    var showPairPicker by
        remember {
            mutableStateOf(
                false
            )
        }

    if (showPairPicker) {
        AlertDialog(
            onDismissRequest = {
                showPairPicker =
                    false
            },

            title = {
                Column {
                    Text(
                        text =
                            "Bài đang trùng",

                        fontWeight =
                            FontWeight.Black
                    )

                    Text(
                        text =
                            "${pendingPairs.size} cặp đang chờ xử lý",

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
            },

            text = {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(
                                360.dp
                            ),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        )
                ) {
                    items(
                        items =
                            pendingPairs,

                        key = {
                            it.id
                        }
                    ) {
                            candidate ->

                        val candidateSelected =
                            candidate.id ==
                                pair.id

                        val candidateScore =
                            if (
                                candidate.kind ==
                                    CompareMatchKind.EXACT
                            ) {
                                100
                            }
                            else {
                                (
                                    candidate.score *
                                        100
                                    )
                                    .roundToInt()
                            }

                        Surface(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .bouncyClickable(
                                        pressedScale =
                                            0.975f
                                    ) {
                                        showPairPicker =
                                            false

                                        if (
                                            candidate.id !=
                                                pair.id
                                        ) {
                                            onSelectPair(
                                                candidate
                                            )
                                        }
                                    },

                            shape =
                                RoundedCornerShape(
                                    18.dp
                                ),

                            color =
                                if (candidateSelected) {
                                    MaterialTheme
                                        .colorScheme
                                        .primaryContainer
                                        .copy(
                                            alpha =
                                                0.86f
                                        )
                                }
                                else {
                                    MaterialTheme
                                        .colorScheme
                                        .surfaceVariant
                                        .copy(
                                            alpha =
                                                0.58f
                                        )
                                },

                            border =
                                BorderStroke(
                                    width =
                                        if (candidateSelected) {
                                            1.5.dp
                                        }
                                        else {
                                            1.dp
                                        },

                                    color =
                                        if (candidateSelected) {
                                            MaterialTheme
                                                .colorScheme
                                                .primary
                                        }
                                        else {
                                            MaterialTheme
                                                .colorScheme
                                                .outlineVariant
                                        }
                                ),

                            tonalElevation =
                                if (candidateSelected) {
                                    5.dp
                                }
                                else {
                                    2.dp
                                }
                        ) {
                            Row(
                                modifier =
                                    Modifier.padding(
                                        9.dp
                                    ),

                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {
                                IndexedCover(
                                    coverPath =
                                        candidate.current
                                            .coverPath,

                                    title =
                                        candidate.current
                                            .title,

                                    size =
                                        52
                                )

                                Spacer(
                                    modifier =
                                        Modifier.width(
                                            9.dp
                                        )
                                )

                                Column(
                                    modifier =
                                        Modifier.weight(
                                            1f
                                        ),

                                    verticalArrangement =
                                        Arrangement.spacedBy(
                                            2.dp
                                        )
                                ) {
                                    Text(
                                        text =
                                            candidate.current
                                                .title
                                                .ifBlank {
                                                    "Chưa có Title"
                                                },

                                        maxLines =
                                            1,

                                        overflow =
                                            TextOverflow
                                                .Ellipsis,

                                        fontWeight =
                                            FontWeight.Black
                                    )

                                    Text(
                                        text =
                                            candidate.current
                                                .artist
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
                                                .bodySmall,

                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .onSurfaceVariant
                                    )

                                    if (candidateSelected) {
                                        Text(
                                            text =
                                                "Đang xử lý",

                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .labelSmall,

                                            fontWeight =
                                                FontWeight.Bold,

                                            color =
                                                MaterialTheme
                                                    .colorScheme
                                                    .primary
                                        )
                                    }
                                }

                                Surface(
                                    shape =
                                        RoundedCornerShape(
                                            99.dp
                                        ),

                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .surface
                                            .copy(
                                                alpha =
                                                    0.56f
                                            ),

                                    border =
                                        BorderStroke(
                                            width =
                                                1.dp,

                                            color =
                                                MaterialTheme
                                                    .colorScheme
                                                    .outlineVariant
                                        )
                                ) {
                                    Text(
                                        text =
                                            "$candidateScore%",

                                        modifier =
                                            Modifier.padding(
                                                horizontal =
                                                    9.dp,

                                                vertical =
                                                    5.dp
                                            ),

                                        fontWeight =
                                            FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showPairPicker =
                            false
                    }
                ) {
                    Text(
                        "Đóng"
                    )
                }
            }
        )
    }

    var pendingAction by
        remember {
            mutableStateOf<PendingCompareAction?>(
                null
            )
        }

    pendingAction
        ?.let {
                action ->

            val keepCurrent =
                action ==
                    PendingCompareAction
                        .KEEP_CURRENT

            AlertDialog(
                onDismissRequest = {
                    pendingAction =
                        null
                },

                title = {
                    Text(
                        if (keepCurrent) {
                            "Giữ bản hiện tại?"
                        }
                        else {
                            "Giữ bản Library?"
                        }
                    )
                },

                text = {
                    Text(
                        if (keepCurrent) {
                            "Bản hiện tại sẽ thay file trong Library sau khi copy và kiểm tra hoàn tất."
                        }
                        else {
                            "Bản hiện tại sẽ bị loại và file trong Library được giữ nguyên."
                        }
                    )
                },

                confirmButton = {
                    Button(
                        onClick = {
                            pendingAction =
                                null

                            if (keepCurrent) {
                                onKeepCurrent()
                            }
                            else {
                                onKeepReference()
                            }
                        }
                    ) {
                        Text(
                            "Xác nhận"
                        )
                    }
                },

                dismissButton = {
                    TextButton(
                        onClick = {
                            pendingAction =
                                null
                        }
                    ) {
                        Text(
                            "Hủy"
                        )
                    }
                }
            )
        }

    AppScreenBackdrop(
        modifier =
            modifier
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(
                        horizontal =
                            12.dp,

                        vertical =
                            7.dp
                    ),

            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text =
                        "DUPLICATE REVIEW",

                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    style =
                        MaterialTheme
                            .typography
                            .labelMedium,

                    fontWeight =
                        FontWeight.Black,

                    color =
                        com.ngoctien.getmp3.ui.theme
                            .BrandPink
                )

                Surface(
                    modifier =
                        Modifier
                            .bouncyClickable(
                                enabled =
                                    !state.isWorking &&
                                        pendingPairs.isNotEmpty(),

                                pressedScale =
                                    0.94f
                            ) {
                                showPairPicker =
                                    true
                            },

                    shape =
                        RoundedCornerShape(
                            99.dp
                        ),

                    color =
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant
                            .copy(
                                alpha =
                                    0.76f
                            ),

                    border =
                        BorderStroke(
                            width =
                                1.dp,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .outlineVariant
                        ),

                    tonalElevation =
                        4.dp,

                    shadowElevation =
                        4.dp
                ) {
                    Row(
                        modifier =
                            Modifier.padding(
                                horizontal =
                                    12.dp,

                                vertical =
                                    7.dp
                            ),

                        verticalAlignment =
                            Alignment.CenterVertically,

                        horizontalArrangement =
                            Arrangement.spacedBy(
                                5.dp
                            )
                    ) {
                        Text(
                            text =
                                "${state.totalPairCount} cặp",

                            style =
                                MaterialTheme
                                    .typography
                                    .labelMedium,

                            fontWeight =
                                FontWeight.Black
                        )

                        Text(
                            text =
                                "⌄",

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .primary,

                            fontWeight =
                                FontWeight.Black
                        )
                    }
                }
            }

            Surface(
                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(
                        20.dp
                    ),

                color =
                    if (exact) {
                        MaterialTheme
                            .colorScheme
                            .primaryContainer
                            .copy(
                                alpha =
                                    0.70f
                            )
                    }
                    else {
                        MaterialTheme
                            .colorScheme
                            .errorContainer
                            .copy(
                                alpha =
                                    0.42f
                            )
                    },

                border =
                    BorderStroke(
                        width =
                            1.dp,

                        color =
                            if (exact) {
                                MaterialTheme
                                    .colorScheme
                                    .primary
                                    .copy(
                                        alpha =
                                            0.46f
                                    )
                            }
                            else {
                                MaterialTheme
                                    .colorScheme
                                    .error
                                    .copy(
                                        alpha =
                                            0.38f
                                    )
                            }
                    ),

                tonalElevation =
                    3.dp
            ) {
                Row(
                    modifier =
                        Modifier.padding(
                            horizontal =
                                14.dp,

                            vertical =
                                11.dp
                        ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        text =
                            "Similarity",

                        modifier =
                            Modifier.weight(
                                1f
                            ),

                        style =
                            MaterialTheme
                                .typography
                                .labelMedium,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )

                    Text(
                        text =
                            "$similarity%",

                        style =
                            MaterialTheme
                                .typography
                                .headlineSmall,

                        fontWeight =
                            FontWeight.ExtraBold,

                        color =
                            if (exact) {
                                MaterialTheme
                                    .colorScheme
                                    .primary
                            }
                            else {
                                MaterialTheme
                                    .colorScheme
                                    .error
                            }
                    )

                    Spacer(
                        modifier =
                            Modifier.width(
                                10.dp
                            )
                    )

                    Surface(
                        shape =
                            RoundedCornerShape(
                                99.dp
                            ),

                        color =
                            MaterialTheme
                                .colorScheme
                                .surface
                                .copy(
                                    alpha =
                                        0.34f
                                )
                    ) {
                        Text(
                            text =
                                if (exact) {
                                    "TRÙNG"
                                }
                                else {
                                    "GẦN"
                                },

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
                                FontWeight.ExtraBold
                        )
                    }
                }
            }

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {
                DuplicateVersionPanel(
                    label =
                        "CURRENT",

                    file =
                        pair.current,

                    playing =
                        state.playingSide ==
                            CompareSide.CURRENT,

                    enabled =
                        !state.isWorking,

                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    onPlay = {
                        onTogglePreview(
                            CompareSide.CURRENT
                        )
                    }
                )

                DuplicateVersionPanel(
                    label =
                        "LIBRARY",

                    file =
                        pair.reference,

                    playing =
                        state.playingSide ==
                            CompareSide.REFERENCE,

                    enabled =
                        !state.isWorking,

                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    onPlay = {
                        onTogglePreview(
                            CompareSide.REFERENCE
                        )
                    }
                )
            }

            DuplicateMatchStrip(
                pair =
                    pair
            )

            Spacer(
                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            OutlinedButton(
                onClick = {
                    pendingAction =
                        PendingCompareAction
                            .KEEP_CURRENT
                },

                enabled =
                    !state.isWorking,

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            44.dp
                        )
            ) {
                Text(
                    "Giữ bản hiện tại"
                )
            }

            Button(
                onClick = {
                    pendingAction =
                        PendingCompareAction
                            .KEEP_REFERENCE
                },

                enabled =
                    !state.isWorking,

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            46.dp
                        )
            ) {
                Text(
                    "Giữ bản Library"
                )
            }

            OutlinedButton(
                onClick =
                    onKeepBoth,

                enabled =
                    !state.isWorking,

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            44.dp
                        )
            ) {
                Text(
                    "Giữ cả hai"
                )
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
                            Modifier.size(
                                15.dp
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

                    Text(
                        text =
                            "Đang xử lý...",

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall
                    )
                }
            }
        }
    }
}


@Composable
private fun DuplicateVersionPanel(
    label: String,
    file: CompareFile,
    playing: Boolean,
    enabled: Boolean,
    modifier: Modifier,
    onPlay: () -> Unit
) {
    Surface(
        modifier =
            modifier,

        shape =
            RoundedCornerShape(
                19.dp
            ),

        color =
            MaterialTheme
                .colorScheme
                .surface
                .copy(
                    alpha =
                        0.74f
                ),

        border =
            BorderStroke(
                width =
                    if (playing) {
                        1.5.dp
                    }
                    else {
                        1.dp
                    },

                color =
                    if (playing) {
                        MaterialTheme
                            .colorScheme
                            .primary
                    }
                    else {
                        MaterialTheme
                            .colorScheme
                            .outlineVariant
                    }
            ),

        tonalElevation =
            if (playing) {
                5.dp
            }
            else {
                3.dp
            }
    ) {
        Column(
            modifier =
                Modifier.padding(
                    10.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    5.dp
                )
        ) {
            Text(
                text =
                    label,

                style =
                    MaterialTheme
                        .typography
                        .labelSmall,

                fontWeight =
                    FontWeight.ExtraBold,

                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )

            Box(
                modifier =
                    Modifier.fillMaxWidth(),

                contentAlignment =
                    Alignment.Center
            ) {
                IndexedCover(
                    coverPath =
                        file.coverPath,

                    title =
                        file.title,

                    size =
                        82
                )

                Surface(
                    modifier =
                        Modifier
                            .align(
                                Alignment.BottomEnd
                            )
                            .size(
                                36.dp
                            )
                            .bouncyClickable(
                                enabled =
                                    enabled
                            ) {
                                onPlay()
                            },

                    shape =
                        RoundedCornerShape(
                            18.dp
                        ),

                    color =
                        if (playing) {
                            MaterialTheme
                                .colorScheme
                                .primary
                        }
                        else {
                            MaterialTheme
                                .colorScheme
                                .surfaceVariant
                        },

                    tonalElevation =
                        5.dp
                ) {
                    Box(
                        modifier =
                            Modifier.fillMaxSize(),

                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            text =
                                if (playing) {
                                    "Ⅱ"
                                }
                                else {
                                    "▶"
                                },

                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text =
                    file.title
                        .ifBlank {
                            "Chưa có Title"
                        },

                maxLines =
                    2,

                overflow =
                    TextOverflow
                        .Ellipsis,

                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                text =
                    file.artist
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
                        .bodySmall,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Text(
                text =
                    file.album
                        .ifBlank {
                            "Chưa có Album"
                        },

                maxLines =
                    1,

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
            )

            Text(
                text =
                    file.year
                        .ifBlank {
                            "Year —"
                        },

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
private fun DuplicateMatchStrip(
    pair: ComparePair
) {
    val currentCover =
        !pair.current
            .coverPath
            .isNullOrBlank()

    val referenceCover =
        !pair.reference
            .coverPath
            .isNullOrBlank()

    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                17.dp
            ),

        color =
            MaterialTheme
                .colorScheme
                .surfaceVariant
                .copy(
                    alpha =
                        0.50f
                ),

        border =
            BorderStroke(
                width =
                    1.dp,

                color =
                    MaterialTheme
                        .colorScheme
                        .outlineVariant
            )
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal =
                            5.dp,

                        vertical =
                            8.dp
                    ),

            horizontalArrangement =
                Arrangement.spacedBy(
                    2.dp
                )
        ) {
            DuplicateMatchCell(
                label =
                    "Cover",

                value =
                    when {
                        currentCover &&
                            referenceCover ->
                            "Có"

                        currentCover ||
                            referenceCover ->
                            "Thiếu"

                        else ->
                            "—"
                    },

                positive =
                    currentCover &&
                        referenceCover,

                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            DuplicateMatchCell(
                label =
                    "Title",

                value =
                    if (
                        compareTextSame(
                            pair.current.title,
                            pair.reference.title
                        )
                    ) {
                        "Trùng"
                    }
                    else {
                        "Khác"
                    },

                positive =
                    compareTextSame(
                        pair.current.title,
                        pair.reference.title
                    ),

                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            DuplicateMatchCell(
                label =
                    "Artist",

                value =
                    if (
                        compareTextSame(
                            pair.current.artist,
                            pair.reference.artist
                        )
                    ) {
                        "Trùng"
                    }
                    else {
                        "Khác"
                    },

                positive =
                    compareTextSame(
                        pair.current.artist,
                        pair.reference.artist
                    ),

                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            DuplicateMatchCell(
                label =
                    "Album",

                value =
                    if (
                        compareTextSame(
                            pair.current.album,
                            pair.reference.album
                        )
                    ) {
                        "Trùng"
                    }
                    else {
                        "Khác"
                    },

                positive =
                    compareTextSame(
                        pair.current.album,
                        pair.reference.album
                    ),

                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            DuplicateMatchCell(
                label =
                    "Year",

                value =
                    if (
                        compareTextSame(
                            pair.current.year,
                            pair.reference.year
                        )
                    ) {
                        "Trùng"
                    }
                    else {
                        "Khác"
                    },

                positive =
                    compareTextSame(
                        pair.current.year,
                        pair.reference.year
                    ),

                modifier =
                    Modifier.weight(
                        1f
                    )
            )
        }
    }
}


@Composable
private fun DuplicateMatchCell(
    label: String,
    value: String,
    positive: Boolean,
    modifier: Modifier
) {
    Column(
        modifier =
            modifier,

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.spacedBy(
                3.dp
            )
    ) {
        Text(
            text =
                label,

            maxLines =
                1,

            style =
                MaterialTheme
                    .typography
                    .labelSmall,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        Surface(
            shape =
                RoundedCornerShape(
                    99.dp
                ),

            color =
                if (positive) {
                    MaterialTheme
                        .colorScheme
                        .primaryContainer
                }
                else {
                    MaterialTheme
                        .colorScheme
                        .errorContainer
                        .copy(
                            alpha =
                                0.72f
                        )
                }
        ) {
            Text(
                text =
                    value,

                modifier =
                    Modifier.padding(
                        horizontal =
                            6.dp,

                        vertical =
                            3.dp
                    ),

                maxLines =
                    1,

                style =
                    MaterialTheme
                        .typography
                        .labelSmall,

                fontWeight =
                    FontWeight.Bold,

                color =
                    if (positive) {
                        MaterialTheme
                            .colorScheme
                            .onPrimaryContainer
                    }
                    else {
                        MaterialTheme
                            .colorScheme
                            .onErrorContainer
                    }
            )
        }
    }
}


private fun compareTextSame(
    left: String,
    right: String
): Boolean {
    return left
        .trim()
        .equals(
            right
                .trim(),

            ignoreCase =
                true
        )
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
