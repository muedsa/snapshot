package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.paint.decoration.Border
import com.muedsa.snapshot.paint.decoration.BorderSide
import com.muedsa.snapshot.paint.decoration.BoxDecoration
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.widget.Container
import org.jetbrains.skia.Color
import kotlin.test.*

class BorderSyntaxTest {
    @Test
    fun shorthand_preserves_explicit_style_and_css_colors() {
        for (value in listOf("2 red", "2 solid red", "2e0 rgb(255 0 0)", " 2 SOLID rgb(255,0,0) ")) {
            val box = parse("border=\"$value\"")
            val border = assertIs<Border>(assertIs<BoxDecoration>(box.decoration).border)
            val expected = BorderSide(width = 2f, color = Color.RED)
            assertEquals(expected, border.left)
            assertEquals(expected, border.top)
            assertEquals(expected, border.right)
            assertEquals(expected, border.bottom)
        }
    }

    @Test
    fun none_and_shorthand_apply_to_individual_and_foreground_sides() {
        val box = parse("border=\"2 red\" borderLeft=\" NoNe \" foregroundBorder=\"none\" foregroundBorderTop=\"3 blue\"")
        val border = assertIs<Border>(assertIs<BoxDecoration>(box.decoration).border)
        assertEquals(BorderSide.NONE, border.left)
        assertEquals(BorderSide(width = 2f, color = Color.RED), border.top)
        val foreground = assertIs<Border>(assertIs<BoxDecoration>(box.foregroundDecoration).border)
        assertEquals(BorderSide.NONE, foreground.left)
        assertEquals(BorderSide(width = 3f, color = Color.BLUE), foreground.top)
    }

    @Test
    fun incomplete_or_unsupported_borders_report_the_actual_attribute() {
        for (value in listOf("", "red", "2", "red 2", "2px red", "2 dashed red", "none red", "2 solid red extra", "NaN red", "1e99 red")) {
            val error = assertFailsWith<ParseException>(value) { parse("foregroundBorderLeft=\"$value\"") }
            val diagnostic = assertNotNull(error.attributeDiagnostic)
            assertEquals("foregroundBorderLeft", diagnostic.attributeName)
            assertContains(assertNotNull(diagnostic.valueInfo).examples, "2 red")
        }
    }

    private fun parse(attributes: String): Container = assertIs<Container>(ParserTest.parse(
        "<Snapshot><Container $attributes/></Snapshot>"
    ).createWidget())
}
