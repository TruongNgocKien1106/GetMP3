package com.ngoctien.getmp3.library

data class MediaIndexProgress(
    val totalFiles: Int = 0,
    val processedFiles: Int = 0,
    val newFiles: Int = 0,
    val changedFiles: Int = 0,
    val skippedFiles: Int = 0,
    val failedFiles: Int = 0,
    val currentFileName: String = ""
) {
    val progressFraction: Float
        get() =
            if (totalFiles <= 0) {
                0f
            } else {
                processedFiles
                    .toFloat()
                    .div(totalFiles.toFloat())
                    .coerceIn(0f, 1f)
            }
}

data class MediaIndexSummary(
    val totalFiles: Int,
    val failedFiles: Int,
    val coverFiles: Int,
    val artistCount: Int,
    val albumCount: Int,
    val updatedAt: Long,
    val treeUri: String?
)

data class MediaMatchCandidate(
    val currentUri: String,
    val referenceUri: String,
    val score: Double,
    val exactFileName: Boolean
)
