package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderPositionedBox

inline fun Widget.Align(
    alignment: BoxAlignment = BoxAlignment.CENTER,
    widthFactor: Float? = null,
    heightFactor: Float? = null,
    content: Align.() -> Unit = {},
) {
    buildChild(
        widget = Align(
            alignment = alignment,
            widthFactor = widthFactor,
            heightFactor = heightFactor,
            parent = this
        ),
        content = content
    )
}

open class Align(
    val alignment: BoxAlignment = BoxAlignment.CENTER,
    val widthFactor: Float? = null,
    val heightFactor: Float? = null,
    parent: Widget? = null,
) : SingleChildWidget(parent = parent) {

    override fun createRenderBox(child: Widget?): RenderBox = RenderPositionedBox(
        alignment = alignment,
        widthFactor = widthFactor,
        heightFactor = heightFactor,
        child = child?.createRenderBox()
    )

}