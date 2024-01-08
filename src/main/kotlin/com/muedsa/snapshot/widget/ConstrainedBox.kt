package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderConstrainedBox

class ConstrainedBox(
    val constraints: BoxConstraints,
    childBuilder: SingleWidgetBuilder?= null
) : SingleChildWidget(childBuilder = childBuilder) {
    override fun createRenderTree(): RenderBox = RenderConstrainedBox(
        additionalConstraints = constraints,
        child = child?.createRenderTree()
    )
}