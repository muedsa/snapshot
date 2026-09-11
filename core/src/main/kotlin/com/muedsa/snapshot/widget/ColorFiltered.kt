package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.ColorFilterRenderObject
import com.muedsa.snapshot.rendering.box.RenderBox
import org.jetbrains.skia.ColorFilter

inline fun ChildSlot.ColorFiltered(
    colorFilter: ColorFilter,
    content: ColorFiltered.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.ColorFiltered(
            colorFilter = colorFilter,
        ).apply(content)
    )
}

class ColorFiltered(
    var colorFilter: ColorFilter,
) : SingleChildWidget() {

    override fun createRenderBox(child: Widget?): RenderBox = ColorFilterRenderObject(
        colorFilter = colorFilter,
    ).also { p ->
        child?.createRenderBox()?.let {
            p.appendChild(it)
        }
    }

}
