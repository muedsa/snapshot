package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.PlaceholderAlignmentAttrDefine
import com.muedsa.snapshot.widget.Widget
import org.jetbrains.skia.paragraph.PlaceholderAlignment

class WidgetSpanParser : WidgetParser {

    override val id: String = "WidgetSpan"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget {
        throw IllegalStateException("Element [$id] $this cant not buildWidget, it can only be used in the Text")
    }

    companion object {
        val ATTR_ALIGNMENT = PlaceholderAlignmentAttrDefine(
            name = "alignment",
            defaultValue = PlaceholderAlignment.BOTTOM,
        )
    }
}
