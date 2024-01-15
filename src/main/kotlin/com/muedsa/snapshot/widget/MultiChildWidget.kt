package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderContainerBox

abstract class MultiChildWidget(
    val children: Array<out Widget>?,
) : Widget() {
    constructor(childrenBuilder: MultiWidgetBuilder?) : this(children = childrenBuilder?.invoke())

    init {
        children?.forEach {
            it.parent = this
        }
    }

    protected abstract fun createRenderTree(): RenderBox

    final override fun createRenderBox(): RenderBox {
        val renderTree = createRenderTree()
        if (children != null) {
            if (renderTree is RenderContainerBox
                && (renderTree.children?.size ?: 0) == children.size
            ) {
                children.forEachIndexed { index, child ->
                    if (child is ParentDataWidget) {
                        child.applyParentData(renderTree.children!![index])
                    }
                }
            }
        }
        return renderTree
    }

}