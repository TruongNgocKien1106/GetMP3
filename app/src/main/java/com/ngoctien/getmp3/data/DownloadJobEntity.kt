package com.ngoctien.getmp3.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ngoctien.getmp3.model.DownloadStatus

@Entity(tableName = "download_jobs")
data class DownloadJobEntity(
    @PrimaryKey
    val id: String,

    val url: String,

    val title: String,
    val artist: String,
    val thumbnailUrl: String?,

    val status: DownloadStatus,

    val stageProgress: Int,
    val overallProgress: Int,

    val downloadedBytes: Long,
    val totalBytes: Long,
    val speedBytesPerSecond: Long,
    val etaSeconds: Long,

    val durationSeconds: Long,
    val processedSeconds: Long,
    val ffmpegSpeed: Float,

    val stageStartedAt: Long,
    val lastProgressAt: Long,

    val statusMessage: String?,
    val warningMessage: String?,
    val errorMessage: String?,

    val outputUri: String?,

    val createdAt: Long,
    val updatedAt: Long
)