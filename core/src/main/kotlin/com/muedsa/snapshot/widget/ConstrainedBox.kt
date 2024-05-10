package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderConstrainedBox

inline fun Widget.ConstrainedBox(
    constraints: BoxConstraints,
    content: ConstrainedBox.() -> Unit = {},
) {
    buildChild(
        widget = ConstrainedBox(
            constraints = constraints,
            parent = this
        ),
        content = content
    )
}

class ConstrainedBox(
    var constraints: BoxConstraints,
    parent: Widget? = null,
) : SingleChildWidget(parent = parent) {
    override fun createRenderBox(child: Widget?): RenderBox = RenderConstrainedBox(
        additionalConstraints = constraints,
    ).also { p ->
        child?.createRenderBox()?.let {
            p.appendChild(it)
        }
    }
}