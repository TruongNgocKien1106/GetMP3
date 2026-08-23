package com.ngoctien.getmp3.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ngoctien.getmp3.data.DownloadJobEntity
import com.ngoctien.getmp3.model.DownloadStatus
import com.ngoctien.getmp3.note.ReferenceSongMatch
import com.ngoctien.getmp3.settings.AppSettings
import com.ngoctien.getmp3.ui.design.appPressable
import com.ngoctien.getmp3.viewmodel.DownloadScreenUiState
import com.ngoctien.getmp3.viewmodel.FfmpegReadyState
import com.ngoctien.getmp3.viewmodel.SearchDownloadSection
import com.ngoctien.getmp3.viewmodel.YouTubeSearchUiState
import com.ngoctien.getmp3.youtube.YouTubeSearchResult
import java.io.File


private enum class InboxTab {
    RESULTS,
    DUPLICATES,
    HISTORY
}


@Suppress("UNUSED_PARAMETER")
@Composable
internal fun BentoSearchDownloadScreen(
    searchState: YouTubeSearchUiState,
    downloadState: DownloadScreenUiState,
    settings: AppSettings,
    modifier: Modifier = Modifier,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearSearch: () -> Unit,
    onLoadMore: () -> Unit,
    onSelectSection:
        (SearchDownloadSection) -> Unit,
    onPasteAndDownload:
        (CharSequence?) -> Unit,
    onDownloadResult:
        (YouTubeSearchResult) -> Unit,
    onOpenReferenceSong:
        (ReferenceSongMatch) -> Unit,
    onCancelJob: (String) -> Unit,
    onRetryJob:
        (DownloadJobEntity) -> Unit,
    onClearHistory: () -> Unit,
    onRetryFfmpeg: () -> Unit
) {
    val initialTab =
        if (
            searchState.selectedSection ==
            SearchDownloadSection.QUEUE
        ) {
            InboxTab.HISTORY
        } else {
            InboxTab.RESULTS
        }

    var selectedTab by
        remember {
            mutableStateOf(
                initialTab
            )
        }

    LaunchedEffect(
        searchState.selectedSection
    ) {
        if (
            selectedTab !=
            InboxTab.DUPLICATES
        ) {
            selectedTab =
                if (
                    searchState.selectedSection ==
                    SearchDownloadSection.QUEUE
                ) {
                    InboxTab.HISTORY
                } else {
                    InboxTab.RESULTS
                }
        }
    }

    val downloadEnabled =
        downloadState.ffmpegState ==
            FfmpegReadyState.READY &&
            !downloadState
                .isPreparingDownload

    val historyCount =
        downloadState.activeJobs.size +
            downloadState.recentJobs.size

    fun showResults() {
        selectedTab =
            InboxTab.RESULTS

        onSelectSection(
            SearchDownloadSection.RESULTS
        )
    }

    fun showDuplicates() {
        selectedTab =
            InboxTab.DUPLICATES

        onSelectSection(
            SearchDownloadSection.RESULTS
        )
    }

    fun showHistory() {
        selectedTab =
            InboxTab.HISTORY

        onSelectSection(
            SearchDownloadSection.QUEUE
        )
    }

    fun searchFromInbox() {
        showResults()
        onSearch()
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(
                    start = 18.dp,
                    top = 14.dp,
                    end = 18.dp
                )
    ) {
        InboxHeader()

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        PrototypeInboxSearchField(
            query =
                searchState.query,
            isSearching =
                searchState.isSearching ||
                    searchState
                        .isSearchingReference,
            onQueryChange =
                onQueryChange,
            onSearch = {
                searchFromInbox()
            },
            onClear =
                onClearSearch
        )

        Spacer(
            modifier =
                Modifier.height(14.dp)
        )

        PrototypeInboxTabs(
            selectedTab =
                selectedTab,
            resultCount =
                searchState.results.size,
            duplicateCount =
                searchState
                    .referenceMatches
                    .size,
            historyCount =
                historyCount,
            onResults = {
                showResults()
            },
            onDuplicates = {
                showDuplicates()
            },
            onHistory = {
                showHistory()
            }
        )

        if (
            downloadState
                .isPreparingDownload
        ) {
            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            PreparingDownloadNotice(
                message =
                    downloadState
                        .preparingMessage
                        ?: "Đang chuẩn bị tải..."
            )
        }

        if (
            downloadState.ffmpegState ==
            FfmpegReadyState.FAILED
        ) {
            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            FfmpegErrorNotice(
                message =
                    downloadState
                        .ffmpegMessage,
                onRetry =
                    onRetryFfmpeg
            )
        }

        Spacer(
            modifier =
                Modifier.height(18.dp)
        )

        AnimatedContent(
            targetState =
                selectedTab,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            transitionSpec = {
                val movingForward =
                    targetState.ordinal >
                        initialState.ordinal

                if (movingForward) {
                    (
                        slideInHorizontally(
                            animationSpec =
                                tween(
                                    durationMillis =
                                        320,
                                    easing =
                                        FastOutSlowInEasing
                                ),
                            initialOffsetX = {
                                it / 4
                            }
                        ) +
                            fadeIn(
                                animationSpec =
                                    tween(220)
                            )
                    ).togetherWith(
                        slideOutHorizontally(
                            animationSpec =
                                tween(
                                    durationMillis =
                                        260,
                                    easing =
                                        FastOutSlowInEasing
                                ),
                            targetOffsetX = {
                                -it / 5
                            }
                        ) +
                            fadeOut(
                                animationSpec =
                                    tween(180)
                            )
                    )
                } else {
                    (
                        slideInHorizontally(
                            animationSpec =
                                tween(
                                    durationMillis =
                                        320,
                                    easing =
                                        FastOutSlowInEasing
                                ),
                            initialOffsetX = {
                                -it / 4
                            }
                        ) +
                            fadeIn(
                                animationSpec =
                                    tween(220)
                            )
                    ).togetherWith(
                        slideOutHorizontally(
                            animationSpec =
                                tween(
                                    durationMillis =
                                        260,
                                    easing =
                                        FastOutSlowInEasing
                                ),
                            targetOffsetX = {
                                it / 5
                            }
                        ) +
                            fadeOut(
                                animationSpec =
                                    tween(180)
                            )
                    )
                }
            },
            label =
                "inbox-tab-content"
        ) { tab ->
            when (tab) {
                InboxTab.RESULTS -> {
                    SearchResultsContent(
                        state =
                            searchState,
                        downloadEnabled =
                            downloadEnabled,
                        bitrateKbps =
                            settings.bitrateKbps,
                        onSearch = {
                            searchFromInbox()
                        },
                        onLoadMore =
                            onLoadMore,
                        onDownload =
                            onDownloadResult
                    )
                }

                InboxTab.DUPLICATES -> {
                    PrototypeDuplicateContent(
                        query =
                            searchState.query,
                        matches =
                            searchState
                                .referenceMatches,
                        isSearching =
                            searchState
                                .isSearchingReference,
                        message =
                            searchState
                                .referenceMessage,
                        onOpenEditor =
                            onOpenReferenceSong
                    )
                }

                InboxTab.HISTORY -> {
                    PrototypeHistoryContent(
                        state =
                            downloadState,
                        onCancelJob =
                            onCancelJob,
                        onRetryJob =
                            onRetryJob,
                        onClearHistory =
                            onClearHistory
                    )
                }
            }
        }
    }
}


@Composable
private fun InboxHeader() {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text =
                "SEARCH & DOWNLOAD",
            style =
                MaterialTheme
                    .typography
                    .labelSmall,
            fontWeight =
                FontWeight.Black,
            letterSpacing =
                1.35.sp,
            color =
                MaterialTheme
                    .colorScheme
                    .primary
        )

        Text(
            text =
                "Inbox",
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
    }
}


@Composable
private fun PrototypeInboxSearchField(
    query: String,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit
) {
    val focusManager =
        LocalFocusManager.current

    val keyboardController =
        LocalSoftwareKeyboardController
            .current

    fun submit() {
        if (
            query.isBlank() ||
            isSearching
        ) {
            return
        }

        focusManager.clearFocus(
            force = true
        )

        keyboardController?.hide()

        onSearch()
    }

    StableOutlinedTextField(
        value =
            query,
        onValueChange =
            onQueryChange,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(58.dp),
        singleLine =
            true,
        shape =
            RoundedCornerShape(18.dp),
        placeholder = {
            Text(
                text =
                    "Tìm trên YouTube hoặc dán link",
                maxLines =
                    1,
                overflow =
                    TextOverflow.Ellipsis
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
            when {
                isSearching -> {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(20.dp),
                        strokeWidth =
                            2.dp
                    )
                }

                query.isNotEmpty() -> {
                    IconButton(
                        onClick =
                            onClear
                    ) {
                        Icon(
                            imageVector =
                                Icons.Rounded.Close,
                            contentDescription =
                                "Xóa tìm kiếm"
                        )
                    }
                }
            }
        },
        keyboardOptions =
            KeyboardOptions(
                imeAction =
                    ImeAction.Search
            ),
        keyboardActions =
            KeyboardActions(
                onSearch = {
                    submit()
                }
            ),
        colors =
            OutlinedTextFieldDefaults
                .colors(
                    focusedTextColor =
                        MaterialTheme
                            .colorScheme
                            .onSurface,
                    unfocusedTextColor =
                        MaterialTheme
                            .colorScheme
                            .onSurface,
                    cursorColor =
                        MaterialTheme
                            .colorScheme
                            .primary,
                    focusedContainerColor =
                        MaterialTheme
                            .colorScheme
                            .surface
                            .copy(
                                alpha = 0.70f
                            ),
                    unfocusedContainerColor =
                        MaterialTheme
                            .colorScheme
                            .surface
                            .copy(
                                alpha = 0.70f
                            ),
                    focusedBorderColor =
                        MaterialTheme
                            .colorScheme
                            .primary
                            .copy(
                                alpha = 0.72f
                            ),
                    unfocusedBorderColor =
                        MaterialTheme
                            .colorScheme
                            .outline
                            .copy(
                                alpha = 0.32f
                            ),
                    focusedPlaceholderColor =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                    unfocusedPlaceholderColor =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                    focusedLeadingIconColor =
                        MaterialTheme
                            .colorScheme
                            .primary,
                    unfocusedLeadingIconColor =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                    focusedTrailingIconColor =
                        MaterialTheme
                            .colorScheme
                            .primary,
                    unfocusedTrailingIconColor =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
    )
}


@Composable
private fun PrototypeInboxTabs(
    selectedTab: InboxTab,
    resultCount: Int,
    duplicateCount: Int,
    historyCount: Int,
    onResults: () -> Unit,
    onDuplicates: () -> Unit,
    onHistory: () -> Unit
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(58.dp),
        shape =
            RoundedCornerShape(19.dp),
        color =
            MaterialTheme
                .colorScheme
                .surface
                .copy(
                    alpha = 0.58f
                ),
        border =
            BorderStroke(
                1.dp,
                MaterialTheme
                    .colorScheme
                    .outline
                    .copy(
                        alpha = 0.23f
                    )
            )
    ) {
        BoxWithConstraints(
            modifier =
                Modifier.fillMaxSize()
        ) {
            val slotWidth =
                maxWidth / 3

            val indicatorOffset by
                animateDpAsState(
                    targetValue =
                        slotWidth *
                            selectedTab.ordinal,
                    animationSpec =
                        tween(
                            durationMillis =
                                340,
                            easing =
                                FastOutSlowInEasing
                        ),
                    label =
                        "inbox-tab-indicator"
                )

            Box(
                modifier =
                    Modifier
                        .offset(
                            x =
                                indicatorOffset
                        )
                        .width(slotWidth)
                        .fillMaxHeight()
                        .padding(4.dp)
                        .clip(
                            RoundedCornerShape(
                                15.dp
                            )
                        )
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme
                                        .colorScheme
                                        .primary
                                        .copy(
                                            alpha =
                                                0.86f
                                        ),
                                    MaterialTheme
                                        .colorScheme
                                        .secondary
                                        .copy(
                                            alpha =
                                                0.76f
                                        )
                                )
                            )
                        )
            )

            Row(
                modifier =
                    Modifier.fillMaxSize()
            ) {
                PrototypeTabLabel(
                    label =
                        "Kết quả",
                    count =
                        resultCount,
                    selected =
                        selectedTab ==
                            InboxTab.RESULTS,
                    modifier =
                        Modifier.weight(1f),
                    onClick =
                        onResults
                )

                PrototypeTabLabel(
                    label =
                        "Trùng",
                    count =
                        duplicateCount,
                    selected =
                        selectedTab ==
                            InboxTab.DUPLICATES,
                    modifier =
                        Modifier.weight(1f),
                    onClick =
                        onDuplicates
                )

                PrototypeTabLabel(
                    label =
                        "Lịch sử",
                    count =
                        historyCount,
                    selected =
                        selectedTab ==
                            InboxTab.HISTORY,
                    modifier =
                        Modifier.weight(1f),
                    onClick =
                        onHistory
                )
            }
        }
    }
}


@Composable
private fun PrototypeTabLabel(
    label: String,
    count: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier =
            modifier
                .fillMaxHeight()
                .appPressable(
                    pressedScale =
                        0.96f,
                    haptic =
                        true,
                    onClick =
                        onClick
                ),
        contentAlignment =
            Alignment.Center
    ) {
        Row(
            horizontalArrangement =
                Arrangement.spacedBy(6.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text =
                    label,
                style =
                    MaterialTheme
                        .typography
                        .labelMedium,
                fontWeight =
                    if (selected) {
                        FontWeight.Black
                    } else {
                        FontWeight.Bold
                    },
                color =
                    if (selected) {
                        Color.White
                    } else {
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                    }
            )

            Surface(
                shape =
                    CircleShape,
                color =
                    if (selected) {
                        Color.White.copy(
                            alpha = 0.94f
                        )
                    } else {
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant
                            .copy(
                                alpha = 0.72f
                            )
                    }
            ) {
                Box(
                    modifier =
                        Modifier.size(20.dp),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text =
                            count.toString(),
                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,
                        fontWeight =
                            FontWeight.Black,
                        color =
                            if (selected) {
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
}


@Composable
private fun PrototypeDuplicateContent(
    query: String,
    matches:
        List<ReferenceSongMatch>,
    isSearching: Boolean,
    message: String?,
    onOpenEditor:
        (ReferenceSongMatch) -> Unit
) {
    LazyColumn(
        modifier =
            Modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(
                bottom = 28.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(13.dp)
    ) {
        item(
            key = "duplicate-header"
        ) {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        text =
                            "Có thể đã có",
                        modifier =
                            Modifier.weight(1f),
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                        fontWeight =
                            FontWeight.Black,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onBackground
                    )

                    Surface(
                        shape =
                            RoundedCornerShape(
                                99.dp
                            ),
                        color =
                            MaterialTheme
                                .colorScheme
                                .tertiary
                                .copy(
                                    alpha = 0.16f
                                ),
                        border =
                            BorderStroke(
                                1.dp,
                                MaterialTheme
                                    .colorScheme
                                    .tertiary
                                    .copy(
                                        alpha =
                                            0.42f
                                    )
                            )
                    ) {
                        Text(
                            text =
                                "${matches.size} kết quả",
                            modifier =
                                Modifier.padding(
                                    horizontal = 9.dp,
                                    vertical = 5.dp
                                ),
                            style =
                                MaterialTheme
                                    .typography
                                    .labelSmall,
                            fontWeight =
                                FontWeight.Bold,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .tertiary
                        )
                    }
                }

                Text(
                    text =
                        "File trong Library có độ tương đồng cao.",
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

        if (isSearching) {
            item(
                key = "duplicate-progress"
            ) {
                LinearProgressIndicator(
                    modifier =
                        Modifier.fillMaxWidth()
                )
            }
        }

        when {
            query.isBlank() -> {
                item(
                    key = "duplicate-no-query"
                ) {
                    DuplicateEmptyState(
                        title =
                            "Chưa có từ khóa",
                        description =
                            "Tìm một bài hát để GetMP3 đối chiếu với Library."
                    )
                }
            }

            matches.isNotEmpty() -> {
                items(
                    items =
                        matches,
                    key =
                        ReferenceSongMatch::uri
                ) { match ->
                    PrototypeDuplicateCard(
                        match =
                            match,
                        onOpenEditor = {
                            onOpenEditor(
                                match
                            )
                        }
                    )
                }
            }

            !isSearching -> {
                item(
                    key = "duplicate-empty"
                ) {
                    DuplicateEmptyState(
                        title =
                            "Không thấy bài trùng",
                        description =
                            message
                                ?.takeIf(
                                    String::isNotBlank
                                )
                                ?: "Không có file đủ giống với “$query”."
                    )
                }
            }
        }
    }
}


@Composable
private fun PrototypeDuplicateCard(
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
                Color(0xFF28C7C5)

            match.score >= 0.84 ->
                Color(0xFF6D90FF)

            else ->
                Color(0xFFFFB64A)
        }

    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(23.dp),
        color =
            MaterialTheme
                .colorScheme
                .surface
                .copy(
                    alpha = 0.86f
                ),
        border =
            BorderStroke(
                1.dp,
                accent.copy(
                    alpha = 0.42f
                )
            )
    ) {
        Column(
            modifier =
                Modifier.padding(13.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment =
                    Alignment.Top
            ) {
                DuplicateArtwork(
                    match =
                        match,
                    accent =
                        accent
                )

                Spacer(
                    modifier =
                        Modifier.width(11.dp)
                )

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {
                    Text(
                        text =
                            match.title,
                        maxLines =
                            2,
                        overflow =
                            TextOverflow.Ellipsis,
                        style =
                            MaterialTheme
                                .typography
                                .titleSmall,
                        fontWeight =
                            FontWeight.Black,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurface
                    )

                    Spacer(
                        modifier =
                            Modifier.height(2.dp)
                    )

                    Text(
                        text =
                            match.artist
                                .ifBlank {
                                    "Chưa xác định Artist"
                                },
                        maxLines =
                            1,
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

                    if (
                        match.durationSeconds !=
                        null
                    ) {
                        Spacer(
                            modifier =
                                Modifier.height(2.dp)
                        )

                        Text(
                            text =
                                formatInlineDuration(
                                    match.durationSeconds
                                ),
                            style =
                                MaterialTheme
                                    .typography
                                    .labelSmall,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                                    .copy(
                                        alpha = 0.72f
                                    )
                        )
                    }
                }

                Surface(
                    shape =
                        RoundedCornerShape(
                            13.dp
                        ),
                    color =
                        accent.copy(
                            alpha = 0.14f
                        ),
                    border =
                        BorderStroke(
                            1.dp,
                            accent.copy(
                                alpha = 0.42f
                            )
                        )
                ) {
                    Column(
                        modifier =
                            Modifier.padding(
                                horizontal = 9.dp,
                                vertical = 6.dp
                            ),
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {
                        Text(
                            text =
                                "$percent%",
                            style =
                                MaterialTheme
                                    .typography
                                    .labelLarge,
                            fontWeight =
                                FontWeight.Black,
                            color =
                                accent
                        )

                        Text(
                            text =
                                badge,
                            style =
                                MaterialTheme
                                    .typography
                                    .labelSmall,
                            color =
                                accent
                        )
                    }
                }
            }

            Surface(
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    RoundedCornerShape(11.dp),
                color =
                    MaterialTheme
                        .colorScheme
                        .surfaceVariant
                        .copy(
                            alpha = 0.46f
                        )
            ) {
                Text(
                    text =
                        match.displayName,
                    modifier =
                        Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 7.dp
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

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(9.dp)
            ) {
                PrototypeSecondaryAction(
                    label =
                        "Nghe thử",
                    icon =
                        Icons.Rounded.PlayArrow,
                    modifier =
                        Modifier.weight(1f),
                    onClick = {
                        openInlineReferenceAudio(
                            context =
                                context,
                            uriText =
                                match.uri
                        )
                    }
                )

                PrototypePrimaryAction(
                    label =
                        "Sửa thẻ",
                    icon =
                        Icons.Rounded.Edit,
                    modifier =
                        Modifier.weight(1f),
                    onClick =
                        onOpenEditor
                )
            }
        }
    }
}


@Composable
private fun DuplicateArtwork(
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
        modifier =
            Modifier
                .size(72.dp)
                .clip(
                    RoundedCornerShape(
                        17.dp
                    )
                )
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(
                                alpha = 0.62f
                            ),
                            Color(0xFF173B67),
                            Color(0xFF35245E)
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
                Color.White.copy(
                    alpha = 0.92f
                )
        )

        if (coverFile != null) {
            AsyncImage(
                model =
                    coverFile,
                contentDescription =
                    "Ảnh ${match.title}",
                modifier =
                    Modifier.fillMaxSize(),
                contentScale =
                    ContentScale.Crop
            )
        }
    }
}


@Composable
private fun DuplicateEmptyState(
    title: String,
    description: String
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(22.dp),
        color =
            MaterialTheme
                .colorScheme
                .surface
                .copy(
                    alpha = 0.58f
                ),
        border =
            BorderStroke(
                1.dp,
                MaterialTheme
                    .colorScheme
                    .outline
                    .copy(
                        alpha = 0.20f
                    )
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    horizontal = 18.dp,
                    vertical = 25.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.Search,
                contentDescription =
                    null,
                modifier =
                    Modifier.size(28.dp),
                tint =
                    MaterialTheme
                        .colorScheme
                        .primary
            )

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
                    description,
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


@Composable
private fun PrototypeHistoryContent(
    state: DownloadScreenUiState,
    onCancelJob: (String) -> Unit,
    onRetryJob:
        (DownloadJobEntity) -> Unit,
    onClearHistory: () -> Unit
) {
    LazyColumn(
        modifier =
            Modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(
                bottom = 28.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        if (
            state.activeJobs.isEmpty() &&
            state.recentJobs.isEmpty()
        ) {
            item(
                key = "history-empty"
            ) {
                HistoryEmptyState()
            }

            return@LazyColumn
        }

        if (
            state.activeJobs.isNotEmpty()
        ) {
            item(
                key = "active-header"
            ) {
                HistoryHeader(
                    title =
                        "Đang tải",
                    count =
                        state.activeJobs.size,
                    subtitle =
                        "Tiến trình hiện tại"
                )
            }

            items(
                items =
                    state.activeJobs,
                key = {
                    it.id
                }
            ) { job ->
                ActiveDownloadCard(
                    job =
                        job,
                    onCancel = {
                        onCancelJob(
                            job.id
                        )
                    }
                )
            }
        }

        if (
            state.recentJobs.isNotEmpty()
        ) {
            item(
                key = "recent-header"
            ) {
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    HistoryHeader(
                        title =
                            "Gần đây",
                        count =
                            state.recentJobs.size,
                        subtitle =
                            "Lịch sử tải"
                    )

                    Spacer(
                        modifier =
                            Modifier.weight(1f)
                    )

                    TextButton(
                        onClick =
                            onClearHistory
                    ) {
                        Icon(
                            imageVector =
                                Icons.Rounded
                                    .DeleteSweep,
                            contentDescription =
                                null,
                            modifier =
                                Modifier.size(17.dp)
                        )

                        Text(
                            text =
                                "Xóa"
                        )
                    }
                }
            }

            items(
                items =
                    state.recentJobs
                        .take(30),
                key = {
                    it.id
                }
            ) { job ->
                RecentDownloadCard(
                    job =
                        job,
                    onRetry = {
                        onRetryJob(
                            job
                        )
                    }
                )
            }
        }
    }
}


@Composable
private fun HistoryHeader(
    title: String,
    count: Int,
    subtitle: String
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(2.dp)
    ) {
        Row(
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(7.dp)
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

            Surface(
                shape =
                    CircleShape,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary
                        .copy(
                            alpha = 0.18f
                        )
            ) {
                Text(
                    text =
                        count.toString(),
                    modifier =
                        Modifier.padding(
                            horizontal = 8.dp,
                            vertical = 3.dp
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,
                    fontWeight =
                        FontWeight.Black,
                    color =
                        MaterialTheme
                            .colorScheme
                            .primary
                )
            }
        }

        Text(
            text =
                subtitle,
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
private fun ActiveDownloadCard(
    job: DownloadJobEntity,
    onCancel: () -> Unit
) {
    val progress =
        job.overallProgress
            .coerceIn(
                0,
                100
            )

    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(22.dp),
        color =
            MaterialTheme
                .colorScheme
                .surface
                .copy(
                    alpha = 0.88f
                ),
        border =
            BorderStroke(
                1.dp,
                MaterialTheme
                    .colorScheme
                    .primary
                    .copy(
                        alpha = 0.34f
                    )
            )
    ) {
        Column(
            modifier =
                Modifier.padding(12.dp),
            verticalArrangement =
                Arrangement.spacedBy(11.dp)
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                HistoryArtwork(
                    job =
                        job
                )

                Spacer(
                    modifier =
                        Modifier.width(11.dp)
                )

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {
                    Text(
                        text =
                            job.title,
                        maxLines =
                            1,
                        overflow =
                            TextOverflow.Ellipsis,
                        style =
                            MaterialTheme
                                .typography
                                .titleSmall,
                        fontWeight =
                            FontWeight.Black
                    )

                    Text(
                        text =
                            job.artist
                                .ifBlank {
                                    "Không rõ Artist"
                                },
                        maxLines =
                            1,
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
                            job.statusMessage
                                ?.takeIf(
                                    String::isNotBlank
                                )
                                ?: historyStatusText(
                                    job.status
                                ),
                        maxLines =
                            1,
                        overflow =
                            TextOverflow.Ellipsis,
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

                Surface(
                    shape =
                        RoundedCornerShape(
                            12.dp
                        ),
                    color =
                        MaterialTheme
                            .colorScheme
                            .primary
                            .copy(
                                alpha = 0.15f
                            )
                ) {
                    Text(
                        text =
                            "$progress%",
                        modifier =
                            Modifier.padding(
                                horizontal = 9.dp,
                                vertical = 6.dp
                            ),
                        style =
                            MaterialTheme
                                .typography
                                .labelMedium,
                        fontWeight =
                            FontWeight.Black,
                        color =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(6.dp)
                )

                IconButton(
                    onClick =
                        onCancel
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Close,
                        contentDescription =
                            "Hủy tải"
                    )
                }
            }

            LinearProgressIndicator(
                progress = {
                    progress / 100f
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(
                            CircleShape
                        )
            )
        }
    }
}


@Composable
private fun RecentDownloadCard(
    job: DownloadJobEntity,
    onRetry: () -> Unit
) {
    val context =
        LocalContext.current

    val statusColor =
        historyStatusColor(
            job.status
        )

    val actionable =
        job.status ==
            DownloadStatus.COMPLETED ||
            job.status ==
            DownloadStatus.FAILED ||
            job.status ==
            DownloadStatus.CANCELLED

    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(21.dp),
        color =
            MaterialTheme
                .colorScheme
                .surface
                .copy(
                    alpha = 0.80f
                ),
        border =
            BorderStroke(
                1.dp,
                statusColor.copy(
                    alpha = 0.30f
                )
            )
    ) {
        Row(
            modifier =
                Modifier.padding(11.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            HistoryArtwork(
                job =
                    job
            )

            Spacer(
                modifier =
                    Modifier.width(11.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {
                Text(
                    text =
                        job.title,
                    maxLines =
                        1,
                    overflow =
                        TextOverflow.Ellipsis,
                    style =
                        MaterialTheme
                            .typography
                            .titleSmall,
                    fontWeight =
                        FontWeight.Black
                )

                Text(
                    text =
                        job.artist
                            .ifBlank {
                                "Không rõ Artist"
                            },
                    maxLines =
                        1,
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

                Spacer(
                    modifier =
                        Modifier.height(3.dp)
                )

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(5.dp),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector =
                            when (
                                job.status
                            ) {
                                DownloadStatus.COMPLETED ->
                                    Icons.Rounded
                                        .CheckCircle

                                DownloadStatus.FAILED ->
                                    Icons.Rounded
                                        .ErrorOutline

                                DownloadStatus.CANCELLED ->
                                    Icons.Rounded.Close

                                else ->
                                    Icons.Rounded.History
                            },
                        contentDescription =
                            null,
                        modifier =
                            Modifier.size(14.dp),
                        tint =
                            statusColor
                    )

                    Text(
                        text =
                            historyStatusText(
                                job.status
                            ),
                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,
                        fontWeight =
                            FontWeight.Bold,
                        color =
                            statusColor
                    )
                }
            }

            if (actionable) {
                Surface(
                    modifier =
                        Modifier
                            .size(44.dp)
                            .appPressable(
                                enabled =
                                    when (
                                        job.status
                                    ) {
                                        DownloadStatus.COMPLETED ->
                                            !job.outputUri
                                                .isNullOrBlank()

                                        else ->
                                            true
                                    },
                                pressedScale =
                                    0.91f,
                                haptic =
                                    true,
                                onClick = {
                                    when (
                                        job.status
                                    ) {
                                        DownloadStatus.COMPLETED -> {
                                            openHistoryAudio(
                                                context =
                                                    context,
                                                outputUri =
                                                    job.outputUri
                                            )
                                        }

                                        DownloadStatus.FAILED,
                                        DownloadStatus.CANCELLED -> {
                                            onRetry()
                                        }

                                        else ->
                                            Unit
                                    }
                                }
                            ),
                    shape =
                        RoundedCornerShape(14.dp),
                    color =
                        statusColor.copy(
                            alpha = 0.16f
                        ),
                    border =
                        BorderStroke(
                            1.dp,
                            statusColor.copy(
                                alpha = 0.35f
                            )
                        )
                ) {
                    Box(
                        modifier =
                            Modifier.fillMaxSize(),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Icon(
                            imageVector =
                                if (
                                    job.status ==
                                    DownloadStatus.COMPLETED
                                ) {
                                    Icons.Rounded.PlayArrow
                                } else {
                                    Icons.Rounded.Refresh
                                },
                            contentDescription =
                                null,
                            modifier =
                                Modifier.size(20.dp),
                            tint =
                                statusColor
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun HistoryArtwork(
    job: DownloadJobEntity
) {
    val thumbnail =
        job.thumbnailUrl
            ?.takeIf(
                String::isNotBlank
            )

    Box(
        modifier =
            Modifier
                .size(62.dp)
                .clip(
                    RoundedCornerShape(
                        16.dp
                    )
                )
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF163F70),
                            Color(0xFF202C58),
                            Color(0xFF402963)
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
                Color.White.copy(
                    alpha = 0.86f
                )
        )

        if (thumbnail != null) {
            AsyncImage(
                model =
                    thumbnail,
                contentDescription =
                    "Ảnh ${job.title}",
                modifier =
                    Modifier.fillMaxSize(),
                contentScale =
                    ContentScale.Crop
            )
        }
    }
}


@Composable
private fun HistoryEmptyState() {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(22.dp),
        color =
            MaterialTheme
                .colorScheme
                .surface
                .copy(
                    alpha = 0.58f
                ),
        border =
            BorderStroke(
                1.dp,
                MaterialTheme
                    .colorScheme
                    .outline
                    .copy(
                        alpha = 0.20f
                    )
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    horizontal = 18.dp,
                    vertical = 28.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.History,
                contentDescription =
                    null,
                modifier =
                    Modifier.size(30.dp),
                tint =
                    MaterialTheme
                        .colorScheme
                        .primary
            )

            Text(
                text =
                    "Chưa có lịch sử tải",
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                fontWeight =
                    FontWeight.Black
            )

            Text(
                text =
                    "Các bài đang tải và đã xử lý sẽ xuất hiện ở đây.",
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


@Composable
private fun PrototypeSecondaryAction(
    label: String,
    icon:
        androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier =
            modifier
                .height(44.dp)
                .appPressable(
                    pressedScale =
                        0.96f,
                    haptic =
                        true,
                    onClick =
                        onClick
                ),
        shape =
            RoundedCornerShape(14.dp),
        color =
            MaterialTheme
                .colorScheme
                .surfaceVariant
                .copy(
                    alpha = 0.34f
                ),
        border =
            BorderStroke(
                1.dp,
                MaterialTheme
                    .colorScheme
                    .outline
                    .copy(
                        alpha = 0.34f
                    )
            )
    ) {
        Row(
            modifier =
                Modifier.fillMaxSize(),
            horizontalArrangement =
                Arrangement.Center,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Icon(
                imageVector =
                    icon,
                contentDescription =
                    null,
                modifier =
                    Modifier.size(17.dp)
            )

            Spacer(
                modifier =
                    Modifier.width(5.dp)
            )

            Text(
                text =
                    label,
                style =
                    MaterialTheme
                        .typography
                        .labelMedium,
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}


@Composable
private fun PrototypePrimaryAction(
    label: String,
    icon:
        androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier =
            modifier
                .height(44.dp)
                .clip(
                    RoundedCornerShape(
                        14.dp
                    )
                )
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF72C8FF),
                            Color(0xFF789DFF),
                            Color(0xFFA17FFF)
                        )
                    )
                )
                .appPressable(
                    pressedScale =
                        0.96f,
                    haptic =
                        true,
                    onClick =
                        onClick
                ),
        contentAlignment =
            Alignment.Center
    ) {
        Row(
            horizontalArrangement =
                Arrangement.spacedBy(6.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Icon(
                imageVector =
                    icon,
                contentDescription =
                    null,
                modifier =
                    Modifier.size(17.dp),
                tint =
                    Color(0xFF071526)
            )

            Text(
                text =
                    label,
                style =
                    MaterialTheme
                        .typography
                        .labelMedium,
                fontWeight =
                    FontWeight.Black,
                color =
                    Color(0xFF071526)
            )
        }
    }
}


@Composable
private fun PreparingDownloadNotice(
    message: String
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(14.dp),
        color =
            MaterialTheme
                .colorScheme
                .primaryContainer
                .copy(
                    alpha = 0.72f
                )
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 10.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier =
                    Modifier.size(18.dp),
                strokeWidth =
                    2.dp
            )

            Spacer(
                modifier =
                    Modifier.width(9.dp)
            )

            Text(
                text =
                    message,
                modifier =
                    Modifier.weight(1f),
                style =
                    MaterialTheme
                        .typography
                        .bodySmall
            )
        }
    }
}


@Composable
private fun FfmpegErrorNotice(
    message: String,
    onRetry: () -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(14.dp),
        color =
            MaterialTheme
                .colorScheme
                .errorContainer
                .copy(
                    alpha = 0.76f
                )
    ) {
        Row(
            modifier =
                Modifier.padding(
                    start = 12.dp,
                    top = 6.dp,
                    end = 4.dp,
                    bottom = 6.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text =
                    message,
                modifier =
                    Modifier.weight(1f),
                maxLines =
                    2,
                overflow =
                    TextOverflow.Ellipsis,
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onErrorContainer
            )

            TextButton(
                onClick =
                    onRetry
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.Refresh,
                    contentDescription =
                        null,
                    modifier =
                        Modifier.size(17.dp)
                )

                Text(
                    text =
                        "Thử lại"
                )
            }
        }
    }
}


private fun historyStatusText(
    status: DownloadStatus
): String {
    return when (status) {
        DownloadStatus.QUEUED ->
            "Đang chờ"

        DownloadStatus.EXTRACTING ->
            "Đang đọc thông tin"

        DownloadStatus.DOWNLOADING ->
            "Đang tải"

        DownloadStatus.CONVERTING ->
            "Đang chuyển MP3"

        DownloadStatus.TAGGING ->
            "Đang ghi thẻ"

        DownloadStatus.SAVING ->
            "Đang lưu"

        DownloadStatus.COMPLETED ->
            "Đã tải"

        DownloadStatus.FAILED ->
            "Lỗi tải"

        DownloadStatus.CANCELLED ->
            "Đã hủy"
    }
}


@Composable
private fun historyStatusColor(
    status: DownloadStatus
): Color {
    return when (status) {
        DownloadStatus.COMPLETED ->
            Color(0xFF38D79F)

        DownloadStatus.FAILED ->
            Color(0xFFFF6E86)

        DownloadStatus.CANCELLED ->
            MaterialTheme
                .colorScheme
                .onSurfaceVariant

        else ->
            MaterialTheme
                .colorScheme
                .primary
    }
}


private fun formatInlineDuration(
    seconds: Long?
): String {
    val safe =
        seconds
            ?.coerceAtLeast(0L)
            ?: return ""

    return "%d:%02d".format(
        safe / 60L,
        safe % 60L
    )
}


private fun openInlineReferenceAudio(
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
    } catch (
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
}


private fun openHistoryAudio(
    context: Context,
    outputUri: String?
) {
    val uriText =
        outputUri
            ?.takeIf(
                String::isNotBlank
            )
            ?: return

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
    } catch (
        _: ActivityNotFoundException
    ) {
        val fallback =
            Intent.createChooser(
                intent,
                "Chọn ứng dụng phát nhạc"
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
}