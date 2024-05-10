package com.muedsa.snapshot.render

import com.muedsa.snapshot.layerToPixels
import com.muedsa.snapshot.painterToPicture
import com.muedsa.snapshot.pictureToPixels
import com.muedsa.snapshot.rendering.PictureLayer
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Rect
import kotlin.test.Test

class PictureLayerTest {

    @Test
    fun paint_test() {
        val size = 200f
        val picture = painterToPicture(width = size, height = size) {
            it.drawRect(
                Rect.makeXYWH(0f, 0f, size, size),
                paint = Paint().apply {
                    color = Color.RED
                    mode = PaintMode.FILL
                }
            )
        }
        val pictureLayer = PictureLayer().apply {
            this.picture = picture
        }

        val picturePixels = pictureToPixels(picture)
        val pictureByteArray = picturePixels.buffer.bytes

        val layerPixels = layerToPixels(size, size, pictureLayer)
        val layerByteArray = layerPixels.buffer.bytes

        assert(pictureByteArray.contentEquals(layerByteArray))
    }
}