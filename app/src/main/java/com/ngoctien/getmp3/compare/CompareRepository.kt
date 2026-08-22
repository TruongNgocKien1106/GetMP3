package com.ngoctien.getmp3.compare

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.ngoctien.getmp3.storage.resolveSafDirectoryDocumentId
import com.ngoctien.getmp3.data.IndexedMediaEntity
import com.ngoctien.getmp3.library.MediaIndexRepository
import com.ngoctien.getmp3.library.MediaMatchEngine
import com.ngoctien.getmp3.settings.AppSettingsRepository
import com.ngoctien.getmp3.tag.TagEditorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import kotlin.math.absoluteValue

class CompareRepository(
    context: Context
) {
    companion object {
        private const val MIME_TYPE =
            "audio/mpeg"

        private const val COPY_BUFFER_SIZE =
            256 * 1024
    }

    private val applicationContext =
        context.applicationContext

    private val resolver =
        applicationContext.contentResolver

    private val settingsRepository =
        AppSettingsRepository(
            applicationContext
        )

    private val tagEditorRepository =
        TagEditorRepository(
            applicationContext
        )

    private val mediaIndexRepository =
        MediaIndexRepository(
            applicationContext
        )

    /*
     * Reference data changes only when Settings prepares the library.
     * Keep the already-built matching structures in this ViewModel-scoped
     * repository so repeated comparisons do not rebuild 2,000-song indices.
     */
    private var cachedReferenceUpdatedAt =
        Long.MIN_VALUE

    private var cachedReferenceSongs:
        List<IndexedMediaEntity> =
        emptyList()

    private var cachedReferenceByUri:
        Map<String, IndexedMediaEntity> =
        emptyMap()

    private var cachedMatchEngine:
        MediaMatchEngine? =
        null

    /**
     * Compares the current/download folder with the shared reference DB.
     * This method NEVER scans the reference SAF tree.
     */
    suspend fun scan(): CompareScanResult =
        withContext(Dispatchers.IO) {
            val settings =
                settingsRepository.getSettings()

            if (
                settings.compareTreeUri
                    .isNullOrBlank()
            ) {
                throw IllegalStateException(
                    "Chưa chọn thư mục đối chiếu"
                )
            }

            val referenceBundle =
                getPreparedReferenceBundle()

            /*
             * The working folder is small and changes often. The shared
             * DOWNLOAD index is incremental: unchanged files are not reopened.
             */
            mediaIndexRepository
                .syncDownloadLibrary()

            val currentSongs =
                mediaIndexRepository
                    .getDownloadSongs()

            val ignoredKeys =
                mediaIndexRepository
                    .getIgnoredPairKeys()

            val exactPairs =
                mutableListOf<ComparePair>()

            val nearPairs =
                mutableListOf<ComparePair>()

            val ignoredMatched =
                linkedSetOf<String>()

            currentSongs.forEach { current ->
                val match =
                    referenceBundle.engine
                        .findBest(
                            current = current,
                            threshold =
                                MediaMatchEngine
                                    .DEFAULT_NEAR_THRESHOLD
                        )
                        ?: return@forEach

                val reference =
                    referenceBundle.byUri[
                        match.referenceUri
                    ]
                        ?: return@forEach

                val pairKey =
                    createPairKey(
                        current = current,
                        reference = reference
                    )

                if (pairKey in ignoredKeys) {
                    ignoredMatched += pairKey
                    return@forEach
                }

                val pair =
                    ComparePair(
                        current =
                            current.toCompareFile(),
                        reference =
                            reference.toCompareFile(),
                        kind =
                            if (match.exactFileName) {
                                CompareMatchKind.EXACT
                            } else {
                                CompareMatchKind.NEAR
                            },
                        score = match.score,
                        ignoreKey = pairKey
                    )

                if (match.exactFileName) {
                    exactPairs += pair
                } else {
                    nearPairs += pair
                }
            }

            CompareScanResult(
                exactPairs =
                    exactPairs.sortedBy {
                        it.current.title.lowercase()
                    },
                nearPairs =
                    nearPairs.sortedWith(
                        compareByDescending<ComparePair> {
                            it.score
                        }.thenBy {
                            it.current.title.lowercase()
                        }
                    ),
                ignoredPairCount =
                    ignoredMatched.size
            )
        }

    suspend fun keepBoth(
        pair: ComparePair
    ): CompareActionResult {
        mediaIndexRepository.ignorePair(
            pairKey = pair.ignoreKey,
            currentSignature =
                pair.current.contentSignature,
            referenceSignature =
                pair.reference.contentSignature
        )

        return CompareActionResult(
            message =
                "Đã giữ cả hai. Cặp này chỉ hiện lại nếu một phiên bản thay đổi."
        )
    }

    suspend fun keepReference(
        pair: ComparePair
    ): CompareActionResult =
        withContext(Dispatchers.IO) {
            tagEditorRepository.deleteSong(
                pair.current.toMediaSongFile()
            )

            mediaIndexRepository.deleteUri(
                pair.current.uri
            )

            CompareActionResult(
                message =
                    "Đã giữ bản cũ và xóa bản mới."
            )
        }

    suspend fun keepCurrent(
        pair: ComparePair
    ): CompareActionResult =
        withContext(Dispatchers.IO) {
            val treeUriText =
                pair.reference.treeUri
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: throw IllegalStateException(
                        "File đối chiếu không có quyền SAF hợp lệ"
                    )

            val treeUri =
                Uri.parse(treeUriText)

            val rootDocumentId =
                resolveSafDirectoryDocumentId(
                    treeUri
                )

            val rootDocumentUri =
                DocumentsContract
                    .buildDocumentUriUsingTree(
                        treeUri,
                        rootDocumentId
                    )

            val token =
                System.currentTimeMillis()

            val tempUri =
                DocumentsContract.createDocument(
                    resolver,
                    rootDocumentUri,
                    MIME_TYPE,
                    ".__getmp3_new_$token.mp3"
                ) ?: throw IllegalStateException(
                    "Không tạo được file tạm trong thư mục đối chiếu"
                )

            var backupUri: Uri? = null
            var finalUri: Uri? = null

            try {
                val copied =
                    copyUri(
                        source =
                            Uri.parse(
                                pair.current.uri
                            ),
                        destination = tempUri
                    )

                if (
                    pair.current.sizeBytes > 0L &&
                    copied != pair.current.sizeBytes
                ) {
                    throw IllegalStateException(
                        "Copy chưa đủ dữ liệu: " +
                            "$copied / ${pair.current.sizeBytes} bytes"
                    )
                }

                backupUri =
                    DocumentsContract.renameDocument(
                        resolver,
                        Uri.parse(
                            pair.reference.uri
                        ),
                        ".__getmp3_old_$token.bak"
                    ) ?: throw IllegalStateException(
                        "Không tạo được bản bảo vệ của file cũ"
                    )

                finalUri =
                    DocumentsContract.renameDocument(
                        resolver,
                        tempUri,
                        pair.current.displayName
                    )

                val safeFinalUri =
                    finalUri
                        ?: run {
                            restoreBackup(
                                backupUri,
                                pair.reference.displayName
                            )

                            throw IllegalStateException(
                                "Không đổi được file mới sang tên chính thức"
                            )
                        }

                mediaIndexRepository
                    .replaceReferenceWithCurrentCopy(
                        oldReferenceUri =
                            pair.reference.uri,
                        newReferenceUri =
                            safeFinalUri.toString(),
                        treeUri = treeUriText,
                        currentUri =
                            pair.current.uri
                    )

                /* Reference DB changed: force rebuilding only the in-memory
                 * matcher next time. The physical reference tree is NOT read. */
                invalidateReferenceMemoryCache()

                val backupToDelete =
                    backupUri

                val backupDeleted =
                    backupToDelete != null &&
                        runCatching {
                            DocumentsContract.deleteDocument(
                                resolver,
                                backupToDelete
                            )
                        }.getOrDefault(false)

                val sourceDeleteError =
                    runCatching {
                        tagEditorRepository.deleteSong(
                            pair.current.toMediaSongFile()
                        )
                    }.exceptionOrNull()

                if (sourceDeleteError == null) {
                    mediaIndexRepository.deleteUri(
                        pair.current.uri
                    )
                }

                val warning =
                    buildList {
                        if (!backupDeleted) {
                            add(
                                "File bảo vệ của bản cũ chưa xóa được"
                            )
                        }

                        if (sourceDeleteError != null) {
                            add(
                                "Bản mới trong thư mục tải chưa xóa được"
                            )
                        }
                    }
                        .takeIf {
                            it.isNotEmpty()
                        }
                        ?.joinToString(". ")

                CompareActionResult(
                    message =
                        "Đã thay bản cũ bằng bản mới.",
                    warning = warning
                )
            } catch (exception: Exception) {
                if (finalUri == null) {
                    runCatching {
                        DocumentsContract.deleteDocument(
                            resolver,
                            tempUri
                        )
                    }
                }

                if (
                    backupUri != null &&
                    finalUri == null
                ) {
                    restoreBackup(
                        backupUri,
                        pair.reference.displayName
                    )
                }

                throw exception
            }
        }

    private suspend fun getPreparedReferenceBundle():
        ReferenceBundle {
        val summary =
            mediaIndexRepository
                .referenceSummary()
                ?: throw IllegalStateException(
                    "Chưa có dữ liệu thư viện. Hãy vào Cài đặt → Chuẩn bị dữ liệu."
                )

        if (summary.totalFiles <= 0) {
            throw IllegalStateException(
                "Dữ liệu thư viện đang trống. Hãy vào Cài đặt → Chuẩn bị dữ liệu."
            )
        }

        val cachedEngine =
            cachedMatchEngine

        if (
            cachedEngine != null &&
            cachedReferenceUpdatedAt ==
                summary.updatedAt &&
            cachedReferenceSongs.isNotEmpty()
        ) {
            return ReferenceBundle(
                songs = cachedReferenceSongs,
                byUri = cachedReferenceByUri,
                engine = cachedEngine
            )
        }

        val songs =
            mediaIndexRepository
                .getReferenceSongs()

        if (songs.isEmpty()) {
            throw IllegalStateException(
                "Dữ liệu thư viện đang trống. Hãy vào Cài đặt → Chuẩn bị dữ liệu."
            )
        }

        val byUri =
            songs.associateBy {
                it.uri
            }

        val engine =
            MediaMatchEngine(songs)

        cachedReferenceUpdatedAt =
            summary.updatedAt

        cachedReferenceSongs =
            songs

        cachedReferenceByUri =
            byUri

        cachedMatchEngine =
            engine

        return ReferenceBundle(
            songs = songs,
            byUri = byUri,
            engine = engine
        )
    }

    private fun invalidateReferenceMemoryCache() {
        cachedReferenceUpdatedAt =
            Long.MIN_VALUE

        cachedReferenceSongs =
            emptyList()

        cachedReferenceByUri =
            emptyMap()

        cachedMatchEngine =
            null
    }

    private fun restoreBackup(
        backupUri: Uri?,
        originalName: String
    ) {
        val uri =
            backupUri ?: return

        runCatching {
            DocumentsContract.renameDocument(
                resolver,
                uri,
                originalName
            )
        }
    }

    private fun copyUri(
        source: Uri,
        destination: Uri
    ): Long {
        val input =
            resolver.openInputStream(source)
                ?: throw IllegalStateException(
                    "Không mở được file nguồn"
                )

        val output =
            resolver.openOutputStream(
                destination,
                "w"
            ) ?: run {
                input.close()

                throw IllegalStateException(
                    "Không mở được file đích"
                )
            }

        return input.use { sourceStream ->
            output.use { destinationStream ->
                sourceStream.copyTo(
                    destinationStream,
                    COPY_BUFFER_SIZE
                )
            }
        }
    }

    private fun createPairKey(
        current: IndexedMediaEntity,
        reference: IndexedMediaEntity
    ): String {
        val raw =
            current.contentSignature +
                "||" +
                reference.contentSignature

        return MessageDigest
            .getInstance("SHA-256")
            .digest(
                raw.toByteArray(
                    Charsets.UTF_8
                )
            )
            .joinToString("") { byte ->
                "%02x".format(
                    byte.toInt() and 0xff
                )
            }
    }

    private fun IndexedMediaEntity.toCompareFile():
        CompareFile {
        return CompareFile(
            id =
                uri.hashCode()
                    .toLong()
                    .absoluteValue,
            uri = uri,
            treeUri = treeUri,
            displayName = displayName,
            title = title,
            artist = artist,
            album = album,
            year = year,
            coverPath = coverPath,
            sizeBytes = sizeBytes,
            dateModifiedSeconds =
                lastModifiedMs / 1000L,
            durationSeconds =
                durationMs
                    .takeIf {
                        it > 0L
                    }
                    ?.div(1000L),
            bitrateKbps =
                bitrateKbps
                    .takeIf {
                        it > 0
                    },
            contentSignature =
                contentSignature
        )
    }

    private data class ReferenceBundle(
        val songs: List<IndexedMediaEntity>,
        val byUri: Map<String, IndexedMediaEntity>,
        val engine: MediaMatchEngine
    )
}
