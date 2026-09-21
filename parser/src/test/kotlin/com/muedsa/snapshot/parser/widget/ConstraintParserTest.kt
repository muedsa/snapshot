package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.toLayoutNode
import com.muedsa.snapshot.widget.ConstrainedBox
import com.muedsa.snapshot.widget.LimitedBox
import com.muedsa.snapshot.widget.OverflowBox
import com.muedsa.snapshot.widget.SizedBox
import com.muedsa.snapshot.widget.SizedOverflowBox
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class ConstraintParserTest {

    @Test
    fun constrained_box_applies_min_and_max_constraints() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot>
                <ConstrainedBox minWidth="40" maxWidth="80" minHeight="30" maxHeight="60">
                    <SizedBox width="100" height="10"/>
                </ConstrainedBox>
            </Snapshot>
            """.trimIndent()
        )

        val widget = assertIs<ConstrainedBox>(snapshot.createWidget())
        assertEquals(BoxConstraints(40f, 80f, 30f, 60f), widget.constraints)
        val renderBox = widget.createRenderBox().also { it.layout(BoxConstraints()) }
        assertEquals(Size(80f, 30f), renderBox.definiteSize)
    }

    @Test
    fun limited_box_limits_child_under_unbounded_constraints() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot>
                <LimitedBox maxWidth="70" maxHeight="50">
                    <SizedBox width="100" height="100"/>
                </LimitedBox>
            </Snapshot>
            """.trimIndent()
        )

        val widget = assertIs<LimitedBox>(snapshot.createWidget())
        assertEquals(70f, widget.maxWidth)
        assertEquals(50f, widget.maxHeight)
        val renderBox = widget.createRenderBox().also { it.layout(BoxConstraints()) }
        assertEquals(Size(70f, 50f), renderBox.definiteSize)
    }

    @Test
    fun overflow_box_allows_larger_child_and_aligns_it() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot>
                <SizedBox width="100" height="80">
                    <OverflowBox minWidth="0" maxWidth="200" minHeight="0" maxHeight="120" alignment="BOTTOM_RIGHT">
                        <SizedBox width="200" height="120"/>
                    </OverflowBox>
                </SizedBox>
            </Snapshot>
            """.trimIndent()
        )

        val root = assertIs<SizedBox>(snapshot.createWidget())
        val overflow = assertIs<OverflowBox>(root.child)
        assertEquals(BoxAlignment.BOTTOM_RIGHT, overflow.alignment)
        assertEquals(200f, overflow.maxWidth)
        assertEquals(120f, overflow.maxHeight)
        val layout = root.createRenderBox().also { it.layout(BoxConstraints()) }.toLayoutNode()
        val overflowLayout = layout.children.single()
        assertEquals(Size(100f, 80f), overflowLayout.size)
        assertEquals(Offset(-100f, -40f), overflowLayout.children.single().absoluteOffset)
    }

    @Test
    fun sized_overflow_box_keeps_requested_size_and_allows_larger_child() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot>
                <SizedOverflowBox width="100" height="80" alignment="BOTTOM_RIGHT">
                    <SizedBox width="200" height="120"/>
                </SizedOverflowBox>
            </Snapshot>
            """.trimIndent()
        )

        val widget = assertIs<SizedOverflowBox>(snapshot.createWidget())
        assertEquals(Size(100f, 80f), widget.size)
        assertEquals(BoxAlignment.BOTTOM_RIGHT, widget.alignment)
        val layout = widget.createRenderBox().also { it.layout(BoxConstraints()) }.toLayoutNode()
        assertEquals(Size(100f, 80f), layout.size)
        assertEquals(Offset(-100f, -40f), layout.children.single().absoluteOffset)
    }

    @Test
    fun rejects_invalid_constraints() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot><ConstrainedBox minWidth="100" maxWidth="50"><SizedBox/></ConstrainedBox></Snapshot>
            """.trimIndent()
        )

        assertFailsWith<ParseException> { snapshot.createWidget() }
    }

    @Test
    fun sized_overflow_box_requires_both_dimensions() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot><SizedOverflowBox width="100"><SizedBox/></SizedOverflowBox></Snapshot>
            """.trimIndent()
        )

        assertFailsWith<ParseException> { snapshot.createWidget() }
    }
}
