package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.paragraph.BaselineMode
import kotlin.math.min

abstract class RenderContainerBox : RenderBox() {

    private val _children: MutableList<RenderBox> = mutableListOf()

    val children: List<RenderBox> = _children

    fun appendChild(child: RenderBox) {
        check(!_children.contains(child)) {
            "RenderContainerBox cant append duplicate child"
        }
        _children.add(child)
        val index = _children.size - 1
        child.parent = this
        val parentData = child.parentData
        check(parentData is ContainerBoxParentData) {
            "${this::class.simpleName} requires a ContainerBoxParentData from setupParentData(), " +
                "but got ${parentData?.let { it::class.simpleName } ?: "null"}"
        }
        if (index > 0) {
            parentData.previousSibling = _children[index - 1]
        }
        if (index < _children.size - 1) {
            parentData.nextSibling = _children[index + 1]
        }
    }

    fun appendChildren(list: List<RenderBox>) {
        if (list.isNotEmpty()) {
            list.forEach { appendChild(child = it) }
        }
    }

    val childCount: Int get() = _children.size

    fun defaultComputeDistanceToFirstActualBaseline(baseline: BaselineMode): Float? {
        for (child in children) {
            // 不带回退:无基线子树应报告 null,而不是被替换成盒高(与 Flutter 的默认基线一致)
            val result: Float? = child.getDistanceToActualBaseline(baseline)
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
                // 不带回退,同 defaultComputeDistanceToFirstActualBaseline
                var candidate: Float? = child.getDistanceToActualBaseline(baseline)
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