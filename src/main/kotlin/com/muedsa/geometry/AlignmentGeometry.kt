package com.muedsa.geometry

import org.jetbrains.skia.Rect
import org.jetbrains.skia.paragraph.Direction

abstract class AlignmentGeometry {

    abstract val x: Float
    abstract val start: Float
    abstract val y: Float

    fun alongOffset(other: Offset): Offset {
        val centerX: Float = other.x / 2f
        val centerY: Float = other.y / 2f
        return Offset((centerX + x * centerX), (centerY + y * centerY))
    }
    fun alongSize(other: Size): Offset {
        val centerX: Float = other.width / 2f
        val centerY: Float = other.height / 2f
        return Offset(centerX + x * centerX, centerY + y * centerY)
    }

    fun withinRect(rect: Rect): Offset {
        val halfWidth: Float = rect.width / 2f
        val halfHeight: Float = rect.height / 2f
        return Offset(
            rect.left + halfWidth + x * halfWidth,
            rect.top + halfHeight + y * halfHeight,
        )
    }

    fun inscribe(size: Size, rect: Rect): Rect {
        val halfWidthDelta: Float = (rect.width - size.width) / 2f
        val halfHeightDelta: Float  = (rect.height - size.height) / 2f
        return Rect.makeXYWH(
            rect.left + halfWidthDelta + x * halfWidthDelta,
            rect.top + halfHeightDelta + y * halfHeightDelta,
            size.width,
            size.height,
        )
    }

    abstract fun resolve(direction: Direction?): BoxAlignment


    override fun toString(): String {
        if (start == 0f) {
            return BoxAlignment.stringify(x, y)
        }
        if (x == 0f) {
            return AlignmentDirectional.stringify(start, y)
        }
        return "${BoxAlignment.stringify(x, y)} + ${AlignmentDirectional.stringify(start, 0f)}"
    }
}