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
        val pictureLayer = PictureLayer().apply {
            this.picture = painterToPicture(width = 200f, height = 200f) {
                it.drawRect(Rect.makeXYWH(0f, 0f, 100f, 100f), paint = Paint().apply {
                    color = Color.RED
                    mode = PaintMode.FILL
                })
                it.drawRect(Rect.makeXYWH(100f, 0f, 100f, 100f), paint = Paint().apply {
                    color = Color.GREEN
                    mode = PaintMode.FILL
                })
                it.drawRect(Rect.makeXYWH(0f, 100f, 100f, 100f), paint = Paint().apply {
                    color = Color.BLUE
                    mode = PaintMode.FILL
                })
                it.drawRect(Rect.makeXYWH(100f, 100f, 100f, 100f), paint = Paint().apply {
                    color = Color.YELLOW
                    mode = PaintMode.FILL
                })
            }
        }

        val layer = ContainerLayer().apply {
            append(pictureLayer)
            append(
                BackdropFilterLayer(
                    bounds = Rect.makeXYWH(50f, 50f, 100f, 100f),
                    filter = ImageFilter.makeBlur(15f, 15f, mode = FilterTileMode.CLAMP),
                    blendMode = BlendMode.SRC
                )
            )
        }

        val picturePixels = layerToPixels(200f, 200f, pictureLayer)
        val pictureByteArray = picturePixels.buffer.bytes

        val layerPixels = layerToPixels(200f, 200f, layer)
        val layerByteArray = layerPixels.buffer.bytes

        assertFalse(pictureByteArray.contentEquals(layerByteArray))
    }
}