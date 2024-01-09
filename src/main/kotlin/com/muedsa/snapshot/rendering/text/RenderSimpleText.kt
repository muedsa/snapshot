package com.muedsa.snapshot.rendering.text

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.PaintingContext
import com.muedsa.snapshot.rendering.box.RenderBox
import org.jetbrains.skia.Color
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.paragraph.*
import kotlin.math.ceil

@Deprecated(message = "only unit test use it")
internal class RenderSimpleText(
    val content: String,
    val color: Int = Color.BLACK,
    val fontSize: Float = 15f
) : RenderBox() {

    var paragraph: Paragraph? = null

    override fun performLayout() {

        val width: Float = if(definiteConstraints.hasBoundedWidth) definiteConstraints.maxWidth
            else if (definiteConstraints.minWidth > 0) definiteConstraints.minWidth
            else Float.POSITIVE_INFINITY
        //assert(width.isFinite() && width > 0) { "$this must have definite width" }

        paragraph = ParagraphBuilder(style = paragraphStyle, fc = fontCollection)
            .apply {
                pushStyle(TextStyle().apply {
                    color = this@RenderSimpleText.color
                    fontSize = this@RenderSimpleText.fontSize
                })
                addText(content)
            }
            .build()
        paragraph!!.layout(width)
        size = definiteConstraints.constrainDimensions(width = ceil(paragraph!!.maxIntrinsicWidth), height = ceil(paragraph!!.height))
    }

    override fun paint(context: PaintingContext, offset: Offset) {
        assert(definiteSize.width.isFinite())
        if (definiteSize.isEmpty) {
            return
        }
        val paragraph = ParagraphBuilder(style = paragraphStyle, fc = fontCollection)
            .apply {
                pushStyle(TextStyle().apply {
                    color = this@RenderSimpleText.color
                    fontSize = this@RenderSimpleText.fontSize
                })
                addText(content)
            }
            .build()
        paragraph.layout(definiteSize.width)

        if (paragraph.height > definiteSize.height) {
            context.doClipRect(
                offset = offset,
                clipRect = Offset.ZERO combine definiteSize,
                clipBehavior = ClipBehavior.HARD_EDGE
            ) { c, o ->
                paragraph.paint(c.canvas, o.x, o.y)
            }
        } else {
            paragraph.paint(context.canvas, offset.x, offset.y)
        }
    }

    companion object {
        val paragraphStyle = ParagraphStyle().apply {
            strutStyle = StrutStyle()
        }
        val fontCollection = FontCollection().apply {
            setDefaultFontManager(FontMgr.default)
            setEnableFallback(true)
        }
    }

}