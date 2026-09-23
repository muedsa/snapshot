package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.paint.Axis
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderUnconstrainedBox

inline fun ChildSlot.UnconstrainedBox(
    constrainedAxis: Axis? = null,
    alignment: BoxAlignment = BoxAlignment.CENTER,
    content: UnconstrainedBox.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.UnconstrainedBox(
            constrainedAxis = constrainedAxis,
            alignment = alignment,
        ).apply(content)
    )
}

/** 子节点可摆脱父约束，组件自身仍受父约束限制。 */
class UnconstrainedBox(
    var constrainedAxis: Axis? = null,
    var alignment: BoxAlignment = BoxAlignment.CENTER,
) : SingleChildWidget() {

    override fun createRenderBox(child: Widget?): RenderBox = RenderUnconstrainedBox(
        constrainedAxis = constrainedAxis,
        alignment = alignment,
    ).also { box ->
        child?.createRenderBox()?.let(box::appendChild)
    }
}
