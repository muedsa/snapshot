package com.muedsa.snapshot.parser

import com.muedsa.snapshot.parser.token.Token
import com.muedsa.snapshot.parser.token.Tokenizer
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
        println("### First is ${Tag.SNAPSHOT.id}")
        parseUntilEOF("<${Tag.SNAPSHOT.id} aaa=1 bbb=2>123</${Tag.SNAPSHOT.id}>")

        println("### First not is ${Tag.SNAPSHOT.id}")
        assertThrows<Throwable> {
            parseUntilEOF("<${Tag.CONTAINER.id} aaa=1 bbb=2>123</${Tag.CONTAINER.id}>")
        }

        println("### Second is ${Tag.SNAPSHOT.id}")
        assertThrows<Throwable> {
            parseUntilEOF(
                "<${Tag.SNAPSHOT.id} aaa=1 bbb=2>" +
                        "<${Tag.SNAPSHOT.id} aaa=1 bbb=2>123</${Tag.SNAPSHOT.id}>" +
                        "</${Tag.SNAPSHOT.id}>"
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

    private fun warpFirstTag(text: String) = "<${Tag.SNAPSHOT.id}>$text</${Tag.SNAPSHOT.id}>"
}