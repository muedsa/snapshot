package com.muedsa.snapshot.paint.text

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.*

@ExperimentalStdlibApi
class SimpleTextPainter(
    val text: String,
    val color: Int = Color.BLACK,
    val fontSize: Float = 14f,
    val fontFamilyName: Array<String>? = null,
    val fontStyle: FontStyle = FontStyle.NORMAL,
    val textAlign: Alignment = Alignment.START,
    val textDirection: Direction = Direction.LTR,
    val maxLines: Int? = null,
    val ellipsis: String? = null,
    val textWidthBasis: TextWidthBasis = TextWidthBasis.PARENT,
    val textHeightMode: HeightMode? = null,
): AutoCloseable {

    init {
        fontFamilyName?.let {
            assert(FONT_COLLECTION.findTypefaces(fontFamilyName, fontStyle).isNotEmpty())
        }
    }

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

    private fun createParagraph(text: String, color: Int, fontSize: Float): Paragraph =
        ParagraphBuilder(
            style =  ParagraphStyle().apply {
                strutStyle = StrutStyle()
                this.direction = textDirection
                this.alignment = alignment
                this.ellipsis = this@SimpleTextPainter.ellipsis
                maxLines?.let {
                    this.maxLinesCount = it
                }
                textHeightMode?.let {
                    this.heightMode = it
                }
            },
            fc = FONT_COLLECTION
        ).apply {
            pushStyle(TextStyle().apply {
                this.color = color
                this.fontSize = fontSize
                fontFamilyName?.let {
                    this.fontFamilies = it
                }
                this.fontStyle = fontStyle
            })
            addText(text)
        }.build()

    fun layout(minWidth: Float = 0f, maxWidth: Float = Float.POSITIVE_INFINITY) {
        assert(!maxWidth.isNaN())
        assert(!minWidth.isNaN())

        val cachedLayout = layoutCache
        if (cachedLayout != null  && cachedLayout.resizeToFit(minWidth, maxWidth, textWidthBasis)) {
            return
        }

        val paintOffsetAlignment: Float = computePaintOffsetFraction(textAlign, textDirection)
        // Try to avoid laying out the paragraph with maxWidth=double.infinity
        // when the text is not left-aligned, so we don't have to deal with an
        // infinite paint offset.
        val adjustMaxWidth: Boolean = !maxWidth.isFinite() && paintOffsetAlignment != 0f
        val adjustedMaxWidth: Float? = if(!adjustMaxWidth) maxWidth else cachedLayout?.layout?.maxIntrinsicLineExtent
        inputWidth = adjustedMaxWidth ?: maxWidth

        // Only rebuild the paragraph when there're layout changes, even when
        // `_rebuildParagraphForPaint` is true. It's best to not eagerly rebuild
        // the paragraph to avoid the extra work, because:
        // 1. the text color could change again before `paint` is called (so one of
        //    the paragraph rebuilds is unnecessary)
        // 2. the user could be measuring the text layout so `paint` will never be
        //    called.

        val paragraph: Paragraph = cachedLayout?.paragraph ?: createParagraph(text, color, fontSize)
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
        val paintOffset = layoutCache!!.paintOffset
        if (!paintOffset.x.isFinite() || !paintOffset.y.isFinite()) {
            return
        }
        val textOffsetX = offset.x + paintOffset.x
        val textOffsetY = offset.y + paintOffset.y


        // debug paintOffset
        if (paintOffset.x > 0) {
            canvas.drawLine(
                x0 = offset.x,
                y0 = textOffsetY,
                x1 = textOffsetX,
                y1 = textOffsetY,
                paint = Paint().apply {
                    setStroke(true)
                    setARGB(144, 0, 255, 0)
                    pathEffect = PathEffect.makeDash(floatArrayOf(3f, 3f), 0f)
                }
            )
        }
        if (paintOffset.y > 0) {
            canvas.drawLine(
                x0 = textOffsetX,
                y0 = offset.y,
                x1 = textOffsetX,
                y1 = textOffsetY,
                paint = Paint().apply {
                    setStroke(true)
                    setARGB(144, 0, 255, 0)
                    pathEffect = PathEffect.makeDash(floatArrayOf(3f, 3f), 0f)
                }
            )
        }

        val lineMetrics: Array<LineMetrics> = layoutCache!!.paragraph.lineMetrics
        if (lineMetrics.isNotEmpty()) {
            val firstLineMetrics: LineMetrics = lineMetrics[0]

            val textEndOffsetX = textOffsetX + firstLineMetrics.width.toFloat()

            // top edge
            val topEdgeOffsetY = textOffsetY + (firstLineMetrics.baseline - firstLineMetrics.ascent).toFloat()
            canvas.drawLine(
                x0 = textOffsetX,
                y0 = topEdgeOffsetY,
                x1 = textEndOffsetX,
                y1 = topEdgeOffsetY,
                paint = Paint().apply {
                    setStroke(true)
                    setARGB(144, 0, 0, 255)
                    pathEffect = PathEffect.makeDash(floatArrayOf(3f, 3f), 0f)
                }
            )

            // baseline
            val baselineOffsetY = textOffsetY + firstLineMetrics.baseline.toFloat()
            canvas.drawLine(
                x0 = textOffsetX,
                y0 = baselineOffsetY,
                x1 = textEndOffsetX,
                y1 = baselineOffsetY,
                paint = Paint().apply {
                    setStroke(true)
                    setARGB(144, 0, 0, 255)
                    pathEffect = PathEffect.makeDash(floatArrayOf(3f, 3f), 0f)
                }
            )

            // bottom edge
            val bottomEdgeOffsetY = textOffsetY  + (firstLineMetrics.baseline + firstLineMetrics.descent).toFloat()
            canvas.drawLine(
                x0 = textOffsetX,
                y0 = bottomEdgeOffsetY,
                x1 = textEndOffsetX,
                y1 = bottomEdgeOffsetY,
                paint = Paint().apply {
                    setStroke(true)
                    setARGB(144, 0, 0, 255)
                    pathEffect = PathEffect.makeDash(floatArrayOf(3f, 3f), 0f)
                }
            )
        }
    }

    override fun close() {
        layoutCache?.paragraph?.close()
        layoutCache = null
    }

    companion object {

        @JvmStatic
        fun computeWidth(
            text: String,
            color: Int,
            fontSize: Float,
            fontFamilyName: Array<String>? = null,
            fontStyle: FontStyle = FontStyle.NORMAL,
            textAlign: Alignment = Alignment.START,
            textDirection: Direction = Direction.LTR,
            maxLines: Int? = null,
            ellipsis: String? = null,
            textWidthBasis: TextWidthBasis = TextWidthBasis.PARENT,
            textHeightMode: HeightMode? = null,
            minWidth: Float = 0f,
            maxWidth: Float = Float.POSITIVE_INFINITY,
        ): Float {
            val painter = SimpleTextPainter(
                text = text,
                color = color,
                fontSize = fontSize,
                fontFamilyName = fontFamilyName,
                fontStyle = fontStyle,
                textAlign = textAlign,
                textDirection = textDirection,
                maxLines = maxLines,
                ellipsis = ellipsis,
                textWidthBasis = textWidthBasis,
                textHeightMode = textHeightMode
            ).apply {
                layout(minWidth = minWidth, maxWidth = maxWidth)
            }
            return painter.use { painter.width }
        }

        @JvmStatic
        fun computeMaxIntrinsicWidth(
            text: String,
            color: Int,
            fontSize: Float,
            fontFamilyName: Array<String>? = null,
            fontStyle: FontStyle = FontStyle.NORMAL,
            textAlign: Alignment = Alignment.START,
            textDirection: Direction = Direction.LTR,
            maxLines: Int? = null,
            ellipsis: String? = null,
            textWidthBasis: TextWidthBasis = TextWidthBasis.PARENT,
            textHeightMode: HeightMode? = null,
            minWidth: Float = 0f,
            maxWidth: Float = Float.POSITIVE_INFINITY,
        ): Float {
            val painter = SimpleTextPainter(
                text = text,
                color = color,
                fontSize = fontSize,
                fontFamilyName = fontFamilyName,
                fontStyle = fontStyle,
                textAlign = textAlign,
                textDirection = textDirection,
                maxLines = maxLines,
                ellipsis = ellipsis,
                textWidthBasis = textWidthBasis,
                textHeightMode = textHeightMode
            ).apply {
                layout(minWidth = minWidth, maxWidth = maxWidth)
            }
            return painter.use { painter.maxIntrinsicWidth }
        }

        private fun computePaintOffsetFraction(textAlign: Alignment, textDirection: Direction) :Float =
            when(textAlign) {
                Alignment.LEFT -> 0f
                Alignment.RIGHT -> 1f
                Alignment.CENTER -> 0.5f
                Alignment.JUSTIFY -> if (textDirection == Direction.LTR) 0f else 1f
                Alignment.START -> if (textDirection == Direction.LTR) 0f else 1f
                Alignment.END -> if (textDirection == Direction.LTR) 1f else 0f
            }


        internal val FONT_COLLECTION = FontCollection().apply {
            setDefaultFontManager(FontMgr.default)
            setEnableFallback(true)
        }
    }
}