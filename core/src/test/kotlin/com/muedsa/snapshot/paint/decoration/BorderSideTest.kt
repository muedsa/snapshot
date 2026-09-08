package com.muedsa.snapshot.paint.decoration

import org.jetbrains.skia.Color
import org.jetbrains.skia.PaintMode
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BorderSideTest {

    private fun approx(a: Float, b: Float, tol: Float = 1e-3f) = abs(a - b) <= tol

    // 语义对齐 Flutter 与生产绘制(Border.dimensions 用 strokeInset 内缩内容、
    // BoxBorder.paintUniformBorderWithRadius 用 inset 收缩内圈/outset 扩张外圈):
    // strokeInset = 位于边界矩形内部的描边宽度(INSIDE 对齐时为整条 width);
    // strokeOutset = 溢出到矩形外部的描边宽度;strokeOffset = width * strokeAlign。
    // 注:任务参考文本中 INSIDE/OUTSIDE 的 inset/outset 数值互为颠倒,此处以实际实现为准。
    @Test
    fun stroke_geometry_by_align() {
        val inside = BorderSide(width = 10f, strokeAlign = BorderSide.STROKE_ALIGN_INSIDE)
        assertTrue(approx(inside.strokeInset, 10f))
        assertTrue(approx(inside.strokeOutset, 0f))
        assertTrue(approx(inside.strokeOffset, -10f))

        val center = BorderSide(width = 10f, strokeAlign = BorderSide.STROKE_ALIGN_CENTER)
        assertTrue(approx(center.strokeInset, 5f))
        assertTrue(approx(center.strokeOutset, 5f))
        assertTrue(approx(center.strokeOffset, 0f))

        val outside = BorderSide(width = 10f, strokeAlign = BorderSide.STROKE_ALIGN_OUTSIDE)
        assertTrue(approx(outside.strokeInset, 0f))
        assertTrue(approx(outside.strokeOutset, 10f))
        assertTrue(approx(outside.strokeOffset, 10f))
    }

    @Test
    fun scale_zero_or_negative_yields_none() {
        val s = BorderSide(color = Color.RED, width = 4f)
        val t0 = s.scale(0f)
        assertEquals(0f, t0.width)
        assertEquals(BorderStyle.NONE, t0.style)
        assertEquals(Color.RED, t0.color)
        val tn = s.scale(-1f)
        assertEquals(0f, tn.width)
        assertEquals(BorderStyle.NONE, tn.style)
    }

    @Test
    fun scale_positive_keeps_style() {
        val t = BorderSide(color = Color.RED, width = 4f).scale(0.5f)
        assertEquals(2f, t.width)
        assertEquals(BorderStyle.SOLID, t.style)
        assertEquals(Color.RED, t.color)
    }

    @Test
    fun canMerge_rules() {
        val a = BorderSide(color = Color.RED, width = 1f)
        val b = BorderSide(color = Color.RED, width = 2f)
        val c = BorderSide(color = Color.BLUE, width = 1f)
        assertTrue(BorderSide.canMerge(a, b))
        assertTrue(BorderSide.canMerge(a, BorderSide.NONE))
        assertTrue(BorderSide.canMerge(BorderSide.NONE, a))
        assertFalse(BorderSide.canMerge(a, c))
        // none 判定要求 NONE 且 width == 0(width>0 的 NONE 视为另一实边,需颜色风格都一致)
        val hairlineNone = BorderSide(color = Color.RED, width = 1f, style = BorderStyle.NONE)
        assertFalse(BorderSide.canMerge(hairlineNone, a))
    }

    @Test
    fun merge_none_and_solid_cases() {
        assertEquals(BorderSide.NONE, BorderSide.merge(BorderSide.NONE, BorderSide.NONE))
        val a = BorderSide(color = Color.RED, width = 1f)
        assertEquals(a, BorderSide.merge(BorderSide.NONE, a))
        assertEquals(a, BorderSide.merge(a, BorderSide.NONE))
        val sum = BorderSide.merge(
            BorderSide(color = Color.RED, width = 1f),
            BorderSide(color = Color.RED, width = 3f)
        )
        assertEquals(4f, sum.width)
        assertEquals(Color.RED, sum.color)
        assertEquals(BorderStyle.SOLID, sum.style)
    }

    @Test
    fun toPaint_modes() {
        val p = BorderSide(color = Color.RED, width = 4f).toPaint()
        assertEquals(Color.RED, p.color)
        assertEquals(4f, p.strokeWidth)
        assertEquals(PaintMode.STROKE, p.mode)

        val n = BorderSide.NONE.toPaint()
        assertEquals(Color.BLACK, n.color)
        assertEquals(0f, n.strokeWidth)
    }
}
