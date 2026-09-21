package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.widget.Padding
import com.muedsa.snapshot.widget.Widget

open class PaddingParser : WidgetParser {

    override val id: String = "Padding"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget =
        Padding(
            padding = WidgetParser.parseAttrValue(CommonAttrDefine.PADDING, element.attrs),
        ).also {
            WidgetParser.createWidgetForChildElement(it, element.children)
        }
}
