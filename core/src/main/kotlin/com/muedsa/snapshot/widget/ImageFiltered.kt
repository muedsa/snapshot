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
    var imageFilter: ImageFilter,
    parent: Widget? = null,
) : SingleChildWidget(parent = parent) {
    override fun createRenderBox(child: Widget?): RenderBox = ImageFilterRenderObject(
        imageFilter = imageFilter,
    ).also { p ->
        child?.createRenderBox()?.let {
            p.appendChild(it)
        }
    }

}