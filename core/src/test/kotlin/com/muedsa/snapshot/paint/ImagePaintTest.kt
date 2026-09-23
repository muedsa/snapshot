package com.muedsa.snapshot.paint

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.painterImage
import com.muedsa.snapshot.painterPixels
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Color
import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import kotlin.test.Test
import kotlin.test.assertTrue

class ImagePaintTest {

    private fun quadrantImage(): Image = painterImage(8f, 8f) { canvas ->
        val paint = Paint()
        for ((rect, color) in listOf(
            Rect.makeXYWH(0f, 0f, 4f, 4f) to Color.RED,
            Rect.makeXYWH(4f, 0f, 4f, 4f) to Color.GREEN,
            Rect.makeXYWH(0f, 4f, 4f, 4f) to Color.BLUE,
            Rect.makeXYWH(4f, 4f, 4f, 4f) to Color.YELLOW,
        )) {
            paint.color = color
            canvas.drawRect(rect, paint)
        }
    }

    private fun ninePatchImage(patchSize: Float = 2f): Image =
        painterImage(patchSize * 3f, patchSize * 3f, Color.MAGENTA) { canvas ->
            val paint = Paint()
            for ((rect, color) in listOf(
                Rect.makeXYWH(0f, 0f, patchSize, patchSize) to Color.RED,
                Rect.makeXYWH(2f * patchSize, 0f, patchSize, patchSize) to Color.GREEN,
                Rect.makeXYWH(0f, 2f * patchSize, patchSize, patchSize) to Color.BLUE,
                Rect.makeXYWH(2f * patchSize, 2f * patchSize, patchSize, patchSize) to Color.YELLOW,
                Rect.makeXYWH(patchSize, patchSize, patchSize, patchSize) to Color.CYAN,
            )) {
                paint.color = color
                canvas.drawRect(rect, paint)
            }
        }

    @Test
    fun contain_and_alignment_place_the_entire_image() {
        val image = quadrantImage()
        try {
            for ((alignment, imageX) in listOf(BoxAlignment.TOP_LEFT to 0, BoxAlignment.BOTTOM_RIGHT to 4)) {
                val pixels = painterPixels(12f, 8f) { canvas ->
                    paintImage(canvas, Rect.makeWH(12f, 8f), image, fit = BoxFit.CONTAIN, alignment = alignment)
                }
                expectColorAt(pixels, imageX + 1, 1, Color.RED)
                expectColorAt(pixels, imageX + 6, 1, Color.GREEN)
                expectColorAt(pixels, imageX + 1, 6, Color.BLUE)
                expectColorAt(pixels, imageX + 6, 6, Color.YELLOW)
                expectColorAt(pixels, if (imageX == 0) 11 else 0, 3, Color.WHITE)
            }
        } finally {
            image.close()
        }
    }

    @Test
    fun none_fit_crops_the_source_according_to_alignment() {
        val image = quadrantImage()
        try {
            for ((alignment, expected) in listOf(
                BoxAlignment.TOP_LEFT to Color.RED,
                BoxAlignment.BOTTOM_RIGHT to Color.YELLOW,
            )) {
                val pixels = painterPixels(4f, 4f) { canvas ->
                    paintImage(canvas, Rect.makeWH(4f, 4f), image, fit = BoxFit.NONE, alignment = alignment)
                }
                expectColorAt(pixels, 1, 1, expected)
                expectColorAt(pixels, 2, 2, expected)
            }
        } finally {
            image.close()
        }
    }

    @Test
    fun repeat_modes_tile_only_on_the_selected_axes() {
        val image = quadrantImage()
        try {
            for (repeat in listOf(ImageRepeat.REPEAT, ImageRepeat.REPEAT_X, ImageRepeat.REPEAT_Y)) {
                val pixels = painterPixels(20f, 20f) { canvas ->
                    paintImage(
                        canvas, Rect.makeWH(20f, 20f), image,
                        fit = BoxFit.NONE, alignment = BoxAlignment.TOP_LEFT, repeat = repeat,
                    )
                }
                expectColorAt(pixels, 1, 1, Color.RED)
                expectColorAt(pixels, 9, 1, if (repeat == ImageRepeat.REPEAT_Y) Color.WHITE else Color.RED)
                expectColorAt(pixels, 1, 9, if (repeat == ImageRepeat.REPEAT_X) Color.WHITE else Color.RED)
                expectColorAt(pixels, 17, 17, if (repeat == ImageRepeat.REPEAT) Color.RED else Color.WHITE)
            }
        } finally {
            image.close()
        }
    }

    @Test
    fun opacity_and_color_filter_affect_painted_pixels() {
        val image = quadrantImage()
        try {
            val translucent = painterPixels(8f, 8f, background = Color.TRANSPARENT) { canvas ->
                paintImage(canvas, Rect.makeWH(8f, 8f), image, opacity = 0.5f)
            }
            val alpha = translucent.getColor(1, 1) ushr 24
            assertTrue(alpha in 126..128, "透明度应约为 50%，实际 alpha=$alpha")

            val filter = ColorFilter.makeBlend(Color.MAGENTA, BlendMode.SRC_IN)
            try {
                val tinted = painterPixels(8f, 8f) { canvas ->
                    paintImage(canvas, Rect.makeWH(8f, 8f), image, colorFilter = filter)
                }
                expectColorAt(tinted, 1, 1, Color.MAGENTA)
                expectColorAt(tinted, 6, 6, Color.MAGENTA)
            } finally {
                filter.close()
            }
        } finally {
            image.close()
        }
    }

    @Test
    fun center_slice_stretches_the_center_without_changing_corners() {
        val image = ninePatchImage()
        try {
            val pixels = painterPixels(12f, 12f) { canvas ->
                paintImage(
                    canvas, Rect.makeWH(12f, 12f), image,
                    centerSlice = Rect.makeXYWH(2f, 2f, 2f, 2f), fit = BoxFit.FILL,
                )
            }
            expectColorAt(pixels, 0, 0, Color.RED)
            expectColorAt(pixels, 11, 0, Color.GREEN)
            expectColorAt(pixels, 0, 11, Color.BLUE)
            expectColorAt(pixels, 11, 11, Color.YELLOW)
            expectColorAt(pixels, 6, 6, Color.CYAN)
            expectColorAt(pixels, 6, 0, Color.MAGENTA)
            expectColorAt(pixels, 0, 6, Color.MAGENTA)
        } finally {
            image.close()
        }
    }

    @Test
    fun center_slice_can_repeat_without_losing_the_clip() {
        val image = ninePatchImage()
        try {
            val pixels = painterPixels(16f, 16f) { canvas ->
                paintImage(
                    canvas, Rect.makeXYWH(1f, 1f, 14f, 14f), image,
                    centerSlice = Rect.makeXYWH(2f, 2f, 2f, 2f),
                    fit = BoxFit.SCALE_DOWN, alignment = BoxAlignment.TOP_LEFT, repeat = ImageRepeat.REPEAT,
                )
            }
            expectColorAt(pixels, 1, 1, Color.RED)
            expectColorAt(pixels, 7, 1, Color.RED)
            expectColorAt(pixels, 13, 13, Color.RED)
            expectColorAt(pixels, 14, 14, Color.RED)
            expectColorAt(pixels, 15, 14, Color.WHITE)
        } finally {
            image.close()
        }
    }

    @Test
    fun center_slice_uses_logical_coordinates_for_scaled_images() {
        val image = ninePatchImage(patchSize = 4f)
        try {
            val pixels = painterPixels(12f, 12f) { canvas ->
                paintImage(
                    canvas, Rect.makeWH(12f, 12f), image,
                    scale = 2f, centerSlice = Rect.makeXYWH(2f, 2f, 2f, 2f), fit = BoxFit.FILL,
                )
            }
            expectColorAt(pixels, 1, 1, Color.RED)
            expectColorAt(pixels, 10, 1, Color.GREEN)
            expectColorAt(pixels, 1, 10, Color.BLUE)
            expectColorAt(pixels, 10, 10, Color.YELLOW)
            expectColorAt(pixels, 6, 6, Color.CYAN)
        } finally {
            image.close()
        }
    }

    @Test
    fun horizontal_flip_mirrors_pixels_and_restores_the_canvas() {
        val image = quadrantImage()
        try {
            val pixels = painterPixels(12f, 8f) { canvas ->
                paintImage(canvas, Rect.makeXYWH(2f, 0f, 8f, 8f), image, flipHorizontally = true)
                canvas.drawRect(Rect.makeXYWH(0f, 0f, 1f, 1f), Paint().apply { color = Color.BLACK })
            }
            expectColorAt(pixels, 3, 1, Color.GREEN)
            expectColorAt(pixels, 8, 1, Color.RED)
            expectColorAt(pixels, 3, 6, Color.YELLOW)
            expectColorAt(pixels, 8, 6, Color.BLUE)
            expectColorAt(pixels, 0, 0, Color.BLACK)
            expectColorAt(pixels, 11, 1, Color.WHITE)
        } finally {
            image.close()
        }
    }

    @Test
    fun multiply_blend_mode_combines_image_and_background() {
        val image = quadrantImage()
        try {
            val pixels = painterPixels(8f, 8f, background = Color.BLUE) { canvas ->
                paintImage(canvas, Rect.makeWH(8f, 8f), image, blendMode = BlendMode.MULTIPLY)
            }
            expectColorAt(pixels, 1, 1, Color.BLACK)
            expectColorAt(pixels, 1, 6, Color.BLUE)
        } finally {
            image.close()
        }
    }
}
