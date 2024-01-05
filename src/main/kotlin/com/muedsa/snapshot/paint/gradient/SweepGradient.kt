package com.muedsa.snapshot.paint.gradient

import com.muedsa.geometry.Alignment
import com.muedsa.geometry.MATH_PI
import com.muedsa.geometry.Offset
import com.muedsa.snapshot.paint.lerpColor
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.GradientStyle
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Shader

class SweepGradient(
    val center: Alignment = Alignment.CENTER,
    val startAngle: Float = 0f,
    val endAngle: Float = MATH_PI * 2,
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
        val centerOffset: Offset = center.withinRect(rect)
        return Shader.makeSweepGradient(
            x = centerOffset.x,
            y = centerOffset.y,
            startAngle = startAngle / MATH_PI * 180f,
            endAngle = endAngle / MATH_PI * 180f,
            colors = this.colors,
            positions = impliedStops(),
            style = GradientStyle(
                tileMode = tileMode,
                isPremul = true,
                localMatrix = transform?.transform(rect)?.toRMO()?.asMatrix33()
            )
        )
    }

    override fun scale(factor: Float): Gradient = SweepGradient(
        center = center,
        startAngle = startAngle,
        endAngle = endAngle,
        colors = colors.map { lerpColor(null, it, factor)!! }.toIntArray(),
        stops = stops!!,
        tileMode = tileMode,
    )

}