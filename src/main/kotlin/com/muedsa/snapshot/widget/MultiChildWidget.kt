package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox

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
        children?.forEach {
            if (it is ParentDataWidget) {
                it.applyChildParentDate(renderTree)
            }
        }
        return renderTree
    }

}