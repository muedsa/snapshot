package com.muedsa.snapshot

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

class ShowcaseSampleContractTest {

    @Test
    fun dashboard_and_poster_are_rich_offline_pngs() {
        val sample = ShowcaseSample()

        assertShowcasePng(
            bytes = sample.renderDashboard(),
            expectedWidth = 1200,
            expectedHeight = 720,
        )
        assertShowcasePng(
            bytes = sample.renderPoster(),
            expectedWidth = 720,
            expectedHeight = 960,
        )
    }

    @Test
    fun dashboard_keeps_hero_status_and_supporting_cards() {
        withDecodedPixels(ShowcaseSample().renderDashboard()) { pixels ->
            assertColorNear(pixels, 60, 200, 0xFF_20_18_45.toInt(), channelTolerance = 3)
            expectColorAt(pixels, 1030, 73, 0xFF_22_C5_5E.toInt())
            expectColorAt(pixels, 980, 290, 0xFF_7C_3A_ED.toInt())
            assertColorNear(pixels, 750, 500, 0xFF_15_1F_35.toInt(), channelTolerance = 3)
            assertTrue(
                countPixelsNear(pixels, Rect.makeLTRB(80f, 215f, 520f, 325f), 0xFF_F8_FA_FC.toInt(), 32) > 4_000,
                "英雄卡片应保留白色标题文字",
            )
            assertTrue(
                countPixelsNear(pixels, Rect.makeLTRB(80f, 330f, 660f, 365f), 0xFF_B8_C2_D8.toInt(), 32) > 500,
                "英雄卡片应保留中文说明文字",
            )
        }
    }

    @Test
    fun poster_keeps_ring_center_and_rotated_labels() {
        withDecodedPixels(ShowcaseSample().renderPoster()) { pixels ->
            assertColorNear(pixels, 360, 405, 0xFF_22_D2_E9.toInt(), channelTolerance = 3)
            assertColorNear(pixels, 360, 500, 0xFF_0F_15_27.toInt(), channelTolerance = 3)
            expectColorAt(pixels, 680, 350, 0xFF_EA_58_0C.toInt())
            expectColorAt(pixels, 120, 740, 0xFF_8B_5C_F6.toInt())
            assertTrue(
                countPixelsNear(pixels, Rect.makeLTRB(40f, 130f, 500f, 225f), 0xFF_F8_FA_FC.toInt(), 32) > 5_000,
                "海报应保留白色主标题文字",
            )
            assertTrue(
                countPixelsNear(pixels, Rect.makeLTRB(40f, 235f, 500f, 295f), 0xFF_A7_F3_D0.toInt(), 32) > 4_000,
                "海报应保留薄荷色副标题文字",
            )
        }
    }

    private fun assertShowcasePng(bytes: ByteArray, expectedWidth: Int, expectedHeight: Int) {
        assertTrue(bytes.size > 10_000, "示例图不能退化为空白或低信息量图片")
        withDecodedPixels(bytes) { pixels ->
            assertEquals(expectedWidth, pixels.info.width)
            assertEquals(expectedHeight, pixels.info.height)
            val sampledColors = buildSet {
                for (y in 0 until expectedHeight step 24) {
                    for (x in 0 until expectedWidth step 24) {
                        add(pixels.getColor(x, y))
                    }
                }
            }
            assertTrue(sampledColors.size >= 24, "示例图应体现渐变、层次和多种强调色")
        }
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

    private fun assertColorNear(
        pixels: Pixmap,
        x: Int,
        y: Int,
        expectedColor: Int,
        channelTolerance: Int,
    ) {
        val actualColor = pixels.getColor(x, y)
        val maxDifference = colorChannelShifts.maxOf { shift ->
            abs(actualColor.channel(shift) - expectedColor.channel(shift))
        }
        assertTrue(
            maxDifference <= channelTolerance,
            "像素（$x, $y）与预期颜色的最大通道差为 $maxDifference，允许值为 $channelTolerance",
        )
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
                val actualColor = pixels.getColor(x, y)
                val matches = colorChannelShifts.all { shift ->
                    abs(actualColor.channel(shift) - expectedColor.channel(shift)) <= channelTolerance
                }
                if (matches) count++
            }
        }
        return count
    }

    private fun Int.channel(shift: Int): Int = (this shr shift) and 0xFF

    private val colorChannelShifts = intArrayOf(24, 16, 8, 0)
}
