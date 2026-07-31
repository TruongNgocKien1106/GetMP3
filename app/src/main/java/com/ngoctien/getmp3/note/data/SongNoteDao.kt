package com.ngoctien.getmp3.note.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SongNoteDao {

    @Query(
        """
        SELECT *
        FROM song_notes
        ORDER BY
            isCompleted ASC,
            isImportant DESC,
            sortOrder DESC,
            createdAt DESC
        """
    )
    fun observeAll(): Flow<List<SongNoteEntity>>

    @Query(
        """
        SELECT *
        FROM song_notes
        """
    )
    suspend fun getAllOnce(): List<SongNoteEntity>

    @Insert
    suspend fun insert(
        note: SongNoteEntity
    ): Long

    @Update
    suspend fun update(
        note: SongNoteEntity
    )

    @Query(
        """
        DELETE FROM song_notes
        WHERE id = :id
        """
    )
    suspend fun deleteById(
        id: Long
    ): Int

    @Query(
        """
        DELETE FROM song_notes
        WHERE isCompleted = 1
        """
    )
    suspend fun deleteCompleted(): Int
}