package com.muedsa.snapshot.tools

import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayInputStream
import java.net.URL
import kotlin.test.Test
import kotlin.test.expect

class LimitedImageInputStreamTest {
    @Test
    fun header_buffer_read_test() {
        val testBytes = ByteArray(26) {
            when (it) {
                // fill jpeg header
                0 -> 0xFF.toByte()
                1 -> 0xD8.toByte()
                2 -> 0xFF.toByte()
                else -> (it + 1).toByte()
            }
        }
        val inputStream = LimitedImageInputStream(ByteArrayInputStream(testBytes), 100)
        inputStream.use {
            it.read()
            expect(testBytes[0]) {
                it.headerBuffer[0]
            }
            it.read(ByteArray(25))
            for (i in 1..<testBytes.size) {
                if (i in it.headerBuffer.indices) {
                    expect(testBytes[i]) {
                        it.headerBuffer[i]
                    }
                }
            }
        }
    }

    @Test
    fun image_format_test() {
        // jpg
        readAll("https://samples-files.com/samples/Images/jpg/480-360-sample.jpg")
        // png
        readAll("https://samples-files.com/samples/Images/jpg/480-360-sample.jpg")
        // webp
        readAll("https://samples-files.com/samples/Images/webp/480-360-sample.webp")

        assertThrows<Throwable> {
            readAll("https://samples-files.com/samples/Images/bmp/480-360-sample.bmp")
        }
    }

    private fun readAll(url: String) {
        LimitedImageInputStream(URL(url).openStream(), Int.MAX_VALUE).use {
            it.readAllBytes()
        }
    }
}