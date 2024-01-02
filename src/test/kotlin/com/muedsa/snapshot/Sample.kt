package com.muedsa.snapshot

import com.muedsa.geometry.Alignment
import com.muedsa.snapshot.paint.*
import com.muedsa.snapshot.tools.NetworkImageCacheManager
import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.SizedBox
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface

fun sample() {
    val snapshot = Snapshot(background = Color.WHITE) {
        SizedBox(
            width = 500f,
            height = 500f,
        ) {
            Container(
                decoration = BoxDecoration(
                    color = 0xFF_7C_94_b6.toInt(),
                    image = DecorationImage(
                        image = Image.makeFromEncoded(NetworkImageCacheManager.defaultCache.getImage("https://flutter.github.io/assets-for-api-docs/assets/widgets/owl-2.jpg")),
                        fit = BoxFit.COVER
                    ),
                    border = Border.all(width = 10f),
                    borderRadius = BorderRadius.circular(50f)
                )
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