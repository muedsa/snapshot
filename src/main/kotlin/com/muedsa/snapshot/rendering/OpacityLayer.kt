package com.muedsa.snapshot.rendering

import org.jetbrains.skia.Paint

class OpacityLayer(
    val opacity: Float = 1f,
) : ContainerLayer() {

    override fun paint(context: LayerPaintContext) {
        context.canvas.saveLayer(bounds = null, Paint().setAlphaf(opacity))
        super.paint(context)
        context.canvas.restore()
    }
}