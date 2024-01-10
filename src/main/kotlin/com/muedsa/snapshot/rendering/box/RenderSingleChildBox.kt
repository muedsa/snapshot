package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.paragraph.BaselineMode

abstract class RenderSingleChildBox(val child: RenderBox?) : RenderBox() {

    init {
        child?.let {
            it.parent = this
        }
    }

    override fun computeDistanceToActualBaseline(baseline: BaselineMode): Float? {
        return child?.getDistanceToBaseline(baseline)
    }

    override fun performLayout() {
        if (child != null) {
            child.layout(definiteConstraints)
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