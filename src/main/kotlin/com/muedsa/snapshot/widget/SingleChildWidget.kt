package com.muedsa.snapshot.widget

import com.muedsa.snapshot.annotation.MustCallSuper
import com.muedsa.snapshot.rendering.box.RenderBox

abstract class SingleChildWidget(

    val child: Widget?
) : Widget() {

    constructor(childBuilder: SingleWidgetBuilder?) : this(child = childBuilder?.invoke())

    init {
        child?.let {
            it.parent = this
        }
    }

    @MustCallSuper
    override fun dispose() {
        child?.dispose()
        super.dispose()
    }
}