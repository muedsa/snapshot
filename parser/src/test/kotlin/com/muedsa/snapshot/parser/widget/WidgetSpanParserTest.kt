package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.snapshotPixels
import com.muedsa.snapshot.testFontFamily
import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.text.RichText
import com.muedsa.snapshot.widget.text.WidgetSpan
import org.jetbrains.skia.Color
import org.jetbrains.skia.Pixmap
import org.jetbrains.skia.paragraph.BaselineMode
import org.jetbrains.skia.paragraph.PlaceholderAlignment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WidgetSpanParserTest {

    @Test
    fun parses_widget_span_attributes_child_and_style() {
        val richText = assertIs<RichText>(
            ParserTest.parse(
                """
                <Snapshot>
                    <Text fontFamily="$testFontFamily" fontSize="18">
                        前
                        <WidgetSpan alignment="MIDDLE"
                                    baseline="IDEOGRAPHIC"
                                    color="#FFFF0000"
                                    fontSize="24">
                            <Container width="30" height="20" color="#FF0000FF"/>
                        </WidgetSpan>
                        后
                    </Text>
                </Snapshot>
                """.trimIndent()
            ).createWidget()
        )

        val rootSpan = assertIs<TextSpan>(richText.text)
        val widgetSpan = assertIs<WidgetSpan>(rootSpan.children.single { it is WidgetSpan })
        assertEquals(PlaceholderAlignment.MIDDLE, widgetSpan.alignment)
        assertEquals(BaselineMode.IDEOGRAPHIC, widgetSpan.baseline)
        assertEquals(Color.RED, requireNotNull(widgetSpan.style).color)
        assertEquals(24f, requireNotNull(widgetSpan.style).fontSize)

        val child = assertIs<Container>(widgetSpan.child)
        assertEquals(30f, child.width)
        assertEquals(20f, child.height)
        assertEquals(Color.BLUE, child.color)

        val pixels = snapshotPixels(background = Color.TRANSPARENT) { attach(richText) }
        val bluePixels = pixels.countPixels { color -> color.isBlueish() }
        assertTrue(bluePixels > 400, "行内 Container 应产生蓝色像素，实际像素数为 $bluePixels")
    }

    @Test
    fun uses_core_widget_span_defaults() {
        val richText = assertIs<RichText>(
            ParserTest.parse(
                """
                <Snapshot>
                    <Text><WidgetSpan><SizedBox width="12" height="8"/></WidgetSpan></Text>
                </Snapshot>
                """.trimIndent()
            ).createWidget()
        )

        val rootSpan = assertIs<TextSpan>(richText.text)
        val widgetSpan = assertIs<WidgetSpan>(rootSpan.children.single())
        assertEquals(PlaceholderAlignment.BOTTOM, widgetSpan.alignment)
        assertNull(widgetSpan.baseline)
        assertNull(widgetSpan.style)
    }

    @Test
    fun rejects_missing_or_multiple_children() {
        val missingChild = ParserTest.parse(
            "<Snapshot><Text><WidgetSpan/></Text></Snapshot>"
        )
        assertFailsWith<ParseException> { missingChild.createWidget() }

        assertFailsWith<ParseException> {
            ParserTest.parse(
                """
                <Snapshot>
                    <Text>
                        <WidgetSpan>
                            <SizedBox width="1" height="1"/>
                            <SizedBox width="2" height="2"/>
                        </WidgetSpan>
                    </Text>
                </Snapshot>
                """.trimIndent()
            )
        }
    }

    @Test
    fun rejects_invalid_attributes_and_usage_outside_text() {
        listOf(
            "<Snapshot><Text><WidgetSpan alignment=\"INVALID\"><SizedBox/></WidgetSpan></Text></Snapshot>",
            "<Snapshot><Text><WidgetSpan baseline=\"INVALID\"><SizedBox/></WidgetSpan></Text></Snapshot>",
            "<Snapshot><WidgetSpan><SizedBox/></WidgetSpan></Snapshot>",
        ).forEach { source ->
            assertFailsWith<ParseException>(source) {
                ParserTest.parse(source).createWidget()
            }
        }
    }

    private fun Pixmap.countPixels(predicate: (Int) -> Boolean): Int {
        var count = 0
        for (y in 0 until info.height) {
            for (x in 0 until info.width) {
                val color = getColor(x, y)
                if (((color ushr 24) and 0xFF) != 0 && predicate(color)) count++
            }
        }
        return count
    }

    private fun Int.isBlueish(): Boolean {
        val red = (this ushr 16) and 0xFF
        val green = (this ushr 8) and 0xFF
        val blue = this and 0xFF
        return blue > red + 60 && blue > green + 60
    }
}
