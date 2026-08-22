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
    entities = [
        DownloadJobEntity::class,
        IndexedMediaEntity::class,
        MediaIndexStateEntity::class,
        IgnoredComparePairEntity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun downloadJobDao(): DownloadJobDao

    abstract fun mediaIndexDao(): MediaIndexDao

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

        /*
         * Version 4 connects a download job to an optional song note.
         * Existing rows remain valid because sourceNoteId is nullable.
         */
        private val migration3To4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `download_jobs` ADD COLUMN `sourceNoteId` INTEGER"
                )
            }
        }

        /*
         * Version 5 adds one shared media index used by:
         * - Settings library preparation
         * - Tag editor artist/album data
         * - Lyrics library suggestions
         * - Duplicate comparison
         *
         * Download history remains untouched.
         */
        private val migration4To5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `media_index` (
                        `uri` TEXT NOT NULL,
                        `source` TEXT NOT NULL,
                        `treeUri` TEXT,
                        `documentId` TEXT,
                        `displayName` TEXT NOT NULL,
                        `mimeType` TEXT NOT NULL,
                        `sizeBytes` INTEGER NOT NULL,
                        `lastModifiedMs` INTEGER NOT NULL,
                        `fileTitle` TEXT NOT NULL,
                        `fileArtist` TEXT NOT NULL,
                        `tagTitle` TEXT NOT NULL,
                        `tagArtist` TEXT NOT NULL,
                        `album` TEXT NOT NULL,
                        `albumArtist` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `artist` TEXT NOT NULL,
                        `canonicalFileName` TEXT NOT NULL,
                        `normalizedFileName` TEXT NOT NULL,
                        `normalizedTitle` TEXT NOT NULL,
                        `normalizedArtist` TEXT NOT NULL,
                        `normalizedAlbum` TEXT NOT NULL,
                        `titleTokens` TEXT NOT NULL,
                        `durationMs` INTEGER NOT NULL,
                        `bitrateKbps` INTEGER NOT NULL,
                        `coverPath` TEXT,
                        `contentSignature` TEXT NOT NULL,
                        `scanGeneration` INTEGER NOT NULL,
                        `indexedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`uri`)
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_media_index_source` " +
                        "ON `media_index` (`source`)"
                )

                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_media_index_source_canonicalFileName` " +
                        "ON `media_index` (`source`, `canonicalFileName`)"
                )

                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_media_index_source_title_artist` " +
                        "ON `media_index` (`source`, `normalizedTitle`, `normalizedArtist`)"
                )

                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_media_index_source_artist` " +
                        "ON `media_index` (`source`, `normalizedArtist`)"
                )

                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_media_index_source_album` " +
                        "ON `media_index` (`source`, `normalizedAlbum`)"
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `media_index_state` (
                        `source` TEXT NOT NULL,
                        `treeUri` TEXT,
                        `generation` INTEGER NOT NULL,
                        `totalFiles` INTEGER NOT NULL,
                        `indexedFiles` INTEGER NOT NULL,
                        `failedFiles` INTEGER NOT NULL,
                        `coverFiles` INTEGER NOT NULL,
                        `artistCount` INTEGER NOT NULL,
                        `albumCount` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`source`)
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `ignored_compare_pairs` (
                        `pairKey` TEXT NOT NULL,
                        `currentSignature` TEXT NOT NULL,
                        `referenceSignature` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`pairKey`)
                    )
                    """.trimIndent()
                )
            }
        }
        /*
         * Version 6 makes metadata indexing resilient to
         * malformed ID3 tags.
         *
         * Old UI-safe columns stay intact.
         *
         * New raw-tag columns are nullable so we can distinguish
         * a missing/broken tag from an intentionally empty value.
         */
        private val migration5To6 =
            object : Migration(5, 6) {

                override fun migrate(
                    db: SupportSQLiteDatabase
                ) {

                    db.execSQL(
                        "ALTER TABLE `media_index` " +
                            "ADD COLUMN `rawTagTitle` TEXT"
                    )

                    db.execSQL(
                        "ALTER TABLE `media_index` " +
                            "ADD COLUMN `rawTagArtist` TEXT"
                    )

                    db.execSQL(
                        "ALTER TABLE `media_index` " +
                            "ADD COLUMN `rawTagAlbum` TEXT"
                    )

                    db.execSQL(
                        "ALTER TABLE `media_index` " +
                            "ADD COLUMN `rawTagAlbumArtist` TEXT"
                    )

                    db.execSQL(
                        "ALTER TABLE `media_index` " +
                            "ADD COLUMN `metadataStatus` " +
                            "TEXT NOT NULL DEFAULT 'OK'"
                    )

                    db.execSQL(
                        "ALTER TABLE `media_index` " +
                            "ADD COLUMN `metadataErrorCode` TEXT"
                    )

                    db.execSQL(
                        "ALTER TABLE `media_index` " +
                            "ADD COLUMN `metadataErrorFields` TEXT"
                    )

                    db.execSQL(
                        "ALTER TABLE `media_index` " +
                            "ADD COLUMN `metadataErrorMessage` TEXT"
                    )

                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS " +
                            "`index_media_index_source_metadataStatus` " +
                            "ON `media_index` " +
                            "(`source`, `metadataStatus`)"
                    )

                    /*
                     * Force exactly one safe re-read after migration.
                     *
                     * The next normal Settings update will see these
                     * signatures as stale and re-index them using
                     * Media3 instead of the old text retriever.
                     */
                    db.execSQL(
                        "UPDATE `media_index` SET " +
                            "`contentSignature` = " +
                            "'V5|' || `contentSignature`, " +
                            "`metadataStatus` = 'MISSING'"
                    )
                }
            }

        /*
         * Version 7 adds Year to media_index.
         *
         * year stores the normalized 4-digit value used by the app.
         * rawTagYear keeps the source ID3 value when available.
         */
        private val migration6To7 =
            object : Migration(6, 7) {

                override fun migrate(
                    db: SupportSQLiteDatabase
                ) {

                    db.execSQL(
                        "ALTER TABLE `media_index` " +
                            "ADD COLUMN `year` TEXT NOT NULL DEFAULT ''"
                    )

                    db.execSQL(
                        "ALTER TABLE `media_index` " +
                            "ADD COLUMN `rawTagYear` TEXT"
                    )

                    /*
                     * Force one safe metadata reread.
                     *
                     * Otherwise the incremental index could treat old files
                     * as unchanged and never populate Year.
                     */
                    db.execSQL(
                        "UPDATE `media_index` SET " +
                            "`contentSignature` = " +
                            "'V7|' || `contentSignature`, " +
                            "`metadataStatus` = 'MISSING'"
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
                        migration2To3,
                        migration3To4,
                        migration4To5,
                        migration5To6,
                        migration6To7
                    )
                    .build()
                    .also {
                        instance = it
                    }
            }
        }
    }
}