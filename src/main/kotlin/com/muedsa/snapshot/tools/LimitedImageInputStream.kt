package com.muedsa.snapshot.tools

import java.io.InputStream

class LimitedImageInputStream(
    inputStream: InputStream,
    limit: Int,
) : LimitedInputStream(
    inputStream = inputStream,
    limit = limit
) {
    var headerBufferPos: Int = 0
    val headerBuffer: ByteArray = ByteArray(ImageFormatValidator.NECESSARY_MAGIC_LENGTH)

    override fun read(): Int {
        val result: Int = super.read()
        if (result != -1) {
            if (headerBufferPos in headerBuffer.indices) {
                headerBuffer[headerBufferPos++] = result.toByte()
                if (headerBufferPos == headerBuffer.size) {
                    checkImage()
                }
            }
        }
        return result
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val result: Int = super.read(b, off, len)
        if (result != -1) {
            val start = headerBufferPos
            for (i in 0..<len) {
                val pos = start + i
                if (pos >= headerBuffer.size) {
                    break
                }
                if (pos in headerBuffer.indices) {
                    headerBuffer[headerBufferPos++] = b[off + i]
                    if (headerBufferPos == headerBuffer.size) {
                        checkImage()
                    }
                }
            }
        }
        return result
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun checkImage() {
        check(ImageFormatValidator.valid(headerBuffer)) { "Data stream is not available image format, header hex: ${headerBuffer.toHexString()}" }
    }
}