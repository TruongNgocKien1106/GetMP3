package com.ngoctien.getmp3.note.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "song_notes",
    indices = [
        Index(value = ["normalizedTitle"]),
        Index(value = ["isCompleted"]),
        Index(value = ["sortOrder"])
    ]
)
data class SongNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val rawText: String,

    val title: String,

    val artist: String,

    val normalizedTitle: String,

    val youtubeUrl: String? = null,

    val isImportant: Boolean = false,

    /*
     * isCompleted có nghĩa là người dùng đã tải xong
     * và đánh dấu note này là hoàn tất.
     */
    val isCompleted: Boolean = false,

    val sortOrder: Long,

    val createdAt: Long,

    val updatedAt: Long,

    val completedAt: Long? = null
)

data class SongNoteDraft(
    val rawText: String,
    val title: String,
    val artist: String,
    val normalizedTitle: String,
    val youtubeUrl: String?
)

enum class DuplicateSource {
    NOTE_LIST,
    COMPARE_FOLDER
}

data class DuplicateSongMatch(
    val key: String,
    val title: String,
    val artist: String,
    val source: DuplicateSource,
    val score: Double
)