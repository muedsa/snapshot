package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.widget.text.RichText
import org.jetbrains.skia.FontEdging
import org.jetbrains.skia.FontHinting
import org.jetbrains.skia.paragraph.BaselineMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TextStyleOptionsParserTest {

    @Test
    fun parses_extended_text_style_options() {
        val richText = assertIs<RichText>(
            ParserTest.parse(
                """
                <Snapshot>
                    <Text height="1.5"
                          topRatio="0.7"
                          letterSpacing="1.25"
                          wordSpacing="2.5"
                          locale="zh-CN"
                          baselineMode="IDEOGRAPHIC"
                          fontEdging="ANTI_ALIAS"
                          fontHinting="FULL"
                          subpixel="true">文本</Text>
                </Snapshot>
                """.trimIndent()
            ).createWidget()
        )

        val style = requireNotNull(assertIs<TextSpan>(richText.text).style)
        assertEquals(1.5f, style.height)
        assertEquals(0.7f, style.topRatio)
        assertEquals(1.25f, style.letterSpacing)
        assertEquals(2.5f, style.wordSpacing)
        assertEquals("zh-CN", style.locale)
        assertEquals(BaselineMode.IDEOGRAPHIC, style.baselineMode)
        assertEquals(FontEdging.ANTI_ALIAS, style.fontEdging)
        assertEquals(FontHinting.FULL, style.fontHinting)
        assertTrue(requireNotNull(style.subpixel))
    }

    @Test
    fun nested_text_and_raw_support_extended_style_options() {
        val richText = assertIs<RichText>(
            ParserTest.parse(
                """
                <Snapshot>
                    <Text>父级<Text letterSpacing="3">子级</Text><Raw wordSpacing="4"> 原始文本 </Raw></Text>
                </Snapshot>
                """.trimIndent()
            ).createWidget()
        )

        val root = assertIs<TextSpan>(richText.text)
        val textSpans = root.children.map { assertIs<TextSpan>(it) }
        val nestedText = textSpans.first { it.style?.letterSpacing == 3f }
        val rawText = textSpans.first { it.style?.wordSpacing == 4f }
        assertEquals(3f, requireNotNull(nestedText.style).letterSpacing)
        assertEquals(4f, requireNotNull(rawText.style).wordSpacing)
        val plainText = StringBuffer()
        root.computeToPlainText(plainText, includePlaceholders = false)
        assertTrue(plainText.toString().contains("子级 原始文本 "))
    }

    @Test
    fun absent_options_keep_style_null_and_explicit_false_is_preserved() {
        val plainText = assertIs<RichText>(
            ParserTest.parse("<Snapshot><Text>默认文本</Text></Snapshot>").createWidget()
        )
        assertNull(assertIs<TextSpan>(plainText.text).style)

        val subpixelText = assertIs<RichText>(
            ParserTest.parse("<Snapshot><Text subpixel=\"false\">关闭子像素</Text></Snapshot>").createWidget()
        )
        val style = requireNotNull(assertIs<TextSpan>(subpixelText.text).style)
        assertFalse(requireNotNull(style.subpixel))

        val enabledText = assertIs<RichText>(
            ParserTest.parse("<Snapshot><Text subpixel=\"TRUE\">开启子像素</Text></Snapshot>").createWidget()
        )
        val enabledStyle = requireNotNull(assertIs<TextSpan>(enabledText.text).style)
        assertTrue(requireNotNull(enabledStyle.subpixel))
    }

    @Test
    fun rejects_invalid_extended_text_style_options() {
        listOf(
            "<Snapshot><Text height=\"invalid\">text</Text></Snapshot>",
            "<Snapshot><Text topRatio=\"invalid\">text</Text></Snapshot>",
            "<Snapshot><Text letterSpacing=\"invalid\">text</Text></Snapshot>",
            "<Snapshot><Text wordSpacing=\"invalid\">text</Text></Snapshot>",
            "<Snapshot><Text baselineMode=\"INVALID\">text</Text></Snapshot>",
            "<Snapshot><Text fontEdging=\"INVALID\">text</Text></Snapshot>",
            "<Snapshot><Text fontHinting=\"INVALID\">text</Text></Snapshot>",
        ).forEach { text ->
            assertFailsWith<ParseException>(text) {
                ParserTest.parse(text).createWidget()
            }
        }
    }
}
