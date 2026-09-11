package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderOpacity

inline fun ChildSlot.Opacity(
    opacity: Float = 1f,
    content: Opacity.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.Opacity(
            opacity = opacity,
        ).apply(content)
    )
}

class Opacity(
    var opacity: Float = 1f,
) : SingleChildWidget() {

    init {
        assert(opacity in 0f..1f)
    }

    override fun createRenderBox(child: Widget?): RenderBox = RenderOpacity(
        opacity = opacity,
    ).also { p ->
        child?.createRenderBox()?.let {
            p.appendChild(it)
        }
    }
}
