package com.muedsa.snapshot.tools

import java.io.ByteArrayInputStream
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertFailsWith

class LimitedInputStreamTest {

    @Test
    fun limited_read_test() {
        val testString = "limited_read_test\n"
        val limit = testString.toByteArray().size
        val testBytes = testString.repeat(3).toByteArray()
        val inputStream = LimitedInputStream(ByteArrayInputStream(testBytes), limit)
        inputStream.use {
            it.read()
            it.read(ByteArray(limit - 1))
            assertFailsWith<IOException> { it.read() }
        }

        val inputStream2 = LimitedInputStream(ByteArrayInputStream(testBytes), limit)
        inputStream2.use {
            assertFailsWith<IOException> { it.readBytes() }
        }
    }
}