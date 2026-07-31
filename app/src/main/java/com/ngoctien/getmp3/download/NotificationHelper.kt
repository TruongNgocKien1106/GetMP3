package com.ngoctien.getmp3.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ngoctien.getmp3.MainActivity
import com.ngoctien.getmp3.R
import com.ngoctien.getmp3.data.DownloadJobEntity
import com.ngoctien.getmp3.model.DownloadStatus

class NotificationHelper(
    private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "getmp3_downloads"
        const val FOREGROUND_NOTIFICATION_ID = 1001
    }

    private val manager =
        context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

    init {
        createChannel()
    }

    fun buildPreparingNotification(): Notification {
        return baseBuilder()
            .setContentTitle("GetMP3")
            .setContentText("Đang kiểm tra hàng đợi...")
            .setOngoing(true)
            .setProgress(0, 0, true)
            .build()
    }

    fun updateProgress(
        job: DownloadJobEntity
    ) {
        manager.notify(
            FOREGROUND_NOTIFICATION_ID,
            buildProgressNotification(job)
        )
    }

    fun showTerminal(
        job: DownloadJobEntity
    ) {
        val title: String
        val content: String

        when (job.status) {
            DownloadStatus.COMPLETED -> {
                title = "Tải MP3 hoàn tất"
                content = job.title
            }

            DownloadStatus.CANCELLED -> {
                title = "Đã hủy tải MP3"
                content = job.title
            }

            else -> {
                title = "Tải MP3 thất bại"
                content = job.errorMessage
                    ?: job.title
            }
        }

        val notification = baseBuilder()
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(content)
            )
            .setOngoing(false)
            .setAutoCancel(true)
            .setProgress(0, 0, false)
            .build()

        val terminalId =
            2000 + (job.id.hashCode() and 0x0FFF)

        manager.notify(
            terminalId,
            notification
        )
    }

    fun cancelForegroundNotification() {
        manager.cancel(
            FOREGROUND_NOTIFICATION_ID
        )
    }

    private fun buildProgressNotification(
        job: DownloadJobEntity
    ): Notification {
        val message = job.statusMessage
            ?: statusLabel(job.status)

        val indeterminate =
            job.status == DownloadStatus.QUEUED ||
                job.status == DownloadStatus.EXTRACTING ||
                (
                    job.status ==
                        DownloadStatus.DOWNLOADING &&
                        job.totalBytes <= 0L
                    )

        val builder = baseBuilder()
            .setContentTitle(job.title)
            .setContentText(message)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(
                R.drawable.ic_notification,
                "Hủy",
                cancelPendingIntent(job.id)
            )

        if (indeterminate) {
            builder.setProgress(
                0,
                0,
                true
            )
        } else {
            builder.setProgress(
                100,
                job.overallProgress.coerceIn(0, 100),
                false
            )
        }

        return builder.build()
    }

    private fun baseBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openAppPendingIntent())
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSilent(true)
            .setForegroundServiceBehavior(
                NotificationCompat
                    .FOREGROUND_SERVICE_IMMEDIATE
            )
    }

    private fun openAppPendingIntent(): PendingIntent {
        val intent = Intent(
            context,
            MainActivity::class.java
        ).apply {
            flags =
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun cancelPendingIntent(
        jobId: String
    ): PendingIntent {
        val intent = Intent(
            context,
            DownloadService::class.java
        ).apply {
            action =
                DownloadService.ACTION_CANCEL_JOB

            putExtra(
                DownloadService.EXTRA_JOB_ID,
                jobId
            )
        }

        return PendingIntent.getService(
            context,
            jobId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createChannel() {
        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.O
        ) {
            return
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Tải MP3",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description =
                "Tiến trình tải và chuyển đổi MP3"
            setShowBadge(false)
        }

        manager.createNotificationChannel(channel)
    }

    private fun statusLabel(
        status: DownloadStatus
    ): String {
        return when (status) {
            DownloadStatus.QUEUED ->
                "Đang chờ"

            DownloadStatus.EXTRACTING ->
                "Đang lấy thông tin"

            DownloadStatus.DOWNLOADING ->
                "Đang tải âm thanh"

            DownloadStatus.CONVERTING ->
                "Đang chuyển đổi MP3"

            DownloadStatus.TAGGING ->
                "Đang ghi ID3v2.3"

            DownloadStatus.SAVING ->
                "Đang lưu file"

            DownloadStatus.COMPLETED ->
                "Hoàn tất"

            DownloadStatus.FAILED ->
                "Thất bại"

            DownloadStatus.CANCELLED ->
                "Đã hủy"
        }
    }
}