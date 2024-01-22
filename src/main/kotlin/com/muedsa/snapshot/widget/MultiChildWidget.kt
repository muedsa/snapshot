package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderContainerBox

abstract class MultiChildWidget(
    parent: Widget?,
) : Widget(parent = parent) {
    val children: MutableList<Widget> = mutableListOf()

    protected abstract fun createRenderBox(children: List<Widget>): RenderBox

    final override fun createRenderBox(): RenderBox {
        val children = this.children.toList()
        val renderBox = createRenderBox(children)
        if (renderBox is RenderContainerBox
            && (renderBox.children?.size ?: 0) == children.size
        ) {
            children.forEachIndexed { index, child ->
                if (child is ParentDataWidget) {
                    child.applyParentData(renderBox.children!![index])
                }
            }
        }
        return renderBox
    }
}

fun List<Widget>.createRenderBox(): Array<RenderBox>? =
    if (isEmpty()) null else Array(this.size) { this[it].createRenderBox() }