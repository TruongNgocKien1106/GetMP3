package com.ngoctien.getmp3.note

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReferenceSongSearchTest {

    @Test
    fun normalizeTitle_removesNoiseAndVietnameseMarks() {
        val normalized =
            SongNameMatcher.normalizeTitle(
                "Nơi Này Có Anh (Official Music Video)"
            )

        assertEquals(
            "noi nay co anh",
            normalized
        )
    }

    @Test
    fun similarity_exactNormalizedTitle_isPerfectMatch() {
        val query =
            SongNameMatcher.normalizeTitle(
                "Nơi Này Có Anh"
            )

        val candidate =
            SongNameMatcher.normalizeTitle(
                "Noi Nay Co Anh"
            )

        assertEquals(
            1.0,
            SongNameMatcher.similarity(
                query,
                candidate
            ),
            0.0001
        )
    }

    @Test
    fun parseFileName_extractsTitleAndArtist() {
        val parsed =
            requireNotNull(
                SongNameMatcher.parseFileName(
                    "Nơi Này Có Anh - Sơn Tùng M-TP.mp3"
                )
            )

        assertEquals(
            "Nơi Này Có Anh",
            parsed.title
        )

        assertEquals(
            "Sơn Tùng M-TP",
            parsed.artist
        )

        assertTrue(
            parsed.normalizedTitle
                .contains("noi nay co anh")
        )
    }
    @Test
    fun parseFileName_preservesConfiguredTitleArtistRule() {
        val parsed =
            requireNotNull(
                SongNameMatcher.parseFileName(
                    "Tìm Em Trong Mơ - Chi Dân.mp3"
                )
            )

        assertEquals(
            "Tìm Em Trong Mơ",
            parsed.title
        )

        assertEquals(
            "Chi Dân",
            parsed.artist
        )
    }

}
