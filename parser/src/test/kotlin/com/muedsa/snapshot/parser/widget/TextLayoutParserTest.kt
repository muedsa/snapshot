package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.paint.text.TextOverflow
import com.muedsa.snapshot.paint.text.TextWidthBasis
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.widget.text.RichText
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.Direction
import org.jetbrains.skia.paragraph.HeightMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TextLayoutParserTest {

    @Test
    fun parses_rich_text_layout_options() {
        val widget = ParserTest.parse(
            """
            <Snapshot>
                <Text textAlign="RIGHT"
                      textDirection="RTL"
                      softWrap="false"
                      overflow="ELLIPSIS"
                      maxLines="2"
                      textWidthBasis="LONGESTLINE"
                      textHeightMode="DISABLE_ALL">测试文本</Text>
            </Snapshot>
            """.trimIndent()
        ).createWidget()

        val richText = assertIs<RichText>(widget)
        assertEquals(Alignment.RIGHT, richText.textAlign)
        assertEquals(Direction.RTL, richText.textDirection)
        assertFalse(richText.softWrap)
        assertEquals(TextOverflow.ELLIPSIS, richText.overflow)
        assertEquals(2, richText.maxLines)
        assertEquals(TextWidthBasis.LONGESTLINE, richText.textWidthBasis)
        assertEquals(HeightMode.DISABLE_ALL, richText.textHeightMode)
    }

    @Test
    fun keeps_rich_text_layout_defaults() {
        val widget = ParserTest.parse(
            "<Snapshot><Text>默认文本</Text></Snapshot>"
        ).createWidget()

        val richText = assertIs<RichText>(widget)
        assertEquals(Alignment.START, richText.textAlign)
        assertEquals(Direction.LTR, richText.textDirection)
        assertTrue(richText.softWrap)
        assertEquals(TextOverflow.CLIP, richText.overflow)
        assertNull(richText.maxLines)
        assertEquals(TextWidthBasis.PARENT, richText.textWidthBasis)
        assertNull(richText.textHeightMode)
    }

    @Test
    fun rejects_invalid_rich_text_layout_options() {
        listOf(
            "<Snapshot><Text textAlign=\"INVALID\">text</Text></Snapshot>",
            "<Snapshot><Text textDirection=\"INVALID\">text</Text></Snapshot>",
            "<Snapshot><Text overflow=\"INVALID\">text</Text></Snapshot>",
            "<Snapshot><Text maxLines=\"INVALID\">text</Text></Snapshot>",
            "<Snapshot><Text textWidthBasis=\"INVALID\">text</Text></Snapshot>",
            "<Snapshot><Text textHeightMode=\"INVALID\">text</Text></Snapshot>",
        ).forEach { text ->
            assertFailsWith<ParseException> { ParserTest.parse(text).createWidget() }
        }
    }
}
