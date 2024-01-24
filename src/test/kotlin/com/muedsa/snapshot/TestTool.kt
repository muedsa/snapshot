package com.muedsa.snapshot

import com.muedsa.geometry.EdgeInsets
import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.paint.text.SimpleTextPainter
import com.muedsa.snapshot.rendering.LayerPaintContext
import com.muedsa.snapshot.rendering.PaintingContext
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.widget.*
import org.jetbrains.skia.*
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.math.ceil

fun noLimitedLayout(renderBox: RenderBox) {
    renderBox.layout(BoxConstraints())
}

val testImagesDirection: Path = Path.of("testOutputs").apply {
    if (!exists()) {
        createDirectory()
    }
}

fun getTestPngFile(imagePathWithoutSuffix: String): File {
    var suffix = imagePathWithoutSuffix
    if (imagePathWithoutSuffix.startsWith("/")) {
        suffix = suffix.substring(1)
    }
    val path = testImagesDirection.resolve("$suffix.png")
    path.createParentDirectories()
    return path.toFile()
}

@OptIn(ExperimentalStdlibApi::class)
fun drawWidget(
    imagePathWithoutSuffix: String,
    debugInfo: String? = null,
    drawDebug: Boolean = false,
    content: Widget.() -> Unit,
) {
    var snapshotImage = SnapshotImage(
        background = Color.TRANSPARENT,
        debug = drawDebug,
        content = content
    )
    if (!debugInfo.isNullOrEmpty()) {
        snapshotImage = SnapshotImage(
            background = Color.TRANSPARENT
        ) {
            Column {
                RawImage(image = snapshotImage)
                Container(
                    padding = EdgeInsets.all(10f),
                    color = Color.WHITE,
                    constraints = BoxConstraints(
                        maxWidth = snapshotImage.width.toFloat()
                    )
                ) {
                    SimpleText(debugInfo)
                }
            }
        }
    }
    getTestPngFile(imagePathWithoutSuffix).writeBytes(snapshotImage.encodeToData(EncodedImageFormat.PNG)!!.bytes)
}

@OptIn(ExperimentalStdlibApi::class)
fun drawPainter(
    imagePathWithoutSuffix: String,
    width: Float,
    height: Float,
    debugInfo: String? = null,
    background: Int = Color.WHITE,
    painter: (Canvas) -> Unit = {},
) {
    var h = height
    val debugInfoPainter: SimpleTextPainter? = debugInfo?.let {
        SimpleTextPainter(debugInfo).apply {
            layout(0f, width - 20f)
            h += this@apply.height + 20f
        }
    }
    val surface = Surface.makeRasterN32Premul(ceil(width).toInt(), ceil(h).toInt())
    surface.canvas.clear(background)
    painter.invoke(surface.canvas)
    debugInfoPainter?.paint(surface.canvas, Offset(10f, height + 10f))
    getTestPngFile(imagePathWithoutSuffix).writeBytes(
        surface.makeImageSnapshot().encodeToData(EncodedImageFormat.PNG)!!.bytes
    )
}

fun drawPainter(
    imagePathWithoutSuffix: String,
    size: Size,
    background: Int = Color.WHITE,
    debugInfo: String? = null,
    painter: (Canvas) -> Unit = {},
) {
    drawPainter(
        imagePathWithoutSuffix = imagePathWithoutSuffix,
        width = size.width,
        height = size.height,
        background = background,
        debugInfo = debugInfo,
        painter = painter
    )
}

fun renderBoxToPixels(renderBox: RenderBox): Pixmap {
    val size = renderBox.definiteSize
    val surface = Surface.makeRasterN32Premul(ceil(size.width).toInt(), ceil(size.width).toInt())
    surface.canvas.clear(Color.WHITE)
    LayerPaintContext.composite(
        PaintingContext.paintRoot(
            bounds = Offset.ZERO combine size,
            renderBox = renderBox
        ), surface.canvas
    )
    return surface.makeImageSnapshot().peekPixels()!!
}