package com.muedsa.geometry

open class Size(width: Float, height: Float) : Offset(width, height) {

    val width: Float = x
    val height: Float = y

    val isEmpty: Boolean by lazy {
        width <= 0f || height <= 0f
    }

    operator fun plus(size: Size) = Size(width = width + size.width, height = height + size.height)

    override operator fun plus(offset: Offset) = Size(width = width + offset.x, height = height + offset.y)

    operator fun minus(size: Size) = Size(width = width - size.width, height = height - size.height)

    override operator fun minus(offset: Offset) = Size(width = width - offset.x, height = height - offset.y)

    override operator fun times(operand: Float) = Size(width * operand, height = height * operand)

    override operator fun div(operand: Float) = Size(width / operand, height = height / operand)


    override fun toString(): String {
        return "Size(width=$width, height=$height)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Size) return false

        if (width != other.width) return false
        if (height != other.height) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width.hashCode()
        result = 31 * result + height.hashCode()
        return result
    }

    companion object {
        val ZERO = Size(0f, 0f)
        val INFINITY = Size(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)

        @JvmStatic
        fun square(dimension: Float): Size = Size(dimension, dimension)
    }
}