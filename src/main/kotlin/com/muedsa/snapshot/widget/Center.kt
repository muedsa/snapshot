package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment

class Center(
    widthFactor: Float? = null,
    heightFactor: Float? = null,
    childBuilder: SingleWidgetBuilder? = null,
) : Align(
    alignment = BoxAlignment.CENTER,
    widthFactor = widthFactor,
    heightFactor = heightFactor,
    childBuilder = childBuilder
)