package com.muedsa.snapshot.render.box

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.EdgeInsets
import com.muedsa.geometry.Matrix44CMO
import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderConstrainedBox
import com.muedsa.snapshot.rendering.box.RenderPadding
import com.muedsa.snapshot.rendering.box.RenderTransform
import com.muedsa.snapshot.rendering.stack.RenderStack
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RenderBoxTransformTest {
    private fun leaf() = RenderConstrainedBox(BoxConstraints.tight(Size(10f, 10f)))

    @Test
    fun direct_parent_transform_is_included() {
        val parent = RenderTransform(Matrix44CMO.identity().apply { translate(12f, 7f) })
        val child = leaf()
        parent.appendChild(child)
        parent.layout(BoxConstraints())
        assertContentEquals(parent.effectiveTransform.mat, child.getTransformTo(parent).mat)
    }

    @Test
    fun apply_paint_transform_accumulates_layout_offset() {
        val parent = RenderPadding(EdgeInsets.only(left = 12f, top = 7f))
        val child = leaf()
        parent.appendChild(child)
        parent.layout(BoxConstraints())
        val actual = Matrix44CMO.identity().apply { scale(2f, 3f, 1f) }
        parent.applyPaintTransform(child, actual)
        assertEquals(24f, actual.mat[12])
        assertEquals(21f, actual.mat[13])
    }

    @Test
    fun multiple_ancestors_include_offsets_but_not_the_target_parent() {
        val outer = RenderPadding(EdgeInsets.all(100f))
        val root = RenderPadding(EdgeInsets.only(left = 5f, top = 7f))
        val inner = RenderPadding(EdgeInsets.only(left = 11f, top = 13f))
        val child = leaf()
        outer.appendChild(root)
        root.appendChild(inner)
        inner.appendChild(child)
        outer.layout(BoxConstraints())
        val actual = child.getTransformTo(root)
        assertEquals(16f, actual.mat[12])
        assertEquals(20f, actual.mat[13])
    }

    @Test
    fun nested_scaling_and_padding_keep_paint_order() {
        val root = RenderPadding(EdgeInsets.only(left = 5f, top = 7f))
        val transform = RenderTransform(Matrix44CMO.identity().apply { scale(2f, 3f, 1f) })
        val inner = RenderPadding(EdgeInsets.only(left = 11f, top = 13f))
        val child = leaf()
        root.appendChild(transform)
        transform.appendChild(inner)
        inner.appendChild(child)
        root.layout(BoxConstraints())
        val actual = child.getTransformTo(root)
        assertEquals(2f, actual.mat[0])
        assertEquals(3f, actual.mat[5])
        assertEquals(27f, actual.mat[12])
        assertEquals(46f, actual.mat[13])
    }

    @Test
    fun nested_rotation_rotates_the_inner_layout_offset() {
        // 按列主序表示逆时针旋转 90 度，避免三角函数舍入影响断言。
        val rotation = Matrix44CMO(
            0f, 1f, 0f, 0f, -1f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f,
        )
        val root = RenderPadding(EdgeInsets.only(left = 5f, top = 7f))
        val transform = RenderTransform(rotation)
        val inner = RenderPadding(EdgeInsets.only(left = 11f, top = 13f))
        val child = leaf()
        root.appendChild(transform)
        transform.appendChild(inner)
        inner.appendChild(child)
        root.layout(BoxConstraints())
        val actual = child.getTransformTo(root)
        assertEquals(-8f, actual.mat[12])
        assertEquals(18f, actual.mat[13])
    }

    @Test
    fun origin_and_alignment_are_applied_once() {
        val parent = RenderTransform(
            Matrix44CMO.identity().apply { scale(2f, 3f, 1f) },
            origin = Offset(2f, 3f), alignment = BoxAlignment.CENTER,
        )
        val child = leaf()
        parent.appendChild(child)
        parent.layout(BoxConstraints())
        val actual = child.getTransformTo(parent)
        assertEquals(-7f, actual.mat[12])
        assertEquals(-16f, actual.mat[13])
    }

    @Test
    fun self_transform_is_identity_with_or_without_parent() {
        val root = RenderPadding(EdgeInsets.all(5f))
        val child = leaf()
        root.appendChild(child)
        for (box in listOf(root, child)) {
            assertContentEquals(Matrix44CMO.identity().mat, box.getTransformTo(box).mat)
        }
    }

    @Test
    fun non_ancestor_targets_are_rejected() {
        val root = RenderStack()
        val first = leaf()
        val second = leaf()
        root.appendChildren(listOf(first, second))
        assertFailsWith<IllegalArgumentException> { first.getTransformTo(second) }
        assertFailsWith<IllegalArgumentException> { first.getTransformTo(leaf()) }
        assertFailsWith<IllegalArgumentException> { root.getTransformTo(first) }
    }

    @Test
    fun apply_paint_transform_rejects_a_foreign_child() {
        val parent = RenderPadding(EdgeInsets.ZERO)
        assertFailsWith<IllegalArgumentException> {
            parent.applyPaintTransform(leaf(), Matrix44CMO.identity())
        }
    }
}
