package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderOpacity

class Opacity(
    val opacity: Float = 1f,
    childBuilder: SingleWidgetBuilder? = null,
) : SingleChildWidget(childBuilder = childBuilder) {

    init {
        assert(opacity in 0f..1f)
    }

    override fun createRenderTree(): RenderBox = RenderOpacity(
        opacity = opacity,
        child = child?.createRenderBox()
    )
}