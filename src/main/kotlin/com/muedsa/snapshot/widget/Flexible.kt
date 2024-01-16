package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.flex.FlexFit
import com.muedsa.snapshot.rendering.flex.FlexParentData

open class Flexible(
    val flex: Int = 1,
    val fit: FlexFit = FlexFit.LOOSE,
    childBuilder: SingleWidgetBuilder,
) : ParentDataWidget(
    child = childBuilder.invoke()!!
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