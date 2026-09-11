package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.flex.FlexFit

inline fun Flex.Expanded(
    flex: Int = 1,
    content: Expanded.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.Expanded(
            flex = flex,
        ).apply(content)
    )
}

class Expanded(
    flex: Int = 1,
) : Flexible(
    flex = flex,
    fit = FlexFit.TIGHT,
)
