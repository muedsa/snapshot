package com.muedsa.snapshot.widget

import com.muedsa.snapshot.drawWidget
import org.jetbrains.skia.Color
import kotlin.test.Test

class ColoredBoxTest {

    @Test
    fun color_test() {
        println("\n\n\nColoredBoxTest.color_test()")
        val colors = intArrayOf(
            Color.BLACK, Color.WHITE, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA
        )
        colors.forEach {
            val name = "widget/colored_box/color_$it"
            val description =
                "ColoredBox(\na=${Color.getA(it)}\nr=${Color.getR(it)}\ng=${Color.getG(it)}\nb=${Color.getB(it)}\n)"
            println("\n\ndraw: $name\n$description")
            drawWidget(imagePathWithoutSuffix = name, debugInfo = description, drawDebug = true) {
                SizedBox(width = 100f, height = 100f) {
                    ColoredBox(color = it)
                }
            }
        }
    }

}