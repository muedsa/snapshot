package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.required.RequiredFloatAttrDefine
import com.muedsa.snapshot.widget.AspectRatio
import com.muedsa.snapshot.widget.Widget

class AspectRatioParser : WidgetParser {

    override val id: String = "AspectRatio"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    val aspectRatio = RequiredFloatAttrDefine("aspectRatio")

    override fun buildWidget(element: Element): Widget {
        val ratio = WidgetParser.parseAttrValue(aspectRatio, element.attrs)
        require(ratio.isFinite() && ratio > 0f) { "Attr [aspectRatio] must be finite and positive" }
        return AspectRatio(ratio).also {
            WidgetParser.createWidgetForChildElement(it, element.children)
        }
    }
}
