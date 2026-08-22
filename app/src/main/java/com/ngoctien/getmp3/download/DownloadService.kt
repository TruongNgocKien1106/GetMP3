package com.ngoctien.getmp3.download

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.ngoctien.getmp3.data.AppDatabase
import com.ngoctien.getmp3.data.DownloadRepository
import com.ngoctien.getmp3.model.DownloadStatus
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class DownloadService : Service() {
    companion object {
        private const val TAG = "DownloadService"

        const val ACTION_CANCEL_JOB =
            "com.ngoctien.getmp3.action.CANCEL_JOB"

        const val EXTRA_JOB_ID =
            "extra_job_id"
    }

    private val serviceJob = SupervisorJob()

    private val exceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            Log.e(
                TAG,
                "Service coroutine failed",
                throwable
            )
        }

    private val serviceScope =
        CoroutineScope(
            Dispatchers.IO +
                serviceJob +
                exceptionHandler
        )

    private val processing =
        AtomicBoolean(false)

    private val recoveryCompleted =
        AtomicBoolean(false)

    private lateinit var repository:
        DownloadRepository


    private lateinit var notificationHelper:
        NotificationHelper

    private lateinit var coordinator:
        DownloadCoordinator

    private var wakeLock:
        PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()

        repository = DownloadRepository(
            AppDatabase
                .getDatabase(this)
                .downloadJobDao()
        )


        notificationHelper =
            NotificationHelper(this)

        coordinator = DownloadCoordinator(
            context = this,
            repository = repository,
            onJobUpdated = { job ->
                if (
                    job.status !in setOf(
                        DownloadStatus.COMPLETED,
                        DownloadStatus.FAILED,
                        DownloadStatus.CANCELLED
                    )
                ) {
                    notificationHelper.updateProgress(
                        job
                    )
                }
            }
        )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        startForeground(
            NotificationHelper
                .FOREGROUND_NOTIFICATION_ID,
            notificationHelper
                .buildPreparingNotification()
        )

        when (intent?.action) {
            ACTION_CANCEL_JOB -> {
                val jobId = intent.getStringExtra(
                    EXTRA_JOB_ID
                )

                if (!jobId.isNullOrBlank()) {
                    serviceScope.launch {
                        val cancelledBeforeStart =
                            repository.cancelQueuedJob(
                                jobId
                            )

                        if (!cancelledBeforeStart) {
                            coordinator.cancelJob(jobId)
                        }

                        repository.getJobById(jobId)
                            ?.takeIf {
                                it.status ==
                                    DownloadStatus.CANCELLED
                            }
                            ?.let {
                                notificationHelper
                                    .showTerminal(it)
                            }

                        ensureQueueProcessor()
                    }
                } else {
                    ensureQueueProcessor()
                }
            }

            else -> {
                ensureQueueProcessor()
            }
        }

        return START_STICKY
    }

    private fun ensureQueueProcessor() {
        if (
            !processing.compareAndSet(
                false,
                true
            )
        ) {
            return
        }

        serviceScope.launch {
            var hasMoreJobs = false

            try {
                if (
                    recoveryCompleted.compareAndSet(
                        false,
                        true
                    )
                ) {
                    repository.requeueInterruptedJobs()
                }

                acquireWakeLock()

                while (true) {
                    val nextJob =
                        repository.getNextQueuedJob()
                            ?: break

                    val finalJob = try {
                        coordinator.processJob(
                            nextJob.id
                        )
                    } catch (exception: Exception) {
                        Log.e(
                            TAG,
                            "Unexpected coordinator error",
                            exception
                        )

                        val failedJob =
                            repository.getJobById(
                                nextJob.id
                            )?.copy(
                                status =
                                    DownloadStatus.FAILED,
                                statusMessage =
                                    "Thất bại",
                                errorMessage =
                                    exception.message
                                        ?: "Lỗi service",
                                updatedAt =
                                    System.currentTimeMillis()
                            )

                        if (failedJob != null) {
                            repository.updateJob(
                                failedJob
                            )
                        }

                        failedJob
                    }

                    if (
                        finalJob != null &&
                        finalJob.status in setOf(
                            DownloadStatus.COMPLETED,
                            DownloadStatus.FAILED,
                            DownloadStatus.CANCELLED
                        )
                    ) {
                        notificationHelper.showTerminal(
                            finalJob
                        )
                    }
                }
            } finally {
                releaseWakeLock()
                processing.set(false)

                hasMoreJobs =
                    repository.getNextQueuedJob() != null

                if (hasMoreJobs) {
                    ensureQueueProcessor()
                } else {
                    stopForeground(
                        STOP_FOREGROUND_REMOVE
                    )

                    notificationHelper
                        .cancelForegroundNotification()

                    stopSelf()
                }
            }
        }
    }

    private fun acquireWakeLock() {
        val existing = wakeLock

        if (
            existing != null &&
            existing.isHeld
        ) {
            return
        }

        val powerManager =
            getSystemService(
                POWER_SERVICE
            ) as PowerManager

        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "$packageName:GetMp3Download"
        ).apply {
            setReferenceCounted(false)

            // Tự nhả sau 6 giờ nếu có lỗi bất thường.
            acquire(6 * 60 * 60 * 1000L)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { lock ->
            if (lock.isHeld) {
                runCatching {
                    lock.release()
                }
            }
        }

        wakeLock = null
    }

    override fun onDestroy() {
        coordinator.shutdown()
        releaseWakeLock()
        serviceScope.cancel()

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {
        return null
    }
}