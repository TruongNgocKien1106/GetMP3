package com.ngoctien.getmp3.note

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.ngoctien.getmp3.note.data.DuplicateSongMatch
import com.ngoctien.getmp3.note.data.DuplicateSource
import com.ngoctien.getmp3.settings.AppSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.json.JSONObject
import java.io.File
import java.text.Normalizer
import java.util.Locale
import kotlin.math.max

internal data class CompareSongEntry(
    val displayName: String,
    val title: String,
    val artist: String,
    val normalizedTitle: String
)

class CompareSongIndexRepository(
    context: Context
) {
    companion object {
        private const val CACHE_VERSION = 1

        private const val BATCH_SIZE = 250

        private const val MP3_MIME_TYPE =
            "audio/mpeg"

        private const val MATCH_THRESHOLD =
            0.84
    }

    private val applicationContext =
        context.applicationContext

    private val resolver =
        applicationContext.contentResolver

    private val settingsRepository =
        AppSettingsRepository(
            applicationContext
        )

    private val cacheFile =
        File(
            applicationContext.filesDir,
            "compare_song_index.jsonl"
        )

    private val indexMutex =
        Mutex()

    private var memorySourceKey:
        String? = null

    private var memoryEntries:
        List<CompareSongEntry>? = null

    suspend fun findNearMatches(
        title: String,
        limit: Int = 5
    ): List<DuplicateSongMatch> {
        val normalizedQuery =
            SongNameMatcher.normalizeTitle(
                title
            )

        if (normalizedQuery.isBlank()) {
            return emptyList()
        }

        val settings =
            settingsRepository.getSettings()

        val treeUriText =
            settings.compareTreeUri
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: return emptyList()

        /*
         * Khi người dùng nhấn Quét lại trong Cài đặt,
         * compareIndexGeneratedAt đổi và cache sẽ được
         * tạo lại ở lần đối chiếu tiếp theo.
         */
        val sourceKey =
            buildString {
                append(treeUriText)
                append('|')
                append(
                    settings
                        .compareIndexGeneratedAt
                )
            }

        val entries =
            ensureIndex(
                treeUriText = treeUriText,
                sourceKey = sourceKey
            )

        return withContext(
            Dispatchers.Default
        ) {
            entries.asSequence()
                .mapNotNull { entry ->
                    val score =
                        SongNameMatcher.similarity(
                            normalizedQuery,
                            entry.normalizedTitle
                        )

                    if (
                        score <
                        MATCH_THRESHOLD
                    ) {
                        null
                    } else {
                        DuplicateSongMatch(
                            key =
                                "compare:${entry.displayName}",

                            title =
                                entry.title,

                            artist =
                                entry.artist,

                            source =
                                DuplicateSource
                                    .COMPARE_FOLDER,

                            score = score
                        )
                    }
                }
                .sortedByDescending {
                    it.score
                }
                .distinctBy {
                    SongNameMatcher
                        .normalizeTitle(
                            "${it.title} ${it.artist}"
                        )
                }
                .take(
                    limit.coerceAtLeast(1)
                )
                .toList()
        }
    }

    private suspend fun ensureIndex(
        treeUriText: String,
        sourceKey: String
    ): List<CompareSongEntry> {
        indexMutex.lock()

        try {
            val memory =
                memoryEntries

            if (
                memory != null &&
                memorySourceKey ==
                sourceKey
            ) {
                return memory
            }

            val diskEntries =
                loadCache(
                    expectedSourceKey =
                        sourceKey
                )

            if (diskEntries != null) {
                memorySourceKey =
                    sourceKey

                memoryEntries =
                    diskEntries

                return diskEntries
            }

            val scannedEntries =
                scanDirectChildren(
                    treeUriText =
                        treeUriText
                )

            saveCache(
                sourceKey =
                    sourceKey,

                entries =
                    scannedEntries
            )

            memorySourceKey =
                sourceKey

            memoryEntries =
                scannedEntries

            return scannedEntries
        } finally {
            indexMutex.unlock()
        }
    }

    private suspend fun scanDirectChildren(
        treeUriText: String
    ): List<CompareSongEntry> {
        return withContext(
            Dispatchers.IO
        ) {
            val treeUri =
                Uri.parse(treeUriText)

            val rootDocumentId =
                DocumentsContract
                    .getTreeDocumentId(
                        treeUri
                    )

            /*
             * Chỉ lấy phần tử trực tiếp trong
             * thư mục đã chọn.
             */
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
                        .COLUMN_DISPLAY_NAME,

                    DocumentsContract
                        .Document
                        .COLUMN_MIME_TYPE
                )

            val entries =
                ArrayList<CompareSongEntry>(
                    5_000
                )

            var scannedFiles = 0

            resolver.query(
                childrenUri,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
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

                    /*
                     * Không đọc thư mục con.
                     */
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
                            displayName.endsWith(
                                ".mp3",
                                ignoreCase = true
                            )

                    if (!isMp3) {
                        continue
                    }

                    scannedFiles++

                    SongNameMatcher
                        .parseFileName(
                            displayName
                        )
                        ?.let(
                            entries::add
                        )

                    if (
                        scannedFiles %
                        BATCH_SIZE == 0
                    ) {
                        yield()
                    }
                }
            }

            entries
                .distinctBy {
                    "${it.normalizedTitle}|${SongNameMatcher.normalizeText(it.artist)}"
                }
        }
    }

    private fun loadCache(
        expectedSourceKey: String
    ): List<CompareSongEntry>? {
        if (
            !cacheFile.isFile ||
            cacheFile.length() <= 0L
        ) {
            return null
        }

        return runCatching {
            cacheFile.bufferedReader(
                Charsets.UTF_8
            ).use { reader ->
                val headerLine =
                    reader.readLine()
                        ?: return null

                val header =
                    JSONObject(
                        headerLine
                    )

                if (
                    header.optInt(
                        "version",
                        -1
                    ) != CACHE_VERSION
                ) {
                    return null
                }

                if (
                    header.optString(
                        "sourceKey"
                    ) != expectedSourceKey
                ) {
                    return null
                }

                val result =
                    ArrayList<CompareSongEntry>(
                        header.optInt(
                            "count",
                            0
                        )
                    )

                reader.forEachLine { line ->
                    if (line.isBlank()) {
                        return@forEachLine
                    }

                    val json =
                        JSONObject(line)

                    val title =
                        json.optString(
                            "title"
                        )

                    val normalizedTitle =
                        json.optString(
                            "normalizedTitle"
                        )

                    if (
                        title.isBlank() ||
                        normalizedTitle.isBlank()
                    ) {
                        return@forEachLine
                    }

                    result.add(
                        CompareSongEntry(
                            displayName =
                                json.optString(
                                    "displayName"
                                ),

                            title = title,

                            artist =
                                json.optString(
                                    "artist"
                                ),

                            normalizedTitle =
                                normalizedTitle
                        )
                    )
                }

                result
            }
        }.getOrNull()
    }

    private fun saveCache(
        sourceKey: String,
        entries: List<CompareSongEntry>
    ) {
        runCatching {
            cacheFile.parentFile
                ?.mkdirs()

            val temporaryFile =
                File(
                    cacheFile.parentFile,
                    "${cacheFile.name}.tmp"
                )

            temporaryFile.bufferedWriter(
                Charsets.UTF_8
            ).use { writer ->
                val header =
                    JSONObject()
                        .put(
                            "version",
                            CACHE_VERSION
                        )
                        .put(
                            "sourceKey",
                            sourceKey
                        )
                        .put(
                            "count",
                            entries.size
                        )

                writer.appendLine(
                    header.toString()
                )

                entries.forEach { entry ->
                    val json =
                        JSONObject()
                            .put(
                                "displayName",
                                entry.displayName
                            )
                            .put(
                                "title",
                                entry.title
                            )
                            .put(
                                "artist",
                                entry.artist
                            )
                            .put(
                                "normalizedTitle",
                                entry.normalizedTitle
                            )

                    writer.appendLine(
                        json.toString()
                    )
                }
            }

            if (!temporaryFile.renameTo(cacheFile)) {
                temporaryFile.copyTo(
                    target = cacheFile,
                    overwrite = true
                )

                temporaryFile.delete()
            }
        }
    }
}

internal object SongNameMatcher {

    private val noisePhrases =
        listOf(
            "official music video",
            "official video",
            "official audio",
            "official mv",
            "lyric video",
            "lyrics video",
            "lyrics",
            "lyric",
            "vietsub",
            "viet sub",
            "visualizer",
            "nhac remix hot tiktok cuc chay",
            "nhac remix hot tik tok cuc chay",
            "nhac remix hot tiktok",
            "nhac remix hot tik tok",
            "nhac remix cuc chay",
            "remix hot tiktok",
            "remix hot tik tok",
            "hot tiktok",
            "hot tik tok",
            "tiktok",
            "tik tok",
            "ban remix",
            "remix version",
            "remix",
            "speed up",
            "sped up",
            "slowed reverb",
            "slowed and reverb",
            "lofi",
            "vinahouse",
            "nonstop",
            "bass boosted",
            "cuc chay",
            "cuc cuon"
        )
            .sortedByDescending {
                it.length
            }

    fun parseInput(
        rawText: String
    ): SongNoteDraftParts {
        val clean =
            normalizeSpaces(
                rawText
            )

        val youtubeUrl =
            clean.takeIf(
                ::isYoutubeUrl
            )

        if (youtubeUrl != null) {
            return SongNoteDraftParts(
                title = clean,
                artist = "",
                youtubeUrl = youtubeUrl
            )
        }

        val split =
            splitTitleArtist(
                clean
            )

        return SongNoteDraftParts(
            title =
                split.first.ifBlank {
                    clean
                },

            artist =
                split.second,

            youtubeUrl = null
        )
    }

    fun parseFileName(
        displayName: String
    ): CompareSongEntry? {
        val stem =
            displayName
                .substringBeforeLast(
                    delimiter = ".",
                    missingDelimiterValue =
                        displayName
                )
                .let(::normalizeSpaces)

        if (stem.isBlank()) {
            return null
        }

        val split =
            splitTitleArtist(stem)

        val title =
            split.first.ifBlank {
                stem
            }

        val normalizedTitle =
            normalizeTitle(title)

        if (normalizedTitle.isBlank()) {
            return null
        }

        return CompareSongEntry(
            displayName =
                displayName,

            title = title,

            artist =
                split.second,

            normalizedTitle =
                normalizedTitle
        )
    }

    fun normalizeTitle(
        value: String
    ): String {
        var result =
            removeBalancedGroups(
                value
            )

        result =
            stripDecorativeSymbols(
                result
            )

        result =
            result
                .lowercase(
                    Locale.ROOT
                )
                .replace(
                    oldChar = 'đ',
                    newChar = 'd'
                )

        result =
            Normalizer.normalize(
                result,
                Normalizer.Form.NFD
            )
                .replace(
                    Regex("""\p{M}+"""),
                    ""
                )

        noisePhrases.forEach { phrase ->
            result =
                result.replace(
                    phrase,
                    " "
                )
        }

        return result
            .replace(
                Regex("""[^a-z0-9]+"""),
                " "
            )
            .let(::normalizeSpaces)
    }

    fun normalizeText(
        value: String
    ): String {
        return Normalizer.normalize(
            value
                .lowercase(Locale.ROOT)
                .replace('đ', 'd'),
            Normalizer.Form.NFD
        )
            .replace(
                Regex("""\p{M}+"""),
                ""
            )
            .replace(
                Regex("""[^a-z0-9]+"""),
                " "
            )
            .let(::normalizeSpaces)
    }

    fun similarity(
        normalizedLeft: String,
        normalizedRight: String
    ): Double {
        if (
            normalizedLeft.isBlank() ||
            normalizedRight.isBlank()
        ) {
            return 0.0
        }

        if (
            normalizedLeft ==
            normalizedRight
        ) {
            return 1.0
        }

        val shorter =
            minOf(
                normalizedLeft.length,
                normalizedRight.length
            )

        if (
            shorter >= 5 &&
            (
                normalizedLeft.contains(
                    normalizedRight
                ) ||
                    normalizedRight.contains(
                        normalizedLeft
                    )
                )
        ) {
            return 0.94
        }

        val tokenScore =
            tokenSimilarity(
                normalizedLeft,
                normalizedRight
            )

        val editScore =
            stringSimilarity(
                normalizedLeft,
                normalizedRight
            )

        return max(
            tokenScore * 0.96,
            editScore
        )
    }

    fun isYoutubeUrl(
        value: String
    ): Boolean {
        val lower =
            value
                .trim()
                .lowercase(
                    Locale.ROOT
                )

        return (
            lower.startsWith(
                "https://"
            ) ||
                lower.startsWith(
                    "http://"
                )
            ) &&
            (
                "youtube.com/" in lower ||
                    "youtu.be/" in lower
                )
    }

    private fun splitTitleArtist(
        value: String
    ): Pair<String, String> {
        val separators =
            listOf(
                " - ",
                " – ",
                " — "
            )

        var bestIndex = -1
        var bestSeparator = ""

        separators.forEach { separator ->
            val index =
                value.lastIndexOf(
                    separator
                )

            if (index > bestIndex) {
                bestIndex = index
                bestSeparator = separator
            }
        }

        if (bestIndex <= 0) {
            return normalizeSpaces(value) to ""
        }

        val title =
            value.substring(
                0,
                bestIndex
            )
                .let(::normalizeSpaces)

        val artist =
            value.substring(
                bestIndex +
                    bestSeparator.length
            )
                .let(::normalizeSpaces)

        return title to artist
    }

    private fun tokenSimilarity(
        left: String,
        right: String
    ): Double {
        val leftTokens =
            left.split(' ')
                .filter {
                    it.isNotBlank()
                }
                .toSet()

        val rightTokens =
            right.split(' ')
                .filter {
                    it.isNotBlank()
                }
                .toSet()

        if (
            leftTokens.isEmpty() ||
            rightTokens.isEmpty()
        ) {
            return 0.0
        }

        val intersection =
            leftTokens
                .intersect(
                    rightTokens
                )
                .size

        val union =
            leftTokens
                .union(
                    rightTokens
                )
                .size

        return intersection.toDouble() /
            union.toDouble()
    }

    private fun stringSimilarity(
        left: String,
        right: String
    ): Double {
        val maximumLength =
            max(
                left.length,
                right.length
            )

        if (maximumLength == 0) {
            return 1.0
        }

        val distance =
            levenshteinDistance(
                left,
                right
            )

        return 1.0 -
            distance.toDouble() /
            maximumLength.toDouble()
    }

    private fun levenshteinDistance(
        left: String,
        right: String
    ): Int {
        if (left == right) {
            return 0
        }

        if (left.isEmpty()) {
            return right.length
        }

        if (right.isEmpty()) {
            return left.length
        }

        var previous =
            IntArray(
                right.length + 1
            ) {
                it
            }

        var current =
            IntArray(
                right.length + 1
            )

        left.forEachIndexed {
                leftIndex,
                leftCharacter ->

            current[0] =
                leftIndex + 1

            right.forEachIndexed {
                    rightIndex,
                    rightCharacter ->

                val insertion =
                    current[
                        rightIndex
                    ] + 1

                val deletion =
                    previous[
                        rightIndex + 1
                    ] + 1

                val replacement =
                    previous[
                        rightIndex
                    ] +
                        if (
                            leftCharacter ==
                            rightCharacter
                        ) {
                            0
                        } else {
                            1
                        }

                current[
                    rightIndex + 1
                ] = minOf(
                    insertion,
                    deletion,
                    replacement
                )
            }

            val temporary =
                previous

            previous = current
            current = temporary
        }

        return previous[
            right.length
        ]
    }

    private fun removeBalancedGroups(
        value: String
    ): String {
        if (value.isEmpty()) {
            return value
        }

        val removeFlags =
            BooleanArray(
                value.length
            )

        val stack =
            mutableListOf<
                Pair<Char, Int>
                >()

        fun expectedOpening(
            closing: Char
        ): Char? {
            return when (closing) {
                ')' -> '('
                ']' -> '['
                '}' -> '{'
                else -> null
            }
        }

        value.forEachIndexed {
                index,
                character ->

            when (character) {
                '(',
                '[',
                '{' -> {
                    stack.add(
                        character to index
                    )
                }

                ')',
                ']',
                '}' -> {
                    val latest =
                        stack.lastOrNull()

                    val expected =
                        expectedOpening(
                            character
                        )

                    if (
                        latest != null &&
                        latest.first == expected
                    ) {
                        stack.removeAt(
                            stack.lastIndex
                        )

                        for (
                            removeIndex in
                            latest.second..index
                        ) {
                            removeFlags[
                                removeIndex
                            ] = true
                        }
                    }
                }
            }
        }

        val output =
            StringBuilder(
                value.length
            )

        value.indices.forEach { index ->
            if (removeFlags[index]) {
                if (
                    index == 0 ||
                    !removeFlags[
                        index - 1
                    ]
                ) {
                    output.append(' ')
                }
            } else {
                output.append(
                    value[index]
                )
            }
        }

        return output.toString()
    }

    private fun stripDecorativeSymbols(
        value: String
    ): String {
        val output =
            StringBuilder(
                value.length
            )

        var index = 0

        while (index < value.length) {
            val codePoint =
                Character.codePointAt(
                    value,
                    index
                )

            index +=
                Character.charCount(
                    codePoint
                )

            val type =
                Character.getType(
                    codePoint
                )

            val shouldRemove =
                type ==
                    Character
                        .OTHER_SYMBOL
                        .toInt() ||
                    type ==
                    Character
                        .MODIFIER_SYMBOL
                        .toInt() ||
                    type ==
                    Character
                        .PRIVATE_USE
                        .toInt() ||
                    type ==
                    Character
                        .FORMAT
                        .toInt() ||
                    codePoint in
                        0x1F1E6..0x1FAFF ||
                    codePoint in
                        0x2600..0x27BF ||
                    codePoint in
                        0xFE00..0xFE0F

            if (shouldRemove) {
                output.append(' ')
            } else {
                output.appendCodePoint(
                    codePoint
                )
            }
        }

        return output.toString()
    }

    private fun normalizeSpaces(
        value: String
    ): String {
        val output =
            StringBuilder(
                value.length
            )

        var previousWasSpace = true
        var index = 0

        while (index < value.length) {
            val codePoint =
                Character.codePointAt(
                    value,
                    index
                )

            index +=
                Character.charCount(
                    codePoint
                )

            val invisible =
                codePoint in
                    0x200B..0x200D ||
                    codePoint ==
                    0xFEFF

            if (invisible) {
                continue
            }

            val isSpace =
                Character.isWhitespace(
                    codePoint
                ) ||
                    Character.isSpaceChar(
                        codePoint
                    )

            if (isSpace) {
                if (
                    !previousWasSpace &&
                    output.isNotEmpty()
                ) {
                    output.append(' ')
                }

                previousWasSpace = true
            } else {
                output.appendCodePoint(
                    codePoint
                )

                previousWasSpace = false
            }
        }

        return output
            .toString()
            .trim()
    }
}

internal data class SongNoteDraftParts(
    val title: String,
    val artist: String,
    val youtubeUrl: String?
)