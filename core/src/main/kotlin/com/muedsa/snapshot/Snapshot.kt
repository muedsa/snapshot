package com.muedsa.snapshot

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.rendering.LayerPaintContext
import com.muedsa.snapshot.rendering.PaintingContext
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.widget.ProxyWidget
import com.muedsa.snapshot.widget.Widget
import org.jetbrains.skia.*
import kotlin.math.ceil
import com.muedsa.snapshot.widget.ChildSlot

/**
 * @param background 背景
 * @param debug
 * @param initSurface 默认使用CPU渲染图片
 * @param content
 */
inline fun Snapshot(
    background: Int = Color.makeARGB(255, 255, 255, 255),
    debug: Boolean = false,
    initSurface: (Int, Int) -> Surface = { width, height ->
        Surface.makeRasterN32Premul(
            width = width,
            height = height
        )
    },
    content: ChildSlot.() -> Unit,
): Surface {
    val rootRenderBox = layoutWidget(content = content)
    val rootSize = rootRenderBox.definiteSize
    if (rootSize.isEmpty) {
        throw IllegalArgumentException("layout size is empty")
    }
    if (rootSize.isInfinite) {
        throw IllegalArgumentException("layout size is infinite")
    }
    val surface = initSurface(ceil(rootSize.width).toInt(), ceil(rootSize.height).toInt())
    surface.canvas.drawRenderBox(renderBox = rootRenderBox, background = background, debug = debug)
    surface.flushAndSubmit()
    return surface
}

inline fun SnapshotImage(
    background: Int = Color.makeARGB(255, 255, 255, 255),
    debug: Boolean = false,
    initSurface: (Int, Int) -> Surface = { width, height ->
        Surface.makeRasterN32Premul(
            width = width,
            height = height
        )
    },
    content: ChildSlot.() -> Unit,
): Image =
    Snapshot(background = background, debug = debug, initSurface = initSurface, content = content).makeImageSnapshot()

inline fun SnapshotJPEG(
    background: Int = Color.makeARGB(255, 255, 255, 255),
    debug: Boolean = false,
    initSurface: (Int, Int) -> Surface = { width, height ->
        Surface.makeRasterN32Premul(
            width = width,
            height = height
        )
    },
    content: ChildSlot.() -> Unit,
): ByteArray = SnapshotImage(background = background, debug = debug, initSurface = initSurface, content = content)
    .encodeToData(format = EncodedImageFormat.JPEG)!!.bytes

inline fun SnapshotPNG(
    background: Int = Color.makeARGB(255, 255, 255, 255),
    debug: Boolean = false,
    initSurface: (Int, Int) -> Surface = { width, height ->
        Surface.makeRasterN32Premul(
            width = width,
            height = height
        )
    },
    content: ChildSlot.() -> Unit,
): ByteArray = SnapshotImage(background = background, debug = debug, initSurface = initSurface, content = content)
    .encodeToData(format = EncodedImageFormat.PNG)!!.bytes

inline fun SnapshotWEBP(
    background: Int = Color.makeARGB(255, 255, 255, 255),
    debug: Boolean = false,
    initSurface: (Int, Int) -> Surface = { width, height ->
        Surface.makeRasterN32Premul(
            width = width,
            height = height
        )
    },
    content: ChildSlot.() -> Unit,
): ByteArray = SnapshotImage(background = background, debug = debug, initSurface = initSurface, content = content)
    .encodeToData(format = EncodedImageFormat.WEBP)!!.bytes

inline fun layoutWidget(
    content: ChildSlot.() -> Unit,
): RenderBox {
    val rootRenderBox = ProxyWidget.buildWidget(content).createRenderBox()
    rootRenderBox.layout(constraints = BoxConstraints())
    return rootRenderBox
}

fun Canvas.drawRenderBox(
    renderBox: RenderBox,
    background: Int,
    debug: Boolean = false,
) {
    clear(background)
    val context = PaintingContext.paintRoot(
        bounds = Offset.ZERO combine renderBox.definiteSize,
        renderBox = renderBox,
        debug = debug
    )
    LayerPaintContext.composite(context = context, canvas = this)
}
