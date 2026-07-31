package com.ngoctien.getmp3.note

import android.content.Context
import com.ngoctien.getmp3.note.data.DuplicateSongMatch
import com.ngoctien.getmp3.note.data.DuplicateSource
import com.ngoctien.getmp3.note.data.SongNoteDatabase
import com.ngoctien.getmp3.note.data.SongNoteDraft
import com.ngoctien.getmp3.note.data.SongNoteEntity
import kotlinx.coroutines.flow.Flow

class SongNoteRepository(
    context: Context
) {
    companion object {
        private const val NOTE_MATCH_THRESHOLD =
            0.86
    }

    private val applicationContext =
        context.applicationContext

    private val dao =
        SongNoteDatabase
            .getInstance(
                applicationContext
            )
            .songNoteDao()

    private val compareRepository =
        CompareSongIndexRepository(
            applicationContext
        )

    fun observeNotes():
        Flow<List<SongNoteEntity>> {

        return dao.observeAll()
    }

    fun parseDraft(
        rawText: String
    ): SongNoteDraft {
        val clean =
            rawText
                .trim()
                .replace(
                    Regex("""\s+"""),
                    " "
                )

        require(clean.isNotBlank()) {
            "Hãy nhập tên bài hát"
        }

        val parsed =
            SongNameMatcher.parseInput(
                clean
            )

        val normalizedTitle =
            SongNameMatcher
                .normalizeTitle(
                    parsed.title
                )

        require(
            normalizedTitle.isNotBlank() ||
                parsed.youtubeUrl != null
        ) {
            "Tên bài hát không hợp lệ"
        }

        return SongNoteDraft(
            rawText = clean,

            title =
                parsed.title,

            artist =
                parsed.artist,

            normalizedTitle =
                normalizedTitle,

            youtubeUrl =
                parsed.youtubeUrl
        )
    }

    suspend fun findDuplicates(
        draft: SongNoteDraft
    ): List<DuplicateSongMatch> {
        if (
            draft.youtubeUrl != null ||
            draft.normalizedTitle.isBlank()
        ) {
            return emptyList()
        }

        val existingNotes =
            dao.getAllOnce()

        val noteMatches =
            existingNotes.mapNotNull { note ->
                val score =
                    SongNameMatcher.similarity(
                        draft.normalizedTitle,
                        note.normalizedTitle
                    )

                if (
                    score <
                    NOTE_MATCH_THRESHOLD
                ) {
                    null
                } else {
                    DuplicateSongMatch(
                        key =
                            "note:${note.id}",

                        title =
                            note.title,

                        artist =
                            note.artist,

                        source =
                            DuplicateSource
                                .NOTE_LIST,

                        score = score
                    )
                }
            }

        val compareMatches =
            compareRepository
                .findNearMatches(
                    title = draft.title,
                    limit = 5
                )

        return (
            noteMatches +
                compareMatches
            )
            .sortedByDescending {
                it.score
            }
            .distinctBy {
                "${it.source}:${SongNameMatcher.normalizeTitle(it.title)}:${SongNameMatcher.normalizeText(it.artist)}"
            }
            .take(7)
    }

    suspend fun insert(
        draft: SongNoteDraft
    ): Long {
        val now =
            System.currentTimeMillis()

        return dao.insert(
            SongNoteEntity(
                rawText =
                    draft.rawText,

                title =
                    draft.title,

                artist =
                    draft.artist,

                normalizedTitle =
                    draft.normalizedTitle,

                youtubeUrl =
                    draft.youtubeUrl,

                sortOrder = now,

                createdAt = now,

                updatedAt = now
            )
        )
    }

    suspend fun toggleCompleted(
        note: SongNoteEntity
    ) {
        val now =
            System.currentTimeMillis()

        val completed =
            !note.isCompleted

        dao.update(
            note.copy(
                isCompleted =
                    completed,

                completedAt =
                    if (completed) {
                        now
                    } else {
                        null
                    },

                updatedAt = now
            )
        )
    }

    suspend fun toggleImportant(
        note: SongNoteEntity
    ) {
        dao.update(
            note.copy(
                isImportant =
                    !note.isImportant,

                updatedAt =
                    System.currentTimeMillis()
            )
        )
    }

    suspend fun delete(
        note: SongNoteEntity
    ): Int {
        return dao.deleteById(
            note.id
        )
    }

    suspend fun deleteCompleted():
        Int {

        return dao.deleteCompleted()
    }
}