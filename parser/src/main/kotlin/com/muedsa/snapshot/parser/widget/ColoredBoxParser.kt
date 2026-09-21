package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.widget.ColoredBox
import com.muedsa.snapshot.widget.Widget

open class ColoredBoxParser : WidgetParser {

    override val id: String = "ColoredBox"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget {
        val color = requireNotNull(
            WidgetParser.parseAttrValue(CommonAttrDefine.COLOR_N, element.attrs)
        ) { "Attr [color] must not be null" }
        return ColoredBox(color = color).also {
            WidgetParser.createWidgetForChildElement(it, element.children)
        }
    }
}
