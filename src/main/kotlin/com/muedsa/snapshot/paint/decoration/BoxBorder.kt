package com.muedsa.snapshot.paint.decoration

import com.muedsa.geometry.*
import org.jetbrains.skia.*

abstract class BoxBorder : ShapeBorder() {

    abstract val top: BorderSide

    abstract val bottom: BorderSide

    abstract val isUniform: Boolean

    override fun add(other: ShapeBorder, reversed: Boolean): ShapeBorder? = null

    override fun getInnerPath(rect: Rect): Path = Path().addRect(dimensions.deflateRect(rect))

    override fun getOuterPath(rect: Rect): Path = Path().addRect(rect)

    override fun paintInterior(canvas: Canvas, rect: Rect, paint: Paint) {
        // For `ShapeDecoration(shape: Border.all())`, a rectangle with sharp edges
        // is always painted. There is no borderRadius parameter for
        // ShapeDecoration or Border, only for BoxDecoration, which doesn't call
        // this method.
        canvas.drawRect(rect, paint)
    }

    override val preferPaintInterior: Boolean = true

    override fun paint(canvas: Canvas, rect: Rect) {
        paint(canvas = canvas, rect = rect, shape = BoxShape.RECTANGLE, borderRadius = null)
    }

    abstract fun paint(canvas: Canvas, rect: Rect, shape: BoxShape = BoxShape.RECTANGLE, borderRadius: BorderRadius?)

    companion object {

        @JvmStatic
        fun lerp(a: BoxBorder?, b: BoxBorder?, t: Float): BoxBorder? {
            if (a == b) {
                return a
            }
            TODO()
        }

        fun paintUniformBorderWithRadius(
            canvas: Canvas,
            rect: Rect,
            side: BorderSide,
            borderRadius: BorderRadius,
        ) {
            assert(side.style != BorderStyle.NONE)
            val paint: Paint = Paint().apply {
                color = side.color
            }
            val width: Float = side.width
            if (width == 0f) {
                paint.mode = PaintMode.STROKE
                paint.strokeWidth = 0f
                canvas.drawRRect(borderRadius.toRRect(rect), paint)
            } else {
                val borderRect: RRect = borderRadius.toRRect(rect)
                val inner: RRect = borderRect.deflate(side.strokeInset) as RRect
                val outer: RRect = borderRect.inflate(side.strokeOutset) as RRect
                canvas.drawDRRect(outer, inner, paint)
            }
        }

        fun paintNonUniformBorder(
            canvas: Canvas,
            rect: Rect,
            borderRadius: BorderRadius?,
            shape: BoxShape = BoxShape.RECTANGLE,
            top: BorderSide = BorderSide.NONE,
            right: BorderSide = BorderSide.NONE,
            bottom: BorderSide = BorderSide.NONE,
            left: BorderSide = BorderSide.NONE,
            color: Int,
        ) {
            val borderRect: RRect = when (shape) {
                BoxShape.RECTANGLE -> {
                    (borderRadius ?: BorderRadius.ZERO).toRRect(rect)
                }

                BoxShape.CIRCLE -> {
                    assert(borderRadius == null) { "A borderRadius cannot be given when shape is a BoxShape.CIRCLE." }
                    makeRRectFromRectAndRadius(
                        rect = makeRectFromCircle(center = rect.center, radius = rect.shortestSide / 2f),
                        radius = Radius.circular(radius = rect.width)
                    )
                }
            }
            val paint: Paint = Paint().apply {
                this.color = color
            }
            val inner: RRect = deflateRRect(
                rect = borderRect,
                insets = EdgeInsets.fromLTRB(left.strokeInset, top.strokeInset, right.strokeInset, bottom.strokeInset)
            )
            val outer: RRect = inflateRRect(
                rect = borderRect,
                insets = EdgeInsets.fromLTRB(left.strokeOutset, top.strokeOutset, right.strokeOutset, bottom.strokeOutset)
            )
            canvas.drawDRRect(outer = outer, inner = inner, paint = paint)
        }

        private fun inflateRRect(rect: RRect, insets: EdgeInsets): RRect = makeRRectFromLTRBAndCorners(
            left = rect.left - insets.left,
            top = rect.top - insets.top,
            right = rect.right + insets.right,
            bottom = rect.bottom + insets.bottom,
            topLeft = (rect.tlRadius + Radius.elliptical(insets.left, insets.top)).coerce(minimum = Radius.ZERO),
            topRight = (rect.trRadius + Radius.elliptical(insets.right, insets.top)).coerce(minimum = Radius.ZERO),
            bottomRight = (rect.brRadius + Radius.elliptical(insets.right, insets.bottom)).coerce(minimum = Radius.ZERO),
            bottomLeft = (rect.blRadius + Radius.elliptical(insets.left, insets.bottom)).coerce(minimum = Radius.ZERO),
        )

        private fun deflateRRect(rect: RRect, insets: EdgeInsets): RRect = makeRRectFromLTRBAndCorners(
            left = rect.left + insets.left,
            top = rect.top + insets.top,
            right = rect.right - insets.right,
            bottom = rect.bottom - insets.bottom,
            topLeft = (rect.tlRadius - Radius.elliptical(insets.left, insets.top)).coerce(minimum = Radius.ZERO),
            topRight = (rect.trRadius - Radius.elliptical(insets.right, insets.top)).coerce(minimum = Radius.ZERO),
            bottomRight = (rect.brRadius - Radius.elliptical(insets.right, insets.bottom)).coerce(minimum = Radius.ZERO),
            bottomLeft = (rect.blRadius - Radius.elliptical(insets.left, insets.bottom)).coerce(minimum = Radius.ZERO),
        )

        fun paintUniformBorderWithCircle(canvas: Canvas, rect: Rect, side: BorderSide) {
            assert(side.style != BorderStyle.NONE)
            val radius: Float = (rect.shortestSide + side.strokeOffset) / 2
            val offset = rect.center
            canvas.drawCircle(x = offset.x, y = offset.y, radius = radius, paint = side.toPaint())
        }

        fun paintUniformBorderWithRectangle(canvas: Canvas, rect: Rect, side: BorderSide) {
            assert(side.style != BorderStyle.NONE)
            canvas.drawRect(rect.inflate(side.strokeOffset / 2), side.toPaint())
        }
    }
}