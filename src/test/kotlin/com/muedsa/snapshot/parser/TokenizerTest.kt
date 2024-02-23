package com.muedsa.snapshot.parser

import com.muedsa.snapshot.parser.token.Token
import com.muedsa.snapshot.parser.token.Tokenizer
import com.muedsa.snapshot.parser.widget.SnapshotParser
import org.junit.jupiter.api.assertThrows
import java.io.StringReader
import kotlin.test.Test

class TokenizerTest {

    @Test
    fun null_char_test() {
        assertThrows<Throwable> {
            parseOnce(Char.MIN_VALUE.toString())
        }
    }

    @Test
    fun cdata_test() {
        val data = "a <= b"
        val token: Token = parseOnce("<![CDATA[$data]]>")
        assert(token is Token.CDATA)
        assert(data == (token as Token.CDATA).data)
    }

    @Test
    fun first_tag_test() {
        println("### First is ${SnapshotParser.id}")
        parseUntilEOF("<${SnapshotParser.id} aaa=1 bbb=2>123</${SnapshotParser.id}>")

        println("### First not is ${SnapshotParser.id}")
        assertThrows<Throwable> {
            parseUntilEOF("<Container aaa=1 bbb=2>123</Container>")
        }

        println("### Second is ${SnapshotParser.id}")
        assertThrows<Throwable> {
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
        println("${token.type}: ${token.toStringWithPos()}")
        return token
    }

    private fun parseUntilEOF(text: String) {
        val reader = StringReader(text)
        val tokenizer = Tokenizer(reader)
        do {
            val token: Token = tokenizer.read()
            println("${token.type}: ${token.toStringWithPos()}")
        } while (token !is Token.EOF)
    }

    private fun warpFirstTag(text: String) = "<${SnapshotParser.id}>$text</${SnapshotParser.id}>"
}