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
}
