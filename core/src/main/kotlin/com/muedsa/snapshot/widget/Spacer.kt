package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.flex.FlexFit
import com.muedsa.snapshot.rendering.flex.FlexParentData

/** 在 Flex 主轴上按 [flex] 权重占用剩余空间，不绘制内容。 */
class Spacer(val flex: Int = 1) : ParentDataWidget() {

    init {
        require(flex > 0) { "flex must be positive" }
        attach(SizedBox.shrink())
    }

    override fun applyParentData(renderBox: RenderBox) {
        val parentData = renderBox.parentData as? FlexParentData
            ?: throw IllegalArgumentException("Spacer must be a direct child of Flex, Row or Column")
        parentData.flex = flex
        parentData.fit = FlexFit.TIGHT
    }
}

fun Flex.Spacer(flex: Int = 1) {
    attach(com.muedsa.snapshot.widget.Spacer(flex))
}
