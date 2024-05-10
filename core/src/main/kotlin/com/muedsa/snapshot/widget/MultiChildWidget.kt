package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderContainerBox

abstract class MultiChildWidget(
    parent: Widget?,
) : Widget(parent = parent) {
    protected val _children: MutableList<Widget> = mutableListOf()

    val children: List<Widget> = _children

    @Synchronized
    fun appendChild(child: Widget) {
        check(!_children.contains(child)) {
            "MultiChildWidget cant append duplicate child"
        }
        _children.add(child)
    }

    @Synchronized
    fun appendChildren(list: List<Widget>) {
        if (list.isNotEmpty()) {
            list.forEach { appendChild(child = it) }
        }
    }

    protected abstract fun createRenderBox(children: List<Widget>): RenderBox

    final override fun createRenderBox(): RenderBox {
        val currentChildren = _children.toList()
        val renderBox = createRenderBox(currentChildren)
        if (renderBox is RenderContainerBox
            && renderBox.children.size == currentChildren.size
        ) {
            children.forEachIndexed { index, child ->
                if (child is ParentDataWidget) {
                    child.applyParentData(renderBox.children[index])
                }
            }
        }
        return renderBox
    }
}

fun List<Widget>.createRenderBox(): List<RenderBox>? =
    if (isEmpty()) null else map { it.createRenderBox() }