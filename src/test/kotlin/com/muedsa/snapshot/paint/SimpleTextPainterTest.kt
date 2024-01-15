package com.muedsa.snapshot.paint

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.drawPainter
import com.muedsa.snapshot.paint.text.SimpleTextPainter
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PathEffect
import org.jetbrains.skia.Rect
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.Direction
import org.jetbrains.skia.paragraph.HeightMode
import kotlin.math.max
import kotlin.test.Test

class SimpleTextPainterTest {

    @OptIn(ExperimentalStdlibApi::class)
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
            SimpleTextPainter(
                text = "$SAMPLE_TEXT ($familyName)",
                fontFamilyName = arrayOf(familyName)
            ).apply {
                offsetArr[it] = Offset(padding, height)
                layout(0f, Float.POSITIVE_INFINITY)
                width = max(width, this@apply.width + padding * 2)
                height += this@apply.height + padding
            }
        }

        drawPainter("text/fonts", width, height) { canvas ->
            painterArr.forEachIndexed { index, painter ->
                painter.paint(canvas, offset = offsetArr[index])
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun textAlign_ltr_test() {
        println("\n\n\nSimpleTextPainterTest.textAlign_ltr_test()")
        val boxWidth = 300f
        val boxHeight = 60f

        val offsetArr = Array(Alignment.entries.size) {
            Offset(0f, boxHeight * it)
        }

        val painterArr: Array<SimpleTextPainter> = Array(Alignment.entries.size) {
            val textAlign: Alignment = Alignment.entries[it]
            SimpleTextPainter(
                text = "$SAMPLE_TEXT\n$SAMPLE_TEXT($textAlign)\n$SAMPLE_TEXT",
                textAlign = textAlign
            ).apply {
                layout(0f, boxWidth)
            }
        }

        drawPainter("text/text_align_ltr", boxWidth, boxHeight * Alignment.entries.size) { canvas ->
            painterArr.forEachIndexed { index, painter ->
                val offset = offsetArr[index]
                canvas.drawRect(Rect.makeXYWH(offset.x, offset.y, boxWidth, boxHeight),
                    Paint().apply {
                        setStroke(true)
                        setARGB(144, 255, 0, 0)
                        pathEffect = PathEffect.makeDash(floatArrayOf(3f, 3f), 0f)
                    }
                )
                painter.paint(canvas, offset = offset)
                painter.debugPaint(canvas, offset = offset)
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun textAlign_rtl_test() {
        println("\n\n\nSimpleTextPainterTest.textAlign_rtl_test()")
        val boxWidth = 300f
        val boxHeight = 60f

        val offsetArr = Array(Alignment.entries.size) {
            Offset(0f, boxHeight * it)
        }

        val painterArr: Array<SimpleTextPainter> = Array(Alignment.entries.size) {
            val textAlign: Alignment = Alignment.entries[it]

            SimpleTextPainter(
                text = "$SAMPLE_TEXT_EN\n$SAMPLE_TEXT_EN($textAlign)\n$SAMPLE_TEXT_EN",
                textAlign = textAlign,
                textDirection = Direction.RTL
            ).apply {
                layout(0f, boxWidth)
            }
        }

        drawPainter("text/text_align_rtl", boxWidth, boxHeight * Alignment.entries.size) { canvas ->
            painterArr.forEachIndexed { index, painter ->
                val offset = offsetArr[index]
                canvas.drawRect(Rect.makeXYWH(offset.x, offset.y, boxWidth, boxHeight),
                    Paint().apply {
                        setStroke(true)
                        setARGB(144, 255, 0, 0)
                        pathEffect = PathEffect.makeDash(floatArrayOf(3f, 3f), 0f)
                    }
                )
                painter.paint(canvas, offset = offset)
                painter.debugPaint(canvas, offset = offset)
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun heightModel_test() {
        println("\n\n\nSimpleTextPainterTest.heightModel_test()")
        val padding = 100f
        var width = 0f
        var height = 0f

        val offsetArr = Array(HeightMode.entries.size) {
            Offset.ZERO
        }
        val painterArr: Array<SimpleTextPainter> = Array(HeightMode.entries.size) {
            val heightMode = HeightMode.entries[it]
            SimpleTextPainter(
                text = "$SAMPLE_TEXT($heightMode)",
                fontSize = 200f,
                textHeightMode = heightMode
            ).apply {
                offsetArr[it] = Offset(padding, height)
                layout(0f, Float.POSITIVE_INFINITY)
                width = max(width, this@apply.width + padding * 2)
                height += this@apply.height + padding
            }
        }

        drawPainter("text/height_mode", width, height) { canvas ->
            painterArr.forEachIndexed { index, painter ->
                painter.paint(canvas, offset = offsetArr[index])
                painter.debugPaint(canvas, offset = offsetArr[index])
            }
        }
    }


    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun emoji_test() {
        println("\n\n\nSimpleTextPainterTest.emoji_test()")
        val textPainter = SimpleTextPainter(
            text = SAMPLE_EMOJI,
            fontSize = 30f
        ).apply {
            layout(0f, 600f)
        }
        drawPainter("text/emoji", textPainter.size) { canvas ->
            textPainter.paint(canvas, offset = Offset.ZERO)
            textPainter.debugPaint(canvas, offset = Offset.ZERO)
        }
    }

    companion object {
        const val SAMPLE_TEXT_EN = "Hello Word!"
        const val SAMPLE_TEXT_CN = "你好，世界！"
        const val SAMPLE_TEXT = "$SAMPLE_TEXT_EN $SAMPLE_TEXT_CN"
        const val SAMPLE_EMOJI =
            "\uD83E\uDD70\uD83D\uDC80✌\uFE0F\uD83C\uDF34\uD83D\uDC22\uD83D\uDC10\uD83C\uDF44⚽\uD83C\uDF7B\uD83D\uDC51\uD83D\uDCF8\uD83D\uDE2C\uD83D\uDC40\uD83D\uDEA8\uD83C\uDFE1\uD83D\uDD4A\uFE0F\uD83C\uDFC6\uD83D\uDE3B\uD83C\uDF1F\uD83E\uDDFF\uD83C\uDF40\uD83C\uDFA8\uD83C\uDF5C"
    }
}