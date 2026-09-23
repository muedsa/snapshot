package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderFractionallySizedBox

inline fun ChildSlot.FractionallySizedBox(
    widthFactor: Float? = null,
    heightFactor: Float? = null,
    alignment: BoxAlignment = BoxAlignment.CENTER,
    content: FractionallySizedBox.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.FractionallySizedBox(
            widthFactor = widthFactor,
            heightFactor = heightFactor,
            alignment = alignment,
        ).apply(content)
    )
}

/** 按父约束的最大宽高设置子节点尺寸；未指定比例的方向沿用父约束。 */
class FractionallySizedBox(
    var widthFactor: Float? = null,
    var heightFactor: Float? = null,
    var alignment: BoxAlignment = BoxAlignment.CENTER,
) : SingleChildWidget() {

    init {
        widthFactor?.let { factor ->
            require(factor.isFinite() && factor >= 0f) { "widthFactor must be finite and non-negative" }
        }
        heightFactor?.let { factor ->
            require(factor.isFinite() && factor >= 0f) { "heightFactor must be finite and non-negative" }
        }
    }

    override fun createRenderBox(child: Widget?): RenderBox = RenderFractionallySizedBox(
        widthFactor = widthFactor,
        heightFactor = heightFactor,
        alignment = alignment,
    ).also { box ->
        child?.createRenderBox()?.let(box::appendChild)
    }
}
