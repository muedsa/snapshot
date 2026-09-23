package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.stack.StackFit
import com.muedsa.snapshot.snapshotPixels
import com.muedsa.snapshot.widget.IndexedStack
import com.muedsa.snapshot.widget.Positioned
import org.jetbrains.skia.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

class IndexedStackParserTest {

    @Test
    fun parses_index_and_stack_options_with_positioned_child() {
        val widget = assertIs<IndexedStack>(
            ParserTest.parse(
                """
                <Snapshot><IndexedStack index="1" fit="EXPAND" clipBehavior="NONE">
                    <Container width="80" height="60"/>
                    <Positioned left="12" top="8"><Container width="20" height="15"/></Positioned>
                </IndexedStack></Snapshot>
                """.trimIndent()
            ).createWidget()
        )

        assertEquals(1, widget.index)
        assertEquals(StackFit.EXPAND, widget.fit)
        assertEquals(ClipBehavior.NONE, widget.clipBehavior)
        assertEquals(2, widget.children.size)
        assertIs<Positioned>(widget.children[1])
    }

    @Test
    fun selected_parser_child_is_the_only_painted_child() {
        val widget = assertIs<IndexedStack>(
            ParserTest.parse(
                """
                <Snapshot><IndexedStack index="1">
                    <Container width="60" height="40" color="#FFFF0000"/>
                    <Container width="20" height="15" color="#FF0000FF"/>
                </IndexedStack></Snapshot>
                """.trimIndent()
            ).createWidget()
        )

        val pixels = snapshotPixels { attach(widget) }
        assertEquals(60, pixels.info.width)
        assertEquals(40, pixels.info.height)
        expectColorAt(pixels, 10, 10, Color.BLUE)
        expectColorAt(pixels, 40, 30, Color.WHITE)
    }

    @Test
    fun omitted_index_selects_first_child_and_valueless_index_selects_none() {
        val default = assertIs<IndexedStack>(
            ParserTest.parse("<Snapshot><IndexedStack/></Snapshot>").createWidget()
        )
        assertEquals(0, default.index)

        val hidden = assertIs<IndexedStack>(
            ParserTest.parse("<Snapshot><IndexedStack index/></Snapshot>").createWidget()
        )
        assertNull(hidden.index)
    }

    @Test
    fun invalid_index_is_reported_by_parser_or_layout() {
        assertFailsWith<ParseException> {
            ParserTest.parse("<Snapshot><IndexedStack index=\"bad\"/></Snapshot>").createWidget()
        }
        val negative = assertIs<IndexedStack>(
            ParserTest.parse("<Snapshot><IndexedStack index=\"-1\"/></Snapshot>").createWidget()
        )
        assertFailsWith<IllegalArgumentException> { negative.createRenderBox() }

        val outOfRange = assertIs<IndexedStack>(
            ParserTest.parse(
                "<Snapshot><IndexedStack index=\"1\"><Container width=\"10\" height=\"10\"/></IndexedStack></Snapshot>"
            ).createWidget()
        )
        assertFailsWith<IllegalArgumentException> { snapshotPixels { attach(outOfRange) } }
    }
}
