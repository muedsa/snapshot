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
    val size: Size,
    val alignment: BoxAlignment = BoxAlignment.CENTER,
    parent: Widget? = null,
) : SingleChildWidget(parent = parent) {

    override fun createRenderBox(child: Widget?): RenderBox =
        RenderSizedOverflowBox(
            requestedSize = size,
            alignment = alignment,
            child = child?.createRenderBox()
        )
}