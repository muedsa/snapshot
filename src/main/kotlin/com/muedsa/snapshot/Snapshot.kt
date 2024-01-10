package com.muedsa.snapshot

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.rendering.PaintingContext
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.widget.SingleWidgetBuilder
import org.jetbrains.skia.*
import kotlin.math.ceil


class Snapshot(
    val background: Int = Color.makeARGB(255, 255, 255, 255),
    val debug: Boolean = false,
    val widgetBuilder: SingleWidgetBuilder
) {

    var isDrawed: Boolean = false
        private set

    var image: Image? = null

    fun draw() {
        val rootWidget = widgetBuilder.invoke()
        assert(rootWidget != null)
        val rootRenderBox = rootWidget!!.createRenderTree()
        rootRenderBox.layout(constraints = BoxConstraints())
        val rootSize = rootRenderBox.definiteSize
        if (rootSize.isEmpty) {
            throw IllegalArgumentException("layout size is empty")
        }
        if (rootSize.isInfinite) {
            throw IllegalArgumentException("layout size is infinite")
        }
        val surface = Surface.makeRaster(ImageInfo.makeN32Premul(ceil(rootSize.width).toInt(), ceil(rootSize.height).toInt()))
        surface.canvas.clear(background)
        val context = PaintingContext(canvas = surface.canvas, debug = debug)
        context.paintChild(rootRenderBox, Offset.ZERO)
        surface.flush()
        image = surface.makeImageSnapshot()
        surface.close()
        rootWidget.dispose()
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