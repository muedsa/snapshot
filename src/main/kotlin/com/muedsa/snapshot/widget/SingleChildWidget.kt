package com.muedsa.snapshot.widget

abstract class SingleChildWidget(
    val child: Widget?,
) : Widget() {

    constructor(childBuilder: SingleWidgetBuilder?) : this(child = childBuilder?.invoke())

    init {
        child?.let {
            it.parent = this
        }
    }
}