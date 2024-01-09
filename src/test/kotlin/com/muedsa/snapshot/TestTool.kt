package com.muedsa.snapshot

import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.flex.CrossAxisAlignment
import com.muedsa.snapshot.rendering.flex.MainAxisAlignment
import com.muedsa.snapshot.widget.*
import org.jetbrains.skia.Color
import org.junit.jupiter.api.DisplayNameGenerator.Simple
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

fun noLimitedLayout(renderBox: RenderBox) {
    renderBox.layout(BoxConstraints())
}


val testImagesDirection: Path = Path.of("testOutputs").apply {
    if (!exists()) {
        createDirectory()
    }
}

fun drawWidget(filename: String, debugInfo: String? = null, singleWidgetBuilder: SingleWidgetBuilder) {
    var snapshot = Snapshot(
        background = Color.TRANSPARENT,
        widgetBuilder = singleWidgetBuilder
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
    val path = testImagesDirection.resolve("$filename.png")
    path.createParentDirectories()
    path.toFile().writeBytes(snapshot.toPNGImageBytes())
}