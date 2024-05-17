package com.muedsa.snapshot

import com.muedsa.geometry.EdgeInsets
import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.paint.text.TextPainter
import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.rendering.Layer
import com.muedsa.snapshot.rendering.LayerPaintContext
import com.muedsa.snapshot.rendering.PaintingContext
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.widget.*
import com.muedsa.snapshot.widget.text.Text
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

val testImagesDirection: Path = Path.of("build/test-results/test-image-outputs").apply {
    if (!exists()) {
        createDirectory()
    }
}

val rootDirection: Path = Path.of("..")

fun getTestPngFile(imagePathWithoutSuffix: String): File {
    var suffix = imagePathWithoutSuffix
    if (imagePathWithoutSuffix.startsWith("/")) {
        suffix = suffix.substring(1)
    }
    val path = testImagesDirection.resolve("$suffix.png")
    path.createParentDirectories()
    return path.toFile()
}

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
                    Text(debugInfo)
                }
            }
        }
    }
    getTestPngFile(imagePathWithoutSuffix).writeBytes(snapshotImage.encodeToData(EncodedImageFormat.PNG)!!.bytes)
}

fun drawPainter(
    imagePathWithoutSuffix: String,
    width: Float,
    height: Float,
    debugInfo: String? = null,
    background: Int = Color.WHITE,
    painter: (Canvas) -> Unit = {},
) {
    var h = height
    val debugInfoPainter: TextPainter? = debugInfo?.let {
        TextPainter(
            text = TextSpan(
                text = debugInfo
            ),
        ).apply {
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

fun renderBoxToPixels(renderBox: RenderBox, background: Int = Color.WHITE): Pixmap {
    val size = renderBox.definiteSize
    val surface = Surface.makeRasterN32Premul(ceil(size.width).toInt(), ceil(size.height).toInt())
    surface.canvas.clear(background)
    LayerPaintContext.composite(
        PaintingContext.paintRoot(
            bounds = Offset.ZERO combine size,
            renderBox = renderBox
        ),
        surface.canvas
    )
    return surface.makeImageSnapshot().peekPixels()!!
}

fun painterToPicture(
    width: Float,
    height: Float,
    background: Int = Color.WHITE,
    painter: (Canvas) -> Unit = {},
): Picture {
    val record = PictureRecorder()
    val canvas = record.beginRecording(Rect.makeXYWH(0f, 0f, width, height))
    canvas.clear(background)
    painter(canvas)
    return record.finishRecordingAsPicture()
}

fun layerToPixels(width: Float, height: Float, layer: Layer): Pixmap {
    val surface = Surface.makeRasterN32Premul(ceil(width).toInt(), ceil(height).toInt())
    layer.paint(LayerPaintContext(surface.canvas))
    return surface.makeImageSnapshot().peekPixels()!!
}

fun layersToPixels(width: Float, height: Float, layers: List<Layer>): Pixmap {
    val surface = Surface.makeRasterN32Premul(ceil(width).toInt(), ceil(height).toInt())
    val context = LayerPaintContext(surface.canvas)
    layers.forEach { it.paint(context) }
    return surface.makeImageSnapshot().peekPixels()!!
}

fun pictureToPixels(picture: Picture): Pixmap {
    val rect = picture.cullRect
    val surface = Surface.makeRasterN32Premul(ceil(rect.width).toInt(), ceil(rect.height).toInt())
    surface.canvas.drawPicture(picture)
    return surface.makeImageSnapshot().peekPixels()!!
}
