package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.Radius
import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.snapshotPixels
import com.muedsa.snapshot.widget.ClipOval
import com.muedsa.snapshot.widget.ClipRRect
import com.muedsa.snapshot.widget.ClipRect
import com.muedsa.snapshot.widget.Container
import org.jetbrains.skia.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class ClipParserTest {

    @Test
    fun clip_rect_defaults_to_hard_edge() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot><ClipRect><Container width="100" height="80"/></ClipRect></Snapshot>
            """.trimIndent()
        )

        val widget = assertIs<ClipRect>(snapshot.createWidget())
        assertEquals(ClipBehavior.HARD_EDGE, widget.clipBehavior)
        assertIs<Container>(widget.child)
    }

    @Test
    fun clip_oval_defaults_to_antialias_and_clips_corners() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot background="#FFFFFFFF">
                <ClipOval>
                    <Container width="100" height="100" color="#FF00FF00"/>
                </ClipOval>
            </Snapshot>
            """.trimIndent()
        )

        val widget = assertIs<ClipOval>(snapshot.createWidget())
        assertEquals(ClipBehavior.ANTI_ALIAS, widget.clipBehavior)
        val pixels = snapshotPixels { attach(widget) }
        expectColorAt(pixels, 50, 50, Color.GREEN)
        expectColorAt(pixels, 0, 0, Color.WHITE)
    }

    @Test
    fun clip_rrect_parses_per_corner_radius_and_behavior() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot>
                <ClipRRect borderRadius="8" borderRadiusTopLeft="(12,6)"
                           clipBehavior="ANTI_ALIAS_WITH_SAVE_LAYER">
                    <Container width="100" height="80"/>
                </ClipRRect>
            </Snapshot>
            """.trimIndent()
        )

        val widget = assertIs<ClipRRect>(snapshot.createWidget())
        assertEquals(Radius.elliptical(12f, 6f), widget.borderRadius.topLeft)
        assertEquals(Radius.circular(8f), widget.borderRadius.topRight)
        assertEquals(Radius.circular(8f), widget.borderRadius.bottomLeft)
        assertEquals(Radius.circular(8f), widget.borderRadius.bottomRight)
        assertEquals(ClipBehavior.ANTI_ALIAS_WITH_SAVE_LAYER, widget.clipBehavior)
    }

    @Test
    fun rejects_unknown_clip_behavior() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot><ClipOval clipBehavior="SOFT"><Container width="10" height="10"/></ClipOval></Snapshot>
            """.trimIndent()
        )

        assertFailsWith<ParseException> { snapshot.createWidget() }
    }
}
