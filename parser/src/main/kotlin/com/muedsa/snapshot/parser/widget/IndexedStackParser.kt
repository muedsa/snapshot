package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.nullable.NullableIntAttrDefine
import com.muedsa.snapshot.widget.IndexedStack
import com.muedsa.snapshot.widget.Widget

class IndexedStackParser : StackParser() {

    override val id: String = "IndexedStack"

    val index = NullableIntAttrDefine("index")

    override fun buildWidget(element: Element): Widget = IndexedStack(
        index = if (element.attrs.containsKey(index.name)) {
            WidgetParser.parseAttrValue(index, element.attrs)
        } else {
            0
        },
        alignment = WidgetParser.parseAttrValue(alignment, element.attrs),
        textDirection = WidgetParser.parseAttrValue(textDirection, element.attrs),
        fit = WidgetParser.parseAttrValue(fit, element.attrs),
        clipBehavior = WidgetParser.parseAttrValue(clipBehavior, element.attrs),
    ).also {
        WidgetParser.createWidgetForChildElement(it, element.children)
    }
}
