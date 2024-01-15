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
        val separation: Float = 1f / (colors.size - 1)
        val newStops = FloatArray(colors.size)
        for (i in colors.indices) {
            newStops[i] = i * separation
        }
        return newStops
    }

    abstract fun createShader(rect: Rect, textDirection: Direction? = null): Shader

    private fun resolveTransform(bounds: Rect): FloatArray? = transform?.transform(bounds)?.mat
}