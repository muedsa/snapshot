package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderPositionedBox

inline fun ChildSlot.Align(
    alignment: BoxAlignment = BoxAlignment.CENTER,
    widthFactor: Float? = null,
    heightFactor: Float? = null,
    content: Align.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.Align(
            alignment = alignment,
            widthFactor = widthFactor,
            heightFactor = heightFactor,
        ).apply(content)
    )
}

open class Align(
    var alignment: BoxAlignment = BoxAlignment.CENTER,
    var widthFactor: Float? = null,
    var heightFactor: Float? = null,
) : SingleChildWidget() {

    override fun createRenderBox(child: Widget?): RenderBox = RenderPositionedBox(
        alignment = alignment,
        widthFactor = widthFactor,
        heightFactor = heightFactor,
    ).also { p ->
        child?.createRenderBox()?.let {
            p.appendChild(it)
        }
    }

}
