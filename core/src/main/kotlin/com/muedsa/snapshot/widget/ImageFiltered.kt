package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.ImageFilterRenderObject
import com.muedsa.snapshot.rendering.box.RenderBox
import org.jetbrains.skia.ImageFilter

inline fun ChildSlot.ImageFiltered(
    imageFilter: ImageFilter,
    content: ImageFiltered.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.ImageFiltered(
            imageFilter = imageFilter,
        ).apply(content)
    )
}

class ImageFiltered(
    var imageFilter: ImageFilter,
) : SingleChildWidget() {
    override fun createRenderBox(child: Widget?): RenderBox = ImageFilterRenderObject(
        imageFilter = imageFilter,
    ).also { p ->
        child?.createRenderBox()?.let {
            p.appendChild(it)
        }
    }

}
