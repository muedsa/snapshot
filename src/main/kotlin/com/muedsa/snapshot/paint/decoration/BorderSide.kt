package com.muedsa.snapshot.paint.decoration

import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import kotlin.math.max

class BorderSide(
    val color: Int = Color.BLACK,
    val width: Float = 1f,
    val style: BorderStyle = BorderStyle.SOLID,
    val strokeAlign: Float = STROKE_ALIGN_INSIDE
) {

    val strokeInset: Float by lazy {
        width * (1 - (1 + strokeAlign) / 2)
    }

    val strokeOutset: Float by lazy {
        width * (1 + strokeAlign) / 2
    }

    val strokeOffset: Float by lazy {
        width * strokeAlign
    }

    fun scale(t: Float) = BorderSide(
        color = color,
        width = max(0f, width * t),
        style =  if(t <= 0f) BorderStyle.NONE else style
    )

    fun toPaint(): Paint = when(style) {
        BorderStyle.SOLID -> Paint().apply {
            color = this@BorderSide.color
            strokeWidth = this@BorderSide.width
            mode = PaintMode.STROKE
        }
        BorderStyle.NONE -> Paint().apply {
            color = Color.BLACK
            strokeWidth = 0f
            mode = PaintMode.STROKE
        }
    }

    companion object {
        @JvmStatic
        val NONE: BorderSide = BorderSide(width = 0f, style = BorderStyle.NONE)
        const val STROKE_ALIGN_INSIDE: Float = -1f
        const val STROKE_ALIGN_CENTER: Float = 0f
        const val STROKE_ALIGN_OUTSIDE: Float = 1f

        @JvmStatic
        fun merge(a: BorderSide, b: BorderSide): BorderSide {
            assert(canMerge(a, b))
            val aIsNone: Boolean = a.style == BorderStyle.NONE && a.width == 0f
            val bIsNone: Boolean = b.style == BorderStyle.NONE && b.width == 0f
            if (aIsNone && bIsNone) {
                return NONE
            }
            if (aIsNone) {
                return b
            }
            if (bIsNone) {
                return a
            }
            assert(a.color == b.color)
            assert(a.style == b.style)
            return BorderSide(
                color = a.color, // == b.color
                width = a.width + b.width,
                strokeAlign = max(a.strokeAlign, b.strokeAlign),
                style = a.style, // == b.style
            )
        }

        @JvmStatic
        fun canMerge(a: BorderSide, b: BorderSide): Boolean {
            if ((a.style == BorderStyle.NONE && a.width == 0f) ||
                (b.style == BorderStyle.NONE && b.width == 0f)
            ) {
                return true
            }
            return a.style == b.style
                    && a.color == b.color
        }
    }
}