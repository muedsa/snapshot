package com.muedsa.geometry

import org.jetbrains.skia.Point
import org.jetbrains.skia.Rect

open class Offset(val x: Float, val y: Float) : Comparable<Offset> {

    val isInfinite by lazy {
        x >= Float.POSITIVE_INFINITY || y >= Float.POSITIVE_INFINITY
    }

    val isFinite by lazy {
        x.isFinite() && y.isFinite()
    }

    fun translate(translateX: Float, translateY: Float): Offset = Offset(x + translateX, y + translateY)

    fun toSkPoint(): Point = Point(x = x, y = y)

    override fun compareTo(other: Offset): Int {
        if (x == other.x && y == other.y) {
            return 0
        } else if (x > other.x && y > other.y) {
            return 1
        } else if (x < other.x && y < other.y) {
            return -1
        }
        throw UnsupportedOperationException("compareTo($this, $other)")
    }

    open operator fun plus(offset: Offset) = Offset(x = x + offset.x, y = y + offset.y)

    open operator fun minus(offset: Offset) = Offset(x = x - offset.x, y = y - offset.y)

    open operator fun plus(operand: Float) = Offset(x = x + operand, y = y + operand)

    open operator fun minus(operand: Float) = Offset(x = x - operand, y = y - operand)

    open operator fun times(operand: Float) = Offset(x = x * operand, y = y * operand)

    open operator fun div(operand: Float) = Offset(x = x / operand, y = y / operand)

    open operator fun rem(operand: Float) = Offset(x = x % operand, y = y % operand)

    infix fun combine(size: Size): Rect = Rect.makeXYWH(x, y, size.width, size.height)
    override fun toString(): String {
        return "Offset(x=$x, y=$y)"
    }

    companion object {
        @JvmStatic
        val ZERO = Offset(0f, 0f)
    }
}