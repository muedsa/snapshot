package com.muedsa.snapshot.rendering

import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.Paint

class ColorFilterLayer(
    val filter: ColorFilter,
) : ContainerLayer() {

    override fun paint(context: LayerPaintContext) {
        context.canvas.saveLayer(bounds = null, Paint().also {
            it.colorFilter = filter
        })
        super.paint(context)
        context.canvas.restore()
    }
}