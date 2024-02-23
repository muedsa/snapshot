package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.Stack
import org.jetbrains.skia.paragraph.Direction
import kotlin.test.Test

class StackParserTest {
    @Test
    fun build_widget_test() {
        val text = """
            <Snapshot>
                <Stack alignment="CENTER">
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
        assert(widget is Stack)
        val stack: Stack = widget as Stack
        assert(stack.alignment == BoxAlignment.CENTER)
        assert(stack.textDirection == Direction.LTR)
        val children = stack.children
        assert(children.size == 3)
        assert(children[0] is Container)
        assert(children[1] is Container)
        assert(children[2] is Container)
    }
}