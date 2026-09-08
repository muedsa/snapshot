package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.paint.decoration.BoxDecoration
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.flex.MainAxisSize
import com.muedsa.snapshot.snapshotPixels
import org.jetbrains.skia.Color
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.Pixmap
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ImageFilteredTest {

    // 256x256 画布,四角各 128x128 色块(左上/右下 RED,右上/左下 BLUE);
    // 中央 128x128 区域(64,64)-(192,192)由 center 注入,供"有滤镜 / 无滤镜孪生"复用同一场景。
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

    // 4 条横向色带,每条 108x27,合计 108x108,恰好填满内边距 10 后的内容区 (74,74)-(182,182)。
    private fun Widget.bands4() {
        Column(mainAxisSize = MainAxisSize.MIN) {
            Container(width = 108f, height = 27f, color = Color.GREEN)
            Container(width = 108f, height = 27f, color = Color.YELLOW)
            Container(width = 108f, height = 27f, color = Color.MAGENTA)
            Container(width = 108f, height = 27f, color = Color.CYAN)
        }
    }

    private fun Widget.blurScene(withFilter: Boolean) = cornerStack {
        // 孪生两版只差"是否有 ImageFiltered 包裹",内容盒完全相同。
        fun Widget.content() {
            Container(
                width = 128f,
                height = 128f,
                padding = EdgeInsets.all(10f),
                alignment = BoxAlignment.CENTER,
            ) { bands4() }
        }

        if (withFilter) {
            ImageFiltered(imageFilter = ImageFilter.makeBlur(2f, 2f, FilterTileMode.CLAMP)) { content() }
        } else {
            content()
        }
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

    @Test
    fun blur_test() {
        val with = snapshotPixels { blurScene(true) }
        val without = snapshotPixels { blurScene(false) }

        // 1) 滤镜生效:第一条色带 GREEN 上边缘在 y=74,(100,72) 位于其上方 2px 的背景 RED 中,
        //    模糊半径(σ≈1.65)覆盖此处。实测:有滤镜 0xffbf4000(红绿混),无滤镜 0xffff0000(纯 RED)。
        val blended = with.getColor(100, 72)
        val sharp = without.getColor(100, 72)
        assertNotEquals(
            sharp, blended,
            "有/无滤镜在色带边缘外 (100,72) 应不同:无滤镜=${hex(sharp)} 有滤镜=${hex(blended)}"
        )

        // 2) 边缘混合:该点是模糊过渡色,既非纯背景 RED 也非纯内容 GREEN。
        assertTrue(
            blended != Color.RED && blended != Color.GREEN,
            "(100,72) 应为背景/内容混合色,实际 ${hex(blended)}"
        )

        // 3) 未越界:远离滤镜区域处两版一致 —— 四角 (10,10)/(250,250) 为 RED,(128,5) 为 BLUE;
        //    色带内部纯色区 (100,90)=GREEN(模糊对常量区域恒等)。
        expectColorAt(with, 10, 10, Color.RED)
        expectColorAt(with, 250, 250, Color.RED)
        expectColorAt(with, 128, 5, Color.BLUE)
        assertSamePixel(with, without, 10, 10, "远角")
        assertSamePixel(with, without, 250, 250, "远角")
        assertSamePixel(with, without, 128, 5, "远角")
        assertSamePixel(with, without, 100, 90, "色带内部纯色区")
    }

    // blur_clip 场景:10 条色带(总高 270)远超 108 的内容区,溢出部分由容器
    // clipBehavior=HARD_EDGE 裁掉(迁移前用 TextOverflow.CLIP 的文本裁剪表达同一意图)。
    // 容器尺寸 128x128 不变,故滤镜区域与 blur_test 相同 (64,64)-(192,192)。
    private fun Widget.bandsOverflow() {
        Column(mainAxisSize = MainAxisSize.MIN) {
            repeat(10) { i ->
                Container(
                    width = 108f,
                    height = 27f,
                    color = when (i % 4) {
                        0 -> Color.GREEN
                        1 -> Color.YELLOW
                        2 -> Color.MAGENTA
                        else -> Color.CYAN
                    }
                )
            }
        }
    }

    private fun Widget.blurClipScene(withFilter: Boolean) = cornerStack {
        // 孪生两版只差"是否有 ImageFiltered 包裹",内容盒完全相同。
        // decoration = BoxDecoration() 是 Container 对 clipBehavior != NONE 的硬性要求
        //(见 Container.init 的 check),空装饰本身不绘制任何内容,勿当作无用参数清理。
        fun Widget.content() {
            Container(
                width = 128f,
                height = 128f,
                padding = EdgeInsets.all(10f),
                alignment = BoxAlignment.CENTER,
                clipBehavior = ClipBehavior.HARD_EDGE,
                decoration = BoxDecoration(),
            ) { bandsOverflow() }
        }

        if (withFilter) {
            ImageFiltered(imageFilter = ImageFilter.makeBlur(2f, 2f, FilterTileMode.CLAMP)) { content() }
        } else {
            content()
        }
    }

    @Test
    fun blur_clip_test() {
        val with = snapshotPixels { blurClipScene(true) }
        val without = snapshotPixels { blurClipScene(false) }

        // 色带从内容区顶部 (74,74) 起排,第 5 条(GREEN, i=4)占 y182..209,被容器下边界 y=192 裁掉。
        // (100,190) 位于裁切边缘内 2px,模糊把 GREEN 向裁剪外摊开。
        // 实测:有滤镜 0xff00bf40(绿蓝混),无滤镜 0xff00ff00(纯 GREEN)。
        val blended = with.getColor(100, 190)
        val sharp = without.getColor(100, 190)
        assertNotEquals(
            sharp, blended,
            "有/无滤镜在裁剪边缘 (100,190) 应不同:无滤镜=${hex(sharp)} 有滤镜=${hex(blended)}"
        )
        // 边缘混合色既非纯内容 GREEN,也非裁剪外露出的背景 BLUE。
        assertTrue(
            blended != Color.GREEN && blended != Color.BLUE,
            "(100,190) 应为内容/背景混合色,实际 ${hex(blended)}"
        )

        // 裁剪生效:y=200 已在容器下边界(192)之外,两版都应露出背景 BLUE
        //(若内容未被裁剪,第 5 条色带会一直画到 y=209,此处应为 GREEN)。
        expectColorAt(with, 100, 200, Color.BLUE)
        expectColorAt(without, 100, 200, Color.BLUE)

        // 未越界:远离滤镜区域处两版一致。
        assertSamePixel(with, without, 10, 10, "远角")
        assertSamePixel(with, without, 250, 250, "远角")
    }
}
