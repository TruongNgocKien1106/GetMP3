package com.ngoctien.getmp3.python

import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.ngoctien.getmp3.model.VideoInfo
import org.json.JSONArray
import org.json.JSONObject

class YtDlpBridge(
    context: Context
) {
    init {
        if (!Python.isStarted()) {
            Python.start(
                AndroidPlatform(
                    context.applicationContext
                )
            )
        }
    }

    private val module = Python.getInstance()
        .getModule("ytdlp_bridge")

    fun extractVideoInfo(url: String): VideoInfoResult {
        return try {
            val response = module.callAttr(
                "extract_video_info",
                url
            ).toString()

            val json = JSONObject(response)

            if (!json.optBoolean("success", false)) {
                return VideoInfoResult.Error(
                    json.optString(
                        "error",
                        "Không thể đọc thông tin video"
                    )
                )
            }

            VideoInfoResult.Success(
                VideoInfo(
                    sourceUrl = url,
                    title = json.optString(
                        "title",
                        "Unknown Title"
                    ),
                    artist = json.optString(
                        "artist",
                        "Unknown Artist"
                    ),
                    thumbnailUrl = json.optNullableString(
                        "thumbnailUrl"
                    ),
                    durationSeconds = json.optLong(
                        "durationSeconds",
                        0L
                    )
                )
            )
        } catch (exception: Exception) {
            VideoInfoResult.Error(
                exception.message
                    ?: "Không thể đọc phản hồi từ Python"
            )
        }
    }

    fun downloadAudio(
        url: String,
        outputDirectory: String,
        jobId: String,
        callback: PythonProgressCallback
    ): DownloadResult {
        return try {
            val response = module.callAttr(
                "download_audio",
                url,
                outputDirectory,
                jobId,
                callback
            ).toString()

            val json = JSONObject(response)

            if (!json.optBoolean("success", false)) {
                return DownloadResult.Error(
                    json.optString(
                        "error",
                        "Không thể tải âm thanh"
                    )
                )
            }

            DownloadResult.Success(
                audioPath = json.getString("audioPath"),
                coverPath = json.optNullableString("coverPath"),
                coverWarning = json.optNullableString(
                    "coverWarning"
                )
            )
        } catch (exception: Exception) {
            DownloadResult.Error(
                exception.message
                    ?: "Không thể gọi trình tải Python"
            )
        }
    }

    fun writeId3Tags(
        mp3Path: String,
        title: String,
        artist: String,
        coverPath: String?
    ): TagWriteResult {
        return try {
            val response = module.callAttr(
                "write_id3_tags",
                mp3Path,
                title,
                artist,
                coverPath
            ).toString()

            val json = JSONObject(response)

            if (!json.optBoolean("success", false)) {
                return TagWriteResult.Error(
                    message = json.optString(
                        "error",
                        "Không thể ghi ID3"
                    ),
                    technicalDetails = json.optNullableString(
                        "traceback"
                    )
                )
            }

            TagWriteResult.Success(
                coverEmbedded = json.optBoolean(
                    "coverEmbedded",
                    false
                ),
                warning = json.optNullableString("warning"),
                frames = json.optStringList("frames"),
                id3Version = json.optString(
                    "id3Version",
                    ""
                )
            )
        } catch (exception: Exception) {
            TagWriteResult.Error(
                message = exception.message
                    ?: "Không thể đọc kết quả ghi ID3",
                technicalDetails = null
            )
        }
    }
}

sealed interface VideoInfoResult {
    data class Success(
        val info: VideoInfo
    ) : VideoInfoResult

    data class Error(
        val message: String
    ) : VideoInfoResult
}

sealed interface DownloadResult {
    data class Success(
        val audioPath: String,
        val coverPath: String?,
        val coverWarning: String?
    ) : DownloadResult

    data class Error(
        val message: String
    ) : DownloadResult
}

sealed interface TagWriteResult {
    data class Success(
        val coverEmbedded: Boolean,
        val warning: String?,
        val frames: List<String>,
        val id3Version: String
    ) : TagWriteResult

    data class Error(
        val message: String,
        val technicalDetails: String?
    ) : TagWriteResult
}

private fun JSONObject.optNullableString(
    key: String
): String? {
    if (!has(key) || isNull(key)) {
        return null
    }

    return optString(key)
        .takeIf {
            it.isNotBlank()
        }
}

private fun JSONObject.optStringList(
    key: String
): List<String> {
    val array: JSONArray = optJSONArray(key)
        ?: return emptyList()

    return buildList {
        for (index in 0 until array.length()) {
            val value = array.optString(index)

            if (value.isNotBlank()) {
                add(value)
            }
        }
    }
}