package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.widget.Column
import com.muedsa.snapshot.widget.Widget

open class ColumnParser : WidgetParser {

    override val id: String = "Column"

    override val containerMode: ContainerMode = ContainerMode.MULTIPLE

    val textDirection = CommonAttrDefine.DIRECTION.copyWith("textDirection")
    val textBaseline = CommonAttrDefine.BASELINE_N.copyWith("textBaseline")

    override fun buildWidget(element: Element): Widget = Column(
        mainAxisAlignment = WidgetParser.parseAttrValue(CommonAttrDefine.MAIN_AXIS_ALIGNMENT, element.attrs),
        mainAxisSize = WidgetParser.parseAttrValue(CommonAttrDefine.MAIN_AXIS_SIZE, element.attrs),
        crossAxisAlignment = WidgetParser.parseAttrValue(CommonAttrDefine.CROSS_AXIS_ALIGNMENT, element.attrs),
        textDirection = WidgetParser.parseAttrValue(textDirection, element.attrs),
        verticalDirection = WidgetParser.parseAttrValue(CommonAttrDefine.VERTICAL_DIRECTION, element.attrs),
        textBaseline = WidgetParser.parseAttrValue(textBaseline, element.attrs)
    )
}