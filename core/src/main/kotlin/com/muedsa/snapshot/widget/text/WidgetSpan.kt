package com.muedsa.snapshot.widget.text

import com.muedsa.snapshot.kDefaultFontSize
import com.muedsa.snapshot.paint.text.InlineSpan
import com.muedsa.snapshot.paint.text.ParagraphBuilder
import com.muedsa.snapshot.paint.text.PlaceholderSpan
import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.widget.ProxyWidget
import com.muedsa.snapshot.widget.Widget
import com.muedsa.snapshot.widget.bind
import org.jetbrains.skia.paragraph.BaselineMode
import org.jetbrains.skia.paragraph.PlaceholderAlignment
import org.jetbrains.skia.paragraph.PlaceholderStyle
import org.jetbrains.skia.paragraph.TextStyle

inline fun TextSpan.WidgetSpan(
    alignment: PlaceholderAlignment = PlaceholderAlignment.BOTTOM,
    baseline: BaselineMode? = null,
    style: TextStyle? = null,
    crossinline content: Widget.() -> Unit,
) {
    this.children.add(
        WidgetSpan(
            alignment = alignment,
            baseline = baseline,
            style = style,
            child = ProxyWidget.buildWidget{
                content()
            }
        )
    )
}

class WidgetSpan(
    alignment: PlaceholderAlignment = PlaceholderAlignment.BOTTOM,
    baseline: BaselineMode? = null,
    style: TextStyle? = null,
    val child: Widget,
) : PlaceholderSpan(
    alignment = alignment,
    baseline = baseline,
    style = style,
) {

    override fun build(builder: ParagraphBuilder, dimensions: List<PlaceholderStyle>?) {
        assert(dimensions != null)
        val hasStyle = style != null
        if (hasStyle) {
            builder.pushStyle(style)
        }
        assert(builder.placeholderCount < dimensions!!.size)
        val currentDimensions = dimensions[builder.placeholderCount]
        builder.addPlaceholder(currentDimensions)
        if (hasStyle) {
            builder.popStyle()
        }
    }

    override fun visitChildren(visitor: (InlineSpan) -> Boolean): Boolean = visitor(this)

    override fun visitDirectChildren(visitor: (InlineSpan) -> Boolean): Boolean = true

    companion object {

        fun extractFromInlineSpan(span: InlineSpan): List<Widget> {
            val widgets: MutableList<Widget> = mutableListOf()
            val fontSizeStack: MutableList<Float> = mutableListOf(kDefaultFontSize)
            visitSubtree(span, widgets, fontSizeStack)
            return widgets
        }

        private fun visitSubtree(
            span: InlineSpan,
            widgets: MutableList<Widget>,
            fontSizeStack: MutableList<Float>,
        ): Boolean {
            val fontSizeToPush: Float? = when (span) {
                is TextSpan -> {
                    if (span.style?.fontSize != fontSizeStack.last()) {
                        span.style?.fontSize
                    } else null
                }

                else -> null
            }
            if (fontSizeToPush != null) {
                fontSizeStack.add(fontSizeToPush);
            }
            if (span is WidgetSpan) {
                widgets.add(
                    WidgetSpanParentDataWidget(
                        span = span
                    ).bind(span.child)
                )
            }

            assert(span is WidgetSpan || span !is PlaceholderSpan) {
                "$span is a PlaceholderSpan but not a WidgetSpan subclass. This is currently not supported."
            }
            span.visitDirectChildren {
                visitSubtree(it, widgets, fontSizeStack)
            }
            if (fontSizeToPush != null) {
                val poppedFontSize = fontSizeStack.removeLast()
                assert(fontSizeStack.isNotEmpty())
                assert(poppedFontSize == fontSizeToPush)
            }
            return true
        }
    }
}