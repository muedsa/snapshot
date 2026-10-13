package com.muedsa.snapshot.paint.decoration

import com.muedsa.geometry.Radius
import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.painterPixels
import org.jetbrains.skia.Color
import org.jetbrains.skia.Pixmap
import org.jetbrains.skia.Rect
import kotlin.test.Test

class BoxBorderTest {

    @Test
    fun inside_border_handles_width_below_equal_to_and_above_radius() {
        for (width in listOf(1, 2, 4)) {
            val pixels = paintBorder(width = width.toFloat())
            expectColorAt(pixels, 60, 19, Color.WHITE)
            expectColorAt(pixels, 60, 20 + width - 1, Color.RED)
            expectColorAt(pixels, 60, 20 + width, Color.WHITE)
            expectColorAt(pixels, 60, 50, Color.WHITE)
            if (width >= 2) {
                // 圆角收缩到零后，内圈角落也应露出背景，不能保留原半径。
                expectColorAt(pixels, 20 + width, 20 + width, Color.WHITE)
            }
        }
    }

    @Test
    fun surviving_corner_radii_are_preserved() {
        val pixels = paintBorder(
            width = 4f,
            radius = BorderRadius.only(
                topLeft = Radius.circular(16f),
                topRight = Radius.elliptical(1f, 3f),
                bottomLeft = Radius.elliptical(3f, 2f),
                bottomRight = Radius.elliptical(2f, 1f),
            ),
        )
        // 左上角仍有半径，不能把整个内圈转换为直角矩形。
        expectColorAt(pixels, 20, 20, Color.WHITE)
        expectColorAt(pixels, 25, 25, Color.RED)
        expectColorAt(pixels, 29, 29, Color.WHITE)
        // 其他角已经退化为直角。
        expectColorAt(pixels, 95, 24, Color.WHITE)
        expectColorAt(pixels, 24, 75, Color.WHITE)
        expectColorAt(pixels, 95, 75, Color.WHITE)
    }

    @Test
    fun centered_border_can_collapse_inner_corners() {
        val pixels = paintBorder(width = 8f, align = BorderSide.STROKE_ALIGN_CENTER)
        expectColorAt(pixels, 60, 15, Color.WHITE)
        expectColorAt(pixels, 60, 18, Color.RED)
        expectColorAt(pixels, 60, 22, Color.RED)
        expectColorAt(pixels, 24, 24, Color.WHITE)
        expectColorAt(pixels, 60, 50, Color.WHITE)
    }

    @Test
    fun outside_border_keeps_rounded_inner_and_outer_contours() {
        val pixels = paintBorder(
            width = 4f,
            align = BorderSide.STROKE_ALIGN_OUTSIDE,
            radius = BorderRadius.circular(8f),
        )
        expectColorAt(pixels, 60, 15, Color.WHITE)
        expectColorAt(pixels, 60, 18, Color.RED)
        expectColorAt(pixels, 60, 22, Color.WHITE)
        expectColorAt(pixels, 16, 16, Color.WHITE)
        expectColorAt(pixels, 21, 21, Color.RED)
        expectColorAt(pixels, 25, 25, Color.WHITE)
    }

    @Test
    fun asymmetric_elliptical_radii_can_all_collapse() {
        val pixels = paintBorder(
            width = 4f,
            radius = BorderRadius.only(
                topLeft = Radius.elliptical(1f, 2f),
                topRight = Radius.elliptical(2f, 3f),
                bottomLeft = Radius.elliptical(3f, 1f),
                bottomRight = Radius.circular(1f),
            ),
        )
        expectColorAt(pixels, 60, 22, Color.RED)
        expectColorAt(pixels, 24, 24, Color.WHITE)
        expectColorAt(pixels, 95, 24, Color.WHITE)
        expectColorAt(pixels, 24, 75, Color.WHITE)
        expectColorAt(pixels, 95, 75, Color.WHITE)
    }

    @Test
    fun negative_outset_can_collapse_outer_corners() {
        // 现有 API 允许自定义对齐值；-3 表示内缩 8、外扩 -4，外圈也会返回 Rect。
        val pixels = paintBorder(width = 4f, align = -3f)
        expectColorAt(pixels, 60, 23, Color.WHITE)
        expectColorAt(pixels, 60, 25, Color.RED)
        expectColorAt(pixels, 24, 24, Color.RED)
        expectColorAt(pixels, 28, 28, Color.WHITE)
        expectColorAt(pixels, 60, 50, Color.WHITE)
    }

    @Test
    fun border_wider_than_half_height_fills_inner_area() {
        val pixels = paintBorder(width = 40f)
        expectColorAt(pixels, 60, 19, Color.WHITE)
        expectColorAt(pixels, 60, 21, Color.RED)
        expectColorAt(pixels, 60, 50, Color.RED)
        expectColorAt(pixels, 60, 79, Color.RED)
        expectColorAt(pixels, 60, 80, Color.WHITE)
    }

    private fun paintBorder(
        width: Float,
        align: Float = BorderSide.STROKE_ALIGN_INSIDE,
        radius: BorderRadius = BorderRadius.circular(2f),
    ): Pixmap = painterPixels(width = 120f, height = 100f) { canvas ->
        Border.all(color = Color.RED, width = width, strokeAlign = align).paint(
            canvas = canvas,
            rect = Rect.makeLTRB(20f, 20f, 100f, 80f),
            borderRadius = radius,
        )
    }
}
