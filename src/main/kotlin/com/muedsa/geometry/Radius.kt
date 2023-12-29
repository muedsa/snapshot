package com.muedsa.geometry

import com.muedsa.snapshot.paint.lerpFloat

class Radius(val x: Float, val y: Float) {

    fun coerce(minimum: Radius?, maximum: Radius? = null): Radius = elliptical(
        x = x.coerceIn(
            minimum?.x ?: Float.NEGATIVE_INFINITY,
            maximum?.x ?: Float.POSITIVE_INFINITY,
        ),
        y = y.coerceIn(
            minimum?.y ?: Float.NEGATIVE_INFINITY,
            maximum?.y ?: Float.POSITIVE_INFINITY,
        )
    )

    operator fun unaryMinus(): Radius = elliptical(x = -x, y = -y)

    operator fun plus(other: Radius): Radius = elliptical(x = x + other.x, y = y + other.y)

    operator fun minus(other: Radius): Radius = elliptical(x = x - other.x, y = y - other.y)

    operator fun times(other: Radius): Radius = elliptical(x = x * other.x, y = y * other.y)

    operator fun div(other: Radius): Radius = elliptical(x = x / other.x, y = y / other.y)

    operator fun rem(other: Radius): Radius = elliptical(x = x % other.x, y = y % other.y)

    operator fun plus(other: Float): Radius = elliptical(x = x + other, y = y + other)

    operator fun minus(other: Float): Radius = elliptical(x = x - other, y = y - other)

    operator fun times(other: Float): Radius = elliptical(x = x * other, y = y * other)

    operator fun div(other: Float): Radius = elliptical(x = x / other, y = y / other)

    operator fun rem(other: Float): Radius = elliptical(x = x % other, y = y % other)

    override fun toString(): String {
        return "Radius(x=$x, y=$y)"
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Radius

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }


    companion object {
        val ZERO = Radius(0f, 0f)

        @JvmStatic
        fun circular(radius: Float): Radius = elliptical(radius, radius)
        @JvmStatic
        fun elliptical(x: Float, y: Float): Radius = Radius(x, y)

        @JvmStatic
        fun lerp(a: Radius?, b: Radius?, t: Float): Radius? {
            if (b == null) {
                if (a == null) {
                    return null
                } else {
                    val k: Float = 1f - t
                    return elliptical(x = a.x * k, y = a.y * k)
                }
            } else {
                if (a == null) {
                    return elliptical(x = b.x * t, y = b.y * t)
                } else {
                    return elliptical(
                        x = lerpFloat(a.x, b.x, t),
                        y = lerpFloat(a.y, b.y, t)
                    )
                }
            }
        }
    }
}