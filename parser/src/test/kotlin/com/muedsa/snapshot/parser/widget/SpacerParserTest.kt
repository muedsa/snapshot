package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.assertGlobalRect
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.toLayoutNode
import com.muedsa.snapshot.widget.Column
import com.muedsa.snapshot.widget.Flex
import com.muedsa.snapshot.widget.Row
import com.muedsa.snapshot.widget.SizedBox
import com.muedsa.snapshot.widget.Spacer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class SpacerParserTest {

    @Test
    fun parses_spacers_and_distributes_row_space() {
        val root = assertIs<SizedBox>(
            ParserTest.parse(
                """
                <Snapshot>
                    <SizedBox width="300" height="40">
                        <Row crossAxisAlignment="STRETCH">
                            <SizedBox width="40"/>
                            <Spacer/>
                            <Spacer flex="2"/>
                            <SizedBox width="20"/>
                        </Row>
                    </SizedBox>
                </Snapshot>
                """.trimIndent()
            ).createWidget()
        )
        val row = assertIs<Row>(root.child)
        assertEquals(1, assertIs<Spacer>(row.children[1]).flex)
        assertEquals(2, assertIs<Spacer>(row.children[2]).flex)

        val layout = root.createRenderBox().also { it.layout(BoxConstraints()) }.toLayoutNode()
        val rowLayout = layout.children.single()
        rowLayout.children[1].assertGlobalRect(40f, 0f, 80f, 40f)
        rowLayout.children[2].assertGlobalRect(120f, 0f, 160f, 40f)
    }

    @Test
    fun flex_and_column_tags_accept_spacer() {
        val flex = assertIs<Flex>(
            ParserTest.parse(
                "<Snapshot><Flex direction=\"HORIZONTAL\"><Spacer/></Flex></Snapshot>"
            ).createWidget()
        )
        assertIs<Spacer>(flex.children.single())

        val column = assertIs<Column>(
            ParserTest.parse("<Snapshot><Column><Spacer/></Column></Snapshot>").createWidget()
        )
        assertIs<Spacer>(column.children.single())
    }

    @Test
    fun rejects_wrong_parent_invalid_flex_fit_and_children() {
        listOf(
            "<Snapshot><Spacer/></Snapshot>",
            "<Snapshot><Padding><Spacer/></Padding></Snapshot>",
            "<Snapshot><Row><Spacer flex=\"0\"/></Row></Snapshot>",
            "<Snapshot><Row><Spacer flex=\"-1\"/></Row></Snapshot>",
            "<Snapshot><Row><Spacer flex=\"bad\"/></Row></Snapshot>",
            "<Snapshot><Row><Spacer fit=\"LOOSE\"/></Row></Snapshot>",
        ).forEach { text ->
            assertFailsWith<ParseException>(text) {
                ParserTest.parse(text).createWidget()
            }
        }

        assertFailsWith<ParseException> {
            ParserTest.parse("<Snapshot><Row><Spacer><SizedBox/></Spacer></Row></Snapshot>")
        }
    }
}
