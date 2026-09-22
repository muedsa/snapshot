package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.paint.text.TextStyle
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.parser.SnapshotElement
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.parser.token.RawAttr
import com.muedsa.snapshot.snapshotPixels
import com.muedsa.snapshot.testFontFamily
import com.muedsa.snapshot.tools.NetworkImageCache
import com.muedsa.snapshot.widget.Column
import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.text.RichText
import com.muedsa.snapshot.widget.text.Text
import org.jetbrains.skia.Color
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Image
import org.jetbrains.skia.Pixmap
import org.jetbrains.skia.Surface
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TextParserTest {

    @Test
    fun parses_required_text_attribute() {
        val expected = "123 123132✅1ada asda 🤣"
        assertEquals(
            expected,
            CommonAttrDefine.TEXT.parseValue(RawAttr(CommonAttrDefine.TEXT.name, expected)),
        )
    }

    @Test
    fun parses_text_style_and_renders_bundled_font() {
        val expectedText = "Hello World! 你好，世界！"
        val widget = ParserTest.parse(
            """
            <Snapshot>
                <Text color="#FFFF0000"
                      fontSize="20"
                      fontFamily="$testFontFamily"
                      fontStyle="BOLD">$expectedText</Text>
            </Snapshot>
            """.trimIndent()
        ).createWidget()

        val richText = assertIs<RichText>(widget)
        val rootSpan = assertIs<TextSpan>(richText.text)
        val style = requireNotNull(rootSpan.style)
        assertEquals(Color.RED, style.color)
        assertEquals(20f, style.fontSize)
        assertEquals(listOf(testFontFamily), style.fontFamilies)
        assertEquals(FontStyle.BOLD, style.fontStyle)

        val plainText = StringBuffer()
        rootSpan.computeToPlainText(plainText, includePlaceholders = false)
        assertEquals(expectedText, plainText.toString())

        val pixels = snapshotPixels(background = Color.TRANSPARENT) { attach(richText) }
        val redInk = pixels.countPixels { color -> color.isReddish() }
        assertTrue(redInk > 100, "内置字体应渲染出红色中英文墨迹，实际像素数为 $redInk")
    }

    @Test
    fun parser_and_kotlin_dsl_render_the_same_text_scene() {
        val chinese = "结构化文本与 Kotlin DSL 应生成一致的像素"
        val english = "Parser rendering contract"
        val parsedWidget = ParserTest.parse(
            """
            <Snapshot>
                <Container color="#FFE59865" padding="20">
                    <Column>
                        <Text color="#FFFFFFFF" fontSize="18" fontFamily="$testFontFamily">$chinese</Text>
                        <Text color="#FFFFFFFF" fontSize="14" fontFamily="$testFontFamily">$english</Text>
                    </Column>
                </Container>
            </Snapshot>
            """.trimIndent()
        ).createWidget()

        val parsedPixels = snapshotPixels(background = Color.TRANSPARENT) { attach(parsedWidget) }
        val dslPixels = snapshotPixels(background = Color.TRANSPARENT) {
            Container(
                color = 0xFF_E5_98_65.toInt(),
                padding = EdgeInsets.all(20f),
            ) {
                Column {
                    Text(
                        text = chinese,
                        style = TextStyle(
                            color = Color.WHITE,
                            fontSize = 18f,
                            fontFamilies = listOf(testFontFamily),
                        ),
                    )
                    Text(
                        text = english,
                        style = TextStyle(
                            color = Color.WHITE,
                            fontSize = 14f,
                            fontFamilies = listOf(testFontFamily),
                        ),
                    )
                }
            }
        }

        assertPixelsEqual(dslPixels, parsedPixels)
    }

    @Test
    fun parses_nested_text_raw_and_emoji_without_network() {
        val emojiImage = solidImage(Color.BLUE)
        val cache = FixedImageCache(emojiImage)
        val previousCacheBuilder = SnapshotElement.NETWORK_IMAGE_CACHE_BUILDER
        SnapshotElement.NETWORK_IMAGE_CACHE_BUILDER = { cache }
        try {
            val widget = ParserTest.parse(
                """
                <Snapshot>
                    <Text color="#FFFF0000" fontSize="20" fontFamily="$testFontFamily">
                        第一层
                        <Emoji url="https://example.invalid/emoji.png" width="18" height="18"/>
                        <Text fontSize="24">第二层<Raw> 保留空格 </Raw></Text>
                    </Text>
                </Snapshot>
                """.trimIndent()
            ).createWidget()

            val richText = assertIs<RichText>(widget)
            assertEquals(1, richText.children.size)
            val plainText = StringBuffer()
            richText.text.computeToPlainText(plainText, includePlaceholders = false)
            assertTrue(plainText.contains("第一层"))
            assertTrue(plainText.contains("第二层 保留空格 "))
            assertEquals(listOf("https://example.invalid/emoji.png" to false), cache.requests)

            val pixels = snapshotPixels(background = Color.TRANSPARENT) { attach(richText) }
            val redInk = pixels.countPixels { color -> color.isReddish() }
            val bluePixels = pixels.countPixels { color -> color.isBlueish() }
            assertTrue(redInk > 50, "嵌套文本应产生红色墨迹，实际像素数为 $redInk")
            assertTrue(bluePixels > 100, "本地 emoji 应产生蓝色像素，实际像素数为 $bluePixels")
        } finally {
            SnapshotElement.NETWORK_IMAGE_CACHE_BUILDER = previousCacheBuilder
        }
    }

    private fun assertPixelsEqual(expected: Pixmap, actual: Pixmap) {
        assertEquals(expected.info.width, actual.info.width, "渲染宽度不一致")
        assertEquals(expected.info.height, actual.info.height, "渲染高度不一致")
        var mismatchCount = 0
        for (y in 0 until expected.info.height) {
            for (x in 0 until expected.info.width) {
                if (expected.getColor(x, y) != actual.getColor(x, y)) mismatchCount++
            }
        }
        assertEquals(0, mismatchCount, "Parser 与 Kotlin DSL 的渲染像素不一致")
    }

    private fun Pixmap.countPixels(predicate: (Int) -> Boolean): Int {
        var count = 0
        for (y in 0 until info.height) {
            for (x in 0 until info.width) {
                val color = getColor(x, y)
                if (((color ushr 24) and 0xFF) != 0 && predicate(color)) count++
            }
        }
        return count
    }

    private fun Int.isReddish(): Boolean {
        val red = (this ushr 16) and 0xFF
        val green = (this ushr 8) and 0xFF
        val blue = this and 0xFF
        return red > green + 60 && red > blue + 60
    }

    private fun Int.isBlueish(): Boolean {
        val red = (this ushr 16) and 0xFF
        val green = (this ushr 8) and 0xFF
        val blue = this and 0xFF
        return blue > red + 60 && blue > green + 60
    }

    private fun solidImage(color: Int): Image {
        val surface = Surface.makeRasterN32Premul(8, 8)
        surface.canvas.clear(color)
        return surface.makeImageSnapshot()
    }

    private class FixedImageCache(private val image: Image) : NetworkImageCache {
        override val name: String = "parser-test-fixed-image"
        val requests = mutableListOf<Pair<String, Boolean>>()

        override fun getImage(url: String, noCache: Boolean): Image {
            requests += url to noCache
            return image
        }

        override fun clearAll() = Unit

        override fun clearImage(url: String) = Unit

        override fun count(): Int = 1

        override fun size(): Int = image.width * image.height * 4
    }
}
