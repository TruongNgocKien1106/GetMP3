package com.ngoctien.getmp3.lyrics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LyricsMatchingTest {

    @Test
    fun parseLyricsSearchInput_plainText_keepsWholeTitle() {
        val parsed =
            parseLyricsSearchInput(
                "  Lời Nói Dối Chân Thật  "
            )

        assertEquals(
            "Lời Nói Dối Chân Thật",
            parsed.title
        )
        assertEquals("", parsed.artist)
    }

    @Test
    fun parseLyricsSearchInput_titleAndArtist_splitsAtLastSeparator() {
        val parsed =
            parseLyricsSearchInput(
                "Lời Nói Dối Chân Thật - JustaTee"
            )

        assertEquals(
            "Lời Nói Dối Chân Thật",
            parsed.title
        )
        assertEquals("JustaTee", parsed.artist)
    }

    @Test
    fun rankWriteCandidates_usesFileNameWhenMetadataTitleIsWeak() {
        val expected =
            candidate(
                uri = "content://expected",
                displayName =
                    "Loi Noi Doi Chan That - JustaTee.mp3",
                title = "Track 01",
                artist = "JustaTee"
            )

        val unrelated =
            candidate(
                uri = "content://unrelated",
                displayName = "Bai Khac.mp3",
                title = "Bài Khác",
                artist = "Ca sĩ khác"
            )

        val ranked =
            rankWriteCandidates(
                songs =
                    listOf(
                        unrelated,
                        expected
                    ),
                title =
                    "Lời Nói Dối Chân Thật",
                artist = "JustaTee"
            )

        assertTrue(ranked.isNotEmpty())
        assertEquals(
            expected.uri,
            ranked.first().uri
        )
        assertFalse(
            ranked.any {
                it.uri == unrelated.uri
            }
        )
    }

    @Test
    fun rankWriteCandidates_preferredUriBoostsOnlyRelevantSong() {
        val exact =
            candidate(
                uri = "content://exact",
                displayName =
                    "Noi Nay Co Anh.mp3",
                title = "Nơi Này Có Anh",
                artist = "Sơn Tùng M-TP"
            )

        val unrelatedPreferred =
            candidate(
                uri = "content://preferred",
                displayName = "Bài Khác.mp3",
                title = "Bài Khác",
                artist = "Ca sĩ khác"
            )

        val ranked =
            rankWriteCandidates(
                songs =
                    listOf(
                        unrelatedPreferred,
                        exact
                    ),
                title = "Nơi Này Có Anh",
                artist = "Sơn Tùng M-TP",
                preferredUri =
                    unrelatedPreferred.uri
            )

        assertEquals(
            exact.uri,
            ranked.first().uri
        )
        assertFalse(
            ranked.any {
                it.uri == unrelatedPreferred.uri
            }
        )
    }

    @Test
    fun rankWriteCandidates_neverReturnsMoreThanTwelve() {
        val songs =
            (1..20).map { index ->
                candidate(
                    uri = "content://song-$index",
                    displayName =
                        "Nơi Này Có Anh $index.mp3",
                    title =
                        "Nơi Này Có Anh $index",
                    artist = "Sơn Tùng M-TP"
                )
            }

        val ranked =
            rankWriteCandidates(
                songs = songs,
                title = "Nơi Này Có Anh",
                artist = "Sơn Tùng M-TP",
                limit = 99
            )

        assertEquals(12, ranked.size)
    }

    @Test
    fun decideLyricsWrite_emptyExisting_writesImmediately() {
        assertEquals(
            LyricsWriteDecision.WRITE_NOW,
            decideLyricsWrite(
                existingLyrics = "",
                newLyrics = "Lời mới"
            )
        )
    }

    @Test
    fun decideLyricsWrite_sameContent_doesNotAskToReplace() {
        assertEquals(
            LyricsWriteDecision.ALREADY_IDENTICAL,
            decideLyricsWrite(
                existingLyrics =
                    "Dòng một  \r\nDòng hai",
                newLyrics =
                    "Dòng một\nDòng hai"
            )
        )
    }

    @Test
    fun decideLyricsWrite_differentExisting_asksToReplace() {
        assertEquals(
            LyricsWriteDecision.CONFIRM_REPLACE,
            decideLyricsWrite(
                existingLyrics = "Lời cũ",
                newLyrics = "Lời mới"
            )
        )
    }

    private fun candidate(
        uri: String,
        displayName: String,
        title: String,
        artist: String
    ): LibrarySongCandidate {
        return LibrarySongCandidate(
            uri = uri,
            treeUri = null,
            displayName = displayName,
            title = title,
            artist = artist
        )
    }
}
