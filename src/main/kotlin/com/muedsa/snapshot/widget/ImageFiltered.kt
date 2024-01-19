package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.ImageFilterRenderObject
import com.muedsa.snapshot.rendering.box.RenderBox
import org.jetbrains.skia.ImageFilter

class ImageFiltered(
    val imageFilter: ImageFilter,
    childBuilder: SingleWidgetBuilder? = null,
) : SingleChildWidget(childBuilder = childBuilder) {
    override fun createRenderTree(): RenderBox = ImageFilterRenderObject(
        imageFilter = imageFilter,
        child = child?.createRenderBox()
    )

}