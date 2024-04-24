package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderConstrainedOverflowBox

inline fun Widget.OverflowBox(
    alignment: BoxAlignment = BoxAlignment.CENTER,
    minWidth: Float? = null,
    maxWidth: Float? = null,
    minHeight: Float? = null,
    maxHeight: Float? = null,
    content: OverflowBox.() -> Unit = {},
) {
    buildChild(
        widget = OverflowBox(
            minWidth = minWidth,
            maxWidth = maxWidth,
            minHeight = minHeight,
            maxHeight = maxHeight,
            alignment = alignment,
            parent = this
        ),
        content = content
    )
}

class OverflowBox(
    var alignment: BoxAlignment = BoxAlignment.CENTER,
    var minWidth: Float? = null,
    var maxWidth: Float? = null,
    var minHeight: Float? = null,
    var maxHeight: Float? = null,
    parent: Widget? = null,
) : SingleChildWidget(parent = parent) {

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