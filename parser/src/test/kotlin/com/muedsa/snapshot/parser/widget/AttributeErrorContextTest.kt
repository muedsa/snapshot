package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class AttributeErrorContextTest {

    @Test
    fun malformed_number_reports_attribute_name_and_value_position() {
        val source = "<Snapshot>\n  <SizedBox width=\"bad\"/>\n</Snapshot>"
        val exception = assertFailsWith<ParseException> {
            ParserTest.parse(source).createWidget()
        }

        assertContains(exception.message.orEmpty(), "Attr [width] value is invalid")
        assertEquals(source.indexOf("bad"), exception.pos.pos)
        assertEquals(2, exception.pos.line)
        assertEquals(source.indexOf("bad") - source.lastIndexOf('\n', source.indexOf("bad")), exception.pos.column)
        assertIs<NumberFormatException>(exception.cause)
    }

    @Test
    fun invalid_enum_reports_attribute_name_and_value_position() {
        val source = "<Snapshot><Row mainAxisAlignment=\"MIDDLE\"/></Snapshot>"
        val exception = assertFailsWith<ParseException> {
            ParserTest.parse(source).createWidget()
        }

        assertContains(exception.message.orEmpty(), "Attr [mainAxisAlignment] value is invalid")
        assertEquals(source.indexOf("MIDDLE"), exception.pos.pos)
        assertIs<IllegalArgumentException>(exception.cause)
    }

    @Test
    fun existing_attribute_message_is_not_prefixed_twice_and_valueless_attr_uses_name_position() {
        val source = "<Snapshot><ConstrainedBox minWidth/></Snapshot>"
        val exception = assertFailsWith<ParseException> {
            ParserTest.parse(source).createWidget()
        }

        assertEquals("Attr [minWidth] value can not be null", exception.message)
        assertEquals(source.indexOf("minWidth"), exception.pos.pos)
    }

    @Test
    fun missing_required_attr_keeps_tag_position_and_specific_message() {
        val source = "<Snapshot><AspectRatio/></Snapshot>"
        val exception = assertFailsWith<ParseException> {
            ParserTest.parse(source).createWidget()
        }

        assertEquals("Attr [aspectRatio] must not be null", exception.message)
        assertEquals(source.indexOf("<AspectRatio"), exception.pos.pos)
    }

    @Test
    fun invalid_root_attr_keeps_specific_message_and_reports_value_position() {
        val source = "<Snapshot type=\"gif\"/>"
        val exception = assertFailsWith<ParseException> {
            ParserTest.parse(source)
        }

        assertContains(exception.message.orEmpty(), "Attr [type] value must be one of")
        assertEquals(source.indexOf("gif"), exception.pos.pos)
    }
}
