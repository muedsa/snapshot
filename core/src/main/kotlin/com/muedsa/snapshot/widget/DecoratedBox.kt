package com.muedsa.snapshot.widget

import com.muedsa.snapshot.paint.decoration.Decoration
import com.muedsa.snapshot.rendering.box.DecorationPosition
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderDecoratedBox

inline fun ChildSlot.DecoratedBox(
    decoration: Decoration,
    position: DecorationPosition = DecorationPosition.BACKGROUND,
    content: DecoratedBox.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.DecoratedBox(
            decoration = decoration,
            position = position,
        ).apply(content)
    )
}

class DecoratedBox(
    var decoration: Decoration,
    var position: DecorationPosition = DecorationPosition.BACKGROUND,
) : SingleChildWidget() {
    override fun createRenderBox(child: Widget?): RenderBox = RenderDecoratedBox(
        decoration = decoration,
        position = position,
    ).also { p ->
        child?.createRenderBox()?.let {
            p.appendChild(it)
        }
    }
}
