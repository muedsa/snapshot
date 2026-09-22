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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TextShadowParserTest {

    @Test
    fun parses_multiple_text_shadows_and_renders_them() {
        val richText = assertIs<RichText>(
            ParserTest.parse(
                """
                <Snapshot>
                    <Text color="#FF0000FF"
                          fontSize="40"
                          fontFamily="$testFontFamily"
                          textShadow="6 0 0 #FFFF0000,-2 2 1.5 #8000FF00">文本阴影展示</Text>
                </Snapshot>
                """.trimIndent()
            ).createWidget()
        )

        val shadows = requireNotNull(assertIs<TextSpan>(richText.text).style?.shadows)
        assertEquals(2, shadows.size)
        assertEquals(Color.RED, shadows[0].color)
        assertEquals(6f, shadows[0].offsetX)
        assertEquals(0f, shadows[0].offsetY)
        assertEquals(0.0, shadows[0].blurSigma)
        assertEquals(0x8000FF00.toInt(), shadows[1].color)
        assertEquals(-2f, shadows[1].offsetX)
        assertEquals(2f, shadows[1].offsetY)
        assertEquals(1.5, shadows[1].blurSigma)

        val pixels = snapshotPixels(background = Color.TRANSPARENT) { attach(richText) }
        val redPixels = pixels.countPixels { color -> color.isReddish() }
        assertTrue(redPixels > 40, "红色文本阴影应产生可见像素，实际像素数为 $redPixels")
    }

    @Test
    fun uses_shadow_defaults_and_supports_none() {
        val defaultShadowText = assertIs<RichText>(
            ParserTest.parse(
                "<Snapshot><Text textShadow=\"1 2\">text</Text></Snapshot>"
            ).createWidget()
        )
        val shadow = requireNotNull(assertIs<TextSpan>(defaultShadowText.text).style?.shadows).single()
        assertEquals(Color.BLACK, shadow.color)
        assertEquals(1f, shadow.offsetX)
        assertEquals(2f, shadow.offsetY)
        assertEquals(0.0, shadow.blurSigma)

        val noShadowText = assertIs<RichText>(
            ParserTest.parse(
                "<Snapshot><Text textShadow=\"1 1\"><Text textShadow=\"NONE\">nested</Text></Text></Snapshot>"
            ).createWidget()
        )
        val nested = assertIs<TextSpan>(assertIs<TextSpan>(noShadowText.text).children.single())
        assertTrue(requireNotNull(nested.style?.shadows).isEmpty())
    }

    @Test
    fun text_shadow_is_available_to_all_inline_span_tags() {
        val richText = assertIs<RichText>(
            ParserTest.parse(
                "<Snapshot><Text><Text textShadow=\"1 0\">text</Text>" +
                        "<Raw textShadow=\"2 0\"> raw </Raw>" +
                        "<WidgetSpan textShadow=\"3 0\"><SizedBox/></WidgetSpan>" +
                        "</Text></Snapshot>"
            ).createWidget()
        )

        val children = assertIs<TextSpan>(richText.text).children
        assertEquals(1f, assertIs<TextSpan>(children[0]).style?.shadows?.single()?.offsetX)
        assertEquals(2f, assertIs<TextSpan>(children[1]).style?.shadows?.single()?.offsetX)
        assertEquals(3f, assertIs<WidgetSpan>(children[2]).style?.shadows?.single()?.offsetX)
    }

    @Test
    fun rejects_invalid_text_shadows() {
        listOf(
            "<Snapshot><Text textShadow>text</Text></Snapshot>",
            "<Snapshot><Text textShadow=\"1\">text</Text></Snapshot>",
            "<Snapshot><Text textShadow=\"1 2 3 #FF000000 extra\">text</Text></Snapshot>",
            "<Snapshot><Text textShadow=\"NaN 0\">text</Text></Snapshot>",
            "<Snapshot><Text textShadow=\"0 0 -1\">text</Text></Snapshot>",
            "<Snapshot><Text textShadow=\"0 0 1 2\">text</Text></Snapshot>",
            "<Snapshot><Text textShadow=\"0 0 #FF000000 #FFFFFFFF\">text</Text></Snapshot>",
            "<Snapshot><Text textShadow=\"0 0 UNKNOWN\">text</Text></Snapshot>",
            "<Snapshot><Text textShadow=\"NONE,0 0\">text</Text></Snapshot>",
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
