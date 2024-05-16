package com.muedsa.snapshot.widget.text

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.widget.ParentDataWidget

class WidgetSpanParentDataWidget(
    val span: WidgetSpan
) : ParentDataWidget() {
    override fun applyParentData(renderBox: RenderBox) {
        assert(renderBox.parentData is TextParentData)
        val textParentData = renderBox.parentData as TextParentData
        textParentData.span = span
    }
}