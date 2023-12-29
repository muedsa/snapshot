package com.muedsa.snapshot.widget

import com.muedsa.geometry.Alignment

class Center(
    widthFactor: Float? = null,
    heightFactor: Float? = null,
    childBuilder: SingleWidgetBuilder? = null
) : Align(
    alignment = Alignment.CENTER,
    widthFactor = widthFactor,
    heightFactor = heightFactor,
    childBuilder = childBuilder
)