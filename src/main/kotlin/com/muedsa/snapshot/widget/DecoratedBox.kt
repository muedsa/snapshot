package com.muedsa.snapshot.widget

import com.muedsa.snapshot.paint.decoration.Decoration
import com.muedsa.snapshot.rendering.box.DecorationPosition
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderDecoratedBox

inline fun Widget.DecoratedBox(
    decoration: Decoration,
    position: DecorationPosition = DecorationPosition.BACKGROUND,
    content: DecoratedBox.() -> Unit = {},
) {
    buildChild(
        widget = DecoratedBox(
            decoration = decoration,
            position = position,
            parent = this
        ),
        content = content
    )
}

class DecoratedBox(
    val decoration: Decoration,
    val position: DecorationPosition = DecorationPosition.BACKGROUND,
    parent: Widget? = null,
) : SingleChildWidget(parent = parent) {
    override fun createRenderBox(child: Widget?): RenderBox = RenderDecoratedBox(
        decoration = decoration,
        position = position,
    ).also { p ->
        child?.createRenderBox()?.let {
            p.appendChild(it)
        }
    }
}