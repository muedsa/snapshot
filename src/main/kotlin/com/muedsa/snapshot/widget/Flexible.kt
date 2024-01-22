package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.flex.FlexFit
import com.muedsa.snapshot.rendering.flex.FlexParentData

inline fun Flex.Flexible(
    flex: Int = 1,
    fit: FlexFit = FlexFit.LOOSE,
    content: Flexible.() -> Unit = {},
) {
    buildChild(
        widget = Flexible(
            flex = flex,
            fit = fit,
            parent = this
        ),
        content = content
    )
}

open class Flexible(
    val flex: Int = 1,
    val fit: FlexFit = FlexFit.LOOSE,
    parent: Widget? = null,
) : ParentDataWidget(
    parent = parent
) {
    override fun applyParentData(renderBox: RenderBox) {
        assert(renderBox.parentData is FlexParentData)
        val parentData: FlexParentData = renderBox.parentData as FlexParentData
        if (parentData.flex != flex) {
            parentData.flex = flex
        }
        if (parentData.fit != fit) {
            parentData.fit = fit
        }
    }
}