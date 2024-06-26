package com.muedsa.snapshot.paint.text

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.geometry.shift
import com.muedsa.snapshot.kDefaultFontSize
import com.muedsa.snapshot.kDefaultTextColor
import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.*

class TextPainter(
    val text: InlineSpan,
    val textAlign: Alignment = Alignment.START,
    val textDirection: Direction = Direction.LTR,
    val maxLines: Int? = null,
    val ellipsis: String? = null,
    val strutStyle: StrutStyle? = null,
    val textWidthBasis: TextWidthBasis = TextWidthBasis.PARENT,
    val textHeightMode: HeightMode? = null,
) {

    private var layoutCache: TextPainterLayoutCacheWithOffset? = null

    private var inputWidth: Float = Float.NaN

    val minIntrinsicWidth: Float
        get() = layoutCache!!.layout.minIntrinsicLineExtent

    val maxIntrinsicWidth: Float
        get() = layoutCache!!.layout.maxIntrinsicLineExtent

    val width: Float
        get() = layoutCache!!.contentWidth

    val height: Float
        get() = layoutCache!!.layout.height

    val size: Size
        get() = Size(width, height)

    fun computeDistanceToActualBaseline(baseline: BaselineMode) =
        layoutCache!!.layout.getDistanceToBaseline(baseline)

    val didExceedMaxLines: Boolean
        get() = layoutCache!!.paragraph.didExceedMaxLines()

    fun inlinePlaceholderBoxes(): List<TextBox>? {
        val layout: TextPainterLayoutCacheWithOffset? = layoutCache
        if (layout == null) {
            return null
        }
        val offset: Offset = layout.paintOffset
        if (!offset.x.isFinite() || !offset.y.isFinite()) {
            return emptyList()
        }
        val rawBoxes = layout.inlinePlaceholderBoxes
        if (offset == Offset.ZERO) {
            return rawBoxes
        }

        return rawBoxes.map {
            shiftTextBox(it, offset)
        }
    }

    var placeholderDimensions: List<PlaceholderStyle>? = null

    private fun createParagraph(text: InlineSpan): Paragraph =
        ParagraphBuilder(
            style = ParagraphStyle().apply {
                strutStyle = this@TextPainter.strutStyle ?: StrutStyle()
                this.direction = textDirection
                this.alignment = textAlign
                this.ellipsis = this@TextPainter.ellipsis
                maxLines?.let {
                    this.maxLinesCount = it
                }
                textHeightMode?.let {
                    this.heightMode = it
                }
                fontRastrSettings = DEFAULT_FONT_RASTR_SETTINGS
            },
            fc = FONT_COLLECTION
        ).also {
            text.updateMergedStyle(DEFAULT_TEXT_STYLE)
            text.build(it, dimensions = placeholderDimensions)
        }.build()

    fun layout(minWidth: Float = 0f, maxWidth: Float = Float.POSITIVE_INFINITY) {
        assert(!maxWidth.isNaN())
        assert(!minWidth.isNaN())

        val cachedLayout = layoutCache
        if (cachedLayout != null && cachedLayout.resizeToFit(minWidth, maxWidth, textWidthBasis)) {
            return
        }

        val paintOffsetAlignment: Float = computePaintOffsetFraction(textAlign, textDirection)
        // Try to avoid laying out the paragraph with maxWidth=double.infinity
        // when the text is not left-aligned, so we don't have to deal with an
        // infinite paint offset.
        val adjustMaxWidth: Boolean = !maxWidth.isFinite() && paintOffsetAlignment != 0f
        val adjustedMaxWidth: Float? = if (!adjustMaxWidth) maxWidth else cachedLayout?.layout?.maxIntrinsicLineExtent
        inputWidth = adjustedMaxWidth ?: maxWidth

        // Only rebuild the paragraph when there're layout changes, even when
        // `_rebuildParagraphForPaint` is true. It's best to not eagerly rebuild
        // the paragraph to avoid the extra work, because:
        // 1. the text color could change again before `paint` is called (so one of
        //    the paragraph rebuilds is unnecessary)
        // 2. the user could be measuring the text layout so `paint` will never be
        //    called.

        val paragraph: Paragraph = cachedLayout?.paragraph ?: createParagraph(text)
        paragraph.layout(inputWidth)

        val newLayoutCache: TextPainterLayoutCacheWithOffset = TextPainterLayoutCacheWithOffset(
            layout = TextLayout(paragraph),
            textAlignment = paintOffsetAlignment,
            minWidth = minWidth,
            maxWidth = maxWidth,
            widthBasis = textWidthBasis
        )
        // Call layout again if newLayoutCache had an infinite paint offset.
        // This is not as expensive as it seems, line breaking is relatively cheap
        // as compared to shaping.
        if (adjustedMaxWidth == null && minWidth.isFinite()) {
            assert(maxWidth.isInfinite())
            val newInputWidth: Float = newLayoutCache.layout.maxIntrinsicLineExtent
            paragraph.layout(newInputWidth)
            inputWidth = newInputWidth
        }
        layoutCache = newLayoutCache
    }


    fun paint(canvas: Canvas, offset: Offset) {
        val layoutCache = layoutCache
        assert(layoutCache != null) {
            "TextPainter.paint called when text geometry was not yet calculated\n" +
                    "Please call layout() before paint() to position the text before painting it."
        }

        val paintOffset = layoutCache!!.paintOffset
        if (!paintOffset.x.isFinite() || !paintOffset.y.isFinite()) {
            return
        }
        layoutCache.paragraph.paint(canvas, offset.x + paintOffset.x, offset.y + paintOffset.y)
    }

    fun debugPaint(canvas: Canvas, offset: Offset) {
        println(toString())
        val paintOffset = layoutCache!!.paintOffset
        if (!paintOffset.x.isFinite() || !paintOffset.y.isFinite()) {
            return
        }

        // debug paintOffset
        println("offset:$offset, width: $width, paintOffset:$paintOffset due to textAlign:$textAlign")

        val lineMetrics: Array<LineMetrics> = layoutCache!!.paragraph.lineMetrics
        if (lineMetrics.isNotEmpty()) {
            val firstLineMetrics: LineMetrics = lineMetrics[0]

            val textEndOffsetX = offset.x + width

            // top edge
            val topEdgeOffsetY = offset.y + (firstLineMetrics.baseline - firstLineMetrics.ascent).toFloat()
            canvas.drawLine(
                x0 = offset.x,
                y0 = topEdgeOffsetY,
                x1 = textEndOffsetX,
                y1 = topEdgeOffsetY,
                paint = Paint().apply {
                    setStroke(true)
                    color = 0x90_FF_69_B4.toInt()
                    pathEffect = PathEffect.makeDash(floatArrayOf(2f, 4f), 0f)
                }
            )

            // baseline
            val baselineOffsetY = offset.y + firstLineMetrics.baseline.toFloat()
            canvas.drawLine(
                x0 = offset.x,
                y0 = baselineOffsetY,
                x1 = textEndOffsetX,
                y1 = baselineOffsetY,
                paint = Paint().apply {
                    setStroke(true)
                    color = 0x90_C7_15_85.toInt()
                    pathEffect = PathEffect.makeDash(floatArrayOf(3f, 6f), 0f)
                }
            )

            // bottom edge
            val bottomEdgeOffsetY = offset.y + (firstLineMetrics.baseline + firstLineMetrics.descent).toFloat()
            canvas.drawLine(
                x0 = offset.x,
                y0 = bottomEdgeOffsetY,
                x1 = textEndOffsetX,
                y1 = bottomEdgeOffsetY,
                paint = Paint().apply {
                    setStroke(true)
                    color = 0x90_FF_14_93.toInt()
                    pathEffect = PathEffect.makeDash(floatArrayOf(2f, 4f), 0f)
                }
            )
        }
    }

    companion object {

        @JvmStatic
        fun computeWidth(
            text: TextSpan,
            textAlign: Alignment = Alignment.START,
            textDirection: Direction = Direction.LTR,
            maxLines: Int? = null,
            ellipsis: String? = null,
            textWidthBasis: TextWidthBasis = TextWidthBasis.PARENT,
            textHeightMode: HeightMode? = null,
            minWidth: Float = 0f,
            maxWidth: Float = Float.POSITIVE_INFINITY,
        ): Float {
            val painter = TextPainter(
                text = text,
                textAlign = textAlign,
                textDirection = textDirection,
                maxLines = maxLines,
                ellipsis = ellipsis,
                textWidthBasis = textWidthBasis,
                textHeightMode = textHeightMode
            ).apply {
                layout(minWidth = minWidth, maxWidth = maxWidth)
            }
            return painter.width
        }

        @JvmStatic
        fun computeMaxIntrinsicWidth(
            text: TextSpan,
            textAlign: Alignment = Alignment.START,
            textDirection: Direction = Direction.LTR,
            maxLines: Int? = null,
            ellipsis: String? = null,
            strutStyle: StrutStyle? = null,
            textWidthBasis: TextWidthBasis = TextWidthBasis.PARENT,
            textHeightMode: HeightMode? = null,
            minWidth: Float = 0f,
            maxWidth: Float = Float.POSITIVE_INFINITY,
        ): Float {
            val painter = TextPainter(
                text = text,
                textAlign = textAlign,
                textDirection = textDirection,
                maxLines = maxLines,
                ellipsis = ellipsis,
                strutStyle = strutStyle,
                textWidthBasis = textWidthBasis,
                textHeightMode = textHeightMode
            ).apply {
                layout(minWidth = minWidth, maxWidth = maxWidth)
            }
            return painter.maxIntrinsicWidth
        }

        private fun computePaintOffsetFraction(textAlign: Alignment, textDirection: Direction): Float =
            when (textAlign) {
                Alignment.LEFT -> 0f
                Alignment.RIGHT -> 1f
                Alignment.CENTER -> 0.5f
                Alignment.JUSTIFY -> if (textDirection == Direction.LTR) 0f else 1f
                Alignment.START -> if (textDirection == Direction.LTR) 0f else 1f
                Alignment.END -> if (textDirection == Direction.LTR) 1f else 0f
            }

        private fun shiftTextBox(box: TextBox, offset: Offset): TextBox =
            TextBox(
                rect = box.rect.shift(offset),
                direction = box.direction,
            )

        val FONT_COLLECTION = FontCollection().apply {
            setDefaultFontManager(FontMgr.default)
            setEnableFallback(true)
        }

        val DEFAULT_FONT_RASTR_SETTINGS: FontRastrSettings = FontRastrSettings(
            edging = FontEdging.SUBPIXEL_ANTI_ALIAS,
            hinting = FontHinting.NORMAL,
            subpixel = true
        )

        var DEFAULT_TEXT_STYLE: TextStyle = TextStyle(
            color = kDefaultTextColor,
            fontSize = kDefaultFontSize
        )
    }
}