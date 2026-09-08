package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.golden
import com.muedsa.snapshot.material.ELEVATION_MAP
import com.muedsa.snapshot.paint.decoration.BorderRadius
import com.muedsa.snapshot.paint.decoration.BoxDecoration
import com.muedsa.snapshot.regionStats
import com.muedsa.snapshot.rendering.box.DecorationPosition
import com.muedsa.snapshot.snapshotPixels
import org.jetbrains.skia.Color
import org.jetbrains.skia.Pixmap
import org.jetbrains.skia.Rect
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

    /** 区域亮度 = 区域平均色的 RGB 三通道算术均值(0..255)。 */
    private fun Pixmap.regionLuminance(rect: Rect): Int {
        val avg = regionStats(rect).averageColor
        return ((avg shr 16 and 0xFF) + (avg shr 8 and 0xFF) + (avg and 0xFF)) / 3
    }

    @Test
    fun elevation_shadow_sampling() {
        val elevations = listOf(1, 4, 12, 24)
        val pixmaps = elevations.map { snapshotPixels { elevationScene(it) } }

        // 采样矩形 (240,203)-(260,213):盒底 y=200 正下方 3..13px、水平居中 20px 宽。
        // 用区域平均而非单像素,放大相邻档位差值并降低跨平台栅格化敏感度;实测区域平均亮度:
        //   e=1 → 254, e=4 → 241, e=12 → 206, e=24 → 194(2026-09-08 Windows 本机栅格化)
        // 阴影随 elevation 变深是结构性的(偏移/模糊/扩散都增大),故按"非增"逐对比较;
        // 不用具体数值,避免跨平台差异导致闪断。
        val shadowRect = Rect.makeLTRB(240f, 203f, 260f, 213f)
        val luminances = pixmaps.map { it.regionLuminance(shadowRect) }
        for (i in 0 until elevations.size - 1) {
            assertTrue(
                luminances[i] >= luminances[i + 1],
                "阴影亮度应随 elevation 非增:e=${elevations[i]} 亮度 ${luminances[i]} " +
                    "但 e=${elevations[i + 1]} 亮度 ${luminances[i + 1]}($shadowRect)"
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
            // 盒中心 (250,150):白盒压白底,此处只断言"没被阴影污染",属有意的弱断言
            //(子盒无 color,本来就画不出东西;阴影才是本用例的被测对象)。
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

    // 回归(同一 bug 的 FOREGROUND 分支):前景装饰必须画在子盒之上。
    // 若顺序反了(子盒后画),不透明的子盒会盖住前景装饰。
    @Test
    fun foreground_decoration_paints_above_child() {
        val pixmap = snapshotPixels {
            DecoratedBox(
                decoration = BoxDecoration(
                    color = Color.RED,
                    borderRadius = BorderRadius.circular(20f),
                ),
                position = DecorationPosition.FOREGROUND,
            ) {
                Container(width = 100f, height = 100f, color = Color.BLUE)
            }
        }
        // 实测:中心 (50,50)=0xffff0000 前景 RED(盖住子盒);
        // 圆角外 (2,2)/(98,98)=0xff0000ff 子盒 BLUE(前景圆角未覆盖处仍露出子盒)。
        expectColorAt(pixmap, 50, 50, Color.RED)
        expectColorAt(pixmap, 2, 2, Color.BLUE)
        expectColorAt(pixmap, 98, 98, Color.BLUE)
    }

    @Test
    fun elevation_zero_no_shadow() {
        val pixmap = snapshotPixels { elevationScene(0) }
        // elevation=0 是空阴影数组:(250,201) 紧贴盒底下方、(250,210) 阴影带内,都应保持白。
        // 同样是白盒压白底下的弱断言,仅用于锁定"空阴影数组不产生任何阴影"。
        expectColorAt(pixmap, 250, 201, Color.WHITE)
        expectColorAt(pixmap, 250, 210, Color.WHITE)
    }
}
