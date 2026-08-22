package com.ngoctien.getmp3.library

import com.ngoctien.getmp3.data.IndexedMediaEntity
import com.ngoctien.getmp3.data.MediaIndexSource
import com.ngoctien.getmp3.data.MediaMetadataStatus

data class LibraryAdmissionResult(
    val allowed: Boolean,
    val reasons: List<String>
) {

    val message: String
        get() =
            reasons.joinToString(
                separator = " • "
            )
}

object LibraryAdmissionPolicy {

    private val yearPattern =
        Regex("""\d{4}""")

    fun evaluate(
        item: IndexedMediaEntity
    ): LibraryAdmissionResult {

        val reasons =
            buildList {

                if (
                    item.source !=
                    MediaIndexSource.INBOX
                ) {
                    add(
                        "File không nằm trong Inbox"
                    )
                }

                if (
                    !item.displayName
                        .endsWith(
                            ".mp3",
                            ignoreCase = true
                        )
                ) {
                    add(
                        "File không phải MP3"
                    )
                }

                if (
                    MediaMetadataStatus
                        .isError(
                            item.metadataStatus
                        )
                ) {
                    add(
                        "Metadata chưa đọc được"
                    )
                }

                if (
                    item.tagTitle
                        .isBlank()
                ) {
                    add(
                        "Thiếu Title"
                    )
                }

                if (
                    item.tagArtist
                        .isBlank()
                ) {
                    add(
                        "Thiếu Artist"
                    )
                }

                if (
                    item.album
                        .isBlank()
                ) {
                    add(
                        "Thiếu Album"
                    )
                }

                if (
                    !yearPattern.matches(
                        item.year.trim()
                    )
                ) {
                    add(
                        "Year phải có 4 chữ số"
                    )
                }

                if (
                    item.coverPath
                        .isNullOrBlank()
                ) {
                    add(
                        "Thiếu cover"
                    )
                }

                if (
                    item.sizeBytes <=
                    0L
                ) {
                    add(
                        "File rỗng"
                    )
                }
            }

        return LibraryAdmissionResult(
            allowed =
                reasons.isEmpty(),

            reasons =
                reasons
        )
    }

    fun requireReady(
        item: IndexedMediaEntity
    ) {

        val result =
            evaluate(
                item
            )

        if (!result.allowed) {
            throw IllegalStateException(
                "Chưa thể đưa vào Library: " +
                    result.message
            )
        }
    }
}
