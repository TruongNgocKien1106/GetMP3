package com.ngoctien.getmp3.python

import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import org.json.JSONObject

class Mp3TagBridge(
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

    private val module = Python
        .getInstance()
        .getModule("ytdlp_bridge")

    fun readTags(
        mp3Path: String,
        coverOutputPath: String
    ): EditorTagReadResult {
        return try {
            val response = module.callAttr(
                "read_mp3_editor_tags",
                mp3Path,
                coverOutputPath
            ).toString()

            val json = JSONObject(response)

            if (!json.optBoolean("success", false)) {
                return EditorTagReadResult.Error(
                    json.optString(
                        "error",
                        "Không đọc được metadata MP3"
                    )
                )
            }

            EditorTagReadResult.Success(
                title = json.optString("title"),
                artist = json.optString("artist"),
                album = json.optString("album"),
                coverPath = json
                    .optString("coverPath")
                    .takeIf {
                        it.isNotBlank()
                    }
            )
        } catch (exception: Exception) {
            EditorTagReadResult.Error(
                exception.message
                    ?: "Không đọc được phản hồi Python"
            )
        }
    }

    fun writeTags(
        mp3Path: String,
        title: String,
        artist: String,
        album: String
    ): EditorTagWriteResult {
        return try {
            val response = module.callAttr(
                "update_mp3_editor_tags",
                mp3Path,
                title,
                artist,
                album
            ).toString()

            val json = JSONObject(response)

            if (!json.optBoolean("success", false)) {
                return EditorTagWriteResult.Error(
                    json.optString(
                        "error",
                        "Không ghi được metadata MP3"
                    )
                )
            }

            EditorTagWriteResult.Success(
                id3Version = json.optString(
                    "id3Version"
                )
            )
        } catch (exception: Exception) {
            EditorTagWriteResult.Error(
                exception.message
                    ?: "Không ghi được metadata MP3"
            )
        }
    }
}

sealed interface EditorTagReadResult {
    data class Success(
        val title: String,
        val artist: String,
        val album: String,
        val coverPath: String?
    ) : EditorTagReadResult

    data class Error(
        val message: String
    ) : EditorTagReadResult
}

sealed interface EditorTagWriteResult {
    data class Success(
        val id3Version: String
    ) : EditorTagWriteResult

    data class Error(
        val message: String
    ) : EditorTagWriteResult
}