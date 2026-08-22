package com.ngoctien.getmp3.tag

import android.content.Context
import com.ngoctien.getmp3.library.MediaIndexRepository
import com.ngoctien.getmp3.note.SongNameMatcher
import java.util.concurrent.ConcurrentHashMap

data class Mp3ListMetadata(
    val title: String,
    val artist: String,
    val album: String,
    val isAlbumLoaded: Boolean
)

internal object Mp3ListMetadataReader {

    private val cache =
        ConcurrentHashMap<
            String,
            Mp3ListMetadata
            >()

    fun cacheKey(
        file: MediaSongFile
    ): String {
        return buildString {
            append(file.uri)
            append('|')
            append(file.dateModifiedSeconds)
        }
    }

    fun fromFileName(
        file: MediaSongFile
    ): Mp3ListMetadata {
        val parsed =
            SongNameMatcher
                .parseFileName(
                    file.displayName
                )

        val stem =
            file.displayName
                .substringBeforeLast(
                    delimiter = ".",
                    missingDelimiterValue =
                        file.displayName
                )
                .trim()

        return Mp3ListMetadata(
            title =
                parsed
                    ?.title
                    ?.trim()
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: stem,

            artist =
                parsed
                    ?.artist
                    ?.trim()
                    .orEmpty(),

            album = "",

            isAlbumLoaded =
                false
        )
    }

    /**
     * Metadata list được lấy từ Media Index dùng chung.
     *
     * UI không mở lại MP3 để đọc ID3 mỗi lần render row.
     */
    suspend fun load(
        context: Context,
        file: MediaSongFile
    ): Mp3ListMetadata {

        val key =
            cacheKey(
                file
            )

        cache[key]
            ?.let {
                return it
            }

        val indexed =
            MediaIndexRepository(
                context.applicationContext
            )
                .getByUri(
                    file.uri
                )

        val result =
            if (
                indexed != null
            ) {

                Mp3ListMetadata(
                    title =
                        indexed.title,

                    artist =
                        indexed.artist,

                    album =
                        indexed.album,

                    isAlbumLoaded =
                        true
                )

            } else {

                fromFileName(
                    file
                )
            }

        cache[key] =
            result

        return result
    }

    fun invalidate(
        file: MediaSongFile
    ) {
        cache.remove(
            cacheKey(
                file
            )
        )
    }

    fun clear() {
        cache.clear()
    }
}
