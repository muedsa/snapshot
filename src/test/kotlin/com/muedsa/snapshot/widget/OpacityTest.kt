package com.muedsa.snapshot.widget


import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.drawWidget
import org.jetbrains.skia.Color
import kotlin.test.Test

class OpacityTest {

    @Test
    fun opacity_test() {
        println("\n\n\nOpacity.normal_value_test()")
        with_value_test(0.5f)

        println("\n\n\nOpacity.zero_value_test()")
        with_value_test(0f)
    }

    private fun with_value_test(opacity: Float) {
        val name = "widget/opacity/opacity_%.2f".format(opacity)
        val description = "Opacity($opacity)"
        println("\n\ndraw: $name\n$description")
        drawWidget(imagePathWithoutSuffix = name, debugInfo = description, drawDebug = true) {
            Container(
                alignment = BoxAlignment.CENTER,
                width = 300f,
                height = 300f,
                color = Color.YELLOW,
            ) {
                Opacity(
                    opacity = opacity
                ) {
                    Container(
                        width = 200f,
                        height = 200f,
                        color = Color.GREEN
                    )
                }
            }
        }
    }
}