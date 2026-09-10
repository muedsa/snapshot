package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.SnapshotPNG
import com.muedsa.snapshot.getTestPngFile
import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.parser.token.RawAttr
import com.muedsa.snapshot.widget.*
import com.muedsa.snapshot.widget.text.RichText
import com.muedsa.snapshot.widget.text.Text
import com.muedsa.snapshot.paint.text.TextStyle
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Image
import org.junit.jupiter.api.Tag
import kotlin.test.*

class TextParserTest {

    @Test
    fun parse_text_attr_test() {
        CommonAttrDefine.TEXT.parseValue(RawAttr(CommonAttrDefine.TEXT.name, "123 123132✅1ada asda 🤣"))
    }

    @Test
    fun buildWidget_test() {
        val text = """
            <Snapshot>
                <Text color="#FFFF0000" 
                    fontSize="12" 
                    fontFamily="Noto Sans SC,WenQuanYi Micro Hei Mono" 
                    fontStyle="BOLD"
                >Hello Word! 你好，世界！</Text>
            </Snapshot>
        """.trimIndent()
        val snapshotElement = ParserTest.parse(text)
        val widget = snapshotElement.createWidget()
        assertTrue(widget is RichText, "widget is RichText")
        val richText: RichText = widget as RichText
        assertTrue(richText.text is TextSpan, "richText.text is TextSpan")
        val textSpan: TextSpan = widget.text as TextSpan
        assertTrue(textSpan.style?.color == 0xFF_FF_00_00.toInt(), "textSpan.style?.color == 0xFF_FF_00_00.toInt()")
        assertTrue(textSpan.style?.fontSize == 12f, "textSpan.style?.fontSize == 12f")
        assertNotNull(textSpan.style?.fontFamilies)
        assertTrue(textSpan.style?.fontFamilies?.size == 2, "textSpan.style?.fontFamilies?.size == 2")
        assertTrue(textSpan.style?.fontFamilies!!.contains("Noto Sans SC"), "textSpan.style?.fontFamilies!!.contains(\"Noto Sans SC\")")
        assertTrue(textSpan.style?.fontFamilies!!.contains("WenQuanYi Micro Hei Mono"), "textSpan.style?.fontFamilies!!.contains(\"WenQuanYi Micro Hei Mono\")")
        assertTrue(textSpan.style?.fontStyle?.weight == FontStyle.BOLD.weight, "textSpan.style?.fontStyle?.weight == FontStyle.BOLD.weight")
        assertTrue(textSpan.style?.fontStyle?.width == FontStyle.BOLD.width, "textSpan.style?.fontStyle?.width == FontStyle.BOLD.width")
        assertTrue(textSpan.style?.fontStyle?.slant == FontStyle.BOLD.slant, "textSpan.style?.fontStyle?.slant == FontStyle.BOLD.slant")
        assertTrue(textSpan.children.size == 1, "textSpan.children.size == 1")
        assertTrue(textSpan.children[0] is TextSpan, "textSpan.children[0] is TextSpan")
        val childTextSpan = textSpan.children[0] as TextSpan
        assertTrue(childTextSpan.text == "Hello Word! 你好，世界！", "childTextSpan.text == \"Hello Word! 你好，世界！\"")
    }

    @Test
    fun font_diff_test() {
        val text = ("""
                <Snapshot background="#FFFFFFFF">
                    <Column>
                        <Text color="#FFFF0000" 
                              fontSize="32" 
                              fontFamily="Noto Sans SC"
                        >Hello Word! 你好，世界！</Text>
                        <Text color="#FFFF0000" 
                              fontSize="32"
                        >Hello Word! 你好，世界！</Text>
                    </Column>
                </Snapshot>
            """).trimIndent()
        val snapshotElement = ParserTest.parse(text)
        getTestPngFile("parser/text_diff_font").writeBytes(snapshotElement.snapshot())
    }

    @Test
    fun text_diff_test() {
        val text = ("""
            <Snapshot background="#FFE59865" type="png">
                <Container padding="20">
                    <Column>
                        <Text color="#FFFFFFFF" 
                              fontSize="14" 
                              fontFamily="Noto Sans SC"
                        >鉴于对人类家庭所有成员的固有尊严及其平等的和不移的权利的承认,乃是世界自由、正义与和平的基础</Text>
                        <Text color="#FFFFFFFF" 
                              fontSize="14"
                              fontFamily="Noto Sans SC"
                        >This is parsed from dom</Text>
                    </Column>
                </Container>
            </Snapshot>
            """).trimIndent()
        val snapshotElement = ParserTest.parse(text)
        getTestPngFile("parser/text_diff").writeBytes(
            SnapshotPNG {
                Column {
                    Container(
                        color = 0xFF_E5_98_65.toInt(),
                        padding = EdgeInsets.all(20f),
                    ) {
                        Column {
                            Text(
                                text = "鉴于对人类家庭所有成员的固有尊严及其平等的和不移的权利的承认,乃是世界自由、正义与和平的基础",
                                style = TextStyle(
                                    color = 0xFF_FF_FF_FF.toInt(),
                                    fontSize = 14f,
                                    fontFamilies = listOf("Noto Sans SC")
                                )
                            )
                            Text(
                                text = "This generated from code",
                                style = TextStyle(
                                    color = 0xFF_FF_FF_FF.toInt(),
                                    fontSize = 14f,
                                    fontFamilies = listOf("Noto Sans SC")
                                )
                            )
                        }
                    }
                    Padding(
                        padding = EdgeInsets.only(top = 20f),
                    )
                    RawImage(image = Image.makeFromEncoded(snapshotElement.snapshot()))
                }
            }
        )
    }


    @Tag("network")
    @Test
    fun rich_text_test() {
        val text = """
            <Snapshot>
                <Text color="#FFFF0000" 
                    fontSize="12" 
                    fontFamily="Noto Sans SC,WenQuanYi Micro Hei Mono" 
                    fontStyle="BOLD">
                    Hello Word! 
                    <Emoji url="http://i0.hdslb.com/bfs/garb/69c5565c2971bcc2298d0c6347ceed9012c32300.png@65w.webp"></Emoji>
                    <Text color="#FFFF0000" 
                        fontSize="20" 
                        fontFamily="Noto Sans SC,WenQuanYi Micro Hei Mono" 
                        fontStyle="BOLD">
                        Hello Word! 
                        <Emoji url="http://i0.hdslb.com/bfs/garb/69c5565c2971bcc2298d0c6347ceed9012c32300.png@65w.webp"></Emoji>
                        <Text color="#FFFF0000" 
                            fontSize="30" 
                            fontFamily="Noto Sans SC,WenQuanYi Micro Hei Mono" 
                            fontStyle="BOLD">
                            Hello Word! 
                            <Emoji url="http://i0.hdslb.com/bfs/garb/69c5565c2971bcc2298d0c6347ceed9012c32300.png@65w.webp"></Emoji>
                            你好，世界！
                        </Text>
                        你好，世界！
                    </Text>
                    你好，世界！
                    <Raw> 
                        渲染原始文本
                    </Raw>
                </Text>
            </Snapshot>
        """.trimIndent()
        val snapshotElement = ParserTest.parse(text)
        getTestPngFile("parser/rich_text").writeBytes(snapshotElement.snapshot())
    }
}