package com.muedsa.snapshot.rendering

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Picture

class LayerPaintContext(
    val canvas: Canvas,
) {
    val pictures: MutableList<Picture> = mutableListOf()

    companion object {

        @JvmStatic
        fun composite(context: PaintingContext, canvas: Canvas) {
            context.composite(LayerPaintContext(canvas))
        }
    }
}