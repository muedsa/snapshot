package com.muedsa.snapshot.paint.text

import org.jetbrains.skia.paragraph.PlaceholderStyle

abstract class InlineSpan(
    val style: TextStyle?
) {

    var mergedStyle: TextStyle? = null
        private set

    fun updateMergedStyle(parentStyle: TextStyle?) {
        mergedStyle = style ?: parentStyle
        if (parentStyle != null) {
            mergedStyle = mergedStyle?.mergeFrom(parentStyle)
        }
    }

    abstract fun build(builder: ParagraphBuilder, dimensions: List<PlaceholderStyle>? = null)

    abstract fun visitChildren(visitor: (InlineSpan) -> Boolean): Boolean

    abstract fun visitDirectChildren(visitor: (InlineSpan) -> Boolean): Boolean

    abstract fun computeToPlainText(buffer: StringBuffer, includePlaceholders: Boolean = true)
}