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

@SnapshotWidgetDsl
abstract class Widget {
    abstract fun createRenderBox(): RenderBox
}
