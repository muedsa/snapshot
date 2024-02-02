package com.muedsa.snapshot.parser.tag

import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.rendering.flex.CrossAxisAlignment
import com.muedsa.snapshot.rendering.flex.MainAxisAlignment
import com.muedsa.snapshot.rendering.flex.MainAxisSize
import com.muedsa.snapshot.rendering.flex.VerticalDirection
import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.Row
import org.jetbrains.skia.paragraph.Direction
import kotlin.test.Test

class TagRowTest {

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
        println(text)
        val snapshotElement = ParserTest.parse(text)
        println(snapshotElement.toTreeString(0))
        val widget = snapshotElement.createWidget()
        assert(widget is Row)
        val row: Row = widget as Row
        assert(row.mainAxisAlignment == MainAxisAlignment.START)
        assert(row.mainAxisSize == MainAxisSize.MAX)
        assert(row.crossAxisAlignment == CrossAxisAlignment.START)
        assert(row.textDirection == Direction.RTL)
        assert(row.verticalDirection == VerticalDirection.DOWN)
        assert(row.textBaseline == null)
        val children = row.children
        assert(children.size == 3)
        assert(children[0] is Container)
        assert(children[1] is Container)
        assert(children[2] is Container)
    }
}