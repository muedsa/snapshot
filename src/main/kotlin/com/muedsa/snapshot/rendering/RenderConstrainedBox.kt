package com.muedsa.snapshot.rendering

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import org.jetbrains.skia.Paint

class RenderConstrainedBox(
    var additionalConstraints: BoxConstraints,
    child: RenderBox? = null
) : RenderSingleChildBox(child = child) {


    override fun computeMinIntrinsicWidth(height: Float): Float {
        if (additionalConstraints.hasBoundedWidth && additionalConstraints.hasTightWidth) {
            return additionalConstraints.minWidth
        }
        val width = super.computeMinIntrinsicWidth(height)
        assert(width.isFinite())
        if (!additionalConstraints.hasInfiniteWidth) {
            return additionalConstraints.constrainWidth(width)
        }
        return width
    }

    override fun computeMaxIntrinsicWidth(height: Float): Float {
        if (additionalConstraints.hasBoundedWidth && additionalConstraints.hasTightWidth) {
            return additionalConstraints.minWidth
        }
        val width = super.computeMaxIntrinsicWidth(height)
        assert(width.isFinite())
        if (!additionalConstraints.hasInfiniteWidth) {
            return additionalConstraints.constrainWidth(width)
        }
        return width
    }

    override fun computeMinIntrinsicHeight(width: Float): Float {
        if (additionalConstraints.hasBoundedHeight && additionalConstraints.hasTightHeight) {
            return additionalConstraints.minHeight
        }
        val height = super.computeMinIntrinsicHeight(width)
        assert(height.isFinite())
        if (additionalConstraints.hasInfiniteHeight) {
            return additionalConstraints.constrainHeight(height)
        }
        return height
    }

    override fun computeMaxIntrinsicHeight(width: Float): Float {
        if (additionalConstraints.hasBoundedHeight && additionalConstraints.hasTightHeight) {
            return additionalConstraints.minHeight
        }
        val height = super.computeMaxIntrinsicHeight(width)
        assert(height.isFinite())
        if (!additionalConstraints.hasInfiniteHeight) {
            return additionalConstraints.constrainHeight(height)
        }
        return height
    }

    override fun performLayout() {
        if (child != null) {
            child.layout(additionalConstraints.enforce(definiteConstraints))
            size = child.definiteSize
        } else {
            size = additionalConstraints.enforce(definiteConstraints).constrain(Size.ZERO)
        }
    }

    override fun debugPaint(context: PaintingContext, offset: Offset) {
        super.debugPaint(context, offset)
        if (child == null || child.definiteSize.isEmpty) {
            context.canvas.drawRect(offset combine definiteSize, Paint().setARGB(144, 144, 144, 144))
        }
    }
}