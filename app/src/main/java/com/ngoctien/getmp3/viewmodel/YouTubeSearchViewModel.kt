package com.ngoctien.getmp3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ngoctien.getmp3.note.ReferenceSongMatch
import com.ngoctien.getmp3.note.ReferenceSongSearchRepository
import com.ngoctien.getmp3.settings.AppSettingsRepository
import com.ngoctien.getmp3.youtube.YouTubeSearchRepository
import com.ngoctien.getmp3.youtube.YouTubeSearchResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout

enum class SearchDownloadSection {
    RESULTS,
    QUEUE
}

data class YouTubeSearchUiState(
    val query: String = "",
    val results: List<YouTubeSearchResult> = emptyList(),
    val referenceMatches: List<ReferenceSongMatch> = emptyList(),
    val isSearching: Boolean = false,
    val isSearchingReference: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val referenceMessage: String? = null,
    val loadMoreError: String? = null,
    val hasLibraryFolder: Boolean = false,
    val hasMore: Boolean = false,
    val nextOffset: Int = 0,
    val selectedSection:
        SearchDownloadSection =
            SearchDownloadSection.RESULTS
)

class YouTubeSearchViewModel(
    application: Application
) : AndroidViewModel(application) {

    companion object {
        private const val PAGE_SIZE = 10
        private const val SEARCH_TIMEOUT = 35_000L
        private const val REFERENCE_SEARCH_TIMEOUT = 15_000L
    }

    private val repository =
        YouTubeSearchRepository(application)

    private val referenceRepository =
        ReferenceSongSearchRepository(application)

    private val settingsRepository =
        AppSettingsRepository(application)

    private val mutableUiState =
        MutableStateFlow(
            YouTubeSearchUiState(
                hasLibraryFolder =
                    settingsRepository
                        .getSettings()
                        .hasLibraryFolder
            )
        )

    val uiState:
        StateFlow<YouTubeSearchUiState> =
            mutableUiState.asStateFlow()

    private var initialSearchJob: Job? = null
    private var loadMoreJob: Job? = null
    private var generation = 0L

    fun setQuery(value: String) {
        mutableUiState.update {
            it.copy(
                query = value,
                errorMessage = null,
                referenceMessage = null,
                loadMoreError = null
            )
        }
    }

    fun search() {
        val cleanQuery =
            normalizeQuery(
                mutableUiState.value.query
            )

        if (cleanQuery.isBlank()) {
            mutableUiState.update {
                it.copy(
                    results = emptyList(),
                    referenceMatches = emptyList(),
                    isSearching = false,
                    isSearchingReference = false,
                    isLoadingMore = false,
                    hasMore = false,
                    nextOffset = 0,
                    errorMessage =
                        "Hãy nhập tên bài hát hoặc Artist",
                    referenceMessage = null,
                    loadMoreError = null,
                    selectedSection =
                        SearchDownloadSection.RESULTS
                )
            }

            return
        }

        startInitialSearch(cleanQuery)
    }

    fun searchFromText(query: String) {
        val cleanQuery =
            normalizeQuery(query)

        if (cleanQuery.isBlank()) {
            mutableUiState.update {
                it.copy(
                    query = "",
                    results = emptyList(),
                    referenceMatches = emptyList(),
                    errorMessage =
                        "Tên bài hát không hợp lệ",
                    referenceMessage = null,
                    hasMore = false,
                    nextOffset = 0,
                    selectedSection =
                        SearchDownloadSection.RESULTS
                )
            }

            return
        }

        startInitialSearch(cleanQuery)
    }

    fun loadMore() {
        val state =
            mutableUiState.value

        if (
            state.isSearching ||
            state.isLoadingMore ||
            !state.hasMore ||
            state.query.isBlank()
        ) {
            return
        }

        val query =
            normalizeQuery(state.query)

        val offset =
            state.nextOffset

        val expectedGeneration =
            generation

        loadMoreJob =
            viewModelScope.launch {
                mutableUiState.update {
                    it.copy(
                        isLoadingMore = true,
                        loadMoreError = null
                    )
                }

                try {
                    val page =
                        withTimeout(
                            SEARCH_TIMEOUT
                        ) {
                            repository.searchPage(
                                query = query,
                                offset = offset,
                                limit = PAGE_SIZE
                            )
                        }

                    if (
                        expectedGeneration !=
                        generation
                    ) {
                        return@launch
                    }

                    mutableUiState.update { current ->
                        current.copy(
                            results =
                                (
                                    current.results +
                                        page.items
                                    )
                                    .distinctBy(
                                        YouTubeSearchResult::videoId
                                    ),
                            nextOffset =
                                page.nextOffset,
                            hasMore =
                                page.hasMore,
                            loadMoreError = null
                        )
                    }
                }
                catch (_: TimeoutCancellationException) {
                    updateLoadMoreError(
                        expectedGeneration,
                        "Tải thêm kết quả quá lâu. Hãy thử lại."
                    )
                }
                catch (exception: CancellationException) {
                    throw exception
                }
                catch (exception: Exception) {
                    updateLoadMoreError(
                        expectedGeneration,
                        exception.message
                            ?.takeIf(String::isNotBlank)
                            ?: "Không tải thêm được kết quả"
                    )
                }
                finally {
                    if (
                        expectedGeneration ==
                        generation
                    ) {
                        mutableUiState.update {
                            it.copy(
                                isLoadingMore = false
                            )
                        }
                    }
                }
            }
    }

    fun selectSection(
        section: SearchDownloadSection
    ) {
        mutableUiState.update {
            it.copy(
                selectedSection = section
            )
        }
    }

    fun showQueue() =
        selectSection(
            SearchDownloadSection.QUEUE
        )

    fun showResults() =
        selectSection(
            SearchDownloadSection.RESULTS
        )

    fun clearSearch() {
        initialSearchJob?.cancel()
        loadMoreJob?.cancel()

        generation += 1L

        mutableUiState.value =
            YouTubeSearchUiState(
                hasLibraryFolder =
                    settingsRepository
                        .getSettings()
                        .hasLibraryFolder
            )
    }

    private fun startInitialSearch(
        query: String
    ) {
        initialSearchJob?.cancel()
        loadMoreJob?.cancel()

        generation += 1L

        val expectedGeneration =
            generation

        val hasLibraryFolder =
            settingsRepository
                .getSettings()
                .hasLibraryFolder

        mutableUiState.update {
            it.copy(
                query = query,
                results = emptyList(),
                referenceMatches = emptyList(),
                isSearching = true,
                isSearchingReference =
                    hasLibraryFolder,
                isLoadingMore = false,
                errorMessage = null,
                referenceMessage =
                    if (hasLibraryFolder) {
                        null
                    }
                    else {
                        "Chưa chọn Library trong Cài đặt"
                    },
                loadMoreError = null,
                hasLibraryFolder =
                    hasLibraryFolder,
                hasMore = false,
                nextOffset = 0,
                selectedSection =
                    SearchDownloadSection.RESULTS
            )
        }

        initialSearchJob =
            viewModelScope.launch {
                supervisorScope {
                    launch {
                        searchReferenceFolder(
                            query = query,
                            expectedGeneration =
                                expectedGeneration,
                            hasLibraryFolder =
                                hasLibraryFolder
                        )
                    }

                    launch {
                        searchYouTube(
                            query = query,
                            expectedGeneration =
                                expectedGeneration
                        )
                    }
                }
            }
    }

    private suspend fun searchReferenceFolder(
        query: String,
        expectedGeneration: Long,
        hasLibraryFolder: Boolean
    ) {
        if (!hasLibraryFolder) return

        try {
            val matches =
                withTimeout(
                    REFERENCE_SEARCH_TIMEOUT
                ) {
                    referenceRepository.search(
                        rawQuery = query,
                        limit = 6
                    )
                }

            if (
                expectedGeneration !=
                generation
            ) {
                return
            }

            mutableUiState.update {
                it.copy(
                    referenceMatches = matches,
                    referenceMessage =
                        if (matches.isEmpty()) {
                            "Không thấy bài gần giống trong Library"
                        }
                        else {
                            null
                        }
                )
            }
        }
        catch (_: TimeoutCancellationException) {
            updateReferenceError(
                expectedGeneration,
                "Quét Library quá lâu. Hãy thử lại."
            )
        }
        catch (exception: CancellationException) {
            throw exception
        }
        catch (exception: Exception) {
            updateReferenceError(
                expectedGeneration,
                exception.message
                    ?.takeIf(String::isNotBlank)
                    ?: "Không đọc được Library"
            )
        }
        finally {
            if (
                expectedGeneration ==
                generation
            ) {
                mutableUiState.update {
                    it.copy(
                        isSearchingReference = false
                    )
                }
            }
        }
    }

    private suspend fun searchYouTube(
        query: String,
        expectedGeneration: Long
    ) {
        try {
            val page =
                withTimeout(
                    SEARCH_TIMEOUT
                ) {
                    repository.searchPage(
                        query = query,
                        offset = 0,
                        limit = PAGE_SIZE
                    )
                }

            if (
                expectedGeneration !=
                generation
            ) {
                return
            }

            mutableUiState.update {
                it.copy(
                    results = page.items,
                    nextOffset =
                        page.nextOffset,
                    hasMore =
                        page.hasMore,
                    errorMessage =
                        if (page.items.isEmpty()) {
                            "Không tìm thấy video phù hợp"
                        }
                        else {
                            null
                        }
                )
            }
        }
        catch (_: TimeoutCancellationException) {
            updateYoutubeError(
                expectedGeneration,
                "YouTube phản hồi quá lâu. Hãy thử lại."
            )
        }
        catch (exception: CancellationException) {
            throw exception
        }
        catch (exception: Exception) {
            updateYoutubeError(
                expectedGeneration,
                exception.message
                    ?.takeIf(String::isNotBlank)
                    ?: "Không tìm được video trên YouTube"
            )
        }
        finally {
            if (
                expectedGeneration ==
                generation
            ) {
                mutableUiState.update {
                    it.copy(
                        isSearching = false
                    )
                }
            }
        }
    }

    private fun updateReferenceError(
        expectedGeneration: Long,
        message: String
    ) {
        if (
            expectedGeneration ==
            generation
        ) {
            mutableUiState.update {
                it.copy(
                    referenceMessage = message
                )
            }
        }
    }

    private fun updateYoutubeError(
        expectedGeneration: Long,
        message: String
    ) {
        if (
            expectedGeneration ==
            generation
        ) {
            mutableUiState.update {
                it.copy(
                    errorMessage = message
                )
            }
        }
    }

    private fun updateLoadMoreError(
        expectedGeneration: Long,
        message: String
    ) {
        if (
            expectedGeneration ==
            generation
        ) {
            mutableUiState.update {
                it.copy(
                    loadMoreError = message
                )
            }
        }
    }

    private fun normalizeQuery(
        value: String
    ): String {
        return value
            .trim()
            .replace(
                Regex("""[\s\p{Zs}]+"""),
                " "
            )
    }

    override fun onCleared() {
        initialSearchJob?.cancel()
        loadMoreJob?.cancel()

        super.onCleared()
    }
}
