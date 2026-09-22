package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.paint.gradient.GradientRotation
import com.muedsa.snapshot.paint.gradient.LinearGradient
import com.muedsa.snapshot.paint.gradient.RadialGradient
import com.muedsa.snapshot.paint.gradient.SweepGradient
import com.muedsa.snapshot.paint.decoration.BoxDecoration
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.snapshotPixels
import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.DecoratedBox
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Color
import org.jetbrains.skia.FilterTileMode
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GradientParserTest {

    @Test
    fun parses_linear_gradient_for_container() {
        val container = assertIs<Container>(
            ParserTest.parse(
                """
                <Snapshot>
                    <Container width="100" height="50"
                               gradientType="LINEAR"
                               gradientColors="#FFFF0000, #FF00FF00, #FF0000FF"
                               gradientStops="0, 0.4, 1"
                               gradientBegin="TOP_LEFT"
                               gradientEnd="BOTTOM_RIGHT"
                               gradientTileMode="MIRROR"
                               gradientRotation="0.5"
                               backgroundBlendMode="MULTIPLY"/>
                </Snapshot>
                """.trimIndent()
            ).createWidget()
        )

        assertNull(container.color)
        val decoration = assertIs<BoxDecoration>(container.decoration)
        val gradient = assertIs<LinearGradient>(decoration.gradient)
        assertContentEquals(intArrayOf(Color.RED, Color.GREEN, Color.BLUE), gradient.colors)
        assertContentEquals(floatArrayOf(0f, 0.4f, 1f), gradient.stops)
        assertEquals(BoxAlignment.TOP_LEFT, gradient.begin)
        assertEquals(BoxAlignment.BOTTOM_RIGHT, gradient.end)
        assertEquals(FilterTileMode.MIRROR, gradient.tileMode)
        assertEquals(0.5f, assertIs<GradientRotation>(gradient.transform).radians)
        assertEquals(BlendMode.MULTIPLY, decoration.backgroundBlendMode)

        val pixels = snapshotPixels(background = Color.TRANSPARENT) { attach(container) }
        assertEquals(100, pixels.info.width)
        assertEquals(50, pixels.info.height)
        assertTrue(((pixels.getColor(50, 25) ushr 24) and 0xFF) > 0, "渐变中心应产生非透明像素")
    }

    @Test
    fun parses_radial_gradient_for_decorated_box() {
        val decoratedBox = assertIs<DecoratedBox>(
            ParserTest.parse(
                """
                <Snapshot>
                    <DecoratedBox gradientType="RADIAL"
                                  gradientColors="#FFFFFFFF,#FF000000"
                                  gradientCenter="TOP_CENTER"
                                  gradientRadius="0.8"
                                  gradientFocal="BOTTOM_CENTER"
                                  gradientFocalRadius="0.2"/>
                </Snapshot>
                """.trimIndent()
            ).createWidget()
        )

        val gradient = assertIs<RadialGradient>(assertIs<BoxDecoration>(decoratedBox.decoration).gradient)
        assertEquals(BoxAlignment.TOP_CENTER, gradient.center)
        assertEquals(0.8f, gradient.radius)
        assertEquals(BoxAlignment.BOTTOM_CENTER, gradient.focal)
        assertEquals(0.2f, gradient.focalRadius)
        assertEquals(FilterTileMode.CLAMP, gradient.tileMode)
        assertNull(gradient.stops)
        assertNull(gradient.transform)
    }

    @Test
    fun parses_sweep_gradient_and_defaults() {
        val decoratedBox = assertIs<DecoratedBox>(
            ParserTest.parse(
                """
                <Snapshot>
                    <Border gradientType="SWEEP"
                            gradientColors="#FFFF0000,#FF0000FF"
                            gradientCenter="CENTER_RIGHT"
                            gradientStartAngle="0.25"
                            gradientEndAngle="5.5"/>
                </Snapshot>
                """.trimIndent()
            ).createWidget()
        )

        val gradient = assertIs<SweepGradient>(assertIs<BoxDecoration>(decoratedBox.decoration).gradient)
        assertEquals(BoxAlignment.CENTER_RIGHT, gradient.center)
        assertEquals(0.25f, gradient.startAngle)
        assertEquals(5.5f, gradient.endAngle)
        assertContentEquals(intArrayOf(Color.RED, Color.BLUE), gradient.colors)
    }

    @Test
    fun parses_foreground_gradient() {
        val container = assertIs<Container>(
            ParserTest.parse(
                """
                <Snapshot>
                    <Container width="40" height="40"
                               foregroundGradientType="LINEAR"
                               foregroundGradientColors="#66FFFFFF,#00000000"/>
                </Snapshot>
                """.trimIndent()
            ).createWidget()
        )

        val foreground = assertIs<BoxDecoration>(container.foregroundDecoration)
        val gradient = assertIs<LinearGradient>(foreground.gradient)
        assertEquals(BoxAlignment.CENTER_LEFT, gradient.begin)
        assertEquals(BoxAlignment.CENTER_RIGHT, gradient.end)
        assertNull(container.decoration)
    }

    @Test
    fun rejects_invalid_gradient_options() {
        listOf(
            "<Snapshot><Border gradientType=\"INVALID\" gradientColors=\"#FFFF0000,#FF0000FF\"/></Snapshot>",
            "<Snapshot><Border gradientType=\"LINEAR\"/></Snapshot>",
            "<Snapshot><Border gradientColors=\"#FFFF0000,#FF0000FF\"/></Snapshot>",
            "<Snapshot><Border gradientType=\"LINEAR\" gradientColors=\"#FFFF0000\"/></Snapshot>",
            "<Snapshot><Border gradientType=\"LINEAR\" gradientColors=\"#FFFF0000,#FF0000FF\" gradientStops=\"0\"/></Snapshot>",
            "<Snapshot><Border gradientType=\"LINEAR\" gradientColors=\"#FFFF0000,#FF0000FF\" gradientStops=\"0,1.1\"/></Snapshot>",
            "<Snapshot><Border gradientType=\"LINEAR\" gradientColors=\"#FFFF0000,#FF0000FF\" gradientStops=\"0.8,0.2\"/></Snapshot>",
            "<Snapshot><Border gradientType=\"LINEAR\" gradientColors=\"#FFFF0000,#FF0000FF\" gradientRadius=\"0.5\"/></Snapshot>",
            "<Snapshot><Border gradientType=\"RADIAL\" gradientColors=\"#FFFF0000,#FF0000FF\" gradientRadius=\"0\"/></Snapshot>",
            "<Snapshot><Border gradientType=\"RADIAL\" gradientColors=\"#FFFF0000,#FF0000FF\" gradientFocalRadius=\"0.2\"/></Snapshot>",
            "<Snapshot><Border gradientType=\"SWEEP\" gradientColors=\"#FFFF0000,#FF0000FF\" gradientStartAngle=\"2\" gradientEndAngle=\"1\"/></Snapshot>",
            "<Snapshot><Border gradientType=\"SWEEP\" gradientColors=\"#FFFF0000,#FF0000FF\" gradientRotation=\"NaN\"/></Snapshot>",
        ).forEach { text ->
            assertFailsWith<ParseException>(text) {
                ParserTest.parse(text).createWidget()
            }
        }
    }
}
