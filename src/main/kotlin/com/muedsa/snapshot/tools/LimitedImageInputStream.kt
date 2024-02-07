package com.muedsa.snapshot.tools

import java.io.InputStream

class LimitedImageInputStream(
    inputStream: InputStream,
    limit: Int,
) : LimitedInputStream(
    inputStream = inputStream,
    limit = limit
) {
    val headerBuffer: ByteArray = ByteArray(ImageFormatValidator.NECESSARY_MAGIC_LENGTH)

    override fun read(): Int {
        val result: Int = super.read()
        if (result != -1) {
            val pos = limit - left - 1
            if (pos in headerBuffer.indices) {
                headerBuffer[pos] = result.toByte()
                if (pos == headerBuffer.size - 1) {
                    checkImage()
                }
            }
        }
        return result
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val result: Int = super.read(b, off, len)
        if (result != -1) {
            val start = limit - left - len
            for (i in 0..<len) {
                val pos = start + i
                if (pos >= headerBuffer.size) {
                    break
                }
                if (pos in headerBuffer.indices) {
                    headerBuffer[pos] = b[off + i]
                    if (pos == headerBuffer.size - 1) {
                        checkImage()
                    }
                }
            }
        }
        return result
    }

    private fun checkImage() {
        check(ImageFormatValidator.valid(headerBuffer)) { "Data stream is not available image format" }
    }
}