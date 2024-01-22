package com.muedsa.snapshot

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.rendering.LayerPaintContext
import com.muedsa.snapshot.rendering.PaintingContext
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.widget.ProxyWidget
import com.muedsa.snapshot.widget.Widget
import org.jetbrains.skia.*
import kotlin.math.ceil


class Snapshot(
    val background: Int = Color.makeARGB(255, 255, 255, 255),
    val debug: Boolean = false,
    val content: Widget.() -> Unit,
) {

    var isDrawed: Boolean = false
        private set

    var image: Image? = null

    fun draw() {
        val rootWidget = ProxyWidget().apply(content)
        assert(rootWidget.widget != null)
        val rootRenderBox = rootWidget.createRenderBox()
        rootRenderBox.layout(constraints = BoxConstraints())
        val rootSize = rootRenderBox.definiteSize
        if (rootSize.isEmpty) {
            throw IllegalArgumentException("layout size is empty")
        }
        if (rootSize.isInfinite) {
            throw IllegalArgumentException("layout size is infinite")
        }
        val surface =
            Surface.makeRaster(ImageInfo.makeN32Premul(ceil(rootSize.width).toInt(), ceil(rootSize.height).toInt()))
        surface.canvas.clear(background)
        val context =
            PaintingContext.paintRoot(bounds = Offset.ZERO combine rootSize, renderBox = rootRenderBox, debug = debug)
        val layerPaintContext = LayerPaintContext(canvas = surface.canvas)
        context.containerLayer.paint(layerPaintContext)
        surface.flush()
        image = surface.makeImageSnapshot()
        surface.close()
        isDrawed = true
    }

    fun toJPEGImageBytes(): ByteArray {
        assert(isDrawed)
        assert(image != null)
        return image!!.encodeToData(format = EncodedImageFormat.JPEG)!!.bytes
    }

    fun toPNGImageBytes(): ByteArray {
        assert(isDrawed)
        assert(image != null)
        return image!!.encodeToData(format = EncodedImageFormat.PNG)!!.bytes
    }

    fun toWEBPImageBytes(): ByteArray {
        assert(isDrawed)
        assert(image != null)
        return image!!.encodeToData(format = EncodedImageFormat.WEBP)!!.bytes
    }
}