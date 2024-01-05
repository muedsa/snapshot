package com.muedsa.snapshot.paint.gradient

import com.muedsa.geometry.Alignment
import com.muedsa.snapshot.paint.lerpColor
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.GradientStyle
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Shader

class LinearGradient(
    val begin: Alignment = Alignment.CENTER_LEFT,
    val end: Alignment = Alignment.CENTER_RIGHT,
    colors: IntArray,
    stops: FloatArray? = null,
    val tileMode: FilterTileMode = FilterTileMode.CLAMP,
    transform: GradientTransform? = null
) : Gradient(
    colors = colors,
    stops = stops,
    transform = transform
) {


    override fun createShader(rect: Rect): Shader {
        val beginOffset = begin.withinRect(rect)
        val endOffset = end.withinRect(rect)
        return Shader.makeLinearGradient(
            x0 = beginOffset.x,
            y0 = beginOffset.y,
            x1 = endOffset.x,
            y1 = endOffset.y,
            colors = colors,
            positions = impliedStops(),
            style = GradientStyle(
                tileMode = tileMode,
                isPremul = false,
                localMatrix = transform?.transform(rect)?.toRMO()?.asMatrix33()
            )
        )
    }

    override fun scale(factor: Float): Gradient = LinearGradient(
        begin = begin,
        end = end,
        colors = colors.map { lerpColor(null, it, factor)!! }.toIntArray(),
        stops = stops!!,
        tileMode = tileMode
    )


}