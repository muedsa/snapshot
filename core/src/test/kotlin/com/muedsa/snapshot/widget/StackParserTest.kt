package com.muedsa.snapshot.widget

import com.muedsa.geometry.AlignmentDirectional
import com.muedsa.geometry.AlignmentGeometry
import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.assertApproxEq
import com.muedsa.snapshot.assertGlobalRect
import com.muedsa.snapshot.assertSize
import com.muedsa.snapshot.golden
import com.muedsa.snapshot.rendering.LayoutNode
import com.muedsa.snapshot.rootLayout
import org.jetbrains.skia.Color
import org.jetbrains.skia.paragraph.Direction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StackParserTest {

    private companion object {
        const val STACK_WIDTH = 200f
        const val STACK_HEIGHT = 150f
        const val EPS = 0.01f
    }

    // 三个半透明色块;Stack(LOOSE)尺寸由最大子盒决定 = 200x150(实测)。
    private fun Widget.stackScene(alignment: AlignmentGeometry, direction: Direction) {
        Stack(alignment = alignment, textDirection = direction) {
            Container(width = 200f, height = 80f, color = Color.withA(Color.RED, 128))
            Container(width = 50f, height = 150f, color = Color.withA(Color.YELLOW, 128))
            Container(width = 100f, height = 100f, color = Color.withA(Color.GREEN, 128))
        }
    }

    private fun layoutOf(alignment: AlignmentGeometry, direction: Direction): LayoutNode =
        rootLayout { stackScene(alignment, direction) }

    // 方向无关组(11):start == 0 的 AlignmentDirectional 三档 + AlignmentDirectional.CENTER + BoxAlignment 八档
    private val directionIndependent = listOf(
        AlignmentDirectional.TOP_CENTER, AlignmentDirectional.CENTER, AlignmentDirectional.BOTTOM_CENTER,
        BoxAlignment.TOP_CENTER, BoxAlignment.TOP_RIGHT,
        BoxAlignment.CENTER_LEFT, BoxAlignment.CENTER, BoxAlignment.CENTER_RIGHT,
        BoxAlignment.BOTTOM_LEFT, BoxAlignment.BOTTOM_CENTER, BoxAlignment.BOTTOM_RIGHT,
    )

    // 镜像组(6):start == ±1 的 AlignmentDirectional 六档
    private val mirrored = listOf(
        AlignmentDirectional.TOP_START, AlignmentDirectional.TOP_END,
        AlignmentDirectional.CENTER_START, AlignmentDirectional.CENTER_END,
        AlignmentDirectional.BOTTOM_START, AlignmentDirectional.BOTTOM_END,
    )

    private fun assertSameRects(a: LayoutNode, b: LayoutNode, message: String) {
        assertEquals(a.children.size, b.children.size, "$message: 子节点数不同")
        a.children.forEachIndexed { i, la ->
            val lb = b.children[i]
            assertApproxEq(lb.rect.left, la.rect.left, EPS)
            assertApproxEq(lb.rect.top, la.rect.top, EPS)
            assertApproxEq(lb.rect.width, la.rect.width, EPS)
            assertApproxEq(lb.rect.height, la.rect.height, EPS)
        }
    }

    private fun assertMirrored(ltr: LayoutNode, rtl: LayoutNode, alignment: AlignmentGeometry) {
        assertEquals(ltr.children.size, rtl.children.size, "$alignment: 子节点数不同")
        ltr.children.forEachIndexed { i, l ->
            val r = rtl.children[i]
            // RTL 是 LTR 的水平镜像:x' = W - x - w,y/尺寸不变
            assertApproxEq(r.rect.left, STACK_WIDTH - l.rect.left - l.rect.width, EPS)
            assertApproxEq(r.rect.top, l.rect.top, EPS)
            assertApproxEq(r.rect.width, l.rect.width, EPS)
            assertApproxEq(r.rect.height, l.rect.height, EPS)
        }
    }

    private fun assertInsideStack(node: LayoutNode) {
        val rect = node.rect
        assertTrue(rect.left >= -EPS, "left=${rect.left} 越界")
        assertTrue(rect.top >= -EPS, "top=${rect.top} 越界")
        assertTrue(rect.right <= STACK_WIDTH + EPS, "right=${rect.right} 越界")
        assertTrue(rect.bottom <= STACK_HEIGHT + EPS, "bottom=${rect.bottom} 越界")
    }

    @Test
    fun alignment_direction_invariants_test() {
        // 方向无关:同一档位在 LTR/RTL 下矩形逐点相同
        directionIndependent.forEach { alignment ->
            val ltr = layoutOf(alignment, Direction.LTR)
            val rtl = layoutOf(alignment, Direction.RTL)
            ltr.assertSize(STACK_WIDTH, STACK_HEIGHT)
            rtl.assertSize(STACK_WIDTH, STACK_HEIGHT)
            assertSameRects(ltr, rtl, "$alignment 应方向无关")
        }

        // 镜像:START/END 档在 RTL 下是 LTR 的水平镜像
        mirrored.forEach { alignment ->
            val ltr = layoutOf(alignment, Direction.LTR)
            val rtl = layoutOf(alignment, Direction.RTL)
            ltr.assertSize(STACK_WIDTH, STACK_HEIGHT)
            rtl.assertSize(STACK_WIDTH, STACK_HEIGHT)
            assertMirrored(ltr, rtl, alignment)
        }

        // 全 51 个场景:子盒必须落在 Stack 内
        (directionIndependent + mirrored).forEach { alignment ->
            listOf(Direction.LTR, Direction.RTL).forEach { direction ->
                layoutOf(alignment, direction).children.forEach { assertInsideStack(it) }
            }
        }
    }

    @Test
    fun alignment_representative_offsets_test() {
        // 期望值来自探针实测(格式 left, top, width, height;顺序同三个子盒)
        val topStart = layoutOf(AlignmentDirectional.TOP_START, Direction.LTR)
        topStart.assertSize(STACK_WIDTH, STACK_HEIGHT)
        topStart.children[0].assertGlobalRect(0f, 0f, 200f, 80f)
        topStart.children[1].assertGlobalRect(0f, 0f, 50f, 150f)
        topStart.children[2].assertGlobalRect(0f, 0f, 100f, 100f)

        val center = layoutOf(AlignmentDirectional.CENTER, Direction.LTR)
        center.assertSize(STACK_WIDTH, STACK_HEIGHT)
        center.children[0].assertGlobalRect(0f, 35f, 200f, 80f)
        center.children[1].assertGlobalRect(75f, 0f, 50f, 150f)
        center.children[2].assertGlobalRect(50f, 25f, 100f, 100f)

        val bottomEnd = layoutOf(AlignmentDirectional.BOTTOM_END, Direction.LTR)
        bottomEnd.assertSize(STACK_WIDTH, STACK_HEIGHT)
        bottomEnd.children[0].assertGlobalRect(0f, 70f, 200f, 80f)
        bottomEnd.children[1].assertGlobalRect(150f, 0f, 50f, 150f)
        bottomEnd.children[2].assertGlobalRect(100f, 50f, 100f, 100f)
    }

    @Test
    fun alignment_representative_golden_test() {
        // 半透明纯色叠加是确定的(OpacityTest 先例),代表档上整图基准
        golden("widget/stack/alignment_top_start") {
            stackScene(AlignmentDirectional.TOP_START, Direction.LTR)
        }
        golden("widget/stack/alignment_center") {
            stackScene(AlignmentDirectional.CENTER, Direction.LTR)
        }
        golden("widget/stack/alignment_bottom_end") {
            stackScene(AlignmentDirectional.BOTTOM_END, Direction.LTR)
        }
    }
}
