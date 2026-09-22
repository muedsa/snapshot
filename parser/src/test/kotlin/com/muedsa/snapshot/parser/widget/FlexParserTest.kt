package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.paint.Axis
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.flex.CrossAxisAlignment
import com.muedsa.snapshot.rendering.flex.MainAxisAlignment
import com.muedsa.snapshot.rendering.flex.MainAxisSize
import com.muedsa.snapshot.rendering.flex.VerticalDirection
import com.muedsa.snapshot.widget.Flex
import com.muedsa.snapshot.widget.SizedBox
import org.jetbrains.skia.paragraph.BaselineMode
import org.jetbrains.skia.paragraph.Direction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

class FlexParserTest {

    @Test
    fun parses_flex_direction_options_and_children() {
        val flex = assertIs<Flex>(
            ParserTest.parse(
                """
                <Snapshot>
                    <Flex direction="HORIZONTAL"
                          mainAxisAlignment="SPACE_BETWEEN"
                          mainAxisSize="MIN"
                          crossAxisAlignment="BASELINE"
                          textDirection="RTL"
                          verticalDirection="UP"
                          textBaseline="ALPHABETIC"
                          clipBehavior="HARD_EDGE">
                        <SizedBox width="10" height="20"/>
                        <SizedBox width="30" height="40"/>
                    </Flex>
                </Snapshot>
                """.trimIndent()
            ).createWidget()
        )

        assertEquals(Axis.HORIZONTAL, flex.direction)
        assertEquals(MainAxisAlignment.SPACE_BETWEEN, flex.mainAxisAlignment)
        assertEquals(MainAxisSize.MIN, flex.mainAxisSize)
        assertEquals(CrossAxisAlignment.BASELINE, flex.crossAxisAlignment)
        assertEquals(Direction.RTL, flex.textDirection)
        assertEquals(VerticalDirection.UP, flex.verticalDirection)
        assertEquals(BaselineMode.ALPHABETIC, flex.textBaseline)
        assertEquals(ClipBehavior.HARD_EDGE, flex.clipBehavior)
        assertEquals(2, flex.children.size)
        assertIs<SizedBox>(flex.children[0])
        assertIs<SizedBox>(flex.children[1])
    }

    @Test
    fun flex_supports_vertical_direction_and_defaults() {
        val flex = assertIs<Flex>(
            ParserTest.parse("<Snapshot><Flex direction=\"VERTICAL\"/></Snapshot>").createWidget()
        )

        assertEquals(Axis.VERTICAL, flex.direction)
        assertEquals(MainAxisAlignment.START, flex.mainAxisAlignment)
        assertEquals(MainAxisSize.MAX, flex.mainAxisSize)
        assertEquals(CrossAxisAlignment.CENTER, flex.crossAxisAlignment)
        assertEquals(Direction.LTR, flex.textDirection)
        assertEquals(VerticalDirection.DOWN, flex.verticalDirection)
        assertNull(flex.textBaseline)
        assertEquals(ClipBehavior.NONE, flex.clipBehavior)
    }

    @Test
    fun flex_requires_valid_direction() {
        listOf(
            "<Snapshot><Flex/></Snapshot>",
            "<Snapshot><Flex direction=\"DIAGONAL\"/></Snapshot>",
        ).forEach { text ->
            assertFailsWith<ParseException>(text) {
                ParserTest.parse(text).createWidget()
            }
        }
    }
}
