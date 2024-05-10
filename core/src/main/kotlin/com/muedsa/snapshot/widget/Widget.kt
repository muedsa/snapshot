package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox

inline fun <T : Widget> Widget.buildChild(
    widget: T,
    content: T.() -> Unit,
) {
    when (this) {
        is ProxyWidget -> this.widget = widget
        is SingleChildWidget -> this.child = widget
        is MultiChildWidget -> this.appendChild(widget)
        else -> throw IllegalCallerException()
    }
    widget.content()
}

fun Widget.bind(
    child: Widget?,
): Widget {
    child?.let {
        when (this) {
            is ProxyWidget -> this.widget = it
            is SingleChildWidget -> this.child = it
            is MultiChildWidget -> this.appendChild(it)
            else -> throw IllegalCallerException()
        }
        it.parent = this
    }
    return this
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