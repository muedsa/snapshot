package com.muedsa.geometry

import org.jetbrains.skia.Rect


class EdgeInsets(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {

    val horizontal: Float by lazy {
        left + right
    }

    val vertical: Float by lazy {
        top + bottom
    }

    fun flipped(): EdgeInsets = EdgeInsets(left = right, top = bottom, right = left, bottom = top)

    fun inflateRect(rect: Rect): Rect = Rect(rect.left - left, rect.top - top, rect.right + right, rect.bottom + bottom)

    fun deflateRect(rect: Rect): Rect = Rect(rect.left + left, rect.top + top, rect.right - right, rect.bottom - bottom)

    fun add(other: EdgeInsets) = EdgeInsets(
        left = left + other.left,
        top = top + other.top,
        right = right + other.right,
        bottom = bottom + other.bottom
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EdgeInsets) return false

        if (left != other.left) return false
        if (top != other.top) return false
        if (right != other.right) return false
        if (bottom != other.bottom) return false

        return true
    }

    override fun hashCode(): Int {
        var result = left.hashCode()
        result = 31 * result + top.hashCode()
        result = 31 * result + right.hashCode()
        result = 31 * result + bottom.hashCode()
        return result
    }


    companion object {
        @JvmStatic
        val ZERO = only()

        @JvmStatic
        fun fromLTRB(left: Float, top: Float, right: Float, bottom: Float) = EdgeInsets(
            left = left,
            top = top,
            right = right,
            bottom = bottom
        )

        @JvmStatic
        fun all(value: Float) = EdgeInsets(
            left = value,
            top = value,
            right = value,
            bottom = value
        )

        @JvmStatic
        fun only(left: Float = 0f, top: Float = 0f, right: Float = 0f, bottom: Float = 0f) = EdgeInsets(
            left = left,
            top = top,
            right = right,
            bottom = bottom
        )

        @JvmStatic
        fun symmetric(vertical: Float = 0f, horizontal: Float = 0f) = EdgeInsets(
            left = horizontal,
            top = vertical,
            right = horizontal,
            bottom = vertical
        )
    }

}