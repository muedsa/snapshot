package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.rendering.flex.CrossAxisAlignment
import com.muedsa.snapshot.rendering.flex.MainAxisAlignment
import com.muedsa.snapshot.rendering.flex.MainAxisSize
import com.muedsa.snapshot.rendering.flex.VerticalDirection
import com.muedsa.snapshot.widget.Column
import com.muedsa.snapshot.widget.Container
import org.jetbrains.skia.paragraph.Direction
import kotlin.test.Test
import kotlin.test.assertTrue

class ColumnParserTest {

    @Test
    fun build_widget_test() {
        val text = """
            <Snapshot>
                <Column mainAxisAlignment="START" 
                     mainAxisSize="MAX" 
                     crossAxisAlignment="START"
                     textDirection="RTL"
                     verticalDirection="DOWN">
                    <Container width="33" height="44"/>
                    <Container width="77" height="34"/>
                    <Container width="88" height="56"/>
                </Column>
            </Snapshot>
        """.trimIndent()
        val snapshotElement = ParserTest.parse(text)
        val widget = snapshotElement.createWidget()
        assertTrue(widget is Column, "widget is Column")
        val column: Column = widget as Column
        assertTrue(column.mainAxisAlignment == MainAxisAlignment.START, "column.mainAxisAlignment == MainAxisAlignment.START")
        assertTrue(column.mainAxisSize == MainAxisSize.MAX, "column.mainAxisSize == MainAxisSize.MAX")
        assertTrue(column.crossAxisAlignment == CrossAxisAlignment.START, "column.crossAxisAlignment == CrossAxisAlignment.START")
        assertTrue(column.textDirection == Direction.RTL, "column.textDirection == Direction.RTL")
        assertTrue(column.verticalDirection == VerticalDirection.DOWN, "column.verticalDirection == VerticalDirection.DOWN")
        assertTrue(column.textBaseline == null, "column.textBaseline == null")
        val children = column.children
        assertTrue(children.size == 3, "children.size == 3")
        assertTrue(children[0] is Container, "children[0] is Container")
        assertTrue(children[1] is Container, "children[1] is Container")
        assertTrue(children[2] is Container, "children[2] is Container")
    }
}