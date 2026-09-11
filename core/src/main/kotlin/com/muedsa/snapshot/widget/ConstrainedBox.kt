package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderConstrainedBox

inline fun ChildSlot.ConstrainedBox(
    constraints: BoxConstraints,
    content: ConstrainedBox.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.ConstrainedBox(
            constraints = constraints,
        ).apply(content)
    )
}

class ConstrainedBox(
    var constraints: BoxConstraints,
) : SingleChildWidget() {
    override fun createRenderBox(child: Widget?): RenderBox = RenderConstrainedBox(
        additionalConstraints = constraints,
    ).also { p ->
        child?.createRenderBox()?.let {
            p.appendChild(it)
        }
    }
}
