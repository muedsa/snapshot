package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderLimitedBox

inline fun Widget.LimitedBox(
    maxWidth: Float = Float.POSITIVE_INFINITY,
    maxHeight: Float = Float.POSITIVE_INFINITY,
    content: LimitedBox.() -> Unit = {},
) {
    buildChild(
        widget = LimitedBox(
            maxWidth = maxWidth,
            maxHeight = maxHeight,
            parent = this
        ),
        content = content
    )
}

class LimitedBox(
    val maxWidth: Float = Float.POSITIVE_INFINITY,
    val maxHeight: Float = Float.POSITIVE_INFINITY,
    parent: Widget? = null,
) : SingleChildWidget(parent = parent) {

    override fun createRenderBox(child: Widget?): RenderBox = RenderLimitedBox(
        maxWidth = maxWidth,
        maxHeight = maxHeight,
    ).also { p ->
        child?.createRenderBox()?.let {
            p.appendChild(it)
        }
    }
}