package com.muedsa.snapshot.paint

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

    companion object {

        @JvmStatic
        fun lerp(a: BorderRadiusGeometry?, b: BorderRadiusGeometry?, t: Float): BorderRadiusGeometry? {
            if (a == b) {
                return a
            }
            val ta = a ?: BorderRadius.ZERO
            val tb = b ?: BorderRadius.ZERO


            TODO()
        }
    }
}