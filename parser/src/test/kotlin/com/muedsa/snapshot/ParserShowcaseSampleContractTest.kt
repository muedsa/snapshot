package com.muedsa.snapshot

import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.widget.TextParser
import com.muedsa.snapshot.paint.text.TextPainter
import com.muedsa.snapshot.widget.text.RichText
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Pixmap
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import java.lang.ref.Reference
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ParserShowcaseSampleContractTest {

    @Test
    fun dom_card_is_a_rich_offline_png() {
        val bytes = ParserShowcaseSample().renderDomCard()

        assertTrue(bytes.size > 8_000, "解析器示例图不能退化为空白或低信息量图片")
        withDecodedPixels(bytes) { pixels ->
            assertEquals(1000, pixels.info.width)
            assertEquals(560, pixels.info.height)
            val sampledColors = buildSet {
                for (y in 0 until pixels.info.height step 20) {
                    for (x in 0 until pixels.info.width step 20) {
                        add(pixels.getColor(x, y))
                    }
                }
            }
            assertTrue(sampledColors.size >= 12, "解析器示例图应展示卡片、边框和强调色")
        }
    }

    @Test
    fun dom_card_text_uses_bundled_test_font() {
        val root = ParserShowcaseSample().parseDomCard()
        val textElements = root.walk()
            .filter { it.widgetParser is TextParser && it.parent?.widgetParser !is TextParser }
            .toList()

        assertTrue(textElements.isNotEmpty(), "解析器示例应包含文本节点")
        val plainText = StringBuffer()
        textElements.forEach { element ->
            val richText = element.createWidget() as RichText
            richText.text.computeToPlainText(plainText, includePlaceholders = false)
            TextPainter(richText.text).layout(maxWidth = 1_000f)
            richText.text.visitChildren { span ->
                assertEquals(
                    listOf(testFontFamily),
                    span.mergedStyle?.fontFamilies,
                    "最终文本叶子应使用 testkit 内置字体族",
                )
                true
            }
        }
        assertTrue(
            plainText.contains(SHOWCASE_CHINESE_TEXT),
            "解析结果应保留完整中文说明",
        )
        val missingGlyphs = SHOWCASE_CHINESE_GLYPHS.filter { char ->
            testTypeface.getUTF32Glyph(char.code).toInt() == 0
        }
        assertTrue(
            missingGlyphs.isEmpty(),
            "testkit 内置字体缺少展示所需中文字形：$missingGlyphs",
        )
    }

    @Test
    fun dom_card_keeps_decorations_cards_and_status_badge() {
        withDecodedPixels(ParserShowcaseSample().renderDomCard()) { pixels ->
            expectColorAt(pixels, 650, 10, 0xFF_40_20_7A.toInt())
            expectColorAt(pixels, 850, 100, 0xFF_06_B6_D4.toInt())
            expectColorAt(pixels, 60, 410, 0xFF_15_1C_2E.toInt())
            expectColorAt(pixels, 380, 410, 0xFF_15_1C_2E.toInt())
            expectColorAt(pixels, 680, 410, 0xFF_15_1C_2E.toInt())
            assertTrue(
                countPixelsNear(
                    pixels = pixels,
                    rect = Rect.makeLTRB(830f, 48f, 945f, 96f),
                    expectedColor = 0xFF_22_C5_5E.toInt(),
                    channelTolerance = 64,
                ) >= 30,
                "状态徽章区域应保留绿色边框与文字",
            )
            val titleInkPixels = countPixelsNear(
                pixels = pixels,
                rect = Rect.makeLTRB(45f, 165f, 550f, 285f),
                expectedColor = 0xFF_F8_FA_FC.toInt(),
                channelTolerance = 32,
            )
            assertTrue(
                titleInkPixels > 4_000,
                "解析器卡片应保留白色主标题文字，实际墨迹像素数为 $titleInkPixels",
            )
            val descriptionInkPixels = countPixelsDifferentFrom(
                pixels = pixels,
                rect = Rect.makeLTRB(45f, 288f, 590f, 316f),
                backgroundColor = 0xFF_07_0B_16.toInt(),
                channelTolerance = 8,
            )
            assertTrue(
                descriptionInkPixels > 100,
                "解析器卡片应保留中文说明文字，实际墨迹像素数为 $descriptionInkPixels",
            )
        }
    }

    private fun Element.walk(): Sequence<Element> = sequence {
        yield(this@walk)
        children.forEach { yieldAll(it.walk()) }
    }

    private inline fun <T> withDecodedPixels(bytes: ByteArray, block: (Pixmap) -> T): T {
        val image = Image.makeFromEncoded(bytes)
        val surface = Surface.makeRasterN32Premul(image.width, image.height)
        val bounds = Rect.makeWH(image.width.toFloat(), image.height.toFloat())
        surface.canvas.drawImageRect(image, bounds, bounds, Paint())
        surface.flushAndSubmit()
        val snapshot = surface.makeImageSnapshot()
        val pixels = assertNotNull(snapshot.peekPixels())
        return try {
            block(pixels)
        } finally {
            Reference.reachabilityFence(snapshot)
            Reference.reachabilityFence(surface)
            Reference.reachabilityFence(image)
        }
    }

    private fun countPixelsNear(
        pixels: Pixmap,
        rect: Rect,
        expectedColor: Int,
        channelTolerance: Int,
    ): Int {
        var count = 0
        for (y in rect.top.toInt() until rect.bottom.toInt()) {
            for (x in rect.left.toInt() until rect.right.toInt()) {
                val actual = pixels.getColor(x, y)
                val maxDifference = colorChannelShifts.maxOf { shift ->
                    abs(actual.channel(shift) - expectedColor.channel(shift))
                }
                if (maxDifference <= channelTolerance) count++
            }
        }
        return count
    }

    private fun countPixelsDifferentFrom(
        pixels: Pixmap,
        rect: Rect,
        backgroundColor: Int,
        channelTolerance: Int,
    ): Int {
        var count = 0
        for (y in rect.top.toInt() until rect.bottom.toInt()) {
            for (x in rect.left.toInt() until rect.right.toInt()) {
                val actual = pixels.getColor(x, y)
                val maxDifference = colorChannelShifts.maxOf { shift ->
                    abs(actual.channel(shift) - backgroundColor.channel(shift))
                }
                if (maxDifference > channelTolerance) count++
            }
        }
        return count
    }

    private fun Int.channel(shift: Int): Int = (this shr shift) and 0xFF

    private val colorChannelShifts = intArrayOf(24, 16, 8, 0)

    private companion object {
        const val SHOWCASE_CHINESE_TEXT = "将结构化文本解析为同一棵 Widget 树，再输出 PNG、JPEG 或 WEBP。"
        const val SHOWCASE_CHINESE_GLYPHS = "将结构化文本解析为同一棵树再输出图片字节"
    }
}
