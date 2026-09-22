package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.snapshotPixels
import com.muedsa.snapshot.testFontFamily
import com.muedsa.snapshot.widget.text.RichText
import com.muedsa.snapshot.widget.text.WidgetSpan
import org.jetbrains.skia.Color
import org.jetbrains.skia.FontFeature
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class FontFeatureParserTest {

    @Test
    fun parses_font_feature_values_and_ranges() {
        val richText = assertIs<RichText>(
            ParserTest.parse(
                """
                <Snapshot>
                    <Text fontFamily="$testFontFamily"
                          fontSize="28"
                          fontFeatures="+liga -kern tnum=2 smcp[2:8]">OpenType 1234</Text>
                </Snapshot>
                """.trimIndent()
            ).createWidget()
        )

        val features = requireNotNull(assertIs<TextSpan>(richText.text).style?.fontFeatures)
        assertEquals(4, features.size)
        assertFeature(features[0], tag = "liga", value = 1)
        assertFeature(features[1], tag = "kern", value = 0)
        assertFeature(features[2], tag = "tnum", value = 2)
        assertFeature(features[3], tag = "smcp", value = 1, start = 2u, end = 8u)

        val pixels = snapshotPixels(background = Color.TRANSPARENT) { attach(richText) }
        assertTrue(pixels.computeInkPixels() > 100, "启用 OpenType 特性后文本应正常渲染")
    }

    @Test
    fun supports_none_to_override_inherited_font_features() {
        val richText = assertIs<RichText>(
            ParserTest.parse(
                "<Snapshot><Text fontFeatures=\"liga\"><Text fontFeatures=\"NONE\">nested</Text></Text></Snapshot>"
            ).createWidget()
        )

        val nested = assertIs<TextSpan>(assertIs<TextSpan>(richText.text).children.single())
        assertTrue(requireNotNull(nested.style?.fontFeatures).isEmpty())
    }

    @Test
    fun font_features_are_available_to_all_inline_span_tags() {
        val richText = assertIs<RichText>(
            ParserTest.parse(
                "<Snapshot><Text><Text fontFeatures=\"liga\">text</Text>" +
                        "<Raw fontFeatures=\"kern\"> raw </Raw>" +
                        "<WidgetSpan fontFeatures=\"tnum\"><SizedBox/></WidgetSpan>" +
                        "</Text></Snapshot>"
            ).createWidget()
        )

        val children = assertIs<TextSpan>(richText.text).children
        assertEquals("liga", assertIs<TextSpan>(children[0]).style?.fontFeatures?.single()?.tag)
        assertEquals("kern", assertIs<TextSpan>(children[1]).style?.fontFeatures?.single()?.tag)
        assertEquals("tnum", assertIs<WidgetSpan>(children[2]).style?.fontFeatures?.single()?.tag)
    }

    @Test
    fun rejects_invalid_font_features() {
        listOf(
            "<Snapshot><Text fontFeatures>text</Text></Snapshot>",
            "<Snapshot><Text fontFeatures=\"\">text</Text></Snapshot>",
            "<Snapshot><Text fontFeatures=\"lig\">text</Text></Snapshot>",
            "<Snapshot><Text fontFeatures=\"LIGA\">text</Text></Snapshot>",
            "<Snapshot><Text fontFeatures=\"liga,tnum\">text</Text></Snapshot>",
            "<Snapshot><Text fontFeatures=\"liga=-1\">text</Text></Snapshot>",
            "<Snapshot><Text fontFeatures=\"smcp[8:2]\">text</Text></Snapshot>",
            "<Snapshot><Text fontFeatures=\"NONE liga\">text</Text></Snapshot>",
        ).forEach { source ->
            assertFailsWith<ParseException>(source) {
                ParserTest.parse(source).createWidget()
            }
        }
    }

    private fun assertFeature(
        actual: FontFeature,
        tag: String,
        value: Int,
        start: UInt = FontFeature.GLOBAL_START,
        end: UInt = FontFeature.GLOBAL_END,
    ) {
        assertEquals(tag, actual.tag)
        assertEquals(value, actual.value)
        assertEquals(start, actual.start)
        assertEquals(end, actual.end)
    }

    private fun org.jetbrains.skia.Pixmap.computeInkPixels(): Int {
        var count = 0
        for (y in 0 until info.height) {
            for (x in 0 until info.width) {
                if (((getColor(x, y) ushr 24) and 0xFF) != 0) count++
            }
        }
        return count
    }
}
