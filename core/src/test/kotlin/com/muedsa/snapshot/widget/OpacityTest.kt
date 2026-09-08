package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.golden
import com.muedsa.snapshot.snapshotPixels
import org.jetbrains.skia.Color
import org.jetbrains.skia.Pixmap
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class OpacityTest {

    // 300x300 黄底、居中 200x200 绿块:绿块占 (50,50)-(250,250)。
    // 中心 (150,150) 反映 Opacity 后的合成色;四角 (1,1)/(298,298) 在黄底内。
    private fun Widget.opacityScene(opacity: Float) {
        Container(width = 300f, height = 300f, alignment = BoxAlignment.CENTER, color = Color.YELLOW) {
            Opacity(opacity = opacity) {
                Container(width = 200f, height = 200f, color = Color.GREEN)
            }
        }
    }

    private fun Pixmap.assertCornersYellow() {
        expectColorAt(this, 1, 1, Color.YELLOW)
        expectColorAt(this, 298, 298, Color.YELLOW)
    }

    @Test
    fun opacity_1_fully_visible() {
        val pixmap = snapshotPixels { opacityScene(1f) }
        pixmap.assertCornersYellow()
        expectColorAt(pixmap, 150, 150, Color.GREEN)
        golden("widget/opacity/opacity_1") { opacityScene(1f) }
    }

    @Test
    fun opacity_0_fully_hidden() {
        val pixmap = snapshotPixels { opacityScene(0f) }
        pixmap.assertCornersYellow()
        expectColorAt(pixmap, 150, 150, Color.YELLOW)
        golden("widget/opacity/opacity_0") { opacityScene(0f) }
    }

    @Test
    fun opacity_0_5_blends_center() {
        val pixmap = snapshotPixels { opacityScene(0.5f) }
        pixmap.assertCornersYellow()
        val center = pixmap.getColor(150, 150)
        // 绿(0,255,0)与黄(255,255,0)在 0.5 叠加 → 各通道 ≈ (127,255,0),纯色确定场景可按通道容差断言
        val tolerance = 2
        val r = (center shr 16) and 0xFF
        val g = (center shr 8) and 0xFF
        val b = center and 0xFF
        assertTrue(
            abs(r - 127) <= tolerance && abs(g - 255) <= tolerance && abs(b - 0) <= tolerance,
            "opacity=0.5 中心应为绿黄半合成色 R≈127 G≈255 B≈0,实际 R=$r G=$g B=$b " +
                "(0x${center.toUInt().toString(16).padStart(8, '0')})"
        )
        golden("widget/opacity/opacity_0_5") { opacityScene(0.5f) }
    }
}
