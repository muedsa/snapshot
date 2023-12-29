package com.muedsa.snapshot.paint

import com.muedsa.geometry.Alignment
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Shader

class LinearGradient(
    val begin: Alignment = Alignment.CENTER_LEFT,
    val end: Alignment = Alignment.CENTER_RIGHT,
    colors: List<Int>,
    stops: List<Float>,
    val tileMode: TileMode = TileMode.CLAMP,
    transform: GradientTransform?
) : Gradient(
    colors = colors,
    stops = stops,
    transform = transform) {


    override fun createShader(rect: Rect): Shader {
        TODO("Not yet implemented")
    }

    override fun scale(factor: Float): Gradient {
        TODO("Not yet implemented")
    }


}