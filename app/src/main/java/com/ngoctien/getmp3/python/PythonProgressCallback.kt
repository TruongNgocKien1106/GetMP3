package com.ngoctien.getmp3.python

interface PythonProgressCallback {
    fun onProgress(percent: Int, downloadedBytes: Long, totalBytes: Long, speed: Long, eta: Long)
    fun isCancelled(): Boolean = false
}
