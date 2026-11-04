package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.paint.decoration.BoxDecoration
import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.text.RichText
import kotlin.test.*

class ListSyntaxTest {
    @Test
    fun gradient_stops_support_both_separators_for_background_and_foreground() {
        for (stops in listOf("0 5e-1 1", "0, 0.5, 1", " 0\t0.5\n1 ")) {
            val box = assertIs<Container>(ParserTest.parse("""
                <Snapshot><Container gradientType="linear" gradientColors="rgb(255 0 0),green,blue" gradientStops="$stops"
                  foregroundGradientType="linear" foregroundGradientColors="red,green,blue" foregroundGradientStops="$stops"/></Snapshot>
            """.trimIndent()).createWidget())
            for (decoration in listOf(box.decoration, box.foregroundDecoration)) {
                assertContentEquals(floatArrayOf(0f, .5f, 1f), assertNotNull(assertIs<BoxDecoration>(decoration).gradient).stops)
            }
        }
    }

    @Test
    fun text_decoration_lists_preserve_aliases_and_none() {
        for (value in listOf("underline line-through", "UNDERLINE, LINE_THROUGH", " underline\nline-through ")) {
            val decoration = assertNotNull(parseText(value).style?.decorationStyle)
            assertTrue(decoration.hasUnderline())
            assertTrue(decoration.hasLineThrough())
            assertFalse(decoration.hasOverline())
        }
        assertFalse(assertNotNull(parseText("none").style?.decorationStyle).hasUnderline())
    }

    @Test
    fun invalid_lists_keep_empty_items_and_duplicate_diagnostics() {
        for (value in listOf("", "0,,1", "0,", ",1", "0 0.5,1", "0 NaN 1")) {
            val error = assertFailsWith<ParseException>(value) {
                ParserTest.parse("<Snapshot><Container gradientType=\"linear\" gradientColors=\"red,blue\" gradientStops=\"$value\"/></Snapshot>").createWidget()
            }
            assertEquals("gradientStops", error.attributeDiagnostic?.attributeName)
        }
        for (value in listOf("", "underline,", ",underline", "underline,,overline", "underline overline,line-through", "underline UNDERLINE", "line-through LINE_THROUGH", "none underline")) {
            val error = assertFailsWith<ParseException>(value) { parseText(value) }
            assertEquals("decoration", error.attributeDiagnostic?.attributeName)
        }
    }

    private fun parseText(value: String): TextSpan = assertIs<TextSpan>(assertIs<RichText>(ParserTest.parse(
        "<Snapshot><Text decoration=\"$value\">text</Text></Snapshot>"
    ).createWidget()).text)
}
