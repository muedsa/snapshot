package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBackdropFilter
import com.muedsa.snapshot.rendering.box.RenderBox
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.ImageFilter

class BackdropFilter(
    val imageFilter: ImageFilter,
    val blendMode: BlendMode = BlendMode.SRC_OVER,
    childBuilder: SingleWidgetBuilder? = null,
) : SingleChildWidget(childBuilder = childBuilder) {

    override fun createRenderTree(): RenderBox = RenderBackdropFilter(
        imageFilter = imageFilter,
        blendMode = blendMode,
        child = child?.createRenderBox()
    )
}