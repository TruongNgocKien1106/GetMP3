package com.ngoctien.getmp3.compare

import com.ngoctien.getmp3.tag.MediaSongFile

enum class CompareMatchKind {
    EXACT,
    NEAR
}

enum class CompareSide {
    CURRENT,
    REFERENCE
}

data class CompareFile(
    val id: Long,
    val uri: String,
    val treeUri: String?,
    val displayName: String,
    val title: String,
    val artist: String,
    val album: String,
    val year: String,
    val coverPath: String?,
    val sizeBytes: Long,
    val dateModifiedSeconds: Long,
    val durationSeconds: Long?,
    val bitrateKbps: Int?,
    val contentSignature: String
) {
    fun toMediaSongFile(): MediaSongFile {
        return MediaSongFile(
            id = id,
            uri = uri,
            displayName = displayName,
            sizeBytes = sizeBytes,
            dateModifiedSeconds = dateModifiedSeconds,
            treeUri = treeUri
        )
    }
}

data class ComparePair(
    val current: CompareFile,
    val reference: CompareFile,
    val kind: CompareMatchKind,
    val score: Double,
    val ignoreKey: String
) {
    val id: String
        get() = ignoreKey
}

data class CompareScanResult(
    val exactPairs: List<ComparePair>,
    val nearPairs: List<ComparePair>,
    val ignoredPairCount: Int
)

data class CompareActionResult(
    val message: String,
    val warning: String? = null
)
