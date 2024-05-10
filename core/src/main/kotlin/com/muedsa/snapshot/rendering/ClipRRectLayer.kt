package com.muedsa.snapshot.rendering

import org.jetbrains.skia.RRect

class ClipRRectLayer(
    val clipRRect: RRect,
    val clipBehavior: ClipBehavior = ClipBehavior.HARD_EDGE,
) : ContainerLayer() {


    override fun paint(context: LayerPaintContext) {
        context.canvas.save()
        context.canvas.clipRRect(clipRRect, antiAlias = clipBehavior == ClipBehavior.ANTI_ALIAS)
        super.paint(context)
        context.canvas.restore()
    }
}