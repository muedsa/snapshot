package com.muedsa.snapshot.render

import com.muedsa.snapshot.layerToPixels
import com.muedsa.snapshot.painterToPicture
import com.muedsa.snapshot.rendering.OpacityLayer
import com.muedsa.snapshot.rendering.PictureLayer
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Rect
import kotlin.test.Test
import kotlin.test.assertFalse

class OpacityLayerTest {

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

        val layer = OpacityLayer(
            opacity = 0.5f
        ).apply {
            append(pictureLayer)
        }

        val picturePixels = layerToPixels(size, size, pictureLayer)
        val pictureByteArray = picturePixels.buffer.bytes

        val layerPixels = layerToPixels(size, size, layer)
        val layerByteArray = layerPixels.buffer.bytes

        assertFalse(pictureByteArray.contentEquals(layerByteArray))
    }
}