package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Offset
import com.muedsa.geometry.Radius
import com.muedsa.geometry.Size
import com.muedsa.snapshot.drawWidget
import com.muedsa.snapshot.paint.decoration.BorderRadius
import org.jetbrains.skia.Color
import org.jetbrains.skia.Path
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Test

class ClipTest {

    @Test
    fun clipRect_test() {
        println("\n\n\nClipTest.clipRect_test()")
        val name = "widget/clip/clip_rect"
        val description = "ClipRect"
        println("\n\ndraw: $name\n$description")
        drawWidget(imagePathWithoutSuffix = name, debugInfo = description, drawDebug = true) {
            Container(
                width = 300f,
                height = 300f,
                alignment = BoxAlignment.CENTER
            ) {
                ClipRect(
                    clipper = {
                        val clipSize: Size = it / 2f
                        BoxAlignment.CENTER.alongOffset(it - clipSize) combine clipSize
                    }
                ) {
                    Container(
                        width = 200f,
                        height = 200f
                    )
                }
            }


        }

    }

    @Test
    fun clipRRect_borderRadius_test() {
        println("\n\n\nClipTest.clipRRect_borderRadius_test()")
        val name = "widget/clip/clip_rrect_br"
        val description = "ClipRRect(borderRadius)"
        println("\n\ndraw: $name\n$description")
        drawWidget(imagePathWithoutSuffix = name, debugInfo = description, drawDebug = true) {
            Container(
                width = 300f,
                height = 300f,
                alignment = BoxAlignment.CENTER
            ) {
                ClipRRect(
                    borderRadius = BorderRadius.all(Radius.circular(50f))
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

    @Test
    fun clipRRect_clipper_test() {
        println("\n\n\nClipTest.clipRRect_borderRadius_test()")
        val name = "widget/clip/clip_rrect_clipper"
        val description = "ClipRRect(borderRadius)"
        println("\n\ndraw: $name\n$description")
        drawWidget(imagePathWithoutSuffix = name, debugInfo = description, drawDebug = true) {
            Container(
                width = 300f,
                height = 300f,
                alignment = BoxAlignment.CENTER
            ) {
                ClipRRect(
                    clipper = {
                        BorderRadius.all(Radius.circular(50f)).toRRect(Offset.ZERO combine it)
                    }
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


    @Test
    fun clipOval_test() {
        println("\n\n\nClipTest.clipOval_test()")
        val name = "widget/clip/clip_oval"
        val description = "ClipOval"
        println("\n\ndraw: $name\n$description")
        drawWidget(imagePathWithoutSuffix = name, debugInfo = description, drawDebug = true) {
            Container(
                width = 300f,
                height = 300f,
                alignment = BoxAlignment.CENTER
            ) {
                ClipOval(
                    clipper = {
                        Offset.ZERO combine it
                    }
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

    @Test
    fun clipPath_test() {
        println("\n\n\nClipTest.clipPath_test()")
        val name = "widget/clip/clip_path"
        val description = "ClipPath"
        println("\n\ndraw: $name\n$description")
        drawWidget(imagePathWithoutSuffix = name, debugInfo = description, drawDebug = true) {
            Container(
                width = 300f,
                height = 300f,
                alignment = BoxAlignment.CENTER
            ) {
                ClipPath(
                    clipper = {
                        val r: Float = it.width / 2f
                        val c: Float = it.height / 2f
                        Path().apply {
                            moveTo(x = c + r, y = c)
                            for (i in 1..7) {
                                val a: Float = 2.6927936f * i
                                lineTo(c + r * cos(a), c + r * sin(a))
                            }
                        }
                    }
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