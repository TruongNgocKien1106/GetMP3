package com.ngoctien.getmp3.download

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.ngoctien.getmp3.settings.AppSettings
import com.ngoctien.getmp3.storage.resolveSafDirectoryDocumentId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class SavedAudio(
    val uri: Uri,
    val displayName: String,
    val bytesWritten: Long
)

class MediaStoreWriter(
    private val context: Context
) {
    companion object {
        private const val MIME_TYPE =
            "audio/mpeg"
    }

    suspend fun saveMp3(
        sourceFile: File,
        title: String,
        artist: String,
        settings: AppSettings
    ): SavedAudio {
        return withContext(Dispatchers.IO) {
            if (
                !sourceFile.isFile ||
                sourceFile.length() <= 0L
            ) {
                throw IllegalArgumentException(
                    "File MP3 nguồn không hợp lệ"
                )
            }

            val baseName =
                buildBaseFileName(
                    title = title,
                    artist = artist
                )

            if (
                settings
                    .usesCustomInboxFolder
            ) {

                val inboxTreeUriText =
                    settings
                        .inboxTreeUri
                        ?: throw IllegalStateException(
                            "Inbox đã chọn nhưng không có Tree URI"
                        )

                saveToDocumentTree(
                    sourceFile =
                        sourceFile,

                    baseName =
                        baseName,

                    treeUri =
                        Uri.parse(
                            inboxTreeUriText
                        )
                )

            } else {
                saveToMediaStore(
                    sourceFile = sourceFile,
                    baseName = baseName
                )
            }
        }
    }

    private fun saveToMediaStore(
        sourceFile: File,
        baseName: String
    ): SavedAudio {
        val resolver =
            context.contentResolver

        val collection =
            MediaStore.Audio.Media
                .getContentUri(
                    MediaStore
                        .VOLUME_EXTERNAL_PRIMARY
                )

        val displayName =
            findAvailableMediaStoreName(
                baseName = baseName
            )

        val values =
            ContentValues().apply {
                put(
                    MediaStore.Audio.Media
                        .DISPLAY_NAME,

                    displayName
                )

                put(
                    MediaStore.Audio.Media
                        .MIME_TYPE,

                    MIME_TYPE
                )

                put(
                    MediaStore.Audio.Media
                        .RELATIVE_PATH,

                    AppSettings
                        .DEFAULT_RELATIVE_PATH
                )

                put(
                    MediaStore.Audio.Media
                        .IS_PENDING,

                    1
                )
            }

        val destinationUri =
            resolver.insert(
                collection,
                values
            ) ?: throw IllegalStateException(
                "Không thể tạo file trong MediaStore"
            )

        try {
            val output =
                resolver.openOutputStream(
                    destinationUri,
                    "w"
                ) ?: throw IllegalStateException(
                    "Không mở được OutputStream"
                )

            val bytesWritten =
                output.use { outputStream ->
                    sourceFile
                        .inputStream()
                        .use { inputStream ->
                            inputStream.copyTo(
                                outputStream
                            )
                        }
                }

            verifyCopiedBytes(
                sourceFile = sourceFile,
                copiedBytes = bytesWritten
            )

            val completedValues =
                ContentValues().apply {
                    put(
                        MediaStore.Audio.Media
                            .IS_PENDING,

                        0
                    )
                }

            val updated =
                resolver.update(
                    destinationUri,
                    completedValues,
                    null,
                    null
                )

            if (updated <= 0) {
                throw IllegalStateException(
                    "Không thể công bố file MP3"
                )
            }

            return SavedAudio(
                uri = destinationUri,
                displayName = displayName,
                bytesWritten = bytesWritten
            )
        } catch (exception: Exception) {
            resolver.delete(
                destinationUri,
                null,
                null
            )

            throw exception
        }
    }

    private fun saveToDocumentTree(
        sourceFile: File,
        baseName: String,
        treeUri: Uri
    ): SavedAudio {
        val resolver =
            context.contentResolver

        /*
         * URI nhận từ ACTION_OPEN_DOCUMENT_TREE có dạng:
         *
         * content://authority/tree/documentId
         *
         * Đây là Tree URI, không được truyền trực tiếp
         * vào DocumentsContract.createDocument().
         */
        if (!DocumentsContract.isTreeUri(treeUri)) {
            throw IllegalArgumentException(
                "Inbox không phải SAF Tree URI hợp lệ"
            )
        }

        /*
         * Ví dụ:
         *
         * Tree URI:
         * content://.../tree/primary%3AMusic%2FGetMP3
         *
         * Tree document ID:
         * primary:Music/GetMP3
         */
        val treeDocumentId =
            try {
                resolveSafDirectoryDocumentId(
                    treeUri
                )
            } catch (exception: Exception) {
                throw IllegalArgumentException(
                    "Không đọc được Inbox đã chọn",
                    exception
                )
            }

        /*
         * createDocument() yêu cầu URI của document cha.
         *
         * Vì vậy phải đổi Tree URI thành dạng:
         *
         * content://.../tree/.../document/...
         */
        val parentDocumentUri =
            try {
                DocumentsContract
                    .buildDocumentUriUsingTree(
                        treeUri,
                        treeDocumentId
                    )
            } catch (exception: Exception) {
                throw IllegalArgumentException(
                    "Không tạo được URI Inbox hợp lệ",
                    exception
                )
            }

        val displayName =
            try {
                findAvailableTreeName(
                    treeUri = treeUri,
                    treeDocumentId =
                        treeDocumentId,
                    baseName = baseName
                )
            } catch (exception: SecurityException) {
                throw IllegalStateException(
                    "App không còn quyền đọc Inbox. " +
                        "Hãy chọn lại thư mục trong Cài đặt.",
                    exception
                )
            }

        /*
         * Đây mới là URI đúng để tạo file con.
         */
        val destinationUri =
            try {
                DocumentsContract
                    .createDocument(
                        resolver,
                        parentDocumentUri,
                        MIME_TYPE,
                        displayName
                    )
            } catch (exception: SecurityException) {
                throw IllegalStateException(
                    "App không còn quyền ghi vào Inbox. " +
                        "Hãy chọn lại thư mục trong Cài đặt.",
                    exception
                )
            } ?: throw IllegalStateException(
                "Không thể tạo file trong thư mục đã chọn"
            )

        try {
            val output =
                resolver.openOutputStream(
                    destinationUri,
                    "w"
                ) ?: throw IllegalStateException(
                    "Không thể mở file đích"
                )

            val bytesWritten =
                output.use { outputStream ->
                    sourceFile
                        .inputStream()
                        .use { inputStream ->
                            inputStream.copyTo(
                                outputStream
                            )
                        }
                }

            verifyCopiedBytes(
                sourceFile = sourceFile,
                copiedBytes = bytesWritten
            )

            return SavedAudio(
                uri = destinationUri,
                displayName = displayName,
                bytesWritten = bytesWritten
            )
        } catch (exception: Exception) {
            /*
             * Nếu copy lỗi giữa chừng thì xóa file dở,
             * tránh để lại MP3 0 byte hoặc file hỏng.
             */
            runCatching {
                DocumentsContract
                    .deleteDocument(
                        resolver,
                        destinationUri
                    )
            }

            throw exception
        }
    }

    private fun findAvailableMediaStoreName(
        baseName: String
    ): String {
        val resolver =
            context.contentResolver

        val collection =
            MediaStore.Audio.Media
                .getContentUri(
                    MediaStore
                        .VOLUME_EXTERNAL_PRIMARY
                )

        var index = 0

        while (true) {
            val candidate =
                candidateName(
                    baseName,
                    index
                )

            val exists =
                resolver.query(
                    collection,

                    arrayOf(
                        MediaStore.Audio.Media._ID
                    ),

                    (
                        "${MediaStore.Audio.Media.DISPLAY_NAME} = ? " +
                            "AND " +
                            "${MediaStore.Audio.Media.RELATIVE_PATH} = ?"
                        ),

                    arrayOf(
                        candidate,
                        AppSettings
                            .DEFAULT_RELATIVE_PATH
                    ),

                    null
                )?.use {
                    it.moveToFirst()
                } ?: false

            if (!exists) {
                return candidate
            }

            index++
        }
    }

    private fun findAvailableTreeName(
        treeUri: Uri,
        treeDocumentId: String,
        baseName: String
    ): String {
        val resolver =
            context.contentResolver

        /*
         * Query các file con trực tiếp của thư mục SAF
         * để tránh ghi đè file đã có.
         */
        val childrenUri =
            DocumentsContract
                .buildChildDocumentsUriUsingTree(
                    treeUri,
                    treeDocumentId
                )

        val existingNames =
            mutableSetOf<String>()

        resolver.query(
            childrenUri,

            arrayOf(
                DocumentsContract
                    .Document
                    .COLUMN_DISPLAY_NAME
            ),

            null,
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                cursor.getString(0)
                    ?.lowercase()
                    ?.let(
                        existingNames::add
                    )
            }
        }

        var index = 0

        while (true) {
            val candidate =
                candidateName(
                    baseName,
                    index
                )

            if (
                candidate.lowercase()
                    !in existingNames
            ) {
                return candidate
            }

            index++
        }
    }

    private fun candidateName(
        baseName: String,
        index: Int
    ): String {
        return if (index == 0) {
            "$baseName.mp3"
        } else {
            "$baseName ($index).mp3"
        }
    }

    private fun buildBaseFileName(
        title: String,
        artist: String
    ): String {
        val raw =
            buildString {
                append(
                    title.ifBlank {
                        "Unknown Title"
                    }
                )

                append(" - ")

                append(
                    artist.ifBlank {
                        "Unknown Artist"
                    }
                )
            }

        return raw
            .replace(
                Regex(
                    """[\\/:*?"<>|]"""
                ),
                "_"
            )
            .replace(
                Regex(
                    """[\u0000-\u001F\u007F]"""
                ),
                ""
            )
            .replace(
                Regex(
                    """\s+"""
                ),
                " "
            )
            .trim()
            .trimEnd(
                '.',
                ' '
            )
            .take(150)
            .ifBlank {
                "Unknown Title - Unknown Artist"
            }
    }

    private fun verifyCopiedBytes(
        sourceFile: File,
        copiedBytes: Long
    ) {
        if (copiedBytes <= 0L) {
            throw IllegalStateException(
                "Không có dữ liệu được ghi"
            )
        }

        if (
            copiedBytes !=
            sourceFile.length()
        ) {
            throw IllegalStateException(
                "Dung lượng file sau khi ghi không khớp"
            )
        }
    }
}