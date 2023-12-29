package com.muedsa.snapshot.paint

import org.jetbrains.skia.Color

fun lerpFloat(a: Float, b: Float, t: Float): Float = a * (1f - t) + b * t

fun lerpInt(a: Int, b: Int, t: Float): Float = a + (b - a) * t

fun lerpColor(a: Int?, b: Int?, t: Float): Int? {
    return if (b == null) {
        if (a == null) {
            null
        } else {
            Color.scaleAlpha(a, 1f - t)
        }
    } else {
        if (a == null) {
            Color.scaleAlpha(b, t)
        } else {
            Color.makeARGB(
                a = lerpInt(Color.getA(a), Color.getA(b), t).toInt().coerceIn(0, 255),
                r = lerpInt(Color.getR(a), Color.getR(b), t).toInt().coerceIn(0, 255),
                g = lerpInt(Color.getG(a), Color.getG(b), t).toInt().coerceIn(0, 255),
                b = lerpInt(Color.getB(a), Color.getB(b), t).toInt().coerceIn(0, 255)
            )
        }
    }
}
