package com.muedsa.snapshot.parser

import java.io.Reader
import java.io.StringReader
import kotlin.test.Test
import kotlin.test.assertFailsWith

class CharacterReaderTest {

    @Test
    fun `缓冲区大小必须为正数`() {
        assertFailsWith<IllegalArgumentException> {
            CharacterReader(StringReader(""), 0)
        }
        assertFailsWith<IllegalArgumentException> {
            CharacterReader(StringReader(""), -1)
        }
    }

    @Test
    fun `输入读取器必须支持标记和重置`() {
        val reader = object : Reader() {
            override fun read(buffer: CharArray, offset: Int, length: Int): Int = -1

            override fun close() = Unit
        }

        assertFailsWith<IllegalArgumentException> {
            CharacterReader(reader)
        }
    }
}
