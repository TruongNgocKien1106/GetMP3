package com.ngoctien.getmp3.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ngoctien.getmp3.data.DownloadJobEntity
import com.ngoctien.getmp3.note.ReferenceSongMatch
import com.ngoctien.getmp3.settings.AppSettings
import com.ngoctien.getmp3.ui.design.appPressable
import com.ngoctien.getmp3.viewmodel.DownloadScreenUiState
import com.ngoctien.getmp3.viewmodel.FfmpegReadyState
import com.ngoctien.getmp3.viewmodel.SearchDownloadSection
import com.ngoctien.getmp3.viewmodel.YouTubeSearchUiState
import com.ngoctien.getmp3.youtube.YouTubeSearchResult

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
    val downloadEnabled =
        downloadState.ffmpegState ==
            FfmpegReadyState.READY &&
            !downloadState
                .isPreparingDownload

    var showReferenceMatches by
        remember {
            mutableStateOf(false)
        }

    LaunchedEffect(
        searchState.query
    ) {
        showReferenceMatches =
            false
    }

    val historyCount =
        downloadState.activeJobs.size +
            downloadState.recentJobs.size

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
            onSearch =
                onSearch,
            onClear =
                onClearSearch
        )

        Spacer(
            modifier =
                Modifier.height(14.dp)
        )

        PrototypeInboxTabs(
            selectedSection =
                searchState.selectedSection,
            duplicateSelected =
                showReferenceMatches,
            resultCount =
                searchState.results.size,
            duplicateCount =
                searchState
                    .referenceMatches
                    .size,
            historyCount =
                historyCount,
            onResults = {
                showReferenceMatches =
                    false

                onSelectSection(
                    SearchDownloadSection.RESULTS
                )
            },
            onDuplicates = {
                if (
                    searchState
                        .referenceMatches
                        .isNotEmpty()
                ) {
                    showReferenceMatches =
                        true
                }
            },
            onHistory = {
                showReferenceMatches =
                    false

                onSelectSection(
                    SearchDownloadSection.QUEUE
                )
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
                    downloadState.ffmpegMessage,
                onRetry =
                    onRetryFfmpeg
            )
        }

        Spacer(
            modifier =
                Modifier.height(18.dp)
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
        ) {
            when (
                searchState.selectedSection
            ) {
                SearchDownloadSection.RESULTS -> {
                    SearchResultsContent(
                        state =
                            searchState,
                        downloadEnabled =
                            downloadEnabled,
                        onSearch =
                            onSearch,
                        onLoadMore =
                            onLoadMore,
                        onDownload =
                            onDownloadResult
                    )
                }

                SearchDownloadSection.QUEUE -> {
                    DownloadQueueContent(
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

    if (
        showReferenceMatches &&
        searchState
            .referenceMatches
            .isNotEmpty()
    ) {
        ReferenceMatchesSheet(
            query =
                searchState.query,
            matches =
                searchState.referenceMatches,
            onDismiss = {
                showReferenceMatches =
                    false
            },
            onOpenReferenceSong =
                onOpenReferenceSong
        )
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
        LocalSoftwareKeyboardController.current

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
            OutlinedTextFieldDefaults.colors(
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
    selectedSection: SearchDownloadSection,
    duplicateSelected: Boolean,
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
                .height(56.dp),
        shape =
            RoundedCornerShape(18.dp),
        color =
            MaterialTheme
                .colorScheme
                .surface
                .copy(
                    alpha = 0.56f
                ),
        border =
            BorderStroke(
                1.dp,
                MaterialTheme
                    .colorScheme
                    .outline
                    .copy(
                        alpha = 0.24f
                    )
            )
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(5.dp),
            horizontalArrangement =
                Arrangement.spacedBy(4.dp)
        ) {
            PrototypeInboxTab(
                label =
                    "Kết quả",
                count =
                    resultCount,
                selected =
                    selectedSection ==
                        SearchDownloadSection.RESULTS &&
                        !duplicateSelected,
                enabled =
                    true,
                modifier =
                    Modifier.weight(1f),
                onClick =
                    onResults
            )

            PrototypeInboxTab(
                label =
                    "Trùng",
                count =
                    duplicateCount,
                selected =
                    duplicateSelected,
                enabled =
                    duplicateCount > 0,
                modifier =
                    Modifier.weight(1f),
                onClick =
                    onDuplicates
            )

            PrototypeInboxTab(
                label =
                    "Lịch sử",
                count =
                    historyCount,
                selected =
                    selectedSection ==
                        SearchDownloadSection.QUEUE &&
                        !duplicateSelected,
                enabled =
                    true,
                modifier =
                    Modifier.weight(1f),
                onClick =
                    onHistory
            )
        }
    }
}


@Composable
private fun PrototypeInboxTab(
    label: String,
    count: Int,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val selectedContainer =
        MaterialTheme
            .colorScheme
            .primary
            .copy(
                alpha = 0.13f
            )

    val labelColor =
        when {
            !enabled ->
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
                    .copy(
                        alpha = 0.42f
                    )

            selected ->
                MaterialTheme
                    .colorScheme
                    .onSurface

            else ->
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        }

    Box(
        modifier =
            modifier
                .fillMaxHeight()
                .appPressable(
                    enabled =
                        enabled,
                    pressedScale =
                        0.96f,
                    haptic =
                        true,
                    onClick =
                        onClick
                )
    ) {
        if (selected) {
            Surface(
                modifier =
                    Modifier.fillMaxSize(),
                shape =
                    RoundedCornerShape(13.dp),
                color =
                    selectedContainer
            ) {}
        }

        Row(
            modifier =
                Modifier.align(
                    Alignment.Center
                ),
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
                    labelColor
            )

            Surface(
                shape =
                    CircleShape,
                color =
                    if (selected) {
                        MaterialTheme
                            .colorScheme
                            .onSurface
                    } else {
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant
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
                                    .surface
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
                    Modifier.size(9.dp)
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