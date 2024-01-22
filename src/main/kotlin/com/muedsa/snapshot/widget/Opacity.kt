package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderOpacity

inline fun Widget.Opacity(
    opacity: Float = 1f,
    content: Opacity.() -> Unit = {},
) {
    buildChild(
        widget = Opacity(
            opacity = opacity,
            parent = this
        ),
        content = content
    )
}

class Opacity(
    val opacity: Float = 1f,
    parent: Widget? = null,
) : SingleChildWidget(parent = parent) {

    init {
        assert(opacity in 0f..1f)
    }

    override fun createRenderBox(child: Widget?): RenderBox = RenderOpacity(
        opacity = opacity,
        child = child?.createRenderBox()
    )
}