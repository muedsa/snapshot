package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Matrix44CMO
import com.muedsa.geometry.Radius
import com.muedsa.snapshot.paint.decoration.Border
import com.muedsa.snapshot.paint.decoration.BorderRadius
import com.muedsa.snapshot.paint.decoration.BorderSide
import com.muedsa.snapshot.paint.decoration.BoxDecoration
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.widget.Container
import org.jetbrains.skia.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

class ContainerAdvancedParserTest {

    @Test
    fun parses_transform_clip_and_foreground_decoration() {
        val container = ParserTest.parse(
            """
            <Snapshot>
                <Container width="100" height="80" color="#FFFF0000" borderRadius="12"
                           clipBehavior="ANTI_ALIAS"
                           transform="(1,0,0,0,0,1,0,0,0,0,1,0,20,10,0,1)"
                           transformAlignment="CENTER"
                           foregroundColor="#660000FF"
                           foregroundBorder="2 SOLID #FF00FF00"
                           foregroundBorderRadius="8">
                    <SizedBox/>
                </Container>
            </Snapshot>
            """.trimIndent()
        ).createWidget()

        assertIs<Container>(container)
        assertNull(container.color)
        val decoration = assertIs<BoxDecoration>(container.decoration)
        assertEquals(Color.RED, decoration.color)
        assertEquals(BorderRadius.circular(12f), decoration.borderRadius)

        val foregroundDecoration = assertIs<BoxDecoration>(container.foregroundDecoration)
        assertEquals(0x66_00_00_FF, foregroundDecoration.color)
        assertEquals(BorderRadius.circular(8f), foregroundDecoration.borderRadius)
        val foregroundBorder = assertIs<Border>(foregroundDecoration.border)
        val expectedSide = BorderSide(width = 2f, color = Color.GREEN)
        assertEquals(expectedSide, foregroundBorder.left)
        assertEquals(expectedSide, foregroundBorder.top)
        assertEquals(expectedSide, foregroundBorder.right)
        assertEquals(expectedSide, foregroundBorder.bottom)

        assertEquals(ClipBehavior.ANTI_ALIAS, container.clipBehavior)
        assertEquals(Matrix44CMO.translationValues(20f, 10f, 0f), container.transform)
        assertEquals(BoxAlignment.CENTER, container.transformAlignment)
    }

    @Test
    fun advanced_options_use_container_defaults() {
        val container = assertIs<Container>(
            ParserTest.parse("<Snapshot><Container width=\"10\" height=\"10\"/></Snapshot>").createWidget()
        )

        assertNull(container.transform)
        assertNull(container.transformAlignment)
        assertEquals(ClipBehavior.NONE, container.clipBehavior)
        assertNull(container.foregroundDecoration)
    }

    @Test
    fun foreground_color_alone_creates_foreground_decoration() {
        val container = assertIs<Container>(
            ParserTest.parse(
                "<Snapshot><Container width=\"10\" height=\"10\" foregroundColor=\"#660000FF\"/></Snapshot>"
            ).createWidget()
        )

        assertEquals(0x66_00_00_FF, assertIs<BoxDecoration>(container.foregroundDecoration).color)
    }

    @Test
    fun parses_individual_foreground_sides_and_corners() {
        val container = assertIs<Container>(
            ParserTest.parse(
                """
                <Snapshot>
                    <Container width="20" height="20"
                               foregroundBorderLeft="1 SOLID #FFFF0000"
                               foregroundBorderTop="2 SOLID #FF00FF00"
                               foregroundBorderRight="3 SOLID #FF0000FF"
                               foregroundBorderBottom="4 SOLID #FFFFFFFF"
                               foregroundBorderRadiusTopLeft="1"
                               foregroundBorderRadiusTopRight="2"
                               foregroundBorderRadiusBottomLeft="3"
                               foregroundBorderRadiusBottomRight="4"
                               foregroundBoxShadow="1 2"/>
                </Snapshot>
                """.trimIndent()
            ).createWidget()
        )

        val foreground = assertIs<BoxDecoration>(container.foregroundDecoration)
        val border = assertIs<Border>(foreground.border)
        assertEquals(1f, border.left.width)
        assertEquals(2f, border.top.width)
        assertEquals(3f, border.right.width)
        assertEquals(4f, border.bottom.width)
        assertEquals(
            BorderRadius(
                topLeft = Radius.circular(1f),
                topRight = Radius.circular(2f),
                bottomLeft = Radius.circular(3f),
                bottomRight = Radius.circular(4f),
            ),
            foreground.borderRadius
        )
        assertEquals(1, foreground.boxShadow?.size)
    }

    @Test
    fun rejects_invalid_advanced_options() {
        listOf(
            "<Snapshot><Container transform=\"(1,0,0,1)\"/></Snapshot>",
            "<Snapshot><Container transformAlignment=\"INVALID\"/></Snapshot>",
            "<Snapshot><Container clipBehavior=\"INVALID\"/></Snapshot>",
            "<Snapshot><Container clipBehavior=\"ANTI_ALIAS\"/></Snapshot>",
        ).forEach { text ->
            assertFailsWith<ParseException>(text) {
                ParserTest.parse(text).createWidget()
            }
        }
    }
}
