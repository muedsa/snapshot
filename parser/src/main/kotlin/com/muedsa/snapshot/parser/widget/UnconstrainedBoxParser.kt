package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.paint.Axis
import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableEnumAttrDefine
import com.muedsa.snapshot.widget.UnconstrainedBox
import com.muedsa.snapshot.widget.Widget

class UnconstrainedBoxParser : WidgetParser {

    override val id: String = "UnconstrainedBox"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    val constrainedAxis = NullableEnumAttrDefine("constrainedAxis", Axis::valueOf)

    override fun buildWidget(element: Element): Widget = UnconstrainedBox(
        constrainedAxis = WidgetParser.parseAttrValue(constrainedAxis, element.attrs),
        alignment = WidgetParser.parseAttrValue(CommonAttrDefine.ALIGNMENT, element.attrs),
    ).also {
        WidgetParser.createWidgetForChildElement(it, element.children)
    }
}
