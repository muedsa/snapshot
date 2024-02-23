package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.widget.CachedNetworkImage
import com.muedsa.snapshot.widget.Widget

open class ImageParser : WidgetParser {

    override val id: String = "Image"

    override val containerMode: ContainerMode = ContainerMode.NONE

    val colorBlendMode = CommonAttrDefine.BLEND_MODE_N.copyWith("colorBlendMode")

    override fun buildWidget(element: Element): Widget = CachedNetworkImage(
        url = WidgetParser.parseAttrValue(CommonAttrDefine.URL, element.attrs),
        width = WidgetParser.parseAttrValue(CommonAttrDefine.WIDTH_N, element.attrs),
        height = WidgetParser.parseAttrValue(CommonAttrDefine.WIDTH_N, element.attrs),
        fit = WidgetParser.parseAttrValue(CommonAttrDefine.FIT_N, element.attrs),
        alignment = WidgetParser.parseAttrValue(CommonAttrDefine.ALIGNMENT, element.attrs),
        repeat = WidgetParser.parseAttrValue(CommonAttrDefine.REPEAT, element.attrs),
        scale = WidgetParser.parseAttrValue(CommonAttrDefine.SCALE, element.attrs),
        opacity = WidgetParser.parseAttrValue(CommonAttrDefine.OPACITY, element.attrs),
        color = WidgetParser.parseAttrValue(CommonAttrDefine.COLOR_N, element.attrs),
        colorBlendMode = WidgetParser.parseAttrValue(colorBlendMode, element.attrs),
        cache = element.owner!!.getNetworkImageCache()
    )
}