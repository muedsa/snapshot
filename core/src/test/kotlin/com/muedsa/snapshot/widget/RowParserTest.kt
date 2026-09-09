package com.muedsa.snapshot.widget

import com.muedsa.snapshot.assertApproxEq
import com.muedsa.snapshot.assertSize
import com.muedsa.snapshot.golden
import com.muedsa.snapshot.rendering.LayoutNode
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
    fun cross_axis_baseline_without_real_baseline_matches_end() {
        // 待议:实测 y = rowH - h(与 END 相同),而 CrossAxisAlignment.BASELINE 的 KDoc 写的是
        // "Children who report no baseline will be top-aligned."。
        // 根因:RenderSingleChildBox.computeDistanceToActualBaseline 委托子盒时用
        // child?.getDistanceToBaseline(baseline)(onlyReal 默认 false),链底返回 definiteSize.height,
        // 于是无基线子盒报告的是"底边基线"而非 null,RenderFlex 走了 distance != null 分支。
        // 本批不改产品代码(修复需透传 onlyReal,牵涉 RenderBox/RenderSingleChildBox 签名),按实测断言。
        val row = rowNode(CrossAxisAlignment.BASELINE)
        row.assertSize(600f, 300f)
        row.assertMainAxisStarts()
        row.assertCrossTops(200f, 0f, 100f)
    }

    @Test
    fun cross_axis_center_golden() {
        golden("widget/row/cross_axis_center") { rowScene(CrossAxisAlignment.CENTER) }
    }
}
