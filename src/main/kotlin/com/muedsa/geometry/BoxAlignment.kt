package com.muedsa.geometry

import org.jetbrains.skia.paragraph.Direction


open class BoxAlignment(override val x: Float, override val y: Float) : AlignmentGeometry() {

    override val start: Float = 0f
    override fun resolve(direction: Direction?): BoxAlignment = this

    companion object {
        @JvmStatic val TOP_LEFT = BoxAlignment(-1f, -1f)
        @JvmStatic val TOP_CENTER = BoxAlignment(0f, -1f)
        @JvmStatic val TOP_RIGHT = BoxAlignment(1f, -1f)
        @JvmStatic val CENTER_LEFT = BoxAlignment(-1f, 0f)
        @JvmStatic val CENTER = BoxAlignment(0f, 0f)
        @JvmStatic val CENTER_RIGHT = BoxAlignment(1f, 0f)
        @JvmStatic val BOTTOM_LEFT = BoxAlignment(-1f, 1f)
        @JvmStatic val BOTTOM_CENTER = BoxAlignment(0f, 1f)
        @JvmStatic val BOTTOM_RIGHT = BoxAlignment(1f, 1f)


        fun stringify(x: Float, y: Float): String {
            if (x == -1f && y == -1f) {
                return "Alignment.TOP_LEFT"
            }
            if (x == 0f && y == -1f) {
                return "Alignment.TOP_CENTER"
            }
            if (x == 1f && y == -1f) {
                return "Alignment.TOP_RIGHT"
            }
            if (x == -1f && y == 0f) {
                return "Alignment.CENTER_LEFT"
            }
            if (x == 0f && y == 0f) {
                return "Alignment.CENTER"
            }
            if (x == 1f && y == 0f) {
                return "Alignment.CENTER_RIGHT"
            }
            if (x == -1f && y == 1f) {
                return "Alignment.BOTTOM_LEFT"
            }
            if (x == 0f && y == 1f) {
                return "Alignment.BOTTOM_CENTER"
            }
            if (x == 1f && y == 1f) {
                return "Alignment.BOTTOM_RIGHT"
            }
            return "Alignment($x, $y)"
        }
    }
}