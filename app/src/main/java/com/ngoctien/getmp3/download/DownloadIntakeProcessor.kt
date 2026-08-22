package com.ngoctien.getmp3.download

import android.content.Context
import com.ngoctien.getmp3.library.InboxWorkflowRepository
import com.ngoctien.getmp3.library.LibraryAdmissionPolicy
import com.ngoctien.getmp3.library.MediaIndexRepository
import com.ngoctien.getmp3.settings.AppSettings

data class DownloadIntakeResult(
    val outputUri: String,
    val statusMessage: String,
    val warningMessage: String? = null,
    val promotedToLibrary: Boolean = false
)

class DownloadIntakeProcessor(
    context: Context
) {

    private val mediaIndexRepository =
        MediaIndexRepository(
            context.applicationContext
        )

    private val inboxWorkflowRepository =
        InboxWorkflowRepository(
            context.applicationContext
        )

    suspend fun process(
        savedAudio: SavedAudio,
        settings: AppSettings
    ): DownloadIntakeResult {

        /*
         * File đã được MediaStoreWriter ghi an toàn.
         *
         * Lỗi từ đây trở đi không được biến một download
         * thành FAILED vì file MP3 thật đã tồn tại.
         */
        val indexed =
            runCatching {

                mediaIndexRepository
                    .indexInboxFile(
                        uri =
                            savedAudio
                                .uri
                                .toString(),

                        displayName =
                            savedAudio
                                .displayName,

                        sizeBytes =
                            savedAudio
                                .bytesWritten,

                        treeUri =
                            settings
                                .inboxTreeUri
                    )
            }
                .getOrElse {
                        exception ->

                    return DownloadIntakeResult(
                        outputUri =
                            savedAudio
                                .uri
                                .toString(),

                        statusMessage =
                            "Đã lưu vào Inbox",

                        warningMessage =
                            "File đã tải xong nhưng chưa cập nhật được Media Index: " +
                                (
                                    exception
                                        .message
                                        ?.take(
                                            180
                                        )
                                        ?: "lỗi không xác định"
                                    )
                    )
                }

        val admission =
            LibraryAdmissionPolicy
                .evaluate(
                    indexed
                )

        /*
         * Đây là trạng thái bình thường của file mới tải:
         *
         * pipeline hiện tại chỉ ghi:
         * TIT2 / TPE1 / APIC.
         *
         * Album / Year được hoàn thiện ở bước chuẩn hóa.
         */
        if (!admission.allowed) {

            return DownloadIntakeResult(
                outputUri =
                    savedAudio
                        .uri
                        .toString(),

                statusMessage =
                    "Đã lưu vào Inbox • cần chuẩn hóa",

                warningMessage =
                    admission
                        .message
                        .takeIf(
                            String::isNotBlank
                        )
            )
        }

        if (!settings.hasLibraryFolder) {

            return DownloadIntakeResult(
                outputUri =
                    savedAudio
                        .uri
                        .toString(),

                statusMessage =
                    "Đã lưu vào Inbox • chưa chọn Library",

                warningMessage =
                    "File đã đạt chuẩn nhưng chưa có Library để chuyển"
            )
        }

        /*
         * Future-proof:
         *
         * Nếu về sau download pipeline lấy được Album / Year
         * hợp lệ ngay từ nguồn thì bài có thể đi thẳng Library.
         */
        val promotion =
            runCatching {

                inboxWorkflowRepository
                    .promoteToLibrary(
                        indexed.uri
                    )
            }

        val promoted =
            promotion.getOrNull()

        if (promoted != null) {

            return DownloadIntakeResult(
                outputUri =
                    promoted
                        .newUri,

                statusMessage =
                    if (
                        promoted
                            .indexUpdated
                    ) {
                        "Đã đưa vào Library"
                    }
                    else {
                        "Đã đưa vào Library • cần đồng bộ index"
                    },

                warningMessage =
                    promoted
                        .warningMessage,

                promotedToLibrary =
                    true
            )
        }

        val error =
            promotion
                .exceptionOrNull()

        return DownloadIntakeResult(
            outputUri =
                savedAudio
                    .uri
                    .toString(),

            statusMessage =
                "Đã lưu vào Inbox • chưa chuyển được Library",

            warningMessage =
                error
                    ?.message
                    ?.take(
                        220
                    )
                    ?: "Không chuyển được file sang Library"
        )
    }
}
