package com.muedsa.snapshot.parser

import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.text.RichText
import java.io.StringReader
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class ParserCommentTest {

    @Test
    fun comments_are_ignored_before_inside_and_after_root() {
        val snapshot = parse(
            "<!-- Header --><!----><Snapshot><!-- before child --><Container width=\"10\" height=\"20\"/><!-- after child --></Snapshot><!-- Footer -->"
        )

        assertEquals(1, snapshot.children.size)
        val container = assertIs<Container>(snapshot.createWidget())
        assertEquals(10f, container.width)
        assertEquals(20f, container.height)
    }

    @Test
    fun comments_disappear_from_text_but_cdata_remains_literal() {
        val snapshot = parse(
            "<Snapshot><Text>前<!-- <Image/> & 注释 -->后<![CDATA[<!-- 原文 -->]]></Text></Snapshot>"
        )
        val text = assertIs<RichText>(snapshot.createWidget())
        val plainText = StringBuffer()
        text.text.computeToPlainText(plainText, false)

        assertEquals("前后<!-- 原文 -->", plainText.toString())
    }

    @Test
    fun unterminated_comment_fails_at_eof() {
        for (input in listOf("<!-- Header", "<Snapshot><!-- Header")) {
            val error = assertFailsWith<ParseException> { parse(input) }
            assertContains(error.message.orEmpty(), "input state [COMMENT]")
        }
    }

    @Test
    fun unsupported_markup_declaration_still_fails() {
        val error = assertFailsWith<ParseException> { parse("<!DOCTYPE html><Snapshot/>") }
        assertContains(error.message.orEmpty(), "input state [MARKUP_DECLARATION_OPEN]")
    }

    private fun parse(input: String): SnapshotElement = Parser().parse(StringReader(input))
}
