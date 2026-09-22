package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.parser.attr.required.RequiredAxisAttrDefine
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.widget.Flex
import com.muedsa.snapshot.widget.Widget

open class FlexParser : WidgetParser {

    override val id: String = "Flex"

    override val containerMode: ContainerMode = ContainerMode.MULTIPLE

    override fun buildWidget(element: Element): Widget = Flex(
        direction = WidgetParser.parseAttrValue(DIRECTION, element.attrs),
        mainAxisAlignment = WidgetParser.parseAttrValue(CommonAttrDefine.MAIN_AXIS_ALIGNMENT, element.attrs),
        mainAxisSize = WidgetParser.parseAttrValue(CommonAttrDefine.MAIN_AXIS_SIZE, element.attrs),
        crossAxisAlignment = WidgetParser.parseAttrValue(CommonAttrDefine.CROSS_AXIS_ALIGNMENT, element.attrs),
        textDirection = WidgetParser.parseAttrValue(TEXT_DIRECTION, element.attrs),
        verticalDirection = WidgetParser.parseAttrValue(CommonAttrDefine.VERTICAL_DIRECTION, element.attrs),
        textBaseline = WidgetParser.parseAttrValue(TEXT_BASELINE, element.attrs),
        clipBehavior = WidgetParser.parseAttrValue(CLIP_BEHAVIOR, element.attrs),
    ).also {
        WidgetParser.createWidgetForChildElement(it, element.children)
    }

    companion object {
        val DIRECTION = RequiredAxisAttrDefine("direction")
        val TEXT_DIRECTION = CommonAttrDefine.DIRECTION.copyWith("textDirection")
        val TEXT_BASELINE = CommonAttrDefine.BASELINE_N.copyWith("textBaseline")
        val CLIP_BEHAVIOR = CommonAttrDefine.CLIP_BEHAVIOR.copyWith(
            name = "clipBehavior",
            defaultValue = ClipBehavior.NONE,
        )
    }
}
