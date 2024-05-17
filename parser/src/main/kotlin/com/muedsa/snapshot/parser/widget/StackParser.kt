package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.widget.Stack
import com.muedsa.snapshot.widget.Widget

open class StackParser : WidgetParser {

    override val id: String = "Stack"

    override val containerMode: ContainerMode = ContainerMode.MULTIPLE

    val alignment = CommonAttrDefine.ALIGNMENT.copyWith(name = "alignment", defaultValue = BoxAlignment.TOP_LEFT)
    val textDirection = CommonAttrDefine.DIRECTION.copyWith("textDirection")

    override fun buildWidget(element: Element): Widget = Stack(
        alignment = WidgetParser.parseAttrValue(alignment, element.attrs),
        textDirection = WidgetParser.parseAttrValue(textDirection, element.attrs)
    ).also {
        WidgetParser.createWidgetForChildElement(it, element.children)
    }
}