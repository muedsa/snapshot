package com.muedsa.snapshot.widget

import com.muedsa.geometry.EdgeInsets
import com.muedsa.geometry.Matrix44CMO
import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderConstrainedBox
import com.muedsa.snapshot.rendering.stack.RenderStack
import com.muedsa.snapshot.rendering.stack.StackParentData
import com.muedsa.snapshot.snapshotPixels
import org.jetbrains.skia.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StackOverflowTest {
    private fun ChildSlot.positionedChildren() {
        attach(Positioned(left = -10f, top = 0f, width = 20f, height = 20f).apply {
            Container(color = Color.RED)
        })
        attach(Positioned(left = 20f, top = 20f, width = 10f, height = 10f).apply {
            Container(color = Color.BLUE)
        })
    }

    private fun scene(clip: ClipBehavior, indexed: Boolean) = snapshotPixels {
        Padding(EdgeInsets.all(20f)) {
            SizedBox(width = 40f, height = 40f) {
                if (indexed) {
                    IndexedStack(index = 0, clipBehavior = clip) { positionedChildren() }
                } else {
                    Stack(clipBehavior = clip) { positionedChildren() }
                }
            }
        }
    }

    @Test
    fun earlier_overflow_is_clipped_in_stack_and_indexed_stack() {
        for (indexed in listOf(false, true)) {
            for (clip in ClipBehavior.entries.filter { it != ClipBehavior.NONE }) {
                val pixels = scene(clip, indexed)
                expectColorAt(pixels, 15, 25, Color.WHITE)
                expectColorAt(pixels, 25, 25, Color.RED)
                expectColorAt(pixels, 45, 45, if (indexed) Color.WHITE else Color.BLUE)
            }
        }
    }

    @Test
    fun none_keeps_overflow_visible() {
        for (indexed in listOf(false, true)) {
            expectColorAt(scene(ClipBehavior.NONE, indexed), 15, 25, Color.RED)
        }
    }

    @Test
    fun overflow_does_not_skip_layout_of_later_children() {
        val stack = RenderStack()
        val children = List(3) { RenderConstrainedBox(BoxConstraints()) }
        stack.appendChildren(children)
        children.forEachIndexed { index, child ->
            (child.parentData as StackParentData).apply {
                left = if (index == 0) -10f else index * 10f
                top = index * 10f
                width = 10f
                height = 10f
            }
        }
        stack.layout(BoxConstraints.tight(Size(40f, 40f)))
        assertTrue(stack.hasVisualOverflow)
        children.forEachIndexed { index, child ->
            assertEquals(Size(10f, 10f), child.definiteSize)
            assertEquals(Offset(if (index == 0) -10f else index * 10f, index * 10f), child.parentData!!.offset)
        }
    }

    @Test
    fun overflow_is_reset_on_relayout() {
        val stack = RenderStack()
        val child = RenderConstrainedBox(BoxConstraints.tight(Size(20f, 20f)))
        stack.appendChild(child)
        val data = child.parentData as StackParentData
        data.left = -10f
        stack.layout(BoxConstraints.tight(Size(40f, 40f)))
        assertTrue(stack.hasVisualOverflow)
        data.left = 0f
        stack.layout(BoxConstraints.tight(Size(40f, 40f)))
        assertFalse(stack.hasVisualOverflow)
    }

    @Test
    fun painting_outside_a_child_without_geometry_overflow_is_not_clipped() {
        // 与 Flutter 一致：直接子节点的几何未越界时，Stack 不自动裁剪其变换后的绘制内容。
        val pixels = snapshotPixels {
            Padding(EdgeInsets.all(20f)) {
                SizedBox(width = 40f, height = 40f) {
                    Stack(clipBehavior = ClipBehavior.HARD_EDGE) {
                        Positioned(left = 0f, top = 0f, width = 20f, height = 20f) {
                            Transform(Matrix44CMO.identity().apply { translate(-10f, 0f) }, alignment = null) {
                                Container(color = Color.RED)
                            }
                        }
                    }
                }
            }
        }
        expectColorAt(pixels, 15, 25, Color.RED)
    }
}
