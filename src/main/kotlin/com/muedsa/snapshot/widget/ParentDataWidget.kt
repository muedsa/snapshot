package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox

abstract class ParentDataWidget(
    childBuilder: SingleWidgetBuilder,
) : SingleChildWidget(
    childBuilder = childBuilder
) {
    abstract fun applyParentData(renderBox: RenderBox)

    final override fun createRenderTree(): RenderBox = child!!.createRenderBox()
}