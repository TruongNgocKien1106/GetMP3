package com.ngoctien.getmp3.model

enum class DownloadStatus {
    QUEUED,
    EXTRACTING,
    DOWNLOADING,
    CONVERTING,
    TAGGING,
    SAVING,
    COMPLETED,
    FAILED,
    CANCELLED
}
