package com.muedsa.snapshot.paint.text

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.precisionErrorTolerance
import org.jetbrains.skia.paragraph.LineMetrics
import org.jetbrains.skia.paragraph.Paragraph

class TextPainterLayoutCacheWithOffset(
    val layout: TextLayout,
    val textAlignment: Float,
    val minWidth: Float,
    val maxWidth: Float,
    val widthBasis: TextWidthBasis,
) {

    init {
        assert(textAlignment in 0f..1f)
    }

    var contentWidth: Float = contentWidthFor(minWidth, maxWidth, widthBasis, layout)

    val paintOffset: Offset
        get() {
            if (textAlignment == 0f) {
                return Offset.ZERO
            }
            if (!paragraph.maxWidth.isFinite()) {
                return Offset(Float.POSITIVE_INFINITY, 0f)
            }
            val dx: Float = textAlignment * (contentWidth - paragraph.maxWidth)
            assert(!dx.isNaN())
            return Offset(dx, 0f)
        }


    val paragraph: Paragraph = layout.paragraph

    val lineMetrics: Array<LineMetrics> by lazy {
        return@lazy paragraph.lineMetrics
    }

    fun resizeToFit(minWidth: Float, maxWidth: Float, widthBasis: TextWidthBasis): Boolean {
        assert(layout.maxIntrinsicLineExtent.isFinite())
        // The assumption here is that if a Paragraph's width is already >= its
        // maxIntrinsicWidth, further increasing the input width does not change its
        // layout (but may change the paint offset if it's not left-aligned). This is
        // true even for TextAlign.justify: when width >= maxIntrinsicWidth
        // TextAlign.justify will behave exactly the same as TextAlign.start.
        //
        // An exception to this is when the text is not left-aligned, and the input
        // width is double.infinity. Since the resulting Paragraph will have a width
        // of double.infinity, and to make the text visible the paintOffset.dx is
        // bound to be double.negativeInfinity, which invalidates all arithmetic
        // operations.
        val newContentWidth: Float = contentWidthFor(minWidth, maxWidth, widthBasis, layout)
        if (newContentWidth == contentWidth) {
            return true
        }
        assert(minWidth <= maxWidth)
        // Always needsLayout when the current paintOffset and the paragraph width are not finite.
        if (!paintOffset.x.isFinite() && !paragraph.maxWidth.isFinite() && minWidth.isFinite()) {
            assert(paintOffset.x == Float.POSITIVE_INFINITY)
            assert(paragraph.maxWidth == Float.POSITIVE_INFINITY)
            return false
        }
        val maxIntrinsicWidth: Float = paragraph.maxIntrinsicWidth
        if ((paragraph.maxWidth - maxIntrinsicWidth) > -precisionErrorTolerance && (maxWidth - maxIntrinsicWidth) > -precisionErrorTolerance) {
            // Adjust the paintOffset and contentWidth to the new input constraints.
            contentWidth = newContentWidth
            return true
        }
        return false
    }

    companion object {
        private fun contentWidthFor(
            minWidth: Float,
            maxWidth: Float,
            widthBasis: TextWidthBasis,
            layout: TextLayout,
        ): Float =
            when (widthBasis) {
                TextWidthBasis.PARENT -> layout.longestLine.coerceIn(minWidth, maxWidth)
                TextWidthBasis.LONGESTLINE -> layout.maxIntrinsicLineExtent.coerceIn(minWidth, maxWidth)
            }
    }

}