package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.rendering.PaintingContext

abstract class RenderContainerBox(val children: Array<RenderBox>?) : RenderBox() {

    init {
        children?.let {
            children.forEachIndexed { index, child ->
                child.parent = this
                val childParentData: ContainerBoxParentData = child.parentData!! as ContainerBoxParentData
                if (index > 0) {
                    childParentData.previousSibling = children[index - 1]
                }
                if (index < children.size - 1) {
                    childParentData.nextSibling = children[index + 1]
                }
            }
        }
    }

    val childCount: Int = children?.size ?: 0

    override fun paint(context: PaintingContext, offset: Offset) {
        defaultPaint(context, offset)
    }

    open fun defaultPaint(context: PaintingContext, offset: Offset) {
        children?.forEach {
            context.paintChild(it, offset + it.parentData!!.offset)
        }
    }
}