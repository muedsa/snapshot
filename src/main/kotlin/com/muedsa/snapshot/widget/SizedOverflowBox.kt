package com.muedsa.snapshot.widget

import com.muedsa.geometry.Alignment
import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.RenderBox
import com.muedsa.snapshot.rendering.RenderSizedOverflowBox

class SizedOverflowBox(
    val size: Size,
    val alignment: Alignment = Alignment.CENTER,
    childBuilder: SingleWidgetBuilder?= null
) : SingleChildWidget(childBuilder = childBuilder) {

    override fun createRenderTree(): RenderBox =
        RenderSizedOverflowBox(
            requestedSize = size,
            alignment = alignment,
            child = child?.createRenderTree()
        )
}