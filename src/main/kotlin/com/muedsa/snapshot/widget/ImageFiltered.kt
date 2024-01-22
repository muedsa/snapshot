package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.ImageFilterRenderObject
import com.muedsa.snapshot.rendering.box.RenderBox
import org.jetbrains.skia.ImageFilter

inline fun Widget.ImageFiltered(
    imageFilter: ImageFilter,
    content: ImageFiltered.() -> Unit = {},
) {
    buildChild(
        widget = ImageFiltered(
            imageFilter = imageFilter,
            parent = this
        ),
        content = content
    )
}

class ImageFiltered(
    val imageFilter: ImageFilter,
    parent: Widget? = null,
) : SingleChildWidget(parent = parent) {
    override fun createRenderBox(child: Widget?): RenderBox = ImageFilterRenderObject(
        imageFilter = imageFilter,
        child = child?.createRenderBox()
    )

}