package com.muedsa.snapshot.parser

import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.SizedBox
import com.muedsa.snapshot.widget.text.RichText
import java.io.StringReader
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class ParserTerminationTest {

    @Test
    fun empty_or_whitespace_only_document_is_rejected() {
        listOf("", " \t\r\n").forEach { text ->
            val exception = assertFailsWith<ParseException> { parseWithinTimeout(text) }
            assertContains(exception.message.orEmpty(), "root element [Snapshot]")
        }
    }

    @Test
    fun incomplete_markup_states_fail_at_eof() {
        mapOf(
            "<Snapshot" to "TAG_NAME",
            "<Snapshot background=\"" to "ATTR_VALUE_DOUBLE_QUOTED",
            "<Snapshot><Container/" to "SELF_CLOSING_START_TAG",
            "<Snapshot></" to "END_TAG_OPEN",
        ).forEach { (text, state) ->
            val exception = assertFailsWith<ParseException> { parseWithinTimeout(text) }
            assertContains(exception.message.orEmpty(), "input state [$state]")
        }
    }

    @Test
    fun open_elements_are_closed_at_eof() {
        val snapshot = parseWithinTimeout(
            """
            <Snapshot><Container width="20" height="10"><SizedBox width="5" height="4"/>
            """.trimIndent()
        )

        val container = assertIs<Container>(snapshot.createWidget())
        assertEquals(20f, container.width)
        assertEquals(10f, container.height)
        assertEquals(5f, assertIs<SizedBox>(container.child).width)
    }

    @Test
    fun unmatched_end_tag_is_ignored() {
        val snapshot = parseWithinTimeout(
            """
            <Snapshot></Unknown><Container width="20" height="10"/></Snapshot>
            """.trimIndent()
        )

        assertIs<Container>(snapshot.createWidget())
    }

    @Test
    fun unterminated_cdata_is_emitted_before_eof() {
        val snapshot = parseWithinTimeout("<Snapshot><Text><![CDATA[content until eof")
        val text = assertIs<RichText>(snapshot.createWidget())
        val plainText = StringBuffer()

        text.text.computeToPlainText(plainText, false)

        assertEquals("content until eof", plainText.toString())
    }

    @Test
    fun deep_unclosed_tree_terminates_and_preserves_all_elements() {
        val depth = Parser.MAX_QUEUE_DEPTH + 44
        val input = buildString {
            append("<Snapshot>")
            repeat(depth) { append("<Container>") }
            append("<SizedBox width=\"1\" height=\"1\"/>")
        }

        val snapshot = parseWithinTimeout(input)
        var element: Element = snapshot
        var actualDepth = 0
        while (element.children.isNotEmpty()) {
            element = element.children.single()
            actualDepth++
        }

        assertEquals(depth + 1, actualDepth)
        assertEquals("SizedBox", element.widgetParser.id)
    }

    private fun parseWithinTimeout(text: String): SnapshotElement {
        var result: SnapshotElement? = null
        var failure: Throwable? = null
        val worker = Thread {
            try {
                result = Parser().parse(StringReader(text))
            } catch (t: Throwable) {
                failure = t
            }
        }
        worker.isDaemon = true
        worker.start()
        worker.join(TIMEOUT_MILLIS)

        assertFalse(worker.isAlive, "parser did not terminate for input: $text")
        failure?.let { throw it }
        return assertNotNull(result)
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
