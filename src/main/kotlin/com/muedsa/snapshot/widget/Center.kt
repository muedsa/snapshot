package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment

inline fun Widget.Center(
    widthFactor: Float? = null,
    heightFactor: Float? = null,
    content: Center.() -> Unit = {},
) {
    buildChild(
        widget = Center(
            widthFactor = widthFactor,
            heightFactor = heightFactor,
            parent = this
        ),
        content = content
    )
}

class Center(
    widthFactor: Float? = null,
    heightFactor: Float? = null,
    parent: Widget? = null,
) : Align(
    alignment = BoxAlignment.CENTER,
    widthFactor = widthFactor,
    heightFactor = heightFactor,
    parent = parent
)