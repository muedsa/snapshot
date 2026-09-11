package com.muedsa.snapshot.widget.text

import com.muedsa.snapshot.paint.text.InlineSpan
import com.muedsa.snapshot.paint.text.TextOverflow
import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.paint.text.TextWidthBasis
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.text.RenderParagraph
import com.muedsa.snapshot.widget.ChildSlot
import com.muedsa.snapshot.widget.MultiChildWidget
import com.muedsa.snapshot.widget.Widget
import com.muedsa.snapshot.widget.createRenderBox
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.Direction
import org.jetbrains.skia.paragraph.HeightMode
import org.jetbrains.skia.paragraph.StrutStyle

fun ChildSlot.RichText(
    text: InlineSpan,
    textAlign: Alignment = Alignment.START,
    textDirection: Direction = Direction.LTR,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.CLIP,
    maxLines: Int? = null,
    strutStyle: StrutStyle? = null,
    textWidthBasis: TextWidthBasis = TextWidthBasis.PARENT,
    textHeightMode: HeightMode? = null,
) {
    check(this !is RichText) { "only TextSpan can be used in RichText widget" }
    attach(
        com.muedsa.snapshot.widget.text.RichText(
            text = text,
            textAlign = textAlign,
            textDirection = textDirection,
            softWrap = softWrap,
            overflow = overflow,
            maxLines = maxLines,
            strutStyle = strutStyle,
            textWidthBasis = textWidthBasis,
            textHeightMode = textHeightMode,
        )
    )
}

class RichText(
    val text: InlineSpan,
    val textAlign: Alignment = Alignment.START,
    val textDirection: Direction = Direction.LTR,
    val softWrap: Boolean = true,
    val overflow: TextOverflow = TextOverflow.CLIP,
    val maxLines: Int? = null,
    val strutStyle: StrutStyle? = null,
    val textWidthBasis: TextWidthBasis = TextWidthBasis.PARENT,
    val textHeightMode: HeightMode? = null,
) : MultiChildWidget() {

    init {
        WidgetSpan.extractFromInlineSpan(text).forEach { attach(it) }
    }

    override fun createRenderBox(children: List<Widget>): RenderBox {
        // emoji 需要根 span 才能回推继承字号。InlineSpan 没有上行指针,所以在这里显式向下注入,
        // 而不是让子节点沿 Widget.parent 反查(本批起 Widget 不再持有 parent)。
        children.forEach { (it as? WidgetSpanParentDataWidget)?.rootSpan = text }
        return RenderParagraph(
            text = text,
            textAlign = textAlign,
            textDirection = textDirection,
            softWrap = softWrap,
            overflow = overflow,
            maxLines = maxLines,
            strutStyle = strutStyle,
            textWidthBasis = textWidthBasis,
            textHeightMode = textHeightMode,
        ).also { p ->
            children.createRenderBox()?.let {
                p.appendChildren(it)
            }
        }
    }
}
