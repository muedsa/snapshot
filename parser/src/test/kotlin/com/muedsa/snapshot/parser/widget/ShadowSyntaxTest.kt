package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.material.ELEVATION_MAP
import com.muedsa.snapshot.paint.decoration.BoxDecoration
import com.muedsa.snapshot.paint.decoration.BoxShadow
import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.widget.DecoratedBox
import com.muedsa.snapshot.widget.text.RichText
import org.jetbrains.skia.Color
import org.jetbrains.skia.FilterBlurMode
import kotlin.test.*

class ShadowSyntaxTest {
    @Test
    fun box_radii_follow_numeric_order_independently_of_color_and_style() {
        for (value in listOf("0 2 red 4", "0 2 4 red", "0 2 normal red 4", "0 2e0 rgb(255,0,0) 4e0 normal")) {
            assertEquals(BoxShadow(Color.RED, Offset(0f, 2f), 4f), parseBox(value).single())
        }
        for (value in listOf("0 2 4 -1 red inner", "0 2 inner red 4 -1", "0 2 4 red -1 inner", "0 2 4 inner red -1")) {
            assertEquals(BoxShadow(Color.RED, Offset(0f, 2f), 4f, -1f, FilterBlurMode.INNER), parseBox(value).single())
        }
    }

    @Test
    fun text_sigma_preserves_its_semantics_and_accepts_scientific_notation() {
        for (value in listOf("0 2 red 4", "0 2 4 red", "0e0 2e0 rgb(255,0,0) 4e0")) {
            val shadow = parseText(value).single()
            assertEquals(Color.RED, shadow.color)
            assertEquals(0f, shadow.offsetX)
            assertEquals(2f, shadow.offsetY)
            assertEquals(4.0, shadow.blurSigma)
        }
        assertEquals(0.123456789, parseText("0 0 0.123456789").single().blurSigma)
        assertNotEquals(parseBox("0 2 red 4").single().blurSigma.toDouble(), parseText("0 2 red 4").single().blurSigma)
    }

    @Test
    fun none_elevation_and_multiple_shadows_keep_their_boundaries() {
        assertTrue(parseBox(" NoNe ").isEmpty())
        assertTrue(parseText(" NoNe ").isEmpty())
        assertContentEquals(ELEVATION_MAP[2], parseBox(" elevation-2 "))
        val boxes = parseBox("0 2 rgb(255,0,0) 4,1 3 blue 2")
        assertEquals(listOf(Color.RED, Color.BLUE), boxes.map { it.color })
        assertEquals(listOf(4f, 2f), boxes.map { it.blurRadius })
        val text = parseText("0 2 rgb(255,0,0) 4,1 3 blue 2")
        assertEquals(listOf(Color.RED, Color.BLUE), text.map { it.color })
    }

    @Test
    fun ambiguous_or_non_finite_shadows_report_the_attribute() {
        val shared = listOf("", "0", "0 0 red blue", "0 0 NaN", "NaN 0", "0 0 -1", "0 0 1e999", "none,0 0", "0 0,", "0 0,,1 1")
        for (value in shared + listOf("0 0 normal inner", "0 0 1 2 3", "elevation-2,0 0", "0 0 inset")) {
            val error = assertFailsWith<ParseException>(value) {
                ParserTest.parse("<Snapshot><Container foregroundBoxShadow=\"$value\"/></Snapshot>").createWidget()
            }
            assertEquals("foregroundBoxShadow", error.attributeDiagnostic?.attributeName)
            assertEquals(value, error.attributeDiagnostic?.rawValue)
        }
        for (value in shared + listOf("0 0 1 2", "elevation-2")) {
            val error = assertFailsWith<ParseException>(value) { parseText(value) }
            assertEquals("textShadow", error.attributeDiagnostic?.attributeName)
        }
    }

    private fun parseBox(value: String): Array<BoxShadow> {
        val box = assertIs<DecoratedBox>(ParserTest.parse("<Snapshot><Border boxShadow=\"$value\"/></Snapshot>").createWidget())
        return assertNotNull(assertIs<BoxDecoration>(box.decoration).boxShadow)
    }

    private fun parseText(value: String) = assertNotNull(assertIs<TextSpan>(assertIs<RichText>(ParserTest.parse(
        "<Snapshot><Text textShadow=\"$value\">text</Text></Snapshot>"
    ).createWidget()).text).style?.shadows)
}
