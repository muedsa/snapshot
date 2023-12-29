package com.muedsa.snapshot.widget

import com.muedsa.snapshot.paint.Decoration
import com.muedsa.snapshot.rendering.DecorationPosition
import com.muedsa.snapshot.rendering.RenderBox
import com.muedsa.snapshot.rendering.RenderDecoratedBox

class DecoratedBox(
    val decoration: Decoration,
    val position: DecorationPosition = DecorationPosition.BACKGROUND,
    childBuilder: SingleWidgetBuilder?= null
) : SingleChildWidget(childBuilder = childBuilder) {
    override fun createRenderTree(): RenderBox = RenderDecoratedBox(
        decoration = decoration,
        position = position,
        child = child?.createRenderTree()
    )
}