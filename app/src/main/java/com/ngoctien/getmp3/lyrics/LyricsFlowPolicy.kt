package com.ngoctien.getmp3.lyrics

internal enum class LyricsWriteDecision {
    WRITE_NOW,
    ALREADY_IDENTICAL,
    CONFIRM_REPLACE
}

internal fun decideLyricsWrite(
    existingLyrics: String,
    newLyrics: String
): LyricsWriteDecision {
    val existing =
        normalizeLyricsForComparison(
            existingLyrics
        )

    val replacement =
        normalizeLyricsForComparison(
            newLyrics
        )

    return when {
        existing.isBlank() ->
            LyricsWriteDecision.WRITE_NOW

        existing == replacement ->
            LyricsWriteDecision.ALREADY_IDENTICAL

        else ->
            LyricsWriteDecision.CONFIRM_REPLACE
    }
}

private fun normalizeLyricsForComparison(
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
        .trim()
}
