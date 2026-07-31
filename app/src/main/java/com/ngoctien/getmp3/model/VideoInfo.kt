package com.ngoctien.getmp3.model

data class VideoInfo(
    val sourceUrl: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String?,
    val durationSeconds: Long
)