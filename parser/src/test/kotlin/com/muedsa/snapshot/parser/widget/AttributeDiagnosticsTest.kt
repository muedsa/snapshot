package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.parser.TrackPos
import com.muedsa.snapshot.parser.attr.AttributeValueInfo
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.parser.attr.required.AttrDefine
import com.muedsa.snapshot.parser.token.RawAttr
import com.muedsa.snapshot.rendering.flex.MainAxisAlignment
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AttributeDiagnosticsTest {

    @Test
    fun enum_error_lists_all_available_values() {
        val error = failure("<Row mainAxisAlignment=\"MIDDLE\"/>")
        assertContains(error.message.orEmpty(), "可用值：START, END, CENTER, SPACE_BETWEEN, SPACE_AROUND, SPACE_EVENLY")
    }

    @Test
    fun common_formats_include_actionable_examples() {
        val cases = listOf(
            "<SizedBox width=\"wide\"/>" to "浮点数",
            "<Padding padding=\"(1,2,3)\"/>" to "(左,上,右,下)",
            "<ColoredBox color=\"#xyz\"/>" to "#RRGGBBAA",
            "<Transform matrix=\"(1,2)\"/>" to "列优先",
            "<ClipPath viewBox=\"0 0 -1 10\"/>" to "minX minY width height",
            "<Text fontStyle=\"HEAVY\"/>" to "BOLD",
            "<Text foregroundColor=\"red\" foregroundMode=\"INVALID\"/>" to "STROKE_AND_FILL",
        )
        for ((source, expected) in cases) {
            val message = failure(source).message.orEmpty()
            assertContains(message, "格式：", message = source)
            assertContains(message, expected, message = source)
            assertContains(message, "示例：", message = source)
        }
    }

    @Test
    fun missing_required_attribute_also_explains_format() {
        val message = failure("<AspectRatio/>").message.orEmpty()
        assertContains(message, "Attr [aspectRatio] must not be null")
        assertContains(message, "浮点数")
    }

    @Test
    fun nullable_enum_and_renamed_attributes_keep_available_values() {
        assertContains(failure("<UnconstrainedBox constrainedAxis=\"DEPTH\"/>").message.orEmpty(), "HORIZONTAL, VERTICAL")
        assertContains(failure("<Stack fit=\"WRAP\"/>").message.orEmpty(), "LOOSE, EXPAND, PASSTHROUGH")
        assertContains(failure("<Container foregroundGradientType=\"UNKNOWN\"/>").message.orEmpty(), "LINEAR, RADIAL, SWEEP")
    }

    @Test
    fun root_attribute_errors_include_available_values() {
        val error = failure("", "type=\"gif\"")
        assertContains(error.message.orEmpty(), "可用值：png, jpg, webp")
        assertEquals("Snapshot", error.attributeDiagnostic?.elementName)
    }

    @Test
    fun structured_diagnostic_keeps_input_cause_and_multiline_position() {
        val source = "<Snapshot>\n  <Row mainAxisAlignment=\"MIDDLE\"/>\n</Snapshot>"
        val error = assertFailsWith<ParseException> { ParserTest.parse(source).createWidget() }
        val diagnostic = assertNotNull(error.attributeDiagnostic)
        assertEquals("Row", diagnostic.elementName)
        assertEquals("mainAxisAlignment", diagnostic.attributeName)
        assertEquals("MIDDLE", diagnostic.rawValue)
        assertTrue(diagnostic.attributePresent)
        assertEquals(MainAxisAlignment.entries.map { it.name }, diagnostic.valueInfo?.allowedValues)
        assertEquals(source.indexOf("MIDDLE"), error.pos.pos)
        assertEquals(2, error.pos.line)
        assertIs<IllegalArgumentException>(error.cause)
    }

    @Test
    fun missing_valueless_and_empty_values_are_distinguishable() {
        val missing = failure("<AspectRatio/>")
        val valueless = failure("<AspectRatio aspectRatio/>")
        val empty = failure("<AspectRatio aspectRatio=\"\"/>")
        val missingDiagnostic = assertNotNull(missing.attributeDiagnostic)
        val valuelessDiagnostic = assertNotNull(valueless.attributeDiagnostic)
        assertFalse(missingDiagnostic.attributePresent)
        assertTrue(valuelessDiagnostic.attributePresent)
        assertNull(missingDiagnostic.rawValue)
        assertNull(valuelessDiagnostic.rawValue)
        assertEquals("", empty.attributeDiagnostic?.rawValue)
        assertEquals("<Snapshot >".length, missing.pos.pos)
        assertEquals("AspectRatio", missingDiagnostic.elementName)
        assertContains(missing.message.orEmpty(), "未提供属性")
        assertContains(valueless.message.orEmpty(), "未赋值")
    }

    @Test
    fun nested_inline_error_retains_its_own_tag_and_value_position() {
        val source = "<Snapshot><Text><Emoji url=\"https://example.com/image.png\" fit=\"BIG\"/></Text></Snapshot>"
        val error = assertFailsWith<ParseException> { ParserTest.parse(source).createWidget() }
        assertEquals("Emoji", error.attributeDiagnostic?.elementName)
        assertEquals(source.indexOf("BIG"), error.pos.pos)
        assertContains(error.message.orEmpty(), "SCALE_DOWN")
    }

    @Test
    fun restricted_choices_and_compound_formats_are_not_confused_with_full_enums() {
        val clipFit = failure("<ClipPath fit=\"COVER\"/>")
        assertEquals(listOf("FILL", "CONTAIN"), clipFit.attributeDiagnostic?.valueInfo?.allowedValues)
        assertEquals(listOf("NON_ZERO", "EVEN_ODD"), failure("<ClipPath fillRule=\"WINDING\"/>").attributeDiagnostic?.valueInfo?.allowedValues)
        assertContains(failure("<Border border=\"1 DASHED red\"/>").message.orEmpty(), "宽度 样式 颜色")
        assertContains(failure("<Border boxShadow=\"bad\"/>").message.orEmpty(), "offsetX offsetY")
        assertContains(failure("<Image dataUri=\"not-a-data-uri\"/>").message.orEmpty(), "data:image/<类型>;base64")
    }

    @Test
    fun renamed_attribute_keeps_metadata_in_the_diagnostic() {
        val error = failure("<Container foregroundGradientBegin=\"middle\" foregroundGradientType=\"LINEAR\" foregroundGradientColors=\"red,blue\"/>")
        assertEquals("foregroundGradientBegin", error.attributeDiagnostic?.attributeName)
        assertEquals(CommonAttrDefine.ALIGNMENT_N.valueInfo, error.attributeDiagnostic?.valueInfo)
    }

    @Test
    fun custom_attribute_can_supply_help_and_original_cause() {
        val cause = IllegalArgumentException("必须填写偶数")
        val info = AttributeValueInfo("偶数", listOf("2", "4"))
        val attr = object : AttrDefine<Int>("custom") {
            override val valueInfo = info
            override fun parseValue(valueStr: String?): Int = throw cause
            override fun copyWith(name: String): AttrDefine<Int> = error("测试不调用")
        }
        val error = assertFailsWith<ParseException> {
            WidgetParser.parseAttrValue(attr, mapOf("custom" to RawAttr("custom", "3")))
        }
        assertSame(cause, error.cause)
        assertEquals(info, error.attributeDiagnostic?.valueInfo)
        assertNull(error.attributeDiagnostic?.elementName)
        assertContains(error.message.orEmpty(), "必须填写偶数")
        assertContains(error.message.orEmpty(), "格式：偶数")
    }

    @Test
    fun existing_parse_exception_and_non_attribute_errors_are_not_rewritten() {
        val original = ParseException(TrackPos(5, 1, 6), "自定义错误")
        val attr = object : AttrDefine<String>("custom") {
            override fun parseValue(valueStr: String?): String = throw original
            override fun copyWith(name: String): AttrDefine<String> = error("测试不调用")
        }
        val actual = assertFailsWith<ParseException> {
            WidgetParser.parseAttrValue(attr, mapOf("custom" to RawAttr("custom", "test")))
        }
        assertSame(original, actual)
        assertNull(actual.attributeDiagnostic)
        assertNull(failure("<Unknown/>").attributeDiagnostic)
    }

    @Test
    fun custom_attribute_without_help_keeps_full_input_but_shortens_the_preview() {
        val value = "first\nsecond\t" + "x".repeat(200)
        val attr = object : AttrDefine<String>("custom") {
            override fun parseValue(valueStr: String?): String = error("无法处理该输入")
            override fun copyWith(name: String): AttrDefine<String> = error("测试不调用")
        }
        val error = assertFailsWith<ParseException> {
            WidgetParser.parseAttrValue(attr, mapOf("custom" to RawAttr("custom", value)))
        }
        val diagnostic = assertNotNull(error.attributeDiagnostic)
        assertNull(diagnostic.valueInfo)
        assertEquals(value, diagnostic.rawValue)
        assertContains(error.message.orEmpty(), "first\\nsecond\\t")
        assertContains(error.message.orEmpty(), "…")
        assertFalse(error.message.orEmpty().contains(value))
    }

    private fun failure(child: String, rootAttrs: String = ""): ParseException =
        assertFailsWith<ParseException> {
            ParserTest.parse("<Snapshot $rootAttrs>$child</Snapshot>").createWidget()
        }
}
