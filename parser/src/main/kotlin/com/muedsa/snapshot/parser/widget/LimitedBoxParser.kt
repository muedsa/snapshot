package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.widget.LimitedBox
import com.muedsa.snapshot.widget.Widget

open class LimitedBoxParser : WidgetParser {

    override val id: String = "LimitedBox"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget =
        LimitedBox(
            maxWidth = WidgetParser.parseAttrValue(CommonAttrDefine.MAX_WIDTH, element.attrs),
            maxHeight = WidgetParser.parseAttrValue(CommonAttrDefine.MAX_HEIGHT, element.attrs),
        ).also {
            WidgetParser.createWidgetForChildElement(it, element.children)
        }
}
