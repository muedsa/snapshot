package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.Stack
import org.jetbrains.skia.paragraph.Direction
import kotlin.test.Test
import kotlin.test.assertTrue

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
        val snapshotElement = ParserTest.parse(text)
        val widget = snapshotElement.createWidget()
        assertTrue(widget is Stack, "widget is Stack")
        val stack: Stack = widget as Stack
        assertTrue(stack.alignment == BoxAlignment.CENTER, "stack.alignment == BoxAlignment.CENTER")
        assertTrue(stack.textDirection == Direction.LTR, "stack.textDirection == Direction.LTR")
        val children = stack.children
        assertTrue(children.size == 3, "children.size == 3")
        assertTrue(children[0] is Container, "children[0] is Container")
        assertTrue(children[1] is Container, "children[1] is Container")
        assertTrue(children[2] is Container, "children[2] is Container")
    }
}