package com.ngoctien.getmp3.lyrics

import com.ngoctien.getmp3.note.SongNameMatcher

internal data class LyricsSearchInput(
    val displayText: String,
    val title: String,
    val artist: String
)

internal fun parseLyricsSearchInput(
    rawInput: String
): LyricsSearchInput {
    val cleanInput = rawInput
        .trim()
        .replace(
            Regex("""[\s\p{Zs}]+"""),
            " "
        )

    val separatorIndex =
        cleanInput.lastIndexOf(" - ")

    val title =
        if (separatorIndex > 0) {
            cleanInput
                .substring(
                    startIndex = 0,
                    endIndex = separatorIndex
                )
                .trim()
        } else {
            cleanInput
        }

    val artist =
        if (
            separatorIndex > 0 &&
            separatorIndex + 3 < cleanInput.length
        ) {
            cleanInput
                .substring(separatorIndex + 3)
                .trim()
        } else {
            ""
        }

    return LyricsSearchInput(
        displayText = cleanInput,
        title = title,
        artist = artist
    )
}

internal fun rankWriteCandidates(
    songs: List<LibrarySongCandidate>,
    title: String,
    artist: String,
    preferredUri: String? = null,
    limit: Int = 12
): List<LibrarySongCandidate> {
    val normalizedTitle =
        SongNameMatcher.normalizeTitle(title)

    if (normalizedTitle.isBlank()) {
        return emptyList()
    }

    val normalizedArtist =
        SongNameMatcher.normalizeText(artist)

    return songs.asSequence()
        .map { song ->
            val songTitle =
                SongNameMatcher.normalizeTitle(
                    song.title
                )

            val parsedFileName =
                SongNameMatcher.parseFileName(
                    song.displayName
                )

            val fileStem =
                song.displayName
                    .substringBeforeLast(
                        delimiter = ".",
                        missingDelimiterValue =
                            song.displayName
                    )

            val normalizedFileName =
                parsedFileName
                    ?.normalizedTitle
                    ?.takeIf(
                        String::isNotBlank
                    )
                    ?: SongNameMatcher
                        .normalizeTitle(
                            fileStem
                        )

            val titleScore =
                SongNameMatcher.similarity(
                    normalizedTitle,
                    songTitle
                )

            val fileNameScore =
                SongNameMatcher.similarity(
                    normalizedTitle,
                    normalizedFileName
                )

            val songArtist =
                SongNameMatcher.normalizeText(
                    song.artist
                )

            val fileArtist =
                SongNameMatcher.normalizeText(
                    parsedFileName
                        ?.artist
                        .orEmpty()
                )

            val artistScore =
                if (normalizedArtist.isBlank()) {
                    0.5
                } else {
                    maxOf(
                        songArtist
                            .takeIf(
                                String::isNotBlank
                            )
                            ?.let {
                                SongNameMatcher
                                    .similarity(
                                        normalizedArtist,
                                        it
                                    )
                            }
                            ?: 0.0,
                        fileArtist
                            .takeIf(
                                String::isNotBlank
                            )
                            ?.let {
                                SongNameMatcher
                                    .similarity(
                                        normalizedArtist,
                                        it
                                    )
                            }
                            ?: 0.0
                    )
                }

            val exactOrContainsBonus =
                when {
                    songTitle == normalizedTitle ->
                        0.08

                    normalizedFileName ==
                        normalizedTitle ->
                        0.07

                    songTitle.contains(
                        normalizedTitle
                    ) ||
                        normalizedTitle.contains(
                            songTitle
                        ) ->
                        0.04

                    else ->
                        0.0
                }

            val strongestTitleScore =
                maxOf(
                    titleScore,
                    fileNameScore
                )

            val secondaryTitleScore =
                minOf(
                    titleScore,
                    fileNameScore
                )

            val baseScore =
                (
                    strongestTitleScore * 0.68 +
                        artistScore * 0.20 +
                        secondaryTitleScore * 0.05 +
                        exactOrContainsBonus
                    )
                    .coerceIn(
                        minimumValue = 0.0,
                        maximumValue = 1.0
                    )

            val preferredBoost =
                if (
                    preferredUri != null &&
                    song.uri == preferredUri &&
                    maxOf(
                        titleScore,
                        fileNameScore
                    ) >= 0.55
                ) {
                    0.08
                } else {
                    0.0
                }

            song.copy(
                score =
                    (baseScore + preferredBoost)
                        .coerceIn(
                            minimumValue = 0.0,
                            maximumValue = 1.0
                        )
            )
        }
        .filter {
            it.score >= 0.55
        }
        .sortedWith(
            compareByDescending<LibrarySongCandidate> {
                it.score
            }.thenBy {
                it.displayName.lowercase()
            }
        )
        .distinctBy {
            it.uri
        }
        .take(
            limit.coerceIn(
                minimumValue = 1,
                maximumValue = 12
            )
        )
        .toList()
}
