package com.muedsa.snapshot.paint.decoration

import com.muedsa.geometry.*
import com.muedsa.snapshot.paint.BoxPainter
import com.muedsa.snapshot.paint.BoxShape
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Path
import org.jetbrains.skia.Rect

internal class BoxDecorationPainter(
    val decoration: BoxDecoration
) : BoxPainter() {

    var cachedBackgroundPaint: Paint? = null

    var rectForCachedBackgroundPaint: Rect? = null

    private fun getBackgroundPaint(rect: Rect): Paint {
        assert(decoration.gradient != null || rectForCachedBackgroundPaint == null)

        if (cachedBackgroundPaint == null ||
            (decoration.gradient != null &&
                    rectForCachedBackgroundPaint != rect)) {
            val paint: Paint  = Paint();
            if (decoration.backgroundBlendMode != null) {
                paint.blendMode = decoration.backgroundBlendMode
            }
            if (decoration.color != null) {
                paint.color = decoration.color
            }
            if (decoration.gradient != null) {
                paint.shader = decoration.gradient.createShader(rect)
                rectForCachedBackgroundPaint = rect
            }
            cachedBackgroundPaint = paint
        }

        return cachedBackgroundPaint!!
    }


    private fun paintBox(canvas: Canvas, rect: Rect, paint: Paint) {
        when(decoration.shape) {
            BoxShape.CIRCLE -> {
                assert(decoration.borderRadius == null)
                val center: Offset = rect.center
                val radius: Float = rect.shortestSide / 2f
                canvas.drawCircle(x = center.x, y = center.y, radius = radius, paint = paint)
            }
            BoxShape.RECTANGLE -> {
                if (decoration.borderRadius == null ||
                    decoration.borderRadius == BorderRadius.ZERO
                ) {
                    canvas.drawRect(r = rect, paint = paint)
                } else {
                    canvas.drawRRect(r = decoration.borderRadius.toRRect(rect), paint = paint)
                }
            }
        }
    }

    private fun paintShadows(canvas: Canvas, rect: Rect) {
        decoration.boxShadow?.forEach {
            val paint: Paint = it.toPaint()
            val bounds: Rect = rect.shift(it.offset).inflate(it.spreadRadius)
            paintBox(canvas = canvas, rect = bounds, paint = paint)
        }
    }

    private fun paintBackgroundColor(canvas: Canvas, rect: Rect) {
        if (decoration.color != null || decoration.gradient != null) {
            paintBox(canvas = canvas, rect = rect, paint = getBackgroundPaint(rect))
        }
    }

    var imagePainter: DecorationImagePainter? = null
    private fun paintBackgroundImage(canvas: Canvas, rect: Rect) {
        if (decoration.image == null) {
            return
        }
        if (imagePainter == null) {
            imagePainter = decoration.image.createPainter()
        }
        var clipPath: Path? = null
        when(decoration.shape) {
            BoxShape.CIRCLE -> {
                assert(decoration.borderRadius == null)
                val center: Offset = rect.center
                val radius: Float = rect.shortestSide / 2f
                val square: Rect = makeRectFromCircle(center = center, radius = radius)
                clipPath = Path().addOval(square)
            }
            BoxShape.RECTANGLE -> {
                if (decoration.borderRadius != null) {
                    clipPath = Path().addRRect(decoration.borderRadius.toRRect(rect))
                }
            }
        }
        imagePainter!!.paint(canvas = canvas, rect = rect, clipPath = clipPath)
    }

    override fun paint(canvas: Canvas, offset: Offset, size: Size) {
        val rect: Rect = offset combine size
        paintShadows(canvas = canvas, rect = rect)
        paintBackgroundColor(canvas = canvas, rect = rect)
        paintBackgroundImage(canvas = canvas, rect = rect)
        decoration.border?.paint(
            canvas = canvas,
            rect = rect,
            shape = decoration.shape,
            borderRadius = decoration.borderRadius
        )
    }
}