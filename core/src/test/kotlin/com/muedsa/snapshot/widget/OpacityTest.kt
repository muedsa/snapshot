package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.golden
import com.muedsa.snapshot.snapshotPixels
import org.jetbrains.skia.Color
import org.jetbrains.skia.Pixmap
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
        assertTrue(
            center != Color.GREEN && center != Color.YELLOW,
            "opacity=0.5 中心应为绿黄之间的混合色,实际 0x${center.toUInt().toString(16).padStart(8, '0')}"
        )
        golden("widget/opacity/opacity_0_5") { opacityScene(0.5f) }
    }
}
