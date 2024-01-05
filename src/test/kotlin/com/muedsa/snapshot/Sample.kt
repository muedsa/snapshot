package com.muedsa.snapshot

import com.muedsa.geometry.Alignment
import com.muedsa.snapshot.paint.*
import com.muedsa.snapshot.paint.decoration.Border
import com.muedsa.snapshot.paint.decoration.BorderRadius
import com.muedsa.snapshot.paint.decoration.BoxDecoration
import com.muedsa.snapshot.paint.decoration.DecorationImage
import com.muedsa.snapshot.paint.gradient.LinearGradient
import com.muedsa.snapshot.paint.gradient.RadialGradient
import com.muedsa.snapshot.paint.gradient.SweepGradient
import com.muedsa.snapshot.tools.NetworkImageCacheManager
import com.muedsa.snapshot.widget.Align
import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.SizedBox
import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.*

fun sample() {
    val snapshot = Snapshot(background = Color.WHITE) {
        SizedBox(
            width = 500f,
            height = 500f,
        ) {
            Container(
                decoration = BoxDecoration(
                    gradient = SweepGradient(
                        center = Alignment.CENTER,
                        colors = intArrayOf(
                            0xFF4285F4.toInt(),
                            0xFF34A853.toInt(),
                            0xFFFBBC05.toInt(),
                            0xFFEA4335.toInt(),
                            0xFF4285F4.toInt(),
                        ),
                        stops = floatArrayOf(0f, 0.25f, 0.5f, 0.75f, 1f),
                    )
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
    //logo()
    //textTest()
}

fun logo() {
    val snapshot = Snapshot(background = Color.WHITE) {
        SizedBox(
            width = 300f,
            height = 300f,
        ) {
            Align(
                alignment = Alignment.BOTTOM_RIGHT
            ) {
                Container(
                    width = 200f,
                    height = 200f,
                    decoration = BoxDecoration(
                        color = 0xFF_7C_94_b6.toInt(),
                        border = Border.all(width = 10f),
                        borderRadius = BorderRadius.circular(100f)
                    )
                )
            }

        }
    }
    snapshot.draw()
    val filePath = java.nio.file.Path.of("logo.png")
    filePath.toFile().apply {
        writeBytes(snapshot.toPNGImageBytes())
    }
}

fun textTest() {
    val surface = Surface.makeRaster(ImageInfo.makeN32Premul(500, 500))
    val canvas = surface.canvas
    canvas.clear(Color.WHITE)
    val paragraphStyle = ParagraphStyle().apply {
        strutStyle = StrutStyle().apply {

        }
    }
    val fontCollection = FontCollection().apply {
        setDefaultFontManager(FontMgr.default)
        setEnableFallback(true)
    }
    val paragraphBuilder = ParagraphBuilder(style = paragraphStyle, fc = fontCollection)
    paragraphBuilder.pushStyle(TextStyle().apply {
        color = Color.BLACK
        fontSize = 15f
    })
    paragraphBuilder.addText("查询:")
    paragraphBuilder.pushStyle(TextStyle().apply {
        color = Color.RED
        fontSize = 20f
    })
    paragraphBuilder.addText("标题")
    paragraphBuilder.popStyle()
    paragraphBuilder.addText("（这里是内容）")
    val paragraph = paragraphBuilder.build()
    paragraph.layout(500f - 20f)
    paragraph.paint(canvas = canvas, x = 0f, y = 0f)
    surface.flush()
    val image: Image = surface.makeImageSnapshot()
    val filePath = java.nio.file.Path.of("text.png")
    filePath.toFile().apply {
        writeBytes(image.encodeToData(format = EncodedImageFormat.PNG)!!.bytes)
    }
    // val record: PictureRecorder = PictureRecorder()
}