package com.muedsa.snapshot.render

import com.muedsa.snapshot.layerToPixels
import com.muedsa.snapshot.painterToPicture
import com.muedsa.snapshot.rendering.BackdropFilterLayer
import com.muedsa.snapshot.rendering.ContainerLayer
import com.muedsa.snapshot.rendering.PictureLayer
import org.jetbrains.skia.*
import kotlin.test.Test
import kotlin.test.assertFalse

class BackdropFilterLayerTest {

    @Test
    fun paint_test() {
        val size = 200f

        val pictureLayer = PictureLayer().apply {
            this.picture = painterToPicture(width = size, height = size) {
                it.drawRect(Rect.makeXYWH(0f, 0f, size, size), paint = Paint().apply {
                    color = Color.RED
                    mode = PaintMode.FILL
                })
            }
        }

        val layer = ContainerLayer().apply {
            append(pictureLayer)
            append(
                BackdropFilterLayer(
                    bounds = Rect.makeXYWH(0f, 0f, size, size),
                    filter = ImageFilter.makeBlur(15f, 15f, mode = FilterTileMode.CLAMP),
                    blendMode = BlendMode.SRC
                )
            )
        }

        val picturePixels = layerToPixels(size, size, pictureLayer)
        val pictureByteArray = picturePixels.buffer.bytes

        val layerPixels = layerToPixels(size, size, layer)
        val layerByteArray = layerPixels.buffer.bytes

        assertFalse(pictureByteArray.contentEquals(layerByteArray))
    }
}