package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderContainerBox

abstract class MultiChildWidget : Widget(), ChildSlot {
    private val _children: MutableList<Widget> = mutableListOf()

    /** 子节点的只读视图。**注意是活视图而不是快照**:遍历期间不要并发 append。 */
    val children: List<Widget> = _children

    fun appendChild(child: Widget) {
        check(!_children.contains(child)) {
            "${this::class.simpleName} cant append duplicate child ${child::class.simpleName}"
        }
        _children.add(child)
    }

    fun appendChildren(list: List<Widget>) {
        if (list.isNotEmpty()) {
            list.forEach { appendChild(child = it) }
        }
    }

    override fun attach(child: Widget) {
        appendChild(child)
        child.parent = this
    }

    protected abstract fun createRenderBox(children: List<Widget>): RenderBox

    final override fun createRenderBox(): RenderBox {
        // 全程用同一份快照,避免"按拷贝构建出来的 RenderBox 树"与"活视图"对不上
        val currentChildren = _children.toList()
        val renderBox = createRenderBox(currentChildren)
        if (renderBox is RenderContainerBox) {
            check(renderBox.children.size == currentChildren.size) {
                "${this::class.simpleName} produced ${renderBox.children.size} render box(es) " +
                    "for ${currentChildren.size} child widget(s); parent data can not be applied"
            }
            currentChildren.forEachIndexed { index, child ->
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
