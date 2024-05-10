package com.muedsa.snapshot.paint.decoration

import com.muedsa.geometry.Radius
import org.jetbrains.skia.paragraph.Direction

internal class MixedBorderRadius(
    override val topLeft: Radius,
    override val topRight: Radius,
    override val bottomLeft: Radius,
    override val bottomRight: Radius,
    override val topStart: Radius,
    override val topEnd: Radius,
    override val bottomStart: Radius,
    override val bottomEnd: Radius,
) : BorderRadiusGeometry() {

    operator fun unaryMinus(): MixedBorderRadius = MixedBorderRadius(
        topLeft = -topLeft,
        topRight = -topRight,
        bottomLeft = -bottomLeft,
        bottomRight = -bottomRight,
        topStart = -topStart,
        topEnd = -topEnd,
        bottomStart = -bottomStart,
        bottomEnd = -bottomEnd
    )

    operator fun plus(other: Float): MixedBorderRadius = MixedBorderRadius(
        topLeft = topLeft + other,
        topRight = topRight + other,
        bottomLeft = bottomLeft + other,
        bottomRight = bottomRight + other,
        topStart = topStart + other,
        topEnd = topEnd + other,
        bottomStart = bottomStart + other,
        bottomEnd = bottomEnd + other
    )

    operator fun minus(other: Float): MixedBorderRadius = MixedBorderRadius(
        topLeft = topLeft - other,
        topRight = topRight - other,
        bottomLeft = bottomLeft - other,
        bottomRight = bottomRight - other,
        topStart = topStart - other,
        topEnd = topEnd - other,
        bottomStart = bottomStart - other,
        bottomEnd = bottomEnd - other
    )

    operator fun times(other: Float): MixedBorderRadius = MixedBorderRadius(
        topLeft = topLeft * other,
        topRight = topRight * other,
        bottomLeft = bottomLeft * other,
        bottomRight = bottomRight * other,
        topStart = topStart * other,
        topEnd = topEnd * other,
        bottomStart = bottomStart * other,
        bottomEnd = bottomEnd * other
    )

    operator fun div(other: Float): MixedBorderRadius = MixedBorderRadius(
        topLeft = topLeft / other,
        topRight = topRight / other,
        bottomLeft = bottomLeft / other,
        bottomRight = bottomRight / other,
        topStart = topStart / other,
        topEnd = topEnd / other,
        bottomStart = bottomStart / other,
        bottomEnd = bottomEnd / other
    )

    operator fun rem(other: Float): MixedBorderRadius = MixedBorderRadius(
        topLeft = topLeft % other,
        topRight = topRight % other,
        bottomLeft = bottomLeft % other,
        bottomRight = bottomRight % other,
        topStart = topStart % other,
        topEnd = topEnd % other,
        bottomStart = bottomStart % other,
        bottomEnd = bottomEnd % other
    )

    override fun resolve(direction: Direction?): BorderRadius {
        assert(direction != null)
        return when (direction!!) {
            Direction.RTL -> BorderRadius.only(
                topLeft = topLeft + topEnd,
                topRight = topRight + topStart,
                bottomLeft = bottomLeft + bottomEnd,
                bottomRight = bottomRight + bottomStart,
            )

            else -> BorderRadius.only(
                topLeft = topLeft + topStart,
                topRight = topRight + topEnd,
                bottomLeft = bottomLeft + bottomStart,
                bottomRight = bottomRight + bottomEnd,
            )
        }
    }
}