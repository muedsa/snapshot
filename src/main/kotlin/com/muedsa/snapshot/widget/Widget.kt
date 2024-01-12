package com.muedsa.snapshot.widget

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
}