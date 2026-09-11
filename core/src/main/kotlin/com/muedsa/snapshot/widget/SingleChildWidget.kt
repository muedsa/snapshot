package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderSingleChildBox

abstract class SingleChildWidget(
    parent: Widget?,
) : Widget(parent = parent), ChildSlot {
    var child: Widget? = null
        protected set

    override fun attach(child: Widget) {
        check(this.child == null) {
            "${this::class.simpleName} already has a child, can not attach ${child::class.simpleName}"
        }
        this.child = child
        child.parent = this
    }

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
