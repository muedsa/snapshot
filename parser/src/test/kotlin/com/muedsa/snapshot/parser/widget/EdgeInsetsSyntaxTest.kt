package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.Padding
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class EdgeInsetsSyntaxTest {
    @Test
    fun css_shorthand_expands_one_to_four_values() {
        val cases = mapOf(
            "8" to EdgeInsets(8f, 8f, 8f, 8f),
            "8 16" to EdgeInsets(16f, 8f, 16f, 8f),
            "1 2 3" to EdgeInsets(2f, 1f, 2f, 3f),
            "1 2 3 4" to EdgeInsets(4f, 1f, 2f, 3f),
            " \t.5\n+2. 3e1\t4 " to EdgeInsets(4f, .5f, 2f, 30f),
        )
        for ((value, expected) in cases) assertEquals(expected, parsePadding(value).padding, value)
    }

    @Test
    fun legacy_tuples_keep_their_original_order_and_allow_whitespace() {
        assertEquals(EdgeInsets(2f, 1f, 2f, 1f), parsePadding(" (1, 2) ").padding)
        assertEquals(EdgeInsets(1f, 2f, 3f, 4f), parsePadding("(1,2,3,4)").padding)
        assertEquals(parsePadding("(1, 2, 3, 4)").padding, parsePadding("2 3 4 1").padding)
    }

    @Test
    fun container_padding_and_margin_use_the_same_syntax() {
        val widget = assertIs<Container>(ParserTest.parse(
            """<Snapshot><Container padding="1 2 3" margin="4 5 6 7"/></Snapshot>"""
        ).createWidget())
        assertEquals(EdgeInsets(2f, 1f, 2f, 3f), widget.padding)
        assertEquals(EdgeInsets(7f, 4f, 5f, 6f), widget.margin)
    }

    @Test
    fun malformed_shorthand_retains_attribute_diagnostics() {
        for (value in listOf("", "1 2 3 4 5", "1,2,3,4", "(1,2,3)", "(1,2", "1,2)", "(1,,2,3)", "NaN", "1e100", "8px", "(1 2)")) {
            val source = "<Snapshot><Padding padding=\"$value\"/></Snapshot>"
            val error = assertFailsWith<ParseException>(value) { ParserTest.parse(source).createWidget() }
            assertEquals("padding", error.attributeDiagnostic?.attributeName)
            val expectedPosition = if (value.isEmpty()) source.indexOf("padding=")
                else source.indexOf(value, source.indexOf("padding=") + 9)
            assertEquals(expectedPosition, error.pos.pos)
        }
    }

    private fun parsePadding(value: String): Padding = assertIs<Padding>(ParserTest.parse(
        "<Snapshot><Padding padding=\"$value\"/></Snapshot>"
    ).createWidget())
}
