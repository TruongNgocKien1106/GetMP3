package com.ngoctien.getmp3.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ngoctien.getmp3.model.DownloadStatus

class Converters {

    @TypeConverter
    fun fromStatus(status: DownloadStatus): String {
        return status.name
    }

    @TypeConverter
    fun toStatus(name: String): DownloadStatus {
        return runCatching {
            DownloadStatus.valueOf(name)
        }.getOrDefault(DownloadStatus.FAILED)
    }
}

@Database(
    entities = [DownloadJobEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun downloadJobDao(): DownloadJobDao

    companion object {

        @Volatile
        private var instance: AppDatabase? = null

        /*
         * Giữ migration cũ để người đang ở database version 1
         * vẫn có thể đi tiếp lên version 2.
         */
        private val migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `download_jobs`")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `download_jobs` (
                        `id` TEXT NOT NULL,
                        `url` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `artist` TEXT NOT NULL,
                        `thumbnailUrl` TEXT,
                        `localCoverPath` TEXT,
                        `status` TEXT NOT NULL,
                        `stageProgress` INTEGER NOT NULL,
                        `overallProgress` INTEGER NOT NULL,
                        `downloadedBytes` INTEGER NOT NULL,
                        `totalBytes` INTEGER NOT NULL,
                        `speedBytesPerSecond` INTEGER NOT NULL,
                        `etaSeconds` INTEGER NOT NULL,
                        `durationSeconds` INTEGER NOT NULL,
                        `processedSeconds` INTEGER NOT NULL,
                        `ffmpegSpeed` REAL NOT NULL,
                        `stageStartedAt` INTEGER NOT NULL,
                        `lastProgressAt` INTEGER NOT NULL,
                        `statusMessage` TEXT,
                        `warningMessage` TEXT,
                        `errorMessage` TEXT,
                        `destinationMode` INTEGER NOT NULL,
                        `destinationTreeUri` TEXT,
                        `destinationDisplayName` TEXT,
                        `outputUri` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        /*
         * Version 3 bỏ:
         * - localCoverPath
         * - destinationMode
         * - destinationTreeUri
         * - destinationDisplayName
         *
         * File luôn lưu cố định bằng MediaStore tại Music/GetMP3.
         */
        private val migration2To3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `download_jobs_new` (
                        `id` TEXT NOT NULL,
                        `url` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `artist` TEXT NOT NULL,
                        `thumbnailUrl` TEXT,
                        `status` TEXT NOT NULL,
                        `stageProgress` INTEGER NOT NULL,
                        `overallProgress` INTEGER NOT NULL,
                        `downloadedBytes` INTEGER NOT NULL,
                        `totalBytes` INTEGER NOT NULL,
                        `speedBytesPerSecond` INTEGER NOT NULL,
                        `etaSeconds` INTEGER NOT NULL,
                        `durationSeconds` INTEGER NOT NULL,
                        `processedSeconds` INTEGER NOT NULL,
                        `ffmpegSpeed` REAL NOT NULL,
                        `stageStartedAt` INTEGER NOT NULL,
                        `lastProgressAt` INTEGER NOT NULL,
                        `statusMessage` TEXT,
                        `warningMessage` TEXT,
                        `errorMessage` TEXT,
                        `outputUri` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    INSERT INTO `download_jobs_new` (
                        id,
                        url,
                        title,
                        artist,
                        thumbnailUrl,
                        status,
                        stageProgress,
                        overallProgress,
                        downloadedBytes,
                        totalBytes,
                        speedBytesPerSecond,
                        etaSeconds,
                        durationSeconds,
                        processedSeconds,
                        ffmpegSpeed,
                        stageStartedAt,
                        lastProgressAt,
                        statusMessage,
                        warningMessage,
                        errorMessage,
                        outputUri,
                        createdAt,
                        updatedAt
                    )
                    SELECT
                        id,
                        url,
                        title,
                        artist,
                        thumbnailUrl,
                        status,
                        stageProgress,
                        overallProgress,
                        downloadedBytes,
                        totalBytes,
                        speedBytesPerSecond,
                        etaSeconds,
                        durationSeconds,
                        processedSeconds,
                        ffmpegSpeed,
                        stageStartedAt,
                        lastProgressAt,
                        statusMessage,
                        warningMessage,
                        errorMessage,
                        outputUri,
                        createdAt,
                        updatedAt
                    FROM `download_jobs`
                    """.trimIndent()
                )

                db.execSQL("DROP TABLE `download_jobs`")
                db.execSQL(
                    "ALTER TABLE `download_jobs_new` RENAME TO `download_jobs`"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "getmp3_database"
                )
                    .addMigrations(
                        migration1To2,
                        migration2To3
                    )
                    .build()
                    .also {
                        instance = it
                    }
            }
        }
    }
}