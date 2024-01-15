package com.muedsa.snapshot.paint.gradient

import com.muedsa.geometry.AlignmentGeometry
import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Offset
import com.muedsa.geometry.shortestSide
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.GradientStyle
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Shader
import org.jetbrains.skia.paragraph.Direction

class RadialGradient(
    val center: AlignmentGeometry = BoxAlignment.CENTER,
    val radius: Float = 0.5f,
    colors: IntArray,
    stops: FloatArray? = null,
    val tileMode: FilterTileMode = FilterTileMode.CLAMP,
    val focal: AlignmentGeometry? = null,
    val focalRadius: Float = 0.5f,
    transform: GradientTransform? = null,
) : Gradient(
    colors = colors,
    stops = stops,
    transform = transform
) {

    override fun createShader(rect: Rect, textDirection: Direction?): Shader {
        val centerOffset: Offset = center.resolve(textDirection).withinRect(rect)
        return if (focal == null || (focal == center && focalRadius == 0f)) {
            Shader.makeRadialGradient(
                x = centerOffset.x,
                y = centerOffset.y,
                r = radius * rect.shortestSide,
                colors = colors,
                positions = impliedStops(),
                style = GradientStyle(
                    tileMode = tileMode,
                    isPremul = true,
                    localMatrix = transform?.transform(rect)?.toRMO()?.asMatrix33()
                )
            )
        } else {
            val focalOffset: Offset = focal.resolve(textDirection).withinRect(rect)
            Shader.makeTwoPointConicalGradient(
                x0 = centerOffset.x,
                y0 = centerOffset.y,
                r0 = radius * rect.shortestSide,
                x1 = focalOffset.x,
                y1 = focalOffset.y,
                r1 = focalRadius * rect.shortestSide,
                colors = colors,
                positions = impliedStops(),
                style = GradientStyle(
                    tileMode = tileMode,
                    isPremul = true,
                    localMatrix = transform?.transform(rect)?.toRMO()?.asMatrix33()
                )
            )
        }
    }
}