package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.drawWidget
import com.muedsa.snapshot.paint.decoration.BoxDecoration
import com.muedsa.snapshot.paint.decoration.DecorationImage
import com.muedsa.snapshot.paint.text.TextStyle
import com.muedsa.snapshot.tools.NetworkImageCacheManager
import com.muedsa.snapshot.tools.SimpleNoLimitedNetworkImageCacheTest
import com.muedsa.snapshot.widget.text.Text
import org.jetbrains.skia.*
import kotlin.test.Test

class BackdropFilterTest {

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
                        ClipRect {
                            BackdropFilter(
                                imageFilter = ImageFilter.makeBlur(25f, 25f, FilterTileMode.CLAMP)
                            ) {
                                Container(
                                    width = 128f,
                                    height = 128f,
                                    padding = EdgeInsets.all(10f),
                                    alignment = BoxAlignment.CENTER
                                ) {
                                    Text(
                                        text = "233".repeat(100),
                                        style = TextStyle(color = Color.WHITE),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun blur_2_test() {
        println("\n\n\nBackdropFilterTest.blur_2_test()")
        val name = "widget/backdrop_filter/blur_2"
        val description = "BackdropFilter(blur)"
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
                        BackdropFilter(
                            imageFilter = ImageFilter.makeBlur(25f, 25f, FilterTileMode.CLAMP)
                        ) {
                            Container(
                                width = 128f,
                                height = 128f,
                                padding = EdgeInsets.all(10f),
                                alignment = BoxAlignment.CENTER
                            ) {
                                Text(
                                    text = "233".repeat(100),
                                    style = TextStyle(
                                        color = Color.WHITE
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
    fun blur_3_test() {
        println("\n\n\nBackdropFilterTest.blur_3_test()")
        val name = "widget/backdrop_filter/blur_3"
        val description = "BackdropFilter(blur)"
        println("\n\ndraw: $name\n$description")
        drawWidget(imagePathWithoutSuffix = name, debugInfo = description, drawDebug = false) {
            SizedBox(
                width = 500f,
                height = 500f,
            ) {
                ClipPath(
                    clipper = {
                        Path().apply {
                            arcTo(
                                oval = Rect.Companion.makeWH(it.width, it.height),
                                startAngle = 45f,
                                sweepAngle = 180f,
                                forceMoveTo = true
                            )
                        }
                    }
                ) {
                    Container(
                        width = 500f,
                        height = 500f,
                        decoration = BoxDecoration(
                            image = DecorationImage(
                                image = NetworkImageCacheManager.defaultCache[SimpleNoLimitedNetworkImageCacheTest.TEST_IMAGE_URL_2]
                            )
                        ),
                        alignment = BoxAlignment.CENTER
                    ) {
                        ClipPath {
                            BackdropFilter(
                                imageFilter = ImageFilter.makeBlur(25f, 25f, FilterTileMode.CLAMP)
                            ) {
                                Container(
                                    width = 250f,
                                    height = 250f,
                                    padding = EdgeInsets.all(10f),
                                    alignment = BoxAlignment.CENTER
                                ) {
                                    Text(
                                        text = "233".repeat(1000),
                                        style = TextStyle(
                                            color = Color.WHITE
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
}