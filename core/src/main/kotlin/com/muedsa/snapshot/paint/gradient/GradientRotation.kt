package com.muedsa.snapshot.paint.gradient

import com.muedsa.geometry.Matrix44CMO
import com.muedsa.geometry.Offset
import com.muedsa.geometry.center
import org.jetbrains.skia.Rect
import kotlin.math.cos
import kotlin.math.sin

class GradientRotation(val radians: Float) : GradientTransform() {
    override fun transform(bounds: Rect): Matrix44CMO {
        val sinRadians: Float = sin(radians)
        val oneMinusCosRadians: Float = 1 - cos(radians)
        val center: Offset = bounds.center
        val originX: Float = sinRadians * center.y + oneMinusCosRadians * center.x
        val originY: Float = -sinRadians * center.x + oneMinusCosRadians * center.y
        return Matrix44CMO.identity().apply {
            translate(originX, originY)
            rotateZ(radians)
        }
    }
}