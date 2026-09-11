package com.muedsa.snapshot.widget.text

import com.muedsa.snapshot.paint.text.InlineSpan
import com.muedsa.snapshot.paint.text.PlaceholderSpan
import com.muedsa.snapshot.rendering.box.ContainerBoxParentData

class TextParentData(
    var span: PlaceholderSpan? = null,
    /** 所在 RichText 的根 span。由 [RichText] 在建 render box 前注入,emoji 靠它回推继承字号。 */
    var rootSpan: InlineSpan? = null,
) : ContainerBoxParentData()
