package com.ngoctien.getmp3.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.json.JSONArray
import java.util.Locale

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class AppSettings(
    val bitrateKbps: Int =
        DEFAULT_BITRATE,

    val downloadTreeUri: String? =
        null,

    val downloadFolderName: String =
        DEFAULT_FOLDER_NAME,

    val themeMode: AppThemeMode =
        AppThemeMode.SYSTEM,

    val compareTreeUri: String? =
        null,

    val compareFolderName: String =
        "Chưa chọn thư mục",

    val titleFilterTerms: List<String> =
        DEFAULT_FILTER_TERMS,

    val titleFilterSymbols: String =
        DEFAULT_FILTER_SYMBOLS,

    val indexedArtists: List<String> =
        emptyList(),

    val indexedAlbums: List<String> =
        emptyList(),

    val compareIndexSourceUri: String? =
        null,

    val compareIndexGeneratedAt: Long =
        0L
) {
    val usesCustomFolder: Boolean
        get() =
            !downloadTreeUri.isNullOrBlank()

    val hasCompareFolder: Boolean
        get() =
            !compareTreeUri.isNullOrBlank()

    companion object {
        const val DEFAULT_BITRATE = 128

        const val DEFAULT_FOLDER_NAME =
            "Music/GetMP3"

        const val DEFAULT_RELATIVE_PATH =
            "Music/GetMP3/"

        val SUPPORTED_BITRATES =
            listOf(
                96,
                128,
                160,
                192,
                256,
                320
            )

        val DEFAULT_FILTER_TERMS =
            listOf(
                // Video và metadata chính thức
                "Official Music Video",
                "Official Video",
                "Official Audio",
                "Official MV",
                "Official Lyrics",
                "Official Lyric Video",
                "Official Visualizer",
                "Official",
                "Music Video",
                "Audio Lyrics",
                "Lyric Video",
                "Lyrics Video",
                "Full Lyrics",
                "Full Lyric",
                "Lyrics",
                "Lyric",
                "Vietsub Lyrics",
                "Vietsub",
                "Viet Sub",
                "Sub Việt",
                "Visualizer",
                "Karaoke",
                "Instrumental",
                "Beat Chuẩn",

                // Chất lượng và phiên bản video
                "Full Version",
                "Full Audio",
                "Full HD",
                "High Quality",
                "HQ Audio",
                "Lossless",
                "4K Video",
                "4K",
                "HD",

                // Cụm quảng cáo TikTok và Remix
                "Nhạc Remix Hot TikTok Cực Cháy",
                "Nhạc Remix Hot Tik Tok Cực Cháy",
                "Nhạc Remix Hot TikTok",
                "Nhạc Remix Hot Tik Tok",
                "Nhạc Remix TikTok Cực Cháy",
                "Nhạc Remix Tik Tok Cực Cháy",
                "Nhạc Remix TikTok",
                "Nhạc Remix Tik Tok",
                "Nhạc Remix Cực Cháy",
                "Nhạc Remix Cực Cuốn",
                "Nhạc Remix Siêu Cuốn",
                "Remix Hot TikTok Cực Cháy",
                "Remix Hot Tik Tok Cực Cháy",
                "Remix Hot TikTok",
                "Remix Hot Tik Tok",
                "TikTok Remix Cực Cháy",
                "Tik Tok Remix Cực Cháy",
                "TikTok Remix",
                "Tik Tok Remix",
                "Remix TikTok",
                "Remix Tik Tok",
                "Nhạc Hot TikTok",
                "Nhạc Hot Tik Tok",
                "Hot TikTok",
                "Hot Tik Tok",
                "Trend TikTok",
                "Trend Tik Tok",
                "TikTok Viral",
                "Tik Tok Viral",
                "Viral TikTok",
                "Viral Tik Tok",
                "TikTok Music",
                "Tik Tok Music",

                // Cụm quảng cáo thường gặp
                "Cực Cháy",
                "Cực Cuốn",
                "Siêu Cuốn",
                "Cực Sung",
                "Cực Phiêu",
                "Cực Chill",
                "Căng Đét",
                "Nghe Là Nghiện",
                "Nghe Là Mê",
                "Nghe Cực Cuốn",
                "Gây Nghiện",
                "Bass Cực Căng",
                "Bass Căng",
                "Full Bass",
                "Quẩy Banh Nóc",
                "Quẩy Cực Sung",
                "Bay Phòng",
                "Hot Trend",
                "Top Trending",
                "Đang Hot",
                "Thịnh Hành",
                "Mới Nhất",
                "Hay Nhất",
                "Cực Hay",
                "Siêu Phẩm",
                "Tuyển Chọn",
                "Nhạc Hay Mỗi Ngày",

                // Thể loại và tên phiên bản thừa
                "Nhạc Trẻ Remix Hay Nhất",
                "Nhạc Trẻ Remix",
                "Nhạc Việt Remix",
                "Nhạc Hoa Remix",
                "Nhạc Trung Remix",
                "Nhạc Remix",
                "Nhạc Sàn",
                "Nhạc Bay",
                "Nhạc Quẩy",
                "Nhạc Chill",
                "Bản Phối Remix",
                "Bản Phối Mới",
                "Bản Remix",
                "Remix Version",
                "DJ Remix",
                "EDM Remix",
                "Lofi Remix",
                "Nonstop",
                "Vinahouse",
                "Bass Boosted",
                "8D Audio",
                "Speed Up Version",
                "Speed Up",
                "Sped Up Version",
                "Sped Up",
                "Slowed And Reverb",
                "Slowed Reverb",
                "Slowed + Reverb",
                "Nightcore",

                // Nền tảng và nội dung ngắn
                "YouTube Shorts",
                "Youtube Shorts",
                "Instagram Reels",
                "Facebook Reels",
                "Shorts",
                "Reels",
                "TikTok",
                "Tik Tok",

                // Để cuối vì đây là cụm rất ngắn và mạnh
                "Remix"
            )

        const val DEFAULT_FILTER_SYMBOLS =
            "|•~_"
    }
}

class AppSettingsRepository(
    context: Context
) {
    companion object {
        private const val PREFS_NAME =
            "getmp3_app_settings"

        private const val KEY_BITRATE =
            "bitrate_kbps"

        private const val KEY_TREE_URI =
            "download_tree_uri"

        private const val KEY_FOLDER_NAME =
            "download_folder_name"

        private const val KEY_THEME_MODE =
            "theme_mode"

        private const val KEY_COMPARE_TREE_URI =
            "compare_tree_uri"

        private const val KEY_COMPARE_FOLDER_NAME =
            "compare_folder_name"

        private const val KEY_FILTER_TERMS =
            "title_filter_terms"

        private const val KEY_FILTER_SYMBOLS =
            "title_filter_symbols"


        private const val KEY_FILTER_TERMS_VERSION =
            "title_filter_terms_version"

        private const val CURRENT_FILTER_TERMS_VERSION = 1
        private const val KEY_INDEXED_ARTISTS =
            "indexed_artists"

        private const val KEY_INDEXED_ALBUMS =
            "indexed_albums"

        private const val KEY_INDEX_SOURCE_URI =
            "compare_index_source_uri"

        private const val KEY_INDEX_GENERATED_AT =
            "compare_index_generated_at"
    }

    private val preferences =
        context.applicationContext
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

    init {
        migrateQuickFormatTermsIfNeeded()
    }
    fun getSettings(): AppSettings {
        val savedBitrate =
            preferences.getInt(
                KEY_BITRATE,
                AppSettings.DEFAULT_BITRATE
            )

        val bitrate =
            savedBitrate.takeIf {
                it in
                    AppSettings
                        .SUPPORTED_BITRATES
            } ?: AppSettings.DEFAULT_BITRATE

        val themeMode =
            runCatching {
                AppThemeMode.valueOf(
                    preferences.getString(
                        KEY_THEME_MODE,
                        AppThemeMode
                            .SYSTEM
                            .name
                    ) ?: AppThemeMode
                        .SYSTEM
                        .name
                )
            }.getOrDefault(
                AppThemeMode.SYSTEM
            )

        return AppSettings(
            bitrateKbps = bitrate,

            downloadTreeUri =
                preferences.getString(
                    KEY_TREE_URI,
                    null
                )?.takeIf {
                    it.isNotBlank()
                },

            downloadFolderName =
                preferences.getString(
                    KEY_FOLDER_NAME,
                    AppSettings
                        .DEFAULT_FOLDER_NAME
                )?.takeIf {
                    it.isNotBlank()
                } ?: AppSettings
                    .DEFAULT_FOLDER_NAME,

            themeMode = themeMode,

            compareTreeUri =
                preferences.getString(
                    KEY_COMPARE_TREE_URI,
                    null
                )?.takeIf {
                    it.isNotBlank()
                },

            compareFolderName =
                preferences.getString(
                    KEY_COMPARE_FOLDER_NAME,
                    "Chưa chọn thư mục"
                )?.takeIf {
                    it.isNotBlank()
                } ?: "Chưa chọn thư mục",

            titleFilterTerms =
                readStringList(
                    key = KEY_FILTER_TERMS,
                    fallback =
                        AppSettings
                            .DEFAULT_FILTER_TERMS
                ),

            titleFilterSymbols =
                preferences.getString(
                    KEY_FILTER_SYMBOLS,
                    AppSettings
                        .DEFAULT_FILTER_SYMBOLS
                ) ?: AppSettings
                    .DEFAULT_FILTER_SYMBOLS,

            indexedArtists =
                readStringList(
                    key =
                        KEY_INDEXED_ARTISTS,
                    fallback =
                        emptyList()
                ),

            indexedAlbums =
                readStringList(
                    key =
                        KEY_INDEXED_ALBUMS,
                    fallback =
                        emptyList()
                ),

            compareIndexSourceUri =
                preferences.getString(
                    KEY_INDEX_SOURCE_URI,
                    null
                ),

            compareIndexGeneratedAt =
                preferences.getLong(
                    KEY_INDEX_GENERATED_AT,
                    0L
                )
        )
    }

    fun observeSettings():
        Flow<AppSettings> {

        return callbackFlow {
            val listener =
                SharedPreferences
                    .OnSharedPreferenceChangeListener {
                            _,
                            _ ->

                        trySend(
                            getSettings()
                        )
                    }

            trySend(getSettings())

            preferences
                .registerOnSharedPreferenceChangeListener(
                    listener
                )

            awaitClose {
                preferences
                    .unregisterOnSharedPreferenceChangeListener(
                        listener
                    )
            }
        }.distinctUntilChanged()
    }

    fun setBitrate(
        bitrateKbps: Int
    ) {
        require(
            bitrateKbps in
                AppSettings
                    .SUPPORTED_BITRATES
        ) {
            "Bitrate không được hỗ trợ"
        }

        preferences.edit()
            .putInt(
                KEY_BITRATE,
                bitrateKbps
            )
            .apply()
    }

    fun setThemeMode(
        mode: AppThemeMode
    ) {
        preferences.edit()
            .putString(
                KEY_THEME_MODE,
                mode.name
            )
            .apply()
    }

    fun setDownloadFolder(
        treeUri: String,
        displayName: String
    ) {
        require(
            treeUri.isNotBlank()
        )

        preferences.edit()
            .putString(
                KEY_TREE_URI,
                treeUri
            )
            .putString(
                KEY_FOLDER_NAME,
                displayName.ifBlank {
                    "Thư mục đã chọn"
                }
            )
            .apply()
    }

    fun useDefaultFolder() {
        preferences.edit()
            .remove(KEY_TREE_URI)
            .putString(
                KEY_FOLDER_NAME,
                AppSettings
                    .DEFAULT_FOLDER_NAME
            )
            .apply()
    }

    fun setCompareFolder(
        treeUri: String,
        displayName: String
    ) {
        require(
            treeUri.isNotBlank()
        )

        preferences.edit()
            .putString(
                KEY_COMPARE_TREE_URI,
                treeUri
            )
            .putString(
                KEY_COMPARE_FOLDER_NAME,
                displayName.ifBlank {
                    "Thư mục đối chiếu"
                }
            )
            .remove(KEY_INDEXED_ARTISTS)
            .remove(KEY_INDEXED_ALBUMS)
            .remove(KEY_INDEX_SOURCE_URI)
            .remove(KEY_INDEX_GENERATED_AT)
            .apply()
    }

    fun clearCompareFolder() {
        preferences.edit()
            .remove(KEY_COMPARE_TREE_URI)
            .putString(
                KEY_COMPARE_FOLDER_NAME,
                "Chưa chọn thư mục"
            )
            .remove(KEY_INDEXED_ARTISTS)
            .remove(KEY_INDEXED_ALBUMS)
            .remove(KEY_INDEX_SOURCE_URI)
            .remove(KEY_INDEX_GENERATED_AT)
            .apply()
    }

    fun saveCompareIndex(
        sourceUri: String,
        artists: List<String>,
        albums: List<String>
    ) {
        preferences.edit()
            .putString(
                KEY_INDEXED_ARTISTS,
                JSONArray(
                    artists.distinctBy {
                        it.lowercase(
                            Locale.ROOT
                        )
                    }
                ).toString()
            )
            .putString(
                KEY_INDEXED_ALBUMS,
                JSONArray(
                    albums.distinctBy {
                        it.lowercase(
                            Locale.ROOT
                        )
                    }
                ).toString()
            )
            .putString(
                KEY_INDEX_SOURCE_URI,
                sourceUri
            )
            .putLong(
                KEY_INDEX_GENERATED_AT,
                System.currentTimeMillis()
            )
            .apply()
    }

    fun setTitleFilters(
        terms: List<String>,
        symbols: String
    ) {
        val cleanTerms =
            terms.map {
                it.trim()
            }
                .filter {
                    it.isNotBlank()
                }
                .distinctBy {
                    it.lowercase(
                        Locale.ROOT
                    )
                }

        preferences.edit()
            .putString(
                KEY_FILTER_TERMS,
                JSONArray(
                    cleanTerms
                ).toString()
            )
            .putString(
                KEY_FILTER_SYMBOLS,
                symbols
            )
            .apply()
    }

    /**
     * Đưa toàn bộ bộ từ Format nhanh vào Settings đúng một lần.
     *
     * Sau khi migration hoàn tất, người dùng có thể xóa bất kỳ
     * cụm nào và cụm đó sẽ không tự xuất hiện lại.
     */
    private fun migrateQuickFormatTermsIfNeeded() {
        val currentVersion =
            preferences.getInt(
                KEY_FILTER_TERMS_VERSION,
                0
            )

        if (
            currentVersion >=
            CURRENT_FILTER_TERMS_VERSION
        ) {
            return
        }

        val existingTerms =
            readStringList(
                key = KEY_FILTER_TERMS,
                fallback = emptyList()
            )

        val mergedTerms =
            (
                existingTerms +
                    AppSettings.DEFAULT_FILTER_TERMS
                )
                .map {
                    it.trim()
                        .replace(
                            Regex("""\s+"""),
                            " "
                        )
                }
                .filter {
                    it.isNotBlank()
                }
                .distinctBy {
                    it.lowercase(
                        Locale.ROOT
                    )
                }

        preferences.edit()
            .putString(
                KEY_FILTER_TERMS,
                JSONArray(
                    mergedTerms
                ).toString()
            )
            .putInt(
                KEY_FILTER_TERMS_VERSION,
                CURRENT_FILTER_TERMS_VERSION
            )
            .apply()
    }

    private fun readStringList(
        key: String,
        fallback: List<String>
    ): List<String> {
        val raw =
            preferences.getString(
                key,
                null
            ) ?: return fallback

        return runCatching {
            val array =
                JSONArray(raw)

            buildList {
                for (
                    index in
                    0 until array.length()
                ) {
                    val value =
                        array.optString(index)
                            .trim()

                    if (value.isNotBlank()) {
                        add(value)
                    }
                }
            }
        }.getOrDefault(fallback)
    }
}