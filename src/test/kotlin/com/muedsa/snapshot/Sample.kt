package com.muedsa.snapshot

import com.muedsa.geometry.Alignment
import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.SizedBox
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Surface

fun sample() {
    val snapshot = Snapshot(background = Color.WHITE) {
        SizedBox(
            width = 500f,
            height = 500f
        ) {
            Container(
                alignment = Alignment.BOTTOM_RIGHT,
                width = 250f,
                height = 250f,
                color = Color.makeARGB((255 * 0.7).toInt(), 255, 69, 0)
            )
        }
    }
    snapshot.draw()
    val filePath = java.nio.file.Path.of("test.png")
    filePath.toFile().apply {
        writeBytes(snapshot.toPNGImageBytes())
    }
}

fun main() {
    sample()

}