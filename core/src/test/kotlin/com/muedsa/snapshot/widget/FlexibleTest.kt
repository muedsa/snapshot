package com.muedsa.snapshot.widget

import com.muedsa.snapshot.assertGlobalRect
import com.muedsa.snapshot.assertSize
import com.muedsa.snapshot.findType
import com.muedsa.snapshot.paint.Axis
import com.muedsa.snapshot.rendering.box.RenderColoredBox
import com.muedsa.snapshot.rendering.flex.FlexFit
import com.muedsa.snapshot.rendering.flex.FlexParentData
import com.muedsa.snapshot.rendering.flex.RenderFlex
import com.muedsa.snapshot.rootLayout
import org.jetbrains.skia.Color
import kotlin.test.Test
import kotlin.test.expect

class FlexibleTest {

    @Test
    fun applyParentData_test() {
        val flexArr = intArrayOf(1, 2, 3)
        val flex = Flex(direction = Axis.HORIZONTAL).apply {
            flexArr.forEach {
                Flexible(
                    flex = it
                ) {
                    Container()
                }
            }
        }
        val renderFlex = flex.createRenderBox() as RenderFlex
        renderFlex.children.forEachIndexed { index, renderBox ->
            val flexParentData: FlexParentData = renderBox.parentData as FlexParentData
            expect(flexArr[index]) { flexParentData.flex }
        }
    }

    // SizedBox 300x100 定宽内 Row;两个 Flexible(fit=TIGHT,flex=1:2)的纯色盒。
    // 推导:flex 合计 3,剩余空间 300 → 每 flex 100;红(flex=1)宽 100、绿(flex=2)宽 200。
    // Container(color) 无子级,在 composeWidget 中走 LimitedBox+BoxConstraints.expand(),
    // 在紧约束下撑满分配的交叉轴(高 100),故两盒交叉偏移均为 0。
    @Test
    fun row_flex_1_2_distributes_width() {
        val root = rootLayout {
            SizedBox(width = 300f, height = 100f) {
                Row {
                    Flexible(flex = 1, fit = FlexFit.TIGHT) {
                        Container(color = Color.RED)
                    }
                    Flexible(flex = 2, fit = FlexFit.TIGHT) {
                        Container(color = Color.GREEN)
                    }
                }
            }
        }
        root.assertSize(300f, 100f)
        val red = checkNotNull(root.findType<RenderColoredBox> { it.color == Color.RED }) {
            "找不到红色 RenderColoredBox"
        }
        red.assertGlobalRect(0f, 0f, 100f, 100f)
        val green = checkNotNull(root.findType<RenderColoredBox> { it.color == Color.GREEN }) {
            "找不到绿色 RenderColoredBox"
        }
        green.assertGlobalRect(100f, 0f, 200f, 100f)
    }
}
