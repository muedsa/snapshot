package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.widget.ConstrainedBox
import com.muedsa.snapshot.widget.Widget

open class ConstrainedBoxParser : WidgetParser {

    override val id: String = "ConstrainedBox"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget =
        ConstrainedBox(
            constraints = BoxConstraints(
                minWidth = WidgetParser.parseAttrValue(CommonAttrDefine.MIN_WIDTH, element.attrs),
                maxWidth = WidgetParser.parseAttrValue(CommonAttrDefine.MAX_WIDTH, element.attrs),
                minHeight = WidgetParser.parseAttrValue(CommonAttrDefine.MIN_HEIGHT, element.attrs),
                maxHeight = WidgetParser.parseAttrValue(CommonAttrDefine.MAX_HEIGHT, element.attrs),
            ),
        ).also {
            WidgetParser.createWidgetForChildElement(it, element.children)
        }
}
