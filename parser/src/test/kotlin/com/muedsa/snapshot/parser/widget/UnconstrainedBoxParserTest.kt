package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.paint.Axis
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.toLayoutNode
import com.muedsa.snapshot.widget.SizedBox
import com.muedsa.snapshot.widget.UnconstrainedBox
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

class UnconstrainedBoxParserTest {

    @Test
    fun parses_alignment_and_leaves_both_axes_unconstrained_by_default() {
        val root = assertIs<SizedBox>(
            ParserTest.parse(
                """
                <Snapshot>
                    <SizedBox width="100" height="80">
                        <UnconstrainedBox alignment="BOTTOM_RIGHT">
                            <SizedBox width="150" height="120"/>
                        </UnconstrainedBox>
                    </SizedBox>
                </Snapshot>
                """.trimIndent()
            ).createWidget()
        )
        val unconstrained = assertIs<UnconstrainedBox>(root.child)
        assertNull(unconstrained.constrainedAxis)
        assertEquals(BoxAlignment.BOTTOM_RIGHT, unconstrained.alignment)

        val layout = root.createRenderBox().also { it.layout(BoxConstraints()) }.toLayoutNode()
        val childLayout = layout.children.single().children.single()
        assertEquals(Size(150f, 120f), childLayout.size)
        assertEquals(Offset(-50f, -40f), childLayout.absoluteOffset)
    }

    @Test
    fun parses_constrained_axis_and_preserves_only_that_axis() {
        val cases = listOf(
            Triple("HORIZONTAL", Size(100f, 120f), Offset(0f, -20f)),
            Triple("VERTICAL", Size(150f, 80f), Offset(-25f, 0f)),
        )
        cases.forEach { (value, expectedSize, expectedOffset) ->
            val root = assertIs<SizedBox>(
                ParserTest.parse(
                    """
                    <Snapshot><SizedBox width="100" height="80">
                        <UnconstrainedBox constrainedAxis="$value">
                            <SizedBox width="150" height="120"/>
                        </UnconstrainedBox>
                    </SizedBox></Snapshot>
                    """.trimIndent()
                ).createWidget()
            )
            val unconstrained = assertIs<UnconstrainedBox>(root.child)
            assertEquals(Axis.valueOf(value), unconstrained.constrainedAxis)

            val childLayout = root.createRenderBox().also { it.layout(BoxConstraints()) }
                .toLayoutNode().children.single().children.single()
            assertEquals(expectedSize, childLayout.size)
            assertEquals(expectedOffset, childLayout.absoluteOffset)
        }
    }

    @Test
    fun rejects_invalid_axis_and_multiple_children() {
        assertFailsWith<ParseException> {
            ParserTest.parse(
                "<Snapshot><UnconstrainedBox constrainedAxis=\"DIAGONAL\"/></Snapshot>"
            ).createWidget()
        }
        assertFailsWith<ParseException> {
            ParserTest.parse(
                "<Snapshot><UnconstrainedBox><SizedBox/><SizedBox/></UnconstrainedBox></Snapshot>"
            )
        }
    }
}
