package com.ngoctien.getmp3.library

import com.ngoctien.getmp3.data.IndexedMediaEntity
import com.ngoctien.getmp3.data.MediaMetadataStatus

enum class MediaComplianceLevel {
    COMPLIANT,
    NEEDS_NORMALIZATION,
    BROKEN
}

enum class MediaComplianceIssue(
    val key: String,
    val label: String
) {
    INVALID_EXTENSION(
        key = "fileType",
        label = "File không phải MP3"
    ),

    EMPTY_FILE(
        key = "file",
        label = "File rỗng"
    ),

    BROKEN_METADATA(
        key = "metadata",
        label = "Metadata không đọc được"
    ),

    NON_ID3_V23(
        key = "id3Version",
        label = "ID3 không phải v2.3"
    ),

    MISSING_TITLE(
        key = "title",
        label = "Thiếu Title"
    ),

    MISSING_ARTIST(
        key = "artist",
        label = "Thiếu Artist"
    ),

    MISSING_ALBUM(
        key = "album",
        label = "Thiếu Album"
    ),

    INVALID_YEAR(
        key = "year",
        label = "Year phải gồm đúng 4 chữ số"
    ),

    MISSING_COVER(
        key = "cover",
        label = "Thiếu cover"
    ),

    NON_CANONICAL_FILENAME(
        key = "filename",
        label = "Tên file không đúng chuẩn Title - Artist.mp3"
    )
}

data class MediaComplianceResult(
    val level: MediaComplianceLevel,
    val issues: List<MediaComplianceIssue>
) {
    val isCompliant: Boolean
        get() =
            level ==
                MediaComplianceLevel.COMPLIANT

    val needsNormalization: Boolean
        get() =
            level ==
                MediaComplianceLevel.NEEDS_NORMALIZATION

    val isBroken: Boolean
        get() =
            level ==
                MediaComplianceLevel.BROKEN

    val issueFields: String?
        get() =
            issues
                .takeIf {
                    it.isNotEmpty()
                }
                ?.joinToString(
                    separator = ","
                ) {
                    it.key
                }

    val message: String?
        get() =
            issues
                .takeIf {
                    it.isNotEmpty()
                }
                ?.joinToString(
                    separator = " • "
                ) {
                    it.label
                }
}

data class MediaComplianceSummary(
    val compliantFiles: Int = 0,
    val normalizationFiles: Int = 0,
    val brokenFiles: Int = 0
) {
    val attentionFiles: Int
        get() =
            normalizationFiles +
                brokenFiles
}

object MediaCompliancePolicy {

    private val yearPattern =
        Regex("""\d{4}""")

    private val invalidFileCharacters =
        Regex("""[\\/:*?"<>|]""")

    private val controlCharacters =
        Regex("""[\u0000-\u001F\u007F]""")

    private val repeatedWhitespace =
        Regex("""\s+""")

    fun evaluate(
        item: IndexedMediaEntity,
        id3MajorVersion: Int? = null,
        id3AuditKnown: Boolean = false
    ): MediaComplianceResult {

        val storedFields =
            item.metadataErrorFields
                .orEmpty()
                .split(',')
                .asSequence()
                .map {
                    it.trim()
                }
                .filter {
                    it.isNotBlank()
                }
                .toSet()

        val issues =
            linkedSetOf<MediaComplianceIssue>()

        if (
            !item.displayName
                .endsWith(
                    ".mp3",
                    ignoreCase = true
                )
        ) {
            issues +=
                MediaComplianceIssue
                    .INVALID_EXTENSION
        }

        if (
            item.sizeBytes <=
            0L
        ) {
            issues +=
                MediaComplianceIssue
                    .EMPTY_FILE
        }

        if (
            MediaMetadataStatus
                .isError(
                    item.metadataStatus
                )
        ) {
            issues +=
                MediaComplianceIssue
                    .BROKEN_METADATA
        }

        val hasWrongId3Version =
            if (id3AuditKnown) {
                id3MajorVersion != 3
            } else {
                MediaComplianceIssue
                    .NON_ID3_V23
                    .key in
                    storedFields
            }

        if (hasWrongId3Version) {
            issues +=
                MediaComplianceIssue
                    .NON_ID3_V23
        }

        if (
            item.tagTitle
                .trim()
                .isBlank()
        ) {
            issues +=
                MediaComplianceIssue
                    .MISSING_TITLE
        }

        if (
            item.tagArtist
                .trim()
                .isBlank()
        ) {
            issues +=
                MediaComplianceIssue
                    .MISSING_ARTIST
        }

        if (
            item.album
                .trim()
                .isBlank()
        ) {
            issues +=
                MediaComplianceIssue
                    .MISSING_ALBUM
        }

        val rawYear =
            item.rawTagYear
                ?.trim()
                .orEmpty()

        if (
            !yearPattern
                .matches(
                    rawYear
                )
        ) {
            issues +=
                MediaComplianceIssue
                    .INVALID_YEAR
        }

        if (
            item.coverPath
                .isNullOrBlank()
        ) {
            issues +=
                MediaComplianceIssue
                    .MISSING_COVER
        }

        val canonicalName =
            canonicalFileName(
                title =
                    item.tagTitle,

                artist =
                    item.tagArtist
            )

        if (
            canonicalName != null &&
            item.displayName !=
            canonicalName
        ) {
            issues +=
                MediaComplianceIssue
                    .NON_CANONICAL_FILENAME
        }

        val broken =
            issues.any {
                it ==
                    MediaComplianceIssue
                        .INVALID_EXTENSION ||
                    it ==
                    MediaComplianceIssue
                        .EMPTY_FILE ||
                    it ==
                    MediaComplianceIssue
                        .BROKEN_METADATA
            }

        val level =
            when {
                broken ->
                    MediaComplianceLevel
                        .BROKEN

                issues.isNotEmpty() ->
                    MediaComplianceLevel
                        .NEEDS_NORMALIZATION

                else ->
                    MediaComplianceLevel
                        .COMPLIANT
            }

        return MediaComplianceResult(
            level =
                level,

            issues =
                issues.toList()
        )
    }

    fun summarize(
        items: Iterable<IndexedMediaEntity>
    ): MediaComplianceSummary {

        var compliant = 0
        var normalization = 0
        var broken = 0

        items.forEach { item ->
            when (
                evaluate(item)
                    .level
            ) {
                MediaComplianceLevel
                    .COMPLIANT ->
                    compliant++

                MediaComplianceLevel
                    .NEEDS_NORMALIZATION ->
                    normalization++

                MediaComplianceLevel
                    .BROKEN ->
                    broken++
            }
        }

        return MediaComplianceSummary(
            compliantFiles =
                compliant,

            normalizationFiles =
                normalization,

            brokenFiles =
                broken
        )
    }

    fun canonicalFileName(
        title: String,
        artist: String
    ): String? {

        val cleanTitle =
            title.trim()

        val cleanArtist =
            artist.trim()

        if (
            cleanTitle.isBlank() ||
            cleanArtist.isBlank()
        ) {
            return null
        }

        return sanitizeFileBase(
            "$cleanTitle - $cleanArtist"
        ) + ".mp3"
    }

    private fun sanitizeFileBase(
        value: String
    ): String {

        return value
            .replace(
                invalidFileCharacters,
                "_"
            )
            .replace(
                controlCharacters,
                ""
            )
            .replace(
                repeatedWhitespace,
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