package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.painterImage
import com.muedsa.snapshot.snapshotPixels
import com.muedsa.snapshot.paint.BoxFit
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.Parser
import com.muedsa.snapshot.parser.image.DataUriImageDecoder
import com.muedsa.snapshot.widget.RawImage
import org.jetbrains.skia.Color
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.io.StringReader
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ImageDataUriParserTest {

    private fun redPngDataUri(): String {
        val image = painterImage(2f, 2f, Color.RED) { }
        return try {
            val encoded = Base64.getEncoder().encodeToString(image.encodeToData(EncodedImageFormat.PNG)!!.bytes)
            "data:image/png;base64,$encoded"
        } finally {
            image.close()
        }
    }

    private fun parse(imageTag: String, parser: Parser = Parser()) =
        parser.parse(StringReader("<Snapshot>$imageTag</Snapshot>"))

    @Test
    fun data_uri_image_builds_and_renders_offline() {
        val snapshot = parse("<Image dataUri=\"${redPngDataUri()}\" width=\"4\" height=\"4\" fit=\"CONTAIN\"/>")
        val widget = assertIs<RawImage>(snapshot.createWidget())
        assertEquals(4f, widget.width)
        assertEquals(4f, widget.height)
        assertEquals(BoxFit.CONTAIN, widget.fit)
        assertEquals(2, widget.image.width)
        val rendered = Image.makeFromEncoded(snapshot.snapshot())
        try {
            val pixels = snapshotPixels(background = Color.TRANSPARENT) { RawImage(image = rendered) }
            expectColorAt(pixels, 1, 1, Color.RED)
        } finally {
            rendered.close()
            widget.image.close()
        }
    }

    @Test
    fun parser_uses_injected_decoder_without_touching_network_cache() {
        val image = painterImage(2f, 2f, Color.BLUE) { }
        val values = mutableListOf<String>()
        val decoder = DataUriImageDecoder { value -> values += value; image }
        val parser = Parser(dataUriImageDecoder = decoder)
        try {
            val first = parse("<Image dataUri=\"data:image/png;base64,first\"/>", parser)
            val second = parse("<Image dataUri=\"data:image/png;base64,second\"/>", parser)
            assertTrue(values.isEmpty(), "结构解析阶段不应提前解码图片")
            assertSame(image, assertIs<RawImage>(first.createWidget()).image)
            assertSame(image, assertIs<RawImage>(second.createWidget()).image)
            assertEquals(listOf("data:image/png;base64,first", "data:image/png;base64,second"), values)
        } finally {
            image.close()
        }
    }

    @Test
    fun rejects_missing_or_conflicting_sources_before_loading() {
        val missing = parse("<Image/>")
        assertEquals(missing.children.single().pos, assertFailsWith<ParseException> { missing.createWidget() }.pos)

        val conflicting = parse(
            "<Image url=\"https://example.com/image.png\" dataUri=\"data:image/png;base64,AAAA\"/>"
        )
        assertEquals(
            conflicting.children.single().attrs.getValue("dataUri").nameStartPos,
            assertFailsWith<ParseException> { conflicting.createWidget() }.pos,
        )

        val noCache = parse("<Image dataUri=\"data:image/png;base64,AAAA\" noCache=\"false\"/>")
        assertEquals(
            noCache.children.single().attrs.getValue("noCache").nameStartPos,
            assertFailsWith<ParseException> { noCache.createWidget() }.pos,
        )
    }

    @Test
    fun invalid_data_uri_reports_attribute_value_position() {
        val snapshot = parse("<Image dataUri=\"data:image/png;base64,!invalid!\"/>")
        val rawAttr = snapshot.children.single().attrs.getValue("dataUri")
        val error = assertFailsWith<ParseException> { snapshot.createWidget() }
        assertEquals(rawAttr.valueStartPos, error.pos)
        assertTrue(error.message!!.contains("Attr [dataUri]"))
    }
}
