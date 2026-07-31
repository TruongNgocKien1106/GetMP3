package com.ngoctien.getmp3.youtube

import android.content.Context
import com.chaquo.python.PyException
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

class YouTubeSearchRepository(
    context: Context
) {
    private val applicationContext =
        context.applicationContext

    suspend fun search(
        query: String,
        limit: Int = 10
    ): List<YouTubeSearchResult> {
        val cleanQuery =
            query
                .trim()
                .replace(
                    Regex("""\s+"""),
                    " "
                )

        require(cleanQuery.isNotBlank()) {
            "Hãy nhập nội dung cần tìm."
        }

        val safeLimit =
            limit.coerceIn(
                1,
                15
            )

        return withContext(
            Dispatchers.IO
        ) {
            try {
                ensurePythonStarted()

                val python =
                    Python.getInstance()

                val module =
                    python.getModule(
                        "youtube_search"
                    )

                val jsonText =
                    module.callAttr(
                        "search_youtube_json",
                        cleanQuery,
                        safeLimit
                    ).toString()

                parseResults(
                    jsonText
                )
            } catch (exception: PyException) {
                throw IllegalStateException(
                    cleanPythonError(
                        exception.message
                    ),
                    exception
                )
            }
        }
    }

    private fun ensurePythonStarted() {
        if (!Python.isStarted()) {
            Python.start(
                AndroidPlatform(
                    applicationContext
                )
            )
        }
    }

    private fun parseResults(
        jsonText: String
    ): List<YouTubeSearchResult> {
        val jsonArray =
            JSONArray(jsonText)

        val results =
            ArrayList<YouTubeSearchResult>(
                jsonArray.length()
            )

        for (
            index in
            0 until jsonArray.length()
        ) {
            val item =
                jsonArray.optJSONObject(
                    index
                ) ?: continue

            val videoId =
                item.optString(
                    "video_id"
                ).trim()

            val title =
                item.optString(
                    "title"
                ).trim()

            val webpageUrl =
                item.optString(
                    "webpage_url"
                ).trim()

            if (
                videoId.isBlank() ||
                title.isBlank() ||
                webpageUrl.isBlank()
            ) {
                continue
            }

            val duration =
                if (
                    item.isNull(
                        "duration"
                    )
                ) {
                    null
                } else {
                    item.optLong(
                        "duration",
                        -1L
                    )
                        .takeIf {
                            it >= 0L
                        }
                }

            results.add(
                YouTubeSearchResult(
                    videoId =
                        videoId,

                    title =
                        title,

                    channel =
                        item.optString(
                            "channel"
                        ).trim(),

                    durationSeconds =
                        duration,

                    thumbnailUrl =
                        item.optString(
                            "thumbnail_url"
                        )
                            .trim()
                            .takeIf {
                                it.isNotBlank()
                            },

                    webpageUrl =
                        webpageUrl
                )
            )
        }

        return results.distinctBy {
            it.videoId
        }
    }

    private fun cleanPythonError(
        message: String?
    ): String {
        val rawMessage =
            message
                ?.trim()
                .orEmpty()

        if (rawMessage.isBlank()) {
            return "Không tìm được video trên YouTube."
        }

        val usefulLine =
            rawMessage
                .lineSequence()
                .map(String::trim)
                .filter(String::isNotBlank)
                .lastOrNull()
                .orEmpty()

        return usefulLine
            .removePrefix(
                "RuntimeError:"
            )
            .removePrefix(
                "ValueError:"
            )
            .trim()
            .ifBlank {
                "Không tìm được video trên YouTube."
            }
    }
}