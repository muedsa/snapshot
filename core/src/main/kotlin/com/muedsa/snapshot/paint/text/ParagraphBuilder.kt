package com.muedsa.snapshot.paint.text

import org.jetbrains.skia.paragraph.*
import org.jetbrains.skia.paragraph.ParagraphBuilder

/**
 * 对 Skia [ParagraphBuilder] 的轻量代理，并额外记录占位符数量。
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
        _builder.pushStyle(style?.toSkikoTextStyle())
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
