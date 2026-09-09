package com.muedsa.snapshot.widget

import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.snapshotPixels
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Color
import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.paragraph.Direction
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ColorFilteredTest {

    // 六色块横排 50x100(总 300x100);块中心采样点 x = 25 + 50i, y = 50。
    private val blockColors = intArrayOf(
        Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW
    )

    private fun Widget.colorGrid() {
        Row(textDirection = Direction.LTR) {
            blockColors.forEach { c -> Container(width = 50f, height = 100f, color = c) }
        }
    }

    private fun Widget.colorFilteredScene(filter: ColorFilter) {
        ColorFiltered(colorFilter = filter) { colorGrid() }
    }

    private fun sampleX(index: Int): Int = 25 + index * 50

    @Test
    fun red_modulate_multiplies_channels() {
        // MODULATE 逐通道相乘:结果 = src * RED = (R, 0, 0)。
        // 实测:红/品红/黄 → 0xffff0000,绿/蓝/青 → 0xff000000。
        val filter = ColorFilter.makeBlend(Color.RED, BlendMode.MODULATE)
        val pixmap = snapshotPixels { colorFilteredScene(filter) }
        val expected = intArrayOf(
            0xffff0000.toInt(), 0xff000000.toInt(), 0xff000000.toInt(),
            0xff000000.toInt(), 0xffff0000.toInt(), 0xffff0000.toInt(),
        )
        expected.forEachIndexed { i, color -> expectColorAt(pixmap, sampleX(i), 50, color) }
    }

    @Test
    fun red_modulate_differs_from_unfiltered_twin() {
        // 孪生互比:同一场景有/无滤镜,绿块中心必然不同
        val filter = ColorFilter.makeBlend(Color.RED, BlendMode.MODULATE)
        val plain = snapshotPixels { colorGrid() }
        val filtered = snapshotPixels { colorFilteredScene(filter) }
        assertNotEquals(plain.getColor(sampleX(1), 50), filtered.getColor(sampleX(1), 50))
    }

    @Test
    fun gray_saturation_produces_rec601_luma() {
        // SATURATION(灰) 把饱和度置 0 → 等通道灰度。
        // 实测灰度精确等于 0.30R + 0.59G + 0.11B 取整:红 76 / 绿 150 / 蓝 28 / 青 178 / 品红 105 / 黄 227。
        // 系数取自 skiko 当前 SATURATION 矩阵实测;若升级 skiko 改变系数,需同步更新期望值。
        val filter = ColorFilter.makeBlend(0xFF9E9E9E.toInt(), BlendMode.SATURATION)
        val pixmap = snapshotPixels { colorFilteredScene(filter) }
        val expectedGray = intArrayOf(76, 150, 28, 178, 105, 227)
        blockColors.forEachIndexed { i, source ->
            val actual = pixmap.getColor(sampleX(i), 50)
            val r = (actual shr 16) and 0xFF
            val g = (actual shr 8) and 0xFF
            val b = actual and 0xFF
            assertTrue(
                abs(r - g) <= 1 && abs(g - b) <= 1,
                "块$i(源 0x${source.toUInt().toString(16)})应为等通道灰度,实际 R=$r G=$g B=$b"
            )
            assertTrue(
                abs(r - expectedGray[i]) <= 2,
                "块$i 灰度应≈${expectedGray[i]},实际 R=$r G=$g B=$b"
            )
        }
    }
}
