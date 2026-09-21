package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.paint.decoration.Border
import com.muedsa.snapshot.paint.decoration.BorderSide
import com.muedsa.snapshot.paint.decoration.BoxDecoration
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.rendering.box.DecorationPosition
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
    fun parses_colored_box_with_child() {
        val widget = ParserTest.parse(
            """
            <Snapshot>
                <ColoredBox color="#FF336699">
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
                <DecoratedBox position="FOREGROUND" color="#FFFF0000" border="2 SOLID #FF00FF00">
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
