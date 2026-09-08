package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.golden
import com.muedsa.snapshot.material.ELEVATION_MAP
import com.muedsa.snapshot.paint.decoration.BorderRadius
import com.muedsa.snapshot.paint.decoration.BoxDecoration
import com.muedsa.snapshot.snapshotPixels
import org.jetbrains.skia.Color
import org.jetbrains.skia.Pixmap
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class DecoratedBoxTest {

    // 200x200 白色圆角盒(半径 100 → 圆形):锐边几何,可用整图 golden。
    // 背景取 TRANSPARENT(与迁移前的透明底快照一致),否则白盒压白底会得到全白基准,失去回归意义。
    private fun Widget.borderRadiusScene() {
        DecoratedBox(
            decoration = BoxDecoration(
                color = Color.WHITE,
                borderRadius = BorderRadius.circular(100f),
            )
        ) {
            Container(width = 200f, height = 200f)
        }
    }

    @Test
    fun border_radius_golden() {
        val pixmap = snapshotPixels(background = Color.TRANSPARENT) { borderRadiusScene() }
        // 实测:圆角外 (2,2)=0x00000000 透明,中心 (100,100)=0xffffffff 白。
        expectColorAt(pixmap, 2, 2, Color.TRANSPARENT)
        expectColorAt(pixmap, 100, 100, Color.WHITE)
        golden("widget/decorated_box/borderRadius_w200_r100", background = Color.TRANSPARENT) {
            borderRadiusScene()
        }
    }

    // 阴影场景:500x300 白底画布,居中 200x100 白盒(全局占 x150..350, y100..200)。
    // 阴影来自 BoxDecoration.boxShadow = ELEVATION_MAP[elevation],白盒与白底只让阴影外露。
    private fun Widget.elevationScene(elevation: Int) {
        Container(width = 500f, height = 300f, color = Color.WHITE, alignment = BoxAlignment.CENTER) {
            DecoratedBox(
                decoration = BoxDecoration(
                    color = Color.WHITE,
                    boxShadow = ELEVATION_MAP[elevation],
                )
            ) {
                Container(width = 200f, height = 100f)
            }
        }
    }

    /** 亮度 = RGB 三通道算术均值(0..255),用于比较阴影深浅,不做整色断言。 */
    private fun Pixmap.luminanceAt(x: Int, y: Int): Int {
        val c = getColor(x, y)
        return ((c shr 16 and 0xFF) + (c shr 8 and 0xFF) + (c and 0xFF)) / 3
    }

    @Test
    fun elevation_shadow_sampling() {
        val elevations = listOf(1, 4, 12, 24)
        val pixmaps = elevations.map { snapshotPixels { elevationScene(it) } }

        // 采样点 (250,210) 位于盒底(y=200)正下方 10px、水平居中处,实测亮度:
        //   e=1 → 255, e=4 → 252, e=12 → 216, e=24 → 201(2026-09-08 Windows 本机栅格化)
        // 阴影随 elevation 变深是结构性的(偏移/模糊/扩散都增大),故按"非增"逐对比较;
        // 不用具体数值,避免跨平台栅格化差异导致闪断。
        val luminances = pixmaps.map { it.luminanceAt(250, 210) }
        for (i in 0 until elevations.size - 1) {
            assertTrue(
                luminances[i] >= luminances[i + 1],
                "阴影亮度应随 elevation 非增:e=${elevations[i]} 亮度 ${luminances[i]} " +
                    "但 e=${elevations[i + 1]} 亮度 ${luminances[i + 1]}(采样点 (250,210))"
            )
        }
        // 防止"阴影完全失效 → 全白"也能通过非增断言:最大档必须明显暗于最小档。
        assertTrue(
            luminances.last() < luminances.first(),
            "elevation=24 的阴影应明显暗于 elevation=1:亮度 ${luminances.last()} vs ${luminances.first()}"
        )

        pixmaps.forEach { pixmap ->
            // (250,5) 距盒顶(y=100)95px,远在阴影外 → 恒为白底。
            expectColorAt(pixmap, 250, 5, Color.WHITE)
            // 盒中心 (250,150) 为不透明白盒 → 恒为白。
            expectColorAt(pixmap, 250, 150, Color.WHITE)
        }
    }

    // 回归:RenderDecoratedBox 曾无条件多调一次 super.paint,导致子盒被绘制两次,
    // 半透明子盒被双重合成(白底 + 50% 蓝 → 0xff3f3fff 而非正确的 0xff7f7fff)。
    @Test
    fun translucent_child_composited_once() {
        val pixmap = snapshotPixels(background = Color.TRANSPARENT) {
            DecoratedBox(decoration = BoxDecoration(color = Color.WHITE)) {
                Container(width = 100f, height = 100f, color = 0x800000FF.toInt())
            }
        }
        val c = pixmap.getColor(50, 50)
        val r = (c shr 16) and 0xFF
        val g = (c shr 8) and 0xFF
        val b = c and 0xFF
        // 50% 蓝叠在不透明白上 → 各通道 ≈ (127,127,255);容差 1 对抗取整差异。
        assertTrue(
            abs(r - 127) <= 1 && abs(g - 127) <= 1 && abs(b - 255) <= 1,
            "半透明子盒应只合成一次(期望 R≈127 G≈127 B=255),实际 R=$r G=$g B=$b " +
                "(0x${c.toUInt().toString(16).padStart(8, '0')})"
        )
    }

    @Test
    fun elevation_zero_no_shadow() {
        val pixmap = snapshotPixels { elevationScene(0) }
        // elevation=0 是空阴影数组:(250,201) 紧贴盒底下方、(250,210) 阴影带内,都应保持白。
        expectColorAt(pixmap, 250, 201, Color.WHITE)
        expectColorAt(pixmap, 250, 210, Color.WHITE)
    }
}
