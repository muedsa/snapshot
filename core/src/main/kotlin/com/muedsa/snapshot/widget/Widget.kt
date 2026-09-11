package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox

/**
 * 标记 snapshot 的 Widget DSL 作用域。
 *
 * `@DslMarker` 会遮蔽同属一个 DSL 的外层隐式接收者:最近的接收者胜出;若某次调用只能
 * 绑定到被遮蔽的外层接收者,则报编译错误。因此 `Stack { Row { Positioned(…) } }` 这类
 * "代码嵌套结构与实际挂载父节点不一致"的写法不再静默通过。
 */
@DslMarker
annotation class SnapshotWidgetDsl

inline fun <T : Widget> Widget.buildChild(
    widget: T,
    content: T.() -> Unit,
) {
    when (this) {
        is ProxyWidget -> this.widget = widget
        is SingleChildWidget -> this.child = widget
        is MultiChildWidget -> this.appendChild(widget)
        else -> throw IllegalStateException()
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
            else -> throw IllegalStateException()
        }
        it.parent = this
    }
    return this
}

@SnapshotWidgetDsl
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