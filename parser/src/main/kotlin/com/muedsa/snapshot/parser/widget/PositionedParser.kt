package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.widget.Positioned
import com.muedsa.snapshot.widget.Widget

open class PositionedParser : WidgetParser {

    override val id: String = "Positioned"

    override val containerMode: ContainerMode = ContainerMode.SINGLE
    override fun buildWidget(element: Element): Widget =
        Positioned(
            width = WidgetParser.parseAttrValue(CommonAttrDefine.WIDTH_N, element.attrs),
            height = WidgetParser.parseAttrValue(CommonAttrDefine.HEIGHT_N, element.attrs),
            left = WidgetParser.parseAttrValue(CommonAttrDefine.LEFT_N, element.attrs),
            top = WidgetParser.parseAttrValue(CommonAttrDefine.TOP_N, element.attrs),
            right = WidgetParser.parseAttrValue(CommonAttrDefine.RIGHT_N, element.attrs),
            bottom = WidgetParser.parseAttrValue(CommonAttrDefine.BOTTOM_N, element.attrs),
        ).also {
            WidgetParser.createWidgetForChildElement(it, element.children)
        }
}