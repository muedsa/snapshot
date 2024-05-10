package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderSizedOverflowBox

inline fun Widget.SizedOverflowBox(
    size: Size,
    alignment: BoxAlignment = BoxAlignment.CENTER,
    content: SizedOverflowBox.() -> Unit = {},
) {
    buildChild(
        widget = SizedOverflowBox(
            size = size,
            alignment = alignment,
            parent = this
        ),
        content = content
    )
}

class SizedOverflowBox(
    var size: Size,
    var alignment: BoxAlignment = BoxAlignment.CENTER,
    parent: Widget? = null,
) : SingleChildWidget(parent = parent) {

    override fun createRenderBox(child: Widget?): RenderBox =
        RenderSizedOverflowBox(
            requestedSize = size,
            alignment = alignment,
        ).also { p ->
            child?.createRenderBox()?.let {
                p.appendChild(it)
            }
        }
}