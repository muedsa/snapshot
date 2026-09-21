package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.widget.Center
import com.muedsa.snapshot.widget.Widget

open class CenterParser : WidgetParser {

    override val id: String = "Center"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget =
        Center(
            widthFactor = WidgetParser.parseAttrValue(CommonAttrDefine.WIDTH_FACTOR_N, element.attrs),
            heightFactor = WidgetParser.parseAttrValue(CommonAttrDefine.HEIGHT_FACTOR_N, element.attrs),
        ).also {
            WidgetParser.createWidgetForChildElement(it, element.children)
        }
}
