package com.muedsa.snapshot

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.rendering.LayerPaintContext
import com.muedsa.snapshot.rendering.PaintingContext
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.widget.ProxyWidget
import com.muedsa.snapshot.widget.Widget
import org.jetbrains.skia.*
import kotlin.math.ceil

inline fun Snapshot(
    background: Int = Color.makeARGB(255, 255, 255, 255),
    debug: Boolean = false,
    content: Widget.() -> Unit,
): Surface {
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
    val surface = Surface.makeRaster(
        imageInfo = ImageInfo.makeN32Premul(
            width = ceil(rootSize.width).toInt(),
            height = ceil(rootSize.height).toInt()
        )
    )
    surface.canvas.clear(background)
    val context = PaintingContext.paintRoot(
        bounds = Offset.ZERO combine rootSize,
        renderBox = rootRenderBox,
        debug = debug
    )
    LayerPaintContext.composite(context = context, canvas = surface.canvas)
    surface.flush()
    return surface
}

inline fun SnapshotImage(
    background: Int = Color.makeARGB(255, 255, 255, 255),
    debug: Boolean = false,
    content: Widget.() -> Unit,
): Image = Snapshot(background = background, debug = debug, content = content).makeImageSnapshot()

inline fun SnapshotJPEG(
    background: Int = Color.makeARGB(255, 255, 255, 255),
    debug: Boolean = false,
    content: Widget.() -> Unit,
): ByteArray = SnapshotImage(background = background, debug = debug, content = content)
    .encodeToData(format = EncodedImageFormat.JPEG)!!.bytes

inline fun SnapshotPNG(
    background: Int = Color.makeARGB(255, 255, 255, 255),
    debug: Boolean = false,
    content: Widget.() -> Unit,
): ByteArray = SnapshotImage(background = background, debug = debug, content = content)
    .encodeToData(format = EncodedImageFormat.PNG)!!.bytes

inline fun SnapshotWEBP(
    background: Int = Color.makeARGB(255, 255, 255, 255),
    debug: Boolean = false,
    content: Widget.() -> Unit,
): ByteArray = SnapshotImage(background = background, debug = debug, content = content)
    .encodeToData(format = EncodedImageFormat.WEBP)!!.bytes