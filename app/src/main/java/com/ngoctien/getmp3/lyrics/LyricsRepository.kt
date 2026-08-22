package com.ngoctien.getmp3.lyrics

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import com.ngoctien.getmp3.storage.resolveSafDirectoryDocumentId
import com.ngoctien.getmp3.library.MediaIndexRepository
import com.ngoctien.getmp3.note.SongNameMatcher
import com.ngoctien.getmp3.python.LyricsTagReadResult
import com.ngoctien.getmp3.python.LyricsTagWriteResult
import com.ngoctien.getmp3.python.Mp3TagBridge
import com.ngoctien.getmp3.settings.AppSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class LyricsRepository(
    context: Context
) {
    companion object {
        /*
         * Endpoint tương thích LRCLIB.
         * Nếu dịch vụ đổi API, chỉ cần đổi URL này.
         */
        private const val BASE_URL =
            "https://lrclib.net/api/search"

        private const val CONNECT_TIMEOUT_MS =
            15_000

        private const val READ_TIMEOUT_MS =
            20_000

        private const val MP3_MIME_TYPE =
            "audio/mpeg"

        private const val LIBRARY_BATCH_SIZE =
            250

        private val NoisePhrases =
            listOf(
                "official music video",
                "official video",
                "official audio",
                "official mv",
                "music video",
                "lyric video",
                "lyrics video",
                "official lyrics",
                "official lyric",
                "lyrics",
                "lyric",
                "visualizer",
                "vietsub",
                "viet sub",
                "nhạc remix hot tiktok cực cháy",
                "nhac remix hot tiktok cuc chay",
                "nhạc remix hot tik tok cực cháy",
                "nhac remix hot tik tok cuc chay",
                "nhạc remix hot tiktok",
                "nhac remix hot tiktok",
                "nhạc remix cực cháy",
                "nhac remix cuc chay",
                "remix hot tiktok",
                "remix hot tik tok",
                "hot tiktok",
                "hot tik tok",
                "bass boosted",
                "slowed and reverb",
                "slowed reverb",
                "speed up",
                "sped up",
                "full hd",
                "4k"
            )
                .sortedByDescending {
                    it.length
                }

        private val VersionWords =
            listOf(
                "remix",
                "vinahouse",
                "nonstop",
                "lofi",
                "live",
                "acoustic",
                "cover",
                "karaoke",
                "instrumental",
                "beat",
                "mashup",
                "medley"
            )
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

    private val bridge =
        Mp3TagBridge(
            applicationContext
        )

    private val cacheDirectory =
        File(
            applicationContext.cacheDir,
            "lyrics_editor"
        )

    private val libraryMutex =
        Mutex()

    private var cachedLibraryKey:
        String? = null

    private var cachedLibrary:
        List<LibrarySongCandidate>? =
            null

    suspend fun suggestSongs(
        query: String,
        limit: Int = 20
    ): List<LibrarySongCandidate> {
        val cleanQuery =
            normalizeSpaces(query)

        if (cleanQuery.length < 2) {
            return emptyList()
        }

        val songs =
            getLibrarySongs()

        val normalizedQuery =
            SongNameMatcher.normalizeText(
                cleanQuery
            )

        val normalizedTitleQuery =
            SongNameMatcher.normalizeTitle(
                cleanQuery
            )

        return withContext(
            Dispatchers.Default
        ) {
            songs.asSequence()
                .map { song ->
                    val titleScore =
                        SongNameMatcher.similarity(
                            normalizedTitleQuery,
                            SongNameMatcher
                                .normalizeTitle(
                                    song.title
                                )
                        )

                    val combinedText =
                        SongNameMatcher.normalizeText(
                            "${song.title} ${song.artist}"
                        )

                    val combinedScore =
                        SongNameMatcher.similarity(
                            normalizedQuery,
                            combinedText
                        )

                    val containsScore =
                        when {
                            combinedText ==
                                normalizedQuery -> {
                                1.0
                            }

                            combinedText.contains(
                                normalizedQuery
                            ) -> {
                                0.97
                            }

                            else -> {
                                0.0
                            }
                        }

                    song.copy(
                        score =
                            maxOf(
                                titleScore,
                                combinedScore * 0.98,
                                containsScore
                            )
                    )
                }
                .filter {
                    it.score >= 0.48
                }
                .sortedWith(
                    compareByDescending<
                        LibrarySongCandidate
                        > {
                        it.score
                    }.thenBy {
                        it.displayName
                            .lowercase(
                                Locale.ROOT
                            )
                    }
                )
                .take(
                    limit.coerceIn(
                        1,
                        50
                    )
                )
                .toList()
        }
    }

    suspend fun findWriteCandidates(
        title: String,
        artist: String,
        preferredUri: String? = null,
        limit: Int = 12
    ): List<LibrarySongCandidate> {
        val songs =
            getLibrarySongs()

        return withContext(
            Dispatchers.Default
        ) {
            rankWriteCandidates(
                songs = songs,
                title =
                    cleanTitleForLyrics(
                        title
                    ),
                artist = artist,
                preferredUri = preferredUri,
                limit = limit
            )
        }
    }

    suspend fun searchLyrics(
        rawTitle: String,
        rawArtist: String,
        limit: Int = 10
    ): List<LyricsSearchResult> {
        return withContext(
            Dispatchers.IO
        ) {
            searchLyricsInternal(
                rawTitle =
                    rawTitle,

                rawArtist =
                    rawArtist,

                limit =
                    limit
            )
        }
    }
    private suspend fun searchLyricsInternal(
        rawTitle: String,
        rawArtist: String,
        limit: Int = 10
    ): List<LyricsSearchResult> {
        val cleanedTitle =
            cleanTitleForLyrics(
                rawTitle
            )

        val cleanedArtist =
            cleanArtistForLyrics(
                rawArtist
            )

        require(cleanedTitle.isNotBlank()) {
            "Không xác định được tên bài hát để tra lời"
        }

        val titleVariants =
            buildTitleVariants(
                cleanedTitle
            )

        val collected =
            LinkedHashMap<
                String,
                LyricsSearchResult
                >()

        for (titleVariant in titleVariants) {
            currentCoroutineContext()
                .ensureActive()

            val exactResults =
                requestLyrics(
                    title =
                        titleVariant,

                    artist =
                        cleanedArtist
                )

            exactResults.forEach {
                result ->

                val ranked =
                    rankLyricsResult(
                        queryTitle =
                            cleanedTitle,

                        queryArtist =
                            cleanedArtist,

                        result =
                            result
                    )

                val key =
                    if (ranked.id > 0L) {
                        "id:${ranked.id}"
                    } else {
                        buildString {
                            append(
                                SongNameMatcher
                                    .normalizeTitle(
                                        ranked.trackName
                                    )
                            )

                            append('|')

                            append(
                                SongNameMatcher
                                    .normalizeText(
                                        ranked.artistName
                                    )
                            )
                        }
                    }

                val current =
                    collected[key]

                if (
                    current == null ||
                    ranked.score >
                    current.score
                ) {
                    collected[key] =
                        ranked
                }
            }

            if (collected.size >= limit) {
                break
            }

            /*
             * Fallback không có Artist, hữu ích với file
             * có tên kênh hoặc Artist không chính xác.
             */
            if (
                cleanedArtist.isNotBlank() &&
                collected.size <
                limit
            ) {
                requestLyrics(
                    title =
                        titleVariant,

                    artist =
                        ""
                ).forEach { result ->
                    val ranked =
                        rankLyricsResult(
                            queryTitle =
                                cleanedTitle,

                            queryArtist =
                                cleanedArtist,

                            result =
                                result
                        )

                    val key =
                        if (ranked.id > 0L) {
                            "id:${ranked.id}"
                        } else {
                            "${ranked.trackName}|${ranked.artistName}"
                        }

                    val current =
                        collected[key]

                    if (
                        current == null ||
                        ranked.score >
                        current.score
                    ) {
                        collected[key] =
                            ranked
                    }
                }
            }
        }

        return collected.values
            .filter {
                it.readableLyrics.isNotBlank()
            }
            .sortedByDescending {
                it.score
            }
            .take(
                limit.coerceIn(
                    1,
                    20
                )
            )
    }

    fun candidateFromInput(
        rawInput: String
    ): LibrarySongCandidate {
        val parsed =
            parseLyricsSearchInput(
                rawInput
            )

        return LibrarySongCandidate(
            uri = "",
            treeUri = null,
            displayName =
                parsed.displayText,
            title =
                parsed.title,
            artist =
                parsed.artist,
            score = 1.0
        )
    }

    suspend fun readStoredLyrics(
        target: LibrarySongCandidate
    ): StoredLyrics {
        require(target.uri.isNotBlank()) {
            "File MP3 chưa được chọn"
        }

        return withContext(
            Dispatchers.IO
        ) {
            cacheDirectory.mkdirs()

            val temporaryFile =
                File(
                    cacheDirectory,
                    "lyrics_read_${
                        target.uri
                            .hashCode()
                            .absoluteValue
                    }.mp3"
                )

            temporaryFile.delete()

            try {
                copyUriToFile(
                    uri =
                        Uri.parse(
                            target.uri
                        ),

                    destination =
                        temporaryFile
                )

                when (
                    val result =
                        bridge.readLyrics(
                            temporaryFile
                                .absolutePath
                        )
                ) {
                    is LyricsTagReadResult.Success -> {
                        StoredLyrics(
                            text =
                                result.text,

                            language =
                                result.language,

                            description =
                                result.description
                        )
                    }

                    is LyricsTagReadResult.Error -> {
                        throw IllegalStateException(
                            result.message
                        )
                    }
                }
            } finally {
                temporaryFile.delete()
            }
        }
    }

    suspend fun writeLyrics(
        target: LibrarySongCandidate,
        lyrics: String
    ) {
        require(target.uri.isNotBlank()) {
            "File MP3 chưa được chọn"
        }

        val cleanLyrics =
            normalizeLyricsText(
                lyrics
            )

        require(cleanLyrics.isNotBlank()) {
            "Nội dung lời bài hát đang trống"
        }

        withContext(
            Dispatchers.IO
        ) {
            cacheDirectory.mkdirs()

            val fileKey =
                target.uri
                    .hashCode()
                    .absoluteValue

            val originalFile =
                File(
                    cacheDirectory,
                    "lyrics_original_$fileKey.mp3"
                )

            val editedFile =
                File(
                    cacheDirectory,
                    "lyrics_edited_$fileKey.mp3"
                )

            val verificationFile =
                File(
                    cacheDirectory,
                    "lyrics_verify_$fileKey.mp3"
                )

            originalFile.delete()
            editedFile.delete()
            verificationFile.delete()

            val targetUri =
                Uri.parse(
                    target.uri
                )

            try {
                copyUriToFile(
                    uri =
                        targetUri,

                    destination =
                        originalFile
                )

                originalFile.copyTo(
                    target =
                        editedFile,

                    overwrite =
                        true
                )

                val language =
                    detectLanguage(
                        cleanLyrics
                    )

                when (
                    val result =
                        bridge.writeLyrics(
                            mp3Path =
                                editedFile
                                    .absolutePath,

                            lyrics =
                                cleanLyrics,

                            language =
                                language
                        )
                ) {
                    is LyricsTagWriteResult.Success -> {
                        if (
                            !result.id3Version
                                .startsWith(
                                    "2.3"
                                )
                        ) {
                            throw IllegalStateException(
                                "Tag lời không phải ID3v2.3"
                            )
                        }
                    }

                    is LyricsTagWriteResult.Error -> {
                        throw IllegalStateException(
                            result.message
                        )
                    }
                }

                try {
                    writeFileToUri(
                        sourceFile =
                            editedFile,

                        destinationUri =
                            targetUri
                    )

                    copyUriToFile(
                        uri =
                            targetUri,

                        destination =
                            verificationFile
                    )

                    val verifiedLyrics =
                        when (
                            val result =
                                bridge.readLyrics(
                                    verificationFile
                                        .absolutePath
                                )
                        ) {
                            is LyricsTagReadResult.Success -> {
                                normalizeLyricsText(
                                    result.text
                                )
                            }

                            is LyricsTagReadResult.Error -> {
                                throw IllegalStateException(
                                    result.message
                                )
                            }
                        }

                    if (
                        verifiedLyrics !=
                        cleanLyrics
                    ) {
                        throw IllegalStateException(
                            "Lyrics đọc lại sau khi lưu không khớp"
                        )
                    }

                    refreshMediaMetadata(
                        targetUri
                    )
                } catch (
                    writeException: Exception
                ) {
                    /*
                     * Có bản gốc trong cache nên thử khôi phục
                     * nếu provider gặp lỗi trong lúc ghi.
                     */
                    runCatching {
                        writeFileToUri(
                            sourceFile =
                                originalFile,

                            destinationUri =
                                targetUri
                        )
                    }

                    throw writeException
                }
            } finally {
                originalFile.delete()
                .also {
                    editedFile.delete()
                    verificationFile.delete()
                }
            }
        }
    }

    private suspend fun getLibrarySongs():
        List<LibrarySongCandidate> {

        val settings =
            settingsRepository
                .getSettings()

        if (!settings.hasCompareFolder) {
            throw IllegalStateException(
                "Hãy chọn thư mục đối chiếu trong Cài đặt"
            )
        }

        val summary =
            mediaIndexRepository
                .referenceSummary()
                ?: throw IllegalStateException(
                    "Chưa có dữ liệu thư viện. Hãy vào Cài đặt → Chuẩn bị dữ liệu."
                )

        val sourceKey =
            "${summary.treeUri}|${summary.updatedAt}"

        return libraryMutex.withLock {
            val memory =
                cachedLibrary

            if (
                memory != null &&
                cachedLibraryKey ==
                sourceKey
            ) {
                return@withLock memory
            }

            val indexedSongs =
                mediaIndexRepository
                    .getReferenceSongs()

            if (indexedSongs.isEmpty()) {
                throw IllegalStateException(
                    "Dữ liệu thư viện đang trống. Hãy vào Cài đặt → Chuẩn bị dữ liệu."
                )
            }

            val mapped =
                indexedSongs.map { song ->
                    LibrarySongCandidate(
                        uri = song.uri,
                        treeUri = song.treeUri,
                        displayName = song.displayName,
                        title = song.title,
                        artist = song.artist,
                        score = 1.0
                    )
                }

            cachedLibraryKey =
                sourceKey

            cachedLibrary =
                mapped

            mapped
        }
    }


    private suspend fun scanDirectChildren(
        treeUriText: String
    ): List<LibrarySongCandidate> {
        return withContext(
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
                    DocumentsContract
                        .Document
                        .COLUMN_DOCUMENT_ID,

                    DocumentsContract
                        .Document
                        .COLUMN_DISPLAY_NAME,

                    DocumentsContract
                        .Document
                        .COLUMN_MIME_TYPE
                )

            val result =
                ArrayList<
                    LibrarySongCandidate
                    >(
                    5_000
                )

            var scannedCount = 0

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

                while (cursor.moveToNext()) {
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
                            displayName
                                .endsWith(
                                    ".mp3",
                                    ignoreCase = true
                                )

                    if (
                        !isMp3 ||
                        documentId.isBlank() ||
                        displayName.isBlank()
                    ) {
                        continue
                    }

                    val documentUri =
                        DocumentsContract
                            .buildDocumentUriUsingTree(
                                treeUri,
                                documentId
                            )

                    val parsed =
                        SongNameMatcher
                            .parseFileName(
                                displayName
                            )

                    val stem =
                        displayName
                            .substringBeforeLast(
                                ".",
                                displayName
                            )
                            .trim()

                    result.add(
                        LibrarySongCandidate(
                            uri =
                                documentUri
                                    .toString(),

                            treeUri =
                                treeUriText,

                            displayName =
                                displayName,

                            title =
                                parsed
                                    ?.title
                                    ?.takeIf(
                                        String::isNotBlank
                                    )
                                    ?: stem,

                            artist =
                                parsed
                                    ?.artist
                                    .orEmpty(),

                            score =
                                1.0
                        )
                    )

                    scannedCount++

                    if (
                        scannedCount %
                        LIBRARY_BATCH_SIZE == 0
                    ) {
                        yield()
                    }
                }
            }

            result
                .distinctBy {
                    it.uri
                }
                .sortedBy {
                    it.displayName
                        .lowercase(
                            Locale.ROOT
                        )
                }
        }
    }

    private fun requestLyrics(
        title: String,
        artist: String
    ): List<LyricsSearchResult> {
        val encodedTitle =
            encodeUrlParameter(
                title
            )

        val encodedArtist =
            encodeUrlParameter(
                artist
            )

        val urlText =
            buildString {
                append(BASE_URL)
                append("?track_name=")
                append(encodedTitle)

                if (artist.isNotBlank()) {
                    append("&artist_name=")
                    append(encodedArtist)
                }
            }

        val connection =
            URL(urlText)
                .openConnection() as
                HttpURLConnection

        try {
            connection.requestMethod =
                "GET"

            connection.connectTimeout =
                CONNECT_TIMEOUT_MS

            connection.readTimeout =
                READ_TIMEOUT_MS

            connection.setRequestProperty(
                "Accept",
                "application/json"
            )

            connection.setRequestProperty(
                "User-Agent",
                "GetMP3/1.0 Android"
            )

            val responseCode =
                connection.responseCode

            val stream =
                if (
                    responseCode in
                    200..299
                ) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

            val body =
                stream
                    ?.bufferedReader(
                        Charsets.UTF_8
                    )
                    ?.use {
                        it.readText()
                    }
                    .orEmpty()

            if (
                responseCode !in
                200..299
            ) {
                val message =
                    runCatching {
                        JSONObject(body)
                            .optString(
                                "message"
                            )
                    }
                        .getOrNull()
                        .orEmpty()
                        .ifBlank {
                            "Máy chủ lời bài hát trả về mã $responseCode"
                        }

                throw IllegalStateException(
                    message
                )
            }

            val array =
                JSONArray(body)

            val results =
                ArrayList<
                    LyricsSearchResult
                    >(
                    array.length()
                )

            for (
                index in
                0 until array.length()
            ) {
                val item =
                    array.optJSONObject(
                        index
                    ) ?: continue

                val plainLyrics =
                    if (
                        item.isNull(
                            "plainLyrics"
                        )
                    ) {
                        ""
                    } else {
                        item.optString(
                            "plainLyrics"
                        )
                    }

                val syncedLyrics =
                    if (
                        item.isNull(
                            "syncedLyrics"
                        )
                    ) {
                        null
                    } else {
                        item.optString(
                            "syncedLyrics"
                        )
                            .takeIf(
                                String::isNotBlank
                            )
                    }

                if (
                    plainLyrics.isBlank() &&
                    syncedLyrics.isNullOrBlank()
                ) {
                    continue
                }

                val duration =
                    item.optDouble(
                        "duration",
                        -1.0
                    )
                        .takeIf {
                            it >= 0.0
                        }
                        ?.roundToInt()

                results.add(
                    LyricsSearchResult(
                        id =
                            item.optLong(
                                "id",
                                0L
                            ),

                        trackName =
                            item.optString(
                                "trackName"
                            ).trim(),

                        artistName =
                            item.optString(
                                "artistName"
                            ).trim(),

                        albumName =
                            item.optString(
                                "albumName"
                            ).trim(),

                        durationSeconds =
                            duration,

                        plainLyrics =
                            plainLyrics,

                        syncedLyrics =
                            syncedLyrics,

                        score =
                            0.0
                    )
                )
            }

            return results
        } finally {
            connection.disconnect()
        }
    }

    private fun rankLyricsResult(
        queryTitle: String,
        queryArtist: String,
        result: LyricsSearchResult
    ): LyricsSearchResult {
        val titleScore =
            SongNameMatcher.similarity(
                SongNameMatcher
                    .normalizeTitle(
                        queryTitle
                    ),

                SongNameMatcher
                    .normalizeTitle(
                        result.trackName
                    )
            )

        val artistScore =
            if (
                queryArtist.isBlank() ||
                result.artistName.isBlank()
            ) {
                0.50
            } else {
                SongNameMatcher.similarity(
                    SongNameMatcher
                        .normalizeText(
                            queryArtist
                        ),

                    SongNameMatcher
                        .normalizeText(
                            result.artistName
                        )
                )
            }

        val lyricScore =
            if (
                result.readableLyrics
                    .isNotBlank()
            ) {
                1.0
            } else {
                0.0
            }

        return result.copy(
            score =
                (
                    titleScore * 0.72 +
                        artistScore * 0.23 +
                        lyricScore * 0.05
                    )
                    .coerceIn(
                        0.0,
                        1.0
                    )
        )
    }

    private fun buildTitleVariants(
        title: String
    ): List<String> {
        val result =
            linkedSetOf<String>()

        normalizeSpaces(title)
            .takeIf(
                String::isNotBlank
            )
            ?.let(result::add)

        title.split(
            Regex(
                """(?i)\s+(?:x|mashup|medley|ft\.?|feat\.?)\s+"""
            )
        )
            .map(::normalizeSpaces)
            .filter {
                it.length >= 3
            }
            .sortedByDescending {
                it.length
            }
            .forEach(result::add)

        return result
            .take(4)
    }

    private fun cleanTitleForLyrics(
        value: String
    ): String {
        var result =
            normalizeSpaces(
                value
            )

        result =
            Regex(
                """[\(\[\{]([^\)\]\}]*)[\)\]\}]"""
            ).replace(result) { match ->
                val inside =
                    match.groupValues[1]

                val normalizedInside =
                    SongNameMatcher
                        .normalizeText(
                            inside
                        )

                val isNoiseGroup =
                    NoisePhrases.any {
                        phrase ->

                        normalizedInside.contains(
                            SongNameMatcher
                                .normalizeText(
                                    phrase
                                )
                        )
                    } ||
                        VersionWords.any {
                            word ->

                            normalizedInside
                                .split(' ')
                                .contains(
                                    SongNameMatcher
                                        .normalizeText(
                                            word
                                        )
                                )
                        }

                if (isNoiseGroup) {
                    " "
                } else {
                    " $inside "
                }
            }

        NoisePhrases.forEach {
                phrase ->

            result =
                result.replace(
                    Regex(
                        Regex.escape(
                            phrase
                        ),
                        RegexOption
                            .IGNORE_CASE
                    ),
                    " "
                )
        }

        result =
            result
                .replace(
                    Regex(
                        """[\p{So}\p{Sk}\p{Cn}]+"""
                    ),
                    " "
                )
                .replace(
                    Regex(
                        """[^\p{L}\p{N}'’&+\-\s]+"""
                    ),
                    " "
                )
                .replace(
                    Regex(
                        """(?:^|\s)[|•·]+(?:\s|$)"""
                    ),
                    " "
                )

        return normalizeSpaces(
            result
        )
    }

    private fun cleanArtistForLyrics(
        value: String
    ): String {
        var result =
            normalizeSpaces(
                value
            )

        NoisePhrases.forEach {
                phrase ->

            result =
                result.replace(
                    Regex(
                        Regex.escape(
                            phrase
                        ),
                        RegexOption
                            .IGNORE_CASE
                    ),
                    " "
                )
        }

        return result
            .replace(
                Regex(
                    """[\p{So}\p{Sk}\p{Cn}]+"""
                ),
                " "
            )
            .replace(
                Regex(
                    """[^\p{L}\p{N}'’&+\-\s]+"""
                ),
                " "
            )
            .let(::normalizeSpaces)
    }

    private fun detectLanguage(
        lyrics: String
    ): String {
        val vietnameseCharacters =
            Regex(
                """[ăâđêôơưĂÂĐÊÔƠƯáàảãạấầẩẫậắằẳẵặéèẻẽẹếềểễệíìỉĩịóòỏõọốồổỗộớờởỡợúùủũụứừửữựýỳỷỹỵ]""",
                RegexOption.IGNORE_CASE
            )

        return if (
            vietnameseCharacters
                .containsMatchIn(
                    lyrics
                )
        ) {
            "vie"
        } else {
            /*
             * Nhiều ứng dụng Android nhận USLT với
             * language=eng tốt hơn language=und.
             */
            "eng"
        }
    }
    private fun normalizeLyricsText(
        value: String
    ): String {
        return value
            .replace(
                "\r\n",
                "\n"
            )
            .replace(
                '\r',
                '\n'
            )
            .lineSequence()
            .map {
                it.trimEnd()
            }
            .joinToString("\n")
            .replace(
                Regex(
                    """\n{4,}"""
                ),
                "\n\n\n"
            )
            .trim()
    }

    private fun normalizeSpaces(
        value: String
    ): String {
        return value
            .replace(
                Regex(
                    """[\u200B-\u200D\uFEFF]"""
                ),
                ""
            )
            .replace(
                Regex(
                    """[\s\p{Zs}]+"""
                ),
                " "
            )
            .trim()
    }

    private fun encodeUrlParameter(
        value: String
    ): String {
        return URLEncoder.encode(
            value,
            StandardCharsets.UTF_8
                .name()
        )
    }

    private fun refreshMediaMetadata(
        uri: Uri
    ) {
        runCatching {
            resolver.notifyChange(
                uri,
                null
            )
        }

        val filePath =
            resolvePrimaryExternalStoragePath(
                uri
            )

        if (filePath == null) {
            return
        }

        MediaScannerConnection.scanFile(
            applicationContext,
            arrayOf(filePath),
            arrayOf(MP3_MIME_TYPE)
        ) {
                _,
                scannedUri ->

            runCatching {
                resolver.notifyChange(
                    scannedUri ?: uri,
                    null
                )
            }
        }
    }

    private fun resolvePrimaryExternalStoragePath(
        uri: Uri
    ): String? {
        if (
            !DocumentsContract
                .isDocumentUri(
                    applicationContext,
                    uri
                )
        ) {
            return null
        }

        val documentId =
            runCatching {
                DocumentsContract
                    .getDocumentId(
                        uri
                    )
            }
                .getOrNull()
                ?.trim()
                .orEmpty()

        val separatorIndex =
            documentId.indexOf(':')

        if (
            separatorIndex <= 0 ||
            separatorIndex >=
            documentId.lastIndex
        ) {
            return null
        }

        val volumeName =
            documentId
                .substring(
                    0,
                    separatorIndex
                )

        if (
            !volumeName.equals(
                "primary",
                ignoreCase = true
            )
        ) {
            return null
        }

        val relativePath =
            documentId
                .substring(
                    separatorIndex + 1
                )
                .trimStart('/')

        if (relativePath.isBlank()) {
            return null
        }

        return File(
            Environment
                .getExternalStorageDirectory(),

            relativePath
        ).absolutePath
    }
    private fun copyUriToFile(
        uri: Uri,
        destination: File
    ) {
        val input =
            resolver.openInputStream(
                uri
            ) ?: throw IllegalStateException(
                "Không mở được file MP3"
            )

        destination.parentFile
            ?.mkdirs()

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
            !destination.isFile ||
            destination.length() <= 0L
        ) {
            throw IllegalStateException(
                "File MP3 tạm không hợp lệ"
            )
        }
    }

    private fun writeFileToUri(
        sourceFile: File,
        destinationUri: Uri
    ) {
        require(
            sourceFile.isFile &&
                sourceFile.length() > 0L
        ) {
            "File MP3 đã chỉnh không hợp lệ"
        }

        val output =
            resolver.openOutputStream(
                destinationUri,
                "rwt"
            )
                ?: resolver.openOutputStream(
                    destinationUri,
                    "w"
                )
                ?: throw IllegalStateException(
                    "Không mở được file để ghi lyrics"
                )

        val writtenBytes =
            output.use { outputStream ->
                sourceFile
                    .inputStream()
                    .use { inputStream ->
                        inputStream.copyTo(
                            outputStream
                        )
                    }
            }

        if (
            writtenBytes !=
            sourceFile.length()
        ) {
            throw IllegalStateException(
                "Dung lượng file sau khi ghi không khớp"
            )
        }
    }
}