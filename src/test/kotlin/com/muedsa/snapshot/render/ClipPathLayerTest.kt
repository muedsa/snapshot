package com.muedsa.snapshot.render

import com.muedsa.snapshot.layerToPixels
import com.muedsa.snapshot.painterToPicture
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.ClipPathLayer
import com.muedsa.snapshot.rendering.PictureLayer
import org.jetbrains.skia.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class ClipPathLayerTest {

    @Test
    fun paint_test() {
        val size = 200f

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

        val clipLayer = ClipPathLayer(
            clipBehavior = ClipBehavior.ANTI_ALIAS,
            clipPath = Path()
                .lineTo(size, 0f)
                .lineTo(size, size)
                .lineTo(0f, 0f) // clip ◥
        ).apply {
            append(pictureLayer)
        }

        val originPixels = layerToPixels(size, size, pictureLayer)
        val originByteArray = originPixels.buffer.bytes

        val afterClipPixels = layerToPixels(size, size, clipLayer)
        val afterClipByteArray = afterClipPixels.buffer.bytes

        assertFalse(originByteArray.contentEquals(afterClipByteArray))

        val offset = (size / 4).toInt()
        assertEquals(
            originPixels.getColor(x = offset * 3, y = offset),
            afterClipPixels.getColor(x = offset * 3, y = offset)
        )
        assertNotEquals(
            originPixels.getColor(x = offset, y = offset * 3),
            afterClipPixels.getColor(x = offset, y = offset * 3)
        )
    }
}