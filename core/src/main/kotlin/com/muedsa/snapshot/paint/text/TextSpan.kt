package com.muedsa.snapshot.paint.text

import org.jetbrains.skia.paragraph.PlaceholderStyle
import org.jetbrains.skia.paragraph.TextStyle

class TextSpan(
    val text: String? = null,
    style: TextStyle? = null,
    initChildren: List<InlineSpan>? = null
) : InlineSpan(style) {

    val children: MutableList<InlineSpan> = mutableListOf()

    override fun build(builder: ParagraphBuilder, dimensions: List<PlaceholderStyle>?) {
        val hasStyle = style != null
        if (hasStyle) {
            builder.pushStyle(style)
        }
        text?.let {
            builder.addText(text)
        }
        children.forEach { it.build(builder, dimensions) }
        if (hasStyle) {
            builder.popStyle()
        }
    }

    override fun visitChildren(visitor: (InlineSpan) -> Boolean): Boolean {
        if (text != null && !visitor(this)) {
            return false
        }
        if (children.isEmpty()) {
            return true
        }
        return !children.any { !it.visitChildren(visitor) }
    }

    override fun visitDirectChildren(visitor: (InlineSpan) -> Boolean): Boolean {
        if (children.isEmpty()) {
            return true
        }
        return !children.any { !it.visitChildren(visitor) }
    }

    override fun computeToPlainText(buffer: StringBuffer, includePlaceholders: Boolean) {
        text?.let { buffer.append(it) }
        children.forEach { it.computeToPlainText(buffer, includePlaceholders) }
    }

    init {
        initChildren?.let {
            this.children.addAll(it)
        }
    }
}

fun TextSpan.TextSpan(
    text: String,
    style: TextStyle? = null,
) {
    children.add(TextSpan(text = text, style = style, initChildren = null))
}

fun TextSpan.TextSpan(
    style: TextStyle? = null,
    content: TextSpan.() -> Unit
) {
    children.add(TextSpan(style = style, initChildren = null).also(content))
}