package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderFractionallySizedBox
import com.muedsa.snapshot.rendering.toLayoutNode
import com.muedsa.snapshot.snapshotPixels
import org.jetbrains.skia.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FractionallySizedBoxTest {

    @Test
    fun factors_size_the_child_from_parent_maximums() {
        val box = FractionallySizedBox(widthFactor = 0.5f, heightFactor = 0.25f).apply {
            attach(com.muedsa.snapshot.widget.SizedBox(width = 400f, height = 400f))
        }.createRenderBox() as RenderFractionallySizedBox
        box.layout(BoxConstraints(maxWidth = 200f, maxHeight = 120f))

        assertEquals(Size(100f, 30f), box.definiteSize)
        assertEquals(Size(100f, 30f), box.child!!.definiteSize)
    }

    @Test
    fun tight_parent_keeps_its_size_and_aligns_fractional_child() {
        val box = FractionallySizedBox(
            widthFactor = 0.5f,
            heightFactor = 0.5f,
            alignment = BoxAlignment.BOTTOM_RIGHT,
        ).apply {
            attach(com.muedsa.snapshot.widget.SizedBox(width = 400f, height = 400f))
        }.createRenderBox()
        box.layout(BoxConstraints.tight(Size(200f, 100f)))

        val layout = box.toLayoutNode()
        assertEquals(Size(200f, 100f), layout.size)
        assertEquals(Size(100f, 50f), layout.children.single().size)
        assertEquals(Offset(100f, 50f), layout.children.single().absoluteOffset)
    }

    @Test
    fun factor_above_one_allows_child_to_overflow() {
        val box = FractionallySizedBox(widthFactor = 1.5f, heightFactor = 1.5f).apply {
            attach(com.muedsa.snapshot.widget.SizedBox(width = 1f, height = 1f))
        }.createRenderBox()
        box.layout(BoxConstraints.tight(Size(100f, 80f)))

        val layout = box.toLayoutNode()
        assertEquals(Size(100f, 80f), layout.size)
        assertEquals(Size(150f, 120f), layout.children.single().size)
        assertEquals(Offset(-25f, -20f), layout.children.single().absoluteOffset)
    }

    @Test
    fun unspecified_axis_preserves_parent_constraints_and_empty_child_still_sizes() {
        val withChild = FractionallySizedBox(heightFactor = 0.5f).apply {
            attach(com.muedsa.snapshot.widget.SizedBox(width = 20f, height = 100f))
        }.createRenderBox() as RenderFractionallySizedBox
        withChild.layout(BoxConstraints(maxWidth = 200f, maxHeight = 80f))
        assertEquals(Size(20f, 40f), withChild.child!!.definiteSize)

        val empty = FractionallySizedBox(widthFactor = 0.5f).createRenderBox()
        empty.layout(BoxConstraints(minHeight = 12f, maxWidth = 100f, maxHeight = 80f))
        assertEquals(Size(50f, 12f), empty.definiteSize)
    }

    @Test
    fun paints_fractionally_sized_child() {
        val pixels = snapshotPixels {
            ConstrainedBox(BoxConstraints(maxWidth = 120f, maxHeight = 80f)) {
                FractionallySizedBox(widthFactor = 0.5f, heightFactor = 0.5f) {
                    ColoredBox(color = Color.BLUE)
                }
            }
        }

        assertEquals(60, pixels.info.width)
        assertEquals(40, pixels.info.height)
        expectColorAt(pixels, 59, 39, Color.BLUE)
    }

    @Test
    fun rejects_invalid_factors_unbounded_axes_and_overflowed_products() {
        listOf(-1f, Float.NaN, Float.POSITIVE_INFINITY).forEach { factor ->
            assertFailsWith<IllegalArgumentException> { FractionallySizedBox(widthFactor = factor) }
            assertFailsWith<IllegalArgumentException> { FractionallySizedBox(heightFactor = factor) }
        }
        assertFailsWith<IllegalArgumentException> {
            FractionallySizedBox(widthFactor = 0f).createRenderBox().layout(BoxConstraints(maxHeight = 80f))
        }
        assertFailsWith<IllegalArgumentException> {
            FractionallySizedBox(widthFactor = 2f).createRenderBox()
                .layout(BoxConstraints(maxWidth = Float.MAX_VALUE, maxHeight = 80f))
        }
    }
}
