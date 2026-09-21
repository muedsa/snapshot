package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.EdgeInsets
import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderPositionedBox
import com.muedsa.snapshot.rendering.box.RenderPadding
import com.muedsa.snapshot.rendering.toLayoutNode
import com.muedsa.snapshot.widget.Align
import com.muedsa.snapshot.widget.Center
import com.muedsa.snapshot.widget.Padding
import com.muedsa.snapshot.widget.SizedBox
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class BasicLayoutParserTest {

    @Test
    fun parses_nested_padding_align_and_sized_box() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot>
                <Padding padding="(1,2,3,4)">
                    <Align alignment="BOTTOM_RIGHT" widthFactor="2" heightFactor="3">
                        <SizedBox width="10" height="20"/>
                    </Align>
                </Padding>
            </Snapshot>
            """.trimIndent()
        )

        val padding = assertIs<Padding>(snapshot.createWidget())
        assertEquals(EdgeInsets(1f, 2f, 3f, 4f), padding.padding)
        val align = assertIs<Align>(padding.child)
        assertEquals(BoxAlignment.BOTTOM_RIGHT, align.alignment)
        assertEquals(2f, align.widthFactor)
        assertEquals(3f, align.heightFactor)
        val sizedBox = assertIs<SizedBox>(align.child)
        assertEquals(10f, sizedBox.width)
        assertEquals(20f, sizedBox.height)

        val renderRoot = padding.createRenderBox().also { it.layout(BoxConstraints()) }
        val layout = renderRoot.toLayoutNode()
        assertIs<RenderPadding>(layout.renderBox)
        assertEquals(Size(24f, 66f), layout.size)
        val alignLayout = layout.children.single()
        assertIs<RenderPositionedBox>(alignLayout.renderBox)
        assertEquals(Offset(1f, 2f), alignLayout.absoluteOffset)
        assertEquals(Size(20f, 60f), alignLayout.size)
        assertEquals(Offset(11f, 42f), alignLayout.children.single().absoluteOffset)
    }

    @Test
    fun parses_center_factors_and_default_padding() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot>
                <Padding>
                    <Center widthFactor="1.5" heightFactor="2">
                        <SizedBox width="40" height="30"/>
                    </Center>
                </Padding>
            </Snapshot>
            """.trimIndent()
        )

        val padding = assertIs<Padding>(snapshot.createWidget())
        assertEquals(EdgeInsets.ZERO, padding.padding)
        val center = assertIs<Center>(padding.child)
        assertEquals(BoxAlignment.CENTER, center.alignment)
        assertEquals(1.5f, center.widthFactor)
        assertEquals(2f, center.heightFactor)

        val renderRoot = padding.createRenderBox().also { it.layout(BoxConstraints()) }
        assertEquals(Size(60f, 60f), renderRoot.definiteSize)
    }
}
