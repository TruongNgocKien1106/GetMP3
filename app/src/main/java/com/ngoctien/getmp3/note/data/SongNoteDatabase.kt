package com.ngoctien.getmp3.note.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SongNoteEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SongNoteDatabase :
    RoomDatabase() {

    abstract fun songNoteDao():
        SongNoteDao

    companion object {
        @Volatile
        private var instance:
            SongNoteDatabase? = null

        fun getInstance(
            context: Context
        ): SongNoteDatabase {
            return instance
                ?: synchronized(this) {
                    instance
                        ?: Room.databaseBuilder(
                            context.applicationContext,
                            SongNoteDatabase::class.java,
                            "song_notes.db"
                        )
                            .build()
                            .also {
                                instance = it
                            }
                }
        }
    }
}