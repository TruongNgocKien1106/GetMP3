package com.ngoctien.getmp3.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ngoctien.getmp3.youtube.YouTubeSearchRepository
import com.ngoctien.getmp3.youtube.YouTubeSearchResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun YouTubeSearchSheet(
    initialQuery: String,
    onDismiss: () -> Unit,
    onDownload: (
        YouTubeSearchResult
    ) -> Unit
) {
    val context =
        LocalContext.current

    val repository =
        remember {
            YouTubeSearchRepository(
                context.applicationContext
            )
        }

    val coroutineScope =
        rememberCoroutineScope()

    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

    var query by
        remember(initialQuery) {
            mutableStateOf(
                initialQuery
            )
        }

    var results by
        remember {
            mutableStateOf<
                List<YouTubeSearchResult>
                >(
                emptyList()
            )
        }

    var isSearching by
        remember {
            mutableStateOf(false)
        }

    var errorMessage by
        remember {
            mutableStateOf<String?>(
                null
            )
        }

    var searchJob by
        remember {
            mutableStateOf<Job?>(
                null
            )
        }

    suspend fun performSearch(
        value: String
    ) {
        val cleanQuery =
            value
                .trim()
                .replace(
                    Regex("""\s+"""),
                    " "
                )

        if (cleanQuery.isBlank()) {
            results =
                emptyList()

            errorMessage =
                "Hãy nhập tên bài hát."

            return
        }

        isSearching = true
        errorMessage = null

        try {
            val newResults =
                withTimeout(
                    35_000L
                ) {
                    repository.search(
                        query =
                            cleanQuery,

                        limit = 10
                    )
                }

            results =
                newResults

            if (newResults.isEmpty()) {
                errorMessage =
                    "Không tìm thấy video phù hợp."
            }
        } catch (
            exception: CancellationException
        ) {
            throw exception
        } catch (
            exception: Exception
        ) {
            results =
                emptyList()

            errorMessage =
                exception.message
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: "Tìm kiếm YouTube thất bại."
        } finally {
            isSearching = false
        }
    }

    fun requestSearch() {
        searchJob?.cancel()

        searchJob =
            coroutineScope.launch {
                performSearch(query)
            }
    }

    LaunchedEffect(initialQuery) {
        performSearch(
            initialQuery
        )
    }

    ModalBottomSheet(
        onDismissRequest = {
            searchJob?.cancel()
            onDismiss()
        },

        sheetState =
            sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = 18.dp,
                    end = 18.dp,
                    bottom = 18.dp
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
                        Modifier.weight(1f)
                ) {
                    Text(
                        text =
                            "Tìm trên YouTube",

                        style =
                            MaterialTheme
                                .typography
                                .headlineSmall,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            "Chọn đúng video để đưa vào hàng đợi tải.",

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

                IconButton(
                    onClick = {
                        searchJob?.cancel()
                        onDismiss()
                    }
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Close,

                        contentDescription =
                            "Đóng"
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )

            OutlinedTextField(
                value =
                    query,

                onValueChange = {
                    query = it
                },

                modifier =
                    Modifier.fillMaxWidth(),

                singleLine = true,

                label = {
                    Text(
                        "Tên bài hát hoặc Artist"
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
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(
                                    22.dp
                                ),

                            strokeWidth =
                                2.dp
                        )
                    } else {
                        IconButton(
                            onClick =
                                ::requestSearch
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Rounded.Search,

                                contentDescription =
                                    "Tìm kiếm"
                            )
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
                            requestSearch()
                        }
                    )
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            when {
                isSearching &&
                    results.isEmpty() -> {
                    SearchingPanel()
                }

                errorMessage != null &&
                    results.isEmpty() -> {
                    SearchErrorPanel(
                        message =
                            errorMessage.orEmpty(),

                        onRetry =
                            ::requestSearch
                    )
                }

                results.isNotEmpty() -> {
                    Text(
                        text =
                            "${results.size} kết quả",

                        style =
                            MaterialTheme
                                .typography
                                .labelLarge,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(
                                min = 260.dp,
                                max = 600.dp
                            ),

                        verticalArrangement =
                            Arrangement.spacedBy(
                                10.dp
                            )
                    ) {
                        items(
                            items =
                                results,

                            key = {
                                it.videoId
                            }
                        ) { result ->
                            YouTubeResultItem(
                                result =
                                    result,

                                onDownload = {
                                    searchJob
                                        ?.cancel()

                                    onDownload(
                                        result
                                    )
                                }
                            )
                        }
                    }
                }

                else -> {
                    SearchingPanel()
                }
            }
        }
    }
}

@Composable
private fun SearchingPanel() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),

        contentAlignment =
            Alignment.Center
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Text(
                text =
                    "Đang tìm video...",

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SearchErrorPanel(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),

        contentAlignment =
            Alignment.Center
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.MusicNote,

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
                    message,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            TextButton(
                onClick =
                    onRetry
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.Refresh,

                    contentDescription =
                        null
                )

                Spacer(
                    modifier =
                        Modifier.width(6.dp)
                )

                Text("Thử lại")
            }
        }
    }
}

@Composable
private fun YouTubeResultItem(
    result: YouTubeSearchResult,
    onDownload: () -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                18.dp
            ),

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
                    horizontal = 14.dp,
                    vertical = 12.dp
                )
        ) {
            Row(
                verticalAlignment =
                    Alignment.Top
            ) {
                Surface(
                    modifier =
                        Modifier.size(48.dp),

                    shape =
                        RoundedCornerShape(
                            15.dp
                        ),

                    color =
                        MaterialTheme
                            .colorScheme
                            .primaryContainer
                ) {
                    Box(
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Icon(
                            imageVector =
                                Icons.Rounded
                                    .PlayCircle,

                            contentDescription =
                                null,

                            tint =
                                MaterialTheme
                                    .colorScheme
                                    .primary,

                            modifier =
                                Modifier.size(
                                    29.dp
                                )
                        )
                    }
                }

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
                            result.title,

                        maxLines = 3,

                        overflow =
                            TextOverflow.Ellipsis,

                        fontWeight =
                            FontWeight.SemiBold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    Text(
                        text =
                            result.channel
                                .ifBlank {
                                    "Không rõ kênh"
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
                }

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Surface(
                    shape =
                        RoundedCornerShape(
                            10.dp
                        ),

                    color =
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant
                ) {
                    Text(
                        text =
                            result.formattedDuration,

                        modifier =
                            Modifier.padding(
                                horizontal = 7.dp,
                                vertical = 4.dp
                            ),

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            HorizontalDivider(
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

                horizontalArrangement =
                    Arrangement.End
            ) {
                Button(
                    onClick =
                        onDownload
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Download,

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(
                                18.dp
                            )
                    )

                    Spacer(
                        modifier =
                            Modifier.width(6.dp)
                    )

                    Text("Tải")
                }
            }
        }
    }
}