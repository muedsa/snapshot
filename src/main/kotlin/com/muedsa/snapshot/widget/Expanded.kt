package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.flex.FlexFit

inline fun Flex.Expanded(
    flex: Int = 1,
    content: Expanded.() -> Unit = {},
) {
    buildChild(
        widget = Expanded(
            flex = flex,
            parent = this
        ),
        content = content
    )
}

class Expanded(
    flex: Int = 1,
    parent: Widget? = null,
) : Flexible(
    flex = flex,
    fit = FlexFit.TIGHT,
    parent = parent
)