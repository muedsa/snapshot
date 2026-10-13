package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.paint.decoration.Border
import com.muedsa.snapshot.paint.decoration.BorderSide
import com.muedsa.snapshot.paint.decoration.BoxDecoration
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.rendering.box.DecorationPosition
import com.muedsa.snapshot.snapshotPixels
import com.muedsa.snapshot.widget.ColoredBox
import com.muedsa.snapshot.widget.DecoratedBox
import com.muedsa.snapshot.widget.SizedBox
import org.jetbrains.skia.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class DecorationParserTest {

    @Test
    fun thick_rounded_borders_render_through_decoration_tags() {
        val borderAttrs = """border="4 SOLID #FF0000" borderRadius="2""""
        val documents = listOf(
            """<Container width="80" height="60" $borderAttrs/>""",
            """<Border $borderAttrs><SizedBox width="80" height="60"/></Border>""",
            """<DecoratedBox $borderAttrs><SizedBox width="80" height="60"/></DecoratedBox>""",
            """<Container width="80" height="60" foregroundBorder="4 SOLID #FF0000" foregroundBorderRadius="2"/>""",
        )
        for (document in documents) {
            val widget = ParserTest.parse("<Snapshot>$document</Snapshot>").createWidget()
            val pixels = snapshotPixels { attach(widget) }
            assertEquals(80, pixels.info.width)
            assertEquals(60, pixels.info.height)
            expectColorAt(pixels, 40, 1, Color.RED)
            expectColorAt(pixels, 1, 30, Color.RED)
            expectColorAt(pixels, 4, 4, Color.WHITE)
            expectColorAt(pixels, 40, 30, Color.WHITE)
        }
    }

    @Test
    fun parses_colored_box_with_child() {
        val widget = ParserTest.parse(
            """
            <Snapshot>
                <ColoredBox color="#336699FF">
                    <SizedBox width="20" height="10"/>
                </ColoredBox>
            </Snapshot>
            """.trimIndent()
        ).createWidget()

        val coloredBox = assertIs<ColoredBox>(widget)
        assertEquals(0xFF_33_66_99.toInt(), coloredBox.color)
        assertIs<SizedBox>(coloredBox.child)
    }

    @Test
    fun colored_box_requires_valid_color() {
        listOf(
            "<Snapshot><ColoredBox/></Snapshot>",
            "<Snapshot><ColoredBox color=\"336699\"/></Snapshot>",
        ).forEach { text ->
            assertFailsWith<ParseException> { ParserTest.parse(text).createWidget() }
        }
    }

    @Test
    fun parses_decorated_box_position_and_decoration() {
        val widget = ParserTest.parse(
            """
            <Snapshot>
                <DecoratedBox position="FOREGROUND" color="#FF0000FF" border="2 SOLID #00FF00FF">
                    <SizedBox width="30" height="15"/>
                </DecoratedBox>
            </Snapshot>
            """.trimIndent()
        ).createWidget()

        val decoratedBox = assertIs<DecoratedBox>(widget)
        assertEquals(DecorationPosition.FOREGROUND, decoratedBox.position)
        assertIs<SizedBox>(decoratedBox.child)
        val decoration = assertIs<BoxDecoration>(decoratedBox.decoration)
        assertEquals(Color.RED, decoration.color)
        val border = assertIs<Border>(decoration.border)
        assertEquals(BorderSide(width = 2f, color = Color.GREEN), border.left)
        assertEquals(border.left, border.top)
        assertEquals(border.left, border.right)
        assertEquals(border.left, border.bottom)
    }

    @Test
    fun decorated_box_defaults_to_background_and_rejects_invalid_position() {
        val widget = ParserTest.parse(
            "<Snapshot><DecoratedBox color=\"#FFFFFFFF\"/></Snapshot>"
        ).createWidget()
        assertEquals(DecorationPosition.BACKGROUND, assertIs<DecoratedBox>(widget).position)

        assertFailsWith<ParseException> {
            ParserTest.parse(
                "<Snapshot><DecoratedBox position=\"INVALID\"/></Snapshot>"
            ).createWidget()
        }
    }
}
