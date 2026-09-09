package com.muedsa.snapshot.widget

import com.muedsa.snapshot.assertApproxEq
import com.muedsa.snapshot.assertSize
import com.muedsa.snapshot.golden
import com.muedsa.snapshot.rendering.LayoutNode
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.flex.CrossAxisAlignment
import com.muedsa.snapshot.rootLayout
import org.jetbrains.skia.Color
import org.jetbrains.skia.paragraph.BaselineMode
import org.jetbrains.skia.paragraph.Direction
import kotlin.test.Test

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
}
