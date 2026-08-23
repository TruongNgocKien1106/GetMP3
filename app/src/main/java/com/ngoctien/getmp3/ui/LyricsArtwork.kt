package com.ngoctien.getmp3.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.ngoctien.getmp3.lyrics.LyricsSearchResult
import com.ngoctien.getmp3.ui.components.AppMediaCover
import com.ngoctien.getmp3.youtube.YouTubeSearchRepository
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap


private object LyricsArtworkResolver {
    private val cache =
        ConcurrentHashMap<String, String>()

    suspend fun resolve(
        context: Context,
        result: LyricsSearchResult
    ): String? {
        val key =
            result.trackName
                .trim()
                .lowercase(
                    Locale.ROOT
                )

        cache[key]
            ?.let { cached ->
                return cached
                    .takeIf(
                        String::isNotBlank
                    )
            }

        val query =
            listOf(
                result.trackName,
                result.artistName
            )
                .map(String::trim)
                .filter(String::isNotBlank)
                .joinToString(" ")

        if (query.isBlank()) {
            cache[key] = ""

            return null
        }

        val artworkUrl =
            runCatching {
                YouTubeSearchRepository(
                    context.applicationContext
                )
                    .search(
                        query = query,
                        limit = 1
                    )
                    .firstOrNull()
                    ?.effectiveThumbnailUrl
                    .orEmpty()
            }
                .getOrDefault("")
                .trim()

        cache[key] =
            artworkUrl

        return artworkUrl
            .takeIf(
                String::isNotBlank
            )
    }
}


@Composable
internal fun LyricsArtwork(
    result: LyricsSearchResult,
    size: Int,
    modifier: Modifier = Modifier
) {
    val context =
        LocalContext.current

    val artworkUrl by
        produceState<String?>(
            initialValue = null,
            key1 = result.trackName,
            key2 = result.artistName
        ) {
            value =
                LyricsArtworkResolver.resolve(
                    context = context,
                    result = result
                )
        }

    AppMediaCover(
        model =
            artworkUrl,
        size =
            size,
        modifier =
            modifier
    )
}