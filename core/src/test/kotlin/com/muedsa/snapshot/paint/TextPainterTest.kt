package com.muedsa.snapshot.paint

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.drawPainter
import com.muedsa.snapshot.paint.text.TextPainter
import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.paint.text.TextStyle
import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.Direction
import org.jetbrains.skia.paragraph.HeightMode
import kotlin.math.max
import kotlin.test.Test

class TextPainterTest {

    @Test
    fun drawLocalFontListSample() {
        val padding = 10f
        var width = 0f
        var height = 0f

        val offsetArr = Array(FontMgr.default.familiesCount) {
            Offset.ZERO
        }
        val painterArr: Array<TextPainter> = Array(FontMgr.default.familiesCount) {
            val familyName = FontMgr.default.getFamilyName(it)
            TextPainter(
                text = TextSpan(
                    text = "$SAMPLE_TEXT ($familyName)",
                    style = TextStyle(
                        color = Color.BLACK,
                        fontFamilies = listOf(familyName)
                    )
                )
            ).apply {
                offsetArr[it] = Offset(padding, height)
                layout(0f, Float.POSITIVE_INFINITY)
                width = max(width, this@apply.width + padding * 2)
                height += this@apply.height + padding
            }
        }

        drawPainter("paint/text/fonts", width, height) { canvas ->
            painterArr.forEachIndexed { index, painter ->
                painter.paint(canvas, offset = offsetArr[index])
            }
        }
    }

    @Test
    fun textAlign_ltr_test() {
        println("\n\n\nTextPainterTest.textAlign_ltr_test()")
        val boxWidth = 300f
        val boxHeight = 60f

        val offsetArr = Array(Alignment.entries.size) {
            Offset(0f, boxHeight * it)
        }

        val painterArr: Array<TextPainter> = Array(Alignment.entries.size) {
            val textAlign: Alignment = Alignment.entries[it]
            TextPainter(
                text = TextSpan(
                    text = "$SAMPLE_TEXT\n$SAMPLE_TEXT($textAlign)\n$SAMPLE_TEXT"
                ),
                textAlign = textAlign
            ).apply {
                layout(0f, boxWidth)
            }
        }

        drawPainter("paint/text/text_align_ltr", boxWidth, boxHeight * Alignment.entries.size) { canvas ->
            painterArr.forEachIndexed { index, painter ->
                val offset = offsetArr[index]
                canvas.drawRect(
                    Rect.makeXYWH(offset.x, offset.y, boxWidth, boxHeight),
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

    @Test
    fun textAlign_rtl_test() {
        println("\n\n\nTextPainterTest.textAlign_rtl_test()")
        val boxWidth = 300f
        val boxHeight = 60f

        val offsetArr = Array(Alignment.entries.size) {
            Offset(0f, boxHeight * it)
        }

        val painterArr: Array<TextPainter> = Array(Alignment.entries.size) {
            val textAlign: Alignment = Alignment.entries[it]

            TextPainter(
                text = TextSpan(
                    text = "$SAMPLE_TEXT_EN\n$SAMPLE_TEXT_EN($textAlign)\n$SAMPLE_TEXT_EN"
                ),
                textAlign = textAlign,
                textDirection = Direction.RTL
            ).apply {
                layout(0f, boxWidth)
            }
        }

        drawPainter("paint/text/text_align_rtl", boxWidth, boxHeight * Alignment.entries.size) { canvas ->
            painterArr.forEachIndexed { index, painter ->
                val offset = offsetArr[index]
                canvas.drawRect(
                    Rect.makeXYWH(offset.x, offset.y, boxWidth, boxHeight),
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

    @Test
    fun heightModel_test() {
        println("\n\n\nTextPainterTest.heightModel_test()")
        val padding = 100f
        var width = 0f
        var height = 0f

        val offsetArr = Array(HeightMode.entries.size) {
            Offset.ZERO
        }
        val painterArr: Array<TextPainter> = Array(HeightMode.entries.size) {
            val heightMode = HeightMode.entries[it]
            TextPainter(
                text = TextSpan(
                    text = "$SAMPLE_TEXT($heightMode)",
                    style = TextStyle(
                        fontSize = 200f
                    )
                ),
                textHeightMode = heightMode
            ).apply {
                offsetArr[it] = Offset(padding, height)
                layout(0f, Float.POSITIVE_INFINITY)
                width = max(width, this@apply.width + padding * 2)
                height += this@apply.height + padding
            }
        }

        drawPainter("paint/text/height_mode", width, height) { canvas ->
            painterArr.forEachIndexed { index, painter ->
                painter.paint(canvas, offset = offsetArr[index])
                painter.debugPaint(canvas, offset = offsetArr[index])
            }
        }
    }

    @Test
    fun emoji_test() {
        println("\n\n\nPainterTest.emoji_test()")
        val textPainter = TextPainter(
            text = TextSpan(
                text = SAMPLE_EMOJI,
                style = TextStyle(
                    fontSize = 30f
                )
            )
        ).apply {
            layout(0f, 600f)
        }
        drawPainter("paint/text/emoji", textPainter.size) { canvas ->
            textPainter.paint(canvas, offset = Offset.ZERO)
            textPainter.debugPaint(canvas, offset = Offset.ZERO)
        }
    }

    @Test
    fun en_font_size_test() {
        println("\n\n\nTextPainterTest.cn_font_size_test()")
        font_size_test(LONG_TEXT_EN, "Noto Sans SC", "paint/text/en_font_size")
    }

    @Test
    fun cn_font_size_test() {
        println("\n\n\nTextPainterTest.cn_font_size_test()")
        font_size_test(LONG_TEXT_CN, "Noto Sans SC", "paint/text/cn_font_size")
    }

    private fun font_size_test(text: String, fontFamilyName: String, imgPath: String) {
        val padding = 20f
        var y = padding
        val painterList = mutableListOf<TextPainter>()
        var width = 0f
        var height = padding
        for (fontSize in 5..40) {
            val textPainter = TextPainter(
                text = TextSpan(
                    text = "[$fontSize] $text",
                    style = TextStyle(
                        color = Color.WHITE,
                        fontSize = fontSize.toFloat(),
                        fontFamilies = listOf(fontFamilyName)
                    )
                )
            ).apply {
                layout(0f, Float.POSITIVE_INFINITY)
            }
            width = max(textPainter.maxIntrinsicWidth + padding * 2, width)
            height += textPainter.height + padding
            painterList.add(textPainter)
        }
        drawPainter(imgPath, width, height, background = 0xFF_E5_98_65.toInt()) { canvas ->
            painterList.forEach {
                it.paint(canvas, offset = Offset(padding, y))
                y += it.height + padding
            }
        }
    }

    companion object {
        const val SAMPLE_TEXT_EN = "Hello Word!"
        const val SAMPLE_TEXT_CN = "你好，世界！"
        const val LONG_TEXT_EN = "Whereas disregard and contempt for human rights have resulted"
        const val LONG_TEXT_CN =
            "鉴于对人类家庭所有成员的固有尊严及其平等的和不移的权利的承认,乃是世界自由、正义与和平的基础"
        const val SAMPLE_TEXT = "$SAMPLE_TEXT_EN $SAMPLE_TEXT_CN"
        const val SAMPLE_EMOJI =
            "\uD83E\uDD70\uD83D\uDC80✌\uFE0F\uD83C\uDF34\uD83D\uDC22\uD83D\uDC10\uD83C\uDF44⚽\uD83C\uDF7B\uD83D\uDC51\uD83D\uDCF8\uD83D\uDE2C\uD83D\uDC40\uD83D\uDEA8\uD83C\uDFE1\uD83D\uDD4A\uFE0F\uD83C\uDFC6\uD83D\uDE3B\uD83C\uDF1F\uD83E\uDDFF\uD83C\uDF40\uD83C\uDFA8\uD83C\uDF5C"
    }
}