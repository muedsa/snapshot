package com.muedsa.snapshot

import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.paint.text.TextStyle
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.Parser
import com.muedsa.snapshot.parser.SnapshotElement
import com.muedsa.snapshot.parser.widget.TextParser
import com.muedsa.snapshot.parser.widget.WidgetParserManager
import com.muedsa.snapshot.widget.Widget
import com.muedsa.snapshot.widget.text.RichText
import org.junit.jupiter.api.Tag
import java.io.StringReader
import kotlin.test.Test

/** 使用类 DOM 文本离线生成 README 展示卡片。 */
@Tag("sample")
class ParserShowcaseSample {

    internal fun parseDomCard(): SnapshotElement =
        Parser(SHOWCASE_PARSER_MANAGER).parse(StringReader(DOM_CARD))

    fun renderDomCard(): ByteArray = parseDomCard().snapshot()

    @Test
    fun write_dom_card_showcase() {
        rootDirection.resolve("showcase_parser_card.png").toFile().writeBytes(renderDomCard())
    }

    companion object {
        private val SHOWCASE_PARSER_MANAGER = WidgetParserManager().also { manager ->
            WidgetParserManager.DEFAULT_MANAGER.tags.values.forEach(manager::register)
            manager.register(BundledFontTextParser())
        }

        private val DOM_CARD = """
            <Snapshot background="#FF070B16" type="png">
                <Container width="1000" height="560" color="#FF070B16">
                    <Stack alignment="TOP_LEFT">
                        <Container width="1000" height="560" color="#FF070B16"/>
                        <Positioned left="610" top="-170" width="520" height="520">
                            <Container width="520" height="520" color="#FF40207A" borderRadius="260"/>
                        </Positioned>
                        <Positioned left="720" top="-80" width="320" height="320">
                            <Container width="320" height="320" color="#FF06B6D4" borderRadius="160"/>
                        </Positioned>
                        <Positioned left="56" top="48" right="56" height="48">
                            <Row mainAxisAlignment="SPACE_BETWEEN" crossAxisAlignment="CENTER">
                                <Text color="#FFF8FAFC" fontSize="18" fontStyle="BOLD">SNAPSHOT / PARSER</Text>
                                <Container color="#DD07101C" border="1 SOLID #FF22C55E" borderRadius="18" padding="(12,7,12,7)">
                                    <Text color="#FF86EFAC" fontSize="11" fontStyle="BOLD">OFFLINE READY</Text>
                                </Container>
                            </Row>
                        </Positioned>
                        <Positioned left="56" top="142" width="570" height="190">
                            <Column crossAxisAlignment="START">
                                <Text color="#FFA78BFA" fontSize="13" fontStyle="BOLD">DOM-LIKE INPUT  →  NATIVE PIXELS</Text>
                                <Text color="#FFF8FAFC" fontSize="48" fontStyle="BOLD">Describe once.</Text>
                                <Text color="#FFF8FAFC" fontSize="48" fontStyle="BOLD">Render anywhere.</Text>
                                <Text color="#FF94A3B8" fontSize="16">将结构化文本解析为同一棵 Widget 树，再输出 PNG、JPEG 或 WEBP。</Text>
                            </Column>
                        </Positioned>
                        <Positioned left="56" bottom="46" width="888" height="130">
                            <Row mainAxisAlignment="SPACE_BETWEEN" crossAxisAlignment="CENTER">
                                <Container width="272" height="116" color="#FF151C2E" border="1 SOLID #FF334155" borderRadius="24" padding="20">
                                    <Column crossAxisAlignment="START">
                                        <Text color="#FF22D3EE" fontSize="11" fontStyle="BOLD">01 / PARSE</Text>
                                        <Text color="#FFF8FAFC" fontSize="21" fontStyle="BOLD">Readable source</Text>
                                        <Text color="#FF94A3B8" fontSize="12">标签、属性、CDATA</Text>
                                    </Column>
                                </Container>
                                <Container width="272" height="116" color="#FF151C2E" border="1 SOLID #FF334155" borderRadius="24" padding="20">
                                    <Column crossAxisAlignment="START">
                                        <Text color="#FFA78BFA" fontSize="11" fontStyle="BOLD">02 / LAYOUT</Text>
                                        <Text color="#FFF8FAFC" fontSize="21" fontStyle="BOLD">Widget semantics</Text>
                                        <Text color="#FF94A3B8" fontSize="12">Row、Column、Stack</Text>
                                    </Column>
                                </Container>
                                <Container width="272" height="116" color="#FF151C2E" border="1 SOLID #FF334155" borderRadius="24" padding="20">
                                    <Column crossAxisAlignment="START">
                                        <Text color="#FF22C55E" fontSize="11" fontStyle="BOLD">03 / EXPORT</Text>
                                        <Text color="#FFF8FAFC" fontSize="21" fontStyle="BOLD">Image bytes</Text>
                                        <Text color="#FF94A3B8" fontSize="12">One call, ready to ship</Text>
                                    </Column>
                                </Container>
                            </Row>
                        </Positioned>
                    </Stack>
                </Container>
            </Snapshot>
        """.trimIndent()
    }
}

private class BundledFontTextParser : TextParser() {
    override fun buildWidget(element: Element): Widget {
        val parsed = super.buildWidget(element) as RichText
        return RichText(
            text = TextSpan(
                style = TextStyle(fontFamilies = listOf(testFontFamily)),
                initChildren = listOf(parsed.text),
            ),
        )
    }
}
