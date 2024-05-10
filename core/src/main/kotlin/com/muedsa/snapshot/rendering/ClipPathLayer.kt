package com.muedsa.snapshot.rendering

import org.jetbrains.skia.Path

class ClipPathLayer(
    val clipPath: Path,
    val clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
) : ContainerLayer() {


    override fun paint(context: LayerPaintContext) {
        context.canvas.save()
        context.canvas.clipPath(clipPath, antiAlias = clipBehavior == ClipBehavior.ANTI_ALIAS)
        super.paint(context)
        context.canvas.restore()
    }
}