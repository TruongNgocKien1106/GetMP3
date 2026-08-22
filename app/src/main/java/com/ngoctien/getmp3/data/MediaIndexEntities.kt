package com.ngoctien.getmp3.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

object MediaIndexSource {

    /*
     * Persisted values.
     *
     * Không đổi hai string này để Room database cũ
     * tiếp tục tương thích.
     */
    const val REFERENCE =
        "REFERENCE"

    const val DOWNLOAD =
        "DOWNLOAD"

    /*
     * Domain naming mới.
     *
     * Library / Inbox dùng trong code mới,
     * nhưng vẫn map tới persisted values cũ.
     */
    const val LIBRARY =
        REFERENCE

    const val INBOX =
        DOWNLOAD
}

object MediaMetadataStatus {
    const val OK = "OK"
    const val MISSING = "MISSING"
    const val PARTIAL_ERROR = "PARTIAL_ERROR"
    const val BROKEN_METADATA = "BROKEN_METADATA"
    const val UNREADABLE_FILE = "UNREADABLE_FILE"

    fun isError(
        value: String
    ): Boolean {
        return value == PARTIAL_ERROR ||
            value == BROKEN_METADATA ||
            value == UNREADABLE_FILE
    }
}

@Entity(
    tableName = "media_index",
    indices = [
        Index(
            value = ["source"],
            name = "index_media_index_source"
        ),
        Index(
            value = ["source", "canonicalFileName"],
            name = "index_media_index_source_canonicalFileName"
        ),
        Index(
            value = [
                "source",
                "normalizedTitle",
                "normalizedArtist"
            ],
            name = "index_media_index_source_title_artist"
        ),
        Index(
            value = [
                "source",
                "normalizedArtist"
            ],
            name = "index_media_index_source_artist"
        ),
        Index(
            value = [
                "source",
                "normalizedAlbum"
            ],
            name = "index_media_index_source_album"
        ),
        Index(
            value = [
                "source",
                "metadataStatus"
            ],
            name = "index_media_index_source_metadataStatus"
        )
    ]
)
data class IndexedMediaEntity(
    @PrimaryKey
    val uri: String,

    val source: String,
    val treeUri: String?,
    val documentId: String?,

    val displayName: String,
    val mimeType: String,

    val sizeBytes: Long,
    val lastModifiedMs: Long,

    val fileTitle: String,
    val fileArtist: String,

    /*
     * Backward-compatible values used by the current UI.
     *
     * They stay non-null so existing screens do not need to
     * become nullable everywhere at once.
     */
    val tagTitle: String,
    val tagArtist: String,
    val album: String,
    val albumArtist: String,

    @ColumnInfo(defaultValue = "''")
    val year: String = "",

    /*
     * Raw ID3 values.
     *
     * Missing or undecodable values remain NULL.
     * These fields will later power the metadata repair tab.
     */
    val rawTagTitle: String? = null,
    val rawTagArtist: String? = null,
    val rawTagAlbum: String? = null,
    val rawTagAlbumArtist: String? = null,
    val rawTagYear: String? = null,

    @ColumnInfo(defaultValue = "'OK'")
    val metadataStatus: String =
        MediaMetadataStatus.OK,

    val metadataErrorCode: String? =
        null,

    val metadataErrorFields: String? =
        null,

    val metadataErrorMessage: String? =
        null,

    /*
     * Resolved app-facing metadata.
     *
     * Filename remains the preferred source for Title/Artist.
     */
    val title: String,
    val artist: String,

    val canonicalFileName: String,
    val normalizedFileName: String,
    val normalizedTitle: String,
    val normalizedArtist: String,
    val normalizedAlbum: String,
    val titleTokens: String,

    val durationMs: Long,
    val bitrateKbps: Int,

    val coverPath: String?,

    val contentSignature: String,
    val scanGeneration: Long,
    val indexedAt: Long
)

@Entity(
    tableName = "media_index_state"
)
data class MediaIndexStateEntity(
    @PrimaryKey
    val source: String,

    val treeUri: String?,
    val generation: Long,

    val totalFiles: Int,
    val indexedFiles: Int,
    val failedFiles: Int,
    val coverFiles: Int,
    val artistCount: Int,
    val albumCount: Int,

    val updatedAt: Long
)

@Entity(
    tableName = "ignored_compare_pairs"
)
data class IgnoredComparePairEntity(
    @PrimaryKey
    val pairKey: String,

    val currentSignature: String,
    val referenceSignature: String,
    val createdAt: Long
)