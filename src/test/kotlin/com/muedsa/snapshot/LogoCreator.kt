package com.muedsa.snapshot

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.widget.*
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Path
import org.jetbrains.skia.Rect
import kotlin.test.Test

class LogoCreator {

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun logo() {
        val radius = 200f
        val size = radius * 2
        val delta = radius / 5f
        val wholeSize = size + delta
        drawWidget("../logo") {
            Padding(
                padding = EdgeInsets.all(20f)
            ) {
                Row {
                    arrayOf(
                        Stack {
                            arrayOf(
                                Container(
                                    width = wholeSize,
                                    height = wholeSize,
                                    alignment = BoxAlignment.TOP_LEFT
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
                                            width = size,
                                            height = size,
                                            color = 0xFF_FF_D1_33.toInt()
                                        )
                                    }
                                },
                                Container(
                                    width = wholeSize,
                                    height = wholeSize,
                                    alignment = BoxAlignment.BOTTOM_RIGHT
                                ) {
                                    ClipPath(
                                        clipper = {
                                            Path().apply {
                                                arcTo(
                                                    oval = Rect.Companion.makeWH(it.width, it.height),
                                                    startAngle = 45f + 180f,
                                                    sweepAngle = 180f,
                                                    forceMoveTo = true
                                                )
                                            }
                                        }
                                    ) {
                                        Container(
                                            width = size,
                                            height = size,
                                            color = 0xFF_FF_57_33.toInt()
                                        )
                                    }
                                },
                            )
                        },
                        SimpleText(
                            content = "Snapshot",
                            color = 0xFF_A6_A6_A6.toInt(),
                            fontSize = size * 0.8f,
                            fontStyle = FontStyle.BOLD
                        )
                    )
                }
            }
        }
    }
}