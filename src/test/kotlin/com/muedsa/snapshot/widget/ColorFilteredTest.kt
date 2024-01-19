package com.muedsa.snapshot.widget

import com.muedsa.snapshot.drawWidget
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Color
import org.jetbrains.skia.ColorFilter
import kotlin.test.Test

class ColorFilteredTest {


    @Test
    fun red_modulate_test() {
        println("\n\n\nColorFilteredTest.red_modulate_test()")
        val name = "widget/color_filtered/red_modulate"
        val description = "ColorFiltered(red modulate)"
        println("\n\ndraw: $name\n$description")
        drawWidget(imagePathWithoutSuffix = name, debugInfo = description, drawDebug = false) {
            ColorFiltered(
                colorFilter = ColorFilter.makeBlend(Color.RED, BlendMode.MODULATE)
            ) {
                CachedNetworkImage("https://flutter.github.io/assets-for-api-docs/assets/widgets/owl-2.jpg")
            }
        }
    }

    @Test
    fun gray_saturation_test() {
        println("\n\n\nColorFilteredTest.gray_saturation()")
        val name = "widget/color_filtered/gray_saturation"
        val description = "ColorFiltered(gray saturation)"
        println("\n\ndraw: $name\n$description")
        drawWidget(imagePathWithoutSuffix = name, debugInfo = description, drawDebug = false) {
            ColorFiltered(
                colorFilter = ColorFilter.makeBlend(0xFF9E9E9E.toInt(), BlendMode.SATURATION)
            ) {
                CachedNetworkImage("https://flutter.github.io/assets-for-api-docs/assets/widgets/owl.jpg")
            }
        }
    }
}