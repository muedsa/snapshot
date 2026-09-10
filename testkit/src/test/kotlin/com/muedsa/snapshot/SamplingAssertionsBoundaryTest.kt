package com.muedsa.snapshot

import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Pixmap
import org.jetbrains.skia.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * 采样/区域断言的**边界语义**单测。
 *
 * 这些语义(矩形的"左含右开"、分数坐标落到哪些像素、alpha 的分类门槛、通道容差的端点、
 * 空区域的行为)此前只被使用方间接覆盖,此处直接锁定,避免改动实现时无声改变口径。
 *
 * 画布用 `painterPixels` 以整数坐标绘制纯色矩形构造,不依赖字体/抗锯齿,故跨平台确定。
 */
class SamplingAssertionsBoundaryTest {

    /** 4x4 画布:除 (1,1)-(2,2) 四个像素为纯红外,其余全透明。 */
    private fun redSquareOnTransparent(): Pixmap =
        painterPixels(width = 4f, height = 4f, background = Color.TRANSPARENT) { canvas ->
            canvas.drawRect(
                Rect.makeXYWH(1f, 1f, 2f, 2f),
                Paint().apply { color = Color.RED }
            )
        }

    @Test
    fun fractional_rect_maps_with_left_inclusive_right_exclusive() {
        // left=0.5 → 起始列 ceil(0.5)=1;right=2.5 → 结束列 ceil(2.5)-1=2,即列 1..2、行 1..2
        val stats = redSquareOnTransparent().regionStats(Rect.makeXYWH(0.5f, 0.5f, 2f, 2f))
        assertEquals(4, stats.pixelCount, "分数边界应恰好落在 2x2=4 个像素上")
        assertEquals(4, stats.opaqueCount, "四个像素都是不透明红")
        assertEquals(Color.RED, stats.averageColor, "四个像素平均色应为纯红")
    }

    @Test
    fun integer_rect_half_open_at_right_edge() {
        // left=1、right=3 → 列 1..2(不含第 3 列)
        val stats = redSquareOnTransparent().regionStats(Rect.makeXYWH(1f, 1f, 2f, 2f))
        assertEquals(4, stats.pixelCount, "右边界不含:应为 2x2 而非 3x3")
    }

    @Test
    fun region_outside_image_yields_zero_stats() {
        val stats = redSquareOnTransparent().regionStats(Rect.makeXYWH(10f, 10f, 5f, 5f))
        assertEquals(0, stats.pixelCount)
        assertEquals(0, stats.opaqueCount)
        assertEquals(0, stats.transparentCount)
        assertEquals(0, stats.averageColor)
    }

    @Test
    fun empty_region_yields_zero_stats_and_assertions_pass() {
        val pixmap = redSquareOnTransparent()
        val empty = Rect.makeXYWH(1f, 1f, 0f, 0f)
        assertEquals(0, pixmap.regionStats(empty).pixelCount, "空矩形无像素落入")
        // 空区域不得"夹取到边界单像素";三种区域断言都应直接通过
        expectRegionOpaque(pixmap, empty)
        expectRegionTransparent(pixmap, empty)
        expectRegionUniform(pixmap, empty, Color.RED, channelTolerance = 0)
    }

    @Test
    fun alpha_classification_boundary() {
        // 左像素 alpha≈128(半透明),右像素不透明
        val pixmap = painterPixels(width = 2f, height = 1f, background = Color.TRANSPARENT) { canvas ->
            canvas.drawRect(
                Rect.makeXYWH(0f, 0f, 1f, 1f),
                Paint().apply { color = Color.withA(Color.RED, 128) }
            )
            canvas.drawRect(
                Rect.makeXYWH(1f, 0f, 1f, 1f),
                Paint().apply { color = Color.RED }
            )
        }
        val stats = pixmap.regionStats(Rect.makeXYWH(0f, 0f, 2f, 1f))
        assertEquals(2, stats.pixelCount)
        assertEquals(1, stats.opaqueCount, "仅 alpha==255 计入不透明")
        assertEquals(0, stats.transparentCount, "0<alpha<255 不计入透明")

        // 半透明像素既不算"不透明"也不算"透明":
        // 含它的区域里无全透明像素 → expectRegionOpaque 通过
        expectRegionOpaque(pixmap, Rect.makeXYWH(0f, 0f, 2f, 1f))
        // 只含半透明像素的区域里无不透明像素 → expectRegionTransparent 通过
        expectRegionTransparent(pixmap, Rect.makeXYWH(0f, 0f, 1f, 1f))
        // 含不透明像素的区域 → expectRegionTransparent 失败
        assertFailsWith<AssertionError>("含不透明像素时 transparent 断言应失败") {
            expectRegionTransparent(pixmap, Rect.makeXYWH(1f, 0f, 1f, 1f))
        }
    }

    @Test
    fun uniform_channel_tolerance_endpoint() {
        // 两像素仅蓝通道相差 1:容差 1 通过(含端点)、容差 0 失败
        val pixmap = painterPixels(width = 2f, height = 1f) { canvas ->
            canvas.drawRect(Rect.makeXYWH(0f, 0f, 1f, 1f), Paint().apply { color = 0xFF808080.toInt() })
            canvas.drawRect(Rect.makeXYWH(1f, 0f, 1f, 1f), Paint().apply { color = 0xFF808081.toInt() })
        }
        val region = Rect.makeXYWH(0f, 0f, 2f, 1f)
        expectRegionUniform(pixmap, region, 0xFF808080.toInt(), channelTolerance = 1)
        assertFailsWith<AssertionError>("通道差 1 大于容差 0 时应失败") {
            expectRegionUniform(pixmap, region, 0xFF808080.toInt(), channelTolerance = 0)
        }
    }

    @Test
    fun color_at_asserts_exact_equality() {
        val pixmap = redSquareOnTransparent()
        expectColorAt(pixmap, 1, 1, Color.RED)
        assertFailsWith<AssertionError>("非预期颜色应失败") {
            expectColorAt(pixmap, 0, 0, Color.RED)
        }
    }
}
