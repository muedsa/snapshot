package com.muedsa.snapshot.render

import com.muedsa.snapshot.layerToPixels
import com.muedsa.snapshot.layersToPixels
import com.muedsa.snapshot.painterToPicture
import com.muedsa.snapshot.rendering.ContainerLayer
import com.muedsa.snapshot.rendering.PictureLayer
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Rect
import kotlin.test.Test
import kotlin.test.assertFalse

class ContainerLayerTest {

    @Test
    fun paint_test() {
        val size = 200f
        val rectSize = 150f

        val pictureLayer1 = PictureLayer().apply {
            this.picture = painterToPicture(width = size, height = size) {
                it.drawRect(Rect.makeXYWH(0f, 0f, rectSize, rectSize), paint = Paint().apply {
                    color = Color.RED
                    mode = PaintMode.FILL
                })
            }
        }

        val pictureLayer2 = PictureLayer().apply {
            this.picture = painterToPicture(width = size, height = size) {
                it.drawRect(Rect.makeXYWH(size - rectSize, size - rectSize, rectSize, rectSize), paint = Paint().apply {
                    color = Color.GREEN
                    mode = PaintMode.FILL
                })
            }
        }

        val layer1 = ContainerLayer().apply {
            append(pictureLayer1)
            append(pictureLayer2)
        }


        val layer2 = ContainerLayer().apply {
            append(pictureLayer2)
            append(pictureLayer1)
        }

        val list1Pixels = layersToPixels(size, size, listOf(pictureLayer1, pictureLayer2))
        val list1ByteArray = list1Pixels.buffer.bytes

        val list2Pixels = layersToPixels(size, size, listOf(pictureLayer2, pictureLayer1))
        val list2ByteArray = list2Pixels.buffer.bytes

        val layer1Pixels = layerToPixels(size, size, layer1)
        val layer1ByteArray = layer1Pixels.buffer.bytes

        val layer2Pixels = layerToPixels(size, size, layer2)
        val layer2ByteArray = layer2Pixels.buffer.bytes

        assert(list1ByteArray.contentEquals(layer1ByteArray))
        assertFalse(list2ByteArray.contentEquals(layer1ByteArray))
        assertFalse(list1ByteArray.contentEquals(layer2ByteArray))
        assert(list2ByteArray.contentEquals(layer2ByteArray))
    }
}