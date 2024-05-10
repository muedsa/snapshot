package com.muedsa.snapshot.render

import com.muedsa.snapshot.layerToPixels
import com.muedsa.snapshot.painterToPicture
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.ClipRectLayer
import com.muedsa.snapshot.rendering.PictureLayer
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class ClipRectLayerTest {

    @Test
    fun paint_test() {
        val size = 200f
        val halfSize = size / 2
        val quarterSize = size / 4
        val eighthSize = size / 8

        val pictureLayer = PictureLayer().apply {
            this.picture = painterToPicture(width = size, height = size) {
                it.drawRect(
                    Rect.makeXYWH(0f, 0f, size, size),
                    paint = Paint().apply {
                        color = Color.RED
                        mode = PaintMode.FILL
                    }
                )
            }
        }

        val clipLayer = ClipRectLayer(
            clipBehavior = ClipBehavior.ANTI_ALIAS,
            clipRect = Rect.makeXYWH(quarterSize, quarterSize, halfSize, halfSize) // clip ▣
        ).apply {
            append(pictureLayer)
        }

        val originPixels = layerToPixels(size, size, pictureLayer)
        val originByteArray = originPixels.buffer.bytes

        val afterClipPixels = layerToPixels(size, size, clipLayer)
        val afterClipByteArray = afterClipPixels.buffer.bytes

        assertFalse(originByteArray.contentEquals(afterClipByteArray))

        val offset = eighthSize.toInt()
        // clip inner
        assertEquals(
            originPixels.getColor(x = offset * 3, y = offset * 3),
            afterClipPixels.getColor(x = offset * 3, y = offset * 3)
        )

        assertEquals(
            originPixels.getColor(x = offset * 4, y = offset * 4),
            afterClipPixels.getColor(x = offset * 4, y = offset * 4)
        )
        assertEquals(
            originPixels.getColor(x = offset * 5, y = offset * 5),
            afterClipPixels.getColor(x = offset * 5, y = offset * 5)
        )

        // clip outer
        assertNotEquals(
            originPixels.getColor(x = offset, y = offset),
            afterClipPixels.getColor(x = offset, y = offset)
        )
        assertNotEquals(
            originPixels.getColor(x = offset * 7, y = offset * 7),
            afterClipPixels.getColor(x = offset * 7, y = offset * 7)
        )
    }
}