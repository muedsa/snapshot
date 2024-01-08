package com.muedsa.snapshot

import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.widget.SingleWidgetBuilder
import com.muedsa.snapshot.widget.Widget
import org.jetbrains.skia.Color
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

fun drawWidget(filename: String, singleWidgetBuilder: SingleWidgetBuilder) {
    val snapshot = Snapshot(
        background = Color.TRANSPARENT,
        widgetBuilder = singleWidgetBuilder,
    )
    snapshot.draw()
    snapshot.toPNGImageBytes()
    val path = testImagesDirection.resolve("$filename.png")
    path.createParentDirectories()
    path.toFile().writeBytes(snapshot.toPNGImageBytes())
}