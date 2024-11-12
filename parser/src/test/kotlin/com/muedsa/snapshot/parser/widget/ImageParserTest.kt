package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.widget.RawImage
import kotlin.test.Test

class ImageParserTest {
    @Test
    fun build_widget_test() {
        val text = """
            <Snapshot>
                <Image url="https://samples-files.com/samples/images/jpg/480-360-sample.jpg"/>
            </Snapshot>
        """.trimIndent()
        println(text)
        val snapshotElement = ParserTest.parse(text)
        println(snapshotElement.toTreeString(0))
        val widget = snapshotElement.createWidget()
        assert(widget is RawImage)
        val rawImage: RawImage = widget as RawImage
        assert(!rawImage.image.isEmpty)
        assert(rawImage.image.imageInfo.width == 480)
        assert(rawImage.image.imageInfo.height == 360)
    }
}