package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.widget.Expanded
import com.muedsa.snapshot.widget.Widget

open class ExpandedParser : WidgetParser {

    override val id: String = "Expanded"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget {
        requireFlexParent(element, id)
        require(!element.attrs.containsKey(CommonAttrDefine.FLEX_FIT.name)) {
            "Tag [$id] does not support attr [${CommonAttrDefine.FLEX_FIT.name}]"
        }
        return Expanded(
            flex = WidgetParser.parseAttrValue(CommonAttrDefine.FLEX, element.attrs),
        ).also {
            WidgetParser.createWidgetForChildElement(it, element.children)
        }
    }
}
