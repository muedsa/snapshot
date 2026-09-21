package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.Size
import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.parser.attr.required.RequiredFloatAttrDefine
import com.muedsa.snapshot.widget.SizedOverflowBox
import com.muedsa.snapshot.widget.Widget

open class SizedOverflowBoxParser : WidgetParser {

    override val id: String = "SizedOverflowBox"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget =
        SizedOverflowBox(
            size = Size(
                width = WidgetParser.parseAttrValue(WIDTH, element.attrs),
                height = WidgetParser.parseAttrValue(HEIGHT, element.attrs),
            ),
            alignment = WidgetParser.parseAttrValue(CommonAttrDefine.ALIGNMENT, element.attrs),
        ).also {
            WidgetParser.createWidgetForChildElement(it, element.children)
        }

    companion object {
        val WIDTH = RequiredFloatAttrDefine("width")
        val HEIGHT = RequiredFloatAttrDefine("height")
    }
}
