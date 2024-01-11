package com.muedsa.snapshot.paint.gradient

import org.jetbrains.skia.Rect
import org.jetbrains.skia.Shader
import org.jetbrains.skia.paragraph.Direction

abstract class Gradient(
    val colors: IntArray,
    val stops: FloatArray? = null,
    val transform: GradientTransform? = null,
) {
    protected fun impliedStops(): FloatArray {
        if (stops != null) {
            return stops
        }
        assert(colors.size >= 2) { "colors list must have at least two colors" }
        val separation: Float  = 1f / (colors.size - 1)
        val newStops = FloatArray(colors.size)
        for (i in colors.indices) {
            newStops[i] = i * separation
        }
        return newStops
    }
    abstract fun createShader(rect: Rect, textDirection: Direction? = null): Shader

    abstract fun scale(factor: Float): Gradient

    protected fun lerpFrom(a: Gradient?, t: Float): Gradient? {
        if (a == null) {
            return scale(t)
        }
        return null
    }

    protected fun lerpTo(b: Gradient?, t: Float): Gradient? {
        if (b == null) {
            return scale(1f - t)
        }
        return null
    }

    private fun resolveTransform(bounds: Rect): FloatArray? = transform?.transform(bounds)?.mat

    companion object {

        @JvmStatic
        fun lerp(a: Gradient?, b: Gradient?, t: Float): Gradient? {
            if (a == b) {
                return a
            }
            var result: Gradient? = null
            if (b != null) {
                result = b.lerpFrom(a, t) // if a is null, this must return non-null
            }
            if (result == null && a != null) {
                result = a.lerpTo(b, t) // if b is null, this must return non-null
            }
            if (result != null) {
                return result
            }
            assert(a != null && b != null)
            return if(t < 0.5f) a!!.scale(1f - (t * 2f)) else b!!.scale((t - 0.5f) * 2f)
        }

        @JvmStatic
        fun linear() {

        }
    }
}