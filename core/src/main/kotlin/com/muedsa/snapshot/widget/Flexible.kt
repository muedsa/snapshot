package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.flex.FlexFit
import com.muedsa.snapshot.rendering.flex.FlexParentData

inline fun Flex.Flexible(
    flex: Int = 1,
    fit: FlexFit = FlexFit.LOOSE,
    content: Flexible.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.Flexible(
            flex = flex,
            fit = fit,
        ).apply(content)
    )
}

open class Flexible(
    var flex: Int = 1,
    var fit: FlexFit = FlexFit.LOOSE,
) : ParentDataWidget(
) {
    override fun applyParentData(renderBox: RenderBox) {
        require(renderBox.parentData is FlexParentData) { "renderBox.parentData must be FlexParentData" }
        val parentData: FlexParentData = renderBox.parentData as FlexParentData
        if (parentData.flex != flex) {
            parentData.flex = flex
        }
        if (parentData.fit != fit) {
            parentData.fit = fit
        }
    }
}
