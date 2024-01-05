package com.muedsa.snapshot.paint.gradient

import com.muedsa.geometry.Alignment
import com.muedsa.geometry.Offset
import com.muedsa.geometry.shortestSide
import com.muedsa.snapshot.paint.lerpColor
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.GradientStyle
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Shader

class RadialGradient(
    val center: Alignment = Alignment.CENTER,
    val radius: Float = 0.5f,
    colors: IntArray,
    stops: FloatArray? = null,
    val tileMode: FilterTileMode = FilterTileMode.CLAMP,
    val focal: Alignment? = null,
    val focalRadius: Float = 0.5f,
    transform: GradientTransform? = null
) : Gradient(
    colors = colors,
    stops = stops,
    transform = transform
) {

    override fun createShader(rect: Rect): Shader {
        val centerOffset: Offset = center.withinRect(rect)
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
            val focalOffset: Offset = focal.withinRect(rect)
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

    override fun scale(factor: Float): Gradient = RadialGradient(
        center = center,
        radius = radius,
        colors = colors.map { lerpColor(null, it, factor)!! }.toIntArray(),
        stops = stops!!,
        tileMode = tileMode,
        focal = focal,
        focalRadius= focalRadius
    )
}