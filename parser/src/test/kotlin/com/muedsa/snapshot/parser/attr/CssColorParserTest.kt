package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.parser.Parser
import com.muedsa.snapshot.parser.attr.nullable.NullableBorderSideAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableBoxShadowAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableColorListAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableTextShadowAttrDefine
import com.muedsa.snapshot.parser.token.RawAttr
import com.muedsa.snapshot.widget.Container
import org.jetbrains.skia.Color
import java.io.StringReader
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class CssColorParserTest {

    private val color = ColorAttrDefine("color")

    @Test
    fun parses_css_hex_forms_in_rgba_order() {
        assertEquals(0xFFFF0000.toInt(), parse("#f00"))
        assertEquals(0x88FF0000.toInt(), parse("#f008"))
        assertEquals(0xFFFF0000.toInt(), parse("#FF0000"))
        assertEquals(0x80FF0000.toInt(), parse("#ff000080"))
        assertEquals(0x000000FF, parse("#0000ff00"))
    }

    @Test
    fun parses_css_named_colors_and_transparent() {
        assertEquals(Color.RED, parse("red"))
        assertEquals(0xFF663399.toInt(), parse("RebeccaPurple"))
        assertEquals(0xFFF0F8FF.toInt(), parse("aliceblue"))
        assertEquals(Color.TRANSPARENT, parse("transparent"))
        assertEquals(parse("gray"), parse("grey"))
    }

    @Test
    fun parses_legacy_and_modern_rgb_functions() {
        assertEquals(Color.RED, parse("rgb(255, 0, 0)"))
        assertEquals(0x80FF0000.toInt(), parse("rgba(255, 0, 0, .5)"))
        assertEquals(0x80FF0000.toInt(), parse("rgb(100% 0% 0% / 50%)"))
        assertEquals(0xFF800000.toInt(), parse("rgb(50% 0 0)"))
        assertEquals(Color.RED, parse("rgb(300 -1 0)"))
    }

    @Test
    fun parses_legacy_and_modern_hsl_functions() {
        assertEquals(Color.GREEN, parse("hsl(120, 100%, 50%)"))
        assertEquals(0x8000FF00.toInt(), parse("hsla(120, 100%, 50%, 50%)"))
        assertEquals(Color.BLUE, parse("hsl(240deg 100% 50%)"))
        assertEquals(Color.BLUE, parse("hsl(0.6666666667turn 100% 50%)"))
        assertEquals(Color.RED, parse("hsl(-360 100% 50%)"))
    }

    @Test
    fun rejects_invalid_or_unsupported_values() {
        for (value in listOf("#12", "#abcdx", "#123456789", "rgb(1, 2)", "rgb(1 2 3 /)",
            "rgb(NaN 0 0)", "hsl(120 1 50%)", "hsl(0 100% 50% / NaN)",
            "hsl(1e308turn 100% 50%)", "currentColor", "lab(50% 0 0)")) {
            assertFailsWith<IllegalArgumentException>(value) { parse(value) }
        }
    }

    @Test
    fun composite_color_fields_keep_commas_and_spaces_inside_functions() {
        val border = requireNotNull(NullableBorderSideAttrDefine("border").parseValue(
            RawAttr("border", "2 SOLID rgb(255, 0, 0)"),
        ))
        assertEquals(Color.RED, border.color)

        val shadows = requireNotNull(NullableBoxShadowAttrDefine("boxShadow").parseValue(
            RawAttr("boxShadow", "1 2 rgb(255, 0, 0), 3 4 hsla(120, 100%, 50%, 50%)"),
        ))
        assertContentEquals(intArrayOf(Color.RED, 0x8000FF00.toInt()), shadows.map { it.color }.toIntArray())

        val textShadows = requireNotNull(NullableTextShadowAttrDefine("textShadow").parseValue(
            RawAttr("textShadow", "1 2 rgba(255, 0, 0, .5), 3 4 rebeccapurple"),
        ))
        assertContentEquals(intArrayOf(0x80FF0000.toInt(), 0xFF663399.toInt()), textShadows.map { it.color }.toIntArray())

        val colors = NullableColorListAttrDefine("gradientColors").parseValue(
            RawAttr("gradientColors", "rgb(255, 0, 0), hsl(120, 100%, 50%), transparent"),
        )
        assertContentEquals(intArrayOf(Color.RED, Color.GREEN, Color.TRANSPARENT), colors)
    }

    @Test
    fun parser_uses_css_colors_for_root_and_widgets() {
        val snapshot = Parser().parse(StringReader(
            "<Snapshot background=\"rgb(255 0 0 / 50%)\"><Container color=\"rebeccapurple\" width=\"1\" height=\"1\"/></Snapshot>"
        ))
        assertEquals(0x80FF0000.toInt(), snapshot.background)
        assertEquals(0xFF663399.toInt(), assertIs<Container>(snapshot.createWidget()).color)
    }

    private fun parse(value: String): Int = color.parseValue(RawAttr("color", value))
}
