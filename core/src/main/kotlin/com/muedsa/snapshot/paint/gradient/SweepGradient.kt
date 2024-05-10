package com.muedsa.snapshot.paint.gradient

import com.muedsa.geometry.AlignmentGeometry
import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.MATH_PI
import com.muedsa.geometry.Offset
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.GradientStyle
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Shader
import org.jetbrains.skia.paragraph.Direction

class SweepGradient(
    val center: AlignmentGeometry = BoxAlignment.CENTER,
    val startAngle: Float = 0f,
    val endAngle: Float = MATH_PI * 2,
    colors: IntArray,
    stops: FloatArray? = null,
    val tileMode: FilterTileMode = FilterTileMode.CLAMP,
    transform: GradientTransform? = null,
) : Gradient(
    colors = colors,
    stops = stops,
    transform = transform
) {
    override fun createShader(rect: Rect, textDirection: Direction?): Shader {
        val centerOffset: Offset = center.resolve(textDirection).withinRect(rect)
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
}