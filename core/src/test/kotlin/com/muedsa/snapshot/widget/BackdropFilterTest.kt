package com.muedsa.snapshot.widget

import com.muedsa.geometry.AlignmentDirectional
import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.rendering.flex.MainAxisSize
import com.muedsa.snapshot.snapshotPixels
import org.jetbrains.skia.Color
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.PathBuilder
import org.jetbrains.skia.Pixmap
import org.jetbrains.skia.Rect
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BackdropFilterTest {

    // 256x256 画布,四角各 128x128 色块(左上/右下 RED,右上/左下 BLUE);
    // 中央 128x128 区域(64,64)-(192,192)由 center 注入,供"有/无 BackdropFilter 孪生"复用。
    private fun Widget.cornerStack(center: Widget.() -> Unit) {
        SizedBox(width = 256f, height = 256f) {
            Stack {
                Positioned(top = 0f, left = 0f) { Container(width = 128f, height = 128f, color = Color.RED) }
                Positioned(top = 0f, right = 0f) { Container(width = 128f, height = 128f, color = Color.BLUE) }
                Positioned(bottom = 0f, left = 0f) { Container(width = 128f, height = 128f, color = Color.BLUE) }
                Positioned(bottom = 0f, right = 0f) { Container(width = 128f, height = 128f, color = Color.RED) }
                Positioned(top = 64f, right = 64f) { center() }
            }
        }
    }

    // 3 条横向色带,每条 108x20(合计 108x60),在内边距 10 后的内容区里居中 → 占 (74,98)-(182,158)。
    private fun Widget.bands3() {
        Column(mainAxisSize = MainAxisSize.MIN) {
            Container(width = 108f, height = 20f, color = Color.GREEN)
            Container(width = 108f, height = 20f, color = Color.YELLOW)
            Container(width = 108f, height = 20f, color = Color.MAGENTA)
        }
    }

    private fun Widget.blurScene(withFilter: Boolean, clipRect: Boolean) = cornerStack {
        // 孪生两版只差"是否有 BackdropFilter 包裹",内容盒完全相同。
        fun Widget.content() {
            Container(
                width = 128f,
                height = 128f,
                padding = EdgeInsets.all(10f),
                alignment = BoxAlignment.CENTER,
            ) { bands3() }
        }

        fun Widget.filtered() {
            if (withFilter) {
                BackdropFilter(imageFilter = ImageFilter.makeBlur(25f, 25f, FilterTileMode.CLAMP)) { content() }
            } else {
                content()
            }
        }

        if (clipRect) ClipRect { filtered() } else filtered()
    }

    private fun hex(color: Int): String = "0x" + color.toUInt().toString(16).padStart(8, '0')

    private fun assertSamePixel(a: Pixmap, b: Pixmap, x: Int, y: Int, what: String) {
        val ca = a.getColor(x, y)
        val cb = b.getColor(x, y)
        assertTrue(
            ca == cb,
            "$what:($x,$y) 有滤镜=${hex(ca)} 无滤镜=${hex(cb)} 应一致"
        )
    }

    // blur_test:BackdropFilter 外包 ClipRect,滤镜范围被裁到 (64,64)-(192,192)。
    @Test
    fun blur_test() {
        val with = snapshotPixels { blurScene(true, clipRect = true) }
        val without = snapshotPixels { blurScene(false, clipRect = true) }

        // 区域内点 (100,175):在滤镜范围 (64,64)-(192,192) 内、色带(占 y98..158)之下,
        // 直接看到被模糊的背景(该处原为左下 BLUE 块)。
        // 实测:有滤镜 0xff2500da(红蓝混),无滤镜 0xff0000ff(纯 BLUE)。
        val blurred = with.getColor(100, 175)
        val sharp = without.getColor(100, 175)
        assertNotEquals(
            sharp, blurred,
            "有/无 BackdropFilter 在滤镜区域内 (100,175) 应不同:无滤镜=${hex(sharp)} 有滤镜=${hex(blurred)}"
        )
        assertTrue(
            blurred != Color.BLUE && blurred != Color.RED,
            "(100,175) 应为模糊混合色,实际 ${hex(blurred)}"
        )

        // 区域外点:四角 (20,20) 为 RED;滤镜范围下方 (100,200) 为 BLUE —— 两版一致。
        expectColorAt(with, 20, 20, Color.RED)
        expectColorAt(with, 100, 200, Color.BLUE)
        assertSamePixel(with, without, 20, 20, "滤镜区域外")
        assertSamePixel(with, without, 100, 200, "滤镜区域外")
    }

    // blur_2_test:不带 ClipRect 的变体。BackdropFilterLayer 未裁剪,整幅背景图都会被重绘模糊,
    // 因此"区域外"只能取远离色块边界的纯色深处(模糊对常量区域恒等)。
    @Test
    fun blur_2_test() {
        val with = snapshotPixels { blurScene(true, clipRect = false) }
        val without = snapshotPixels { blurScene(false, clipRect = false) }

        val blurred = with.getColor(100, 175)
        val sharp = without.getColor(100, 175)
        assertNotEquals(
            sharp, blurred,
            "有/无 BackdropFilter 在滤镜区域内 (100,175) 应不同:无滤镜=${hex(sharp)} 有滤镜=${hex(blurred)}"
        )

        // (20,20) 位于左上 RED 块深处(距色块边界 108px、距画布边 20px),模糊不改变常量区域。
        expectColorAt(with, 20, 20, Color.RED)
        assertSamePixel(with, without, 20, 20, "远离色块边界的纯色区")
    }

    // ---------------- blur_3:本地色带网格替代外网 DecorationImage ----------------

    // 500x500 背景:10 条横向色带(每条 500x50),按 6 色循环。
    private fun Widget.outerGrid() {
        Column(mainAxisSize = MainAxisSize.MIN) {
            repeat(10) { i ->
                Container(
                    width = 500f,
                    height = 50f,
                    color = when (i % 6) {
                        0 -> Color.RED
                        1 -> Color.GREEN
                        2 -> Color.BLUE
                        3 -> Color.YELLOW
                        4 -> Color.MAGENTA
                        else -> Color.CYAN
                    }
                )
            }
        }
    }

    // 中央 250x250 滤镜区内容:4 条色带,每条 230x40,居中占 (135,170)-(365,330)。
    private fun Widget.innerBands() {
        Column(mainAxisSize = MainAxisSize.MIN) {
            Container(width = 230f, height = 40f, color = Color.BLUE)
            Container(width = 230f, height = 40f, color = Color.YELLOW)
            Container(width = 230f, height = 40f, color = Color.MAGENTA)
            Container(width = 230f, height = 40f, color = Color.CYAN)
        }
    }

    // 保留原结构:45° 起、180° 扫掠的弧线 ClipPath(取 y>=x 的下半区)+ 内层 ClipPath(裁到子盒边界)
    // + BackdropFilter(blur 25);仅把外网 DecorationImage 换成上面的本地色带网格。
    private fun Widget.blur3Scene(withFilter: Boolean) = SizedBox(width = 500f, height = 500f) {
        ClipPath(
            clipper = {
                PathBuilder().apply {
                    arcTo(
                        oval = Rect.makeWH(it.width, it.height),
                        startAngle = 45f,
                        sweepAngle = 180f,
                        forceMoveTo = true
                    )
                }.detach()
            }
        ) {
            Container(
                width = 500f,
                height = 500f,
                alignment = BoxAlignment.CENTER,
            ) {
                Stack(alignment = AlignmentDirectional.CENTER) {
                    outerGrid()
                    if (withFilter) {
                        ClipPath {
                            BackdropFilter(imageFilter = ImageFilter.makeBlur(25f, 25f, FilterTileMode.CLAMP)) {
                                Container(
                                    width = 250f,
                                    height = 250f,
                                    padding = EdgeInsets.all(10f),
                                    alignment = BoxAlignment.CENTER,
                                ) { innerBands() }
                            }
                        }
                    } else {
                        ClipPath {
                            Container(
                                width = 250f,
                                height = 250f,
                                padding = EdgeInsets.all(10f),
                                alignment = BoxAlignment.CENTER,
                            ) { innerBands() }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun blur_3_test() {
        val with = snapshotPixels { blur3Scene(true) }
        val without = snapshotPixels { blur3Scene(false) }

        // 滤镜区为 (125,125)-(375,375)(中央 250x250);内层色带占 (135,170)-(365,330)。
        // 采样点 (250,345):在滤镜区内、色带下方、且位于 45° 弧线裁剪保留的下半区(y>=x)。
        // 该处背景是第 7 条色带 RED(300..350)与第 8 条 GREEN(350..400)的交界附近。
        // 实测:有滤镜 0xff8b7208(红绿混),无滤镜 0xffff0000(纯 RED)。
        val blurred = with.getColor(250, 345)
        val sharp = without.getColor(250, 345)
        assertNotEquals(
            sharp, blurred,
            "有/无 BackdropFilter 在滤镜区域内 (250,345) 应不同:无滤镜=${hex(sharp)} 有滤镜=${hex(blurred)}"
        )
        assertTrue(
            blurred != Color.RED && blurred != Color.GREEN,
            "(250,345) 应为模糊混合色,实际 ${hex(blurred)}"
        )

        // 区域外点 (100,425):在 45° 弧线裁剪内、滤镜区(x>=125)之外 → 两版一致的纯 BLUE 色带。
        expectColorAt(with, 100, 425, Color.BLUE)
        assertSamePixel(with, without, 100, 425, "滤镜区域外")
    }
}
