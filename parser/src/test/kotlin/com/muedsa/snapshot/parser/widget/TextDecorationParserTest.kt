package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.snapshotPixels
import com.muedsa.snapshot.testFontFamily
import com.muedsa.snapshot.widget.text.RichText
import com.muedsa.snapshot.widget.text.WidgetSpan
import org.jetbrains.skia.Color
import org.jetbrains.skia.Pixmap
import org.jetbrains.skia.paragraph.DecorationLineStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TextDecorationParserTest {

    @Test
    fun parses_and_renders_text_decoration() {
        val richText = assertIs<RichText>(
            ParserTest.parse(
                """
                <Snapshot>
                    <Text color="#FF0000FF"
                          fontSize="40"
                          fontFamily="$testFontFamily"
                          decoration="UNDERLINE,LINE_THROUGH"
                          decorationColor="#FFFF0000"
                          decorationLineStyle="WAVY"
                          decorationThickness="2"
                          decorationGaps="false">装饰文本展示</Text>
                </Snapshot>
                """.trimIndent()
            ).createWidget()
        )

        val decoration = requireNotNull(assertIs<TextSpan>(richText.text).style?.decorationStyle)
        assertTrue(decoration.hasUnderline())
        assertFalse(decoration.hasOverline())
        assertTrue(decoration.hasLineThrough())
        assertFalse(decoration.hasGaps())
        assertEquals(Color.RED, decoration.color)
        assertEquals(DecorationLineStyle.WAVY, decoration.lineStyle)
        assertEquals(2f, decoration.thicknessMultiplier)

        val pixels = snapshotPixels(background = Color.TRANSPARENT) { attach(richText) }
        val redPixels = pixels.countPixels { color -> color.isReddish() }
        assertTrue(redPixels > 40, "红色文本装饰线应产生可见像素，实际像素数为 $redPixels")
    }

    @Test
    fun uses_decoration_defaults_and_supports_none() {
        val underlined = assertIs<RichText>(
            ParserTest.parse(
                "<Snapshot><Text decoration=\"UNDERLINE\">text</Text></Snapshot>"
            ).createWidget()
        )
        val defaultDecoration = requireNotNull(assertIs<TextSpan>(underlined.text).style?.decorationStyle)
        assertTrue(defaultDecoration.hasUnderline())
        assertTrue(defaultDecoration.hasGaps())
        assertEquals(Color.BLACK, defaultDecoration.color)
        assertEquals(DecorationLineStyle.SOLID, defaultDecoration.lineStyle)
        assertEquals(1f, defaultDecoration.thicknessMultiplier)

        val noDecoration = assertIs<RichText>(
            ParserTest.parse(
                "<Snapshot><Text decoration=\"UNDERLINE\"><Text decoration=\"NONE\">nested</Text></Text></Snapshot>"
            ).createWidget()
        )
        val nested = assertIs<TextSpan>(assertIs<TextSpan>(noDecoration.text).children.single())
        val none = requireNotNull(nested.style?.decorationStyle)
        assertFalse(none.hasUnderline())
        assertFalse(none.hasOverline())
        assertFalse(none.hasLineThrough())
    }

    @Test
    fun decoration_is_available_to_all_inline_span_tags() {
        val richText = assertIs<RichText>(
            ParserTest.parse(
                "<Snapshot><Text><Text decoration=\"UNDERLINE\">text</Text>" +
                        "<Raw decoration=\"OVERLINE\"> raw </Raw>" +
                        "<WidgetSpan decoration=\"LINE_THROUGH\"><SizedBox/></WidgetSpan>" +
                        "</Text></Snapshot>"
            ).createWidget()
        )

        val children = assertIs<TextSpan>(richText.text).children
        val nestedText = assertIs<TextSpan>(children[0])
        val rawText = assertIs<TextSpan>(children[1])
        val widgetSpan = assertIs<WidgetSpan>(children[2])
        assertTrue(requireNotNull(nestedText.style?.decorationStyle).hasUnderline())
        assertTrue(requireNotNull(rawText.style?.decorationStyle).hasOverline())
        assertTrue(requireNotNull(widgetSpan.style?.decorationStyle).hasLineThrough())
    }

    @Test
    fun rejects_invalid_decoration_attributes() {
        listOf(
            "<Snapshot><Text decoration=\"UNKNOWN\">text</Text></Snapshot>",
            "<Snapshot><Text decoration=\"NONE,UNDERLINE\">text</Text></Snapshot>",
            "<Snapshot><Text decoration=\"UNDERLINE,UNDERLINE\">text</Text></Snapshot>",
            "<Snapshot><Text decoration=\"UNDERLINE\" decorationLineStyle=\"UNKNOWN\">text</Text></Snapshot>",
            "<Snapshot><Text decoration=\"UNDERLINE\" decorationThickness=\"0\">text</Text></Snapshot>",
            "<Snapshot><Text decoration=\"UNDERLINE\" decorationThickness=\"NaN\">text</Text></Snapshot>",
            "<Snapshot><Text decorationColor=\"#FFFF0000\">text</Text></Snapshot>",
        ).forEach { source ->
            assertFailsWith<ParseException>(source) {
                ParserTest.parse(source).createWidget()
            }
        }
    }

    private fun Pixmap.countPixels(predicate: (Int) -> Boolean): Int {
        var count = 0
        for (y in 0 until info.height) {
            for (x in 0 until info.width) {
                val color = getColor(x, y)
                if (((color ushr 24) and 0xFF) != 0 && predicate(color)) count++
            }
        }
        return count
    }

    private fun Int.isReddish(): Boolean {
        val red = (this ushr 16) and 0xFF
        val green = (this ushr 8) and 0xFF
        val blue = this and 0xFF
        return red > green + 60 && red > blue + 60
    }
}
