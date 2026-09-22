package com.muedsa.snapshot.parser

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.regionStats
import com.muedsa.snapshot.snapshotPixels
import com.muedsa.snapshot.testFontFamily
import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.RawImage
import com.muedsa.snapshot.widget.text.RichText
import org.jetbrains.skia.Color
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import java.io.StringReader
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

class ParserTest {

    @Test
    fun parse_test() {
        val text = """
            <Snapshot background="#FFFFFFFF" type="png" debug>
                <Container color="#FF00FF00" width="400" height="300" alignment="CENTER" padding="10" margin="(1,2,4,8)">
                    <Container color="#FFFF0000" width="100" height="50"/>
                </Container>
            </Snapshot>
        """.trimIndent()
        val snapshotElement = parse(text)
        val widget = snapshotElement.createWidget()
        assertTrue(widget is Container, "widget is Container")
        val container: Container = widget
        assertTrue(container.color == 0xFF00FF00.toInt(), "container.color == 0xFF00FF00.toInt()")
        assertTrue(container.width == 400f, "container.width == 400f")
        assertTrue(container.height == 300f, "container.height == 300f")
        assertTrue(container.alignment == BoxAlignment.CENTER, "container.alignment == BoxAlignment.CENTER")
        assertTrue(container.padding == EdgeInsets.all(10f), "container.padding == EdgeInsets.all(10f)")
        assertTrue(container.margin == EdgeInsets(1f, 2f, 4f, 8f), "container.margin == EdgeInsets(1f, 2f, 4f, 8f)")
        assertTrue(container.child is Container, "container.child is Container")
        val childContainer: Container = container.child as Container
        assertTrue(childContainer.color == 0xFFFF0000.toInt(), "childContainer.color == 0xFFFF0000.toInt()")
        assertTrue(childContainer.width == 100f, "childContainer.width == 100f")
        assertTrue(childContainer.height == 50f, "childContainer.height == 50f")
        val encodedImage = Image.makeFromEncoded(snapshotElement.snapshot())
        assertEquals(405, encodedImage.width)
        assertEquals(310, encodedImage.height)
        val pixels = snapshotPixels(background = Color.TRANSPARENT) {
            RawImage(image = encodedImage)
        }
        expectColorAt(pixels, 0, 0, Color.WHITE)
        expectColorAt(pixels, 5, 5, Color.GREEN)
        expectColorAt(pixels, 200, 150, Color.RED)
    }

    @Test
    fun duplicate_snapshot_element_test() {
        assertFailsWith<ParseException> {
            val text = """
                <Snapshot background="#FFFFFFFF" type="png" debug>
                    <Snapshot/>
                </Snapshot>
                <Snapshot background="#FFFFFFFF" type="png" debug>
                    <Container color="#FF00FF00" width="400" height="300" alignment="CENTER" padding="10" margin="(1,2,4,8)">
                        <Container color="#FFFF0000" width="100" height="50"/>
                    </Container>
                </Snapshot>
            """.trimIndent()
            parse(text)
        }
    }

    @Test
    fun duplicate_root_element_test() {
        assertFailsWith<ParseException> {
            val text = """
                <Snapshot background="#FFFFFFFF" type="png" debug>
                    <Container color="#FF00FF00" width="400" height="300" alignment="CENTER" padding="10" margin="(1,2,4,8)">
                        <Container color="#FFFF0000" width="100" height="50"/>
                    </Container>
                </Snapshot>
                <Container color="#FF00FF00" width="400" height="300" alignment="CENTER" padding="10" margin="(1,2,4,8)">
                        <Container color="#FFFF0000" width="100" height="50"/>
                    </Container>
            """.trimIndent()
            parse(text)
        }
    }

    @Test
    fun snapshot_content_empty_test() {
        assertFailsWith<ParseException> {
            val text = """
                <Snapshot background="#FFFFFFFF" type="png" debug></Snapshot>>
            """.trimIndent()
            parse(text)
        }
    }

    @Test
    fun char_token_test() {
        val text = """
            <Snapshot>
                <Container width="400" height="300">
                    <Text fontFamily="$testFontFamily">char_to<![CDATA[ken_test <a></a> 233 哈哈]]>✅🤣哈</Text>
                </Container>
            </Snapshot>
        """.trimIndent()
        val snapshotElement = parse(text)
        val widget = snapshotElement.createWidget()
        assertTrue(widget is Container, "widget is Container")
        val container: Container = widget
        assertTrue(container.width == 400f, "container.width == 400f")
        assertTrue(container.height == 300f, "container.height == 300f")
        val richText: RichText = container.child as RichText
        val stringBuffer: StringBuffer = StringBuffer()
        richText.text.computeToPlainText(stringBuffer, false)
        assertEquals("char_token_test <a></a> 233 哈哈✅🤣哈", stringBuffer.toString())

        val encodedImage = Image.makeFromEncoded(snapshotElement.snapshot())
        assertEquals(400, encodedImage.width)
        assertEquals(300, encodedImage.height)
        val pixels = snapshotPixels(background = Color.TRANSPARENT) {
            RawImage(image = encodedImage)
        }
        val stats = pixels.regionStats(Rect.makeWH(400f, 300f))
        val inkPixels = stats.pixelCount - stats.transparentCount
        assertTrue(inkPixels > 100, "解析后的中英文文本应产生可见墨迹，实际像素数为 $inkPixels")
    }

    @Test
    fun parser_instance_can_parse_multiple_documents() {
        val parser = Parser()

        val first = parser.parse(
            StringReader("<Snapshot><Container width=\"10\" height=\"20\"/></Snapshot>")
        )
        val second = parser.parse(
            StringReader("<Snapshot><Container width=\"30\" height=\"40\"/></Snapshot>")
        )

        assertNotSame(first, second)
        assertEquals(10f, assertIs<Container>(first.createWidget()).width)
        assertEquals(20f, assertIs<Container>(first.createWidget()).height)
        assertEquals(30f, assertIs<Container>(second.createWidget()).width)
        assertEquals(40f, assertIs<Container>(second.createWidget()).height)
    }

    @Test
    fun parser_instance_recovers_after_failed_parse() {
        val parser = Parser()

        assertFailsWith<ParseException> {
            parser.parse(StringReader("<Snapshot><Unknown/></Snapshot>"))
        }

        val recovered = parser.parse(
            StringReader("<Snapshot><Container width=\"50\" height=\"60\"/></Snapshot>")
        )
        val widget = assertIs<Container>(recovered.createWidget())
        assertEquals(50f, widget.width)
        assertEquals(60f, widget.height)
    }

    @Test
    fun parser_instance_serializes_concurrent_parse_calls() {
        val parser = Parser()
        val executor = Executors.newFixedThreadPool(4)
        val start = CountDownLatch(1)

        try {
            val widths = 1..8
            val futures = widths.map { width ->
                executor.submit(Callable {
                    start.await()
                    assertIs<Container>(
                        parser.parse(
                            StringReader(
                                "<Snapshot><Container width=\"$width\" height=\"10\"/></Snapshot>"
                            )
                        ).createWidget()
                    )
                })
            }

            start.countDown()
            futures.zip(widths).forEach { (future, width) ->
                assertEquals(width.toFloat(), future.get(10, TimeUnit.SECONDS).width)
            }
        } finally {
            executor.shutdownNow()
        }
    }

    companion object {
        fun parse(text: String): SnapshotElement = Parser().parse(StringReader(text))
    }
}
