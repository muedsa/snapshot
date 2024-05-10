package com.muedsa.snapshot

import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.widget.*
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Path
import org.jetbrains.skia.Rect
import kotlin.test.Test

class LogoCreator {

    @Test
    fun logo() {
        drawWidget("../../../../logo") {
            logoContent()
        }
    }

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun Widget.logoContent() {
            val radius = 200f
            val size = radius * 2
            val delta = radius / 5f
            val wholeSize = size + delta
            Padding(
                padding = EdgeInsets.all(20f)
            ) {
                Row {
                    SizedBox(
                        width = wholeSize,
                        height = wholeSize
                    ) {
                        Stack {
                            Positioned(
                                top = 0f,
                                left = 0f
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
                                        color = 0xFF_FF_57_33.toInt()
                                    )
                                }
                            }
                            Positioned(
                                bottom = 0f,
                                right = 0f
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
                                        color = 0xFF_FF_D1_33.toInt()
                                    )
                                }
                            }
                        }
                    }
                    SimpleText(
                        text = "Snapshot",
                        color = 0xFF_A6_A6_A6.toInt(),
                        fontSize = size * 0.8f,
                        fontStyle = FontStyle.BOLD
                    )
                }
            }
        }
    }

}