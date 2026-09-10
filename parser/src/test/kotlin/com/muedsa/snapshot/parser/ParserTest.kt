package com.muedsa.snapshot.parser

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.getTestPngFile
import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.text.RichText
import kotlin.test.assertFailsWith
import java.io.StringReader
import kotlin.test.Test
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
        val container: Container = widget as Container
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
        getTestPngFile("parser/container").writeBytes(snapshotElement.snapshot())
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
                    <Text>char_to<![CDATA[ken_test <a></a> 233 哈哈]]>✅🤣哈</Text>
                </Container>
            </Snapshot>
        """.trimIndent()
        val snapshotElement = parse(text)
        val widget = snapshotElement.createWidget()
        assertTrue(widget is Container, "widget is Container")
        val container: Container = widget as Container
        assertTrue(container.width == 400f, "container.width == 400f")
        assertTrue(container.height == 300f, "container.height == 300f")
        val richText: RichText = container.child as RichText
        val stringBuffer: StringBuffer = StringBuffer()
        richText.text.computeToPlainText(stringBuffer, false)
        assertTrue(stringBuffer.toString() == "char_token_test <a></a> 233 哈哈✅🤣哈", "stringBuffer.toString() == \"char_token_test <a></a> 233 哈哈✅🤣哈\"")
        getTestPngFile("parser/text").writeBytes(snapshotElement.snapshot())
    }

    companion object {
        fun parse(text: String): SnapshotElement = Parser().parse(StringReader(text))
    }
}