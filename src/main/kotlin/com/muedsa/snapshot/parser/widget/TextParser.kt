package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.widget.SimpleText
import com.muedsa.snapshot.widget.Widget

open class TextParser : WidgetParser {

    override val id: String = "Text"

    override val containerMode: ContainerMode = ContainerMode.NONE

    @OptIn(ExperimentalStdlibApi::class)
    override fun buildWidget(element: Element): Widget =
        SimpleText(
            text = WidgetParser.parseAttrValue(CommonAttrDefine.TEXT, element.attrs),
            color = WidgetParser.parseAttrValue(CommonAttrDefine.COLOR, element.attrs),
            fontSize = WidgetParser.parseAttrValue(CommonAttrDefine.FONT_SIZE, element.attrs),
            fontFamilyName = WidgetParser.parseAttrValue(CommonAttrDefine.FONT_FAMILY_N, element.attrs)?.split(",")
                ?.toTypedArray(),
            fontStyle = WidgetParser.parseAttrValue(CommonAttrDefine.FONT_STYLE, element.attrs)
        )
}