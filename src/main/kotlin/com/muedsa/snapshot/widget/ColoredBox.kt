package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderColoredBox


class ColoredBox(
    val color: Int,
    childBuilder: SingleWidgetBuilder? = null,
) : SingleChildWidget(childBuilder = childBuilder) {

    override fun createRenderTree(): RenderBox = RenderColoredBox(color = color, child = child?.createRenderTree())
}