package com.muedsa.snapshot

import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.widget.Align
import com.muedsa.snapshot.widget.Padding
import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.*

fun main() {
    //textTest()

    val snapshot = Snapshot {
        Align {
            Padding(padding = EdgeInsets.ZERO) {
                Align {
                    Padding(padding = EdgeInsets.ZERO)
                }
            }
        }
    }
    snapshot.draw()
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
    paragraphBuilder.addPlaceholder(
        PlaceholderStyle(
            width = 50f,
            height = 50f,
            alignment = PlaceholderAlignment.MIDDLE,
            baselineMode = BaselineMode.ALPHABETIC,
            baseline = 50f
        )
    )
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
}