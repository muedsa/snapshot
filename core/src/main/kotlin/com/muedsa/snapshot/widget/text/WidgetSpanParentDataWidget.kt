package com.muedsa.snapshot.widget.text

import com.muedsa.snapshot.paint.text.InlineSpan
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.widget.ParentDataWidget

class WidgetSpanParentDataWidget(
    val span: WidgetSpan
) : ParentDataWidget() {

    /** 由 [RichText] 在建 render box 之前注入(InlineSpan 没有上行指针,无法反查)。 */
    var rootSpan: InlineSpan? = null

    override fun applyParentData(renderBox: RenderBox) {
        val textParentData = renderBox.parentData as? TextParentData
            ?: error(
                "${renderBox::class.simpleName} requires a TextParentData " +
                    "to apply ${span::class.simpleName}"
            )
        textParentData.span = span
        textParentData.rootSpan = rootSpan
    }
}
