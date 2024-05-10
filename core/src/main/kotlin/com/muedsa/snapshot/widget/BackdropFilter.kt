package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBackdropFilter
import com.muedsa.snapshot.rendering.box.RenderBox
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.ImageFilter

inline fun Widget.BackdropFilter(
    imageFilter: ImageFilter,
    blendMode: BlendMode = BlendMode.SRC_OVER,
    content: BackdropFilter.() -> Unit = {},
) {
    buildChild(
        widget = BackdropFilter(
            imageFilter = imageFilter,
            blendMode = blendMode,
            parent = this
        ),
        content = content
    )
}

class BackdropFilter(
    var imageFilter: ImageFilter,
    var blendMode: BlendMode = BlendMode.SRC_OVER,
    parent: Widget? = null,
) : SingleChildWidget(parent = parent) {

    override fun createRenderBox(child: Widget?): RenderBox = RenderBackdropFilter(
        imageFilter = imageFilter,
        blendMode = blendMode,
    ).also { p ->
        child?.createRenderBox()?.let {
            p.appendChild(it)
        }
    }
}