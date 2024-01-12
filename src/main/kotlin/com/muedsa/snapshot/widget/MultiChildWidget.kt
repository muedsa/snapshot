package com.muedsa.snapshot.widget

abstract class MultiChildWidget(
    val children: Array<out Widget>?
) : Widget() {
    constructor(childrenBuilder: MultiWidgetBuilder?) : this(children = childrenBuilder?.invoke())

    init {
        children?.forEach {
            it.parent = this
        }
    }
}