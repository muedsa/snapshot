package com.muedsa.snapshot.rendering

import com.muedsa.geometry.Matrix44CMO

class TransformLayer(
    val transform: Matrix44CMO,
) : ContainerLayer() {


    override fun paint(context: LayerPaintContext) {
        context.canvas.save()
        // skia的Matrix44为行顺序
        context.canvas.concat(transform.toRMO())
        super.paint(context)
        context.canvas.restore()
    }
}