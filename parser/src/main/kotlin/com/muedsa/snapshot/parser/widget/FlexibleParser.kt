package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.widget.Flexible
import com.muedsa.snapshot.widget.Widget

open class FlexibleParser : WidgetParser {

    override val id: String = "Flexible"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget {
        requireFlexParent(element, id)
        return Flexible(
            flex = WidgetParser.parseAttrValue(CommonAttrDefine.FLEX, element.attrs),
            fit = WidgetParser.parseAttrValue(CommonAttrDefine.FLEX_FIT, element.attrs),
        ).also {
            WidgetParser.createWidgetForChildElement(it, element.children)
        }
    }
}

internal fun requireFlexParent(element: Element, tagId: String) {
    val parentParser = element.parent?.widgetParser
    require(parentParser is RowParser || parentParser is ColumnParser) {
        "Tag [$tagId] must be a direct child of Row or Column"
    }
}
