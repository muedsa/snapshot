package com.muedsa.snapshot.rendering

import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect

class BackdropFilterLayer(
    val bounds: Rect,
    val filter: ImageFilter,
    val blendMode: BlendMode = BlendMode.SRC_OVER,
) : ContainerLayer() {

    override fun paint(context: LayerPaintContext) {
        context.pictures.lastOrNull()?.also {
            context.canvas.drawPicture(it, paint = Paint().apply {
                this@apply.imageFilter = this@BackdropFilterLayer.filter
                this@apply.blendMode = this@BackdropFilterLayer.blendMode
            })
        }
        super.paint(context)
    }
}