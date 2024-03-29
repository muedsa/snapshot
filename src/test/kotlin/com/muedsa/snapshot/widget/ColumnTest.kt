package com.muedsa.snapshot.widget

import com.muedsa.snapshot.drawWidget
import com.muedsa.snapshot.rendering.flex.CrossAxisAlignment
import org.jetbrains.skia.Color
import org.jetbrains.skia.paragraph.BaselineMode
import kotlin.test.Test

class ColumnTest {
    @Test
    fun crossAxisAlignment_test() {
        println("\n\n\nColumnTest.crossAxisAlignment_test()")
        //val crossAxisAlignmentArr = CrossAxisAlignment.entries.toTypedArray()
        val crossAxisAlignmentArr = arrayOf(CrossAxisAlignment.START)
        crossAxisAlignmentArr.forEachIndexed { index, crossAxisAlignment ->
            val name = "widget/column/crossAxis$index"
            val description = "Column($crossAxisAlignment)"
            println("\n\ndraw: $name\n$description")
            drawWidget(imagePathWithoutSuffix = name, debugInfo = description) {
                if (crossAxisAlignment == CrossAxisAlignment.STRETCH) {
                    LimitedBox(
                        maxWidth = 1000f,
                        maxHeight = 1000f,
                    ) {
                        Column(
                            crossAxisAlignment = crossAxisAlignment
                        ) {
                            Container(width = 100f, height = 100f, color = Color.RED)
                            Container(width = 300f, height = 300f, color = Color.GREEN)
                            Container(width = 200f, height = 200f, color = Color.BLUE)
                        }
                    }
                } else {
                    Column(
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
}