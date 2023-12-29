package com.muedsa.snapshot.rendering

import com.muedsa.geometry.Offset

abstract class RenderSingleChildBox(val child: RenderBox?) : RenderBox() {

    init {
        child?.let {
            it.parent = this
        }
    }

    override fun computeMinIntrinsicWidth(height: Float): Float {
        return child?.getMinIntrinsicWidth(height) ?: 0f
    }

    override fun computeMaxIntrinsicWidth(height: Float): Float {
        return child?.getMaxIntrinsicWidth(height) ?: 0f
    }

    override fun computeMinIntrinsicHeight(width: Float): Float {
        return child?.getMinIntrinsicHeight(width) ?: 0f
    }

    override fun computeMaxIntrinsicHeight(width: Float): Float {
        return child?.getMaxIntrinsicHeight(width) ?: 0f
    }

    override fun performLayout() {
        if (child != null) {
            child.layout(definiteConstraints);
            size = child.definiteSize
        } else {
            size = definiteConstraints.smallest
        }
    }

    override fun paint(context: PaintingContext, offset: Offset) {
        child?.let {
            context.paintChild(it, offset + it.parentData!!.offset)
        }
    }
}