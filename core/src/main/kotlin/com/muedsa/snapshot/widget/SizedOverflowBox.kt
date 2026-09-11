package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderSizedOverflowBox

inline fun ChildSlot.SizedOverflowBox(
    size: Size,
    alignment: BoxAlignment = BoxAlignment.CENTER,
    content: SizedOverflowBox.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.SizedOverflowBox(
            size = size,
            alignment = alignment,
        ).apply(content)
    )
}

class SizedOverflowBox(
    var size: Size,
    var alignment: BoxAlignment = BoxAlignment.CENTER,
) : SingleChildWidget() {

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
