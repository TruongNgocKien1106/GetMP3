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

    private val module =
        Python.getInstance()
            .getModule(
                "ytdlp_bridge"
            )

    fun readTags(
        mp3Path: String,
        coverOutputPath: String
    ): EditorTagReadResult {
        return try {
            val response =
                module.callAttr(
                    "read_mp3_editor_tags",
                    mp3Path,
                    coverOutputPath
                ).toString()

            val json =
                JSONObject(response)

            if (
                !json.optBoolean(
                    "success",
                    false
                )
            ) {
                return EditorTagReadResult.Error(
                    json.optString(
                        "error",
                        "Không đọc được metadata MP3"
                    )
                )
            }

            EditorTagReadResult.Success(
                album =
                    json.optString(
                        "album"
                    ),

                year =
                    json.optString(
                        "year"
                    ),

                coverPath =
                    json.optString(
                        "coverPath"
                    )
                        .takeIf(
                            String::isNotBlank
                        )
            )
        } catch (
            exception: Exception
        ) {
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
        album: String,
        year: String = ""
    ): EditorTagWriteResult {
        return try {
            val response =
                module.callAttr(
                    "update_mp3_editor_tags",
                    mp3Path,
                    title,
                    artist,
                    album,
                    year
                ).toString()

            val json =
                JSONObject(response)

            if (
                !json.optBoolean(
                    "success",
                    false
                )
            ) {
                return EditorTagWriteResult.Error(
                    json.optString(
                        "error",
                        "Không ghi được metadata MP3"
                    )
                )
            }

            EditorTagWriteResult.Success(
                id3Version =
                    json.optString(
                        "id3Version"
                    )
            )
        } catch (
            exception: Exception
        ) {
            EditorTagWriteResult.Error(
                exception.message
                    ?: "Không ghi được metadata MP3"
            )
        }
    }

    fun readLyrics(
        mp3Path: String
    ): LyricsTagReadResult {
        return try {
            val response =
                module.callAttr(
                    "read_mp3_lyrics",
                    mp3Path
                ).toString()

            val json =
                JSONObject(response)

            if (
                !json.optBoolean(
                    "success",
                    false
                )
            ) {
                return LyricsTagReadResult.Error(
                    json.optString(
                        "error",
                        "Không đọc được lời bài hát"
                    )
                )
            }

            LyricsTagReadResult.Success(
                text =
                    json.optString(
                        "lyrics"
                    ),

                language =
                    json.optString(
                        "language",
                        "und"
                    ),

                description =
                    json.optString(
                        "description"
                    )
            )
        } catch (
            exception: Exception
        ) {
            LyricsTagReadResult.Error(
                exception.message
                    ?: "Không đọc được lời bài hát"
            )
        }
    }

    fun writeLyrics(
        mp3Path: String,
        lyrics: String,
        language: String
    ): LyricsTagWriteResult {
        return try {
            val response =
                module.callAttr(
                    "update_mp3_lyrics",
                    mp3Path,
                    lyrics,
                    language
                ).toString()

            val json =
                JSONObject(response)

            if (
                !json.optBoolean(
                    "success",
                    false
                )
            ) {
                return LyricsTagWriteResult.Error(
                    json.optString(
                        "error",
                        "Không ghi được lời bài hát"
                    )
                )
            }

            LyricsTagWriteResult.Success(
                id3Version =
                    json.optString(
                        "id3Version"
                    ),

                language =
                    json.optString(
                        "language",
                        "und"
                    )
            )
        } catch (
            exception: Exception
        ) {
            LyricsTagWriteResult.Error(
                exception.message
                    ?: "Không ghi được lời bài hát"
            )
        }
    }
}

sealed interface EditorTagReadResult {
    data class Success(
        val album: String,
        val coverPath: String?,
        val year: String = ""
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

sealed interface LyricsTagReadResult {
    data class Success(
        val text: String,
        val language: String,
        val description: String
    ) : LyricsTagReadResult

    data class Error(
        val message: String
    ) : LyricsTagReadResult
}

sealed interface LyricsTagWriteResult {
    data class Success(
        val id3Version: String,
        val language: String
    ) : LyricsTagWriteResult

    data class Error(
        val message: String
    ) : LyricsTagWriteResult
}