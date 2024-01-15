package com.muedsa.snapshot.paint.decoration

import com.muedsa.geometry.Radius
import com.muedsa.geometry.makeRRectFromRectAndCorners
import org.jetbrains.skia.Rect

class BorderRadius(
    override val topLeft: Radius,
    override val topRight: Radius,
    override val bottomLeft: Radius,
    override val bottomRight: Radius,
) : BorderRadiusGeometry() {

    override val topStart: Radius = topLeft
    override val topEnd: Radius = topRight
    override val bottomStart: Radius = bottomLeft
    override val bottomEnd: Radius = bottomRight

    fun toRRect(rect: Rect) = makeRRectFromRectAndCorners(
        rect = rect,
        topLeft = topLeft,
        topRight = topRight,
        bottomLeft = bottomLeft,
        bottomRight = bottomRight
    )

    override fun subtract(other: BorderRadiusGeometry): BorderRadiusGeometry {
        if (other is BorderRadius) {
            return this - other
        }
        return super.subtract(other)
    }

    override fun add(other: BorderRadiusGeometry): BorderRadiusGeometry {
        if (other is BorderRadius) {
            return this + other
        }
        return super.add(other)
    }

    operator fun plus(other: BorderRadius): BorderRadius = only(
        topLeft = topLeft + other.topLeft,
        topRight = topRight + other.topRight,
        bottomLeft = bottomLeft + other.bottomLeft,
        bottomRight = bottomRight + other.bottomRight
    )

    operator fun minus(other: BorderRadius): BorderRadius = only(
        topLeft = topLeft - other.topLeft,
        topRight = topRight - other.topRight,
        bottomLeft = bottomLeft - other.bottomLeft,
        bottomRight = bottomRight - other.bottomRight
    )

    operator fun unaryMinus(): BorderRadius = only(
        topLeft = -topLeft,
        topRight = -topRight,
        bottomLeft = -bottomLeft,
        bottomRight = -bottomRight
    )

    operator fun plus(other: Float): BorderRadius = only(
        topLeft = topLeft + other,
        topRight = topRight + other,
        bottomLeft = bottomLeft + other,
        bottomRight = bottomRight + other
    )

    operator fun minus(other: Float): BorderRadius = only(
        topLeft = topLeft - other,
        topRight = topRight - other,
        bottomLeft = bottomLeft - other,
        bottomRight = bottomRight - other
    )

    operator fun times(other: Float): BorderRadius = only(
        topLeft = topLeft * other,
        topRight = topRight * other,
        bottomLeft = bottomLeft * other,
        bottomRight = bottomRight * other
    )

    operator fun div(other: Float): BorderRadius = only(
        topLeft = topLeft / other,
        topRight = topRight / other,
        bottomLeft = bottomLeft / other,
        bottomRight = bottomRight / other
    )

    operator fun rem(other: Float): BorderRadius = only(
        topLeft = topLeft % other,
        topRight = topRight % other,
        bottomLeft = bottomLeft % other,
        bottomRight = bottomRight % other
    )

    fun copyWith(
        topLeft: Radius? = null,
        topRight: Radius? = null,
        bottomLeft: Radius? = null,
        bottomRight: Radius? = null,
    ): BorderRadius = only(
        topLeft = topLeft ?: this.topLeft,
        topRight = topRight ?: this.topRight,
        bottomLeft = bottomLeft ?: this.bottomLeft,
        bottomRight = bottomRight ?: this.bottomRight
    )

    companion object {
        @JvmStatic
        val ZERO = BorderRadius(
            topLeft = Radius.ZERO,
            topRight = Radius.ZERO,
            bottomLeft = Radius.ZERO,
            bottomRight = Radius.ZERO
        )

        @JvmStatic
        fun only(
            topLeft: Radius = Radius.ZERO,
            topRight: Radius = Radius.ZERO,
            bottomLeft: Radius = Radius.ZERO,
            bottomRight: Radius = Radius.ZERO,
        ): BorderRadius = BorderRadius(
            topLeft = topLeft,
            topRight = topRight,
            bottomLeft = bottomLeft,
            bottomRight = bottomRight
        )

        @JvmStatic
        fun all(radius: Radius): BorderRadius = only(
            topLeft = radius,
            topRight = radius,
            bottomLeft = radius,
            bottomRight = radius
        )

        @JvmStatic
        fun circular(radius: Float): BorderRadius = all(radius = Radius.circular(radius))

        @JvmStatic
        fun vertical(top: Radius = Radius.ZERO, bottom: Radius = Radius.ZERO): BorderRadius = only(
            topLeft = top,
            topRight = top,
            bottomLeft = bottom,
            bottomRight = bottom
        )

        @JvmStatic
        fun horizontal(left: Radius = Radius.ZERO, right: Radius = Radius.ZERO): BorderRadius = only(
            topLeft = left,
            topRight = left,
            bottomLeft = right,
            bottomRight = right
        )
    }
}