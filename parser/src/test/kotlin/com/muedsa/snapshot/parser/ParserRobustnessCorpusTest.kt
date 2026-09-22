package com.muedsa.snapshot.parser

import com.muedsa.snapshot.parser.token.Token
import com.muedsa.snapshot.parser.token.Tokenizer
import com.muedsa.snapshot.widget.text.RichText
import java.io.Reader
import java.io.StringReader
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.min
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class ParserRobustnessCorpusTest {

    @Test
    fun tokenizer_terminates_for_deterministic_malformed_corpus() {
        val currentInput = AtomicReference("")
        val inputs = robustnessPayloads().flatMap(::wrapForTokenizer).distinct()

        assertCompletesWithin("tokenizer", currentInput) {
            inputs.forEach { input ->
                currentInput.set(input)
                try {
                    val tokenizer = Tokenizer(StringReader(input))
                    var tokenCount = 0
                    do {
                        val token = tokenizer.read()
                        tokenCount++
                        check(tokenCount <= input.length * 2 + MAX_TOKEN_OVERHEAD) {
                            "tokenizer emitted too many tokens without reaching EOF"
                        }
                    } while (token !is Token.EOF)
                } catch (_: ParseException) {
                    // 畸形输入可以失败，但必须以统一的解析异常结束，不能挂死或泄漏内部异常。
                }
            }
        }
    }

    @Test
    fun parser_terminates_for_deterministic_malformed_corpus() {
        val currentInput = AtomicReference("")
        val inputs = robustnessPayloads().flatMap(::wrapForParser).distinct()

        assertCompletesWithin("parser", currentInput) {
            inputs.forEach { input ->
                currentInput.set(input)
                try {
                    Parser().parse(StringReader(input))
                } catch (_: ParseException) {
                    // 语法或结构不合法是预期结果；此测试只要求可控失败并保证终止。
                }
            }
        }
    }

    @Test
    fun parser_result_is_stable_across_fragmented_reader_reads() {
        val document = """
            <Snapshot background="#FFFFFFFF">
                <Text fontSize="12">prefix & <![CDATA[<tag attr="&">]]><Raw>  保留空格 &amp;  </Raw>suffix</Text>
            </Snapshot>
        """.trimIndent()
        val expected = Parser().parse(StringReader(document))
        val expectedTree = expected.toTreeString(0)
        val expectedText = expected.plainText()

        listOf(1, 2, 3, 7, 31, 1_023, 1_024, 1_025).forEach { chunkSize ->
            val actual = Parser().parse(ChunkedStringReader(document, chunkSize))

            assertEquals(expectedTree, actual.toTreeString(0), "读取分片大小为 $chunkSize 时元素树发生变化")
            assertEquals(expectedText, actual.plainText(), "读取分片大小为 $chunkSize 时文本内容发生变化")
        }
    }

    @Test
    fun quoted_attribute_and_cdata_survive_internal_buffer_boundaries() {
        val attributePayload = buildString(CharacterReader.MAX_BUFFER_LEN + 2_048) {
            repeat(CharacterReader.MAX_BUFFER_LEN + 2_048) { append('a') }
            listOf(
                1_023,
                1_024,
                CharacterReader.READ_AHEAD_LIMIT - 1,
                CharacterReader.READ_AHEAD_LIMIT,
                CharacterReader.MAX_BUFFER_LEN - 1,
                CharacterReader.MAX_BUFFER_LEN,
            ).forEach { setCharAt(it, '&') }
        }
        val attributeDocument = "<Snapshot marker=\"$attributePayload\"><Text>ok</Text></Snapshot>"
        val attributeSnapshot = parseWithinTimeout(attributeDocument)

        assertEquals(attributePayload, assertNotNull(attributeSnapshot.attrs["marker"]).value)

        listOf(
            CharacterReader.READ_AHEAD_LIMIT - 1,
            CharacterReader.MAX_BUFFER_LEN - 1,
        ).forEach { terminatorPosition ->
            val prefix = "<Snapshot><Text><![CDATA["
            val cdata = "x".repeat(terminatorPosition - prefix.length)
            val document = "$prefix$cdata]]>tail</Text></Snapshot>"

            assertEquals("${cdata}tail", parseWithinTimeout(document).plainText())
        }
    }

    private fun SnapshotElement.plainText(): String {
        val richText = assertIs<RichText>(createWidget())
        return StringBuffer().also { richText.text.computeToPlainText(it, false) }.toString()
    }

    private fun parseWithinTimeout(text: String): SnapshotElement {
        val result = AtomicReference<SnapshotElement?>()
        val currentInput = AtomicReference(text)
        assertCompletesWithin("parser", currentInput) {
            result.set(Parser().parse(StringReader(text)))
        }
        return assertNotNull(result.get())
    }

    private fun assertCompletesWithin(
        subject: String,
        currentInput: AtomicReference<String>,
        block: () -> Unit,
    ) {
        val failure = AtomicReference<Throwable?>()
        val worker = Thread {
            try {
                block()
            } catch (t: Throwable) {
                failure.set(t)
            }
        }
        worker.name = "$subject-robustness-corpus"
        worker.isDaemon = true
        worker.start()
        worker.join(TIMEOUT_MILLIS)

        assertFalse(
            worker.isAlive,
            "$subject did not terminate for input: ${currentInput.get().toDiagnosticString()}",
        )
        failure.get()?.let {
            throw AssertionError(
                "$subject failed for input: ${currentInput.get().toDiagnosticString()}",
                it,
            )
        }
    }

    private fun robustnessPayloads(): List<String> = buildList {
        addAll(
            listOf(
                "",
                "&",
                "&&&&",
                "&amp;",
                "<",
                ">",
                "</",
                "<!",
                "<![CDATA[",
                "<![CDATA[]]",
                "<![CDATA[&<>]]>",
                "<Snapshot",
                "<Snapshot attr=",
                "<Snapshot attr=\"",
                "<Snapshot attr='",
                "<Snapshot><Text>",
                "<Snapshot><Text>&</Text>",
                "<Snapshot><Text><![CDATA[&<>",
                "\u0000",
                "中文 & 🙂",
                "\r\n\t / = ` ' \" < > &",
            )
        )

        val random = Random(RANDOM_SEED)
        repeat(RANDOM_PAYLOAD_COUNT) {
            add(
                buildString {
                    repeat(random.nextInt(RANDOM_PAYLOAD_MAX_LENGTH + 1)) {
                        append(RANDOM_ALPHABET[random.nextInt(RANDOM_ALPHABET.size)])
                    }
                }
            )
        }
    }.distinct()

    private fun wrapForTokenizer(payload: String): List<String> = listOf(
        payload,
        "<Snapshot>$payload</Snapshot>",
        "<Snapshot><Text>$payload</Text></Snapshot>",
        "<Snapshot probe=\"$payload\"><Text>ok</Text></Snapshot>",
        "<Snapshot probe='$payload'><Text>ok</Text></Snapshot>",
        "<Snapshot probe=$payload><Text>ok</Text></Snapshot>",
    )

    private fun wrapForParser(payload: String): List<String> = wrapForTokenizer(payload) + listOf(
        "<Snapshot><Container>$payload</Container></Snapshot>",
        "<Snapshot><Text><Raw>$payload</Raw></Text></Snapshot>",
    )

    private fun String.toDiagnosticString(): String =
        replace("\u0000", "\\0")
            .replace("\r", "\\r")
            .replace("\n", "\\n")
            .replace("\t", "\\t")
            .let { if (it.length <= MAX_DIAGNOSTIC_LENGTH) it else it.take(MAX_DIAGNOSTIC_LENGTH) + "…" }

    private class ChunkedStringReader(
        source: String,
        private val chunkSize: Int,
    ) : Reader() {
        private val source = source.toCharArray()
        private var position = 0
        private var markPosition = 0

        init {
            require(chunkSize > 0)
        }

        override fun read(buffer: CharArray, offset: Int, length: Int): Int {
            if (position >= source.size) return -1
            val count = min(min(length, chunkSize), source.size - position)
            source.copyInto(buffer, offset, position, position + count)
            position += count
            return count
        }

        override fun skip(count: Long): Long {
            val skipped = min(count, (source.size - position).toLong())
            position += skipped.toInt()
            return skipped
        }

        override fun mark(readAheadLimit: Int) {
            markPosition = position
        }

        override fun reset() {
            position = markPosition
        }

        override fun markSupported(): Boolean = true

        override fun close() = Unit
    }

    companion object {
        private const val TIMEOUT_MILLIS = 10_000L
        private const val MAX_TOKEN_OVERHEAD = 16
        private const val RANDOM_SEED = 0x5A17
        private const val RANDOM_PAYLOAD_COUNT = 256
        private const val RANDOM_PAYLOAD_MAX_LENGTH = 64
        private const val MAX_DIAGNOSTIC_LENGTH = 160
        private val RANDOM_ALPHABET = charArrayOf(
            '<', '>', '/', '=', '"', '\'', '`', '&', '!', '[', ']', '-', '_',
            ' ', '\t', '\r', '\n', '\u0000', 'a', 'Z', '0', '中',
        )
    }
}
