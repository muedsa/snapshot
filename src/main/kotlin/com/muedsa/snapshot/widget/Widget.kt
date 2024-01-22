package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox

inline fun <T : Widget> Widget.buildChild(
    widget: T,
    content: T.() -> Unit,
) {
    when (this) {
        is ProxyWidget -> this.widget = widget
        is SingleChildWidget -> this.child = widget
        is MultiChildWidget -> this.children.add(widget)
        else -> throw IllegalCallerException()
    }
    widget.content()
}

abstract class Widget(
    parent: Widget? = null,
) {
    var parent: Widget? = null
        set(value) {
            var temp = value
            while (temp != null) {
                assert(temp != this) { "widget tree circulate" }
                temp = temp.parent
            }
            field = value
        }

    abstract fun createRenderBox(): RenderBox

    init {
        this.parent = parent
    }
}