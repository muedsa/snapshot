package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderContainerBox
import com.muedsa.snapshot.rendering.box.RenderSingleChildBox

abstract class ParentDataWidget(
    childBuilder: SingleWidgetBuilder,
) : SingleChildWidget(
    childBuilder = childBuilder
) {
    protected abstract fun applyParentData(renderBox: RenderBox)

    override fun createRenderTree(): RenderBox = child!!.createRenderBox()

    fun applyChildParentDate(parent: RenderBox) {
        if (parent is RenderSingleChildBox) {
            parent.child?.let {
                applyParentData(it)
            }
        } else if (parent is RenderContainerBox) {
            parent.children?.forEach {
                applyParentData(it)
            }
        }
    }
}