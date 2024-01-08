package com.muedsa.snapshot.widget

import com.muedsa.snapshot.annotation.MustCallSuper

abstract class MultiChildWidget(
    val children: Array<out Widget>?
) : Widget() {
    constructor(childrenBuilder: MultiWidgetBuilder?) : this(children = childrenBuilder?.invoke())

    init {
        children?.forEach {
            it.parent = this
        }
    }

    @MustCallSuper
    override fun dispose() {
        children?.forEach { it.dispose() }
        super.dispose()
    }
}