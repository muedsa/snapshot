package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.paint.Axis
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.toLayoutNode
import com.muedsa.snapshot.snapshotPixels
import org.jetbrains.skia.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UnconstrainedBoxTest {

    @Test
    fun releases_both_child_constraints_and_centers_overflow() {
        val box = UnconstrainedBox().apply {
            attach(com.muedsa.snapshot.widget.SizedBox(width = 150f, height = 50f))
        }.createRenderBox()
        box.layout(BoxConstraints.tight(Size(100f, 80f)))

        val layout = box.toLayoutNode()
        assertEquals(Size(100f, 80f), layout.size)
        assertEquals(Size(150f, 50f), layout.children.single().size)
        assertEquals(Offset(-25f, 15f), layout.children.single().absoluteOffset)
    }

    @Test
    fun constrained_axis_preserves_only_that_direction() {
        val cases = listOf(
            Triple(Axis.HORIZONTAL, Size(100f, 120f), Offset(0f, -20f)),
            Triple(Axis.VERTICAL, Size(150f, 80f), Offset(-25f, 0f)),
        )
        cases.forEach { (axis, childSize, offset) ->
            val box = UnconstrainedBox(constrainedAxis = axis).apply {
                attach(com.muedsa.snapshot.widget.SizedBox(width = 150f, height = 120f))
            }.createRenderBox()
            box.layout(BoxConstraints.tight(Size(100f, 80f)))

            val childLayout = box.toLayoutNode().children.single()
            assertEquals(Size(100f, 80f), box.definiteSize)
            assertEquals(childSize, childLayout.size)
            assertEquals(offset, childLayout.absoluteOffset)
        }
    }

    @Test
    fun empty_child_uses_minimum_size_and_alignment_moves_smaller_child() {
        val empty = UnconstrainedBox().createRenderBox()
        empty.layout(BoxConstraints(minWidth = 20f, maxWidth = 100f, minHeight = 15f, maxHeight = 80f))
        assertEquals(Size(20f, 15f), empty.definiteSize)

        val box = UnconstrainedBox(alignment = BoxAlignment.BOTTOM_RIGHT).apply {
            attach(com.muedsa.snapshot.widget.SizedBox(width = 40f, height = 20f))
        }.createRenderBox()
        box.layout(BoxConstraints.tight(Size(100f, 80f)))
        assertEquals(Offset(60f, 60f), box.toLayoutNode().children.single().absoluteOffset)
    }

    @Test
    fun paints_child_at_aligned_offset() {
        val pixels = snapshotPixels {
            SizedBox(width = 100f, height = 80f) {
                UnconstrainedBox {
                    Container(width = 40f, height = 20f, color = Color.BLUE)
                }
            }
        }

        expectColorAt(pixels, 50, 40, Color.BLUE)
        expectColorAt(pixels, 10, 10, Color.WHITE)
    }

    @Test
    fun rejects_child_that_cannot_resolve_a_finite_size() {
        val box = UnconstrainedBox().apply {
            attach(com.muedsa.snapshot.widget.SizedBox.expand())
        }.createRenderBox()
        assertFailsWith<IllegalArgumentException> {
            box.layout(BoxConstraints.tight(Size(100f, 80f)))
        }
    }
}
