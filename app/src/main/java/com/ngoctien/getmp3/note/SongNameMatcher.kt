package com.ngoctien.getmp3.note

import java.text.Normalizer
import java.util.Locale
import kotlin.math.max

internal data class ParsedSongName(
    val displayName: String,
    val title: String,
    val artist: String,
    val normalizedTitle: String
)

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

    fun parseFileName(
        displayName: String
    ): ParsedSongName? {
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
            splitTitleArtist(
                stem
            )

        val title =
            split.first.ifBlank {
                stem
            }

        val normalizedTitle =
            normalizeTitle(
                title
            )

        if (normalizedTitle.isBlank()) {
            return null
        }

        return ParsedSongName(
            displayName =
                displayName,

            title =
                title,

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
                .lowercase(
                    Locale.ROOT
                )
                .replace(
                    'đ',
                    'd'
                ),
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
                bestIndex =
                    index

                bestSeparator =
                    separator
            }
        }

        if (bestIndex <= 0) {
            return normalizeSpaces(
                value
            ) to ""
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
                ] =
                    minOf(
                        insertion,
                        deletion,
                        replacement
                    )
            }

            val temporary =
                previous

            previous =
                current

            current =
                temporary
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

                previousWasSpace =
                    true
            } else {
                output.appendCodePoint(
                    codePoint
                )

                previousWasSpace =
                    false
            }
        }

        return output
            .toString()
            .trim()
    }
}