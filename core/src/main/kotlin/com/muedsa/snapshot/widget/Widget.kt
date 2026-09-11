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
    val slot = this as? ChildSlot
        ?: throw IllegalStateException(
            "${this::class.simpleName} has no child slot, can not attach ${widget::class.simpleName}"
        )
    slot.attach(widget)
    widget.content()
}

fun Widget.bind(
    child: Widget?,
): Widget {
    child?.let {
        val slot = this as? ChildSlot
            ?: throw IllegalStateException(
                "${this::class.simpleName} has no child slot, can not attach ${it::class.simpleName}"
            )
        slot.attach(it)
    }
    return this
}

@SnapshotWidgetDsl
abstract class Widget(
    parent: Widget? = null,
) {
    var parent: Widget? = null
        internal set

    abstract fun createRenderBox(): RenderBox

    init {
        this.parent = parent
    }
}
