package com.muedsa.snapshot.widget

import com.muedsa.snapshot.annotation.MustCallSuper
import com.muedsa.snapshot.rendering.box.RenderBox

abstract class Widget {

    var parent: Widget? = null
        set(value) {
            var temp = value
            while (temp != null) {
                assert(temp != this) { "widget tree circulate" }
                temp = value?.parent
            }
            field = value
        }

    abstract fun createRenderTree(): RenderBox

    @MustCallSuper
    open fun dispose() { }

    operator fun plus(widget: Widget): Array<Widget> = arrayOf(this, widget)

    operator fun Array<Widget>.plus(widget: Widget): Array<Widget> = arrayOf(*this, widget)
}