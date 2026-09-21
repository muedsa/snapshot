package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.widget.Align
import com.muedsa.snapshot.widget.Widget

open class AlignParser : WidgetParser {

    override val id: String = "Align"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget =
        Align(
            alignment = WidgetParser.parseAttrValue(CommonAttrDefine.ALIGNMENT, element.attrs),
            widthFactor = WidgetParser.parseAttrValue(CommonAttrDefine.WIDTH_FACTOR_N, element.attrs),
            heightFactor = WidgetParser.parseAttrValue(CommonAttrDefine.HEIGHT_FACTOR_N, element.attrs),
        ).also {
            WidgetParser.createWidgetForChildElement(it, element.children)
        }
}
