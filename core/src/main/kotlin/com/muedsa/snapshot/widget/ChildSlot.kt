package com.muedsa.snapshot.widget

/**
 * 拥有子节点槽位的 [Widget]。
 *
 * DSL 函数([Padding]、[Row]、[Stack] 等)以此为接收者,因此"在不具备子槽位的 Widget 上
 * 挂子节点"会成为编译错误,而不是运行时异常。
 *
 * [attach] 是全项目唯一的挂载入口:它把 [child] 放进本节点的子槽位。它允许重新挂载,
 * 因为 [Container] 的 `composeWidget()` 会把已挂载的子节点重新挂到新建的包装节点上。
 */
interface ChildSlot {

    /** 把 [child] 挂到本节点的子槽位。重复挂载抛 [IllegalStateException]。 */
    fun attach(child: Widget)
}
