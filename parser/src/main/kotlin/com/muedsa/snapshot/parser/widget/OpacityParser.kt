package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.widget.Opacity
import com.muedsa.snapshot.widget.Widget

open class OpacityParser : WidgetParser {

    override val id: String = "Opacity"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget =
        Opacity(
            opacity = WidgetParser.parseAttrValue(CommonAttrDefine.OPACITY, element.attrs),
        ).also {
            WidgetParser.createWidgetForChildElement(it, element.children)
        }
}
