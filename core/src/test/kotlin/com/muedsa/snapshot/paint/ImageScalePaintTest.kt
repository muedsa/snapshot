package com.muedsa.snapshot.paint

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.painterImage
import com.muedsa.snapshot.painterPixels
import com.muedsa.snapshot.paint.decoration.DecorationImage
import org.jetbrains.skia.Color
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Pixmap
import org.jetbrains.skia.Rect
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ImageScalePaintTest {
    private val scales = listOf(0.5f, 1f, 2f)

    // 相同的 8×8 逻辑图案，分别以不同像素密度生成，避免依赖文件、网络和字体。
    private fun quadrants(scale: Float): Image = painterImage(8f * scale, 8f * scale) { canvas ->
        Paint().use { paint ->
            listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW).forEachIndexed { index, color ->
                paint.color = color
                canvas.drawRect(Rect.makeXYWH((index % 2) * 4f * scale, (index / 2) * 4f * scale,
                    4f * scale, 4f * scale), paint)
            }
        }
    }

    private fun ninePatch(scale: Float): Image = painterImage(6f * scale, 6f * scale, Color.MAGENTA) { canvas ->
        Paint().use { paint ->
            for ((rect, color) in listOf(
                Rect.makeXYWH(0f, 0f, 2f, 2f) to Color.RED,
                Rect.makeXYWH(4f, 0f, 2f, 2f) to Color.GREEN,
                Rect.makeXYWH(0f, 4f, 2f, 2f) to Color.BLUE,
                Rect.makeXYWH(4f, 4f, 2f, 2f) to Color.YELLOW,
                Rect.makeXYWH(2f, 2f, 2f, 2f) to Color.CYAN,
            )) {
                paint.color = color
                canvas.drawRect(Rect.makeLTRB(rect.left * scale, rect.top * scale,
                    rect.right * scale, rect.bottom * scale), paint)
            }
        }
    }

    private fun assertCorners(pixels: Pixmap, left: Int, top: Int, width: Int, height: Int, inset: Int = 1) {
        expectColorAt(pixels, left + inset, top + inset, Color.RED)
        expectColorAt(pixels, left + width - 1 - inset, top + inset, Color.GREEN)
        expectColorAt(pixels, left + inset, top + height - 1 - inset, Color.BLUE)
        expectColorAt(pixels, left + width - 1 - inset, top + height - 1 - inset, Color.YELLOW)
    }

    @Test
    fun ordinary_image_uses_logical_input_size_for_every_fit() {
        for (scale in scales) quadrants(scale).use { image ->
            for (fit in BoxFit.entries) {
                val pixels = painterPixels(20f, 12f) { canvas ->
                    paintImage(canvas, Rect.makeWH(20f, 12f), image, scale = scale, fit = fit)
                }
                when (fit) {
                    BoxFit.NONE, BoxFit.SCALE_DOWN -> {
                        assertCorners(pixels, 6, 2, 8, 8)
                        expectColorAt(pixels, 5, 5, Color.WHITE)
                        expectColorAt(pixels, 10, 1, Color.WHITE)
                    }
                    BoxFit.CONTAIN, BoxFit.FIT_HEIGHT -> {
                        assertCorners(pixels, 4, 0, 12, 12)
                        expectColorAt(pixels, 3, 5, Color.WHITE)
                    }
                    else -> assertCorners(pixels, 0, 0, 20, 12)
                }
            }
        }
    }

    @Test
    fun none_crops_in_logical_units_and_respects_alignment() {
        for (scale in scales) quadrants(scale).use { image ->
            for ((alignment, color) in listOf(BoxAlignment.TOP_LEFT to Color.RED, BoxAlignment.BOTTOM_RIGHT to Color.YELLOW)) {
                val pixels = painterPixels(4f, 4f) { canvas ->
                    paintImage(canvas, Rect.makeWH(4f, 4f), image, scale = scale, fit = BoxFit.NONE, alignment = alignment)
                }
                expectColorAt(pixels, 1, 1, color)
                expectColorAt(pixels, 2, 2, color)
            }
        }
    }

    @Test
    fun repeated_images_use_logical_tile_size_and_clip_to_target() {
        for (scale in scales) quadrants(scale).use { image ->
            val pixels = painterPixels(20f, 20f) { canvas ->
                paintImage(canvas, Rect.makeXYWH(1f, 1f, 18f, 18f), image, scale = scale,
                    fit = BoxFit.NONE, alignment = BoxAlignment.TOP_LEFT, repeat = ImageRepeat.REPEAT)
            }
            expectColorAt(pixels, 2, 2, Color.RED)
            expectColorAt(pixels, 10, 2, Color.RED)
            expectColorAt(pixels, 14, 2, Color.GREEN)
            expectColorAt(pixels, 18, 18, Color.RED)
            expectColorAt(pixels, 19, 18, Color.WHITE)
            expectColorAt(pixels, 0, 0, Color.WHITE)
        }
    }

    @Test
    fun scaled_images_flip_without_leaving_a_canvas_transform() {
        for (scale in scales) quadrants(scale).use { image ->
            val pixels = painterPixels(12f, 12f) { canvas ->
                paintImage(canvas, Rect.makeXYWH(2f, 2f, 8f, 8f), image, scale = scale,
                    fit = BoxFit.FILL, flipHorizontally = true)
                Paint().use { paint ->
                    paint.color = Color.BLACK
                    canvas.drawRect(Rect.makeWH(1f, 1f), paint)
                }
            }
            expectColorAt(pixels, 3, 3, Color.GREEN)
            expectColorAt(pixels, 8, 3, Color.RED)
            expectColorAt(pixels, 0, 0, Color.BLACK)
            expectColorAt(pixels, 11, 3, Color.WHITE)
        }
    }

    @Test
    fun nine_patch_fill_keeps_logical_corners_at_different_densities() {
        for (scale in scales) ninePatch(scale).use { image ->
            val pixels = painterPixels(12f, 12f) { canvas ->
                paintImage(canvas, Rect.makeWH(12f, 12f), image, scale = scale,
                    centerSlice = Rect.makeXYWH(2f, 2f, 2f, 2f), fit = BoxFit.FILL)
            }
            assertCorners(pixels, 0, 0, 12, 12, inset = 0)
            expectColorAt(pixels, 6, 6, Color.CYAN)
        }
    }

    @Test
    fun nine_patch_non_fill_fits_keep_logical_border_and_destination_size() {
        for (scale in scales) ninePatch(scale).use { image ->
            for (fit in listOf(BoxFit.CONTAIN, BoxFit.FIT_HEIGHT, BoxFit.SCALE_DOWN)) {
                val pixels = painterPixels(20f, 12f) { canvas ->
                    paintImage(canvas, Rect.makeWH(20f, 12f), image, scale = scale,
                        centerSlice = Rect.makeXYWH(2f, 2f, 2f, 2f), fit = fit)
                }
                if (fit == BoxFit.SCALE_DOWN) {
                    assertCorners(pixels, 7, 3, 6, 6, inset = 0)
                    expectColorAt(pixels, 5, 6, Color.WHITE)
                } else {
                    assertCorners(pixels, 4, 0, 12, 12, inset = 0)
                    expectColorAt(pixels, 3, 6, Color.WHITE)
                }
                expectColorAt(pixels, 10, 6, Color.CYAN)
            }
        }
    }

    @Test
    fun repeated_nine_patch_uses_logical_size_and_restores_canvas() {
        for (scale in scales) ninePatch(scale).use { image ->
            val pixels = painterPixels(20f, 20f) { canvas ->
                paintImage(canvas, Rect.makeXYWH(1f, 1f, 18f, 18f), image, scale = scale,
                    centerSlice = Rect.makeXYWH(2f, 2f, 2f, 2f), fit = BoxFit.SCALE_DOWN,
                    alignment = BoxAlignment.TOP_LEFT, repeat = ImageRepeat.REPEAT)
                Paint().use { paint ->
                    paint.color = Color.BLACK
                    canvas.drawRect(Rect.makeWH(1f, 1f), paint)
                }
            }
            expectColorAt(pixels, 1, 1, Color.RED)
            expectColorAt(pixels, 7, 1, Color.RED)
            expectColorAt(pixels, 13, 13, Color.RED)
            expectColorAt(pixels, 18, 18, Color.YELLOW)
            expectColorAt(pixels, 19, 18, Color.WHITE)
            expectColorAt(pixels, 0, 0, Color.BLACK)
        }
    }

    @Test
    fun nine_patch_rejects_fits_that_would_crop_the_scaled_source() {
        for (scale in scales) ninePatch(scale).use { image ->
            painterPixels(20f, 20f) { canvas ->
                for ((fit, target) in listOf(BoxFit.FIT_WIDTH to Rect.makeWH(20f, 12f),
                    BoxFit.FIT_HEIGHT to Rect.makeWH(12f, 20f))) {
                    assertFailsWith<IllegalArgumentException> {
                        paintImage(canvas, target, image, scale = scale,
                            centerSlice = Rect.makeXYWH(2f, 2f, 2f, 2f), fit = fit)
                    }
                }
            }
        }
    }

    @Test
    fun ordinary_image_defaults_to_scale_down_without_enlarging() {
        for (scale in scales) quadrants(scale).use { image ->
            val pixels = painterPixels(20f, 12f) { canvas ->
                paintImage(canvas, Rect.makeWH(20f, 12f), image, scale = scale)
            }
            assertCorners(pixels, 6, 2, 8, 8)
            expectColorAt(pixels, 5, 5, Color.WHITE)
            expectColorAt(pixels, 10, 1, Color.WHITE)
        }
    }

    @Test
    fun ordinary_image_defaults_to_proportional_shrinking() {
        for (scale in scales) quadrants(scale).use { image ->
            val pixels = painterPixels(8f, 4f) { canvas ->
                paintImage(canvas, Rect.makeWH(8f, 4f), image, scale = scale)
            }
            assertCorners(pixels, 2, 0, 4, 4, inset = 0)
            expectColorAt(pixels, 1, 1, Color.WHITE)
            expectColorAt(pixels, 6, 1, Color.WHITE)
        }
    }

    @Test
    fun nine_patch_defaults_to_fill() {
        for (scale in scales) ninePatch(scale).use { image ->
            val pixels = painterPixels(12f, 12f) { canvas ->
                paintImage(canvas, Rect.makeWH(12f, 12f), image, scale = scale,
                    centerSlice = Rect.makeXYWH(2f, 2f, 2f, 2f))
            }
            assertCorners(pixels, 0, 0, 12, 12, inset = 0)
            expectColorAt(pixels, 6, 6, Color.CYAN)
        }
    }

    @Test
    fun explicit_fits_override_the_defaults() {
        for (scale in scales) {
            quadrants(scale).use { image ->
                val pixels = painterPixels(20f, 12f) { canvas ->
                    paintImage(canvas, Rect.makeWH(20f, 12f), image, scale = scale, fit = BoxFit.FILL)
                }
                assertCorners(pixels, 0, 0, 20, 12)
            }
            ninePatch(scale).use { image ->
                val pixels = painterPixels(12f, 12f) { canvas ->
                    paintImage(canvas, Rect.makeWH(12f, 12f), image, scale = scale,
                        centerSlice = Rect.makeXYWH(2f, 2f, 2f, 2f), fit = BoxFit.SCALE_DOWN)
                }
                assertCorners(pixels, 3, 3, 6, 6, inset = 0)
                expectColorAt(pixels, 0, 0, Color.WHITE)
            }
        }
    }

    @Test
    fun decoration_images_share_the_same_default_fits() {
        quadrants(2f).use { image ->
            val pixels = painterPixels(20f, 12f) { canvas ->
                DecorationImage(image = image, scale = 2f).createPainter()
                    .paint(canvas, Rect.makeWH(20f, 12f), clipPath = null)
            }
            assertCorners(pixels, 6, 2, 8, 8)
            expectColorAt(pixels, 5, 5, Color.WHITE)
        }
        ninePatch(2f).use { image ->
            val pixels = painterPixels(12f, 12f) { canvas ->
                DecorationImage(image = image, scale = 2f, centerSlice = Rect.makeXYWH(2f, 2f, 2f, 2f))
                    .createPainter().paint(canvas, Rect.makeWH(12f, 12f), clipPath = null)
            }
            assertCorners(pixels, 0, 0, 12, 12, inset = 0)
        }
    }
}
