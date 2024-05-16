package com.muedsa.snapshot.rendering.text

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.kEllipsis
import com.muedsa.snapshot.paint.text.*
import com.muedsa.snapshot.rendering.PaintingContext
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderContainerBox
import com.muedsa.snapshot.widget.text.TextParentData
import org.jetbrains.skia.paragraph.*

class RenderParagraph(
    val text: InlineSpan,
    val textAlign: Alignment = Alignment.START,
    val textDirection: Direction = Direction.LTR,
    val softWrap: Boolean = true,
    val overflow: TextOverflow = TextOverflow.CLIP,
    val maxLines: Int? = null,
    val strutStyle: StrutStyle? = null,
    val textWidthBasis: TextWidthBasis = TextWidthBasis.PARENT,
    val textHeightMode: HeightMode? = null,
) : RenderContainerBox() {

    private val _textPainter: TextPainter = TextPainter(
        text = text,
        textAlign = textAlign,
        textDirection = textDirection,
        maxLines = maxLines,
        ellipsis = if(overflow == TextOverflow.ELLIPSIS) kEllipsis else null,
        strutStyle = strutStyle,
        textWidthBasis = textWidthBasis,
        textHeightMode = textHeightMode,
    )
    private var _needsClipping: Boolean = false

    override fun setupParentData(child: RenderBox) {
        if (child.parentData !is TextParentData) {
            child.parentData = TextParentData()
        }
    }

    override fun computeDistanceToActualBaseline(baseline: BaselineMode): Float {
        layoutTextWithConstraints(definiteConstraints)
        return _textPainter.computeDistanceToActualBaseline(baseline)
    }

    private fun layoutText(minWidth: Float = 0f, maxWidth: Float = Float.POSITIVE_INFINITY) {
        val widthMatters: Boolean = softWrap || overflow == TextOverflow.ELLIPSIS
        _textPainter.layout(minWidth = minWidth, maxWidth = if (widthMatters) maxWidth else Float.POSITIVE_INFINITY)
    }

    private var _placeholderDimensions: List<PlaceholderStyle>? = null

    private fun layoutTextWithConstraints(constraints: BoxConstraints) {
        _textPainter.placeholderDimensions = _placeholderDimensions
        layoutText(minWidth = constraints.minWidth, maxWidth = constraints.maxWidth)
    }

    override fun performLayout() {
        _placeholderDimensions = layoutInlineChildren(definiteConstraints.maxWidth) { child, constraints ->
            child.layout(constraints)
            return@layoutInlineChildren child.definiteSize
        }
        layoutTextWithConstraints(definiteConstraints)
        positionInlineChildren(_textPainter.inlinePlaceholderBoxes()!!)

        // We grab _textPainter.size and _textPainter.didExceedMaxLines here because
        // assigning to `size` will trigger us to validate our intrinsic sizes,
        // which will change _textPainter's layout because the intrinsic size
        // calculations are destructive. Other _textPainter state will also be
        // affected. See also RenderEditable which has a similar issue.
        val textSize: Size = _textPainter.size
        val textDidExceedMaxLines: Boolean = _textPainter.didExceedMaxLines
        size = definiteConstraints.constrain(textSize)

        val didOverflowHeight: Boolean = definiteSize.height < textSize.height || textDidExceedMaxLines
        val didOverflowWidth: Boolean = definiteSize.width < textSize.width
        _needsClipping = didOverflowWidth || didOverflowHeight
    }

    override fun paint(context: PaintingContext, offset: Offset) {
        // Ideally we could compute the min/max intrinsic width/height with a
        // non-destructive operation. However, currently, computing these values
        // will destroy state inside the painter. If that happens, we need to get
        // back the correct state by calling _layout again.
        //
        // TODO(abarth): Make computing the min/max intrinsic width/height a
        //  non-destructive operation.
        //
        // If you remove this call, make sure that changing the textAlign still
        // works properly.

        layoutTextWithConstraints(definiteConstraints)
        if (_needsClipping) {
            context.pushClipRect(offset = offset, clipRect = Offset.ZERO combine definiteSize) { c, o ->
                _textPainter.paint(c.canvas, o)
                paintInlineChildren(c, o)
            }
        } else {
            _textPainter.paint(context.canvas, offset)
            paintInlineChildren(context, offset)
        }
    }

    override fun debugPaint(context: PaintingContext, offset: Offset) {
        super.debugPaint(context, offset)
        _textPainter.debugPaint(context.canvas, offset)
    }

    private fun layoutInlineChildren(maxWidth: Float, layoutChild: (RenderBox, BoxConstraints) -> Size): List<PlaceholderStyle> {
        return children.map { layoutChild(it,maxWidth, layoutChild) }
    }

    fun positionInlineChildren(boxes: List<TextBox>) {
        boxes.forEachIndexed { index: Int, box: TextBox ->
            val child = children[index]
            val textParentData = child.parentData as TextParentData
            textParentData.offset = Offset(box.rect.left, box.rect.top)
        }
    }

    private fun paintInlineChildren(context: PaintingContext , offset: Offset) {
        children.forEach {
            val childParentData: TextParentData = it.parentData as TextParentData
            val childOffset: Offset = childParentData.offset
            context.paintChild(it, childOffset + offset)
        }
    }

    companion object {

        fun layoutChild(child: RenderBox, maxWidth: Float, layoutChild: (RenderBox, BoxConstraints) -> Size): PlaceholderStyle {
            val parentData: TextParentData = child.parentData as TextParentData
            val span: PlaceholderSpan = parentData.span!!
            val childConstraints: BoxConstraints = BoxConstraints(maxWidth = maxWidth)
            val childSize: Size = layoutChild(child, childConstraints)
            val baselineMode: BaselineMode = span.baseline ?: BaselineMode.ALPHABETIC
            val baseline: Float = if (span.alignment == PlaceholderAlignment.BASELINE) child.getDistanceToBaseline(baselineMode) ?: 0f else 0f
            return PlaceholderStyle(
                width = childSize.width,
                height = childSize.height,
                alignment = span.alignment,
                baselineMode = baselineMode,
                baseline = baseline,
            )
        }
    }
}