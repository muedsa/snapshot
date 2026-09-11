package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderColoredBox

inline fun ChildSlot.ColoredBox(
    color: Int,
    content: ColoredBox.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.ColoredBox(
            color = color,
        ).apply(content)
    )
}

class ColoredBox(
    var color: Int,
) : SingleChildWidget() {

    override fun createRenderBox(child: Widget?): RenderBox = RenderColoredBox(
        color = color,
    ).also { p ->
        child?.createRenderBox()?.let {
            p.appendChild(it)
        }
    }
}
