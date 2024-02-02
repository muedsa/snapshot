package com.muedsa.snapshot.widget

import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderPadding

fun Widget.Padding(
    padding: EdgeInsets,
    content: Padding.() -> Unit = {},
) {
    buildChild(
        widget = Padding(
            padding = padding,
            parent = this
        ),
        content = content
    )
}

class Padding(
    val padding: EdgeInsets,
    parent: Widget?,
) : SingleChildWidget(parent = parent) {

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