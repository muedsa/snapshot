package com.muedsa.snapshot.rendering

import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.Paint

class ImageFilterLayer(
    val filter: ImageFilter,
) : ContainerLayer() {

    override fun paint(context: LayerPaintContext) {
        context.canvas.saveLayer(bounds = null, Paint().also { it.imageFilter = filter })
        super.paint(context)
        context.canvas.restore()
    }
}