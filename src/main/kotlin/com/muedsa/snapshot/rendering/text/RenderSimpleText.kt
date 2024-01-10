package com.muedsa.snapshot.rendering.text

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.paint.text.SimpleTextPainter
import com.muedsa.snapshot.rendering.PaintingContext
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderBox
import org.jetbrains.skia.Color
import org.jetbrains.skia.FontStyle

@Deprecated(message = "only unit test use it")
class RenderSimpleText(
    val content: String,
    val color: Int = Color.BLACK,
    val fontSize: Float = 14f,
    val fontFamilyName: Array<String>? = null,
    val fontStyle: FontStyle = FontStyle.NORMAL
) : RenderBox() {

    val textPainter: SimpleTextPainter = SimpleTextPainter(
        text = content,
        color = color,
        fontSize = fontSize,
        fontFamilyName = fontFamilyName,
        fontStyle = fontStyle
    )
    private var needsClipping: Boolean = false

    private fun layoutTextWithConstraints(constraints: BoxConstraints) {
        textPainter.layout(minWidth = constraints.minWidth, maxWidth= constraints.maxWidth)
    }

    override fun performLayout() {
        layoutTextWithConstraints(definiteConstraints)
        val textSize: Size = textPainter.size
        val textDidExceedMaxLines: Boolean = textPainter.didExceedMaxLines
        size = definiteConstraints.constrain(textSize)

        val didOverflowHeight: Boolean = definiteSize.height < textSize.height || textDidExceedMaxLines
        val didOverflowWidth: Boolean = definiteSize.width < textSize.width
        needsClipping = didOverflowWidth || didOverflowHeight
    }

    override fun paint(context: PaintingContext, offset: Offset) {
        layoutTextWithConstraints(definiteConstraints)
        if (needsClipping) {
            context.doClipRect(offset = offset, clipRect = Offset.ZERO combine definiteSize) {
                c, o -> textPainter.paint(c.canvas, o)
            }
        } else {
            textPainter.paint(context.canvas, offset)
        }
    }
}