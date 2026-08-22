package com.ngoctien.getmp3.library

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.ngoctien.getmp3.settings.AppSettingsRepository
import com.ngoctien.getmp3.storage.resolveSafDirectoryDocumentId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LibraryPromotionResult(
    val oldUri: String,
    val newUri: String,
    val displayName: String,
    val bytesWritten: Long,
    val indexUpdated: Boolean = true,
    val warningMessage: String? = null
)

class InboxWorkflowRepository(
    context: Context
) {

    companion object {

        private const val MP3_MIME_TYPE =
            "audio/mpeg"

        private const val VERIFY_BUFFER_SIZE =
            64 * 1024
    }

    private val applicationContext =
        context.applicationContext

    private val resolver =
        applicationContext
            .contentResolver

    private val settingsRepository =
        AppSettingsRepository(
            applicationContext
        )

    private val mediaIndexRepository =
        MediaIndexRepository(
            applicationContext
        )

    suspend fun promoteToLibrary(
        sourceUriText: String
    ): LibraryPromotionResult {

        return withContext(
            Dispatchers.IO
        ) {

            val item =
                mediaIndexRepository
                    .getByUri(
                        sourceUriText
                    )
                    ?: throw IllegalStateException(
                        "Không tìm thấy file trong Media Index"
                    )

            LibraryAdmissionPolicy
                .requireReady(
                    item
                )

            val settings =
                settingsRepository
                    .getSettings()

            val libraryTreeUriText =
                settings
                    .libraryTreeUri
                    ?.trim()
                    ?.takeIf(
                        String::isNotBlank
                    )
                    ?: throw IllegalStateException(
                        "Chưa chọn Library trong Cài đặt"
                    )

            if (
                item.treeUri != null &&
                item.treeUri ==
                libraryTreeUriText
            ) {
                throw IllegalStateException(
                    "Inbox và Library không được là cùng một thư mục"
                )
            }

            val indexedDuplicate =
                mediaIndexRepository
                    .findLibraryByCanonicalFileName(
                        item.canonicalFileName
                    )

            if (indexedDuplicate != null) {
                throw IllegalStateException(
                    "Library đã có bài cùng filename chuẩn: " +
                        indexedDuplicate.displayName
                )
            }

            val libraryTreeUri =
                Uri.parse(
                    libraryTreeUriText
                )

            if (
                !DocumentsContract
                    .isTreeUri(
                        libraryTreeUri
                    )
            ) {
                throw IllegalStateException(
                    "Library không phải SAF Tree URI hợp lệ"
                )
            }

            val treeDocumentId =
                resolveSafDirectoryDocumentId(
                    libraryTreeUri
                )

            val parentDocumentUri =
                DocumentsContract
                    .buildDocumentUriUsingTree(
                        libraryTreeUri,
                        treeDocumentId
                    )

            val existingDestination =
                findChildByDisplayName(
                    treeUri =
                        libraryTreeUri,

                    treeDocumentId =
                        treeDocumentId,

                    displayName =
                        item.displayName
                )

            if (existingDestination != null) {
                throw IllegalStateException(
                    "Library đã có file: " +
                        item.displayName
                )
            }

            val destinationUri =
                DocumentsContract
                    .createDocument(
                        resolver,
                        parentDocumentUri,
                        MP3_MIME_TYPE,
                        item.displayName
                    )
                    ?: throw IllegalStateException(
                        "Không tạo được file trong Library"
                    )

            val sourceUri =
                Uri.parse(
                    item.uri
                )

            var sourceDeleted =
                false

            try {

                val bytesWritten =
                    copyFile(
                        sourceUri =
                            sourceUri,

                        destinationUri =
                            destinationUri
                    )

                if (
                    bytesWritten <=
                    0L
                ) {
                    throw IllegalStateException(
                        "File copy sang Library rỗng"
                    )
                }

                if (
                    item.sizeBytes >
                    0L &&
                    bytesWritten !=
                    item.sizeBytes
                ) {
                    throw IllegalStateException(
                        "Kích thước file sau copy không khớp"
                    )
                }

                /*
                 * Đọc lại destination thật sự.
                 *
                 * Không chỉ tin vào số byte mà copyTo()
                 * trả về.
                 */
                val verifiedBytes =
                    readDestinationSize(
                        destinationUri
                    )

                if (
                    verifiedBytes !=
                    bytesWritten
                ) {
                    throw IllegalStateException(
                        "File Library sau khi đọc lại không đủ dữ liệu"
                    )
                }

                /*
                 * Chỉ xóa Inbox sau khi destination
                 * đã được xác minh.
                 */
                sourceDeleted =
                    deleteSource(
                        sourceUri
                    )

                if (!sourceDeleted) {

                    /*
                     * Source vẫn tồn tại nên rollback destination
                     * là an toàn.
                     */
                    deleteDestinationQuietly(
                        destinationUri
                    )

                    throw IllegalStateException(
                        "Không xóa được file Inbox. " +
                            "Bản Library đã được rollback."
                    )
                }

                /*
                 * =================================================
                 * TỪ ĐÂY:
                 *
                 * SOURCE INBOX ĐÃ BỊ XÓA.
                 *
                 * Không được phép xóa destination Library nữa.
                 * =================================================
                 */

                val promoteResult =
                    runCatching {

                        mediaIndexRepository
                            .promoteInboxItemToLibrary(
                                oldInboxUri =
                                    item.uri,

                                newLibraryUri =
                                    destinationUri
                                        .toString(),

                                libraryTreeUri =
                                    libraryTreeUriText
                            )
                    }

                if (promoteResult.isSuccess) {

                    LibraryPromotionResult(
                        oldUri =
                            item.uri,

                        newUri =
                            destinationUri
                                .toString(),

                        displayName =
                            item.displayName,

                        bytesWritten =
                            bytesWritten,

                        indexUpdated =
                            true
                    )

                }
                else {

                    val firstIndexError =
                        promoteResult
                            .exceptionOrNull()

                    /*
                     * Room promotion lỗi.
                     *
                     * Re-index đúng một destination file.
                     */
                    val recoveryResult =
                        runCatching {

                            mediaIndexRepository
                                .indexLibraryFile(
                                    uri =
                                        destinationUri
                                            .toString(),

                                    displayName =
                                        item.displayName,

                                    sizeBytes =
                                        bytesWritten,

                                    treeUri =
                                        libraryTreeUriText
                                )

                            /*
                             * Xóa row Inbox cũ nếu nó còn tồn tại.
                             */
                            mediaIndexRepository
                                .deleteUri(
                                    item.uri
                                )
                        }

                    if (recoveryResult.isSuccess) {

                        LibraryPromotionResult(
                            oldUri =
                                item.uri,

                            newUri =
                                destinationUri
                                    .toString(),

                            displayName =
                                item.displayName,

                            bytesWritten =
                                bytesWritten,

                            indexUpdated =
                                true,

                            warningMessage =
                                "Media Index đã được tự khôi phục"
                        )

                    }
                    else {

                        val recoveryError =
                            recoveryResult
                                .exceptionOrNull()

                        /*
                         * File thật vẫn an toàn trong Library.
                         *
                         * Không rollback file vật lý chỉ vì Room.
                         */
                        LibraryPromotionResult(
                            oldUri =
                                item.uri,

                            newUri =
                                destinationUri
                                    .toString(),

                            displayName =
                                item.displayName,

                            bytesWritten =
                                bytesWritten,

                            indexUpdated =
                                false,

                            warningMessage =
                                buildString {

                                    append(
                                        "File đã an toàn trong Library"
                                    )

                                    firstIndexError
                                        ?.message
                                        ?.takeIf(
                                            String::isNotBlank
                                        )
                                        ?.let {
                                                message ->

                                            append(
                                                " • Index: "
                                            )

                                            append(
                                                message.take(
                                                    160
                                                )
                                            )
                                        }

                                    recoveryError
                                        ?.message
                                        ?.takeIf(
                                            String::isNotBlank
                                        )
                                        ?.let {
                                                message ->

                                            append(
                                                " • Recovery: "
                                            )

                                            append(
                                                message.take(
                                                    160
                                                )
                                            )
                                        }
                                }
                        )
                    }
                }

            }
            catch (
                exception: Exception
            ) {

                /*
                 * Chỉ rollback destination nếu source
                 * vẫn chưa bị xóa.
                 */
                if (!sourceDeleted) {

                    deleteDestinationQuietly(
                        destinationUri
                    )
                }

                throw exception
            }
        }
    }

    private fun copyFile(
        sourceUri: Uri,
        destinationUri: Uri
    ): Long {

        val input =
            resolver
                .openInputStream(
                    sourceUri
                )
                ?: throw IllegalStateException(
                    "Không mở được file trong Inbox"
                )

        val output =
            resolver
                .openOutputStream(
                    destinationUri,
                    "w"
                )
                ?: throw IllegalStateException(
                    "Không mở được file đích trong Library"
                )

        return input.use {
                inputStream ->

            output.use {
                    outputStream ->

                inputStream.copyTo(
                    outputStream
                )
            }
        }
    }

    private fun readDestinationSize(
        uri: Uri
    ): Long {

        val input =
            resolver
                .openInputStream(
                    uri
                )
                ?: throw IllegalStateException(
                    "Không đọc lại được file Library"
                )

        return input.use {
                stream ->

            val buffer =
                ByteArray(
                    VERIFY_BUFFER_SIZE
                )

            var total =
                0L

            while (true) {

                val count =
                    stream.read(
                        buffer
                    )

                if (count < 0) {
                    break
                }

                total +=
                    count.toLong()
            }

            total
        }
    }

    private fun findChildByDisplayName(
        treeUri: Uri,
        treeDocumentId: String,
        displayName: String
    ): Uri? {

        val childrenUri =
            DocumentsContract
                .buildChildDocumentsUriUsingTree(
                    treeUri,
                    treeDocumentId
                )

        val projection =
            arrayOf(
                DocumentsContract
                    .Document
                    .COLUMN_DOCUMENT_ID,

                DocumentsContract
                    .Document
                    .COLUMN_DISPLAY_NAME
            )

        /*
         * Không dùng labeled return của use ở đây.
         *
         * Cursor được close bằng try/finally để return type
         * luôn tường minh là Uri?.
         */
        val cursor =
            resolver.query(
                childrenUri,
                projection,
                null,
                null,
                null
            )
                ?: return null

        try {

            val idIndex =
                cursor
                    .getColumnIndexOrThrow(
                        DocumentsContract
                            .Document
                            .COLUMN_DOCUMENT_ID
                    )

            val nameIndex =
                cursor
                    .getColumnIndexOrThrow(
                        DocumentsContract
                            .Document
                            .COLUMN_DISPLAY_NAME
                    )

            while (
                cursor.moveToNext()
            ) {

                val name =
                    cursor.getString(
                        nameIndex
                    )

                if (
                    name.equals(
                        displayName,
                        ignoreCase = true
                    )
                ) {

                    val documentId =
                        cursor.getString(
                            idIndex
                        )

                    return DocumentsContract
                        .buildDocumentUriUsingTree(
                            treeUri,
                            documentId
                        )
                }
            }

        }
        finally {

            cursor.close()
        }

        return null
    }

    private fun deleteSource(
        uri: Uri
    ): Boolean {

        return try {

            if (
                DocumentsContract
                    .isDocumentUri(
                        applicationContext,
                        uri
                    )
            ) {

                DocumentsContract
                    .deleteDocument(
                        resolver,
                        uri
                    )

            }
            else {

                resolver.delete(
                    uri,
                    null,
                    null
                ) > 0
            }

        }
        catch (
            _: Exception
        ) {

            false
        }
    }

    private fun deleteDestinationQuietly(
        uri: Uri
    ) {

        runCatching {

            DocumentsContract
                .deleteDocument(
                    resolver,
                    uri
                )
        }
    }
}
