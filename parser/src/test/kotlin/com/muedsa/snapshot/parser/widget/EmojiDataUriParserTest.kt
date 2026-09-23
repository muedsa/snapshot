package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.painterImage
import com.muedsa.snapshot.snapshotPixels
import com.muedsa.snapshot.testFontFamily
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.Parser
import com.muedsa.snapshot.parser.image.DataUriImageDecoder
import com.muedsa.snapshot.widget.text.RichText
import org.jetbrains.skia.Color
import org.jetbrains.skia.EncodedImageFormat
import java.io.StringReader
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class EmojiDataUriParserTest {

    private fun bluePngDataUri(): String {
        val image = painterImage(8f, 8f, Color.BLUE) { }
        return try {
            val encoded = Base64.getEncoder().encodeToString(image.encodeToData(EncodedImageFormat.PNG)!!.bytes)
            "data:image/png;base64,$encoded"
        } finally {
            image.close()
        }
    }

    private fun parse(emojiTag: String, parser: Parser = Parser()) = parser.parse(
        StringReader("<Snapshot><Text fontSize=\"24\" fontFamily=\"$testFontFamily\">A$emojiTag B</Text></Snapshot>")
    )

    private fun countBluePixels(image: RichText): Int {
        val pixels = snapshotPixels(background = Color.TRANSPARENT) { attach(image) }
        var count = 0
        for (y in 0 until pixels.info.height) {
            for (x in 0 until pixels.info.width) {
                val color = pixels.getColor(x, y)
                val red = (color ushr 16) and 0xff
                val green = (color ushr 8) and 0xff
                val blue = color and 0xff
                if (blue > red + 60 && blue > green + 60) count++
            }
        }
        return count
    }

    @Test
    fun data_uri_emoji_renders_offline() {
        val snapshot = parse("<Emoji dataUri=\"${bluePngDataUri()}\" width=\"18\" height=\"18\"/>")
        val text = assertIs<RichText>(snapshot.createWidget())
        assertTrue(countBluePixels(text) > 100, "Data URI 行内图片应产生蓝色像素")
    }

    @Test
    fun emoji_uses_the_parser_specific_decoder() {
        val image = painterImage(8f, 8f, Color.BLUE) { }
        val dataUri = "data:image/png;base64,custom"
        val calls = mutableListOf<String>()
        val decoder = DataUriImageDecoder { value -> calls += value; image }
        try {
            val snapshot = parse("<Emoji dataUri=\"$dataUri\" width=\"18\" height=\"18\"/>", Parser(dataUriImageDecoder = decoder))
            assertTrue(calls.isEmpty(), "结构解析阶段不应提前解码行内图片")
            val text = assertIs<RichText>(snapshot.createWidget())
            assertEquals(listOf(dataUri), calls)
            assertTrue(countBluePixels(text) > 100)
            assertEquals(listOf(dataUri), calls, "渲染时不应重复解码")
        } finally {
            image.close()
        }
    }

    @Test
    fun emoji_requires_exactly_one_image_source() {
        val missing = parse("<Emoji/>")
        val missingElement = missing.children.single().children.first { it.widgetParser is EmojiParser }
        assertEquals(missingElement.pos, assertFailsWith<ParseException> { missing.createWidget() }.pos)

        val conflicting = parse("<Emoji url=\"https://example.com/emoji.png\" dataUri=\"data:image/png;base64,AAAA\"/>")
        val dataUriAttr = conflicting.children.single().children.first { it.widgetParser is EmojiParser }.attrs.getValue("dataUri")
        assertEquals(dataUriAttr.nameStartPos, assertFailsWith<ParseException> { conflicting.createWidget() }.pos)
    }

    @Test
    fun malformed_emoji_data_uri_reports_its_value_position() {
        val snapshot = parse("<Emoji dataUri=\"data:image/png;base64,!invalid!\"/>")
        val dataUriAttr = snapshot.children.single().children.first { it.widgetParser is EmojiParser }.attrs.getValue("dataUri")
        val error = assertFailsWith<ParseException> { snapshot.createWidget() }
        assertEquals(dataUriAttr.valueStartPos, error.pos)
        assertTrue(error.message!!.contains("Attr [dataUri]"))
    }
}
