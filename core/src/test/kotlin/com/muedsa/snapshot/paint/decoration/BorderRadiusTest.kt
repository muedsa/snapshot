package com.muedsa.snapshot.paint.decoration

import com.muedsa.geometry.Radius
import com.muedsa.geometry.blRadiusX
import com.muedsa.geometry.blRadiusY
import com.muedsa.geometry.brRadiusX
import com.muedsa.geometry.brRadiusY
import com.muedsa.geometry.tlRadiusX
import com.muedsa.geometry.tlRadiusY
import com.muedsa.geometry.trRadiusX
import com.muedsa.geometry.trRadiusY
import org.jetbrains.skia.Rect
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BorderRadiusTest {

    private fun approx(a: Float, b: Float, tol: Float = 1e-3f) = abs(a - b) <= tol

    private fun r(v: Float) = Radius.circular(v)

    @Test
    fun factories_and_zero() {
        val all = BorderRadius.all(r(5f))
        listOf(all.topLeft, all.topRight, all.bottomLeft, all.bottomRight).forEach { assertEquals(r(5f), it) }

        val circular = BorderRadius.circular(3f)
        assertEquals(r(3f), circular.topLeft)
        assertEquals(r(3f), circular.bottomRight)

        val vertical = BorderRadius.vertical(top = r(1f), bottom = r(2f))
        assertEquals(r(1f), vertical.topLeft)
        assertEquals(r(1f), vertical.topRight)
        assertEquals(r(2f), vertical.bottomLeft)
        assertEquals(r(2f), vertical.bottomRight)

        val horizontal = BorderRadius.horizontal(left = r(4f), right = r(8f))
        assertEquals(r(4f), horizontal.topLeft)
        assertEquals(r(4f), horizontal.topRight)
        assertEquals(r(8f), horizontal.bottomLeft)
        assertEquals(r(8f), horizontal.bottomRight)

        // BorderRadius 经 BorderRadiusGeometry.equals 比较各角,默认角均为 ZERO 时与 ZERO 相等
        assertEquals(BorderRadius.ZERO, BorderRadius.only())
    }

    @Test
    fun cornerwise_operators() {
        val a = BorderRadius.only(topLeft = r(1f), topRight = r(2f), bottomLeft = r(3f), bottomRight = r(4f))
        val b = BorderRadius.only(topLeft = r(10f), topRight = r(20f), bottomLeft = r(30f), bottomRight = r(40f))

        // a + b 逐角相加
        assertEquals(r(11f), (a + b).topLeft)
        assertEquals(r(22f), (a + b).topRight)
        assertEquals(r(33f), (a + b).bottomLeft)
        assertEquals(r(44f), (a + b).bottomRight)

        // b - a 逐角相减
        assertEquals(r(9f), (b - a).topLeft)
        assertEquals(r(18f), (b - a).topRight)
        assertEquals(Radius.ZERO, (a - a).topLeft)

        // 一元负号
        assertEquals(Radius.circular(-1f), (-a).topLeft)
        assertEquals(Radius.circular(-4f), (-a).bottomRight)

        // 与标量的四则运算逐角生效
        assertEquals(r(11f), (a + 10f).topLeft)
        assertEquals(r(14f), (a + 10f).bottomRight)
        assertEquals(r(-9f), (a - 10f).topLeft)
        assertEquals(r(-6f), (a - 10f).bottomRight)
        assertEquals(r(0.5f), (a / 2f).topLeft)
        assertEquals(r(2f), (a / 2f).bottomRight)
        assertEquals(r(3f), (a * 3f).topLeft)
        assertEquals(r(12f), (a * 3f).bottomRight)
        assertEquals(r(1f), (a % 2f).topLeft)
        assertEquals(Radius.ZERO, (a % 2f).bottomRight)
    }

    @Test
    fun copyWith() {
        val a = BorderRadius.circular(2f)
        val b = a.copyWith(topRight = r(9f))
        assertEquals(r(2f), b.topLeft)
        assertEquals(r(9f), b.topRight)
        assertEquals(r(2f), b.bottomRight)
        assertEquals(r(2f), b.bottomLeft)
        assertEquals(a, a.copyWith())
    }

    @Test
    fun toRRect_shape_and_corner_radius() {
        val rect = Rect.makeXYWH(0f, 0f, 100f, 50f)
        val br = BorderRadius.circular(10f)
        val rr = br.toRRect(rect)
        // skia RRect 继承 Rect,直接读外框
        assertTrue(approx(rr.left, 0f) && approx(rr.top, 0f))
        assertTrue(approx(rr.right, 100f))
        assertTrue(approx(rr.bottom, 50f))
        assertTrue(approx(rr.width, 100f))
        assertTrue(approx(rr.height, 50f))
        // 角半径经 com.muedsa.geometry.RRect 扩展读取,四角 x/y 均 ≈10
        assertTrue(approx(rr.tlRadiusX, 10f) && approx(rr.tlRadiusY, 10f))
        assertTrue(approx(rr.trRadiusX, 10f) && approx(rr.trRadiusY, 10f))
        assertTrue(approx(rr.brRadiusX, 10f) && approx(rr.brRadiusY, 10f))
        assertTrue(approx(rr.blRadiusX, 10f) && approx(rr.blRadiusY, 10f))
    }
}
