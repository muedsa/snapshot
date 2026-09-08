package com.muedsa.snapshot.paint.decoration

import com.muedsa.geometry.Offset
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
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
}
