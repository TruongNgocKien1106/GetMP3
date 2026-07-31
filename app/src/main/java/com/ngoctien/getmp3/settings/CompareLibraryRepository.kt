package com.ngoctien.getmp3.settings

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.text.Normalizer
import java.util.Locale

data class CompareIndexResult(
    val artists: List<String>,
    val albums: List<String>,
    val scannedFiles: Int
)

class CompareLibraryRepository(
    context: Context
) {
    companion object {
        /*
         * Sau mỗi lô sẽ:
         * - kiểm tra tác vụ có bị hủy không
         * - cập nhật tiến trình
         * - nhường luồng cho hệ thống
         */
        private const val BATCH_SIZE = 250

        private const val MP3_MIME_TYPE =
            "audio/mpeg"
    }

    private val resolver =
        context.applicationContext
            .contentResolver

    /**
     * Chỉ quét file nằm trực tiếp trong thư mục đã chọn.
     *
     * Không:
     * - đệ quy vào thư mục con
     * - đọc ID3
     * - mở MediaMetadataRetriever
     * - giải mã dữ liệu MP3
     *
     * Artist được lấy từ quy tắc:
     *
     * Title - Artist.mp3
     */
    suspend fun scan(
        treeUriText: String,
        onProgress: (Int) -> Unit = {}
    ): CompareIndexResult {
        return withContext(Dispatchers.IO) {
            val treeUri =
                Uri.parse(treeUriText)

            val rootDocumentId =
                DocumentsContract
                    .getTreeDocumentId(
                        treeUri
                    )

            /*
             * buildChildDocumentsUriUsingTree chỉ lấy
             * các phần tử trực tiếp của thư mục gốc.
             */
            val directChildrenUri =
                DocumentsContract
                    .buildChildDocumentsUriUsingTree(
                        treeUri,
                        rootDocumentId
                    )

            val artists =
                linkedMapOf<String, String>()

            var scannedFiles = 0

            val projection = arrayOf(
                DocumentsContract
                    .Document
                    .COLUMN_DOCUMENT_ID,

                DocumentsContract
                    .Document
                    .COLUMN_DISPLAY_NAME,

                DocumentsContract
                    .Document
                    .COLUMN_MIME_TYPE
            )

            resolver.query(
                directChildrenUri,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                val nameColumn =
                    cursor.getColumnIndexOrThrow(
                        DocumentsContract
                            .Document
                            .COLUMN_DISPLAY_NAME
                    )

                val mimeColumn =
                    cursor.getColumnIndexOrThrow(
                        DocumentsContract
                            .Document
                            .COLUMN_MIME_TYPE
                    )

                while (cursor.moveToNext()) {
                    /*
                     * Cho phép lượt quét cũ bị hủy ngay
                     * khi người dùng chọn thư mục khác.
                     */
                    currentCoroutineContext()
                        .ensureActive()

                    val displayName =
                        cursor.getString(
                            nameColumn
                        )
                            ?.trim()
                            .orEmpty()

                    val mimeType =
                        cursor.getString(
                            mimeColumn
                        )
                            ?.trim()
                            .orEmpty()

                    /*
                     * Tuyệt đối bỏ qua thư mục con.
                     */
                    if (
                        mimeType ==
                        DocumentsContract
                            .Document
                            .MIME_TYPE_DIR
                    ) {
                        continue
                    }

                    val isMp3 =
                        mimeType.equals(
                            MP3_MIME_TYPE,
                            ignoreCase = true
                        ) ||
                            displayName.endsWith(
                                ".mp3",
                                ignoreCase = true
                            )

                    if (!isMp3) {
                        continue
                    }

                    scannedFiles++

                    extractArtistsFromFileName(
                        displayName
                    ).forEach { artist ->
                        artists.putPreferred(
                            artist
                        )
                    }

                    /*
                     * Không cập nhật Compose theo từng file.
                     * 5.000 lần cập nhật UI là quá nhiều.
                     */
                    if (
                        scannedFiles == 1 ||
                        scannedFiles % BATCH_SIZE == 0
                    ) {
                        onProgress(
                            scannedFiles
                        )

                        /*
                         * Nhường thời gian cho hệ thống xử lý
                         * các tác vụ khác.
                         */
                        yield()
                    }
                }
            }

            onProgress(scannedFiles)

            CompareIndexResult(
                artists = artists.values
                    .sortedBy {
                        normalizeKey(it)
                    },

                /*
                 * Không đọc Album từ ID3 trong thư mục
                 * đối chiếu để tránh mở hàng nghìn MP3.
                 */
                albums = emptyList(),

                scannedFiles =
                    scannedFiles
            )
        }
    }

    /**
     * File chuẩn:
     *
     * Title - Artist.mp3
     *
     * Dùng dấu phân cách cuối cùng để Title vẫn có thể
     * chứa dấu gạch ngang.
     */
    private fun extractArtistsFromFileName(
        displayName: String
    ): List<String> {
        val stem =
            displayName
                .substringBeforeLast(
                    delimiter = ".",
                    missingDelimiterValue =
                        displayName
                )
                .normalizeSpaces()

        if (stem.isBlank()) {
            return emptyList()
        }

        val parts =
            stem.split(
                Regex(
                    """\s+[-–—]\s+"""
                )
            )

        if (parts.size < 2) {
            /*
             * File không đúng Title - Artist.mp3
             * thì bỏ qua, không cố mở MP3 để đọc ID3.
             */
            return emptyList()
        }

        val fullArtist =
            parts.last()
                .normalizeSpaces()
                .trim(
                    '-',
                    '–',
                    '—',
                    '_',
                    '|',
                    '.',
                    ',',
                    ';'
                )
                .normalizeSpaces()

        if (fullArtist.isBlank()) {
            return emptyList()
        }

        val result =
            linkedSetOf<String>()

        /*
         * Giữ nguyên cả tên nhóm Artist:
         *
         * Sơn Tùng M-TP x Binz
         */
        result.add(fullArtist)

        /*
         * Đồng thời tách từng Artist để tăng khả năng
         * đối chiếu khi Format nhanh.
         */
        fullArtist.split(
            Regex(
                pattern = """
                    (?i)
                    \s+(?:x|ft\.?|feat\.?|featuring)\s+
                    |
                    \s*[,;&]\s*
                """.trimIndent()
            )
        )
            .map {
                it.normalizeSpaces()
            }
            .filter {
                it.length >= 2
            }
            .forEach {
                result.add(it)
            }

        return result.toList()
    }

    private fun MutableMap<String, String>
        .putPreferred(
            value: String
        ) {
        val clean =
            value.normalizeSpaces()

        if (clean.isBlank()) {
            return
        }

        val key =
            normalizeKey(clean)

        if (key.isBlank()) {
            return
        }

        val existing =
            this[key]

        if (
            existing == null ||
            isBetterDisplayName(
                candidate = clean,
                current = existing
            )
        ) {
            this[key] = clean
        }
    }

    /**
     * Ưu tiên tên có dấu tiếng Việt đầy đủ.
     *
     * Ví dụ:
     * - Son Tung M-TP
     * - Sơn Tùng M-TP
     *
     * Sẽ giữ Sơn Tùng M-TP.
     */
    private fun isBetterDisplayName(
        candidate: String,
        current: String
    ): Boolean {
        val candidateScore =
            displayScore(candidate)

        val currentScore =
            displayScore(current)

        return when {
            candidateScore.nonAscii !=
                currentScore.nonAscii -> {
                candidateScore.nonAscii >
                    currentScore.nonAscii
            }

            candidateScore.letters !=
                currentScore.letters -> {
                candidateScore.letters >
                    currentScore.letters
            }

            else -> {
                candidateScore.length >
                    currentScore.length
            }
        }
    }

    private fun displayScore(
        value: String
    ): DisplayScore {
        return DisplayScore(
            nonAscii = value.count {
                it.code > 127
            },

            letters = value.count {
                it.isLetter()
            },

            length = value.length
        )
    }

    private fun normalizeKey(
        value: String
    ): String {
        val decomposed =
            Normalizer.normalize(
                value
                    .normalizeSpaces()
                    .lowercase(
                        Locale.ROOT
                    )
                    .replace('đ', 'd'),
                Normalizer.Form.NFD
            )

        return decomposed
            .replace(
                Regex("""\p{M}+"""),
                ""
            )
            .replace(
                Regex("""[^a-z0-9]+"""),
                " "
            )
            .normalizeSpaces()
    }

    private fun String.normalizeSpaces():
        String {

        return this
            /*
             * Xóa các khoảng trắng ẩn.
             */
            .replace(
                Regex(
                    """[\u200B-\u200D\uFEFF]"""
                ),
                ""
            )
            /*
             * Tab, xuống dòng và nhiều dấu cách
             * đều chỉ còn một dấu cách.
             */
            .replace(
                Regex("""\s+"""),
                " "
            )
            .trim()
    }

    private data class DisplayScore(
        val nonAscii: Int,
        val letters: Int,
        val length: Int
    )
}