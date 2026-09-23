package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.widget.FractionallySizedBox
import com.muedsa.snapshot.widget.Widget

class FractionallySizedBoxParser : WidgetParser {

    override val id: String = "FractionallySizedBox"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget = FractionallySizedBox(
        widthFactor = WidgetParser.parseAttrValue(CommonAttrDefine.WIDTH_FACTOR_N, element.attrs),
        heightFactor = WidgetParser.parseAttrValue(CommonAttrDefine.HEIGHT_FACTOR_N, element.attrs),
        alignment = WidgetParser.parseAttrValue(CommonAttrDefine.ALIGNMENT, element.attrs),
    ).also {
        WidgetParser.createWidgetForChildElement(it, element.children)
    }
}
