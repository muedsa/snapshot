package com.muedsa.snapshot.paint.decoration

import com.muedsa.geometry.EdgeInsets
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Rect

class Border(
    override val top: BorderSide = BorderSide.NONE,
    val right: BorderSide = BorderSide.NONE,
    override val bottom: BorderSide = BorderSide.NONE,
    val left: BorderSide = BorderSide.NONE,
) : BoxBorder() {

    override val dimensions: EdgeInsets
        get() = if (widthIsUniform) {
            EdgeInsets.all(top.strokeInset)
        } else {
            EdgeInsets.fromLTRB(left.strokeInset, top.strokeInset, right.strokeInset, bottom.strokeInset)
        }

    override val isUniform: Boolean
        get() = colorIsUniform && widthIsUniform && styleIsUniform && strokeAlignIsUniform

    private val colorIsUniform: Boolean
        get() {
            val topColor: Int = top.color
            return left.color == topColor && bottom.color == topColor && right.color == topColor
        }

    private val widthIsUniform: Boolean
        get() {
            val topWidth: Float = top.width
            return left.width == topWidth && bottom.width == topWidth && right.width == topWidth
        }

    private val styleIsUniform: Boolean
        get() {
            val topStyle: BorderStyle = top.style
            return left.style == topStyle && bottom.style == topStyle && right.style == topStyle
        }

    private val strokeAlignIsUniform: Boolean
        get() {
            val topStrokeAlign: Float = top.strokeAlign
            return left.strokeAlign == topStrokeAlign
                    && bottom.strokeAlign == topStrokeAlign
                    && right.strokeAlign == topStrokeAlign
        }

    private fun distinctVisibleColors(): Set<Int> {
        val distinctVisibleColors: MutableSet<Int> = mutableSetOf()
        if (top.style != BorderStyle.NONE) {
            distinctVisibleColors.add(top.color)
        }
        if (right.style != BorderStyle.NONE) {
            distinctVisibleColors.add(right.color)
        }
        if (bottom.style != BorderStyle.NONE) {
            distinctVisibleColors.add(bottom.color)
        }
        if (left.style != BorderStyle.NONE) {
            distinctVisibleColors.add(left.color)
        }
        return distinctVisibleColors
    }

    private val hasHairlineBorder: Boolean = (top.style == BorderStyle.SOLID && top.width == 0f) ||
            (right.style == BorderStyle.SOLID && right.width == 0f) ||
            (bottom.style == BorderStyle.SOLID && bottom.width == 0f) ||
            (left.style == BorderStyle.SOLID && left.width == 0f)

    fun add(other: Border): Border? {
        return if (BorderSide.canMerge(top, other.top) &&
            BorderSide.canMerge(right, other.right) &&
            BorderSide.canMerge(bottom, other.bottom) &&
            BorderSide.canMerge(left, other.left)
        ) {
            merge(this, other)
        } else null
    }

    override fun scale(t: Float): Border = Border(
        top = top.scale(t),
        right = right.scale(t),
        bottom = bottom.scale(t),
        left = left.scale(t)
    )

    override fun paint(canvas: Canvas, rect: Rect, shape: BoxShape, borderRadius: BorderRadius?) {
        if (isUniform) {
            when (top.style) {
                BorderStyle.NONE -> {
                    return
                }

                BorderStyle.SOLID -> {
                    when (shape) {
                        BoxShape.CIRCLE -> {
                            assert(borderRadius == null) { "A borderRadius cannot be given when shape is a BoxShape.CIRCLE." }
                            paintUniformBorderWithCircle(canvas, rect, top)
                        }

                        BoxShape.RECTANGLE -> {
                            if (borderRadius != null && borderRadius != BorderRadius.ZERO) {
                                paintUniformBorderWithRadius(canvas, rect, top, borderRadius)
                                return
                            }
                            paintUniformBorderWithRectangle(canvas, rect, top)
                        }
                    }
                    return
                }

            }
        }

        if (styleIsUniform && top.style == BorderStyle.NONE) {
            return
        }

        // Allow painting non-uniform borders if the visible colors are uniform.
        val visibleColors: Set<Int> = distinctVisibleColors()
        val hasHairlineBorder: Boolean = hasHairlineBorder
        // Paint a non uniform border if a single color is visible
        // and (borderRadius is present) or (border is visible and width != 0.0).
        if (visibleColors.size == 1 &&
            !hasHairlineBorder &&
            (shape == BoxShape.CIRCLE ||
                    (borderRadius != null && borderRadius != BorderRadius.ZERO))
        ) {
            paintNonUniformBorder(
                canvas = canvas,
                rect = rect,
                shape = shape,
                borderRadius = borderRadius,
                top = if (top.style == BorderStyle.NONE) BorderSide.NONE else top,
                right = if (right.style == BorderStyle.NONE) BorderSide.NONE else right,
                bottom = if (bottom.style == BorderStyle.NONE) BorderSide.NONE else bottom,
                left = if (left.style == BorderStyle.NONE) BorderSide.NONE else left,
                color = visibleColors.first()
            )
            return
        }
        // assert some
        paintBorder(canvas = canvas, rect = rect, top = top, right = right, bottom = bottom, left = left)
    }

    companion object {

        @JvmStatic
        fun fromBorderSide(side: BorderSide): Border = Border(
            top = side,
            right = side,
            bottom = side,
            left = side
        )

        @JvmStatic
        fun symmetric(vertical: BorderSide = BorderSide.NONE, horizontal: BorderSide = BorderSide.NONE): Border =
            Border(
                top = vertical,
                right = horizontal,
                bottom = vertical,
                left = horizontal
            )

        @JvmStatic
        fun all(
            color: Int = Color.BLACK,
            width: Float = 1f,
            style: BorderStyle = BorderStyle.SOLID,
            strokeAlign: Float = BorderSide.STROKE_ALIGN_INSIDE,
        ): Border = fromBorderSide(
            side = BorderSide(
                color = color,
                width = width,
                style = style,
                strokeAlign = strokeAlign
            )
        )

        @JvmStatic
        fun merge(a: Border, b: Border): Border {
            assert(BorderSide.canMerge(a.top, b.top))
            assert(BorderSide.canMerge(a.right, b.right))
            assert(BorderSide.canMerge(a.bottom, b.bottom))
            assert(BorderSide.canMerge(a.left, b.left))
            return Border(
                top = BorderSide.merge(a.top, b.top),
                right = BorderSide.merge(a.right, b.right),
                bottom = BorderSide.merge(a.bottom, b.bottom),
                left = BorderSide.merge(a.left, b.left)
            )
        }
    }

}