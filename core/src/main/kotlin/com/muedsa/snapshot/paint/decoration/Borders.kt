package com.muedsa.snapshot.paint.decoration

import org.jetbrains.skia.*

fun paintBorder(
    canvas: Canvas,
    rect: Rect,
    top: BorderSide = BorderSide.NONE,
    right: BorderSide = BorderSide.NONE,
    bottom: BorderSide = BorderSide.NONE,
    left: BorderSide = BorderSide.NONE,
) {
    // We draw the borders as filled shapes, unless the borders are hairline
    // borders, in which case we use PaintingStyle.stroke, with the stroke width
    // specified here.
    val paint: Paint = Paint().apply {
        strokeWidth = 1f
    }

    when (top.style) {
        BorderStyle.SOLID -> {
            paint.color = top.color
            val builder = PathBuilder().apply {
                moveTo(rect.left, rect.top)
                lineTo(rect.right, rect.top)
                if (top.width == 0f) {
                    paint.mode = PaintMode.STROKE
                } else {
                    paint.mode = PaintMode.FILL
                    lineTo(rect.right - right.width, rect.top + top.width)
                    lineTo(rect.left + left.width, rect.top + top.width)
                }
            }
            canvas.drawPath(builder.detach(), paint)
        }

        BorderStyle.NONE -> Unit
    }

    when (right.style) {
        BorderStyle.SOLID -> {
            paint.color = right.color
            val builder = PathBuilder().apply {
                moveTo(rect.right, rect.top)
                lineTo(rect.right, rect.bottom)
                if (right.width == 0f) {
                    paint.mode = PaintMode.STROKE
                } else {
                    paint.mode = PaintMode.FILL
                    lineTo(rect.right - right.width, rect.bottom - bottom.width)
                    lineTo(rect.right - right.width, rect.top + top.width)
                }
            }
            canvas.drawPath(builder.detach(), paint)
        }

        BorderStyle.NONE -> Unit
    }

    when (bottom.style) {
        BorderStyle.SOLID -> {
            paint.color = bottom.color
            val builder = PathBuilder().apply {
                moveTo(rect.right, rect.bottom)
                lineTo(rect.left, rect.bottom)
                if (bottom.width == 0f) {
                    paint.mode = PaintMode.STROKE
                } else {
                    paint.mode = PaintMode.FILL
                    lineTo(rect.left + left.width, rect.bottom - bottom.width)
                    lineTo(rect.right - right.width, rect.bottom - bottom.width)
                }
            }
            canvas.drawPath(builder.detach(), paint)
        }

        BorderStyle.NONE -> Unit
    }

    when (left.style) {
        BorderStyle.SOLID -> {
            paint.color = left.color
            val builder = PathBuilder().apply {
                moveTo(rect.left, rect.bottom)
                lineTo(rect.left, rect.top)
                if (left.width == 0f) {
                    paint.mode = PaintMode.STROKE
                } else {
                    paint.mode = PaintMode.FILL
                    lineTo(rect.left + left.width, rect.top + top.width)
                    lineTo(rect.left + left.width, rect.bottom - bottom.width)
                }
            }
            canvas.drawPath(builder.detach(), paint)
        }

        BorderStyle.NONE -> Unit
    }
}
