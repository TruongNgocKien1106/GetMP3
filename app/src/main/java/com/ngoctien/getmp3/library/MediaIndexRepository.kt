package com.ngoctien.getmp3.library

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.ngoctien.getmp3.storage.resolveSafDirectoryDocumentId
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.extractor.metadata.id3.ApicFrame
import androidx.media3.extractor.metadata.id3.Id3Decoder
import androidx.media3.extractor.metadata.id3.TextInformationFrame
import androidx.room.withTransaction
import com.ngoctien.getmp3.data.AppDatabase
import com.ngoctien.getmp3.data.IgnoredComparePairEntity
import com.ngoctien.getmp3.data.IndexedMediaEntity
import com.ngoctien.getmp3.data.MediaIndexSource
import com.ngoctien.getmp3.data.MediaMetadataStatus
import com.ngoctien.getmp3.data.MediaIndexStateEntity
import com.ngoctien.getmp3.note.SongNameMatcher
import com.ngoctien.getmp3.tag.MediaSongFile
import com.ngoctien.getmp3.settings.AppSettings
import com.ngoctien.getmp3.settings.AppSettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import kotlin.math.absoluteValue

class MediaIndexRepository(
    context: Context
) {

    companion object {
        private const val MP3_MIME_TYPE =
            "audio/mpeg"

        private const val UPSERT_BATCH_SIZE =
            64

        private const val PROGRESS_STEP =
            10

        private const val COVER_MAX_SIZE =
            256

        private const val COVER_DECODE_TARGET =
            512

        private const val COVER_JPEG_QUALITY =
            86

        private const val ID3_HEADER_SIZE =
            10

        /*
         * Normal ID3 tags are much smaller than this.
         * This cap prevents a corrupt size field from allocating
         * an uncontrolled ByteArray.
         */
        private const val MAX_ID3_TAG_BYTES =
            16 * 1024 * 1024

        private const val MAX_ERROR_MESSAGE_LENGTH =
            500

        private const val ALL_TAG_FIELDS =
            "title,artist,album,albumArtist,year"
    }

    private val applicationContext =
        context.applicationContext

    private val resolver =
        applicationContext
            .contentResolver

    private val database =
        AppDatabase.getDatabase(
            applicationContext
        )

    private val dao =
        database.mediaIndexDao()

    private val settingsRepository =
        AppSettingsRepository(
            applicationContext
        )

    private val coverDirectory =
        File(
            applicationContext.filesDir,
            "media_index/covers"
        )

    // ========================================================
    // REFERENCE LIBRARY
    // ========================================================

    suspend fun scanReferenceLibrary(
        treeUriText: String,
        forceReadMetadata: Boolean = false,
        onProgress:
            (MediaIndexProgress) -> Unit = {}
    ): MediaIndexSummary {

        require(
            treeUriText.isNotBlank()
        ) {
            "Chưa chọn Library"
        }

        return withContext(
            Dispatchers.IO
        ) {
            val inventory =
                scanReferenceInventory(
                    treeUriText
                )

            syncInventory(
                source =
                    MediaIndexSource
                        .REFERENCE,

                treeUriText =
                    treeUriText,

                inventory =
                    inventory,

                forceReadMetadata =
                    forceReadMetadata,

                onProgress =
                    onProgress
            )
        }
    }

    suspend fun clearReferenceLibrary() {
        withContext(
            Dispatchers.IO
        ) {
            database.withTransaction {
                dao.deleteSource(
                    MediaIndexSource
                        .REFERENCE
                )

                dao.deleteState(
                    MediaIndexSource
                        .REFERENCE
                )
            }

            pruneOrphanCovers()
        }
    }

    suspend fun referenceSummary():
        MediaIndexSummary? =
        withContext(
            Dispatchers.IO
        ) {
            dao.getState(
                MediaIndexSource
                    .REFERENCE
            )
                ?.toSummary()
        }

    suspend fun getReferenceSongs():
        List<IndexedMediaEntity> =
        withContext(
            Dispatchers.IO
        ) {
            dao.getBySource(
                MediaIndexSource
                    .REFERENCE
            )
        }

    suspend fun getReferenceArtists():
        List<String> =
        withContext(
            Dispatchers.IO
        ) {
            dao.getDistinctArtists(
                MediaIndexSource
                    .REFERENCE
            )
        }

    suspend fun getReferenceAlbums():
        List<String> =
        withContext(
            Dispatchers.IO
        ) {
            dao.getDistinctAlbums(
                MediaIndexSource
                    .REFERENCE
            )
        }

    // ========================================================
    // DOWNLOAD / CURRENT LIBRARY
    // ========================================================

    // ========================================================
    // DOMAIN API - INBOX / LIBRARY
    //
    // REFERENCE / DOWNLOAD vẫn là storage identifiers cũ
    // trong Room để không cần migration dữ liệu ở batch này.
    // ========================================================

    suspend fun scanLibrary(
        treeUriText: String,
        forceReadMetadata: Boolean = false,
        onProgress:
            (MediaIndexProgress) -> Unit = {}
    ): MediaIndexSummary =
        scanReferenceLibrary(
            treeUriText =
                treeUriText,

            forceReadMetadata =
                forceReadMetadata,

            onProgress =
                onProgress
        )

    suspend fun clearLibraryIndex() {
        clearReferenceLibrary()
    }

    suspend fun librarySummary():
        MediaIndexSummary? =
        referenceSummary()

    suspend fun getLibrarySongs():
        List<IndexedMediaEntity> =
        getReferenceSongs()

    suspend fun getLibraryArtists():
        List<String> =
        getReferenceArtists()

    suspend fun getLibraryAlbums():
        List<String> =
        getReferenceAlbums()

    suspend fun syncInboxLibrary(
        onProgress:
            (MediaIndexProgress) -> Unit = {}
    ): List<MediaSongFile> =
        syncDownloadLibrary(
            onProgress =
                onProgress
        )

    suspend fun getInboxSongs():
        List<IndexedMediaEntity> =
        getDownloadSongs()

    suspend fun syncDownloadLibrary(
        onProgress:
            (MediaIndexProgress) -> Unit = {}
    ): List<MediaSongFile> {
        return withContext(
            Dispatchers.IO
        ) {
            val files =
                scanDownloadFiles()

            val currentTreeUri =
                files
                    .firstOrNull()
                    ?.treeUri
                    ?.takeIf {
                        it.isNotBlank()
                    }

            val referenceTreeUri =
                dao.getState(
                    MediaIndexSource.REFERENCE
                )
                    ?.treeUri
                    ?.takeIf {
                        it.isNotBlank()
                    }

            if (
                currentTreeUri != null &&
                currentTreeUri == referenceTreeUri
            ) {
                throw IllegalStateException(
                    "Inbox và Library không được trỏ tới cùng một thư mục"
                )
            }

            val inventory =
                files.map {
                    file ->

                    InventoryItem(
                        uri =
                            file.uri,

                        treeUri =
                            file.treeUri,

                        documentId =
                            file.uri,

                        displayName =
                            file.displayName,

                        mimeType =
                            MP3_MIME_TYPE,

                        sizeBytes =
                            file.sizeBytes,

                        lastModifiedMs =
                            file
                                .dateModifiedSeconds *
                                1000L
                    )
                }

            syncInventory(
                source =
                    MediaIndexSource
                        .DOWNLOAD,

                treeUriText =
                    files
                        .firstOrNull()
                        ?.treeUri,

                inventory =
                    inventory,

                forceReadMetadata =
                    false,

                onProgress =
                    onProgress
            )

            files
        }
    }

    suspend fun getDownloadSongs():
        List<IndexedMediaEntity> =
        withContext(
            Dispatchers.IO
        ) {
            dao.getBySource(
                MediaIndexSource
                    .DOWNLOAD
            )
        }

    /*
     * Index đúng một file vừa xuất hiện trong Inbox.
     *
     * Không scan toàn thư mục.
     */
    suspend fun indexInboxFile(
        uri: String,
        displayName: String,
        sizeBytes: Long,
        treeUri: String?,
        lastModifiedMs: Long =
            System.currentTimeMillis()
    ): IndexedMediaEntity =
        indexSingleFile(
            source =
                MediaIndexSource
                    .INBOX,

            uri =
                uri,

            displayName =
                displayName,

            sizeBytes =
                sizeBytes,

            treeUri =
                treeUri,

            lastModifiedMs =
                lastModifiedMs
        )

    /*
     * Dùng cho recovery sau khi file vật lý đã chuyển
     * thành công sang Library nhưng update Room thất bại.
     */
    suspend fun indexLibraryFile(
        uri: String,
        displayName: String,
        sizeBytes: Long,
        treeUri: String,
        lastModifiedMs: Long =
            System.currentTimeMillis()
    ): IndexedMediaEntity =
        indexSingleFile(
            source =
                MediaIndexSource
                    .LIBRARY,

            uri =
                uri,

            displayName =
                displayName,

            sizeBytes =
                sizeBytes,

            treeUri =
                treeUri,

            lastModifiedMs =
                lastModifiedMs
        )

    private suspend fun indexSingleFile(
        source: String,
        uri: String,
        displayName: String,
        sizeBytes: Long,
        treeUri: String?,
        lastModifiedMs: Long
    ): IndexedMediaEntity =
        withContext(
            Dispatchers.IO
        ) {

            require(
                uri.isNotBlank()
            ) {
                "URI file không hợp lệ"
            }

            require(
                displayName.isNotBlank()
            ) {
                "Tên file không hợp lệ"
            }

            require(
                sizeBytes > 0L
            ) {
                "File rỗng"
            }

            val now =
                System.currentTimeMillis()

            val currentState =
                dao.getState(
                    source
                )

            val generation =
                currentState
                    ?.generation
                    ?: now

            val previous =
                dao.getByUri(
                    uri
                )

            val item =
                InventoryItem(
                    uri =
                        uri,

                    treeUri =
                        treeUri,

                    documentId =
                        uri,

                    displayName =
                        displayName,

                    mimeType =
                        MP3_MIME_TYPE,

                    sizeBytes =
                        sizeBytes,

                    lastModifiedMs =
                        lastModifiedMs
                            .coerceAtLeast(
                                0L
                            )
                )

            val signature =
                contentSignature(
                    uri =
                        item.uri,

                    sizeBytes =
                        item.sizeBytes,

                    lastModifiedMs =
                        item.lastModifiedMs
                )

            val indexed =
                try {

                    extractEntity(
                        source =
                            source,

                        item =
                            item,

                        signature =
                            signature,

                        generation =
                            generation,

                        oldCoverPath =
                            previous
                                ?.coverPath
                    )

                } catch (
                    exception:
                        CancellationException
                ) {

                    throw exception

                } catch (
                    exception: Exception
                ) {

                    buildFallbackEntity(
                        source =
                            source,

                        item =
                            item,

                        signature =
                            signature,

                        generation =
                            generation,

                        previous =
                            previous,

                        errorMessage =
                            exception.message
                                ?: exception
                                    .javaClass
                                    .simpleName
                    )
                }

            dao.upsert(
                indexed
            )

            val failedFiles =
                dao.countMetadataErrors(
                    source
                )

            refreshStateFromDatabase(
                source =
                    source,

                treeUri =
                    treeUri
                        ?: currentState
                            ?.treeUri,

                generation =
                    generation,

                failedFiles =
                    failedFiles
            )

            if (
                source ==
                MediaIndexSource
                    .LIBRARY
            ) {
                pruneOrphanCovers()
            }

            indexed
        }

    suspend fun findLibraryByCanonicalFileName(
        canonicalFileName: String
    ): IndexedMediaEntity? =
        withContext(
            Dispatchers.IO
        ) {
            dao.getByCanonicalFileName(
                source =
                    MediaIndexSource
                        .LIBRARY,

                canonicalFileName =
                    canonicalFileName
            )
        }

    suspend fun getByUri(
        uri: String
    ): IndexedMediaEntity? =
        withContext(
            Dispatchers.IO
        ) {
            dao.getByUri(uri)
        }

    suspend fun getMetadataErrors():
        List<IndexedMediaEntity> =
        withContext(
            Dispatchers.IO
        ) {
            dao.getMetadataErrors()
        }

    /**
     * Re-index duy nhất file vừa được sửa.
     *
     * Không scan lại toàn bộ DOWNLOAD hoặc REFERENCE.
     *
     * oldUri có thể khác URI mới vì SAF renameDocument()
     * có thể trả về document URI mới.
     */
    suspend fun refreshEditedFile(
        oldUri: String,
        updatedFile: MediaSongFile
    ): IndexedMediaEntity {
        return withContext(
            Dispatchers.IO
        ) {
            val previous =
                dao.getByUri(oldUri)
                    ?: throw IllegalStateException(
                        "Không tìm thấy file trong Media Index"
                    )

            val generation =
                dao.getState(
                    previous.source
                )
                    ?.generation
                    ?: System.currentTimeMillis()

            val lastModifiedMs =
                updatedFile
                    .dateModifiedSeconds
                    .coerceAtLeast(0L) *
                    1000L

            val item =
                InventoryItem(
                    uri =
                        updatedFile.uri,

                    treeUri =
                        updatedFile.treeUri
                            ?: previous.treeUri,

                    documentId =
                        if (
                            updatedFile.uri ==
                            oldUri
                        ) {
                            previous.documentId
                        } else {
                            updatedFile.uri
                        },

                    displayName =
                        updatedFile.displayName,

                    mimeType =
                        previous.mimeType
                            .ifBlank {
                                MP3_MIME_TYPE
                            },

                    sizeBytes =
                        updatedFile.sizeBytes,

                    lastModifiedMs =
                        lastModifiedMs
                )

            val signature =
                contentSignature(
                    uri = item.uri,
                    sizeBytes =
                        item.sizeBytes,
                    lastModifiedMs =
                        item.lastModifiedMs
                )

            val refreshed =
                try {
                    extractEntity(
                        source =
                            previous.source,

                        item =
                            item,

                        signature =
                            signature,

                        generation =
                            generation,

                        oldCoverPath =
                            previous.coverPath
                    )
                } catch (
                    exception:
                        CancellationException
                ) {
                    throw exception
                } catch (
                    exception: Exception
                ) {
                    buildFallbackEntity(
                        source =
                            previous.source,

                        item =
                            item,

                        signature =
                            signature,

                        generation =
                            generation,

                        previous =
                            previous.takeIf {
                                oldUri ==
                                    item.uri
                            },

                        errorMessage =
                            exception.message
                                ?: exception
                                    .javaClass
                                    .simpleName
                    )
                }

            database.withTransaction {
                if (
                    oldUri !=
                    refreshed.uri
                ) {
                    dao.deleteUri(
                        oldUri
                    )
                }

                dao.upsert(
                    refreshed
                )
            }

            val failedFiles =
                dao.countMetadataErrors(
                    previous.source
                )

            refreshStateFromDatabase(
                source =
                    previous.source,

                treeUri =
                    previous.treeUri,

                generation =
                    generation,

                failedFiles =
                    failedFiles
            )

            if (
                previous.source ==
                MediaIndexSource.REFERENCE
            ) {
                pruneOrphanCovers()
            }

            refreshed
        }
    }
    suspend fun deleteUri(
        uri: String
    ) {
        withContext(
            Dispatchers.IO
        ) {
            dao.deleteUri(uri)
        }
    }

    // ========================================================
    // COMPARE DECISIONS
    // ========================================================

    /*
     * File vật lý đã được chuyển thành công trước khi method
     * này được gọi.
     *
     * Chỉ chuyển đúng một Media Index row:
     *
     * INBOX -> LIBRARY
     *
     * Không scan lại toàn thư mục.
     */
    suspend fun promoteInboxItemToLibrary(
        oldInboxUri: String,
        newLibraryUri: String,
        libraryTreeUri: String
    ): IndexedMediaEntity =
        withContext(
            Dispatchers.IO
        ) {

            val current =
                dao.getByUri(
                    oldInboxUri
                )
                    ?: throw IllegalStateException(
                        "Không tìm thấy file Inbox trong Media Index"
                    )

            require(
                current.source ==
                    MediaIndexSource
                        .INBOX
            ) {
                "Chỉ file Inbox mới được đưa vào Library"
            }

            val duplicate =
                dao.getByCanonicalFileName(
                    source =
                        MediaIndexSource
                            .LIBRARY,

                    canonicalFileName =
                        current
                            .canonicalFileName
                )

            require(
                duplicate == null ||
                    duplicate.uri ==
                    oldInboxUri
            ) {
                "Library đã có bài cùng filename chuẩn: " +
                    duplicate
                        ?.displayName
            }

            val now =
                System.currentTimeMillis()

            val inboxState =
                dao.getState(
                    MediaIndexSource
                        .INBOX
                )

            val libraryState =
                dao.getState(
                    MediaIndexSource
                        .LIBRARY
                )

            val libraryGeneration =
                libraryState
                    ?.generation
                    ?: now

            val promoted =
                current.copy(
                    uri =
                        newLibraryUri,

                    source =
                        MediaIndexSource
                            .LIBRARY,

                    treeUri =
                        libraryTreeUri,

                    /*
                     * Các luồng SAF hiện tại của project cũng
                     * lưu URI document vào field documentId
                     * sau rename/copy.
                     */
                    documentId =
                        newLibraryUri,

                    lastModifiedMs =
                        now,

                    contentSignature =
                        contentSignature(
                            uri =
                                newLibraryUri,

                            sizeBytes =
                                current
                                    .sizeBytes,

                            lastModifiedMs =
                                now
                        ),

                    scanGeneration =
                        libraryGeneration,

                    indexedAt =
                        now
                )

            database.withTransaction {

                dao.deleteUri(
                    oldInboxUri
                )

                dao.upsert(
                    promoted
                )
            }

            /*
             * Refresh summary của Library dựa trực tiếp vào
             * database hiện tại.
             */
            val libraryFailed =
                dao.countMetadataErrors(
                    MediaIndexSource
                        .LIBRARY
                )

            refreshStateFromDatabase(
                source =
                    MediaIndexSource
                        .LIBRARY,

                treeUri =
                    libraryTreeUri,

                generation =
                    libraryGeneration,

                failedFiles =
                    libraryFailed
            )

            /*
             * Inbox state có thể chưa tồn tại ở một install mới.
             * Nếu có thì cập nhật lại summary sau khi mất 1 file.
             */
            if (inboxState != null) {

                val inboxFailed =
                    dao.countMetadataErrors(
                        MediaIndexSource
                            .INBOX
                    )

                refreshStateFromDatabase(
                    source =
                        MediaIndexSource
                            .INBOX,

                    treeUri =
                        inboxState
                            .treeUri,

                    generation =
                        inboxState
                            .generation,

                    failedFiles =
                        inboxFailed
                )
            }

            promoted
        }

    suspend fun getIgnoredPairKeys():
        Set<String> =
        withContext(
            Dispatchers.IO
        ) {
            dao.getIgnoredPairKeys()
                .toSet()
        }

    suspend fun ignorePair(
        pairKey: String,
        currentSignature: String,
        referenceSignature: String
    ) {
        withContext(
            Dispatchers.IO
        ) {
            dao.upsertIgnoredPair(
                IgnoredComparePairEntity(
                    pairKey =
                        pairKey,

                    currentSignature =
                        currentSignature,

                    referenceSignature =
                        referenceSignature,

                    createdAt =
                        System.currentTimeMillis()
                )
            )
        }
    }

    // ========================================================
    // REFERENCE UPDATE AFTER SAFE REPLACE
    // ========================================================

    suspend fun replaceReferenceWithCurrentCopy(
        oldReferenceUri: String,
        newReferenceUri: String,
        treeUri: String,
        currentUri: String
    ) {
        withContext(
            Dispatchers.IO
        ) {
            val current =
                dao.getByUri(
                    currentUri
                )
                    ?: return@withContext

            val generation =
                dao.getState(
                    MediaIndexSource
                        .REFERENCE
                )
                    ?.generation
                    ?: System.currentTimeMillis()

            val now =
                System.currentTimeMillis()

            val copied =
                current.copy(
                    uri =
                        newReferenceUri,

                    source =
                        MediaIndexSource
                            .REFERENCE,

                    treeUri =
                        treeUri,

                    documentId =
                        newReferenceUri,

                    lastModifiedMs =
                        now,

                    contentSignature =
                        contentSignature(
                            uri =
                                newReferenceUri,

                            sizeBytes =
                                current.sizeBytes,

                            lastModifiedMs =
                                now
                        ),

                    scanGeneration =
                        generation,

                    indexedAt =
                        now
                )

            database.withTransaction {
                dao.deleteUri(
                    oldReferenceUri
                )

                dao.upsert(
                    copied
                )
            }

            refreshStateFromDatabase(
                source =
                    MediaIndexSource
                        .REFERENCE,

                treeUri =
                    treeUri,

                generation =
                    generation,

                failedFiles =
                    0
            )
        }
    }

    // ========================================================
    // LIGHTWEIGHT DOWNLOAD INVENTORY
    //
    // This deliberately does not instantiate TagEditorRepository, therefore
    // comparing/listing files does not start the embedded Python runtime.
    // ========================================================

    private fun scanDownloadFiles():
        List<MediaSongFile> {
        val settings =
            settingsRepository
                .getSettings()

        return if (settings.usesCustomFolder) {
            scanDownloadDocumentTree(
                Uri.parse(
                    settings.downloadTreeUri
                )
            )
        } else {
            scanDefaultDownloadFolder()
        }
    }

    private fun scanDefaultDownloadFolder():
        List<MediaSongFile> {
        val collection =
            MediaStore.Audio.Media
                .getContentUri(
                    MediaStore
                        .VOLUME_EXTERNAL_PRIMARY
                )

        val projection =
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATE_MODIFIED
            )

        val selection =
            """
            ${MediaStore.Audio.Media.RELATIVE_PATH} = ?
            AND
            (
                ${MediaStore.Audio.Media.MIME_TYPE} = ?
                OR
                ${MediaStore.Audio.Media.DISPLAY_NAME} LIKE ?
            )
            """.trimIndent()

        val songs =
            mutableListOf<MediaSongFile>()

        resolver.query(
            collection,
            projection,
            selection,
            arrayOf(
                AppSettings.DEFAULT_RELATIVE_PATH,
                MP3_MIME_TYPE,
                "%.mp3"
            ),
            "${MediaStore.Audio.Media.DISPLAY_NAME} COLLATE NOCASE ASC"
        )?.use { cursor ->
            val idColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Media._ID
                )

            val nameColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Media.DISPLAY_NAME
                )

            val sizeColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Media.SIZE
                )

            val modifiedColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Media.DATE_MODIFIED
                )

            while (cursor.moveToNext()) {
                val id =
                    cursor.getLong(idColumn)

                songs +=
                    MediaSongFile(
                        id = id,
                        uri =
                            ContentUris
                                .withAppendedId(
                                    collection,
                                    id
                                )
                                .toString(),
                        displayName =
                            cursor.getString(
                                nameColumn
                            ) ?: "Unknown.mp3",
                        sizeBytes =
                            cursor.getLong(
                                sizeColumn
                            ),
                        dateModifiedSeconds =
                            cursor.getLong(
                                modifiedColumn
                            ),
                        treeUri = null
                    )
            }
        }

        return songs
    }

    private fun scanDownloadDocumentTree(
        treeUri: Uri
    ): List<MediaSongFile> {
        val treeDocumentId =
            resolveSafDirectoryDocumentId(
                treeUri
            )

        val childrenUri =
            DocumentsContract
                .buildChildDocumentsUriUsingTree(
                    treeUri,
                    treeDocumentId
                )

        val songs =
            mutableListOf<MediaSongFile>()

        resolver.query(
            childrenUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_SIZE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED
            ),
            null,
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val documentId =
                    cursor.getString(0)
                        ?: continue

                val displayName =
                    cursor.getString(1)
                        ?: "Unknown.mp3"

                val mimeType =
                    cursor.getString(2)
                        .orEmpty()

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

                val documentUri =
                    DocumentsContract
                        .buildDocumentUriUsingTree(
                            treeUri,
                            documentId
                        )

                songs +=
                    MediaSongFile(
                        id =
                            documentUri
                                .toString()
                                .hashCode()
                                .toLong()
                                .absoluteValue,
                        uri =
                            documentUri.toString(),
                        displayName =
                            displayName,
                        sizeBytes =
                            if (cursor.isNull(3)) {
                                0L
                            } else {
                                cursor.getLong(3)
                            },
                        dateModifiedSeconds =
                            if (cursor.isNull(4)) {
                                0L
                            } else {
                                cursor.getLong(4) /
                                    1000L
                            },
                        treeUri =
                            treeUri.toString()
                    )
            }
        }

        return songs.sortedBy {
            it.displayName.lowercase()
        }
    }

    // ========================================================
    // CORE INCREMENTAL SYNC
    // ========================================================

    private suspend fun syncInventory(
        source: String,
        treeUriText: String?,
        inventory: List<InventoryItem>,
        forceReadMetadata: Boolean,
        onProgress: (MediaIndexProgress) -> Unit
    ): MediaIndexSummary {

        val generation =
            System.currentTimeMillis()

        val existing =
            dao.getBySource(source)
                .associateBy {
                    it.uri
                }

        val total =
            inventory.size

        var processed = 0
        var newFiles = 0
        var changedFiles = 0
        var skippedFiles = 0
        var failedFiles = 0

        val pending =
            ArrayList<IndexedMediaEntity>(
                UPSERT_BATCH_SIZE
            )

        suspend fun flush() {
            if (pending.isEmpty()) {
                return
            }

            dao.upsertAll(
                pending.toList()
            )

            pending.clear()
        }

        onProgress(
            MediaIndexProgress(
                totalFiles = total
            )
        )

        inventory.forEach { item ->
            currentCoroutineContext()
                .ensureActive()

            val signature =
                contentSignature(
                    uri =
                        item.uri,

                    sizeBytes =
                        item.sizeBytes,

                    lastModifiedMs =
                        item.lastModifiedMs
                )

            val old =
                existing[item.uri]

            val unchanged =
                !forceReadMetadata &&
                    old != null &&
                    old.contentSignature ==
                    signature

            val entity =
                if (unchanged) {
                    skippedFiles++

                    old.copy(
                        scanGeneration =
                            generation
                    )
                } else {
                    try {
                        if (old == null) {
                            newFiles++
                        } else {
                            changedFiles++
                        }

                        extractEntity(
                            source = source,
                            item = item,
                            signature = signature,
                            generation = generation,
                            oldCoverPath =
                                old?.coverPath
                        )
                    } catch (
                        exception:
                            CancellationException
                    ) {
                        throw exception
                    } catch (
                        exception: Exception
                    ) {
                        buildFallbackEntity(
                            source = source,
                            item = item,
                            signature = signature,
                            generation = generation,
                            previous = old,
                            errorMessage =
                                exception.message
                                    ?: exception.javaClass.simpleName
                        )
                    }
                }

            if (
                MediaMetadataStatus.isError(
                    entity.metadataStatus
                )
            ) {
                failedFiles++
            }

            pending.add(entity)

            processed++

            if (
                pending.size >=
                UPSERT_BATCH_SIZE
            ) {
                flush()
            }

            if (
                processed == total ||
                processed == 1 ||
                processed %
                PROGRESS_STEP ==
                0
            ) {
                onProgress(
                    MediaIndexProgress(
                        totalFiles =
                            total,

                        processedFiles =
                            processed,

                        newFiles =
                            newFiles,

                        changedFiles =
                            changedFiles,

                        skippedFiles =
                            skippedFiles,

                        failedFiles =
                            failedFiles,

                        currentFileName =
                            item.displayName
                    )
                )

                yield()
            }
        }

        flush()

        database.withTransaction {
            dao.deleteOlderGeneration(
                source = source,
                generation = generation
            )
        }

        val summary =
            refreshStateFromDatabase(
                source = source,
                treeUri = treeUriText,
                generation = generation,
                failedFiles = failedFiles
            )

        if (
            source ==
            MediaIndexSource.REFERENCE
        ) {
            pruneOrphanCovers()
        }

        onProgress(
            MediaIndexProgress(
                totalFiles = total,
                processedFiles = total,
                newFiles = newFiles,
                changedFiles = changedFiles,
                skippedFiles = skippedFiles,
                failedFiles = failedFiles,
                currentFileName = ""
            )
        )

        return summary
    }

    private suspend fun refreshStateFromDatabase(
        source: String,
        treeUri: String?,
        generation: Long,
        failedFiles: Int
    ): MediaIndexSummary {

        val totalFiles =
            dao.countBySource(source)

        val coverFiles =
            dao.countWithCover(source)

        val artistCount =
            dao.getDistinctArtists(source)
                .size

        val albumCount =
            dao.getDistinctAlbums(source)
                .size

        val updatedAt =
            System.currentTimeMillis()

        val state =
            MediaIndexStateEntity(
                source = source,
                treeUri = treeUri,
                generation = generation,
                totalFiles = totalFiles,
                indexedFiles = totalFiles,
                failedFiles = failedFiles,
                coverFiles = coverFiles,
                artistCount = artistCount,
                albumCount = albumCount,
                updatedAt = updatedAt
            )

        dao.upsertState(state)

        return state.toSummary()
    }

    // ========================================================
    // REFERENCE INVENTORY
    // ========================================================

    private fun scanReferenceInventory(
        treeUriText: String
    ): List<InventoryItem> {

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
                DocumentsContract
                    .Document
                    .COLUMN_DOCUMENT_ID,

                DocumentsContract
                    .Document
                    .COLUMN_DISPLAY_NAME,

                DocumentsContract
                    .Document
                    .COLUMN_MIME_TYPE,

                DocumentsContract
                    .Document
                    .COLUMN_SIZE,

                DocumentsContract
                    .Document
                    .COLUMN_LAST_MODIFIED
            )

        val result =
            ArrayList<InventoryItem>(
                2_500
            )

        resolver.query(
            childrenUri,
            projection,
            null,
            null,
            null
        )?.use { cursor ->

            val idColumn =
                cursor.getColumnIndexOrThrow(
                    DocumentsContract
                        .Document
                        .COLUMN_DOCUMENT_ID
                )

            val nameColumn =
                cursor.getColumnIndexOrThrow(
                    DocumentsContract
                        .Document
                        .COLUMN_DISPLAY_NAME
                )

            val mimeColumn =
                cursor.getColumnIndexOrThrow(
                    DocumentsContract
                        .Document
                        .COLUMN_MIME_TYPE
                )

            val sizeColumn =
                cursor.getColumnIndex(
                    DocumentsContract
                        .Document
                        .COLUMN_SIZE
                )

            val modifiedColumn =
                cursor.getColumnIndex(
                    DocumentsContract
                        .Document
                        .COLUMN_LAST_MODIFIED
                )

            while (
                cursor.moveToNext()
            ) {
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
                    DocumentsContract
                        .Document
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

                val documentUri =
                    DocumentsContract
                        .buildDocumentUriUsingTree(
                            treeUri,
                            documentId
                        )

                val sizeBytes =
                    if (
                        sizeColumn >= 0 &&
                        !cursor.isNull(
                            sizeColumn
                        )
                    ) {
                        cursor.getLong(
                            sizeColumn
                        )
                    } else {
                        0L
                    }

                val lastModifiedMs =
                    if (
                        modifiedColumn >= 0 &&
                        !cursor.isNull(
                            modifiedColumn
                        )
                    ) {
                        cursor.getLong(
                            modifiedColumn
                        )
                    } else {
                        0L
                    }

                result.add(
                    InventoryItem(
                        uri =
                            documentUri
                                .toString(),

                        treeUri =
                            treeUriText,

                        documentId =
                            documentId,

                        displayName =
                            displayName,

                        mimeType =
                            mimeType,

                        sizeBytes =
                            sizeBytes,

                        lastModifiedMs =
                            lastModifiedMs
                    )
                )
            }
        }

        return result
    }

    // ========================================================
    // METADATA EXTRACTION
    // ========================================================

    @OptIn(UnstableApi::class)
    private fun extractEntity(
        source: String,
        item: InventoryItem,
        signature: String,
        generation: Long,
        oldCoverPath: String?
    ): IndexedMediaEntity {

        val parsed =
            SongNameMatcher
                .parseFileName(
                    item.displayName
                )

        val stem =
            item.displayName
                .substringBeforeLast(
                    delimiter = ".",
                    missingDelimiterValue =
                        item.displayName
                )
                .trim()

        val fileTitle =
            parsed
                ?.title
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: stem

        val fileArtist =
            parsed
                ?.artist
                ?.trim()
                .orEmpty()

        /*
         * Human-readable metadata is decoded directly from ID3
         * bytes with Media3.
         *
         * We deliberately avoid the framework text metadata path
         * which can abort the process for malformed encodings.
         */
        val id3 =
            readSafeId3Metadata(
                item.uri
            )

        /*
         * Duration / bitrate are numeric technical information.
         * They are read separately with MediaExtractor.
         */
        val technical =
            readTechnicalMetadata(
                uriText =
                    item.uri,
                sizeBytes =
                    item.sizeBytes
            )

        val tagTitle =
            id3.title.orEmpty()

        val tagArtist =
            id3.artist.orEmpty()

        val album =
            id3.album.orEmpty()

        val albumArtist =
            id3.albumArtist.orEmpty()

        val year =
            id3.year.orEmpty()

        /*
         * Filename remains authoritative for Title / Artist.
         *
         * Example:
         *
         * Title - Artist.mp3
         *
         * ID3 is the fallback if one filename component is absent.
         */
        val title =
            fileTitle.ifBlank {
                tagTitle
            }

        val artist =
            fileArtist.ifBlank {
                tagArtist
            }

        val coverPath =
            saveCover(
                uri =
                    item.uri,
                bytes =
                    id3.pictureData,
                oldCoverPath =
                    oldCoverPath
            )

        return buildEntity(
            source =
                source,

            item =
                item,

            signature =
                signature,

            generation =
                generation,

            fileTitle =
                fileTitle,

            fileArtist =
                fileArtist,

            tagTitle =
                tagTitle,

            tagArtist =
                tagArtist,

            album =
                album,

            albumArtist =
                albumArtist,

            year =
                year,

            rawTagTitle =
                id3.title,

            rawTagArtist =
                id3.artist,

            rawTagAlbum =
                id3.album,

            rawTagAlbumArtist =
                id3.albumArtist,

            rawTagYear =
                id3.rawYear,

            metadataStatus =
                id3.status,

            metadataErrorCode =
                id3.errorCode,

            metadataErrorFields =
                id3.errorFields,

            metadataErrorMessage =
                id3.errorMessage,

            title =
                title,

            artist =
                artist,

            durationMs =
                technical.durationMs,

            bitrateKbps =
                technical.bitrateKbps,

            coverPath =
                coverPath
        )
    }


    // ========================================================
    // SAFE ID3
    // ========================================================

    @OptIn(UnstableApi::class)
    private fun readSafeId3Metadata(
        uriText: String
    ): SafeId3Metadata {

        return try {

            val input =
                resolver.openInputStream(
                    Uri.parse(
                        uriText
                    )
                )
                    ?: return SafeId3Metadata(
                        status =
                            MediaMetadataStatus
                                .UNREADABLE_FILE,

                        errorCode =
                            "OPEN_FAILED",

                        errorFields =
                            ALL_TAG_FIELDS,

                        errorMessage =
                            "Không mở được file để đọc ID3"
                    )


            input.use {
                stream ->


                // =============================================
                // READ ID3 HEADER
                // =============================================

                val header =
                    ByteArray(
                        ID3_HEADER_SIZE
                    )

                val headerRead =
                    readFully(
                        input =
                            stream,

                        buffer =
                            header,

                        offset =
                            0,

                        length =
                            ID3_HEADER_SIZE
                    )


                if (
                    headerRead <
                    ID3_HEADER_SIZE
                ) {
                    return SafeId3Metadata(
                        status =
                            MediaMetadataStatus
                                .UNREADABLE_FILE,

                        errorCode =
                            "SHORT_FILE",

                        errorFields =
                            ALL_TAG_FIELDS,

                        errorMessage =
                            "File quá ngắn để đọc metadata"
                    )
                }


                // =============================================
                // ID3 MAGIC = "ID3"
                // =============================================

                val hasId3 =
                    (
                        header[0]
                            .toInt() and
                            0xff
                        ) ==
                        'I'.code &&
                    (
                        header[1]
                            .toInt() and
                            0xff
                        ) ==
                        'D'.code &&
                    (
                        header[2]
                            .toInt() and
                            0xff
                        ) ==
                        '3'.code


                if (!hasId3) {

                    /*
                     * No ID3v2 tag is not corruption.
                     *
                     * Title / Artist can still be obtained from
                     * filename.
                     */

                    return SafeId3Metadata(
                        status =
                            MediaMetadataStatus
                                .MISSING
                    )
                }


                // =============================================
                // SYNCHSAFE ID3 SIZE
                // =============================================

                val size0 =
                    header[6]
                        .toInt() and
                        0xff

                val size1 =
                    header[7]
                        .toInt() and
                        0xff

                val size2 =
                    header[8]
                        .toInt() and
                        0xff

                val size3 =
                    header[9]
                        .toInt() and
                        0xff


                /*
                 * A synchsafe byte uses only seven bits.
                 *
                 * Therefore the high bit of each size byte must
                 * be zero.
                 */

                if (
                    (size0 and 0x80) != 0 ||
                    (size1 and 0x80) != 0 ||
                    (size2 and 0x80) != 0 ||
                    (size3 and 0x80) != 0
                ) {
                    return brokenId3(
                        code =
                            "INVALID_ID3_SIZE",

                        message =
                            "ID3 size không hợp lệ"
                    )
                }


                val payloadSize =
                    (
                        (size0 and 0x7f)
                            shl 21
                        ) or
                        (
                            (size1 and 0x7f)
                                shl 14
                            ) or
                        (
                            (size2 and 0x7f)
                                shl 7
                            ) or
                        (size3 and 0x7f)


                val totalSize =
                    ID3_HEADER_SIZE +
                        payloadSize


                /*
                 * Protect the process against an intentionally or
                 * accidentally corrupt ID3 size.
                 */

                if (
                    totalSize >
                    MAX_ID3_TAG_BYTES
                ) {
                    return brokenId3(
                        code =
                            "ID3_TOO_LARGE",

                        message =
                            "ID3 vượt giới hạn an toàn"
                    )
                }


                // =============================================
                // READ ONLY ID3, NOT THE WHOLE MP3
                // =============================================

                val bytes =
                    ByteArray(
                        totalSize
                    )


                System.arraycopy(
                    header,
                    0,
                    bytes,
                    0,
                    ID3_HEADER_SIZE
                )


                val remainingRead =
                    readFully(
                        input =
                            stream,

                        buffer =
                            bytes,

                        offset =
                            ID3_HEADER_SIZE,

                        length =
                            payloadSize
                    )


                if (
                    remainingRead !=
                    payloadSize
                ) {
                    return brokenId3(
                        code =
                            "TRUNCATED_ID3",

                        message =
                            "ID3 bị thiếu dữ liệu"
                    )
                }


                // =============================================
                // MEDIA3 SAFE DECODER
                // =============================================

                /*
                 * Id3Decoder.decode returns null when the ID3 data
                 * cannot be decoded.
                 *
                 * The bad file is therefore marked in DB and the
                 * scan continues to the next file.
                 */

                val metadata =
                    Id3Decoder()
                        .decode(
                            bytes,
                            bytes.size
                        )
                        ?: return brokenId3(
                            code =
                                "ID3_DECODE_FAILED",

                            message =
                                "Không giải mã được ID3"
                        )


                var title:
                    String? =
                    null

                var artist:
                    String? =
                    null

                var album:
                    String? =
                    null

                var albumArtist:
                    String? =
                    null

                var year:
                    String? =
                    null

                var rawYear:
                    String? =
                    null

                var pictureData:
                    ByteArray? =
                    null


                // =============================================
                // DECODE KNOWN FRAMES
                // =============================================

                for (
                    index in
                    0 until
                        metadata.length()
                ) {

                    when (
                        val entry =
                            metadata.get(
                                index
                            )
                    ) {

                        // -------------------------------------
                        // TEXT
                        // -------------------------------------

                        is TextInformationFrame -> {

                            val value =
                                entry.values
                                    .firstOrNull()
                                    ?.trim()
                                    ?.takeIf {
                                        it.isNotBlank()
                                    }


                            when (
                                entry.id
                            ) {

                                "TIT2",
                                "TT2" -> {

                                    if (
                                        title ==
                                        null
                                    ) {
                                        title =
                                            value
                                    }
                                }


                                "TPE1",
                                "TP1" -> {

                                    if (
                                        artist ==
                                        null
                                    ) {
                                        artist =
                                            value
                                    }
                                }


                                "TALB",
                                "TAL" -> {

                                    if (
                                        album ==
                                        null
                                    ) {
                                        album =
                                            value
                                    }
                                }


                                "TPE2",
                                "TP2" -> {

                                    if (
                                        albumArtist ==
                                        null
                                    ) {
                                        albumArtist =
                                            value
                                    }
                                }


                                "TYER",
                                "TDRC",
                                "TYE" -> {

                                    if (
                                        rawYear ==
                                        null
                                    ) {
                                        rawYear =
                                            value

                                        year =
                                            value
                                                ?.let { raw ->
                                                    Regex(
                                                        """(?<!\d)(\d{4})(?!\d)"""
                                                    )
                                                        .find(raw)
                                                        ?.groupValues
                                                        ?.getOrNull(1)
                                                }
                                    }
                                }
                            }
                        }


                        // -------------------------------------
                        // COVER
                        // -------------------------------------

                        is ApicFrame -> {

                            /*
                             * pictureType 3 = front cover.
                             *
                             * If no front cover exists, keep the
                             * first APIC frame.
                             */

                            if (
                                pictureData ==
                                null ||
                                entry.pictureType ==
                                3
                            ) {
                                pictureData =
                                    entry.pictureData
                            }
                        }
                    }
                }


                // =============================================
                // RESULT
                // =============================================

                val missing =
                    title ==
                        null ||
                        artist ==
                        null ||
                        album ==
                        null ||
                        year ==
                        null


                SafeId3Metadata(
                    title =
                        title,

                    artist =
                        artist,

                    album =
                        album,

                    albumArtist =
                        albumArtist,

                    year =
                        year,

                    rawYear =
                        rawYear,

                    pictureData =
                        pictureData,

                    status =
                        if (missing) {
                            MediaMetadataStatus
                                .MISSING
                        } else {
                            MediaMetadataStatus
                                .OK
                        }
                )
            }

        } catch (
            exception:
                SecurityException
        ) {

            SafeId3Metadata(
                status =
                    MediaMetadataStatus
                        .UNREADABLE_FILE,

                errorCode =
                    "PERMISSION_DENIED",

                errorFields =
                    ALL_TAG_FIELDS,

                errorMessage =
                    safeErrorMessage(
                        exception
                    )
            )

        } catch (
            exception:
                Exception
        ) {

            brokenId3(
                code =
                    "ID3_EXCEPTION",

                message =
                    safeErrorMessage(
                        exception
                    )
            )
        }
    }


    // ========================================================
    // BROKEN ID3
    // ========================================================

    private fun brokenId3(
        code: String,
        message: String?
    ): SafeId3Metadata {

        return SafeId3Metadata(
            status =
                MediaMetadataStatus
                    .BROKEN_METADATA,

            errorCode =
                code,

            errorFields =
                ALL_TAG_FIELDS,

            errorMessage =
                message
                    ?.take(
                        MAX_ERROR_MESSAGE_LENGTH
                    )
        )
    }


    private fun safeErrorMessage(
        exception: Throwable
    ): String {

        return (
            exception.message
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: exception
                    .javaClass
                    .simpleName
            )
            .take(
                MAX_ERROR_MESSAGE_LENGTH
            )
    }


    // ========================================================
    // READ EXACT BYTE COUNT
    // ========================================================

    private fun readFully(
        input: java.io.InputStream,
        buffer: ByteArray,
        offset: Int,
        length: Int
    ): Int {

        var total =
            0


        while (
            total <
            length
        ) {

            val count =
                input.read(
                    buffer,
                    offset + total,
                    length - total
                )


            if (
                count <=
                0
            ) {
                break
            }


            total +=
                count
        }


        return total
    }


    // ========================================================
    // TECHNICAL AUDIO DATA
    // ========================================================

    private fun readTechnicalMetadata(
        uriText: String,
        sizeBytes: Long
    ): TechnicalMetadata {

        val extractor =
            MediaExtractor()


        return try {

            extractor.setDataSource(
                applicationContext,
                Uri.parse(
                    uriText
                ),
                null
            )


            var durationMs =
                0L

            var bitrateKbps =
                0


            for (
                index in
                0 until
                    extractor.trackCount
            ) {

                val format =
                    extractor.getTrackFormat(
                        index
                    )


                val mime =
                    format
                        .getString(
                            MediaFormat
                                .KEY_MIME
                        )
                        .orEmpty()


                if (
                    !mime.startsWith(
                        "audio/"
                    )
                ) {
                    continue
                }


                // ---------------------------------------------
                // DURATION
                // ---------------------------------------------

                if (
                    format.containsKey(
                        MediaFormat
                            .KEY_DURATION
                    )
                ) {

                    durationMs =
                        runCatching {

                            format.getLong(
                                MediaFormat
                                    .KEY_DURATION
                            ) /
                                1000L

                        }
                            .getOrDefault(
                                0L
                            )
                }


                // ---------------------------------------------
                // BITRATE
                // ---------------------------------------------

                if (
                    format.containsKey(
                        MediaFormat
                            .KEY_BIT_RATE
                    )
                ) {

                    bitrateKbps =
                        runCatching {

                            format.getInteger(
                                MediaFormat
                                    .KEY_BIT_RATE
                            ) /
                                1000

                        }
                            .getOrDefault(
                                0
                            )
                }


                break
            }


            /*
             * Some MP3 streams do not expose KEY_BIT_RATE.
             *
             * bytes * 8 / milliseconds numerically gives kbps.
             */

            if (
                bitrateKbps <=
                    0 &&
                    durationMs >
                    0L &&
                    sizeBytes >
                    0L
            ) {

                bitrateKbps =
                    (
                        sizeBytes
                            .toDouble() *
                            8.0 /
                            durationMs
                        )
                        .toInt()
                        .coerceAtLeast(
                            0
                        )
            }


            TechnicalMetadata(
                durationMs =
                    durationMs,

                bitrateKbps =
                    bitrateKbps
            )

        } catch (
            _: Exception
        ) {

            /*
             * Technical fields are optional for indexing.
             *
             * A failure here must never stop metadata indexing.
             */

            TechnicalMetadata()

        } finally {

            runCatching {
                extractor.release()
            }
        }
    }


    // ========================================================
    // FALLBACK ENTITY
    // ========================================================

    private fun buildFallbackEntity(
        source: String,
        item: InventoryItem,
        signature: String,
        generation: Long,
        previous: IndexedMediaEntity?,
        errorMessage: String?
    ): IndexedMediaEntity {

        val safeMessage =
            errorMessage
                ?.take(
                    MAX_ERROR_MESSAGE_LENGTH
                )


        // ====================================================
        // EXISTING RECORD
        // ====================================================

        if (
            previous !=
            null
        ) {

            return previous.copy(
                displayName =
                    item.displayName,

                sizeBytes =
                    item.sizeBytes,

                lastModifiedMs =
                    item.lastModifiedMs,

                rawTagTitle =
                    null,

                rawTagArtist =
                    null,

                rawTagAlbum =
                    null,

                rawTagAlbumArtist =
                    null,

                metadataStatus =
                    MediaMetadataStatus
                        .UNREADABLE_FILE,

                metadataErrorCode =
                    "INDEX_EXCEPTION",

                metadataErrorFields =
                    ALL_TAG_FIELDS,

                metadataErrorMessage =
                    safeMessage,

                /*
                 * Save the current signature.
                 *
                 * Therefore merely changing tabs does not retry
                 * this broken file forever.
                 *
                 * Modifying the actual MP3 changes the signature
                 * and it will be indexed again.
                 */

                contentSignature =
                    signature,

                scanGeneration =
                    generation,

                indexedAt =
                    System.currentTimeMillis()
            )
        }


        // ====================================================
        // NEW RECORD - FALLBACK TO FILENAME
        // ====================================================

        val parsed =
            SongNameMatcher
                .parseFileName(
                    item.displayName
                )


        val stem =
            item.displayName
                .substringBeforeLast(
                    delimiter = ".",
                    missingDelimiterValue =
                        item.displayName
                )
                .trim()


        val title =
            parsed
                ?.title
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: stem


        val artist =
            parsed
                ?.artist
                .orEmpty()


        return buildEntity(
            source =
                source,

            item =
                item,

            signature =
                signature,

            generation =
                generation,

            fileTitle =
                title,

            fileArtist =
                artist,

            tagTitle =
                "",

            tagArtist =
                "",

            album =
                "",

            albumArtist =
                "",

            year =
                "",

            rawTagTitle =
                null,

            rawTagArtist =
                null,

            rawTagAlbum =
                null,

            rawTagAlbumArtist =
                null,

            rawTagYear =
                null,

            metadataStatus =
                MediaMetadataStatus
                    .UNREADABLE_FILE,

            metadataErrorCode =
                "INDEX_EXCEPTION",

            metadataErrorFields =
                ALL_TAG_FIELDS,

            metadataErrorMessage =
                safeMessage,

            title =
                title,

            artist =
                artist,

            durationMs =
                0L,

            bitrateKbps =
                0,

            coverPath =
                null
        )
    }


    // ========================================================
    // BUILD ENTITY
    // ========================================================

    private fun buildEntity(
        source: String,
        item: InventoryItem,
        signature: String,
        generation: Long,

        fileTitle: String,
        fileArtist: String,

        tagTitle: String,
        tagArtist: String,
        album: String,
        albumArtist: String,
        year: String,

        rawTagTitle: String?,
        rawTagArtist: String?,
        rawTagAlbum: String?,
        rawTagAlbumArtist: String?,
        rawTagYear: String?,

        metadataStatus: String,
        metadataErrorCode: String?,
        metadataErrorFields: String?,
        metadataErrorMessage: String?,

        title: String,
        artist: String,

        durationMs: Long,
        bitrateKbps: Int,
        coverPath: String?
    ): IndexedMediaEntity {

        val stem =
            item.displayName
                .substringBeforeLast(
                    delimiter = ".",
                    missingDelimiterValue =
                        item.displayName
                )


        val normalizedTitle =
            SongNameMatcher
                .normalizeTitle(
                    title
                )


        val normalizedArtist =
            SongNameMatcher
                .normalizeText(
                    artist
                )


        val normalizedAlbum =
            SongNameMatcher
                .normalizeText(
                    album
                )


        val normalizedFileName =
            SongNameMatcher
                .normalizeText(
                    stem
                )


        val titleTokens =
            normalizedTitle
                .split(' ')
                .asSequence()
                .map {
                    it.trim()
                }
                .filter {
                    it.length >= 2
                }
                .distinct()
                .joinToString(" ")


        return IndexedMediaEntity(
            uri =
                item.uri,

            source =
                source,

            treeUri =
                item.treeUri,

            documentId =
                item.documentId,

            displayName =
                item.displayName,

            mimeType =
                item.mimeType,

            sizeBytes =
                item.sizeBytes,

            lastModifiedMs =
                item.lastModifiedMs,

            fileTitle =
                fileTitle,

            fileArtist =
                fileArtist,

            tagTitle =
                tagTitle,

            tagArtist =
                tagArtist,

            album =
                album,

            albumArtist =
                albumArtist,

            year =
                year,

            rawTagTitle =
                rawTagTitle,

            rawTagArtist =
                rawTagArtist,

            rawTagAlbum =
                rawTagAlbum,

            rawTagAlbumArtist =
                rawTagAlbumArtist,

            rawTagYear =
                rawTagYear,

            metadataStatus =
                metadataStatus,

            metadataErrorCode =
                metadataErrorCode,

            metadataErrorFields =
                metadataErrorFields,

            metadataErrorMessage =
                metadataErrorMessage,

            title =
                title,

            artist =
                artist,

            canonicalFileName =
                normalizedFileName,

            normalizedFileName =
                normalizedFileName,

            normalizedTitle =
                normalizedTitle,

            normalizedArtist =
                normalizedArtist,

            normalizedAlbum =
                normalizedAlbum,

            titleTokens =
                titleTokens,

            durationMs =
                durationMs,

            bitrateKbps =
                bitrateKbps,

            coverPath =
                coverPath,

            contentSignature =
                signature,

            scanGeneration =
                generation,

            indexedAt =
                System.currentTimeMillis()
        )
    }


    // ========================================================
    // INTERNAL MODELS
    // ========================================================

    private data class SafeId3Metadata(
        val title: String? =
            null,

        val artist: String? =
            null,

        val album: String? =
            null,

        val albumArtist: String? =
            null,

        val year: String? =
            null,

        val rawYear: String? =
            null,

        val pictureData: ByteArray? =
            null,

        val status: String =
            MediaMetadataStatus
                .MISSING,

        val errorCode: String? =
            null,

        val errorFields: String? =
            null,

        val errorMessage: String? =
            null
    )


    private data class TechnicalMetadata(
        val durationMs: Long =
            0L,

        val bitrateKbps: Int =
            0
    )

    // ========================================================
    // COVER STORE
    // ========================================================

    private fun saveCover(
        uri: String,
        bytes: ByteArray?,
        oldCoverPath: String?
    ): String? {

        if (
            bytes == null ||
            bytes.isEmpty()
        ) {
            deleteCoverIfOwned(
                oldCoverPath
            )

            return null
        }

        coverDirectory.mkdirs()

        val outputFile =
            File(
                coverDirectory,
                "${sha256(uri)}.jpg"
            )

        val bounds =
            BitmapFactory.Options()
                .apply {
                    inJustDecodeBounds =
                        true
                }

        BitmapFactory.decodeByteArray(
            bytes,
            0,
            bytes.size,
            bounds
        )

        var sample = 1

        while (
            bounds.outWidth /
                sample >
                COVER_DECODE_TARGET ||
            bounds.outHeight /
                sample >
                COVER_DECODE_TARGET
        ) {
            sample *= 2
        }

        val options =
            BitmapFactory.Options()
                .apply {
                    inSampleSize =
                        sample.coerceAtLeast(1)
                }

        val decoded =
            BitmapFactory.decodeByteArray(
                bytes,
                0,
                bytes.size,
                options
            )
                ?: return null

        val largestSide =
            maxOf(
                decoded.width,
                decoded.height
            )

        val scale =
            if (
                largestSide <=
                COVER_MAX_SIZE
            ) {
                1.0
            } else {
                COVER_MAX_SIZE
                    .toDouble()
                    .div(
                        largestSide
                            .toDouble()
                    )
            }

        val finalBitmap =
            if (scale >= 1.0) {
                decoded
            } else {
                Bitmap.createScaledBitmap(
                    decoded,
                    (
                        decoded.width *
                            scale
                        )
                        .toInt()
                        .coerceAtLeast(1),
                    (
                        decoded.height *
                            scale
                        )
                        .toInt()
                        .coerceAtLeast(1),
                    true
                )
            }

        try {
            FileOutputStream(
                outputFile,
                false
            ).use {
                output ->

                finalBitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    COVER_JPEG_QUALITY,
                    output
                )
            }
        } finally {
            if (
                finalBitmap !==
                decoded
            ) {
                finalBitmap.recycle()
            }

            decoded.recycle()
        }

        return outputFile
            .absolutePath
    }

    private fun deleteCoverIfOwned(
        path: String?
    ) {
        val file =
            path
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let(::File)
                ?: return

        if (
            file.parentFile ==
            coverDirectory
        ) {
            runCatching {
                file.delete()
            }
        }
    }

    private suspend fun pruneOrphanCovers() {
        withContext(
            Dispatchers.IO
        ) {
            if (!coverDirectory.isDirectory) {
                return@withContext
            }

            val used =
                (
                    dao.getBySource(
                        MediaIndexSource
                            .REFERENCE
                    ) +
                        dao.getBySource(
                            MediaIndexSource
                                .DOWNLOAD
                        )
                    )
                    .mapNotNull {
                        it.coverPath
                    }
                    .toSet()

            coverDirectory
                .listFiles()
                .orEmpty()
                .forEach {
                    file ->

                    if (
                        file.absolutePath !in
                        used
                    ) {
                        runCatching {
                            file.delete()
                        }
                    }
                }
        }
    }

    // ========================================================
    // HELPERS
    // ========================================================

    private fun contentSignature(
        uri: String,
        sizeBytes: Long,
        lastModifiedMs: Long
    ): String {
        return buildString {
            append(uri)
            append('|')
            append(sizeBytes)
            append('|')
            append(lastModifiedMs)
        }
    }

    private fun sha256(
        value: String
    ): String {
        return MessageDigest
            .getInstance(
                "SHA-256"
            )
            .digest(
                value.toByteArray(
                    Charsets.UTF_8
                )
            )
            .joinToString(
                separator = ""
            ) { byte ->
                "%02x".format(
                    byte.toInt() and 0xff
                )
            }
    }

    private fun MediaIndexStateEntity
        .toSummary(): MediaIndexSummary {
        return MediaIndexSummary(
            totalFiles = totalFiles,
            failedFiles = failedFiles,
            coverFiles = coverFiles,
            artistCount = artistCount,
            albumCount = albumCount,
            updatedAt = updatedAt,
            treeUri = treeUri
        )
    }

    private data class InventoryItem(
        val uri: String,
        val treeUri: String?,
        val documentId: String?,
        val displayName: String,
        val mimeType: String,
        val sizeBytes: Long,
        val lastModifiedMs: Long
    )
}
