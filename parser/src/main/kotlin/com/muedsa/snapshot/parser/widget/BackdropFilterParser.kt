package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.BlendModeAttrDefine
import com.muedsa.snapshot.widget.BackdropFilter
import com.muedsa.snapshot.widget.Widget
import org.jetbrains.skia.BlendMode

open class BackdropFilterParser : WidgetParser {

    override val id: String = "BackdropFilter"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget =
        BackdropFilter(
            imageFilter = ImageFilteredParser.parseBlurFilter(element),
            blendMode = WidgetParser.parseAttrValue(BLEND_MODE, element.attrs),
        ).also {
            WidgetParser.createWidgetForChildElement(it, element.children)
        }

    companion object {
        val BLEND_MODE = BlendModeAttrDefine("blendMode", BlendMode.SRC_OVER)
    }
}
