package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.drawWidget
import org.jetbrains.skia.Color
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.ImageFilter
import kotlin.test.Test

class BackdropFilterTest {


    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun blur_test() {
        println("\n\n\nBackdropFilterTest.blur_test()")
        val name = "widget/backdrop_filter/blur"
        val description = "BackdropFilter(blur)"
        println("\n\ndraw: $name\n$description")
        drawWidget(imagePathWithoutSuffix = name, debugInfo = description, drawDebug = false) {
            SizedBox(
                width = 256f,
                height = 256f,
            ) {
                Stack {
                    arrayOf(
                        Positioned(
                            top = 0f,
                            left = 0f
                        ) {
                            Container(
                                width = 128f,
                                height = 128f,
                                color = Color.RED
                            )
                        },
                        Positioned(
                            top = 0f,
                            right = 0f
                        ) {
                            Container(
                                width = 128f,
                                height = 128f,
                                color = Color.BLUE
                            )
                        },
                        Positioned(
                            bottom = 0f,
                            left = 0f
                        ) {
                            Container(
                                width = 128f,
                                height = 128f,
                                color = Color.BLUE
                            )
                        },
                        Positioned(
                            bottom = 0f,
                            right = 0f
                        ) {
                            Container(
                                width = 128f,
                                height = 128f,
                                color = Color.RED
                            )
                        },
                        Positioned(
                            top = 64f,
                            right = 64f
                        ) {
                            BackdropFilter(
                                imageFilter = ImageFilter.makeBlur(25f, 25f, FilterTileMode.CLAMP)
                            ) {
                                Container(
                                    width = 128f,
                                    height = 128f,
                                    padding = EdgeInsets.all(10f),
                                    alignment = BoxAlignment.CENTER
                                ) {
                                    SimpleText(
                                        content = "233".repeat(100),
                                        color = Color.WHITE
                                    )
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}