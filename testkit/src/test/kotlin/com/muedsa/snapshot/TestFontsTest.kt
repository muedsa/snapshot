package com.muedsa.snapshot

import com.muedsa.snapshot.paint.text.TextPainter
import kotlin.test.Test
import kotlin.test.assertTrue

class TestFontsTest {

    @Test
    fun bundled_font_is_registered_for_paragraph_font_resolution() {
        val resolvedTypefaces = TextPainter.FONT_COLLECTION.findTypefaces(
            familyNames = arrayOf(testFontFamily),
            style = testTypeface.fontStyle,
        )

        assertTrue(
            resolvedTypefaces.any { it?.uniqueId == testTypeface.uniqueId },
            "Paragraph 字体集合应按字体族名解析到 testkit 内置字体",
        )
    }
}
