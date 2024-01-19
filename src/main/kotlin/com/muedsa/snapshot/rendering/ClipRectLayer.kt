package com.muedsa.snapshot.rendering

import org.jetbrains.skia.Rect

class ClipRectLayer(
    val clipRect: Rect,
    val clipBehavior: ClipBehavior = ClipBehavior.HARD_EDGE,
) : ContainerLayer() {


    override fun paint(context: LayerPaintContext) {
        context.canvas.save()
        context.canvas.clipRect(clipRect, antiAlias = clipBehavior == ClipBehavior.ANTI_ALIAS)
        super.paint(context)
        context.canvas.restore()
    }
}