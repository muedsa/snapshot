package com.muedsa.snapshot.widget

import com.muedsa.snapshot.drawWidget
import com.muedsa.snapshot.rendering.flex.CrossAxisAlignment
import org.jetbrains.skia.Color
import org.jetbrains.skia.paragraph.BaselineMode
import kotlin.test.Test

class RowParserTest {

    @Test
    fun crossAxisAlignment_test() {
        println("\n\n\nRowTest.crossAxisAlignment_test()")
        val crossAxisAlignmentArr = CrossAxisAlignment.entries.toTypedArray()
        crossAxisAlignmentArr.forEachIndexed { index, crossAxisAlignment ->
            val name = "widget/row/crossAxis$index"
            val description = "Row($crossAxisAlignment)"
            println("\n\ndraw: $name \n$description")
            drawWidget(imagePathWithoutSuffix = name, debugInfo = description) {
                if (crossAxisAlignment == CrossAxisAlignment.STRETCH) {
                    LimitedBox(
                        maxWidth = 1000f,
                        maxHeight = 1000f,
                    ) {
                        Row(
                            crossAxisAlignment = crossAxisAlignment
                        ) {
                            Container(width = 100f, height = 100f, color = Color.RED)
                            Container(width = 300f, height = 300f, color = Color.GREEN)
                            Container(width = 200f, height = 200f, color = Color.BLUE)
                        }
                    }
                } else {
                    Row(
                        crossAxisAlignment = crossAxisAlignment,
                        textBaseline = if (crossAxisAlignment == CrossAxisAlignment.BASELINE) BaselineMode.ALPHABETIC else null
                    ) {
                        Container(width = 100f, height = 100f, color = Color.RED)
                        Container(width = 300f, height = 300f, color = Color.GREEN)
                        Container(width = 200f, height = 200f, color = Color.BLUE)
                    }
                }
            }
        }
    }

    @Test
    @OptIn(ExperimentalStdlibApi::class)
    fun baseline_test() {
        println("\n\n\nRowTest.baseline_test()")
        drawWidget(imagePathWithoutSuffix = "widget/row/baseline", debugInfo = "Row(${CrossAxisAlignment.BASELINE})") {
            Row(
                crossAxisAlignment = CrossAxisAlignment.BASELINE,
                textBaseline = BaselineMode.ALPHABETIC
            ) {
                Row(
                    crossAxisAlignment = CrossAxisAlignment.BASELINE,
                    textBaseline = BaselineMode.ALPHABETIC
                ) {
                    SimpleText("test", fontSize = 30f, color = Color.RED)
                    SimpleText("foo", fontSize = 20f, color = Color.GREEN)
                    SimpleText("bar", fontSize = 40f, color = Color.BLUE)
                }
                Row(
                    crossAxisAlignment = CrossAxisAlignment.BASELINE,
                    textBaseline = BaselineMode.ALPHABETIC
                ) {
                    SimpleText("test", fontSize = 100f, color = Color.RED)
                    SimpleText("foo", fontSize = 300f, color = Color.GREEN)
                    SimpleText("bar", fontSize = 200f, color = Color.BLUE)
                }
            }
        }
    }
}