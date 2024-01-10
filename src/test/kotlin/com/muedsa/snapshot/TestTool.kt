package com.muedsa.snapshot

import com.muedsa.geometry.EdgeInsets
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

fun drawPainter(
    imagePathWithoutSuffix: String,
    width: Float,
    height: Float,
    background: Int = Color.WHITE,
    painter: (Canvas) ->Unit = {}
) {
    val surface = Surface.makeRaster(ImageInfo.makeN32Premul(ceil(width).toInt(), ceil(height).toInt()))
    surface.canvas.clear(background)
    painter.invoke(surface.canvas)
    val path = testImagesDirection.resolve("$imagePathWithoutSuffix.png")
    path.createParentDirectories()
    surface.makeImageSnapshot().use {
        path.toFile().writeBytes(it.encodeToData(EncodedImageFormat.PNG)!!.bytes)
    }
}
