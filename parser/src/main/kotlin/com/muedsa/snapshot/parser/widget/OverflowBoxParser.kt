package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.widget.OverflowBox
import com.muedsa.snapshot.widget.Widget

open class OverflowBoxParser : WidgetParser {

    override val id: String = "OverflowBox"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget =
        OverflowBox(
            alignment = WidgetParser.parseAttrValue(CommonAttrDefine.ALIGNMENT, element.attrs),
            minWidth = WidgetParser.parseAttrValue(CommonAttrDefine.MIN_WIDTH_N, element.attrs),
            maxWidth = WidgetParser.parseAttrValue(CommonAttrDefine.MAX_WIDTH_N, element.attrs),
            minHeight = WidgetParser.parseAttrValue(CommonAttrDefine.MIN_HEIGHT_N, element.attrs),
            maxHeight = WidgetParser.parseAttrValue(CommonAttrDefine.MAX_HEIGHT_N, element.attrs),
        ).also {
            WidgetParser.createWidgetForChildElement(it, element.children)
        }
}
