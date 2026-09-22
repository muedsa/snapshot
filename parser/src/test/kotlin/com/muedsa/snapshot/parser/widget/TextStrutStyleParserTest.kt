package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.snapshotPixels
import com.muedsa.snapshot.testFontFamily
import com.muedsa.snapshot.widget.text.RichText
import org.jetbrains.skia.Color
import org.jetbrains.skia.FontStyle
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TextStrutStyleParserTest {

    @Test
    fun parses_all_strut_style_options() {
        val richText = assertIs<RichText>(
            ParserTest.parse(
                """
                <Snapshot>
                    <Text strutEnabled="true"
                          strutFontFamily="$testFontFamily, Arial"
                          strutFontStyle="BOLD"
                          strutFontSize="20"
                          strutHeight="1.5"
                          strutLeading="0.25"
                          strutHeightForced="true"
                          strutHeightOverridden="true">line</Text>
                </Snapshot>
                """.trimIndent()
            ).createWidget()
        )

        val style = requireNotNull(richText.strutStyle)
        assertTrue(style.isEnabled)
        assertContentEquals(arrayOf(testFontFamily, "Arial"), style.fontFamilies)
        assertEquals(FontStyle.BOLD, style.fontStyle)
        assertEquals(20f, style.fontSize)
        assertEquals(1.5f, style.height)
        assertEquals(0.25f, style.leading)
        assertTrue(style.isHeightForced)
        assertTrue(style.isHeightOverridden)
    }

    @Test
    fun creates_strut_only_when_configured_and_enables_it_by_default() {
        val plainText = assertIs<RichText>(
            ParserTest.parse("<Snapshot><Text>plain</Text></Snapshot>").createWidget()
        )
        assertNull(plainText.strutStyle)

        val configuredText = assertIs<RichText>(
            ParserTest.parse(
                "<Snapshot><Text strutFontSize=\"30\">configured</Text></Snapshot>"
            ).createWidget()
        )
        val configuredStyle = requireNotNull(configuredText.strutStyle)
        assertTrue(configuredStyle.isEnabled)
        assertEquals(30f, configuredStyle.fontSize)

        val disabledText = assertIs<RichText>(
            ParserTest.parse(
                "<Snapshot><Text strutEnabled=\"false\" strutFontSize=\"30\">disabled</Text></Snapshot>"
            ).createWidget()
        )
        assertFalse(requireNotNull(disabledText.strutStyle).isEnabled)
    }

    @Test
    fun strut_style_controls_multiline_height() {
        val plainText = assertIs<RichText>(
            ParserTest.parse(
                """
                <Snapshot><Text fontFamily="$testFontFamily" fontSize="16">A
                B</Text></Snapshot>
                """.trimIndent()
            ).createWidget()
        )
        val strutText = assertIs<RichText>(
            ParserTest.parse(
                """
                <Snapshot><Text fontFamily="$testFontFamily"
                                fontSize="16"
                                strutFontFamily="$testFontFamily"
                                strutFontSize="40"
                                strutHeight="1.5"
                                strutHeightForced="true"
                                strutHeightOverridden="true">A
                B</Text></Snapshot>
                """.trimIndent()
            ).createWidget()
        )

        val plainPixels = snapshotPixels(background = Color.TRANSPARENT) { attach(plainText) }
        val strutPixels = snapshotPixels(background = Color.TRANSPARENT) { attach(strutText) }
        assertTrue(
            strutPixels.info.height > plainPixels.info.height,
            "启用大字号强制 Strut 后多行文本高度应增加，普通=${plainPixels.info.height}，Strut=${strutPixels.info.height}",
        )
    }

    @Test
    fun rejects_invalid_strut_style_options() {
        listOf(
            "<Snapshot><Text strutFontFamily=\"\">text</Text></Snapshot>",
            "<Snapshot><Text strutFontFamily=\"Arial,\">text</Text></Snapshot>",
            "<Snapshot><Text strutFontStyle=\"INVALID\">text</Text></Snapshot>",
            "<Snapshot><Text strutFontSize=\"0\">text</Text></Snapshot>",
            "<Snapshot><Text strutFontSize=\"NaN\">text</Text></Snapshot>",
            "<Snapshot><Text strutHeight=\"0\">text</Text></Snapshot>",
            "<Snapshot><Text strutLeading=\"NaN\">text</Text></Snapshot>",
        ).forEach { source ->
            assertFailsWith<ParseException>(source) {
                ParserTest.parse(source).createWidget()
            }
        }
    }
}
