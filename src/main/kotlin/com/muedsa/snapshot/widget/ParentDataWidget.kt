package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox

abstract class ParentDataWidget(
    val child: Widget,
) : Widget() {

    init {
        child.let { it.parent = this }
    }

    abstract fun applyParentData(renderBox: RenderBox)

    final override fun createRenderBox(): RenderBox {
        return child.createRenderBox()
    }
}