package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.paragraph.BaselineMode
import kotlin.math.min

abstract class RenderContainerBox(val children: Array<RenderBox>?) : RenderBox() {

    init {
        children?.let {
            children.forEachIndexed { index, child ->
                // TODO 这里不太对 构造函数this泄露
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

    fun defaultComputeDistanceToFirstActualBaseline(baseline: BaselineMode): Float? {
        children?.let {
            for (child in it) {
                val result: Float? = child.getDistanceToBaseline(baseline)
                if (result != null) {
                    return result + child.parentData!!.offset.y
                }
            }
        }
        return null
    }

    fun defaultComputeDistanceToHighestActualBaseline(baseline: BaselineMode): Float? {
        var result: Float? = null
        if (!children.isNullOrEmpty()) {
            for (child in children) {
                var candidate: Float? = child.getDistanceToBaseline(baseline)
                if (candidate != null) {
                    candidate += child.parentData!!.offset.y
                    result = result?.let { min(it, candidate) } ?: candidate
                }
            }
        }
        return result
    }


    override fun paint(context: PaintingContext, offset: Offset) {
        defaultPaint(context, offset)
    }

    open fun defaultPaint(context: PaintingContext, offset: Offset) {
        children?.forEach {
            context.paintChild(it, offset + it.parentData!!.offset)
        }
    }
}