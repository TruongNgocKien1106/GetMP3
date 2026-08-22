package com.ngoctien.getmp3.tag

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.ngoctien.getmp3.storage.resolveSafDirectoryDocumentId
import com.ngoctien.getmp3.data.AppDatabase
import com.ngoctien.getmp3.data.DownloadRepository
import com.ngoctien.getmp3.note.SongNameMatcher
import com.ngoctien.getmp3.python.EditorTagReadResult
import com.ngoctien.getmp3.python.EditorTagWriteResult
import com.ngoctien.getmp3.python.Mp3TagBridge
import com.ngoctien.getmp3.settings.AppSettings
import com.ngoctien.getmp3.settings.AppSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.absoluteValue

class TagEditorRepository(
    context: Context
) {
    companion object {
        private const val MIME_TYPE =
            "audio/mpeg"
    }

    private val applicationContext =
        context.applicationContext

    private val resolver =
        applicationContext.contentResolver

    private val bridge =
        Mp3TagBridge(applicationContext)

    private val settingsRepository =
        AppSettingsRepository(
            applicationContext
        )

    private val downloadRepository =
        DownloadRepository(
            AppDatabase
                .getDatabase(applicationContext)
                .downloadJobDao()
        )

    private val cacheDirectory =
        File(
            applicationContext.cacheDir,
            "tag_editor"
        )

    suspend fun scanSongs():
        List<MediaSongFile> {

        return withContext(Dispatchers.IO) {
            cacheDirectory.mkdirs()

            val settings =
                settingsRepository.getSettings()

            if (settings.usesCustomFolder) {
                scanDocumentTree(
                    Uri.parse(
                        settings.downloadTreeUri
                    )
                )
            } else {
                scanDefaultMediaStoreFolder()
            }
        }
    }

    suspend fun loadSong(
        file: MediaSongFile
    ): EditableSong {
        return withContext(Dispatchers.IO) {
            cacheDirectory.mkdirs()

            val temporaryMp3 =
                File(
                    cacheDirectory,
                    "read_${file.id}.mp3"
                )

            val temporaryCover =
                File(
                    cacheDirectory,
                    "cover_${file.id}.img"
                )

            temporaryMp3.delete()
            temporaryCover.delete()

            copyUriToFile(
                uri = Uri.parse(file.uri),
                destination = temporaryMp3
            )

            try {
                when (
                    val result =
                        bridge.readTags(
                            mp3Path =
                                temporaryMp3
                                    .absolutePath,

                            coverOutputPath =
                                temporaryCover
                                    .absolutePath
                        )
                ) {
                    is EditorTagReadResult.Success -> {
                        val parsedFileName =
                            SongNameMatcher
                                .parseFileName(
                                    file.displayName
                                )

                        val fileStem =
                            file.displayName
                                .substringBeforeLast(
                                    delimiter = ".",
                                    missingDelimiterValue =
                                        file.displayName
                                )
                                .trim()

                        EditableSong(
                            file = file,

                            title =
                                parsedFileName
                                    ?.title
                                    ?.takeIf(
                                        String::isNotBlank
                                    )
                                    ?: fileStem,

                            artist =
                                parsedFileName
                                    ?.artist
                                    .orEmpty(),

                            album =
                                result.album,

                            coverPath =
                                result.coverPath,

                            year =
                                result.year
                        )
                    }

                    is EditorTagReadResult.Error -> {
                        throw IllegalStateException(
                            result.message
                        )
                    }
                }
            } finally {
                temporaryMp3.delete()
            }
        }
    }

    suspend fun saveSong(
        song: EditableSong,
        title: String,
        artist: String,
        album: String,
        year: String
    ): EditableSong {
        return withContext(Dispatchers.IO) {
            cacheDirectory.mkdirs()

            val sourceUri =
                Uri.parse(song.file.uri)

            val temporaryMp3 =
                File(
                    cacheDirectory,
                    "edit_${song.file.id}.mp3"
                )

            temporaryMp3.delete()

            copyUriToFile(
                uri = sourceUri,
                destination = temporaryMp3
            )

            val cleanTitle =
                title.trim()

            val cleanArtist =
                artist.trim()

            val cleanAlbum =
                album.trim()

            val cleanYear =
                year.trim()

            if (
                cleanYear.isNotBlank() &&
                !Regex("""\d{4}""")
                    .matches(
                        cleanYear
                    )
            ) {
                throw IllegalArgumentException(
                    "Year phải gồm đúng 4 chữ số"
                )
            }

            val updatedFile: MediaSongFile

            try {
                when (
                    val result =
                        bridge.writeTags(
                            mp3Path =
                                temporaryMp3
                                    .absolutePath,

                            title = cleanTitle,
                            artist = cleanArtist,
                            album = cleanAlbum,
                            year = cleanYear
                        )
                ) {
                    is EditorTagWriteResult.Success -> {
                        if (
                            !result.id3Version
                                .startsWith("2.3")
                        ) {
                            throw IllegalStateException(
                                "Tag không phải ID3v2.3"
                            )
                        }
                    }

                    is EditorTagWriteResult.Error -> {
                        throw IllegalStateException(
                            result.message
                        )
                    }
                }

                updatedFile =
                    publishEditedFile(
                        originalFile =
                            song.file,

                        sourceFile =
                            temporaryMp3,

                        title =
                            cleanTitle,

                        artist =
                            cleanArtist,

                        album =
                            cleanAlbum,

                        year =
                            cleanYear
                    )
            } finally {
                temporaryMp3.delete()
            }

            downloadRepository
                .updateOutputReference(
                    oldUri = song.file.uri,
                    newUri = updatedFile.uri,
                    title = cleanTitle,
                    artist = cleanArtist
                )

            /*
             * Không mở lại MediaStore URI ngay sau khi vừa ghi và đổi tên.
             * Một số thiết bị re-index MediaStore trong thời gian ngắn,
             * khiến URI vừa lưu tạm thời báo No item at content URI.
             */
            EditableSong(
                file = updatedFile,
                title = cleanTitle,
                artist = cleanArtist,
                album = cleanAlbum,
                coverPath = song.coverPath,
                year = cleanYear
            )
        }
    }

    suspend fun deleteSong(
        file: MediaSongFile
    ) {
        withContext(Dispatchers.IO) {
            val uri =
                Uri.parse(file.uri)

            val deleted =
                try {
                    if (
                        !file.treeUri
                            .isNullOrBlank()
                    ) {
                        DocumentsContract
                            .deleteDocument(
                                resolver,
                                uri
                            )
                    } else {
                        resolver.delete(
                            uri,
                            null,
                            null
                        ) > 0
                    }
                } catch (
                    exception: SecurityException
                ) {
                    throw IllegalStateException(
                        "Ứng dụng không có quyền xóa file này",
                        exception
                    )
                }

            if (!deleted) {
                throw IllegalStateException(
                    "Không xóa được file MP3"
                )
            }

            /*
             * File đã xóa thành công nên phần dọn database
             * chỉ là best-effort, không được báo xóa thất bại
             * nếu database có vấn đề.
             */
            runCatching {
                downloadRepository
                    .deleteByOutputUri(
                        file.uri
                    )
            }

            File(
                cacheDirectory,
                "cover_${file.id}.img"
            ).delete()

            File(
                cacheDirectory,
                "read_${file.id}.mp3"
            ).delete()

            File(
                cacheDirectory,
                "edit_${file.id}.mp3"
            ).delete()
        }
    }

    private fun scanDefaultMediaStoreFolder():
        List<MediaSongFile> {

        val collection =
            MediaStore.Audio.Media
                .getContentUri(
                    MediaStore
                        .VOLUME_EXTERNAL_PRIMARY
                )

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_MODIFIED
        )

        val selection = """
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
                MIME_TYPE,
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

                songs.add(
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
                )
            }
        }

        return songs
    }

    private fun scanDocumentTree(
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
                DocumentsContract
                    .Document.COLUMN_DOCUMENT_ID,

                DocumentsContract
                    .Document.COLUMN_DISPLAY_NAME,

                DocumentsContract
                    .Document.COLUMN_MIME_TYPE,

                DocumentsContract
                    .Document.COLUMN_SIZE,

                DocumentsContract
                    .Document.COLUMN_LAST_MODIFIED
            ),

            null,
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val documentId =
                    cursor.getString(0)

                val displayName =
                    cursor.getString(1)
                        ?: "Unknown.mp3"

                val mimeType =
                    cursor.getString(2)
                        .orEmpty()

                val isMp3 =
                    mimeType == MIME_TYPE ||
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

                songs.add(
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
                            cursor.getLong(3),

                        dateModifiedSeconds =
                            cursor.getLong(4) /
                                1000L,

                        treeUri =
                            treeUri.toString()
                    )
                )
            }
        }

        return songs.sortedBy {
            it.displayName.lowercase()
        }
    }

    private fun publishEditedFile(
        originalFile: MediaSongFile,
        sourceFile: File,
        title: String,
        artist: String,
        album: String,
        year: String
    ): MediaSongFile {
        val sourceUri =
            Uri.parse(originalFile.uri)

        writeFileToUri(
            sourceFile = sourceFile,
            destinationUri = sourceUri
        )

        val targetBaseName =
            sanitizeFileBase(
                "$title - $artist"
            )

        return if (
            originalFile.treeUri != null
        ) {
            renameDocumentFile(
                originalFile = originalFile,
                targetBaseName = targetBaseName
            )
        } else {
            renameMediaStoreFile(
                originalFile = originalFile,
                targetBaseName = targetBaseName,
                title = title,
                artist = artist,
                album = album,
                year = year
            )
        }
    }

    private fun renameMediaStoreFile(
        originalFile: MediaSongFile,
        targetBaseName: String,
        title: String,
        artist: String,
        album: String,
        year: String
    ): MediaSongFile {
        val uri =
            Uri.parse(originalFile.uri)

        val displayName =
            findAvailableMediaStoreName(
                currentId = originalFile.id,
                baseName = targetBaseName
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
                        .TITLE,

                    title
                )

                put(
                    MediaStore.Audio.Media
                        .ARTIST,

                    artist
                )

                put(
                    MediaStore.Audio.Media
                        .ALBUM,

                    album
                )

                if (
                    year.isBlank()
                ) {
                    putNull(
                        MediaStore.Audio.Media
                            .YEAR
                    )
                }
                else {
                    put(
                        MediaStore.Audio.Media
                            .YEAR,

                        year.toInt()
                    )
                }

                put(
                    MediaStore.Audio.Media
                        .DATE_MODIFIED,

                    System.currentTimeMillis() /
                        1000L
                )
            }

        val changed =
            resolver.update(
                uri,
                values,
                null,
                null
            )

        if (changed <= 0) {
            throw IllegalStateException(
                "Không đổi được tên file"
            )
        }

        return originalFile.copy(
            displayName = displayName,
            sizeBytes =
                queryDocumentSize(uri)
                    ?: originalFile.sizeBytes,

            dateModifiedSeconds =
                System.currentTimeMillis() /
                    1000L
        )
    }

    private fun renameDocumentFile(
        originalFile: MediaSongFile,
        targetBaseName: String
    ): MediaSongFile {
        val treeUri =
            Uri.parse(
                originalFile.treeUri
            )

        val oldUri =
            Uri.parse(
                originalFile.uri
            )

        val displayName =
            findAvailableTreeName(
                treeUri = treeUri,
                currentUri = oldUri,
                baseName = targetBaseName
            )

        val renamedUri =
            if (
                displayName ==
                originalFile.displayName
            ) {
                oldUri
            } else {
                DocumentsContract
                    .renameDocument(
                        resolver,
                        oldUri,
                        displayName
                    ) ?: throw IllegalStateException(
                    "Không đổi được tên file"
                )
            }

        return originalFile.copy(
            id =
                renamedUri
                    .toString()
                    .hashCode()
                    .toLong()
                    .absoluteValue,

            uri =
                renamedUri.toString(),

            displayName =
                displayName,

            sizeBytes =
                queryDocumentSize(renamedUri)
                    ?: originalFile.sizeBytes,

            dateModifiedSeconds =
                System.currentTimeMillis() /
                    1000L
        )
    }

    private fun findAvailableMediaStoreName(
        currentId: Long,
        baseName: String
    ): String {
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

                    """
                    ${MediaStore.Audio.Media.DISPLAY_NAME} = ?
                    AND
                    ${MediaStore.Audio.Media.RELATIVE_PATH} = ?
                    AND
                    ${MediaStore.Audio.Media._ID} != ?
                    """.trimIndent(),

                    arrayOf(
                        candidate,
                        AppSettings.DEFAULT_RELATIVE_PATH,
                        currentId.toString()
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
        currentUri: Uri,
        baseName: String
    ): String {
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

        val occupied =
            mutableSetOf<String>()

        resolver.query(
            childrenUri,

            arrayOf(
                DocumentsContract
                    .Document.COLUMN_DOCUMENT_ID,

                DocumentsContract
                    .Document.COLUMN_DISPLAY_NAME
            ),

            null,
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val childUri =
                    DocumentsContract
                        .buildDocumentUriUsingTree(
                            treeUri,
                            cursor.getString(0)
                        )

                if (childUri == currentUri) {
                    continue
                }

                cursor.getString(1)
                    ?.lowercase()
                    ?.let(occupied::add)
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
                    !in occupied
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

    private fun copyUriToFile(
        uri: Uri,
        destination: File
    ) {
        destination.parentFile
            ?.mkdirs()

        val input =
            resolver.openInputStream(uri)
                ?: throw IllegalStateException(
                    "Không mở được file MP3"
                )

        val copiedBytes =
            input.use { inputStream ->
                destination
                    .outputStream()
                    .use { outputStream ->
                        inputStream.copyTo(
                            outputStream
                        )
                    }
            }

        if (
            copiedBytes <= 0L ||
            destination.length() <= 0L
        ) {
            destination.delete()

            throw IllegalStateException(
                "File MP3 tạm bị rỗng"
            )
        }
    }

    private fun writeFileToUri(
        sourceFile: File,
        destinationUri: Uri
    ) {
        val output =
            resolver.openOutputStream(
                destinationUri,
                "rwt"
            ) ?: throw IllegalStateException(
                "Không mở được file để ghi"
            )

        val copiedBytes =
            output.use { outputStream ->
                sourceFile.inputStream()
                    .use { inputStream ->
                        inputStream.copyTo(
                            outputStream
                        )
                    }
            }

        if (
            copiedBytes <= 0L ||
            copiedBytes !=
            sourceFile.length()
        ) {
            throw IllegalStateException(
                "Dung lượng file sau khi ghi không khớp"
            )
        }
    }

    private fun queryDocumentSize(
        uri: Uri
    ): Long? {
        return resolver.query(
            uri,

            arrayOf(
                DocumentsContract
                    .Document.COLUMN_SIZE
            ),

            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getLong(0)
            } else {
                null
            }
        }
    }

    private fun sanitizeFileBase(
        value: String
    ): String {
        return value
            .replace(
                Regex("""[\\/:*?"<>|]"""),
                "_"
            )
            .replace(
                Regex("""[\u0000-\u001F\u007F]"""),
                ""
            )
            .replace(
                Regex("""\s+"""),
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
}