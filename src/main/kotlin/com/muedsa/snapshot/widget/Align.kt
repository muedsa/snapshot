package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderPositionedBox

open class Align(
    val alignment: BoxAlignment = BoxAlignment.CENTER,
    val widthFactor: Float? = null,
    val heightFactor: Float? = null,
    childBuilder: SingleWidgetBuilder? = null,
) : SingleChildWidget(childBuilder = childBuilder) {

    init {
        assert(widthFactor == null || widthFactor >= 0f)
        assert(heightFactor == null || heightFactor >= 0f)
    }

    override fun createRenderTree(): RenderBox = RenderPositionedBox(
        alignment = alignment,
        widthFactor = widthFactor,
        heightFactor = heightFactor,
        child = child?.createRenderTree()
    )
}