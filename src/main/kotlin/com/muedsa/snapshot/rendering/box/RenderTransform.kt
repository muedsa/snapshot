package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Matrix44CMO
import com.muedsa.geometry.Offset
import com.muedsa.geometry.getAsTranslation
import com.muedsa.snapshot.rendering.PaintingContext

class RenderTransform(
    transform: Matrix44CMO,
    val origin: Offset? = null,
    val alignment: BoxAlignment? = null,
    child: RenderBox? = null,
) : RenderSingleChildBox(child = child) {

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
                context.doTransform(offset = offset, transform = effectiveTransform) { cc, oo ->
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