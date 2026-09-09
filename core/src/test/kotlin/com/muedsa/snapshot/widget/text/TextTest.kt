package com.muedsa.snapshot.widget.text

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.drawPainter
import com.muedsa.snapshot.paint.text.TextPainter
import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.paint.text.TextStyle
import com.muedsa.snapshot.rootLayout
import com.muedsa.snapshot.snapshotPixels
import com.muedsa.snapshot.testTypeface
import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.Widget
import org.jetbrains.skia.Color
import org.jetbrains.skia.Pixmap
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * 文本渲染的区间/存在性断言。
 *
 * 文本测量受 OS 字体影响,本类**只做量级与墨迹存在性断言**,不锁字形像素;
 * 目检见 artifact 用例与 build/test-results/test-image-outputs/ 下的输出。
 */
class TextTest {

    // ---- 墨迹统计(对反走样稳健:按通道阈值而非精确色值) ----

    private fun Pixmap.countInk(predicate: (Int) -> Boolean): Int {
        var count = 0
        for (y in 0 until info.height) {
            for (x in 0 until info.width) {
                val c = getColor(x, y)
                if (((c shr 24) and 0xFF) != 0 && predicate(c)) count++
            }
        }
        return count
    }

    private fun Pixmap.countReddish(): Int = countInk { c ->
        val r = (c shr 16) and 0xFF
        val g = (c shr 8) and 0xFF
        val b = c and 0xFF
        r > g + 60 && r > b + 60
    }

    private fun Pixmap.countDark(): Int = countInk { c ->
        val r = (c shr 16) and 0xFF
        val g = (c shr 8) and 0xFF
        val b = c and 0xFF
        r < 96 && g < 96 && b < 96
    }

    private fun Pixmap.countWhiteish(): Int = countInk { c ->
        val r = (c shr 16) and 0xFF
        val g = (c shr 8) and 0xFF
        val b = c and 0xFF
        r > 200 && g > 200 && b > 200
    }

    private fun Pixmap.countBlueish(): Int = countInk { c ->
        val r = (c shr 16) and 0xFF
        val g = (c shr 8) and 0xFF
        val b = c and 0xFF
        b > r + 60 && b > g + 60
    }

    @Test
    fun simple_text_test() {
        fun Widget.scene() {
            Text("Hello, world!", style = TextStyle(fontSize = 20f, color = Color.RED, typeface = testTypeface))
        }

        val node = rootLayout { scene() }
        assertTrue(node.size.width > 0f, "文本宽应为正,实际 ${node.size.width}")
        assertTrue(
            node.size.height in 1f..(20f * 3f),
            "fontSize=20 的单行高应在 1..3em,实际 ${node.size.height}"
        )
        // 透明底才能区分"没画"与"画了白"
        val pixmap = snapshotPixels(background = Color.TRANSPARENT) { scene() }
        assertTrue(pixmap.countReddish() > 0, "应存在红色墨迹")
    }

    @Test
    fun text_span_test() {
        fun Widget.scene() {
            RichText {
                TextSpan(text = "Hello, one!", style = TextStyle(fontSize = 20f, typeface = testTypeface))
                TextSpan(text = "Hello, two!", style = TextStyle(color = Color.RED, typeface = testTypeface))
                TextSpan("Hello, three!", style = TextStyle(typeface = testTypeface))
            }
        }

        val node = rootLayout { scene() }
        assertTrue(node.size.width > 0f, "文本宽应为正,实际 ${node.size.width}")
        assertTrue(node.size.height in 1f..(20f * 3f), "单行高应在 1..3em,实际 ${node.size.height}")

        val pixmap = snapshotPixels(background = Color.TRANSPARENT) { scene() }
        // 默认色 BLACK 与红色 span 各自生效
        assertTrue(pixmap.countDark() > 0, "应存在默认色(黑)墨迹")
        assertTrue(pixmap.countReddish() > 0, "应存在红色墨迹")
    }

    @Test
    fun widget_span_test() {
        fun Widget.scene() {
            RichText {
                TextSpan("Hello, one!", style = TextStyle(fontSize = 20f, typeface = testTypeface))
                WidgetSpan { Container(width = 20f, height = 20f, color = Color.BLUE) }
                TextSpan {
                    TextSpan("Hello, two!")
                    WidgetSpan { Container(width = 30f, height = 30f, color = Color.BLUE) }
                    TextSpan(
                        text = "Hello, three!",
                        style = TextStyle(fontSize = 20f, color = Color.RED, typeface = testTypeface)
                    )
                }
            }
        }

        val node = rootLayout { scene() }
        assertTrue(node.size.width > 0f, "文本宽应为正,实际 ${node.size.width}")
        // 30x30 的 WidgetSpan 会参与行高,行高不应小于它
        assertTrue(node.size.height >= 30f, "行高应不小于最高的 WidgetSpan(30),实际 ${node.size.height}")

        val pixmap = snapshotPixels(background = Color.TRANSPARENT) { scene() }
        // 两个蓝块理论上共 20*20+30*30=1300 像素;实测 1202(行高裁剪+抗锯齿)。
        // 下界 800 仍可区分"只画出 20x20 盒"的 ~400 量级。
        val blue = pixmap.countBlueish()
        assertTrue(blue >= 800, "蓝色 WidgetSpan 像素数应 ≥ 800,实际 $blue")
        assertTrue(pixmap.countReddish() > 0, "应存在红色墨迹")
    }

    @Test
    fun style_merge_test() {
        fun Widget.scene() {
            RichText {
                TextSpan(style = TextStyle(fontSize = 15f, color = Color.WHITE, typeface = testTypeface)) {
                    TextSpan("15white")
                    TextSpan(style = TextStyle(fontSize = 30f)) {
                        TextSpan("30white")
                        TextSpan("30red", style = TextStyle(color = Color.RED))
                    }
                    TextSpan("15white")
                }
            }
        }

        val node = rootLayout { scene() }
        assertTrue(node.size.width > 0f, "文本宽应为正,实际 ${node.size.width}")
        assertTrue(
            node.size.height in 1f..(30f * 3f),
            "单行高应在 1..3em(最大字号 30),实际 ${node.size.height}"
        )

        val pixmap = snapshotPixels(background = Color.TRANSPARENT) { scene() }
        // 父级 WHITE 被子级 RED 覆盖、字号 15→30 覆盖,两种墨迹都应出现
        assertTrue(pixmap.countWhiteish() > 0, "应存在白色墨迹(父级样式生效)")
        assertTrue(pixmap.countReddish() > 0, "应存在红色墨迹(子级覆盖生效)")
    }

    @Test
    fun text_samples_artifact() {
        // 仅人眼检视:文本渲染不进 golden,量级断言无法反映字形/排版观感
        val painter = TextPainter(
            text = TextSpan("Hello, world! 你好,世界!", style = TextStyle(fontSize = 30f, typeface = testTypeface))
        ).apply { layout(0f, Float.POSITIVE_INFINITY) }
        drawPainter("widget/text/text_samples", width = painter.width, height = painter.height) { canvas ->
            painter.paint(canvas, offset = Offset.ZERO)
        }
    }
}
