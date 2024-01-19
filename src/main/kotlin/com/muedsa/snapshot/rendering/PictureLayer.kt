package com.muedsa.snapshot.rendering

import org.jetbrains.skia.Picture

class PictureLayer : ContainerLayer() {
    var picture: Picture? = null

    override fun paint(context: LayerPaintContext) {
        picture?.let {
            context.pictures.add(it)
            context.canvas.drawPicture(it)
        }
        super.paint(context)
    }
}