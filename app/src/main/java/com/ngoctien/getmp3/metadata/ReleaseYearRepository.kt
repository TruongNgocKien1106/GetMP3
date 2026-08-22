package com.ngoctien.getmp3.metadata

import com.ngoctien.getmp3.note.SongNameMatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

data class ReleaseYearSuggestion(
    val year: String,
    val title: String,
    val artist: String,
    val album: String,
    val releaseDate: String,
    val versionLabel: String?,
    val score: Double,
    val source: String = "MusicBrainz",
    val sourceId: String
)

class ReleaseYearRepository {

    suspend fun lookupSuggestions(
        title: String,
        artist: String,
        album: String,
        limit: Int = DEFAULT_SUGGESTION_LIMIT
    ): List<ReleaseYearSuggestion> =
        withContext(Dispatchers.IO) {
            val cleanTitle = cleanTitleForLookup(title)
            val cleanArtist = artist.trim()

            if (cleanTitle.isBlank() || cleanArtist.isBlank()) {
                return@withContext emptyList()
            }

            val body =
                requestRecordings(
                    title = cleanTitle,
                    artist = cleanArtist
                )

            parseSuggestions(
                body = body,
                wantedTitle = cleanTitle,
                wantedArtist = cleanArtist,
                wantedAlbum = album.trim(),
                limit = limit
            )
        }

    private fun requestRecordings(
        title: String,
        artist: String
    ): String {
        val query =
            buildString {
                append("recording:\"")
                append(escapeQuery(title))
                append("\" AND artist:\"")
                append(escapeQuery(artist))
                append("\"")
            }

        val encodedQuery =
            URLEncoder.encode(
                query,
                StandardCharsets.UTF_8.name()
            )

        val endpoint =
            "$MUSIC_BRAINZ_ENDPOINT?query=$encodedQuery" +
                "&fmt=json&limit=$MUSIC_BRAINZ_RESULT_LIMIT"

        val connection =
            URL(endpoint)
                .openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.setRequestProperty(
                "Accept",
                "application/json"
            )
            connection.setRequestProperty(
                "User-Agent",
                "GetMP3/1.0 Android"
            )

            val responseCode = connection.responseCode
            val stream =
                if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

            val body =
                stream
                    ?.bufferedReader(Charsets.UTF_8)
                    ?.use { it.readText() }
                    .orEmpty()

            if (responseCode !in 200..299) {
                val apiMessage =
                    runCatching {
                        JSONObject(body)
                            .optString("error")
                            .trim()
                    }
                        .getOrNull()
                        .orEmpty()

                throw IllegalStateException(
                    apiMessage.ifBlank {
                        "Tra năm thất bại: HTTP $responseCode"
                    }
                )
            }

            return body
        } finally {
            connection.disconnect()
        }
    }

    internal fun parseSuggestions(
        body: String,
        wantedTitle: String,
        wantedArtist: String,
        wantedAlbum: String,
        limit: Int = DEFAULT_SUGGESTION_LIMIT
    ): List<ReleaseYearSuggestion> {
        val recordings =
            JSONObject(body)
                .optJSONArray("recordings")
                ?: JSONArray()

        val normalizedWantedTitle =
            SongNameMatcher.normalizeTitle(
                cleanTitleForLookup(wantedTitle)
            )
        val normalizedWantedArtist =
            SongNameMatcher.normalizeText(wantedArtist)
        val normalizedWantedAlbum =
            SongNameMatcher.normalizeText(wantedAlbum)

        val albumIsCategory =
            normalizedWantedAlbum.isBlank() ||
                normalizedWantedAlbum in categoryAlbums

        val wantedVersions =
            detectVersions(
                "$wantedTitle $wantedAlbum"
            )

        val suggestions =
            mutableListOf<ReleaseYearSuggestion>()

        for (recordingIndex in 0 until recordings.length()) {
            val recording =
                recordings.optJSONObject(recordingIndex)
                    ?: continue

            val recordingTitle =
                recording
                    .optString("title")
                    .trim()
            val recordingArtist =
                readArtistCredit(
                    recording.optJSONArray("artist-credit")
                )

            if (
                recordingTitle.isBlank() ||
                recordingArtist.isBlank()
            ) {
                continue
            }

            val titleScore =
                SongNameMatcher.similarity(
                    normalizedWantedTitle,
                    SongNameMatcher.normalizeTitle(
                        cleanTitleForLookup(recordingTitle)
                    )
                )
            val artistScore =
                SongNameMatcher.similarity(
                    normalizedWantedArtist,
                    SongNameMatcher.normalizeText(
                        recordingArtist
                    )
                )

            if (
                titleScore < MIN_TITLE_SCORE ||
                artistScore < MIN_ARTIST_SCORE
            ) {
                continue
            }

            val serverScore =
                (
                    recording.optDouble("score", 0.0) /
                        100.0
                    )
                    .coerceIn(0.0, 1.0)

            val recordingId =
                recording
                    .optString("id")
                    .trim()

            val releases =
                recording.optJSONArray("releases")
            var addedRelease = false

            if (releases != null) {
                for (releaseIndex in 0 until releases.length()) {
                    val release =
                        releases.optJSONObject(releaseIndex)
                            ?: continue

                    val releaseDate =
                        release
                            .optString("date")
                            .trim()
                    val year =
                        parseYear(releaseDate)
                            ?: continue
                    val releaseTitle =
                        release
                            .optString("title")
                            .trim()

                    val candidateVersions =
                        detectVersions(
                            listOf(
                                recordingTitle,
                                releaseTitle,
                                readReleaseGroupTypes(release)
                            )
                                .filter(String::isNotBlank)
                                .joinToString(" ")
                        )

                    val albumBonus =
                        if (
                            albumIsCategory ||
                            releaseTitle.isBlank()
                        ) {
                            0.0
                        } else {
                            SongNameMatcher.similarity(
                                normalizedWantedAlbum,
                                SongNameMatcher.normalizeText(
                                    releaseTitle
                                )
                            ) * REAL_ALBUM_WEIGHT
                        }

                    val officialBonus =
                        if (
                            release
                                .optString("status")
                                .equals(
                                    "Official",
                                    ignoreCase = true
                                )
                        ) {
                            OFFICIAL_RELEASE_BONUS
                        } else {
                            0.0
                        }

                    val score =
                        (
                            titleScore * TITLE_WEIGHT +
                                artistScore * ARTIST_WEIGHT +
                                serverScore * SERVER_WEIGHT +
                                albumBonus +
                                scoreVersionMatch(
                                    wantedVersions,
                                    candidateVersions
                                ) +
                                officialBonus
                            )
                            .coerceIn(0.0, 1.0)

                    if (score < MIN_FINAL_SCORE) {
                        continue
                    }

                    suggestions.add(
                        ReleaseYearSuggestion(
                            year = year,
                            title = recordingTitle,
                            artist = recordingArtist,
                            album = releaseTitle,
                            releaseDate = releaseDate,
                            versionLabel =
                                preferredVersionLabel(
                                    wantedVersions,
                                    candidateVersions
                                ),
                            score = score,
                            sourceId =
                                release
                                    .optString("id")
                                    .trim()
                                    .ifBlank {
                                        recordingId
                                    }
                        )
                    )
                    addedRelease = true
                }
            }

            if (!addedRelease) {
                val releaseDate =
                    recording
                        .optString("first-release-date")
                        .trim()
                val year =
                    parseYear(releaseDate)
                        ?: continue
                val candidateVersions =
                    detectVersions(recordingTitle)
                val score =
                    (
                        titleScore * TITLE_WEIGHT +
                            artistScore * ARTIST_WEIGHT +
                            serverScore * SERVER_WEIGHT +
                            scoreVersionMatch(
                                wantedVersions,
                                candidateVersions
                            )
                        )
                        .coerceIn(0.0, 1.0)

                if (score >= MIN_FINAL_SCORE) {
                    suggestions.add(
                        ReleaseYearSuggestion(
                            year = year,
                            title = recordingTitle,
                            artist = recordingArtist,
                            album = "",
                            releaseDate = releaseDate,
                            versionLabel =
                                preferredVersionLabel(
                                    wantedVersions,
                                    candidateVersions
                                ),
                            score = score,
                            sourceId = recordingId
                        )
                    )
                }
            }
        }

        return suggestions
            .sortedWith(
                compareByDescending<ReleaseYearSuggestion> {
                    it.score
                }.thenBy {
                    it.releaseDate
                }
            )
            .distinctBy {
                buildString {
                    append(it.year)
                    append('|')
                    append(
                        it.versionLabel
                            .orEmpty()
                            .lowercase(Locale.ROOT)
                    )
                    append('|')
                    append(
                        SongNameMatcher.normalizeText(
                            it.album
                        )
                    )
                }
            }
            .take(
                limit.coerceIn(
                    1,
                    MAX_SUGGESTION_LIMIT
                )
            )
    }

    private fun readArtistCredit(
        credit: JSONArray?
    ): String {
        if (credit == null) {
            return ""
        }

        val names =
            mutableListOf<String>()

        for (index in 0 until credit.length()) {
            val item =
                credit.optJSONObject(index)
                    ?: continue
            val name =
                item
                    .optString("name")
                    .trim()
                    .ifBlank {
                        item
                            .optJSONObject("artist")
                            ?.optString("name")
                            ?.trim()
                            .orEmpty()
                    }

            if (name.isNotBlank()) {
                names.add(name)
            }
        }

        return names
            .distinct()
            .joinToString(" & ")
    }

    private fun readReleaseGroupTypes(
        release: JSONObject
    ): String {
        val group =
            release.optJSONObject("release-group")
                ?: return ""

        val values =
            mutableListOf<String>()

        group
            .optString("primary-type")
            .trim()
            .takeIf(String::isNotBlank)
            ?.let(values::add)

        val secondary =
            group.optJSONArray("secondary-types")

        if (secondary != null) {
            for (index in 0 until secondary.length()) {
                secondary
                    .optString(index)
                    .trim()
                    .takeIf(String::isNotBlank)
                    ?.let(values::add)
            }
        }

        return values.joinToString(" ")
    }

    private fun scoreVersionMatch(
        wantedVersions: Set<ReleaseVersion>,
        candidateVersions: Set<ReleaseVersion>
    ): Double {
        if (wantedVersions.isEmpty()) {
            return if (candidateVersions.isEmpty()) {
                ORIGINAL_VERSION_BONUS
            } else {
                UNWANTED_VERSION_PENALTY
            }
        }

        if (
            candidateVersions.any {
                it in wantedVersions
            }
        ) {
            return MATCHING_VERSION_BONUS
        }

        return if (candidateVersions.isEmpty()) {
            MISSING_VERSION_PENALTY
        } else {
            MISMATCHED_VERSION_PENALTY
        }
    }

    private fun preferredVersionLabel(
        wantedVersions: Set<ReleaseVersion>,
        candidateVersions: Set<ReleaseVersion>
    ): String? =
        candidateVersions
            .firstOrNull {
                it in wantedVersions
            }
            ?.label
            ?: candidateVersions
                .firstOrNull()
                ?.label

    private fun detectVersions(
        value: String
    ): Set<ReleaseVersion> {
        val normalized =
            SongNameMatcher.normalizeText(value)

        if (normalized.isBlank()) {
            return emptySet()
        }

        return buildSet {
            if (
                containsWord(normalized, "remix") ||
                containsWord(normalized, "vinahouse")
            ) {
                add(ReleaseVersion.REMIX)
            }
            if (containsWord(normalized, "live")) {
                add(ReleaseVersion.LIVE)
            }
            if (containsWord(normalized, "acoustic")) {
                add(ReleaseVersion.ACOUSTIC)
            }
            if (
                normalized.contains("lofi") ||
                normalized.contains("lo fi")
            ) {
                add(ReleaseVersion.LOFI)
            }
            if (
                normalized.contains("speed up") ||
                normalized.contains("sped up")
            ) {
                add(ReleaseVersion.SPEED_UP)
            }
            if (
                containsWord(
                    normalized,
                    "instrumental"
                )
            ) {
                add(ReleaseVersion.INSTRUMENTAL)
            }
            if (normalized.contains("remaster")) {
                add(ReleaseVersion.REMASTER)
            }
        }
    }

    private fun containsWord(
        normalizedText: String,
        word: String
    ): Boolean =
        normalizedText
            .split(' ')
            .any { it == word }

    private fun parseYear(
        releaseDate: String
    ): String? {
        val year =
            Regex("""^\d{4}""")
                .find(releaseDate)
                ?.value
                ?: return null
        val numericYear =
            year.toIntOrNull()
                ?: return null

        return year.takeIf {
            numericYear in
                MIN_RELEASE_YEAR..MAX_RELEASE_YEAR
        }
    }

    private fun cleanTitleForLookup(
        value: String
    ): String {
        val original =
            value.trim()

        if (original.isBlank()) {
            return ""
        }

        val withoutTrackPrefix =
            original
                .replace(
                    Regex(
                        """^(?i:track)\s+\d{1,3}\s*[-._):]\s*"""
                    ),
                    ""
                )
                .replace(
                    Regex("""^0\d{1,2}\s+"""),
                    ""
                )
                .trim()

        return withoutTrackPrefix
            .ifBlank { original }
    }

    private fun escapeQuery(
        value: String
    ): String =
        value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")

    private enum class ReleaseVersion(
        val label: String
    ) {
        REMIX("Remix"),
        LIVE("Live"),
        ACOUSTIC("Acoustic"),
        LOFI("Lofi"),
        SPEED_UP("Speed Up"),
        INSTRUMENTAL("Instrumental"),
        REMASTER("Remaster")
    }

    private companion object {
        const val MUSIC_BRAINZ_ENDPOINT =
            "https://musicbrainz.org/ws/2/recording/"
        const val MUSIC_BRAINZ_RESULT_LIMIT = 16
        const val DEFAULT_SUGGESTION_LIMIT = 3
        const val MAX_SUGGESTION_LIMIT = 5

        const val CONNECT_TIMEOUT_MS = 10_000
        const val READ_TIMEOUT_MS = 12_000

        const val MIN_RELEASE_YEAR = 1900
        const val MAX_RELEASE_YEAR = 2100

        const val MIN_TITLE_SCORE = 0.55
        const val MIN_ARTIST_SCORE = 0.45
        const val MIN_FINAL_SCORE = 0.58

        const val TITLE_WEIGHT = 0.58
        const val ARTIST_WEIGHT = 0.30
        const val SERVER_WEIGHT = 0.04
        const val REAL_ALBUM_WEIGHT = 0.08
        const val OFFICIAL_RELEASE_BONUS = 0.02

        const val ORIGINAL_VERSION_BONUS = 0.03
        const val MATCHING_VERSION_BONUS = 0.14
        const val UNWANTED_VERSION_PENALTY = -0.08
        const val MISSING_VERSION_PENALTY = -0.04
        const val MISMATCHED_VERSION_PENALTY = -0.14

        val categoryAlbums =
            setOf(
                "Nhạc Việt",
                "US-UK",
                "Nhạc Trung",
                "Nhạc Hàn",
                "Nhạc Nhật",
                "Remix",
                "Lofi",
                "Speed Up",
                "Instrumental"
            )
                .map {
                    SongNameMatcher.normalizeText(it)
                }
                .toSet()
    }
}
