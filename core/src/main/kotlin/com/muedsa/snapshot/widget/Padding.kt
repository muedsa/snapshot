package com.muedsa.snapshot.widget

import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderPadding

fun ChildSlot.Padding(
    padding: EdgeInsets,
    content: Padding.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.Padding(
            padding = padding,
        ).apply(content)
    )
}

class Padding(
    var padding: EdgeInsets,
) : SingleChildWidget() {

    override fun createRenderBox(child: Widget?): RenderBox {
        return RenderPadding(
            padding = padding,
        ).also { p ->
            child?.createRenderBox()?.let {
                p.appendChild(it)
            }
        }
    }
}
