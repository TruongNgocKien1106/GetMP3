package com.ngoctien.getmp3.lyrics

private val SyncedTimestampRegex =
    Regex(
        """^\s*(?:\[[0-9:.]+\])+\s*"""
    )

data class LibrarySongCandidate(
    val uri: String,
    val treeUri: String?,
    val displayName: String,
    val title: String,
    val artist: String,
    val score: Double = 1.0
)

data class LyricsSearchResult(
    val id: Long,
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val durationSeconds: Int?,
    val plainLyrics: String,
    val syncedLyrics: String?,
    val score: Double
) {
    val readableLyrics: String
        get() {
            val plain =
                plainLyrics.trim()

            if (plain.isNotBlank()) {
                return plain
            }

            return syncedLyrics
                .orEmpty()
                .lineSequence()
                .map { line ->
                    line.replace(
                        SyncedTimestampRegex,
                        ""
                    ).trim()
                }
                .filter(String::isNotBlank)
                .joinToString("\n")
                .trim()
        }

    val formattedDuration: String
        get() {
            val seconds =
                durationSeconds
                    ?.takeIf {
                        it >= 0
                    }
                    ?: return "--:--"

            val hours =
                seconds / 3600

            val minutes =
                (
                    seconds %
                        3600
                    ) / 60

            val remainingSeconds =
                seconds % 60

            return if (hours > 0) {
                "%d:%02d:%02d".format(
                    hours,
                    minutes,
                    remainingSeconds
                )
            } else {
                "%d:%02d".format(
                    minutes,
                    remainingSeconds
                )
            }
        }
}

data class StoredLyrics(
    val text: String,
    val language: String,
    val description: String
)

enum class LyricsScreen {
    LIBRARY,
    RESULTS,
    READER,
    EDITOR
}