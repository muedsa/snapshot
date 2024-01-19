package com.muedsa.snapshot.rendering

open class ContainerLayer : Layer() {

    private val _children: MutableList<Layer> = mutableListOf()
    val children: List<Layer> = _children

    override fun paint(context: LayerPaintContext) {
        children.forEach {
            it.paint(context)
        }
    }

    fun append(child: Layer) {
        _children.add(child)
        child.parent = this
    }
}