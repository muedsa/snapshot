package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderColoredBox

inline fun Widget.ColoredBox(
    color: Int,
    content: ColoredBox.() -> Unit = {},
) {
    buildChild(
        widget = ColoredBox(
            color = color,
            parent = this
        ),
        content = content
    )
}

class ColoredBox(
    val color: Int,
    parent: Widget? = null,
) : SingleChildWidget(parent = parent) {

    override fun createRenderBox(child: Widget?): RenderBox = RenderColoredBox(
        color = color,
    ).also { p ->
        child?.createRenderBox()?.let {
            p.appendChild(it)
        }
    }
}