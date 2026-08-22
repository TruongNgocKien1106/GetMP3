package com.ngoctien.getmp3.tag

data class MediaSongFile(
    val id: Long,
    val uri: String,
    val displayName: String,
    val sizeBytes: Long,
    val dateModifiedSeconds: Long,

    /*
     * Khác null nếu file được đọc từ thư mục SAF
     * do người dùng tự chọn.
     */
    val treeUri: String? = null
)

data class EditableSong(
    val file: MediaSongFile,
    val title: String,
    val artist: String,
    val album: String,
    val coverPath: String?,
    val year: String = ""
)