package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.assertGlobalRect
import com.muedsa.snapshot.assertSize
import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.findType
import com.muedsa.snapshot.rootLayout
import com.muedsa.snapshot.snapshotPixels
import com.muedsa.snapshot.rendering.box.RenderColoredBox
import org.jetbrains.skia.Color
import kotlin.test.Test

class ColoredBoxTest {

    // 灰:与下方颜色集互异,保证 findType 不会先命中 Container 自带的外层 RenderColoredBox
    private val background = 0xFF808080.toInt()

    private fun assertScene(color: Int) {
        val root = rootLayout {
            Container(
                width = 200f,
                height = 200f,
                alignment = BoxAlignment.CENTER,
                color = background,
            ) {
                SizedBox(width = 100f, height = 100f) {
                    ColoredBox(color = color)
                }
            }
        }
        root.assertSize(200f, 200f)
        val inner = checkNotNull(root.findType<RenderColoredBox> { it.color == color }) {
            "找不到 color=$color 的 RenderColoredBox"
        }
        inner.assertGlobalRect(50f, 50f, 100f, 100f)

        val pixmap = snapshotPixels {
            Container(
                width = 200f,
                height = 200f,
                alignment = BoxAlignment.CENTER,
                color = background,
            ) {
                SizedBox(width = 100f, height = 100f) {
                    ColoredBox(color = color)
                }
            }
        }
        expectColorAt(pixmap, 1, 1, background)
        expectColorAt(pixmap, 198, 198, background)
        expectColorAt(pixmap, 100, 100, color)
    }

    @Test
    fun color_layout_and_pixel_test() {
        val colors = intArrayOf(
            Color.BLACK, Color.WHITE, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA
        )
        colors.forEach(::assertScene)
    }
}
