package com.muedsa.snapshot.widget.text

import com.muedsa.snapshot.drawWidget
import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.paint.text.TextStyle
import com.muedsa.snapshot.widget.Container
import org.jetbrains.skia.Color
import kotlin.test.Test

class TextTest {

    @Test
    fun simple_text_test() {
        println("\n\n\nTextTest.simple_text_test()")
        val name = "widget/text/simple_text"
        val description = "Simple Text"
        println("\n\ndraw: $name\n$description")
        drawWidget(imagePathWithoutSuffix = name, debugInfo = description, drawDebug = false) {
            Text(
                text = "Hello, world!",
                style = TextStyle(
                    fontSize = 20f,
                    color = Color.RED
                )
            )
        }
    }

    @Test
    fun text_span_test() {
        println("\n\n\nTextTest.text_span_test()")
        val name = "widget/text/text_span"
        val description = "Rich Text"
        println("\n\ndraw: $name\n$description")
        drawWidget(imagePathWithoutSuffix = name, debugInfo = description, drawDebug = false) {
            RichText {
                TextSpan(
                    text = "Hello, one!",
                    style = TextStyle(fontSize = 20f)
                )
                TextSpan(
                    text = "Hello, two!",
                    style = TextStyle(color = Color.RED)
                )
                TextSpan("Hello, three!")
            }
        }
    }

    @Test
    fun widget_span_test() {
        println("\n\n\nTextTest.widget_span_test()")
        val name = "widget/text/widget_span"
        val description = "Rich Text"
        println("\n\ndraw: $name\n$description")
        drawWidget(imagePathWithoutSuffix = name, debugInfo = description, drawDebug = false) {
            RichText {
                TextSpan("Hello, one!", style = TextStyle(fontSize = 20f))
                WidgetSpan {
                    Container(
                        width = 20f,
                        height = 20f,
                        color = Color.BLUE
                    )
                }
                TextSpan {
                    TextSpan("Hello, two!")
                    WidgetSpan {
                        Container(
                            width = 30f,
                            height = 30f,
                            color = Color.BLUE
                        )
                    }
                    TextSpan(
                        text = "Hello, three!",
                        style = TextStyle(
                            fontSize = 20f,
                            color = Color.RED
                        )
                    )
                }
            }
        }
    }

    @Test
    fun image_emoji_test() {
        println("\n\n\nTextTest.image_emoji_test()")
        val name = "widget/text/image_emoji"
        val description = "Image Emoji"
        println("\n\ndraw: $name\n$description")
        drawWidget(imagePathWithoutSuffix = name, debugInfo = description, drawDebug = false) {
            RichText {
                TextSpan("DEFAULT")
                ImageEmojiSpan("http://i0.hdslb.com/bfs/garb/69c5565c2971bcc2298d0c6347ceed9012c32300.png@65w.webp")
                TextSpan(style = TextStyle(fontSize = 30f)) {
                    TextSpan("30")
                    ImageEmojiSpan("http://i0.hdslb.com/bfs/garb/69c5565c2971bcc2298d0c6347ceed9012c32300.png@65w.webp")
                    TextSpan(style = TextStyle(fontSize = 20f)) {
                        TextSpan("20")
                        ImageEmojiSpan("http://i0.hdslb.com/bfs/garb/69c5565c2971bcc2298d0c6347ceed9012c32300.png@65w.webp")
                    }
                    TextSpan {
                        TextSpan {
                            TextSpan("P-P")
                            ImageEmojiSpan("http://i0.hdslb.com/bfs/garb/69c5565c2971bcc2298d0c6347ceed9012c32300.png@65w.webp")
                        }
                    }
                }
            }
        }
    }

    @Test
    fun style_merge_test() {
        println("\n\n\nTextTest.style_merge_test()")
        val name = "widget/text/style_merge"
        val description = "Style Merge"
        println("\n\ndraw: $name\n$description")
        drawWidget(imagePathWithoutSuffix = name, debugInfo = description, drawDebug = false) {
            RichText {
                TextSpan(
                    style = TextStyle(
                        fontSize = 15f,
                        color = Color.WHITE
                    )
                ) {
                    TextSpan("15white")
                    TextSpan(
                        style = TextStyle(
                            fontSize = 30f
                        )
                    ) {
                        TextSpan("30white")
                        TextSpan("30red", style = TextStyle(color = Color.RED))
                    }
                    TextSpan("15white")
                }
            }
        }
    }
}