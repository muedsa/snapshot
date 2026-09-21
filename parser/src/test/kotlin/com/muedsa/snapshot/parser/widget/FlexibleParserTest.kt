package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.assertGlobalRect
import com.muedsa.snapshot.findType
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderColoredBox
import com.muedsa.snapshot.rendering.flex.FlexFit
import com.muedsa.snapshot.rendering.toLayoutNode
import com.muedsa.snapshot.widget.Expanded
import com.muedsa.snapshot.widget.Flexible
import com.muedsa.snapshot.widget.Row
import com.muedsa.snapshot.widget.SizedBox
import org.jetbrains.skia.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class FlexibleParserTest {

    @Test
    fun parses_expanded_and_flexible_and_distributes_space() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot>
                <SizedBox width="300" height="100">
                    <Row>
                        <Expanded>
                            <Container color="#FFFF0000"/>
                        </Expanded>
                        <Flexible flex="2" fit="TIGHT">
                            <Container color="#FF00FF00"/>
                        </Flexible>
                    </Row>
                </SizedBox>
            </Snapshot>
            """.trimIndent()
        )

        val root = assertIs<SizedBox>(snapshot.createWidget())
        val row = assertIs<Row>(root.child)
        val expanded = assertIs<Expanded>(row.children[0])
        assertEquals(1, expanded.flex)
        assertEquals(FlexFit.TIGHT, expanded.fit)
        val flexible = assertIs<Flexible>(row.children[1])
        assertEquals(2, flexible.flex)
        assertEquals(FlexFit.TIGHT, flexible.fit)

        val renderRoot = root.createRenderBox().also { it.layout(BoxConstraints()) }.toLayoutNode()
        val red = checkNotNull(renderRoot.findType<RenderColoredBox> { it.color == Color.RED })
        val green = checkNotNull(renderRoot.findType<RenderColoredBox> { it.color == Color.GREEN })
        red.assertGlobalRect(0f, 0f, 100f, 100f)
        green.assertGlobalRect(100f, 0f, 200f, 100f)
    }

    @Test
    fun flexible_uses_loose_fit_by_default() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot>
                <Row>
                    <Flexible><SizedBox width="10" height="10"/></Flexible>
                </Row>
            </Snapshot>
            """.trimIndent()
        )

        val row = assertIs<Row>(snapshot.createWidget())
        val flexible = assertIs<Flexible>(row.children.single())
        assertEquals(1, flexible.flex)
        assertEquals(FlexFit.LOOSE, flexible.fit)
    }

    @Test
    fun rejects_unknown_flex_fit() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot>
                <Row><Flexible fit="UNKNOWN"><SizedBox/></Flexible></Row>
            </Snapshot>
            """.trimIndent()
        )

        assertFailsWith<ParseException> { snapshot.createWidget() }
    }

    @Test
    fun rejects_invalid_flex_number() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot>
                <Row><Expanded flex="1.5"><SizedBox/></Expanded></Row>
            </Snapshot>
            """.trimIndent()
        )

        assertFailsWith<ParseException> { snapshot.createWidget() }
    }

    @Test
    fun expanded_rejects_fit_attribute() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot>
                <Row><Expanded fit="LOOSE"><SizedBox/></Expanded></Row>
            </Snapshot>
            """.trimIndent()
        )

        assertFailsWith<ParseException> { snapshot.createWidget() }
    }

    @Test
    fun rejects_flexible_outside_row_or_column() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot>
                <Padding><Flexible><SizedBox/></Flexible></Padding>
            </Snapshot>
            """.trimIndent()
        )

        assertFailsWith<ParseException> { snapshot.createWidget() }
    }
}
