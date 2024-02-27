package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.Widget

open class ContainerParser : WidgetParser {

    override val id: String = "Container"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget {
        val constraints: BoxConstraints? = if (
            element.attrs.containsKey(CommonAttrDefine.MIN_WIDTH.name)
            || element.attrs.containsKey(CommonAttrDefine.MAX_WIDTH.name)
            || element.attrs.containsKey(CommonAttrDefine.MIN_HEIGHT.name)
            || element.attrs.containsKey(CommonAttrDefine.MAX_HEIGHT.name)
        ) {
            BoxConstraints(
                minWidth = WidgetParser.parseAttrValue(CommonAttrDefine.MIN_WIDTH, element.attrs),
                maxWidth = WidgetParser.parseAttrValue(CommonAttrDefine.MAX_WIDTH, element.attrs),
                minHeight = WidgetParser.parseAttrValue(CommonAttrDefine.MIN_HEIGHT, element.attrs),
                maxHeight = WidgetParser.parseAttrValue(CommonAttrDefine.MAX_HEIGHT, element.attrs)
            )
        } else null
        val borderDecoration = BorderParser.parseBorderDecoration(element)
        return Container(
            alignment = WidgetParser.parseAttrValue(CommonAttrDefine.ALIGNMENT_N, element.attrs),
            padding = WidgetParser.parseAttrValue(CommonAttrDefine.PADDING_N, element.attrs),
            color = WidgetParser.parseAttrValue(CommonAttrDefine.COLOR_N, element.attrs),
            width = WidgetParser.parseAttrValue(CommonAttrDefine.WIDTH_N, element.attrs),
            height = WidgetParser.parseAttrValue(CommonAttrDefine.HEIGHT_N, element.attrs),
            constraints = constraints,
            margin = WidgetParser.parseAttrValue(CommonAttrDefine.MARGIN_N, element.attrs),
            decoration = if (BorderParser.isNullBorder(borderDecoration)) null else borderDecoration
        )
    }
}