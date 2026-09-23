package com.muedsa.snapshot

import com.muedsa.geometry.*
import com.muedsa.snapshot.paint.BoxFit
import com.muedsa.snapshot.paint.paintImage
import com.muedsa.snapshot.paint.decoration.*
import com.muedsa.snapshot.paint.text.TextPainter
import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderConstrainedBox
import com.muedsa.snapshot.rendering.box.RenderImage
import com.muedsa.snapshot.rendering.box.RenderOpacity
import com.muedsa.snapshot.rendering.box.RenderPositionedBox
import com.muedsa.snapshot.rendering.flex.CrossAxisAlignment
import com.muedsa.snapshot.rendering.flex.RenderFlex
import com.muedsa.snapshot.rendering.stack.RenderStack
import com.muedsa.snapshot.rendering.stack.StackParentData
import com.muedsa.snapshot.widget.Flexible
import com.muedsa.snapshot.widget.Opacity
import com.muedsa.snapshot.widget.Positioned
import com.muedsa.snapshot.widget.ProviderImage
import com.muedsa.snapshot.widget.RawImage
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Color
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PublicParameterValidationTest {

    @Test
    fun vector3_rejects_component_counts_other_than_three() {
        assertFailsWith<IllegalArgumentException> { Vector3(1f, 2f) }
        assertFailsWith<IllegalArgumentException> { Vector3(1f, 2f, 3f, 4f) }
    }

    @Test
    fun compute_rotation_rejects_non_finite_angles() {
        assertFailsWith<IllegalArgumentException> { computeRotation(Float.NaN) }
        assertFailsWith<IllegalArgumentException> { computeRotation(Float.POSITIVE_INFINITY) }
        assertFailsWith<IllegalArgumentException> { computeRotation(Float.NEGATIVE_INFINITY) }
    }

    @Test
    fun box_constraints_reject_non_normalized_bounds() {
        assertFailsWith<IllegalArgumentException> { BoxConstraints(maxWidth = Float.NaN) }
        assertFailsWith<IllegalArgumentException> { BoxConstraints(maxHeight = Float.NaN) }
        assertFailsWith<IllegalArgumentException> { BoxConstraints(minWidth = -1f) }
        assertFailsWith<IllegalArgumentException> { BoxConstraints(minHeight = -1f) }
        assertFailsWith<IllegalArgumentException> { BoxConstraints(minWidth = 2f, maxWidth = 1f) }
        assertFailsWith<IllegalArgumentException> { BoxConstraints(minHeight = 2f, maxHeight = 1f) }
    }

    @Test
    fun aspect_ratio_constraint_rejects_non_positive_sizes() {
        val constraints = BoxConstraints(maxWidth = 10f, maxHeight = 10f)

        assertFailsWith<IllegalArgumentException> {
            constraints.constrainSizeAndAttemptToPreserveAspectRatio(Size(0f, 1f))
        }
        assertFailsWith<IllegalArgumentException> {
            constraints.constrainSizeAndAttemptToPreserveAspectRatio(Size(1f, -1f))
        }
    }

    @Test
    fun opacity_apis_reject_values_outside_the_unit_interval() {
        assertFailsWith<IllegalArgumentException> { Opacity(opacity = -0.01f) }
        assertFailsWith<IllegalArgumentException> { Opacity(opacity = 1.01f) }
        assertFailsWith<IllegalArgumentException> { RenderOpacity(opacity = -0.01f) }
        assertFailsWith<IllegalArgumentException> { RenderOpacity(opacity = 1.01f) }
    }

    @Test
    fun positioned_rejects_overconstrained_axes() {
        assertFailsWith<IllegalArgumentException> {
            Positioned(left = 0f, right = 0f, width = 1f)
        }
        assertFailsWith<IllegalArgumentException> {
            Positioned(top = 0f, bottom = 0f, height = 1f)
        }
    }

    @Test
    fun render_positioned_box_rejects_negative_size_factors() {
        assertFailsWith<IllegalArgumentException> { RenderPositionedBox(widthFactor = -0.01f) }
        assertFailsWith<IllegalArgumentException> { RenderPositionedBox(heightFactor = -0.01f) }
    }

    @Test
    fun text_layout_rejects_nan_widths() {
        val painter = TextPainter(text = TextSpan(text = "x"))

        assertFailsWith<IllegalArgumentException> { painter.layout(minWidth = Float.NaN) }
        assertFailsWith<IllegalArgumentException> { painter.layout(maxWidth = Float.NaN) }
    }

    @Test
    fun box_decoration_rejects_blend_mode_without_a_background() {
        assertFailsWith<IllegalArgumentException> {
            BoxDecoration(backgroundBlendMode = BlendMode.SRC_OVER)
        }
    }

    @Test
    fun border_merge_apis_reject_incompatible_borders() {
        val red = BorderSide(color = Color.RED)
        val blue = BorderSide(color = Color.BLUE)

        assertFailsWith<IllegalArgumentException> { BorderSide.merge(red, blue) }
        assertFailsWith<IllegalArgumentException> {
            Border.merge(Border(top = red), Border(top = blue))
        }
    }

    @Test
    fun paint_image_rejects_an_incompatible_center_slice_and_fit() {
        val surface = Surface.makeRasterN32Premul(4, 4)
        val image = surface.makeImageSnapshot()
        try {
            assertFailsWith<IllegalArgumentException> {
                paintImage(
                    canvas = surface.canvas,
                    rect = Rect.makeWH(4f, 4f),
                    image = image,
                    fit = BoxFit.NONE,
                    centerSlice = Rect.makeXYWH(1f, 1f, 2f, 2f),
                )
            }
        } finally {
            image.close()
            surface.close()
        }
    }

    @Test
    fun paint_image_rejects_center_slice_when_fit_crops_the_source() {
        val surface = Surface.makeRasterN32Premul(6, 6)
        val image = surface.makeImageSnapshot()
        try {
            assertFailsWith<IllegalArgumentException> {
                paintImage(
                    canvas = surface.canvas,
                    rect = Rect.makeWH(12f, 8f),
                    image = image,
                    fit = BoxFit.FIT_WIDTH,
                    centerSlice = Rect.makeXYWH(2f, 2f, 2f, 2f),
                )
            }
        } finally {
            image.close()
            surface.close()
        }
    }

    @Test
    fun paint_image_rejects_closed_images() {
        val surface = Surface.makeRasterN32Premul(1, 1)
        val image = surface.makeImageSnapshot()
        image.close()
        try {
            assertFailsWith<IllegalArgumentException> {
                paintImage(
                    canvas = surface.canvas,
                    rect = Rect.makeWH(1f, 1f),
                    image = image,
                )
            }
        } finally {
            surface.close()
        }
    }

    @Test
    fun image_apis_reject_non_positive_or_non_finite_scale_before_loading() {
        val surface = Surface.makeRasterN32Premul(4, 4)
        val image = surface.makeImageSnapshot()
        var providerCalls = 0
        try {
            for (scale in listOf(0f, -1f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)) {
                assertFailsWith<IllegalArgumentException> { RawImage(image = image, scale = scale) }
                assertFailsWith<IllegalArgumentException> { RenderImage(image = image, scale = scale) }
                assertFailsWith<IllegalArgumentException> {
                    ProviderImage(provider = { providerCalls++; image }, scale = scale)
                }
                assertFailsWith<IllegalArgumentException> {
                    paintImage(surface.canvas, Rect.makeWH(4f, 4f), image, scale = scale)
                }
            }
            assertEquals(0, providerCalls, "无效缩放参数不应触发图片提供函数")
        } finally {
            image.close()
            surface.close()
        }
    }

    @Test
    fun border_painting_rejects_incompatible_shape_arguments() {
        val surface = Surface.makeRasterN32Premul(4, 4)
        val rect = Rect.makeWH(4f, 4f)
        val radius = BorderRadius.all(Radius.circular(1f))
        try {
            assertFailsWith<IllegalArgumentException> {
                Border.all().paint(surface.canvas, rect, BoxShape.CIRCLE, radius)
            }
            assertFailsWith<IllegalArgumentException> {
                BoxBorder.paintNonUniformBorder(
                    canvas = surface.canvas,
                    rect = rect,
                    borderRadius = radius,
                    shape = BoxShape.CIRCLE,
                    color = Color.RED,
                )
            }
        } finally {
            surface.close()
        }
    }

    @Test
    fun uniform_border_helpers_reject_none_sides() {
        val surface = Surface.makeRasterN32Premul(4, 4)
        val rect = Rect.makeWH(4f, 4f)
        try {
            assertFailsWith<IllegalArgumentException> {
                BoxBorder.paintUniformBorderWithRadius(surface.canvas, rect, BorderSide.NONE, BorderRadius.ZERO)
            }
            assertFailsWith<IllegalArgumentException> {
                BoxBorder.paintUniformBorderWithCircle(surface.canvas, rect, BorderSide.NONE)
            }
            assertFailsWith<IllegalArgumentException> {
                BoxBorder.paintUniformBorderWithRectangle(surface.canvas, rect, BorderSide.NONE)
            }
        } finally {
            surface.close()
        }
    }

    @Test
    fun render_flex_requires_a_baseline_mode_for_baseline_alignment() {
        val flex = RenderFlex(crossAxisAlignment = CrossAxisAlignment.BASELINE)
        flex.appendChild(RenderConstrainedBox(BoxConstraints.tight(Size(1f, 1f))))

        assertFailsWith<IllegalArgumentException> {
            flex.layout(BoxConstraints.tight(Size(1f, 1f)))
        }
    }

    @Test
    fun parent_data_widgets_reject_incompatible_render_boxes() {
        assertFailsWith<IllegalArgumentException> {
            Positioned().applyParentData(RenderConstrainedBox(BoxConstraints()))
        }
        assertFailsWith<IllegalArgumentException> {
            Flexible().applyParentData(RenderConstrainedBox(BoxConstraints()))
        }
    }

    @Test
    fun render_box_rejects_transform_requests_for_non_children() {
        val parent = RenderConstrainedBox(BoxConstraints())
        val unrelated = RenderConstrainedBox(BoxConstraints())

        assertFailsWith<IllegalArgumentException> {
            parent.applyPaintTransform(unrelated, Matrix44CMO.identity())
        }
    }

    @Test
    fun positioned_layout_helper_rejects_non_positioned_parent_data() {
        val child = RenderConstrainedBox(BoxConstraints())

        assertFailsWith<IllegalArgumentException> {
            RenderStack.layoutPositionedChild(
                child = child,
                childParentData = StackParentData(),
                size = Size(10f, 10f),
                alignment = BoxAlignment.CENTER,
            )
        }
    }

    @Test
    fun positioned_layout_helper_rejects_parent_data_from_another_child() {
        val child = RenderConstrainedBox(BoxConstraints())
        val parentData = StackParentData().apply { width = 1f }

        assertFailsWith<IllegalArgumentException> {
            RenderStack.layoutPositionedChild(
                child = child,
                childParentData = parentData,
                size = Size(10f, 10f),
                alignment = BoxAlignment.CENTER,
            )
        }
    }
}
