package com.muedsa.snapshot.rendering.stack

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import org.jetbrains.skia.Rect
import org.jetbrains.skia.paragraph.Direction
import kotlin.math.max


class RelativeRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val hasInsets: Boolean =  left > 0f || top > 0f || right > 0f || bottom > 0f


    fun shift(offset: Offset): RelativeRect = fromLTRB(
        left + offset.x, top + offset.y, right - offset.x, bottom - offset.y
    )

    fun inflate(delta: Float): RelativeRect = fromLTRB(
        left - delta, top - delta, right - delta, bottom - delta
    )

    fun deflate(delta: Float): RelativeRect = inflate(-delta)

    fun intersect(other: RelativeRect): RelativeRect = fromLTRB(
        max(left, other.left),
        max(top, other.top),
        max(right, other.right),
        max(bottom, other.bottom)
    )

    fun toRect(container: Rect): Rect = Rect.makeLTRB(
        left, top, container.width - right, container.height - bottom
    )


    fun toSize(container: Size): Size =  Size(
        container.width - left - right, container.height - top - bottom
    )

    override fun toString(): String {
        return "RelativeRect(left=$left, top=$top, right=$right, bottom=$bottom, hasInsets=$hasInsets)"
    }

    companion object {

        @JvmStatic
        fun fromLTRB(left: Float, top: Float, right: Float, bottom: Float) = RelativeRect(left, top, right, bottom)

        @JvmStatic
        fun fromSize(rect: Rect, container: Size) = fromLTRB(
            left = rect.left,
            top = rect.top,
            right = container.width - rect.right,
            bottom = container.height - rect.bottom
        )

        @JvmStatic
        fun fromRect(rect: Rect, container: Rect) = fromLTRB(
            left = rect.left - container.left,
            top = rect.top - container.top,
            right = container.right - rect.right,
            bottom = container.bottom - rect.bottom
        )

        fun fromDirectional(textDirection: Direction, start: Float, top: Float, end: Float, bottom: Float): RelativeRect {
            val left: Float
            val right: Float
            when (textDirection) {
                Direction.RTL -> {
                    left = end
                    right = start
                }
                Direction.LTR -> {
                    left = start
                    right = end
                }
            }
            return fromLTRB(left, top, right, bottom)
        }
    }
}