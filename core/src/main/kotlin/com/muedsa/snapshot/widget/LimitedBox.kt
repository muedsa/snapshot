package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderLimitedBox

inline fun ChildSlot.LimitedBox(
    maxWidth: Float = Float.POSITIVE_INFINITY,
    maxHeight: Float = Float.POSITIVE_INFINITY,
    content: LimitedBox.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.LimitedBox(
            maxWidth = maxWidth,
            maxHeight = maxHeight,
        ).apply(content)
    )
}

class LimitedBox(
    var maxWidth: Float = Float.POSITIVE_INFINITY,
    var maxHeight: Float = Float.POSITIVE_INFINITY,
) : SingleChildWidget() {

    override fun createRenderBox(child: Widget?): RenderBox = RenderLimitedBox(
        maxWidth = maxWidth,
        maxHeight = maxHeight,
    ).also { p ->
        child?.createRenderBox()?.let {
            p.appendChild(it)
        }
    }
}
