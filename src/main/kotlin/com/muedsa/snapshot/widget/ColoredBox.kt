package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.RenderBox
import com.muedsa.snapshot.rendering.RenderColoredBox


class ColoredBox(
    val color: Int,
    childBuilder: SingleWidgetBuilder?= null
) : SingleChildWidget(childBuilder = childBuilder) {

    override fun createRenderTree(): RenderBox = RenderColoredBox(color = color, child = child?.createRenderTree())
}