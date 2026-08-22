package com.ngoctien.getmp3.library

import com.ngoctien.getmp3.data.IndexedMediaEntity
import com.ngoctien.getmp3.data.MediaIndexSource
import com.ngoctien.getmp3.data.MediaMetadataStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryAdmissionPolicyTest {

    @Test
    fun readyInboxFileIsAllowed() {

        val result =
            LibraryAdmissionPolicy
                .evaluate(
                    readyItem()
                )

        assertTrue(
            result.allowed
        )

        assertTrue(
            result.reasons.isEmpty()
        )
    }

    @Test
    fun missingYearIsRejected() {

        val result =
            LibraryAdmissionPolicy
                .evaluate(
                    readyItem().copy(
                        year = ""
                    )
                )

        assertFalse(
            result.allowed
        )

        assertTrue(
            result.reasons.any {
                "Year" in it
            }
        )
    }

    @Test
    fun missingAlbumAndCoverAreRejected() {

        val result =
            LibraryAdmissionPolicy
                .evaluate(
                    readyItem().copy(
                        album = "",
                        coverPath = null
                    )
                )

        assertFalse(
            result.allowed
        )

        assertTrue(
            result.reasons.contains(
                "Thiếu Album"
            )
        )

        assertTrue(
            result.reasons.contains(
                "Thiếu cover"
            )
        )
    }

    @Test
    fun libraryFileCannotBePromotedAgain() {

        val result =
            LibraryAdmissionPolicy
                .evaluate(
                    readyItem().copy(
                        source =
                            MediaIndexSource
                                .LIBRARY
                    )
                )

        assertFalse(
            result.allowed
        )

        assertTrue(
            result.reasons.any {
                "Inbox" in it
            }
        )
    }

    private fun readyItem():
        IndexedMediaEntity {

        return IndexedMediaEntity(
            uri =
                "content://test/song",

            source =
                MediaIndexSource
                    .INBOX,

            treeUri =
                null,

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
                "song - artist.mp3",

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
                "cover/test.jpg",

            contentSignature =
                "signature",

            scanGeneration =
                1L,

            indexedAt =
                1L
        )
    }
}
