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
        println(text)
        val snapshotElement = ParserTest.parse(text)
        println(snapshotElement.toTreeString(0))
        val widget = snapshotElement.createWidget()
        assert(widget is Column)
        val column: Column = widget as Column
        assert(column.mainAxisAlignment == MainAxisAlignment.START)
        assert(column.mainAxisSize == MainAxisSize.MAX)
        assert(column.crossAxisAlignment == CrossAxisAlignment.START)
        assert(column.textDirection == Direction.RTL)
        assert(column.verticalDirection == VerticalDirection.DOWN)
        assert(column.textBaseline == null)
        val children = column.children
        assert(children.size == 3)
        assert(children[0] is Container)
        assert(children[1] is Container)
        assert(children[2] is Container)
    }
}