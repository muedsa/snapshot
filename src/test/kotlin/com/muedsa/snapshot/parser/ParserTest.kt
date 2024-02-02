package com.muedsa.snapshot.parser

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.getTestPngFile
import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.SimpleText
import org.junit.jupiter.api.assertThrows
import java.io.StringReader
import kotlin.test.Test

class ParserTest {

    @Test
    fun parse_test() {
        val text = """
            <Snapshot background="#FFFFFFFF" format="png" debug>
                <Container color="#FF00FF00" width="400" height="300" alignment="CENTER" padding="10" margin="(1,2,4,8)">
                    <Container color="#FFFF0000" width="100" height="50"/>
                </Container>
            </Snapshot>
        """.trimIndent()
        println(text)
        val snapshotElement = parse(text)
        println(snapshotElement.toTreeString(0))
        val widget = snapshotElement.createWidget()
        assert(widget is Container)
        val container: Container = widget as Container
        assert(container.color == 0xFF00FF00.toInt())
        assert(container.width == 400f)
        assert(container.height == 300f)
        assert(container.alignment == BoxAlignment.CENTER)
        assert(container.padding == EdgeInsets.all(10f))
        assert(container.margin == EdgeInsets(1f, 2f, 4f, 8f))
        assert(container.child is Container)
        val childContainer: Container = container.child as Container
        assert(childContainer.color == 0xFFFF0000.toInt())
        assert(childContainer.width == 100f)
        assert(childContainer.height == 50f)
        getTestPngFile("parser/container").writeBytes(snapshotElement.snapshot())
    }

    @Test
    fun duplicate_snapshot_element_test() {
        assertThrows<ParseException> {
            val text = """
                <Snapshot background="#FFFFFFFF" format="png" debug>
                    <Snapshot/>
                </Snapshot>
                <Snapshot background="#FFFFFFFF" format="png" debug>
                    <Container color="#FF00FF00" width="400" height="300" alignment="CENTER" padding="10" margin="(1,2,4,8)">
                        <Container color="#FFFF0000" width="100" height="50"/>
                    </Container>
                </Snapshot>
            """.trimIndent()
            println(text)
            parse(text)
        }
    }

    @Test
    fun duplicate_root_element_test() {
        assertThrows<ParseException> {
            val text = """
                <Snapshot background="#FFFFFFFF" format="png" debug>
                    <Container color="#FF00FF00" width="400" height="300" alignment="CENTER" padding="10" margin="(1,2,4,8)">
                        <Container color="#FFFF0000" width="100" height="50"/>
                    </Container>
                </Snapshot>
                <Container color="#FF00FF00" width="400" height="300" alignment="CENTER" padding="10" margin="(1,2,4,8)">
                        <Container color="#FFFF0000" width="100" height="50"/>
                    </Container>
            """.trimIndent()
            println(text)
            parse(text)
        }
    }

    @Test
    fun snapshot_content_empty_test() {
        assertThrows<ParseException> {
            val text = """
                <Snapshot background="#FFFFFFFF" format="png" debug></Snapshot>>
            """.trimIndent()
            println(text)
            parse(text)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun char_token_test() {
        val text = """
            <Snapshot>
                <Container width="400" height="300">
                    <Text>char_to<![CDATA[ken_test <a></a> 233 哈哈]]>✅🤣哈</Text>
                </Container>
            </Snapshot>
        """.trimIndent()
        println(text)
        val snapshotElement = parse(text)
        println(snapshotElement.toTreeString(0))
        val widget = snapshotElement.createWidget()
        assert(widget is Container)
        val container: Container = widget as Container
        assert(container.width == 400f)
        assert(container.height == 300f)
        val textWidget: SimpleText = container.child as SimpleText
        assert(textWidget.text == "char_token_test <a></a> 233 哈哈✅🤣哈")
        getTestPngFile("parser/text").writeBytes(snapshotElement.snapshot())
    }

    companion object {
        fun parse(text: String): SnapshotElement = Parser().parse(StringReader(text))
    }
}