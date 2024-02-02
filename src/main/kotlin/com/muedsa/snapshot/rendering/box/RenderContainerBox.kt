package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.paragraph.BaselineMode
import kotlin.math.min

abstract class RenderContainerBox : RenderBox() {

    private val _children: MutableList<RenderBox> = mutableListOf()

    val children: List<RenderBox> = _children

    @Synchronized
    fun appendChild(child: RenderBox) {
        check(!_children.contains(child)) {
            "RenderContainerBox cant append duplicate child"
        }
        _children.add(child)
        val index = _children.size - 1
        child.parent = this
        val childParentData: ContainerBoxParentData = child.parentData!! as ContainerBoxParentData
        if (index > 0) {
            childParentData.previousSibling = _children[index - 1]
        }
        if (index < _children.size - 1) {
            childParentData.nextSibling = _children[index + 1]
        }
    }

    @Synchronized
    fun appendChildren(list: List<RenderBox>) {
        if (list.isNotEmpty()) {
            list.forEach { appendChild(child = it) }
        }
    }

    val childCount: Int get() = _children.size

    fun defaultComputeDistanceToFirstActualBaseline(baseline: BaselineMode): Float? {
        for (child in children) {
            val result: Float? = child.getDistanceToBaseline(baseline)
            if (result != null) {
                return result + child.parentData!!.offset.y
            }
        }
        return null
    }

    fun defaultComputeDistanceToHighestActualBaseline(baseline: BaselineMode): Float? {
        var result: Float? = null
        if (children.isNotEmpty()) {
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
        children.forEach {
            context.paintChild(it, offset + it.parentData!!.offset)
        }
    }
}