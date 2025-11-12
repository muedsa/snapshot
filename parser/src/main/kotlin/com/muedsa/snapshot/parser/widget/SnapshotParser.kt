package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.widget.Widget

object SnapshotParser : WidgetParser {

    override val id: String = "Snapshot"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget {
        throw IllegalStateException("Element [$id] $this cant not buildWidget, it can only be the root node")
    }
}