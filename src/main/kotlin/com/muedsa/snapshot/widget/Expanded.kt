package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.flex.FlexFit

class Expanded(
    flex: Int = 1,
    childBuilder: SingleWidgetBuilder,
) : Flexible(
    flex = flex,
    fit = FlexFit.TIGHT,
    childBuilder = childBuilder
)