package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.snapshotPixels
import com.muedsa.snapshot.testFontFamily
import com.muedsa.snapshot.widget.text.RichText
import com.muedsa.snapshot.widget.text.WidgetSpan
import org.jetbrains.skia.Color
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.PaintStrokeCap
import org.jetbrains.skia.PaintStrokeJoin
import org.jetbrains.skia.Pixmap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TextPaintParserTest {

    @Test
    fun parses_and_renders_foreground_and_background_paints() {
        val richText = assertIs<RichText>(
            ParserTest.parse(
                """
                <Snapshot>
                    <Text color="#FF0000FF"
                          fontFamily="$testFontFamily"
                          fontSize="40"
                          foregroundColor="#FFFF0000"
                          foregroundMode="STROKE_AND_FILL"
                          foregroundStrokeWidth="2"
                          foregroundStrokeMiter="5"
                          foregroundStrokeCap="ROUND"
                          foregroundStrokeJoin="BEVEL"
                          foregroundAntiAlias="true"
                          backgroundColor="#FFFFFF00">文本画笔</Text>
                </Snapshot>
                """.trimIndent()
            ).createWidget()
        )

        val style = requireNotNull(assertIs<TextSpan>(richText.text).style)
        assertEquals(Color.BLUE, style.color)
        val foreground = requireNotNull(style.foreground)
        assertEquals(Color.RED, foreground.color)
        assertEquals(PaintMode.STROKE_AND_FILL, foreground.mode)
        assertEquals(2f, foreground.strokeWidth)
        assertEquals(5f, foreground.strokeMiter)
        assertEquals(PaintStrokeCap.ROUND, foreground.strokeCap)
        assertEquals(PaintStrokeJoin.BEVEL, foreground.strokeJoin)
        assertTrue(foreground.isAntiAlias)
        assertEquals(Color.YELLOW, requireNotNull(style.background).color)

        val pixels = snapshotPixels(background = Color.TRANSPARENT) { attach(richText) }
        val redPixels = pixels.countPixels { color -> color.isReddish() }
        val yellowPixels = pixels.countPixels { color -> color.isYellowish() }
        assertTrue(redPixels > 100, "前景画笔应产生红色文字像素，实际像素数为 $redPixels")
        assertTrue(yellowPixels > 100, "背景画笔应产生黄色背景像素，实际像素数为 $yellowPixels")
    }

    @Test
    fun paints_are_available_to_all_inline_span_tags() {
        val richText = assertIs<RichText>(
            ParserTest.parse(
                "<Snapshot><Text><Text foregroundColor=\"#FFFF0000\">text</Text>" +
                        "<Raw backgroundColor=\"#FF00FF00\"> raw </Raw>" +
                        "<WidgetSpan foregroundColor=\"#FF0000FF\"><SizedBox/></WidgetSpan>" +
                        "</Text></Snapshot>"
            ).createWidget()
        )

        val children = assertIs<TextSpan>(richText.text).children
        assertEquals(Color.RED, assertIs<TextSpan>(children[0]).style?.foreground?.color)
        assertEquals(Color.GREEN, assertIs<TextSpan>(children[1]).style?.background?.color)
        assertEquals(Color.BLUE, assertIs<WidgetSpan>(children[2]).style?.foreground?.color)
    }

    @Test
    fun foreground_modifiers_require_a_color() {
        listOf(
            "foregroundMode=\"STROKE\"",
            "foregroundStrokeWidth=\"2\"",
            "foregroundStrokeMiter=\"4\"",
            "foregroundStrokeCap=\"ROUND\"",
            "foregroundStrokeJoin=\"ROUND\"",
            "foregroundAntiAlias=\"true\"",
        ).forEach { attr ->
            val source = "<Snapshot><Text $attr>text</Text></Snapshot>"
            assertFailsWith<ParseException>(source) {
                ParserTest.parse(source).createWidget()
            }
        }
    }

    @Test
    fun rejects_invalid_text_paint_options() {
        listOf(
            "<Snapshot><Text foregroundColor=\"invalid\">text</Text></Snapshot>",
            "<Snapshot><Text foregroundColor=\"#FFFF0000\" foregroundMode=\"INVALID\">text</Text></Snapshot>",
            "<Snapshot><Text foregroundColor=\"#FFFF0000\" foregroundStrokeWidth=\"-1\">text</Text></Snapshot>",
            "<Snapshot><Text foregroundColor=\"#FFFF0000\" foregroundStrokeWidth=\"NaN\">text</Text></Snapshot>",
            "<Snapshot><Text foregroundColor=\"#FFFF0000\" foregroundStrokeMiter=\"0\">text</Text></Snapshot>",
            "<Snapshot><Text foregroundColor=\"#FFFF0000\" foregroundStrokeCap=\"INVALID\">text</Text></Snapshot>",
            "<Snapshot><Text foregroundColor=\"#FFFF0000\" foregroundStrokeJoin=\"INVALID\">text</Text></Snapshot>",
            "<Snapshot><Text backgroundColor=\"invalid\">text</Text></Snapshot>",
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

    private fun Int.isYellowish(): Boolean {
        val red = (this ushr 16) and 0xFF
        val green = (this ushr 8) and 0xFF
        val blue = this and 0xFF
        return red > 150 && green > 150 && blue < 100
    }
}
