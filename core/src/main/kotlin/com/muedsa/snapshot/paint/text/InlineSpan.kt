package com.muedsa.snapshot.paint.text

import org.jetbrains.skia.paragraph.PlaceholderStyle
import org.jetbrains.skia.paragraph.TextStyle

abstract class InlineSpan(
    val style: TextStyle?
) {
    abstract fun build(builder: ParagraphBuilder, dimensions: List<PlaceholderStyle>? = null)

    abstract fun visitChildren(visitor: (InlineSpan) -> Boolean): Boolean

    abstract fun visitDirectChildren(visitor: (InlineSpan) -> Boolean): Boolean

    abstract fun computeToPlainText(buffer: StringBuffer, includePlaceholders: Boolean = true)
}