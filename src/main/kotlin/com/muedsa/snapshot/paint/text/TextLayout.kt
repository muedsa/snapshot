package com.muedsa.snapshot.paint.text

import org.jetbrains.skia.paragraph.BaselineMode
import org.jetbrains.skia.paragraph.Paragraph

class TextLayout(
    val paragraph: Paragraph,
) {

    val width: Float = paragraph.maxWidth

    val height: Float = paragraph.height

    val minIntrinsicLineExtent: Float = paragraph.minIntrinsicWidth

    val maxIntrinsicLineExtent: Float = paragraph.maxIntrinsicWidth

    val longestLine: Float = paragraph.longestLine

    fun getDistanceToBaseline(baseline: BaselineMode): Float =
        when (baseline) {
            BaselineMode.ALPHABETIC -> paragraph.alphabeticBaseline
            BaselineMode.IDEOGRAPHIC -> paragraph.ideographicBaseline
        }
}