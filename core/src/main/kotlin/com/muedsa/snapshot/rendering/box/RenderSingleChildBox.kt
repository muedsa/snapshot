package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.paragraph.BaselineMode

abstract class RenderSingleChildBox : RenderBox() {

    var child: RenderBox? = null

    open fun appendChild(child: RenderBox) {
        this.child = child
        child.parent = this
    }

    override fun computeDistanceToActualBaseline(baseline: BaselineMode): Float? {
        // 委托给"不带回退"的访问器:无基线子树向上报告 null,是否退化为盒高由最外层查询决定
        return child?.getDistanceToActualBaseline(baseline)
    }

    override fun performLayout() {
        val currentChild: RenderBox? = this.child
        if (currentChild != null) {
            currentChild.layout(definiteConstraints)
            size = currentChild.definiteSize
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