package com.muedsa.geometry

import org.jetbrains.skia.paragraph.Direction

class AlignmentDirectional(override val start: Float, override val y: Float) : AlignmentGeometry() {

    override val x: Float = 0f

    override fun resolve(direction: Direction?): BoxAlignment =
        when (direction) {
            Direction.RTL -> BoxAlignment(-start, y)
            Direction.LTR -> BoxAlignment(start, y)
            null -> throw IllegalArgumentException("Cannot resolve $this without a Text Direction.")
        }

    override fun toString(): String = stringify(start, y)

    companion object {
        @JvmStatic
        val TOP_START = AlignmentDirectional(-1f, -1f)

        @JvmStatic
        val TOP_CENTER = AlignmentDirectional(0f, -1f)

        @JvmStatic
        val TOP_END = AlignmentDirectional(1f, -1f)

        @JvmStatic
        val CENTER_START = AlignmentDirectional(-1f, 0f)

        @JvmStatic
        val CENTER = AlignmentDirectional(0f, 0f)

        @JvmStatic
        val CENTER_END = AlignmentDirectional(1f, 0f)

        @JvmStatic
        val BOTTOM_START = AlignmentDirectional(-1f, 1f)

        @JvmStatic
        val BOTTOM_CENTER = AlignmentDirectional(0f, 1f)

        @JvmStatic
        val BOTTOM_END = AlignmentDirectional(1f, 1f)

        fun stringify(start: Float, y: Float): String {
            if (start == -1f && y == -1f) {
                return "AlignmentDirectional.topStart"
            }
            if (start == 0f && y == -1f) {
                return "AlignmentDirectional.topCenter"
            }
            if (start == 1f && y == -1f) {
                return "AlignmentDirectional.topEnd"
            }
            if (start == -1f && y == 0f) {
                return "AlignmentDirectional.centerStart"
            }
            if (start == 0f && y == 0f) {
                return "AlignmentDirectional.center"
            }
            if (start == 1f && y == 0f) {
                return "AlignmentDirectional.centerEnd"
            }
            if (start == -1f && y == 1f) {
                return "AlignmentDirectional.bottomStart"
            }
            if (start == 0f && y == 1f) {
                return "AlignmentDirectional.bottomCenter"
            }
            if (start == 1f && y == 1f) {
                return "AlignmentDirectional.bottomEnd"
            }
            return "AlignmentDirectional(${start}, ${y})"
        }
    }
}