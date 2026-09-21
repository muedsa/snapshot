package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.widget.ClipRRect
import com.muedsa.snapshot.widget.Widget

open class ClipRRectParser : WidgetParser {

    override val id: String = "ClipRRect"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget =
        ClipRRect(
            borderRadius = BorderParser.parseBorderRadius(element),
            clipBehavior = WidgetParser.parseAttrValue(CommonAttrDefine.CLIP_BEHAVIOR, element.attrs),
        ).also {
            WidgetParser.createWidgetForChildElement(it, element.children)
        }
}
