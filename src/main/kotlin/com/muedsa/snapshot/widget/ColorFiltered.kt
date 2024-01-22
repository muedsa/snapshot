package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.ColorFilterRenderObject
import com.muedsa.snapshot.rendering.box.RenderBox
import org.jetbrains.skia.ColorFilter

inline fun Widget.ColorFiltered(
    colorFilter: ColorFilter,
    content: ColorFiltered.() -> Unit = {},
) {
    buildChild(
        widget = ColorFiltered(
            colorFilter = colorFilter,
            parent = this
        ),
        content = content
    )
}

class ColorFiltered(
    val colorFilter: ColorFilter,
    parent: Widget? = null,
) : SingleChildWidget(parent = parent) {

    override fun createRenderBox(child: Widget?): RenderBox = ColorFilterRenderObject(
        colorFilter = colorFilter,
        child = child?.createRenderBox()
    )

}