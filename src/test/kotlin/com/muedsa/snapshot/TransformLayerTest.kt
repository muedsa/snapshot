package com.muedsa.snapshot

import com.muedsa.geometry.Matrix44CMO
import com.muedsa.snapshot.rendering.PictureLayer
import com.muedsa.snapshot.rendering.TransformLayer
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Rect
import kotlin.test.Test
import kotlin.test.assertFalse

class TransformLayerTest {

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

        val layer = TransformLayer(
            transform = Matrix44CMO.translationValues(x = 10f, y = 10f, z = 0f)
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