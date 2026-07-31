package com.ngoctien.getmp3.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadJobDao {

    @Query(
        """
        SELECT *
        FROM download_jobs
        ORDER BY createdAt DESC
        """
    )
    fun observeAllJobs(): Flow<List<DownloadJobEntity>>

    @Query(
        """
        SELECT *
        FROM download_jobs
        ORDER BY createdAt DESC
        """
    )
    suspend fun getAllJobsOnce(): List<DownloadJobEntity>

    @Query(
        """
        SELECT *
        FROM download_jobs
        WHERE id = :id
        LIMIT 1
        """
    )
    suspend fun getJobById(id: String): DownloadJobEntity?

    @Query(
        """
        SELECT *
        FROM download_jobs
        WHERE status = 'QUEUED'
        ORDER BY createdAt ASC
        LIMIT 1
        """
    )
    suspend fun getNextQueuedJob(): DownloadJobEntity?

    @Query(
        """
        SELECT COUNT(*)
        FROM download_jobs
        WHERE url = :url
          AND status IN (
              'QUEUED',
              'EXTRACTING',
              'DOWNLOADING',
              'CONVERTING',
              'TAGGING',
              'SAVING'
          )
        """
    )
    suspend fun countActiveJobsByUrl(url: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: DownloadJobEntity): Long
    @Update
    suspend fun updateJob(job: DownloadJobEntity): Int

    @Query(
        """
        UPDATE download_jobs
        SET status = 'QUEUED',
            stageProgress = 0,
            overallProgress = 0,
            downloadedBytes = 0,
            totalBytes = 0,
            speedBytesPerSecond = 0,
            etaSeconds = 0,
            processedSeconds = 0,
            ffmpegSpeed = 0,
            stageStartedAt = :now,
            lastProgressAt = :now,
            statusMessage = 'Đang chờ chạy lại',
            warningMessage = 'Tác vụ trước bị gián đoạn và sẽ được tải lại',
            errorMessage = NULL,
            outputUri = NULL,
            updatedAt = :now
        WHERE status IN (
            'EXTRACTING',
            'DOWNLOADING',
            'CONVERTING',
            'TAGGING',
            'SAVING'
        )
        """
    )
    suspend fun requeueInterruptedJobs(now: Long): Int

    @Query(
        """
        UPDATE download_jobs
        SET status = 'CANCELLED',
            statusMessage = 'Đã hủy',
            errorMessage = NULL,
            updatedAt = :now
        WHERE id = :id
          AND status = 'QUEUED'
        """
    )
    suspend fun cancelQueuedJob(
        id: String,
        now: Long
    ): Int

    @Query(
        """
        DELETE FROM download_jobs
        WHERE status IN ('COMPLETED', 'FAILED', 'CANCELLED')
        """
    )
    suspend fun deleteFinishedJobs(): Int

    @Query(
        """
        DELETE FROM download_jobs
        WHERE id = :id
        """
    )
    suspend fun deleteJob(id: String): Int
    @Query(
        """
        UPDATE download_jobs
        SET outputUri = :newUri,
            title = :title,
            artist = :artist,
            updatedAt = :now
        WHERE outputUri = :oldUri
        """
    )
    suspend fun updateOutputReference(
        oldUri: String,
        newUri: String,
        title: String,
        artist: String,
        now: Long
    ): Int
    @Query(
        """
        DELETE FROM download_jobs
        WHERE outputUri = :outputUri
        """
    )
    suspend fun deleteByOutputUri(
        outputUri: String
    ): Int
}