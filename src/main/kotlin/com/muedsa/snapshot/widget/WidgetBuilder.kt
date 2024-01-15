package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox

typealias SingleWidgetBuilder = () -> Widget?

typealias MultiWidgetBuilder = () -> Array<Widget>

fun Array<out Widget>.createRenderBox(): Array<RenderBox> = Array(this.size) {
    this[it].createRenderBox()
}