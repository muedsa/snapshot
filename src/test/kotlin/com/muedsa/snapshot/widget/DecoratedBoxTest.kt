package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.drawWidget
import com.muedsa.snapshot.material.ELEVATION_MAP
import com.muedsa.snapshot.paint.decoration.BoxDecoration
import org.jetbrains.skia.Color
import kotlin.test.Test

class DecoratedBoxTest {

    @Test
    fun boxShadow_test() {
        println("\n\n\nDecoratedBoxTest.boxShadow_test()")
        ELEVATION_MAP.forEach {
            val name = "widget/decorated_box/elevation_${it.key}"
            val description = "elevation_${it.key}\n${it.value.joinToString("\n")}"
            println("\n\ndraw: $name\n$description")
            drawWidget(name, debugInfo = description) {
                Container(
                    width = 500f,
                    height = 300f,
                    color = Color.WHITE,
                    alignment = BoxAlignment.CENTER
                ) {
                    Container(
                        width = 200f,
                        height = 100f,
                        color = Color.WHITE,
                        decoration = BoxDecoration(
                            boxShadow = it.value
                        )
                    )
                }
            }
        }
    }
}