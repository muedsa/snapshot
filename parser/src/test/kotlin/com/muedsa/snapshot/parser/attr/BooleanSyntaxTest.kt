package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.parser.attr.nullable.NullableBooleanAttrDefine
import com.muedsa.snapshot.parser.token.RawAttr
import com.muedsa.snapshot.parser.widget.WidgetParser
import com.muedsa.snapshot.widget.text.RichText
import kotlin.test.*

class BooleanSyntaxTest {
    @Test
    fun missing_bare_and_assigned_values_have_distinct_meanings_after_copy_with() {
        val definitions = listOf(BooleanAttrDefine("flag"), BooleanAttrDefine("flag", true), NullableBooleanAttrDefine("flag"))
        for (original in definitions) {
            for (definition in listOf(original, original.copyWith("renamed"))) {
                assertEquals(original.defaultValue, definition.parseValue(null as RawAttr?))
                assertEquals(true, definition.parseValue(RawAttr(definition.name)))
                for (value in listOf("true", " TRUE ", "TrUe")) {
                    assertEquals(true, definition.parseValue(RawAttr(definition.name, value)))
                }
                for (value in listOf("false", " FALSE ", "FaLsE")) {
                    assertEquals(false, definition.parseValue(RawAttr(definition.name, value)))
                }
            }
        }
    }

    @Test
    fun invalid_values_preserve_raw_input_and_expose_choices() {
        for (definition in listOf(BooleanAttrDefine("flag"), NullableBooleanAttrDefine("flag").copyWith("renamed"))) {
            for (value in listOf("", " ", "tru", "yes", "0", "1", "null", "true false")) {
                val error = assertFailsWith<ParseException>(value) {
                    WidgetParser.parseAttrValue(definition, mapOf(definition.name to RawAttr(definition.name, value)))
                }
                val diagnostic = assertNotNull(error.attributeDiagnostic)
                assertEquals(definition.name, diagnostic.attributeName)
                assertTrue(diagnostic.attributePresent)
                assertEquals(value, diagnostic.rawValue)
                assertEquals(listOf("true", "false"), diagnostic.valueInfo?.allowedValues)
            }
        }
    }

    @Test
    fun tokenizer_and_widgets_apply_bare_boolean_values() {
        assertFalse(ParserTest.parse("<Snapshot><SizedBox/></Snapshot>").debug)
        assertTrue(ParserTest.parse("<Snapshot debug><SizedBox/></Snapshot>").debug)
        assertFalse(ParserTest.parse("<Snapshot debug=\" FALSE \"><SizedBox/></Snapshot>").debug)
        val text = assertIs<RichText>(ParserTest.parse("<Snapshot><Text softWrap subpixel decoration=\"underline\" decorationGaps>text</Text></Snapshot>").createWidget())
        assertTrue(text.softWrap)
        val style = assertNotNull(assertIs<TextSpan>(text.text).style)
        assertEquals(true, style.subpixel)
        assertTrue(assertNotNull(style.decorationStyle).hasGaps())
        val inherited = assertIs<RichText>(ParserTest.parse("<Snapshot><Text subpixel=\"false\"><Text>child</Text></Text></Snapshot>").createWidget())
        assertNull(assertIs<TextSpan>(assertIs<TextSpan>(inherited.text).children.single()).style?.subpixel)
    }

    @Test
    fun empty_and_misspelled_boolean_attributes_fail_at_the_source() {
        for ((attribute, value) in listOf("softWrap" to "", "subpixel" to "tru")) {
            val source = "<Snapshot><Text $attribute=\"$value\">text</Text></Snapshot>"
            val error = assertFailsWith<ParseException> { ParserTest.parse(source).createWidget() }
            assertEquals(attribute, error.attributeDiagnostic?.attributeName)
            assertEquals(value, error.attributeDiagnostic?.rawValue)
            assertEquals("Text", error.attributeDiagnostic?.elementName)
        }
    }
}
