package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.widget.RawImage
import org.junit.jupiter.api.Tag
import kotlin.test.Test
import kotlin.test.assertTrue

@Tag("network")
class ImageParserTest {
    @Test
    fun build_widget_test() {
        val text = """
            <Snapshot>
                <Image url="https://samples-files.com/samples/images/jpg/480-360-sample.jpg"/>
            </Snapshot>
        """.trimIndent()
        val snapshotElement = ParserTest.parse(text)
        val widget = snapshotElement.createWidget()
        assertTrue(widget is RawImage, "widget is RawImage")
        val rawImage: RawImage = widget as RawImage
        assertTrue(!rawImage.image.isEmpty, "!rawImage.image.isEmpty")
        assertTrue(rawImage.image.imageInfo.width == 480, "rawImage.image.imageInfo.width == 480")
        assertTrue(rawImage.image.imageInfo.height == 360, "rawImage.image.imageInfo.height == 360")
    }
}