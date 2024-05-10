package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.SnapshotPNG
import com.muedsa.snapshot.getTestPngFile
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.parser.token.RawAttr
import com.muedsa.snapshot.widget.*
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Image
import kotlin.test.Test
import kotlin.test.assertContentEquals

class TextParserTest {

    @Test
    fun parse_text_attr_test() {
        CommonAttrDefine.TEXT.parseValue(RawAttr(CommonAttrDefine.TEXT.name, "123 123132✅1ada asda 🤣"))
    }

    @OptIn(ExperimentalStdlibApi::class)
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
        println(text)
        val snapshotElement = ParserTest.parse(text)
        println(snapshotElement.toTreeString(0))
        val widget = snapshotElement.createWidget()
        assert(widget is SimpleText)
        val simpleText: SimpleText = widget as SimpleText
        assert(simpleText.text == "Hello Word! 你好，世界！")
        assert(simpleText.color == 0xFF_FF_00_00.toInt())
        assert(simpleText.fontSize == 12f)
        assertContentEquals(simpleText.fontFamilyName, arrayOf("Noto Sans SC", "WenQuanYi Micro Hei Mono"))
        assert(simpleText.fontStyle.weight == FontStyle.BOLD.weight)
        assert(simpleText.fontStyle.width == FontStyle.BOLD.width)
        assert(simpleText.fontStyle.slant == FontStyle.BOLD.slant)
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

    @OptIn(ExperimentalStdlibApi::class)
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
                            SimpleText(
                                color = 0xFF_FF_FF_FF.toInt(),
                                fontSize = 14f,
                                fontFamilyName = arrayOf("Noto Sans SC"),
                                text = "鉴于对人类家庭所有成员的固有尊严及其平等的和不移的权利的承认,乃是世界自由、正义与和平的基础",
                            )
                            SimpleText(
                                color = 0xFF_FF_FF_FF.toInt(),
                                fontSize = 14f,
                                fontFamilyName = arrayOf("Noto Sans SC"),
                                text = "This generated from code",
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
}