package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.widget.Spacer
import com.muedsa.snapshot.widget.Widget

class SpacerParser : WidgetParser {

    override val id: String = "Spacer"

    override val containerMode: ContainerMode = ContainerMode.NONE

    override fun buildWidget(element: Element): Widget {
        requireFlexParent(element, id)
        require(!element.attrs.containsKey(CommonAttrDefine.FLEX_FIT.name)) {
            "Tag [$id] does not support attr [${CommonAttrDefine.FLEX_FIT.name}]"
        }
        return Spacer(flex = WidgetParser.parseAttrValue(CommonAttrDefine.FLEX, element.attrs))
    }
}
