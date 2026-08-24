package com.ngoctien.getmp3.library

import com.ngoctien.getmp3.data.IndexedMediaEntity
import com.ngoctien.getmp3.data.MediaIndexSource
import com.ngoctien.getmp3.data.MediaMetadataStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaCompliancePolicyTest {

    @Test
    fun compliantId3v23FilePasses() {

        val result =
            MediaCompliancePolicy
                .evaluate(
                    item =
                        readyItem(),

                    id3MajorVersion =
                        3,

                    id3AuditKnown =
                        true
                )

        assertEquals(
            MediaComplianceLevel.COMPLIANT,
            result.level
        )

        assertTrue(
            result.issues.isEmpty()
        )
    }

    @Test
    fun nonCanonicalFilenameNeedsNormalization() {

        val result =
            MediaCompliancePolicy
                .evaluate(
                    item =
                        readyItem()
                            .copy(
                                displayName =
                                    "Song – Artist.mp3"
                            ),

                    id3MajorVersion =
                        3,

                    id3AuditKnown =
                        true
                )

        assertEquals(
            MediaComplianceLevel.NEEDS_NORMALIZATION,
            result.level
        )

        assertTrue(
            MediaComplianceIssue
                .NON_CANONICAL_FILENAME in
                result.issues
        )
    }

    @Test
    fun id3v24NeedsNormalization() {

        val result =
            MediaCompliancePolicy
                .evaluate(
                    item =
                        readyItem(),

                    id3MajorVersion =
                        4,

                    id3AuditKnown =
                        true
                )

        assertEquals(
            MediaComplianceLevel.NEEDS_NORMALIZATION,
            result.level
        )

        assertTrue(
            MediaComplianceIssue
                .NON_ID3_V23 in
                result.issues
        )
    }

    @Test
    fun fullDateStoredInYearNeedsNormalization() {

        val result =
            MediaCompliancePolicy
                .evaluate(
                    item =
                        readyItem()
                            .copy(
                                year =
                                    "2026",

                                rawTagYear =
                                    "2026-08-24"
                            ),

                    id3MajorVersion =
                        3,

                    id3AuditKnown =
                        true
                )

        assertEquals(
            MediaComplianceLevel.NEEDS_NORMALIZATION,
            result.level
        )

        assertTrue(
            MediaComplianceIssue
                .INVALID_YEAR in
                result.issues
        )
    }

    @Test
    fun unreadableMetadataIsBroken() {

        val result =
            MediaCompliancePolicy
                .evaluate(
                    item =
                        readyItem()
                            .copy(
                                metadataStatus =
                                    MediaMetadataStatus
                                        .UNREADABLE_FILE
                            ),

                    id3MajorVersion =
                        null,

                    id3AuditKnown =
                        true
                )

        assertEquals(
            MediaComplianceLevel.BROKEN,
            result.level
        )
    }

    private fun readyItem():
        IndexedMediaEntity {

        return IndexedMediaEntity(
            uri =
                "content://test/song",

            source =
                MediaIndexSource
                    .LIBRARY,

            treeUri =
                "content://test/library",

            documentId =
                "content://test/song",

            displayName =
                "Song - Artist.mp3",

            mimeType =
                "audio/mpeg",

            sizeBytes =
                1024L,

            lastModifiedMs =
                1L,

            fileTitle =
                "Song",

            fileArtist =
                "Artist",

            tagTitle =
                "Song",

            tagArtist =
                "Artist",

            album =
                "Nhạc Việt",

            albumArtist =
                "",

            year =
                "2026",

            rawTagTitle =
                "Song",

            rawTagArtist =
                "Artist",

            rawTagAlbum =
                "Nhạc Việt",

            rawTagAlbumArtist =
                null,

            rawTagYear =
                "2026",

            metadataStatus =
                MediaMetadataStatus
                    .OK,

            metadataErrorCode =
                null,

            metadataErrorFields =
                null,

            metadataErrorMessage =
                null,

            title =
                "Song",

            artist =
                "Artist",

            canonicalFileName =
                "song artist",

            normalizedFileName =
                "song artist",

            normalizedTitle =
                "song",

            normalizedArtist =
                "artist",

            normalizedAlbum =
                "nhac viet",

            titleTokens =
                "song",

            durationMs =
                180_000L,

            bitrateKbps =
                128,

            coverPath =
                "cover/song.jpg",

            contentSignature =
                "signature",

            scanGeneration =
                1L,

            indexedAt =
                1L
        )
    }
}