package com.ngoctien.getmp3.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface MediaIndexDao {

    @Query(
        "SELECT * FROM media_index WHERE source = :source ORDER BY displayName COLLATE NOCASE"
    )
    suspend fun getBySource(
        source: String
    ): List<IndexedMediaEntity>

    @Query(
        "SELECT * FROM media_index WHERE uri = :uri LIMIT 1"
    )
    suspend fun getByUri(
        uri: String
    ): IndexedMediaEntity?

    @Query(
        """
        SELECT *
        FROM media_index
        WHERE
            source = :source
            AND canonicalFileName = :canonicalFileName
        LIMIT 1
        """
    )
    suspend fun getByCanonicalFileName(
        source: String,
        canonicalFileName: String
    ): IndexedMediaEntity?

    /*
     * "Needs attention" query.
     *
     * The old method name is intentionally kept for API
     * compatibility with the repair ViewModel.
     *
     * Missing cover is NOT metadata corruption.
     * It is simply another reason for the UI to offer a
     * replacement download.
     */
    @Query(
        """
        SELECT *
        FROM media_index
        WHERE
            metadataStatus IN (
                'PARTIAL_ERROR',
                'BROKEN_METADATA',
                'UNREADABLE_FILE'
            )
            OR coverPath IS NULL
            OR TRIM(coverPath) = ''
            OR TRIM(tagTitle) = ''
            OR TRIM(tagArtist) = ''
            OR TRIM(album) = ''
            OR TRIM(year) = ''
            OR (
                metadataErrorFields IS NOT NULL
                AND TRIM(metadataErrorFields) <> ''
            )
        ORDER BY
            CASE source
                WHEN 'REFERENCE' THEN 0
                WHEN 'DOWNLOAD' THEN 1
                ELSE 2
            END,
            displayName COLLATE NOCASE
        """
    )
    suspend fun getMetadataErrors():
        List<IndexedMediaEntity>

    @Query(
        """
        SELECT COUNT(*)
        FROM media_index
        WHERE source = :source
        AND metadataStatus IN (
            'PARTIAL_ERROR',
            'BROKEN_METADATA',
            'UNREADABLE_FILE'
        )
        """
    )
    suspend fun countMetadataErrors(
        source: String
    ): Int
    @Query(
        "SELECT DISTINCT artist FROM media_index " +
            "WHERE source = :source AND artist <> '' " +
            "ORDER BY artist COLLATE NOCASE"
    )
    suspend fun getDistinctArtists(
        source: String
    ): List<String>

    @Query(
        "SELECT DISTINCT album FROM media_index " +
            "WHERE source = :source AND album <> '' " +
            "ORDER BY album COLLATE NOCASE"
    )
    suspend fun getDistinctAlbums(
        source: String
    ): List<String>

    @Query(
        "SELECT COUNT(*) FROM media_index WHERE source = :source"
    )
    suspend fun countBySource(
        source: String
    ): Int

    @Query(
        "SELECT COUNT(*) FROM media_index " +
            "WHERE source = :source AND coverPath IS NOT NULL AND coverPath <> ''"
    )
    suspend fun countWithCover(
        source: String
    ): Int

    @Upsert
    suspend fun upsertAll(
        items: List<IndexedMediaEntity>
    )

    @Upsert
    suspend fun upsert(
        item: IndexedMediaEntity
    )

    @Query(
        "DELETE FROM media_index " +
            "WHERE source = :source AND scanGeneration <> :generation"
    )
    suspend fun deleteOlderGeneration(
        source: String,
        generation: Long
    )

    @Query(
        "DELETE FROM media_index WHERE source = :source"
    )
    suspend fun deleteSource(
        source: String
    )

    @Query(
        "DELETE FROM media_index WHERE uri = :uri"
    )
    suspend fun deleteUri(
        uri: String
    )

    @Upsert
    suspend fun upsertState(
        state: MediaIndexStateEntity
    )

    @Query(
        "SELECT * FROM media_index_state WHERE source = :source LIMIT 1"
    )
    suspend fun getState(
        source: String
    ): MediaIndexStateEntity?

    @Query(
        "DELETE FROM media_index_state WHERE source = :source"
    )
    suspend fun deleteState(
        source: String
    )

    @Upsert
    suspend fun upsertIgnoredPair(
        pair: IgnoredComparePairEntity
    )

    @Query(
        "SELECT pairKey FROM ignored_compare_pairs"
    )
    suspend fun getIgnoredPairKeys(): List<String>

    @Query(
        "DELETE FROM ignored_compare_pairs WHERE pairKey = :pairKey"
    )
    suspend fun deleteIgnoredPair(
        pairKey: String
    )
}
