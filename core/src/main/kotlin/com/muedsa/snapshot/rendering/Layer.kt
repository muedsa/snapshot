package com.muedsa.snapshot.rendering

abstract class Layer {

    var parent: Layer? = null

    abstract fun paint(context: LayerPaintContext)
}