package com.muedsa.snapshot

import com.muedsa.geometry.EdgeInsets
import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.paint.text.SimpleTextPainter
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.widget.*
import org.jetbrains.skia.*
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

@OptIn(ExperimentalStdlibApi::class)
fun drawWidget(imagePathWithoutSuffix: String, debugInfo: String? = null, drawDebug: Boolean = false, singleWidgetBuilder: SingleWidgetBuilder) {
    var snapshot = Snapshot(
        background = Color.TRANSPARENT,
        widgetBuilder = singleWidgetBuilder,
        debug = drawDebug
    )
    snapshot.draw()
    val snapshotImage = snapshot.image!!
    if (!debugInfo.isNullOrEmpty()) {
        snapshot = Snapshot(
            background = Color.TRANSPARENT
        ) {
            Column { arrayOf(
                LocalImage(image = snapshotImage),
                Container(
                    padding = EdgeInsets.all(10f),
                    color = Color.WHITE,
                    constraints = BoxConstraints(
                        maxWidth = snapshotImage.width.toFloat()
                    )
                ) {
                    SimpleText(debugInfo)
                }
            ) }
        }
        snapshot.draw()
    }
    val path = testImagesDirection.resolve("$imagePathWithoutSuffix.png")
    path.createParentDirectories()
    path.toFile().writeBytes(snapshot.toPNGImageBytes())
}

@OptIn(ExperimentalStdlibApi::class)
fun drawPainter(
    imagePathWithoutSuffix: String,
    width: Float,
    height: Float,
    debugInfo: String? = null,
    background: Int = Color.WHITE,
    painter: (Canvas) ->Unit = {}
) {
    var h = height
    val debugInfoPainter: SimpleTextPainter? = debugInfo?.let {
        SimpleTextPainter(debugInfo).apply {
            layout(0f, width - 20f)
            h += this@apply.height + 20f
        }
    }
    val surface = Surface.makeRaster(ImageInfo.makeN32Premul(ceil(width).toInt(), ceil(h).toInt()))
    surface.canvas.clear(background)
    painter.invoke(surface.canvas)
    debugInfoPainter?.paint(surface.canvas, Offset(10f, height + 10f))
    var suffix = imagePathWithoutSuffix
    if (imagePathWithoutSuffix.startsWith("/")) {
        suffix = suffix.substring(1)
    }
    val path = testImagesDirection.resolve("$suffix.png")
    path.createParentDirectories()
    surface.makeImageSnapshot().use {
        path.toFile().writeBytes(it.encodeToData(EncodedImageFormat.PNG)!!.bytes)
    }
}

fun drawPainter(
    imagePathWithoutSuffix: String,
    size: Size,
    background: Int = Color.WHITE,
    debugInfo: String? = null,
    painter: (Canvas) ->Unit = {}
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