package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.widget.ClipOval
import com.muedsa.snapshot.widget.Widget

open class ClipOvalParser : WidgetParser {

    override val id: String = "ClipOval"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget =
        ClipOval(
            clipBehavior = WidgetParser.parseAttrValue(CommonAttrDefine.CLIP_BEHAVIOR, element.attrs),
        ).also {
            WidgetParser.createWidgetForChildElement(it, element.children)
        }
}
