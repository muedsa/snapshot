package com.muedsa.snapshot.parser

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.getTestPngFile
import com.muedsa.snapshot.widget.Container
import java.io.StringReader
import kotlin.test.Test

class ParserTest {

    @Test
    fun parse_test() {
        val snapshotElement = parse(
            """
            <Snapshot background="#FFFFFFFF" format="png" debug>
                <Container color="#FF00FF00" width="400" height="300" alignment="CENTER" padding="10" margin="(1,2,4,8)">
                    <Container color="#FFFF0000" width="100" height="50"/>
                </Container>
            </Snapshot>
        """
        )
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
    }

    @Test
    fun draw_test() {
        val snapshotElement = parse(
            """
            <Snapshot background="#FFFFFFFF" format="png">
                <Container color="#FF00FF00" width="400" height="300" alignment="CENTER" padding="10" margin="(1,2,4,8)">
                    <Container color="#FFFF0000" width="100" height="50"/>
                </Container>
            </Snapshot>
        """
        )
        println(snapshotElement.toTreeString(0))
        getTestPngFile("parser/container").writeBytes(snapshotElement.snapshot())
    }

    private fun parse(text: String): SnapshotElement = Parser().parse(StringReader(text))
}