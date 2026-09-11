package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderConstrainedOverflowBox

inline fun ChildSlot.OverflowBox(
    alignment: BoxAlignment = BoxAlignment.CENTER,
    minWidth: Float? = null,
    maxWidth: Float? = null,
    minHeight: Float? = null,
    maxHeight: Float? = null,
    content: OverflowBox.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.OverflowBox(
            minWidth = minWidth,
            maxWidth = maxWidth,
            minHeight = minHeight,
            maxHeight = maxHeight,
            alignment = alignment,
        ).apply(content)
    )
}

class OverflowBox(
    var alignment: BoxAlignment = BoxAlignment.CENTER,
    var minWidth: Float? = null,
    var maxWidth: Float? = null,
    var minHeight: Float? = null,
    var maxHeight: Float? = null,
) : SingleChildWidget() {

    override fun createRenderBox(child: Widget?): RenderBox = RenderConstrainedOverflowBox(
        minWidth = minWidth,
        maxWidth = maxWidth,
        minHeight = minHeight,
        maxHeight = maxHeight,
        alignment = alignment,
    ).also { p ->
        child?.createRenderBox()?.let {
            p.appendChild(it)
        }
    }
}
