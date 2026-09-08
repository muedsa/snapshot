package com.muedsa.snapshot.paint.gradient

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.MATH_PI
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Shader
import org.jetbrains.skia.paragraph.Direction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** 仅暴露 protected impliedStops(),不改产品面。 */
private class StopsProbe(colors: IntArray, stops: FloatArray? = null) : Gradient(colors, stops) {
    fun exposedStops(): FloatArray = impliedStops()

    // 探针只测 impliedStops(),不产生着色器;补齐抽象成员以实例化。
    override fun createShader(rect: Rect, textDirection: Direction?): Shader =
        throw UnsupportedOperationException("StopsProbe 仅用于 impliedStops 单测")
}

class GradientImpliedStopsTest {

    private fun probe(n: Int, stops: FloatArray? = null): StopsProbe =
        StopsProbe(IntArray(n) { 0xFF000000.toInt() }, stops)

    private fun assertStopsClose(expected: FloatArray, actual: FloatArray, tol: Float = 1e-6f) {
        assertEquals(expected.size, actual.size, "stops 长度不符")
        for (i in expected.indices) {
            assertTrue(
                kotlin.math.abs(expected[i] - actual[i]) <= tol,
                "stops[$i] expected ${expected[i]} but was ${actual[i]}"
            )
        }
    }

    @Test
    fun implied_stops_evenly_spaced() {
        assertStopsClose(floatArrayOf(0f, 1f), probe(2).exposedStops())
        assertStopsClose(floatArrayOf(0f, 0.5f, 1f), probe(3).exposedStops())
        assertStopsClose(floatArrayOf(0f, 0.25f, 0.5f, 0.75f, 1f), probe(5).exposedStops())
        assertStopsClose(floatArrayOf(0f, 0.2f, 0.4f, 0.6f, 0.8f, 1f), probe(6).exposedStops())
    }

    @Test
    fun explicit_stops_returned_as_is() {
        val stops = floatArrayOf(0f, 0.1f, 0.9f, 1f)
        val actual = probe(4, stops).exposedStops()
        assertStopsClose(stops, actual)
        assertTrue(actual === stops, "显式 stops 应原样返回同一引用")
    }

    @Test
    fun sweep_defaults() {
        val colors = intArrayOf(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
        assertEquals(0f, SweepGradient(colors = colors).startAngle)
        assertEquals(MATH_PI * 2, SweepGradient(colors = colors).endAngle)
        assertEquals(BoxAlignment.CENTER, SweepGradient(colors = colors).center)
    }
}
