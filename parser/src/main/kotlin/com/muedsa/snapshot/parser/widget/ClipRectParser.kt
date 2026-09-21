package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.ClipBehaviorAttrDefine
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.widget.ClipRect
import com.muedsa.snapshot.widget.Widget

open class ClipRectParser : WidgetParser {

    override val id: String = "ClipRect"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget =
        ClipRect(
            clipBehavior = WidgetParser.parseAttrValue(CLIP_BEHAVIOR, element.attrs),
        ).also {
            WidgetParser.createWidgetForChildElement(it, element.children)
        }

    companion object {
        val CLIP_BEHAVIOR = ClipBehaviorAttrDefine("clipBehavior", ClipBehavior.HARD_EDGE)
    }
}
