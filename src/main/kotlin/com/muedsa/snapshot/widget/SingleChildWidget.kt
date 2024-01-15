package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox

abstract class SingleChildWidget(
    val child: Widget?,
) : Widget() {

    constructor(childBuilder: SingleWidgetBuilder?) : this(child = childBuilder?.invoke())

    init {
        child?.let {
            it.parent = this
        }
    }

    protected abstract fun createRenderTree(): RenderBox

    final override fun createRenderBox(): RenderBox {
        val renderTree = createRenderTree()
        if (child is ParentDataWidget) {
            child.applyChildParentDate(renderTree)
        }
        return renderTree
    }
}