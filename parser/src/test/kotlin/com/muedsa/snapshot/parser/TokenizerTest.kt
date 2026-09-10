package com.muedsa.snapshot.parser

import com.muedsa.snapshot.parser.token.Token
import com.muedsa.snapshot.parser.token.Tokenizer
import com.muedsa.snapshot.parser.widget.SnapshotParser
import kotlin.test.assertFailsWith
import java.io.StringReader
import kotlin.test.Test
import kotlin.test.assertTrue

class TokenizerTest {

    @Test
    fun null_char_test() {
        assertFailsWith<Throwable> {
            parseOnce(Char.MIN_VALUE.toString())
        }
    }

    @Test
    fun cdata_test() {
        val data = "a <= b"
        val token: Token = parseOnce("<![CDATA[$data]]>")
        assertTrue(token is Token.CDATA, "token is Token.CDATA")
        assertTrue(data == (token as Token.CDATA).data, "data == (token as Token.CDATA).data")
    }

    @Test
    fun first_tag_test() {
        parseUntilEOF("<${SnapshotParser.id} aaa=1 bbb=2>123</${SnapshotParser.id}>")

        assertFailsWith<Throwable> {
            parseUntilEOF("<Container aaa=1 bbb=2>123</Container>")
        }

        assertFailsWith<Throwable> {
            parseUntilEOF(
                "<${SnapshotParser.id} aaa=1 bbb=2>" +
                        "<${SnapshotParser.id} aaa=1 bbb=2>123</${SnapshotParser.id}>" +
                        "</${SnapshotParser.id}>"
            )
        }
    }

    private fun parseOnce(text: String): Token {
        val reader = StringReader(warpFirstTag(text))
        val tokenizer = Tokenizer(reader)
        tokenizer.read()
        val token: Token = tokenizer.read()
        return token
    }

    private fun parseUntilEOF(text: String) {
        val reader = StringReader(text)
        val tokenizer = Tokenizer(reader)
        do {
            val token: Token = tokenizer.read()
        } while (token !is Token.EOF)
    }

    private fun warpFirstTag(text: String) = "<${SnapshotParser.id}>$text</${SnapshotParser.id}>"
}