package com.muedsa.geometry

import org.jetbrains.skia.Rect


open class Alignment(val x: Float, val y: Float) {

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

    fun inscribe(size: Size, rect:Rect): Rect {
        val halfWidthDelta: Float = (rect.width - size.width) / 2f
        val halfHeightDelta: Float  = (rect.height - size.height) / 2f
        return Rect.makeXYWH(
            rect.left + halfWidthDelta + x * halfWidthDelta,
            rect.top + halfHeightDelta + y * halfHeightDelta,
            size.width,
            size.height,
        )
    }

    companion object {
        @JvmStatic val TOP_LEFT = Alignment(-1f, -1f)
        @JvmStatic val TOP_CENTER = Alignment(0f, -1f)
        @JvmStatic val TOP_RIGHT = Alignment(1f, -1f)
        @JvmStatic val CENTER_LEFT = Alignment(-1f, 0f)
        @JvmStatic val CENTER = Alignment(0f, 0f)
        @JvmStatic val CENTER_RIGHT = Alignment(1f, 0f)
        @JvmStatic val BOTTOM_LEFT = Alignment(-1f, 1f)
        @JvmStatic val BOTTOM_CENTER = Alignment(0f, 1f)
        @JvmStatic val BOTTOM_RIGHT = Alignment(1f, 1f)
    }
}