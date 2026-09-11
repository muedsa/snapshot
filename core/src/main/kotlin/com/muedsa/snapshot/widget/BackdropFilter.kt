package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBackdropFilter
import com.muedsa.snapshot.rendering.box.RenderBox
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.ImageFilter

inline fun ChildSlot.BackdropFilter(
    imageFilter: ImageFilter,
    blendMode: BlendMode = BlendMode.SRC_OVER,
    content: BackdropFilter.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.BackdropFilter(
            imageFilter = imageFilter,
            blendMode = blendMode,
        ).apply(content)
    )
}

class BackdropFilter(
    var imageFilter: ImageFilter,
    var blendMode: BlendMode = BlendMode.SRC_OVER,
) : SingleChildWidget() {

    override fun createRenderBox(child: Widget?): RenderBox = RenderBackdropFilter(
        imageFilter = imageFilter,
        blendMode = blendMode,
    ).also { p ->
        child?.createRenderBox()?.let {
            p.appendChild(it)
        }
    }
}
