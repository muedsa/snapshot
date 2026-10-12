package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.Size
import com.muedsa.snapshot.assertApproxEq
import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.parser.token.RawAttr
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.snapshotPixels
import com.muedsa.snapshot.widget.ClipPath
import com.muedsa.snapshot.widget.Container
import org.jetbrains.skia.Color
import org.jetbrains.skia.PathFillMode
import org.jetbrains.skia.Rect
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ClipPathParserTest {

    @Test
    fun defaults_keep_local_coordinates_and_child_size() {
        val widget = parseClip("""path="M10 10 H50 V40 H10 Z"""")

        assertEquals(ClipBehavior.ANTI_ALIAS, widget.clipBehavior)
        assertIs<Container>(widget.child)
        val path = widget.clipper!!(Size(120f, 80f))
        assertEquals(PathFillMode.WINDING, path.fillMode)
        assertEquals(Rect.makeLTRB(10f, 10f, 50f, 40f), path.bounds)
        val pixels = snapshotPixels { attach(widget) }
        assertEquals(120, pixels.info.width)
        assertEquals(80, pixels.info.height)
        expectColorAt(pixels, 20, 20, Color.GREEN)
        expectColorAt(pixels, 80, 40, Color.WHITE)
    }

    @Test
    fun svg_line_commands_support_relative_coordinates_and_implicit_repetition() {
        val widget = parseClip("""path="m10 10 40 0 v40 h-40 z"""")
        val pixels = snapshotPixels { attach(widget) }
        expectColorAt(pixels, 30, 30, Color.GREEN)
        expectColorAt(pixels, 60, 30, Color.WHITE)
    }

    @Test
    fun quadratic_and_cubic_curves_clip_pixels() {
        for (path in listOf(
            "M10 60 Q50 -20 90 60 Z",
            "M10 60 C10 0 90 0 90 60 Z",
            "m10 60 q40 -80 80 0 z",
            "m10 60 c0 -60 80 -60 80 0 z",
        )) {
            val pixels = snapshotPixels { attach(parseClip("""path="$path"""")) }
            expectColorAt(pixels, 50, 40, Color.GREEN)
            expectColorAt(pixels, 10, 10, Color.WHITE)
            expectColorAt(pixels, 50, 70, Color.WHITE)
        }
    }

    @Test
    fun elliptical_arcs_clip_a_circle() {
        val widget = parseClip("""path="M50 10 A30 30 0 1 1 50 70 a30 30 0 1 1 0 -60 Z"""")
        val pixels = snapshotPixels { attach(widget) }
        expectColorAt(pixels, 50, 40, Color.GREEN)
        expectColorAt(pixels, 25, 15, Color.WHITE)
        expectColorAt(pixels, 85, 40, Color.WHITE)
    }

    @Test
    fun smooth_curve_commands_are_supported() {
        for (path in listOf(
            "M10 40 Q30 0 50 40 T90 40 L90 70 H10 Z",
            "M10 40 C10 0 50 0 50 40 S90 80 90 40 L90 70 H10 Z",
            "m10 40 q20 -40 40 0 t40 0 l0 30 h-80 z",
            "m10 40 c0 -40 40 -40 40 0 s40 40 40 0 l0 30 h-80 z",
        )) {
            val pixels = snapshotPixels { attach(parseClip("""path="$path"""")) }
            expectColorAt(pixels, 30, 50, Color.GREEN)
            expectColorAt(pixels, 30, 5, Color.WHITE)
        }
    }

    @Test
    fun fill_rule_controls_holes_in_same_direction_contours() {
        val contours = "M10 10 H90 V70 H10 Z M30 30 H70 V50 H30 Z"
        for ((rule, centerColor) in listOf("NON_ZERO" to Color.GREEN, "EVEN_ODD" to Color.WHITE)) {
            val widget = parseClip("""path="$contours" fillRule="$rule"""")
            val pixels = snapshotPixels { attach(widget) }
            expectColorAt(pixels, 20, 20, Color.GREEN)
            expectColorAt(pixels, 50, 40, centerColor)
            expectColorAt(pixels, 5, 5, Color.WHITE)
        }
    }

    @Test
    fun view_box_fill_translates_origin_and_scales_axes_independently() {
        val widget = parseClip("""path="M10 20 H60 V70 H10 Z" viewBox="10 20 100 100"""")
        assertBounds(Rect.makeLTRB(0f, 0f, 60f, 40f), widget.clipper!!(Size(120f, 80f)).bounds)
        val pixels = snapshotPixels { attach(widget) }
        assertEquals(120, pixels.info.width)
        assertEquals(80, pixels.info.height)
        expectColorAt(pixels, 20, 20, Color.GREEN)
        expectColorAt(pixels, 80, 20, Color.WHITE)
        expectColorAt(pixels, 20, 60, Color.WHITE)
    }

    @Test
    fun contain_preserves_aspect_ratio_and_centers_in_both_orientations() {
        val widget = parseClip("""path="M-10 -20 H90 V80 H-10 Z" viewBox="-10,-20,1e2,100" fit="CONTAIN"""")
        assertBounds(Rect.makeLTRB(20f, 0f, 100f, 80f), widget.clipper!!(Size(120f, 80f)).bounds)
        assertBounds(Rect.makeLTRB(0f, 20f, 80f, 100f), widget.clipper!!(Size(80f, 120f)).bounds)
        val pixels = snapshotPixels { attach(widget) }
        expectColorAt(pixels, 60, 40, Color.GREEN)
        expectColorAt(pixels, 10, 40, Color.WHITE)
        expectColorAt(pixels, 110, 40, Color.WHITE)
    }

    @Test
    fun view_box_does_not_add_an_extra_rectangular_clip() {
        val widget = parseClip("""path="M-10 0 H110 V100 H-10 Z" viewBox="0 0 100 100" fit="CONTAIN"""")
        val pixels = snapshotPixels { attach(widget) }
        expectColorAt(pixels, 15, 40, Color.GREEN)
        expectColorAt(pixels, 105, 40, Color.GREEN)
        expectColorAt(pixels, 5, 40, Color.WHITE)
    }

    @Test
    fun all_clip_behaviors_are_supported() {
        for (behavior in ClipBehavior.entries) {
            val widget = parseClip("""path="M10 10 H50 V50 H10 Z" clipBehavior="${behavior.name}"""")
            assertEquals(behavior, widget.clipBehavior)
            val pixels = snapshotPixels { attach(widget) }
            expectColorAt(pixels, 30, 30, Color.GREEN)
            expectColorAt(pixels, 80, 30, if (behavior == ClipBehavior.NONE) Color.GREEN else Color.WHITE)
        }
    }

    @Test
    fun fit_without_view_box_keeps_local_coordinates() {
        val widget = parseClip("""path="M10 10 H50 V50 H10 Z" fit="CONTAIN"""")
        assertEquals(Rect.makeLTRB(10f, 10f, 50f, 50f), widget.clipper!!(Size(120f, 80f)).bounds)
    }

    @Test
    fun view_box_accepts_mixed_svg_separators_and_decimal_numbers() {
        val widget = parseClip("path=\"M.5 -2 H10.5 V8 H.5 Z\" viewBox=\" \t.5 , -2\n1e1, +10. \"")
        assertBounds(Rect.makeLTRB(0f, 0f, 120f, 80f), widget.clipper!!(Size(120f, 80f)).bounds)
    }

    @Test
    fun zero_size_does_not_produce_non_finite_paths_or_mutate_source_path() {
        for (fit in listOf("FILL", "CONTAIN")) {
            val widget = parseClip("""path="M0 0 H100 V100 H0 Z" viewBox="0 0 100 100" fit="$fit"""")
            for (size in listOf(Size.ZERO, Size(0f, 80f), Size(120f, 0f))) {
                val path = widget.clipper!!(size)
                assertTrue(path.isFinite)
                assertTrue(path.bounds.width == 0f || path.bounds.height == 0f)
            }
            assertBounds(Rect.makeLTRB(0f, 0f, 100f, 100f), widget.clipper!!(Size(100f, 100f)).bounds)
        }
    }

    @Test
    fun unclosed_contour_uses_filled_area_for_clipping() {
        val pixels = snapshotPixels { attach(parseClip("""path="M10 10 H90 V70"""")) }
        expectColorAt(pixels, 70, 20, Color.GREEN)
        expectColorAt(pixels, 20, 60, Color.WHITE)
    }

    @Test
    fun usage_examples_render_inside_sized_box_with_scaled_holes() {
        val badge = ParserTest.parse(
            """
            <Snapshot>
                <SizedBox width="240" height="160">
                    <ClipPath path="M0 0 H100 V75 Q50 125 0 75 Z"
                              viewBox="0 0 100 100" fit="FILL">
                        <Container color="#3F51B5"/>
                    </ClipPath>
                </SizedBox>
            </Snapshot>
            """.trimIndent()
        ).createWidget()
        val badgePixels = snapshotPixels { attach(badge) }
        assertEquals(240, badgePixels.info.width)
        assertEquals(160, badgePixels.info.height)
        expectColorAt(badgePixels, 120, 140, 0xFF3F51B5.toInt())
        expectColorAt(badgePixels, 10, 150, Color.WHITE)

        val ring = ParserTest.parse(
            """
            <Snapshot>
                <SizedBox width="240" height="160">
                    <ClipPath path="M0 0 H100 V100 H0 Z M25 25 H75 V75 H25 Z"
                              viewBox="0 0 100 100" fit="CONTAIN" fillRule="EVEN_ODD">
                        <Container color="#00C853"/>
                    </ClipPath>
                </SizedBox>
            </Snapshot>
            """.trimIndent()
        ).createWidget()
        val ringPixels = snapshotPixels { attach(ring) }
        expectColorAt(ringPixels, 60, 20, 0xFF00C853.toInt())
        expectColorAt(ringPixels, 120, 80, Color.WHITE)
        expectColorAt(ringPixels, 20, 80, Color.WHITE)
    }

    @Test
    fun missing_path_reports_tag_position() {
        val source = "<Snapshot>\n  <ClipPath/>\n</Snapshot>"
        val error = assertFailsWith<ParseException> { ParserTest.parse(source).createWidget() }
        assertContains(error.message.orEmpty(), "path")
        assertEquals(source.indexOf("<ClipPath"), error.pos.pos)
    }

    @Test
    fun malformed_attributes_report_value_position() {
        for ((name, value) in listOf(
            "path" to "",
            "path" to "   ",
            "path" to "M10 10 L",
            "path" to "not-a-path",
            "path" to "M0 0 L1e100 10 Z",
            "viewBox" to "",
            "viewBox" to "0 0 100",
            "viewBox" to "0 0 100 100 100",
            "viewBox" to "0 0 0 100",
            "viewBox" to "0 0 100 -1",
            "viewBox" to "0 0 NaN 100",
            "viewBox" to "Infinity 0 100 100",
            "viewBox" to "0 0 1e100 100",
            "viewBox" to "0,,0,100,100",
            "viewBox" to "0 0 100 100,",
            "viewBox" to "0 0 100px 100",
            "fit" to "COVER",
            "fillRule" to "INVERSE_EVEN_ODD",
            "clipBehavior" to "SOFT",
        )) {
            val basePath = if (name == "path") "" else "path=\"M0 0 H10 V10 Z\" "
            val source = "<Snapshot>\n  <ClipPath $basePath$name=\"$value\"/>\n</Snapshot>"
            val error = assertFailsWith<ParseException>("$name=$value") { ParserTest.parse(source).createWidget() }
            assertContains(error.message.orEmpty(), name)
            val expectedPos = source.indexOf("$name=\"") + if (value.isEmpty()) 0 else name.length + 2
            assertEquals(expectedPos, error.pos.pos, "$name=$value")
            assertEquals(2, error.pos.line)
        }
    }

    @Test
    fun rejects_nul_before_passing_path_to_native_parser() {
        assertFailsWith<IllegalArgumentException> {
            ClipPathParser.PATH.parseValue(RawAttr("path", "M0 0 H10 V10 Z\u0000ignored"))
        }
    }

    @Test
    fun valueless_attributes_report_name_position() {
        for (name in listOf("path", "viewBox", "fit", "fillRule", "clipBehavior")) {
            val basePath = if (name == "path") "" else "path=\"M0 0 H10 V10 Z\" "
            val source = "<Snapshot><ClipPath $basePath$name/></Snapshot>"
            val error = assertFailsWith<ParseException> { ParserTest.parse(source).createWidget() }
            assertContains(error.message.orEmpty(), name)
            assertEquals(source.indexOf("$name/>"), error.pos.pos)
        }
    }

    @Test
    fun rejects_multiple_children() {
        assertFailsWith<ParseException> {
            ParserTest.parse("""<Snapshot><ClipPath path="M0 0 H10 V10 Z"><SizedBox/><SizedBox/></ClipPath></Snapshot>""")
        }
    }

    private fun parseClip(attrs: String): ClipPath = assertIs<ClipPath>(
        ParserTest.parse(
            """<Snapshot><ClipPath $attrs><Container width="120" height="80" color="#00FF00"/></ClipPath></Snapshot>"""
        ).createWidget()
    )

    private fun assertBounds(expected: Rect, actual: Rect) {
        assertApproxEq(actual.left, expected.left)
        assertApproxEq(actual.top, expected.top)
        assertApproxEq(actual.right, expected.right)
        assertApproxEq(actual.bottom, expected.bottom)
    }
}
