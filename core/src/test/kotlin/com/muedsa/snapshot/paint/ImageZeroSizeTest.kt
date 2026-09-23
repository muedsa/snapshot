package com.muedsa.snapshot.paint

import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.painterImage
import com.muedsa.snapshot.painterPixels
import org.jetbrains.skia.Color
import org.jetbrains.skia.Rect
import kotlin.test.Test
import kotlin.test.assertTrue

class ImageZeroSizeTest {

    @Test
    fun zero_area_target_does_not_draw_or_generate_tiles() {
        val image = painterImage(2f, 2f, Color.RED) { }
        try {
            for (repeat in ImageRepeat.entries) {
                for (rect in listOf(Rect.makeXYWH(1f, 1f, 0f, 3f), Rect.makeXYWH(1f, 1f, 3f, 0f))) {
                    val pixels = painterPixels(5f, 5f) { canvas ->
                        paintImage(canvas = canvas, rect = rect, image = image, repeat = repeat)
                    }
                    expectColorAt(pixels, 1, 1, Color.WHITE)
                    expectColorAt(pixels, 3, 3, Color.WHITE)
                }
            }
        } finally {
            image.close()
        }
    }

    @Test
    fun tile_generator_rejects_zero_or_non_finite_step() {
        val output = Rect.makeWH(10f, 10f)
        for (repeat in ImageRepeat.entries) {
            assertTrue(generateImageTileRects(output, Rect.makeWH(0f, 2f), repeat).isEmpty())
            assertTrue(generateImageTileRects(output, Rect.makeWH(2f, 0f), repeat).isEmpty())
            assertTrue(generateImageTileRects(output, Rect.makeWH(Float.POSITIVE_INFINITY, 2f), repeat).isEmpty())
        }
    }
}
