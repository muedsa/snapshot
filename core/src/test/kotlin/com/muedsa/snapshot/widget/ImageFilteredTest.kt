package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.drawWidget
import com.muedsa.snapshot.paint.text.TextStyle
import com.muedsa.snapshot.widget.text.Text
import org.jetbrains.skia.Color
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.ImageFilter
import kotlin.test.Test

class ImageFilteredTest {

    @Test
    fun blur_test() {
        println("\n\n\nImageFilteredTest.blur_test()")
        val name = "widget/image_filtered/blur"
        val description = "ImageFiltered(blur)"
        println("\n\ndraw: $name\n$description")
        drawWidget(imagePathWithoutSuffix = name, debugInfo = description, drawDebug = false) {
            SizedBox(
                width = 256f,
                height = 256f,
            ) {
                Stack {
                    Positioned(
                        top = 0f,
                        left = 0f
                    ) {
                        Container(
                            width = 128f,
                            height = 128f,
                            color = Color.RED
                        )
                    }
                    Positioned(
                        top = 0f,
                        right = 0f
                    ) {
                        Container(
                            width = 128f,
                            height = 128f,
                            color = Color.BLUE
                        )
                    }
                    Positioned(
                        bottom = 0f,
                        left = 0f
                    ) {
                        Container(
                            width = 128f,
                            height = 128f,
                            color = Color.BLUE
                        )
                    }
                    Positioned(
                        bottom = 0f,
                        right = 0f
                    ) {
                        Container(
                            width = 128f,
                            height = 128f,
                            color = Color.RED
                        )
                    }
                    Positioned(
                        top = 64f,
                        right = 64f
                    ) {
                        ImageFiltered(
                            imageFilter = ImageFilter.makeBlur(2f, 2f, FilterTileMode.CLAMP)
                        ) {
                            Container(
                                width = 128f,
                                height = 128f,
                                padding = EdgeInsets.all(10f),
                                alignment = BoxAlignment.CENTER,
                                // color = Color.GREEN
                            ) {
                                Text(
                                    text = "233".repeat(5),
                                    style = TextStyle(
                                        color = Color.WHITE,
                                        fontSize = 30f

                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun blur_clip_test() {
        println("\n\n\nImageFilteredTest.blur_clip()")
        val name = "widget/image_filtered/blur_clip"
        val description = "ImageFiltered(blur)"
        println("\n\ndraw: $name\n$description")
        drawWidget(imagePathWithoutSuffix = name, debugInfo = description, drawDebug = false) {
            SizedBox(
                width = 256f,
                height = 256f,
            ) {
                Stack {
                    Positioned(
                        top = 0f,
                        left = 0f
                    ) {
                        Container(
                            width = 128f,
                            height = 128f,
                            color = Color.RED
                        )
                    }
                    Positioned(
                        top = 0f,
                        right = 0f
                    ) {
                        Container(
                            width = 128f,
                            height = 128f,
                            color = Color.BLUE
                        )
                    }
                    Positioned(
                        bottom = 0f,
                        left = 0f
                    ) {
                        Container(
                            width = 128f,
                            height = 128f,
                            color = Color.BLUE
                        )
                    }
                    Positioned(
                        bottom = 0f,
                        right = 0f
                    ) {
                        Container(
                            width = 128f,
                            height = 128f,
                            color = Color.RED
                        )
                    }
                    Positioned(
                        top = 64f,
                        right = 64f
                    ) {
                        ImageFiltered(
                            imageFilter = ImageFilter.makeBlur(2f, 2f, FilterTileMode.CLAMP)
                        ) {
                            Container(
                                width = 128f,
                                height = 128f,
                                padding = EdgeInsets.all(10f),
                                alignment = BoxAlignment.CENTER,
                                // color = Color.GREEN
                            ) {
                                Text(
                                    text = "233".repeat(100), // text will clip
                                    style = TextStyle(
                                        color = Color.WHITE,
                                        fontSize = 30f
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}