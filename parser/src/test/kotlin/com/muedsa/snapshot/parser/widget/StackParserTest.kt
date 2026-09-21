package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.stack.StackFit
import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.Stack
import org.jetbrains.skia.paragraph.Direction
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class StackParserTest {
    @Test
    fun build_widget_test() {
        val text = """
            <Snapshot>
                <Stack alignment="CENTER" fit="EXPAND" clipBehavior="NONE">
                    <Container width="33" height="44"/>
                    <Container width="77" height="34"/>
                    <Container width="88" height="56"/>
                </Stack>
            </Snapshot>
        """.trimIndent()
        val snapshotElement = ParserTest.parse(text)
        val widget = snapshotElement.createWidget()
        assertTrue(widget is Stack, "widget is Stack")
        val stack: Stack = widget
        assertTrue(stack.alignment == BoxAlignment.CENTER, "stack.alignment == BoxAlignment.CENTER")
        assertTrue(stack.textDirection == Direction.LTR, "stack.textDirection == Direction.LTR")
        assertTrue(stack.fit == StackFit.EXPAND, "stack.fit == StackFit.EXPAND")
        assertTrue(stack.clipBehavior == ClipBehavior.NONE, "stack.clipBehavior == ClipBehavior.NONE")
        val children = stack.children
        assertTrue(children.size == 3, "children.size == 3")
        assertTrue(children[0] is Container, "children[0] is Container")
        assertTrue(children[1] is Container, "children[1] is Container")
        assertTrue(children[2] is Container, "children[2] is Container")
    }

    @Test
    fun defaults_and_invalid_values_test() {
        val stack = ParserTest.parse("<Snapshot><Stack/></Snapshot>").createWidget() as Stack
        assertTrue(stack.fit == StackFit.LOOSE, "stack.fit == StackFit.LOOSE")
        assertTrue(stack.clipBehavior == ClipBehavior.HARD_EDGE, "stack.clipBehavior == ClipBehavior.HARD_EDGE")

        listOf(
            "<Snapshot><Stack fit=\"INVALID\"/></Snapshot>",
            "<Snapshot><Stack clipBehavior=\"INVALID\"/></Snapshot>",
        ).forEach { text ->
            assertFailsWith<ParseException> { ParserTest.parse(text).createWidget() }
        }
    }
}
