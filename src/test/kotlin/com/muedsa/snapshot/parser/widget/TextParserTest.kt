package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.paint.SimpleTextPainterTest
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.parser.token.RawAttr
import com.muedsa.snapshot.widget.SimpleText
import org.jetbrains.skia.FontStyle
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
                >${SimpleTextPainterTest.SAMPLE_TEXT}</Text>
            </Snapshot>
        """.trimIndent()
        println(text)
        val snapshotElement = ParserTest.parse(text)
        println(snapshotElement.toTreeString(0))
        val widget = snapshotElement.createWidget()
        assert(widget is SimpleText)
        val simpleText: SimpleText = widget as SimpleText
        assert(simpleText.text == SimpleTextPainterTest.SAMPLE_TEXT)
        assert(simpleText.color == 0xFF_FF_00_00.toInt())
        assert(simpleText.fontSize == 12f)
        assertContentEquals(simpleText.fontFamilyName, arrayOf("Noto Sans SC", "WenQuanYi Micro Hei Mono"))
        assert(simpleText.fontStyle.weight == FontStyle.BOLD.weight)
        assert(simpleText.fontStyle.width == FontStyle.BOLD.width)
        assert(simpleText.fontStyle.slant == FontStyle.BOLD.slant)
    }
}