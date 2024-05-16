package com.muedsa.snapshot.paint.text

import org.jetbrains.skia.paragraph.*
import org.jetbrains.skia.paragraph.ParagraphBuilder

/**
 * proxy class for ParagraphBuilder
 */
class ParagraphBuilder(
    style: ParagraphStyle? = null,
    fc: FontCollection? = null,
) {
    private val _builder = ParagraphBuilder(style, fc)

    var placeholderCount = 0
        private set

    fun addText(text: String) {
        _builder.addText(text)
    }

    fun pushStyle(style: TextStyle?) {
        _builder.pushStyle(style)
    }

    fun popStyle() {
        _builder.popStyle()
    }

    fun addPlaceholder(style: PlaceholderStyle) {
        _builder.addPlaceholder(style)
        placeholderCount++
    }

    fun build(): Paragraph {
        return _builder.build()
    }
}