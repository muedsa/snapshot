package com.muedsa.snapshot.parser.tag

import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.widget.RawImage
import kotlin.test.Test

class TagImageTest {
    @Test
    fun build_widget_test() {
        val text = """
            <Snapshot>
                <Image url="https://picsum.photos/id/201/500"/>
            </Snapshot>
        """.trimIndent()
        println(text)
        val snapshotElement = ParserTest.parse(text)
        println(snapshotElement.toTreeString(0))
        val widget = snapshotElement.createWidget()
        assert(widget is RawImage)
        val rawImage: RawImage = widget as RawImage
        assert(!rawImage.image.isEmpty)
        assert(rawImage.image.imageInfo.width == 500)
        assert(rawImage.image.imageInfo.height == 500)
    }
}