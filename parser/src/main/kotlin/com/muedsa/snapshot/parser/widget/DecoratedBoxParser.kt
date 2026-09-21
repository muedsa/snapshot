package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.DecorationPositionAttrDefine
import com.muedsa.snapshot.widget.DecoratedBox
import com.muedsa.snapshot.widget.Widget

open class DecoratedBoxParser : WidgetParser {

    override val id: String = "DecoratedBox"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    val position = DecorationPositionAttrDefine("position")

    override fun buildWidget(element: Element): Widget = DecoratedBox(
        decoration = BorderParser.parseBorderDecoration(element),
        position = WidgetParser.parseAttrValue(position, element.attrs),
    ).also {
        WidgetParser.createWidgetForChildElement(it, element.children)
    }
}
