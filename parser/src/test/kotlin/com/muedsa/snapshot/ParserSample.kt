package com.muedsa.snapshot

import com.muedsa.snapshot.parser.Parser
import java.io.StringReader
import kotlin.test.Test
import org.junit.jupiter.api.Tag

@Tag("sample")
class ParserSample {

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
                        <Image width="200" height="200" url="https://samples-files.com/samples/images/jpg/480-360-sample.jpg"/>
                        <Container color="#FFFF00" width="200" height="200"/>
                    </Row>
                </Column>
            </Snapshot>
        """.trimIndent()
        rootDirection.resolve("sample_parse_dom_like.png").toFile()
            .writeBytes(Parser().parse(StringReader(text)).snapshot())
    }
}