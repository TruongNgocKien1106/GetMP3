package com.ngoctien.getmp3.download

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

data class CoverProcessResult(
    val jpegFile: File?,
    val warning: String?
)

class CoverProcessor {

    suspend fun convertToJpeg(
        sourceFile: File?,
        destinationFile: File,
        maxDimension: Int = 1280,
        quality: Int = 90
    ): CoverProcessResult {
        return withContext(Dispatchers.IO) {
            if (
                sourceFile == null ||
                !sourceFile.isFile ||
                sourceFile.length() <= 0L
            ) {
                return@withContext CoverProcessResult(
                    jpegFile = null,
                    warning = "Không có ảnh bìa"
                )
            }

            try {
                val boundsOptions =
                    BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }

                BitmapFactory.decodeFile(
                    sourceFile.absolutePath,
                    boundsOptions
                )

                if (
                    boundsOptions.outWidth <= 0 ||
                    boundsOptions.outHeight <= 0
                ) {
                    return@withContext CoverProcessResult(
                        jpegFile = null,
                        warning = "Không đọc được ảnh bìa"
                    )
                }

                var sampleSize = 1

                while (
                    max(
                        boundsOptions.outWidth / sampleSize,
                        boundsOptions.outHeight / sampleSize
                    ) > maxDimension * 2
                ) {
                    sampleSize *= 2
                }

                val decodeOptions =
                    BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }

                val decodedBitmap = BitmapFactory.decodeFile(
                    sourceFile.absolutePath,
                    decodeOptions
                ) ?: return@withContext CoverProcessResult(
                    jpegFile = null,
                    warning = "Không giải mã được ảnh bìa"
                )

                val largestDimension = max(
                    decodedBitmap.width,
                    decodedBitmap.height
                )

                val outputBitmap = if (
                    largestDimension > maxDimension
                ) {
                    val ratio =
                        maxDimension.toFloat() /
                            largestDimension.toFloat()

                    Bitmap.createScaledBitmap(
                        decodedBitmap,
                        (decodedBitmap.width * ratio)
                            .roundToInt()
                            .coerceAtLeast(1),
                        (decodedBitmap.height * ratio)
                            .roundToInt()
                            .coerceAtLeast(1),
                        true
                    )
                } else {
                    decodedBitmap
                }

                destinationFile.parentFile?.mkdirs()

                if (destinationFile.exists()) {
                    destinationFile.delete()
                }

                FileOutputStream(destinationFile).use { output ->
                    val compressed = outputBitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        quality.coerceIn(1, 100),
                        output
                    )

                    if (!compressed) {
                        throw IllegalStateException(
                            "Bitmap không thể nén thành JPEG"
                        )
                    }

                    output.flush()
                }

                if (outputBitmap !== decodedBitmap) {
                    outputBitmap.recycle()
                }

                decodedBitmap.recycle()

                if (
                    !destinationFile.isFile ||
                    destinationFile.length() <= 0L
                ) {
                    return@withContext CoverProcessResult(
                        jpegFile = null,
                        warning = "File ảnh JPEG bị rỗng"
                    )
                }

                val jpegSignature =
                    destinationFile.inputStream().use { input ->
                        ByteArray(3).also {
                            input.read(it)
                        }
                    }

                val validJpeg =
                    jpegSignature.size >= 3 &&
                        jpegSignature[0] == 0xFF.toByte() &&
                        jpegSignature[1] == 0xD8.toByte() &&
                        jpegSignature[2] == 0xFF.toByte()

                if (!validJpeg) {
                    destinationFile.delete()

                    return@withContext CoverProcessResult(
                        jpegFile = null,
                        warning = "Ảnh bìa sau chuyển đổi không phải JPEG"
                    )
                }

                CoverProcessResult(
                    jpegFile = destinationFile,
                    warning = null
                )
            } catch (exception: OutOfMemoryError) {
                destinationFile.delete()

                CoverProcessResult(
                    jpegFile = null,
                    warning = "Ảnh bìa quá lớn để xử lý"
                )
            } catch (exception: Exception) {
                destinationFile.delete()

                CoverProcessResult(
                    jpegFile = null,
                    warning = (
                        "Không xử lý được ảnh bìa: " +
                            (
                                exception.message
                                    ?: exception.javaClass.simpleName
                                )
                        )
                )
            }
        }
    }
}