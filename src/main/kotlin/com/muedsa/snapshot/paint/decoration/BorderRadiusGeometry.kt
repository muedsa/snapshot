package com.muedsa.snapshot.paint.decoration

import com.muedsa.geometry.Radius

abstract class BorderRadiusGeometry {

    abstract val topLeft: Radius
    abstract val topRight: Radius
    abstract val bottomLeft: Radius
    abstract val bottomRight: Radius
    abstract val topStart: Radius
    abstract val topEnd: Radius
    abstract val bottomStart: Radius
    abstract val bottomEnd: Radius

    open fun subtract(other: BorderRadiusGeometry): BorderRadiusGeometry = MixedBorderRadius(
        topLeft = topLeft - other.topLeft,
        topRight = topRight - other.topRight,
        bottomLeft = bottomLeft - other.bottomLeft,
        bottomRight = bottomRight - other.bottomRight,
        topStart = topStart - other.topStart,
        topEnd = topEnd - other.topEnd,
        bottomStart = bottomStart - other.bottomStart,
        bottomEnd = bottomEnd - other.bottomEnd
    )

    open fun add(other: BorderRadiusGeometry): BorderRadiusGeometry = MixedBorderRadius(
        topLeft = topLeft + other.topLeft,
        topRight = topRight + other.topRight,
        bottomLeft = bottomLeft + other.bottomLeft,
        bottomRight = bottomRight + other.bottomRight,
        topStart = topStart + other.topStart,
        topEnd = topEnd + other.topEnd,
        bottomStart = bottomStart + other.bottomStart,
        bottomEnd = bottomEnd + other.bottomEnd
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BorderRadiusGeometry) return false

        if (topLeft != other.topLeft) return false
        if (topRight != other.topRight) return false
        if (bottomLeft != other.bottomLeft) return false
        if (bottomRight != other.bottomRight) return false
        if (topStart != other.topStart) return false
        if (topEnd != other.topEnd) return false
        if (bottomStart != other.bottomStart) return false
        if (bottomEnd != other.bottomEnd) return false

        return true
    }

    override fun hashCode(): Int {
        var result = topLeft.hashCode()
        result = 31 * result + topRight.hashCode()
        result = 31 * result + bottomLeft.hashCode()
        result = 31 * result + bottomRight.hashCode()
        result = 31 * result + topStart.hashCode()
        result = 31 * result + topEnd.hashCode()
        result = 31 * result + bottomStart.hashCode()
        result = 31 * result + bottomEnd.hashCode()
        return result
    }
}