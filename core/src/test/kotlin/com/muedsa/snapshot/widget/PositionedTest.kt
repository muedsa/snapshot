package com.muedsa.snapshot.widget

import com.muedsa.snapshot.assertGlobalRect
import com.muedsa.snapshot.assertSize
import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.findType
import com.muedsa.snapshot.golden
import com.muedsa.snapshot.rootLayout
import com.muedsa.snapshot.rendering.box.RenderColoredBox
import com.muedsa.snapshot.rendering.stack.RenderStack
import com.muedsa.snapshot.rendering.stack.StackParentData
import com.muedsa.snapshot.snapshotPixels
import org.jetbrains.skia.Color
import kotlin.test.Test
import kotlin.test.expect

class PositionedTest {

    @Test
    fun applyParentData_test() {
        val stack = Stack().apply {
            Positioned(
                left = 8f,
                top = 10f,
                right = 6f,
                bottom = 13f
            ) {
                SizedBox(width = 100f, height = 100f)
            }
            Positioned(
                left = 22f,
                top = 5f,
                right = 11f,
                bottom = 16f
            ) {
                SizedBox(width = 100f, height = 100f)
            }
        }
        val renderStack = stack.createRenderBox() as RenderStack
        val stackParentData1 = renderStack.children[0].parentData as StackParentData
        expect(8f) { stackParentData1.left }
        expect(10f) { stackParentData1.top }
        expect(6f) { stackParentData1.right }
        expect(13f) { stackParentData1.bottom }
        val stackParentData2 = renderStack.children[1].parentData as StackParentData
        expect(22f) { stackParentData2.left }
        expect(5f) { stackParentData2.top }
        expect(11f) { stackParentData2.right }
        expect(16f) { stackParentData2.bottom }
    }

    // SizedBox 200x200(白底)内 Stack;Positioned(left=10,top=10) 定位 100x100 红色盒,
    // 期望其全局矩形 (10,10,100,100);采样 (60,60) 为红、角与远点为白底。
    private fun Widget.leftTopScene() {
        SizedBox(width = 200f, height = 200f) {
            Stack {
                Positioned(left = 10f, top = 10f) {
                    Container(width = 100f, height = 100f, color = Color.RED)
                }
            }
        }
    }

    @Test
    fun left_top_layout_and_golden() {
        val root = rootLayout { leftTopScene() }
        root.assertSize(200f, 200f)
        val red = checkNotNull(root.findType<RenderColoredBox> { it.color == Color.RED }) {
            "找不到红色 RenderColoredBox"
        }
        red.assertGlobalRect(10f, 10f, 100f, 100f)

        val pixmap = snapshotPixels { leftTopScene() }
        expectColorAt(pixmap, 60, 60, Color.RED)
        expectColorAt(pixmap, 5, 5, Color.WHITE)
        expectColorAt(pixmap, 150, 150, Color.WHITE)
        golden("widget/positioned/left_top") { leftTopScene() }
    }
}
