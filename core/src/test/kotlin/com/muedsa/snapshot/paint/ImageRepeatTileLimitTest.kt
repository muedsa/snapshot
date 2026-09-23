package com.muedsa.snapshot.paint

import com.muedsa.snapshot.painterImage
import org.jetbrains.skia.Color
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ImageRepeatTileLimitTest {

    @Test
    fun repeat_modes_respect_the_configured_global_limit() {
        val previousLimit = ImageRepeatConfig.maxTileCount
        try {
            val output = Rect.makeWH(3f, 3f)
            val tile = Rect.makeWH(2f, 2f)
            ImageRepeatConfig.maxTileCount = 3
            assertEquals(2, generateImageTileRects(output, tile, ImageRepeat.REPEAT_X).size)
            assertEquals(2, generateImageTileRects(output, tile, ImageRepeat.REPEAT_Y).size)
            assertEquals(1, generateImageTileRects(output, tile, ImageRepeat.NO_REPEAT).size)
            assertFailsWith<IllegalArgumentException> {
                generateImageTileRects(output, tile, ImageRepeat.REPEAT)
            }

            ImageRepeatConfig.maxTileCount = 4
            assertEquals(4, generateImageTileRects(output, tile, ImageRepeat.REPEAT).size)
            ImageRepeatConfig.maxTileCount = 3
            assertFailsWith<IllegalArgumentException> {
                generateImageTileRects(output, tile, ImageRepeat.REPEAT)
            }
        } finally {
            ImageRepeatConfig.maxTileCount = previousLimit
        }
    }

    @Test
    fun tiny_positive_tile_fails_before_allocating_or_drawing() {
        val previousLimit = ImageRepeatConfig.maxTileCount
        val image = painterImage(1f, 1f, Color.RED) { }
        val surface = Surface.makeRasterN32Premul(4, 4)
        try {
            ImageRepeatConfig.maxTileCount = 10
            for (repeat in listOf(ImageRepeat.REPEAT, ImageRepeat.REPEAT_X, ImageRepeat.REPEAT_Y)) {
                val error = assertFailsWith<IllegalArgumentException> {
                    generateImageTileRects(Rect.makeWH(4f, 4f), Rect.makeWH(0.000001f, 0.000001f), repeat)
                }
                assertTrue(error.message.orEmpty().contains("configured limit"))
            }

            assertFailsWith<IllegalArgumentException> {
                paintImage(
                    canvas = surface.canvas,
                    rect = Rect.makeWH(4f, 4f),
                    image = image,
                    fit = BoxFit.NONE,
                    repeat = ImageRepeat.REPEAT,
                )
            }
        } finally {
            surface.close()
            image.close()
            ImageRepeatConfig.maxTileCount = previousLimit
        }
    }

    @Test
    fun global_limit_must_be_positive() {
        val previousLimit = ImageRepeatConfig.maxTileCount
        try {
            assertFailsWith<IllegalArgumentException> { ImageRepeatConfig.maxTileCount = 0 }
            assertFailsWith<IllegalArgumentException> { ImageRepeatConfig.maxTileCount = -1 }
            assertEquals(previousLimit, ImageRepeatConfig.maxTileCount)
        } finally {
            ImageRepeatConfig.maxTileCount = previousLimit
        }
    }

    @Test
    fun tile_index_overflow_is_rejected_even_when_count_is_under_limit() {
        val previousLimit = ImageRepeatConfig.maxTileCount
        try {
            ImageRepeatConfig.maxTileCount = 100_000
            val error = assertFailsWith<IllegalArgumentException> {
                generateImageTileRects(
                    Rect.makeXYWH(10_000f, 0f, 0.001f, 1f),
                    Rect.makeWH(0.0000001f, 2f),
                    ImageRepeat.REPEAT_X,
                )
            }
            assertTrue(error.message.orEmpty().contains("integer range"))
        } finally {
            ImageRepeatConfig.maxTileCount = previousLimit
        }
    }
}
