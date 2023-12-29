package com.muedsa.snapshot.widget

import com.muedsa.geometry.Alignment
import com.muedsa.snapshot.rendering.RenderBox
import com.muedsa.snapshot.rendering.RenderConstrainedOverflowBox

class OverflowBox(
    val alignment: Alignment = Alignment.CENTER,
    val minWidth: Float? = null,
    val maxWidth: Float? = null,
    val minHeight: Float? = null,
    val maxHeight: Float? = null,
    childBuilder: SingleWidgetBuilder?= null
) : SingleChildWidget(childBuilder = childBuilder) {

    override fun createRenderTree(): RenderBox = RenderConstrainedOverflowBox(
        minWidth = minWidth,
        maxWidth = maxWidth,
        minHeight = minHeight,
        maxHeight = maxHeight,
        alignment = alignment,
        child = child?.createRenderTree()
    )
}