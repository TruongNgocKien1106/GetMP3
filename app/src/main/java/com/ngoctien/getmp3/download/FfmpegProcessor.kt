package com.ngoctien.getmp3.download

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.ArrayDeque
import java.util.concurrent.TimeUnit

data class FfmpegCheckResult(
    val ready: Boolean,
    val message: String,
    val versionOutput: String = ""
)

data class FfmpegProgress(
    val percent: Int,
    val processedSeconds: Long,
    val speed: Float
)

class FfmpegExecutionException(
    message: String,
    val technicalDetails: String
) : Exception(message)

class FfmpegCancelledException :
    Exception("Đã hủy chuyển đổi FFmpeg")

class FfmpegProcessor(
    private val context: Context
) {
    @Volatile
    private var currentProcess: Process? = null

    private fun ffmpegFile(): File {
        return File(
            context.applicationInfo.nativeLibraryDir,
            "libffmpeg.so"
        )
    }

    suspend fun verifyInstallation(): FfmpegCheckResult {
        return withContext(Dispatchers.IO) {
            val executable = ffmpegFile()

            if (!executable.exists()) {
                return@withContext FfmpegCheckResult(
                    ready = false,
                    message = (
                        "Không tìm thấy libffmpeg.so cho ABI " +
                            android.os.Build.SUPPORTED_ABIS.firstOrNull()
                    )
                )
            }

            if (executable.length() <= 0L) {
                return@withContext FfmpegCheckResult(
                    ready = false,
                    message = "Binary FFmpeg bị rỗng"
                )
            }

            val header = runCatching {
                executable.inputStream().use { input ->
                    ByteArray(4).also {
                        input.read(it)
                    }
                }
            }.getOrNull()

            if (
                header == null ||
                header.size < 4 ||
                header[0] != 0x7F.toByte() ||
                header[1] != 'E'.code.toByte() ||
                header[2] != 'L'.code.toByte() ||
                header[3] != 'F'.code.toByte()
            ) {
                return@withContext FfmpegCheckResult(
                    ready = false,
                    message = "Binary FFmpeg không phải file ELF hợp lệ"
                )
            }

            val versionResult = runDiagnostic(
                command = listOf(
                    executable.absolutePath,
                    "-hide_banner",
                    "-version"
                ),
                timeoutSeconds = 15
            )

            if (versionResult.exitCode != 0) {
                return@withContext FfmpegCheckResult(
                    ready = false,
                    message = (
                        "FFmpeg không chạy được trên Android. " +
                            versionResult.output.takeLast(500)
                    )
                )
            }

            if (
                !versionResult.output.contains(
                    "ffmpeg version",
                    ignoreCase = true
                )
            ) {
                return@withContext FfmpegCheckResult(
                    ready = false,
                    message = "File hiện tại không phản hồi như FFmpeg"
                )
            }

            val encoderResult = runDiagnostic(
                command = listOf(
                    executable.absolutePath,
                    "-hide_banner",
                    "-encoders"
                ),
                timeoutSeconds = 20
            )

            if (encoderResult.exitCode != 0) {
                return@withContext FfmpegCheckResult(
                    ready = false,
                    message = "Không đọc được danh sách encoder FFmpeg"
                )
            }

            if (
                !encoderResult.output.contains(
                    "libmp3lame",
                    ignoreCase = true
                )
            ) {
                return@withContext FfmpegCheckResult(
                    ready = false,
                    message = (
                        "FFmpeg không có encoder libmp3lame. " +
                            "Hãy thay bằng bản Android có hỗ trợ MP3."
                    )
                )
            }

            FfmpegCheckResult(
                ready = true,
                message = "FFmpeg Android và libmp3lame đã sẵn sàng",
                versionOutput = versionResult.output
            )
        }
    }

    suspend fun convertToMp3(
        inputFile: File,
        outputFile: File,
        durationSeconds: Long,
        bitrateKbps: Int,
        isCancelled: () -> Boolean,
        onProgress: suspend (FfmpegProgress) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            coroutineScope {
                if (!inputFile.isFile || inputFile.length() <= 0L) {
                    throw FfmpegExecutionException(
                        message = "File âm thanh nguồn không hợp lệ",
                        technicalDetails = inputFile.absolutePath
                    )
                }

                val executable = ffmpegFile()

                if (!executable.isFile) {
                    throw FfmpegExecutionException(
                        message = "Không tìm thấy FFmpeg Android",
                        technicalDetails = executable.absolutePath
                    )
                }

                outputFile.parentFile?.mkdirs()

                if (outputFile.exists()) {
                    outputFile.delete()
                }

                val command = listOf(
                    executable.absolutePath,
                    "-hide_banner",
                    "-y",
                    "-i",
                    inputFile.absolutePath,
                    "-map_metadata",
                    "-1",
                    "-vn",
                    "-c:a",
                    "libmp3lame",
                    "-b:a",
                    "${bitrateKbps.coerceIn(64, 320)}k",
                    "-progress",
                    "pipe:1",
                    "-nostats",
                    outputFile.absolutePath
                )

                val process = ProcessBuilder(command)
                    .start()

                currentProcess = process

                val stderrLines = ArrayDeque<String>()

                try {
                    val stdoutJob = launch(Dispatchers.IO) {
                        process.inputStream
                            .bufferedReader()
                            .use { reader ->
                                var outTimeUs = 0L
                                var speed = 0f
                                var lastEmitAt = 0L

                                while (true) {
                                    val line = reader.readLine()
                                        ?: break

                                    if (isCancelled()) {
                                        break
                                    }

                                    val separatorIndex = line.indexOf('=')

                                    if (separatorIndex <= 0) {
                                        continue
                                    }

                                    val key = line.substring(
                                        0,
                                        separatorIndex
                                    )

                                    val value = line.substring(
                                        separatorIndex + 1
                                    )

                                    when (key) {
                                        "out_time_us",
                                        "out_time_ms" -> {
                                            outTimeUs = value.toLongOrNull()
                                                ?: outTimeUs
                                        }

                                        "speed" -> {
                                            speed = value
                                                .removeSuffix("x")
                                                .trim()
                                                .toFloatOrNull()
                                                ?: speed
                                        }

                                        "progress" -> {
                                            val isEnd = value == "end"

                                            val processedSeconds =
                                                outTimeUs / 1_000_000L

                                            val percent = when {
                                                isEnd -> 100

                                                durationSeconds > 0L -> {
                                                    (
                                                        processedSeconds *
                                                            100L /
                                                            durationSeconds
                                                        )
                                                        .toInt()
                                                        .coerceIn(0, 99)
                                                }

                                                else -> 0
                                            }

                                            val now =
                                                System.currentTimeMillis()

                                            if (
                                                isEnd ||
                                                now - lastEmitAt >= 400L
                                            ) {
                                                lastEmitAt = now

                                                onProgress(
                                                    FfmpegProgress(
                                                        percent = percent,
                                                        processedSeconds =
                                                            processedSeconds,
                                                        speed = speed
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                    }

                    val stderrJob = launch(Dispatchers.IO) {
                        process.errorStream
                            .bufferedReader()
                            .use { reader ->
                                while (true) {
                                    val line = reader.readLine()
                                        ?: break

                                    synchronized(stderrLines) {
                                        stderrLines.addLast(line)

                                        while (stderrLines.size > 60) {
                                            stderrLines.removeFirst()
                                        }
                                    }
                                }
                            }
                    }

                    while (process.isAlive) {
                        if (isCancelled()) {
                            process.destroy()

                            if (
                                !process.waitFor(
                                    2,
                                    TimeUnit.SECONDS
                                )
                            ) {
                                process.destroyForcibly()
                            }

                            throw FfmpegCancelledException()
                        }

                        delay(200L)
                    }

                    stdoutJob.join()
                    stderrJob.join()

                    val exitCode = process.exitValue()

                    if (isCancelled()) {
                        throw FfmpegCancelledException()
                    }

                    if (exitCode != 0) {
                        val details = synchronized(stderrLines) {
                            stderrLines.joinToString("\n")
                        }

                        throw FfmpegExecutionException(
                            message = (
                                "FFmpeg chuyển đổi thất bại " +
                                    "(exit code $exitCode)"
                                ),
                            technicalDetails = details
                        )
                    }

                    if (
                        !outputFile.isFile ||
                        outputFile.length() <= 0L
                    ) {
                        throw FfmpegExecutionException(
                            message = "FFmpeg không tạo được file MP3",
                            technicalDetails = outputFile.absolutePath
                        )
                    }

                    onProgress(
                        FfmpegProgress(
                            percent = 100,
                            processedSeconds = durationSeconds,
                            speed = 0f
                        )
                    )
                } finally {
                    if (process.isAlive) {
                        process.destroyForcibly()
                    }

                    if (currentProcess === process) {
                        currentProcess = null
                    }
                }
            }
        }
    }

    fun cancel() {
        currentProcess?.let { process ->
            runCatching {
                process.destroy()
            }
        }
    }

    private fun runDiagnostic(
        command: List<String>,
        timeoutSeconds: Long
    ): DiagnosticResult {
        return try {
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()

            val output = StringBuilder()

            val readerThread = Thread {
                runCatching {
                    process.inputStream
                        .bufferedReader()
                        .useLines { lines ->
                            lines.forEach { line ->
                                output.appendLine(line)
                            }
                        }
                }
            }

            readerThread.start()

            val finished = process.waitFor(
                timeoutSeconds,
                TimeUnit.SECONDS
            )

            if (!finished) {
                process.destroyForcibly()
                readerThread.join(2_000L)

                return DiagnosticResult(
                    exitCode = -1,
                    output = "FFmpeg không phản hồi"
                )
            }

            readerThread.join(2_000L)

            DiagnosticResult(
                exitCode = process.exitValue(),
                output = output.toString()
            )
        } catch (exception: Exception) {
            DiagnosticResult(
                exitCode = -1,
                output = exception.message
                    ?: exception.javaClass.simpleName
            )
        }
    }

    private data class DiagnosticResult(
        val exitCode: Int,
        val output: String
    )
}