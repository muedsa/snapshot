package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.paint.decoration.BoxDecoration
import com.muedsa.snapshot.paint.decoration.BoxShape
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.DecoratedBox
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

class DecorationOptionsParserTest {

    @Test
    fun parses_shape_and_background_blend_mode() {
        val decoratedBox = assertIs<DecoratedBox>(
            ParserTest.parse(
                """
                <Snapshot>
                    <DecoratedBox color="#FFFF0000" shape="CIRCLE" backgroundBlendMode="MULTIPLY">
                        <SizedBox width="20" height="20"/>
                    </DecoratedBox>
                </Snapshot>
                """.trimIndent()
            ).createWidget()
        )

        val decoration = assertIs<BoxDecoration>(decoratedBox.decoration)
        assertEquals(Color.RED, decoration.color)
        assertEquals(BoxShape.CIRCLE, decoration.shape)
        assertEquals(BlendMode.MULTIPLY, decoration.backgroundBlendMode)
        assertNull(decoration.borderRadius)
    }

    @Test
    fun container_uses_box_decoration_for_circle_shape() {
        val container = assertIs<Container>(
            ParserTest.parse(
                "<Snapshot><Container width=\"20\" height=\"20\" color=\"#FF00FF00\" shape=\"CIRCLE\"/></Snapshot>"
            ).createWidget()
        )

        assertNull(container.color)
        val decoration = assertIs<BoxDecoration>(container.decoration)
        assertEquals(Color.GREEN, decoration.color)
        assertEquals(BoxShape.CIRCLE, decoration.shape)
    }

    @Test
    fun container_uses_box_decoration_for_background_blend_mode() {
        val container = assertIs<Container>(
            ParserTest.parse(
                """
                <Snapshot>
                    <Container width="20" height="20" color="#FF00FF00"
                               backgroundBlendMode="MULTIPLY"/>
                </Snapshot>
                """.trimIndent()
            ).createWidget()
        )

        assertNull(container.color)
        val decoration = assertIs<BoxDecoration>(container.decoration)
        assertEquals(Color.GREEN, decoration.color)
        assertEquals(BlendMode.MULTIPLY, decoration.backgroundBlendMode)
    }

    @Test
    fun parses_foreground_shape_and_blend_mode() {
        val container = assertIs<Container>(
            ParserTest.parse(
                """
                <Snapshot>
                    <Container width="20" height="20"
                               foregroundColor="#660000FF"
                               foregroundShape="CIRCLE"
                               foregroundBackgroundBlendMode="SRC_OVER"/>
                </Snapshot>
                """.trimIndent()
            ).createWidget()
        )

        val foreground = assertIs<BoxDecoration>(container.foregroundDecoration)
        assertEquals(BoxShape.CIRCLE, foreground.shape)
        assertEquals(BlendMode.SRC_OVER, foreground.backgroundBlendMode)
        assertNull(foreground.borderRadius)
    }

    @Test
    fun decoration_options_keep_existing_defaults() {
        val decoration = assertIs<BoxDecoration>(
            assertIs<DecoratedBox>(
                ParserTest.parse("<Snapshot><DecoratedBox color=\"#FFFFFFFF\"/></Snapshot>").createWidget()
            ).decoration
        )

        assertEquals(BoxShape.RECTANGLE, decoration.shape)
        assertNull(decoration.backgroundBlendMode)
    }

    @Test
    fun rejects_invalid_decoration_options() {
        listOf(
            "<Snapshot><DecoratedBox shape=\"INVALID\"/></Snapshot>",
            "<Snapshot><DecoratedBox backgroundBlendMode=\"INVALID\" color=\"#FFFFFFFF\"/></Snapshot>",
            "<Snapshot><DecoratedBox backgroundBlendMode=\"MULTIPLY\"/></Snapshot>",
            "<Snapshot><DecoratedBox shape=\"CIRCLE\" borderRadius=\"8\"/></Snapshot>",
            "<Snapshot><Container foregroundShape=\"CIRCLE\" foregroundBorderRadius=\"8\"/></Snapshot>",
        ).forEach { text ->
            assertFailsWith<ParseException>(text) {
                ParserTest.parse(text).createWidget()
            }
        }
    }
}
