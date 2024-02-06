package com.muedsa.snapshot

import com.muedsa.snapshot.parser.Parser
import com.muedsa.snapshot.widget.*
import org.jetbrains.skia.Color
import java.io.File
import java.io.StringReader
import kotlin.test.Test

class Sample {

    @Test
    fun sample_container() {
        File("sample_container.png").writeBytes(
            SnapshotPNG {
                Container(
                    width = 200f,
                    height = 200f,
                    color = Color.RED
                )
            }
        )
    }

    @Test
    fun sample_layout() {
        File("sample_layout.png").writeBytes(
            SnapshotPNG {
                Column {
                    Row {
                        Container(
                            width = 200f,
                            height = 200f,
                            color = Color.RED
                        )
                        Container(
                            width = 200f,
                            height = 200f,
                            color = Color.GREEN
                        )
                    }
                    Row {
                        Container(
                            width = 200f,
                            height = 200f,
                            color = Color.BLUE
                        )
                        Container(
                            width = 200f,
                            height = 200f,
                            color = Color.YELLOW
                        )
                    }
                }
            }
        )
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun sample_image_and_text() {
        File("sample_image_and_text.png").writeBytes(
            SnapshotPNG {
                Stack {
                    CachedNetworkImage("https://picsum.photos/id/201/500/")
                    SimpleText("Hello World!", color = Color.RED, fontSize = 40f)
                }
            }
        )
    }

    @Test
    fun sample_parse_dom_like() {
        val text = """
            <Snapshot background="#FFFFFFFF" type="png">
                <Column>
                    <Row>
                        <Container color="#FF0000" width="200" height="200"/>
                        <Container color="#FFFFFF" width="200" height="200">
                            <Text color="#0000FF" fontSize="20">哈哈 233<![CDATA[ken_test <a></a> 233 哈哈]]>哈🤣🤣🤣</Text>
                        </Container>
                    </Row>
                    <Row>
                        <Image width="200" height="200" url="https://picsum.photos/id/201/200"/>
                        <Container color="#FFFF00" width="200" height="200"/>
                    </Row>
                </Column>
            </Snapshot>
        """.trimIndent()
        File("sample_parse_dom_like.png").writeBytes(Parser().parse(StringReader(text)).snapshot())
    }
}