package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Matrix44CMO
import com.muedsa.geometry.Offset
import com.muedsa.geometry.getAsTranslation
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.Rect
import kotlin.math.max
import kotlin.math.min

class RenderTransform(
    transform: Matrix44CMO,
    val origin: Offset? = null,
    val alignment: BoxAlignment? = null,
) : RenderSingleChildBox() {

    internal override fun getFilterPaintBounds(): Rect? {
        val bounds = super.getFilterPaintBounds() ?: return null
        if (bounds.width <= 0f || bounds.height <= 0f) return EMPTY_FILTER_PAINT_BOUNDS
        val matrix = effectiveTransform.mat
        var left = Float.POSITIVE_INFINITY
        var top = Float.POSITIVE_INFINITY
        var right = Float.NEGATIVE_INFINITY
        var bottom = Float.NEGATIVE_INFINITY
        for (x in floatArrayOf(bounds.left, bounds.right)) {
            for (y in floatArrayOf(bounds.top, bounds.bottom)) {
                val w = matrix[3] * x + matrix[7] * y + matrix[15]
                if (!w.isFinite() || w <= 0f) return null
                val mappedX = (matrix[0] * x + matrix[4] * y + matrix[12]) / w
                val mappedY = (matrix[1] * x + matrix[5] * y + matrix[13]) / w
                if (!mappedX.isFinite() || !mappedY.isFinite()) return null
                left = min(left, mappedX)
                top = min(top, mappedY)
                right = max(right, mappedX)
                bottom = max(bottom, mappedY)
            }
        }
        return Rect.makeLTRB(left, top, right, bottom)
    }

    val transform: Matrix44CMO = transform.clone()

    val effectiveTransform: Matrix44CMO
        get() {
            if (origin == null && alignment == null) {
                return transform
            }
            val result = Matrix44CMO.identity()
            if (origin != null) {
                result.translate(origin.x, origin.y)
            }
            var translation: Offset? = null
            if (alignment != null) {
                translation = alignment.alongSize(definiteSize)
                result.translate(translation.x, translation.y)
            }
            result.multiply(transform)
            if (alignment != null) {
                result.translate(-translation!!.x, -translation.y)
            }
            if (origin != null) {
                result.translate(-origin.x, -origin.y)
            }
            return result
        }

    override fun paint(context: PaintingContext, offset: Offset) {
        if (child != null) {
            val childOffset: Offset? = getAsTranslation(effectiveTransform)
            if (childOffset == null) {
                // if the matrix is singular the children would be compressed to a line or
                // single point, instead short-circuit and paint nothing.
                val det: Float = effectiveTransform.determinant()
                if (det == 0f || !det.isFinite()) {
                    return
                }
                context.pushTransform(offset = offset, transform = effectiveTransform) { cc, oo ->
                    super.paint(cc, oo)
                }
            } else {
                super.paint(context, offset + childOffset)
            }
        }
    }

    override fun applyPaintTransform(child: RenderBox, transform: Matrix44CMO) {
        transform.multiply(effectiveTransform)
    }
}
