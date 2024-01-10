package com.muedsa.snapshot.paint

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.drawPainter
import com.muedsa.snapshot.paint.text.SimpleTextPainter
import org.jetbrains.skia.FontMgr
import kotlin.math.max
import kotlin.test.Test

class SimpleTextPainterTest {

    @Test
    fun drawLocalFontListSample() {
        val padding = 10f
        var width = 0f
        var height = 0f

        val offsetArr = Array(FontMgr.default.familiesCount) {
            Offset.ZERO
        }
        val painterArr: Array<SimpleTextPainter> = Array(FontMgr.default.familiesCount) {
            val familyName = FontMgr.default.getFamilyName(it)
            val painter = SimpleTextPainter(
                text = "$SAMPLE_TEXT ($familyName)",
                fontFamilyName = arrayOf(familyName)
            )
            offsetArr[it] = Offset(padding, height)
            painter.layout(0f, Float.POSITIVE_INFINITY)
            width = max(width, painter.width + padding * 2)
            height += painter.height + padding
            painter
        }

        drawPainter("text/fonts", width, height) { canvas ->
            painterArr.forEachIndexed { index, painter ->
                painter.paint(canvas, offset = offsetArr[index])
            }
        }
    }


    companion object {
        const val SAMPLE_TEXT = "Hello Word! 你好，世界！"
    }
}