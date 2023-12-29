package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.BoxConstraints
import com.muedsa.snapshot.rendering.RenderBox
import com.muedsa.snapshot.rendering.RenderConstrainedBox

class ConstrainedBox(
    val constraints: BoxConstraints,
    childBuilder: SingleWidgetBuilder?= null
) : SingleChildWidget(childBuilder = childBuilder) {
    override fun createRenderTree(): RenderBox = RenderConstrainedBox(
        additionalConstraints = constraints,
        child = child?.createRenderTree()
    )
}