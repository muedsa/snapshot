package com.muedsa.snapshot.widget

import com.muedsa.snapshot.annotation.MustCallSuper
import com.muedsa.snapshot.rendering.RenderBox

abstract class SingleChildWidget(
    val child: Widget?
) : Widget() {

    constructor(childBuilder: SingleWidgetBuilder?) : this(child = childBuilder?.invoke())

    init {
        child?.let {
            it.parent = this
        }
    }

    abstract override fun createRenderTree(): RenderBox


    @MustCallSuper
    override fun dispose() {
        child?.dispose()
        super.dispose()
    }
}