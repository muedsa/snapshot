package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.ColorFilterRenderObject
import com.muedsa.snapshot.rendering.box.RenderBox
import org.jetbrains.skia.ColorFilter

class ColorFiltered(
    val colorFilter: ColorFilter,
    childBuilder: SingleWidgetBuilder? = null,
) : SingleChildWidget(childBuilder = childBuilder) {

    override fun createRenderTree(): RenderBox = ColorFilterRenderObject(
        colorFilter = colorFilter,
        child = child?.createRenderBox()
    )

}