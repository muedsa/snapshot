package com.muedsa.snapshot.paint.decoration

import com.muedsa.geometry.Offset
import org.jetbrains.skia.FilterBlurMode
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BoxShadowTest {

    private fun approx(a: Float, b: Float, tol: Float = 1e-3f) = abs(a - b) <= tol

    @Test
    fun convertRadiusToSigma_bounds_and_formula() {
        assertEquals(0f, BoxShadow.convertRadiusToSigma(0f))
        assertEquals(0f, BoxShadow.convertRadiusToSigma(-5f))
        assertTrue(approx(BoxShadow.convertRadiusToSigma(10f), 10f * 0.57735f + 0.5f))
    }

    @Test
    fun blurSigma_derived() {
        val s = BoxShadow(color = 0xFF000000.toInt(), offset = Offset.ZERO, blurRadius = 4f)
        // 直接对 sigma = radius * 0.57735 + 0.5 独立公式断言,避免与实现纯复述
        assertTrue(approx(s.blurSigma, 4f * 0.57735f + 0.5f))
    }

    @Test
    fun scale_scales_geometry_only() {
        val s = BoxShadow(color = 0xFF112233.toInt(), offset = Offset(2f, 4f), blurRadius = 6f, spreadRadius = 8f)
        val t = s.scale(0.5f)
        assertEquals(0xFF112233.toInt(), t.color)
        assertEquals(Offset(1f, 2f), t.offset)
        assertEquals(3f, t.blurRadius)
        assertEquals(4f, t.spreadRadius)
        assertEquals(s.blurStyle, t.blurStyle)
    }

    @Test
    fun omitted_parameters_use_documented_defaults() {
        val s = BoxShadow(color = 0xFF010203.toInt(), offset = Offset(1f, 2f), blurRadius = 3f)
        assertEquals(0f, s.spreadRadius, "spreadRadius 省略时应为 0")
        assertEquals(FilterBlurMode.NORMAL, s.blurStyle, "blurStyle 省略时应为 NORMAL")
    }

    @Test
    fun toPaint_carries_color_and_blur_mask_filter() {
        val s = BoxShadow(color = 0x80FF0000.toInt(), offset = Offset(5f, 7f), blurRadius = 8f)
        val paint = s.toPaint()
        assertEquals(0x80FF0000.toInt(), paint.color, "画笔颜色应等于阴影颜色")
        // 模糊半径 > 0 → 必须挂上遮罩滤镜;skiko 未暴露滤镜 sigma 的读回,故只断言"已设置"
        assertNotNull(paint.maskFilter, "blurRadius > 0 时应设置模糊遮罩滤镜")
    }
}
