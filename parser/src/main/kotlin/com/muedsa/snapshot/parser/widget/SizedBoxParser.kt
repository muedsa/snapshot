package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.widget.SizedBox
import com.muedsa.snapshot.widget.Widget

open class SizedBoxParser : WidgetParser {

    override val id: String = "SizedBox"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget =
        SizedBox(
            width = WidgetParser.parseAttrValue(CommonAttrDefine.WIDTH_N, element.attrs),
            height = WidgetParser.parseAttrValue(CommonAttrDefine.HEIGHT_N, element.attrs),
        ).also {
            WidgetParser.createWidgetForChildElement(it, element.children)
        }
}
