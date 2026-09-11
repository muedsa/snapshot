package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderSingleChildBox

abstract class SingleChildWidget : Widget(), ChildSlot {
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
            val target = renderBox.child
            checkNotNull(target) {
                "${this::class.simpleName} produced a ${renderBox::class.simpleName} without a child " +
                    "render box, so parent data of ${child::class.simpleName} can not be applied"
            }
            child.applyParentData(target)
        }
        return renderBox
    }
}
