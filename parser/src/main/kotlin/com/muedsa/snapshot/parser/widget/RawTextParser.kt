package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.widget.Widget

class RawTextParser : WidgetParser {

    override val id: String = "Raw"

    override val containerMode: ContainerMode = ContainerMode.MULTIPLE

    override fun buildWidget(element: Element): Widget {
        throw IllegalCallerException("Element [$id] $this cant not buildWidget, it can only be used in the Text")
    }

}