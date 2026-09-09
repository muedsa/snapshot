package com.muedsa.snapshot.widget

import com.muedsa.snapshot.assertApproxEq
import com.muedsa.snapshot.assertSize
import com.muedsa.snapshot.golden
import com.muedsa.snapshot.paint.text.TextPainter
import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.paint.text.TextStyle
import com.muedsa.snapshot.rendering.LayoutNode
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.flex.CrossAxisAlignment
import com.muedsa.snapshot.rootLayout
import com.muedsa.snapshot.testTypeface
import com.muedsa.snapshot.widget.text.RichText
import org.jetbrains.skia.Color
import org.jetbrains.skia.paragraph.BaselineMode
import org.jetbrains.skia.paragraph.Direction
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class RowParserTest {

    private companion object {
        const val EPS = 0.01f
    }

    // 三个纯色盒 100x100 / 300x300 / 200x200;main 轴位置恒为 0 / 100 / 400(实测)。
    private fun Widget.rowScene(crossAxisAlignment: CrossAxisAlignment) {
        if (crossAxisAlignment == CrossAxisAlignment.STRETCH) {
            // STRETCH 需要有限的 cross 轴约束才能观察到拉伸;
            // LimitedBox(1000x1000) 同时把 Row 撑成 1000x1000(mainAxisSize=MAX + 有限 maxWidth)。
            LimitedBox(maxWidth = 1000f, maxHeight = 1000f) {
                Row(crossAxisAlignment = crossAxisAlignment, textDirection = Direction.LTR) {
                    Container(width = 100f, height = 100f, color = Color.RED)
                    Container(width = 300f, height = 300f, color = Color.GREEN)
                    Container(width = 200f, height = 200f, color = Color.BLUE)
                }
            }
        } else {
            Row(
                crossAxisAlignment = crossAxisAlignment,
                textDirection = Direction.LTR,
                textBaseline = if (crossAxisAlignment == CrossAxisAlignment.BASELINE) BaselineMode.ALPHABETIC else null
            ) {
                Container(width = 100f, height = 100f, color = Color.RED)
                Container(width = 300f, height = 300f, color = Color.GREEN)
                Container(width = 200f, height = 200f, color = Color.BLUE)
            }
        }
    }

    private fun rowNode(crossAxisAlignment: CrossAxisAlignment): LayoutNode {
        val root = rootLayout { rowScene(crossAxisAlignment) }
        return if (crossAxisAlignment == CrossAxisAlignment.STRETCH) root.children[0] else root
    }

    private fun LayoutNode.assertMainAxisStarts() {
        assertApproxEq(children[0].rect.left, 0f, EPS)
        assertApproxEq(children[1].rect.left, 100f, EPS)
        assertApproxEq(children[2].rect.left, 400f, EPS)
    }

    private fun LayoutNode.assertCrossTops(y0: Float, y1: Float, y2: Float) {
        assertApproxEq(children[0].rect.top, y0, EPS)
        assertApproxEq(children[1].rect.top, y1, EPS)
        assertApproxEq(children[2].rect.top, y2, EPS)
    }

    @Test
    fun cross_axis_start_top_aligned() {
        val row = rowNode(CrossAxisAlignment.START)
        row.assertSize(600f, 300f)
        row.assertMainAxisStarts()
        row.assertCrossTops(0f, 0f, 0f)
    }

    @Test
    fun cross_axis_end_bottom_aligned() {
        val row = rowNode(CrossAxisAlignment.END)
        row.assertSize(600f, 300f)
        row.assertMainAxisStarts()
        row.assertCrossTops(200f, 0f, 100f)
    }

    @Test
    fun cross_axis_center() {
        val row = rowNode(CrossAxisAlignment.CENTER)
        row.assertSize(600f, 300f)
        row.assertMainAxisStarts()
        row.assertCrossTops(100f, 0f, 50f)
    }

    @Test
    fun cross_axis_stretch_equal_heights() {
        val row = rowNode(CrossAxisAlignment.STRETCH)
        row.assertSize(1000f, 1000f)
        row.assertMainAxisStarts()
        row.assertCrossTops(0f, 0f, 0f)
        row.children.forEach { assertApproxEq(it.rect.height, 1000f, EPS) }
    }

    @Test
    fun cross_axis_baseline_without_real_baseline_top_aligned() {
        // 无基线子树在 BASELINE 档按 KDoc "Children who report no baseline will be top-aligned."
        // 做 top 对齐:委托层(RenderSingleChildBox / RenderContainerBox 的默认基线)不施加
        // "无基线 → size.height" 回退,只有最外层 getDistanceToBaseline(onlyReal = false) 才回退。
        val row = rowNode(CrossAxisAlignment.BASELINE)
        row.assertSize(600f, 300f)
        row.assertMainAxisStarts()
        row.assertCrossTops(0f, 0f, 0f)
    }

    @Test
    fun cross_axis_baseline_is_depth_independent() {
        // 回归:同一棵"无基线子树"不应因外面多包一层代理链(Container 的
        // ConstrainedBox → ColoredBox → LimitedBox → ConstrainedBox)而改变 BASELINE 结果。
        // 修复前:Container 版 y = rowH - h(200/0/100),裸 ConstrainedBox 版 y = 0,两者不一致。
        val wrapped = rowNode(CrossAxisAlignment.BASELINE)
        val bare = rootLayout {
            Row(
                crossAxisAlignment = CrossAxisAlignment.BASELINE,
                textDirection = Direction.LTR,
                textBaseline = BaselineMode.ALPHABETIC
            ) {
                ConstrainedBox(constraints = BoxConstraints.tightFor(width = 100f, height = 100f))
                ConstrainedBox(constraints = BoxConstraints.tightFor(width = 300f, height = 300f))
                ConstrainedBox(constraints = BoxConstraints.tightFor(width = 200f, height = 200f))
            }
        }
        bare.assertSize(600f, 300f)
        wrapped.assertCrossTops(0f, 0f, 0f)
        bare.assertCrossTops(0f, 0f, 0f)
    }

    @Test
    fun cross_axis_center_golden() {
        golden("widget/row/cross_axis_center") { rowScene(CrossAxisAlignment.CENTER) }
    }

    // 用同一 API 独立测量基线距离,供基线对齐断言使用(两端同源实测,与字体无关)
    private fun baselineOf(text: String, fontSize: Float): Float =
        TextPainter(
            text = TextSpan(text, style = TextStyle(fontSize = fontSize, typeface = testTypeface))
        ).apply { layout(0f, Float.POSITIVE_INFINITY) }
            .computeDistanceToActualBaseline(BaselineMode.ALPHABETIC)

    @Test
    fun cross_axis_baseline_text_aligns_by_baseline() {
        // 两段不同字号的文本按 BASELINE 对齐:ascent 更大者顶边更高、高度更大,且基线真正对齐。
        val root = rootLayout {
            Row(
                crossAxisAlignment = CrossAxisAlignment.BASELINE,
                textDirection = Direction.LTR,
                textBaseline = BaselineMode.ALPHABETIC
            ) {
                RichText { TextSpan("Hello", style = TextStyle(fontSize = 20f, typeface = testTypeface)) }
                RichText { TextSpan("Hello", style = TextStyle(fontSize = 40f, typeface = testTypeface)) }
            }
        }
        val small = root.children[0].rect
        val large = root.children[1].rect

        assertTrue(large.top < small.top, "大字号顶边应更高,实际 large=${large.top} small=${small.top}")
        assertTrue(large.height > small.height, "大字号高度应更大,实际 large=${large.height} small=${small.height}")
        // 排除退化为 START/END:两者 top 与 bottom 都不相等
        assertTrue(abs(large.top - small.top) > EPS, "top 不应相等(否则退化为 START)")
        assertTrue(abs(large.bottom - small.bottom) > EPS, "bottom 不应相等(否则退化为 END)")

        // 基线真对齐:两端都实测
        val b20 = baselineOf("Hello", 20f)
        val b40 = baselineOf("Hello", 40f)
        assertApproxEq(small.top + b20, large.top + b40, 0.5f)
    }
}
