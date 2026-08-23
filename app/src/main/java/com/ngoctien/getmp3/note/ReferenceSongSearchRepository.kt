package com.ngoctien.getmp3.note

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.DocumentsContract
import com.ngoctien.getmp3.storage.resolveSafDirectoryDocumentId
import com.ngoctien.getmp3.settings.AppSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.File
import kotlin.math.max

/** Một bài MP3 gần giống trong thư mục đối chiếu. */
data class ReferenceSongMatch(
    val uri: String,
    val treeUri: String,
    val displayName: String,
    val title: String,
    val artist: String,
    val score: Double,
    val titleScore: Double? = null,
    val combinedScore: Double? = null,
    val album: String? = null,
    val year: String? = null,
    val coverPath: String? = null,
    val durationSeconds: Long? = null
)

private data class ReferenceSongEntry(
    val uri: String,
    val treeUri: String,
    val displayName: String,
    val title: String,
    val artist: String,
    val normalizedTitle: String,
    val normalizedCombined: String
)

private data class ReferenceMediaDetails(
    val durationSeconds: Long?,
    val album: String?,
    val year: String?,
    val coverPath: String?
)

/**
 * Tìm bài gần giống trong thư mục đối chiếu.
 *
 * Repository chỉ quét các file MP3 nằm trực tiếp trong thư mục đã chọn.
 * Danh sách lớn vẫn được xếp hạng bằng tên file; chỉ các kết quả đứng đầu
 * mới được mở để đọc thời lượng và ảnh bìa.
 */
class ReferenceSongSearchRepository(
    context: Context
) {

    companion object {
        internal const val SEARCH_THRESHOLD = 0.55
        private const val MP3_MIME_TYPE = "audio/mpeg"
        private const val YIELD_EVERY = 250
        private const val MAX_CACHED_COVERS = 64
    }

    private val applicationContext =
        context.applicationContext

    private val resolver =
        applicationContext.contentResolver

    private val settingsRepository =
        AppSettingsRepository(
            applicationContext
        )

    private val indexMutex =
        Mutex()

    private val coverCacheDirectory =
        File(
            applicationContext.cacheDir,
            "reference_search_covers"
        )

    private var cachedSourceKey:
        String? = null

    private var cachedEntries:
        List<ReferenceSongEntry>? = null

    suspend fun search(
        rawQuery: String,
        limit: Int = 6
    ): List<ReferenceSongMatch> {
        val normalizedQuery =
            SongNameMatcher.normalizeTitle(
                rawQuery
            )

        if (normalizedQuery.isBlank()) {
            return emptyList()
        }

        val settings =
            settingsRepository.getSettings()

        val treeUriText =
            settings.compareTreeUri
                ?.takeIf(
                    String::isNotBlank
                )
                ?: return emptyList()

        val sourceKey =
            "$treeUriText|" +
                settings.compareIndexGeneratedAt

        val entries =
            ensureIndex(
                treeUriText = treeUriText,
                sourceKey = sourceKey
            )

        val rankedMatches =
            rankMatches(
                normalizedQuery =
                    normalizedQuery,

                entries =
                    entries,

                limit =
                    limit
            )

        return loadMediaDetails(
            matches = rankedMatches,
            sourceKey = sourceKey
        )
    }

    private suspend fun ensureIndex(
        treeUriText: String,
        sourceKey: String
    ): List<ReferenceSongEntry> =
        indexMutex.withLock {
            val current =
                cachedEntries

            if (
                current != null &&
                cachedSourceKey == sourceKey
            ) {
                return@withLock current
            }

            scanDirectChildren(
                treeUriText
            ).also { scanned ->
                cachedSourceKey =
                    sourceKey

                cachedEntries =
                    scanned
            }
        }

    private suspend fun scanDirectChildren(
        treeUriText: String
    ): List<ReferenceSongEntry> =
        withContext(
            Dispatchers.IO
        ) {
            val treeUri =
                Uri.parse(
                    treeUriText
                )

            val rootDocumentId =
                resolveSafDirectoryDocumentId(
                    treeUri
                )

            val childrenUri =
                DocumentsContract
                    .buildChildDocumentsUriUsingTree(
                        treeUri,
                        rootDocumentId
                    )

            val projection =
                arrayOf(
                    DocumentsContract.Document
                        .COLUMN_DOCUMENT_ID,

                    DocumentsContract.Document
                        .COLUMN_DISPLAY_NAME,

                    DocumentsContract.Document
                        .COLUMN_MIME_TYPE
                )

            val entries =
                ArrayList<
                    ReferenceSongEntry
                    >(5_000)

            var scannedMp3Count =
                0

            resolver.query(
                childrenUri,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                val idColumn =
                    cursor.getColumnIndexOrThrow(
                        DocumentsContract.Document
                            .COLUMN_DOCUMENT_ID
                    )

                val nameColumn =
                    cursor.getColumnIndexOrThrow(
                        DocumentsContract.Document
                            .COLUMN_DISPLAY_NAME
                    )

                val mimeColumn =
                    cursor.getColumnIndexOrThrow(
                        DocumentsContract.Document
                            .COLUMN_MIME_TYPE
                    )

                while (
                    cursor.moveToNext()
                ) {
                    currentCoroutineContext()
                        .ensureActive()

                    val documentId =
                        cursor.getString(
                            idColumn
                        )
                            ?.trim()
                            .orEmpty()

                    val displayName =
                        cursor.getString(
                            nameColumn
                        )
                            ?.trim()
                            .orEmpty()

                    val mimeType =
                        cursor.getString(
                            mimeColumn
                        )
                            ?.trim()
                            .orEmpty()

                    if (
                        documentId.isBlank() ||
                        displayName.isBlank() ||
                        mimeType ==
                        DocumentsContract.Document
                            .MIME_TYPE_DIR
                    ) {
                        continue
                    }

                    val isMp3 =
                        mimeType.equals(
                            MP3_MIME_TYPE,
                            ignoreCase = true
                        ) ||
                            displayName.endsWith(
                                ".mp3",
                                ignoreCase = true
                            )

                    if (!isMp3) {
                        continue
                    }

                    scannedMp3Count++

                    val parsed =
                        SongNameMatcher
                            .parseFileName(
                                displayName
                            )
                            ?: continue

                    val documentUri =
                        DocumentsContract
                            .buildDocumentUriUsingTree(
                                treeUri,
                                documentId
                            )

                    val combined =
                        buildString {
                            append(
                                parsed.title
                            )

                            if (
                                parsed.artist
                                    .isNotBlank()
                            ) {
                                append(' ')
                                append(
                                    parsed.artist
                                )
                            }
                        }

                    entries +=
                        ReferenceSongEntry(
                            uri =
                                documentUri.toString(),

                            treeUri =
                                treeUriText,

                            displayName =
                                displayName,

                            title =
                                parsed.title,

                            artist =
                                parsed.artist,

                            normalizedTitle =
                                parsed.normalizedTitle,

                            normalizedCombined =
                                SongNameMatcher
                                    .normalizeTitle(
                                        combined
                                    )
                        )

                    if (
                        scannedMp3Count %
                        YIELD_EVERY == 0
                    ) {
                        yield()
                    }
                }
            }

            entries.distinctBy(
                ReferenceSongEntry::uri
            )
        }

    private suspend fun rankMatches(
        normalizedQuery: String,
        entries: List<ReferenceSongEntry>,
        limit: Int
    ): List<ReferenceSongMatch> =
        withContext(
            Dispatchers.Default
        ) {
            entries.asSequence()
                .mapNotNull { entry ->
                    val titleScore =
                        SongNameMatcher
                            .similarity(
                                normalizedQuery,
                                entry.normalizedTitle
                            )

                    val combinedScore =
                        SongNameMatcher
                            .similarity(
                                normalizedQuery,
                                entry.normalizedCombined
                            )

                    val score =
                        max(
                            titleScore,
                            combinedScore
                        )

                    if (
                        score <
                        SEARCH_THRESHOLD
                    ) {
                        null
                    }
                    else {
                        ReferenceSongMatch(
                            uri =
                                entry.uri,

                            treeUri =
                                entry.treeUri,

                            displayName =
                                entry.displayName,

                            title =
                                entry.title,

                            artist =
                                entry.artist,

                            score =
                                score,

                            titleScore =
                                titleScore,

                            combinedScore =
                                combinedScore
                        )
                    }
                }
                .sortedWith(
                    compareByDescending<
                        ReferenceSongMatch
                        >(
                        ReferenceSongMatch::score
                    ).thenBy {
                        it.displayName
                            .lowercase()
                    }
                )
                .take(
                    limit.coerceIn(
                        1,
                        20
                    )
                )
                .toList()
        }

    private suspend fun loadMediaDetails(
        matches: List<ReferenceSongMatch>,
        sourceKey: String
    ): List<ReferenceSongMatch> =
        withContext(
            Dispatchers.IO
        ) {
            coverCacheDirectory.mkdirs()

            val enriched =
                matches.map { match ->
                    currentCoroutineContext()
                        .ensureActive()

                    val details =
                        readMediaDetails(
                            uriText =
                                match.uri,

                            sourceKey =
                                sourceKey
                        )

                    match.copy(
                        album =
                            details?.album,

                        year =
                            details?.year,

                        coverPath =
                            details?.coverPath,

                        durationSeconds =
                            details
                                ?.durationSeconds
                    )
                }

            pruneCoverCache()
            enriched
        }

    private fun readMediaDetails(
        uriText: String,
        sourceKey: String
    ): ReferenceMediaDetails? {
        val retriever =
            MediaMetadataRetriever()

        return try {
            retriever.setDataSource(
                applicationContext,
                Uri.parse(uriText)
            )

            val durationSeconds =
                retriever.extractMetadata(
                    MediaMetadataRetriever
                        .METADATA_KEY_DURATION
                )
                    ?.toLongOrNull()
                    ?.div(1_000L)
                    ?.coerceAtLeast(0L)

            val album =
                retriever.extractMetadata(
                    MediaMetadataRetriever
                        .METADATA_KEY_ALBUM
                )
                    ?.trim()
                    ?.takeIf(
                        String::isNotBlank
                    )

            val year =
                retriever.extractMetadata(
                    MediaMetadataRetriever
                        .METADATA_KEY_YEAR
                )
                    ?.trim()
                    ?.takeIf(
                        String::isNotBlank
                    )

            val coverPath =
                saveEmbeddedCover(
                    imageBytes =
                        retriever
                            .embeddedPicture,

                    cacheKey =
                        "$sourceKey|$uriText"
                )

            ReferenceMediaDetails(
                durationSeconds =
                    durationSeconds,
                album =
                    album,
                year =
                    year,
                coverPath =
                    coverPath
            )
        }
        catch (
            _: Exception
        ) {
            null
        }
        finally {
            runCatching {
                retriever.release()
            }
        }
    }

    private fun saveEmbeddedCover(
        imageBytes: ByteArray?,
        cacheKey: String
    ): String? {
        val bytes =
            imageBytes
                ?.takeIf {
                    it.isNotEmpty()
                }
                ?: return null

        val fileName =
            "cover_${
                cacheKey
                    .hashCode()
                    .toUInt()
                    .toString(16)
            }.img"

        val destination =
            File(
                coverCacheDirectory,
                fileName
            )

        if (
            destination.isFile &&
            destination.length() > 0L
        ) {
            destination.setLastModified(
                System.currentTimeMillis()
            )

            return destination
                .absolutePath
        }

        val temporary =
            File(
                coverCacheDirectory,
                "$fileName.tmp"
            )

        return runCatching {
            temporary.delete()

            temporary.outputStream()
                .buffered()
                .use { output ->
                    output.write(
                        bytes
                    )
                }

            if (
                !temporary.renameTo(
                    destination
                )
            ) {
                temporary.copyTo(
                    target =
                        destination,

                    overwrite =
                        true
                )

                temporary.delete()
            }

            destination
                .absolutePath
                .takeIf {
                    destination.isFile &&
                        destination.length() >
                        0L
                }
        }.getOrNull()
    }

    private fun pruneCoverCache() {
        coverCacheDirectory
            .listFiles()
            ?.asSequence()
            ?.filter {
                it.isFile &&
                    !it.name.endsWith(
                        ".tmp"
                    )
            }
            ?.sortedByDescending {
                it.lastModified()
            }
            ?.drop(
                MAX_CACHED_COVERS
            )
            ?.forEach {
                it.delete()
            }

        coverCacheDirectory
            .listFiles()
            ?.filter {
                it.isFile &&
                    it.name.endsWith(
                        ".tmp"
                    )
            }
            ?.forEach {
                it.delete()
            }
    }
}