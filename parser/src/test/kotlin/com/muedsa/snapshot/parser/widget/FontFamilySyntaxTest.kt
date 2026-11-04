package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.widget.text.RichText
import kotlin.test.*

class FontFamilySyntaxTest {
    @Test
    fun text_and_strut_trim_each_family_without_changing_internal_characters() {
        val rich = assertIs<RichText>(ParserTest.parse(
            """<Snapshot><Text fontFamily=" Arial , Noto Sans , My-Font " strutFontFamily=" Arial , Noto Sans , My-Font ">text</Text></Snapshot>"""
        ).createWidget())
        val expected = listOf("Arial", "Noto Sans", "My-Font")
        assertEquals(expected, assertIs<TextSpan>(rich.text).style?.fontFamilies)
        assertEquals(expected, rich.strutStyle?.fontFamilies?.toList())
    }

    @Test
    fun empty_families_report_the_actual_attribute_and_position() {
        for (name in listOf("fontFamily", "strutFontFamily")) {
            for (value in listOf(" ", "Arial,", ",Arial", "Arial, ,Noto Sans")) {
                val source = "<Snapshot><Text $name=\"$value\">text</Text></Snapshot>"
                val error = assertFailsWith<ParseException> { ParserTest.parse(source).createWidget() }
                assertEquals(name, error.attributeDiagnostic?.attributeName)
                assertEquals(value, error.attributeDiagnostic?.rawValue)
                assertEquals(source.indexOf('"') + 1, error.pos.pos)
            }
        }
    }

    @Test
    fun nested_spans_use_the_same_rules_and_preserve_text() {
        val rich = assertIs<RichText>(ParserTest.parse(
            """<Snapshot><Text><Raw fontFamily=" Arial , Noto Sans ">  Keep Me  </Raw></Text></Snapshot>"""
        ).createWidget())
        val span = assertIs<TextSpan>(assertIs<TextSpan>(rich.text).children.single())
        assertEquals(listOf("Arial", "Noto Sans"), span.style?.fontFamilies)
        val text = StringBuffer()
        span.computeToPlainText(text, includePlaceholders = false)
        assertEquals("  Keep Me  ", text.toString())
    }
}
