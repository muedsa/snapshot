package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderSingleChildBox

abstract class SingleChildWidget(
    parent: Widget?,
) : Widget(parent = parent) {
    var child: Widget? = null

    protected abstract fun createRenderBox(child: Widget?): RenderBox

    final override fun createRenderBox(): RenderBox {
        val child = this.child
        val renderBox = createRenderBox(child)
        if (child is ParentDataWidget && renderBox is RenderSingleChildBox) {
            child.applyParentData(renderBox.child!!)
        }
        return renderBox
    }
}