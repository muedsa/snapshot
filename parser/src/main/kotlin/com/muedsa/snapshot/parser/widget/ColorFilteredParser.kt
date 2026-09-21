package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.widget.ColorFiltered
import com.muedsa.snapshot.widget.Widget
import org.jetbrains.skia.ColorFilter

open class ColorFilteredParser : WidgetParser {

    override val id: String = "ColorFiltered"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget {
        val color = requireNotNull(
            WidgetParser.parseAttrValue(CommonAttrDefine.COLOR_N, element.attrs)
        ) { "Attr [color] must not be null" }
        val blendMode = requireNotNull(
            WidgetParser.parseAttrValue(CommonAttrDefine.BLEND_MODE_N, element.attrs)
        ) { "Attr [blendMode] must not be null" }
        return ColorFiltered(
            colorFilter = ColorFilter.makeBlend(color, blendMode),
        ).also {
            WidgetParser.createWidgetForChildElement(it, element.children)
        }
    }
}
