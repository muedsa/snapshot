package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.painterImage
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.snapshotPixels
import com.muedsa.snapshot.widget.RawImage
import com.muedsa.snapshot.widget.SizedBox
import org.jetbrains.skia.Color
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ImageScaleParserTest {
    @Test
    fun omitted_fit_scales_down_in_a_fixed_box_and_explicit_fill_still_works() {
        painterImage(40f, 20f, Color.RED) { canvas ->
            Paint().use { paint ->
                paint.color = Color.BLUE
                canvas.drawRect(Rect.makeXYWH(20f, 0f, 20f, 20f), paint)
            }
        }.use { source ->
            val uri = source.encodeToData(EncodedImageFormat.PNG)!!.use {
                "data:image/png;base64," + Base64.getEncoder().encodeToString(it.bytes)
            }
            for (fit in listOf("", "fit=\"SCALE_DOWN\"", "fit=\"FILL\"")) {
                for (size in listOf(10, 40)) {
                    val widget = assertIs<SizedBox>(ParserTest.parse("""
                        <Snapshot><SizedBox width="$size" height="$size">
                            <Image dataUri="$uri" scale="2" $fit/>
                        </SizedBox></Snapshot>
                    """).createWidget())
                    val image = assertIs<RawImage>(widget.child)
                    try {
                        val pixels = snapshotPixels { attach(widget) }
                        assertEquals(size, pixels.info.width)
                        assertEquals(size, pixels.info.height)
                        if (fit == "fit=\"FILL\"") {
                            expectColorAt(pixels, 1, 1, Color.RED)
                            expectColorAt(pixels, size - 2, 1, Color.BLUE)
                        } else {
                            expectColorAt(pixels, 1, 1, Color.WHITE)
                            if (size == 40) {
                                expectColorAt(pixels, 12, 18, Color.RED)
                                expectColorAt(pixels, 27, 18, Color.BLUE)
                            } else {
                                expectColorAt(pixels, 1, 4, Color.RED)
                                expectColorAt(pixels, 8, 4, Color.BLUE)
                            }
                        }
                    } finally {
                        image.image.close()
                    }
                }
            }
        }
    }

    @Test
    fun data_uri_scale_controls_logical_size_without_cropping_source() {
        painterImage(40f, 20f, Color.RED) { canvas ->
            Paint().use { paint ->
                paint.color = Color.BLUE
                canvas.drawRect(Rect.makeXYWH(20f, 0f, 20f, 20f), paint)
            }
        }.use { source ->
            val uri = source.encodeToData(EncodedImageFormat.PNG)!!.use {
                "data:image/png;base64," + Base64.getEncoder().encodeToString(it.bytes)
            }
            for (scale in listOf(0.5f, 2f)) {
                val widget = assertIs<RawImage>(ParserTest.parse("""
                    <Snapshot><Image dataUri="$uri" scale="$scale" fit="FILL"/></Snapshot>
                """).createWidget())
                try {
                    val pixels = snapshotPixels { attach(widget) }
                    assertEquals((40f / scale).toInt(), pixels.info.width)
                    assertEquals((20f / scale).toInt(), pixels.info.height)
                    expectColorAt(pixels, 1, pixels.info.height / 2, Color.RED)
                    expectColorAt(pixels, pixels.info.width - 2, pixels.info.height / 2, Color.BLUE)
                } finally {
                    widget.image.close()
                }
            }
        }
    }
}
