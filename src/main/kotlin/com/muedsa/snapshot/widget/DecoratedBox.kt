package com.muedsa.snapshot.widget

import com.muedsa.snapshot.paint.decoration.Decoration
import com.muedsa.snapshot.rendering.box.DecorationPosition
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderDecoratedBox

class DecoratedBox(
    val decoration: Decoration,
    val position: DecorationPosition = DecorationPosition.BACKGROUND,
    childBuilder: SingleWidgetBuilder? = null,
) : SingleChildWidget(childBuilder = childBuilder) {
    override fun createRenderTree(): RenderBox = RenderDecoratedBox(
        decoration = decoration,
        position = position,
        child = child?.createRenderBox()
    )
}