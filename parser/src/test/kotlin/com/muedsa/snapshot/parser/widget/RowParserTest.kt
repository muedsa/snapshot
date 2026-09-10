package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.rendering.flex.CrossAxisAlignment
import com.muedsa.snapshot.rendering.flex.MainAxisAlignment
import com.muedsa.snapshot.rendering.flex.MainAxisSize
import com.muedsa.snapshot.rendering.flex.VerticalDirection
import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.Row
import org.jetbrains.skia.paragraph.Direction
import kotlin.test.Test
import kotlin.test.assertTrue

class RowParserTest {

    @Test
    fun build_widget_test() {
        val text = """
            <Snapshot>
                <Row mainAxisAlignment="START" 
                     mainAxisSize="MAX" 
                     crossAxisAlignment="START"
                     textDirection="RTL"
                     verticalDirection="DOWN">
                    <Container width="33" height="44"/>
                    <Container width="77" height="34"/>
                    <Container width="88" height="56"/>
                </Row>
            </Snapshot>
        """.trimIndent()
        val snapshotElement = ParserTest.parse(text)
        val widget = snapshotElement.createWidget()
        assertTrue(widget is Row, "widget is Row")
        val row: Row = widget as Row
        assertTrue(row.mainAxisAlignment == MainAxisAlignment.START, "row.mainAxisAlignment == MainAxisAlignment.START")
        assertTrue(row.mainAxisSize == MainAxisSize.MAX, "row.mainAxisSize == MainAxisSize.MAX")
        assertTrue(row.crossAxisAlignment == CrossAxisAlignment.START, "row.crossAxisAlignment == CrossAxisAlignment.START")
        assertTrue(row.textDirection == Direction.RTL, "row.textDirection == Direction.RTL")
        assertTrue(row.verticalDirection == VerticalDirection.DOWN, "row.verticalDirection == VerticalDirection.DOWN")
        assertTrue(row.textBaseline == null, "row.textBaseline == null")
        val children = row.children
        assertTrue(children.size == 3, "children.size == 3")
        assertTrue(children[0] is Container, "children[0] is Container")
        assertTrue(children[1] is Container, "children[1] is Container")
        assertTrue(children[2] is Container, "children[2] is Container")
    }
}