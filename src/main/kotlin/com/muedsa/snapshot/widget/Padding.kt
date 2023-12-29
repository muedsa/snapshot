package com.muedsa.snapshot.widget

import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.rendering.RenderBox
import com.muedsa.snapshot.rendering.RenderPadding

class Padding(
    val padding: EdgeInsets,
    childBuilder: SingleWidgetBuilder?= null
) : SingleChildWidget(childBuilder = childBuilder) {

    override fun createRenderTree(): RenderBox = RenderPadding(padding, child = child?.createRenderTree())
}