package com.muedsa.snapshot.widget

import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderContainerBox
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MultiChildWidgetGuardTest {

    /** 一个"吞掉"所有子节点的 RenderContainerBox:用来构造 renderBox.children 与 widget.children 数量不一致的情形。 */
    private class SwallowingContainerBox : RenderContainerBox() {
        override fun performLayout() {
            size = BoxConstraints().constrain(Size.ZERO)
        }
    }

    /** 有 1 个子 Widget,但产出的 RenderContainerBox 里 0 个 RenderBox。 */
    private class MismatchedWidget : MultiChildWidget() {
        override fun createRenderBox(children: List<Widget>): RenderBox = SwallowingContainerBox()
    }

    @Test
    fun render_box_child_count_mismatch_fails_loudly() {
        val widget = MismatchedWidget()
        widget.attach(SizedBox(width = 1f, height = 1f))

        val error = assertFailsWith<IllegalStateException> { widget.createRenderBox() }

        assertTrue(
            error.message!!.contains("MismatchedWidget"),
            "message should contain the widget class name, but was: ${error.message}"
        )
        assertTrue(
            error.message!!.contains("1"),
            "message should mention the widget child count, but was: ${error.message}"
        )
    }
}