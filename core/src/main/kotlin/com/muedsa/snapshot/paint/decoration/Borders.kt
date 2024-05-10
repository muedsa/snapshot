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

    val path: Path = Path()
    when (top.style) {
        BorderStyle.SOLID -> {
            paint.color = top.color
            path.reset()
            path.moveTo(rect.left, rect.top)
            path.lineTo(rect.right, rect.top)
            if (top.width == 0f) {
                paint.mode = PaintMode.STROKE
            } else {
                paint.mode = PaintMode.FILL
                path.lineTo(rect.right - right.width, rect.top + top.width)
                path.lineTo(rect.left + left.width, rect.top + top.width)
            }
            canvas.drawPath(path, paint)
        }

        BorderStyle.NONE -> Unit
    }

    when (right.style) {
        BorderStyle.SOLID -> {
            paint.color = right.color
            path.reset()
            path.moveTo(rect.right, rect.top)
            path.lineTo(rect.right, rect.bottom)
            if (right.width == 0f) {
                paint.mode = PaintMode.STROKE
            } else {
                paint.mode = PaintMode.FILL
                path.lineTo(rect.right - right.width, rect.bottom - bottom.width)
                path.lineTo(rect.right - right.width, rect.top + top.width)
            }
            canvas.drawPath(path, paint)
        }

        BorderStyle.NONE -> Unit
    }

    when (bottom.style) {
        BorderStyle.SOLID -> {
            paint.color = bottom.color
            path.reset()
            path.moveTo(rect.right, rect.bottom)
            path.lineTo(rect.left, rect.bottom)
            if (bottom.width == 0f) {
                paint.mode = PaintMode.STROKE
            } else {
                paint.mode = PaintMode.FILL
                path.lineTo(rect.left + left.width, rect.bottom - bottom.width)
                path.lineTo(rect.right - right.width, rect.bottom - bottom.width)
            }
            canvas.drawPath(path, paint)
        }

        BorderStyle.NONE -> Unit
    }

    when (left.style) {
        BorderStyle.SOLID -> {
            paint.color = left.color
            path.reset()
            path.moveTo(rect.left, rect.bottom)
            path.lineTo(rect.left, rect.top)
            if (left.width == 0f) {
                paint.mode = PaintMode.STROKE
            } else {
                paint.mode = PaintMode.FILL
                path.lineTo(rect.left + left.width, rect.top + top.width)
                path.lineTo(rect.left + left.width, rect.bottom - bottom.width)
            }
            canvas.drawPath(path, paint)
        }

        BorderStyle.NONE -> Unit
    }
}