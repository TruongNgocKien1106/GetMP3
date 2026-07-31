package com.ngoctien.getmp3.data

import kotlinx.coroutines.flow.Flow

class DownloadRepository(
    private val dao: DownloadJobDao
) {
    fun observeAllJobs(): Flow<List<DownloadJobEntity>> {
        return dao.observeAllJobs()
    }

    suspend fun getAllJobsOnce(): List<DownloadJobEntity> {
        return dao.getAllJobsOnce()
    }

    suspend fun getJobById(id: String): DownloadJobEntity? {
        return dao.getJobById(id)
    }

    suspend fun getNextQueuedJob(): DownloadJobEntity? {
        return dao.getNextQueuedJob()
    }

    suspend fun hasActiveJobForUrl(url: String): Boolean {
        return dao.countActiveJobsByUrl(url) > 0
    }

    suspend fun insertJob(job: DownloadJobEntity) {
        dao.insertJob(job)
    }

    suspend fun updateJob(job: DownloadJobEntity) {
        dao.updateJob(job)
    }

    suspend fun requeueInterruptedJobs(): Int {
        return dao.requeueInterruptedJobs(System.currentTimeMillis())
    }

    suspend fun cancelQueuedJob(id: String): Boolean {
        return dao.cancelQueuedJob(
            id = id,
            now = System.currentTimeMillis()
        ) > 0
    }

    suspend fun deleteFinishedJobs() {
        dao.deleteFinishedJobs()
    }

    suspend fun deleteJob(id: String) {
        dao.deleteJob(id)
    }
    suspend fun updateOutputReference(
        oldUri: String,
        newUri: String,
        title: String,
        artist: String
    ) {
        dao.updateOutputReference(
            oldUri = oldUri,
            newUri = newUri,
            title = title,
            artist = artist,
            now = System.currentTimeMillis()
        )
    }
    suspend fun deleteByOutputUri(
        outputUri: String
    ): Int {
        return dao.deleteByOutputUri(
            outputUri
        )
    }
}