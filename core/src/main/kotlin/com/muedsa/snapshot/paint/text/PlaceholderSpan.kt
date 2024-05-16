package com.muedsa.snapshot.paint.text

import org.jetbrains.skia.paragraph.BaselineMode
import org.jetbrains.skia.paragraph.PlaceholderAlignment
import org.jetbrains.skia.paragraph.TextStyle

abstract class PlaceholderSpan(
    val alignment: PlaceholderAlignment = PlaceholderAlignment.BOTTOM,
    val baseline: BaselineMode?,
    style: TextStyle?
) : InlineSpan(style) {

    override fun computeToPlainText(buffer: StringBuffer, includePlaceholders: Boolean) {
        if (includePlaceholders) {
            buffer.append(PLACEHOLDER_CODE_UNIT)
        }
    }

    companion object {
        const val PLACEHOLDER_CODE_UNIT: Int = 0xFFFC
    }
}