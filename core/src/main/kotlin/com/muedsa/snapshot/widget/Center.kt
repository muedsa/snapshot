package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment

inline fun ChildSlot.Center(
    widthFactor: Float? = null,
    heightFactor: Float? = null,
    content: Center.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.Center(
            widthFactor = widthFactor,
            heightFactor = heightFactor,
        ).apply(content)
    )
}

class Center(
    widthFactor: Float? = null,
    heightFactor: Float? = null,
) : Align(
    alignment = BoxAlignment.CENTER,
    widthFactor = widthFactor,
    heightFactor = heightFactor,
)
