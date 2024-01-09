package com.muedsa.snapshot

import com.muedsa.geometry.Alignment
import com.muedsa.snapshot.paint.decoration.BoxDecoration
import com.muedsa.snapshot.paint.decoration.BoxShape
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.widget.ClipPath
import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.Stack
import org.jetbrains.skia.Path
import kotlin.test.Test

class LogoCreator {

    @Test
    fun logo() {
        val radius = 200f
        val size = radius * 2
        val delta = radius / 3f
        val alignOffset = delta / radius
        val wholeSize = (radius + delta) * 2
        val leftAlignment = Alignment(-alignOffset, -alignOffset)
        val rightAlignment = Alignment(alignOffset, alignOffset)
        drawWidget("../logo") {
            Stack {
                arrayOf(
                    Container(
                        width = wholeSize,
                        height = wholeSize,
                        alignment = leftAlignment
                    ) {
                        ClipPath(
                            clipper = {
                                Path().apply {
                                    moveTo(0f, 0f)
                                    lineTo(0f, it.x)
                                    lineTo(it.x, it.y)
                                }
                            }
                        ) {
                            Container(
                                width = size,
                                height = size,
                                color = 0xFF_FF_D1_33.toInt(),
                                clipBehavior = ClipBehavior.ANTI_ALIAS,
                                decoration = BoxDecoration(
                                    shape = BoxShape.CIRCLE,
                                )
                            )
                        }
                    },
                    Container(
                        width = wholeSize,
                        height = wholeSize,
                        alignment = rightAlignment
                    ) {
                        ClipPath(
                            clipper = {
                                Path().apply {
                                    moveTo(0f, 0f)
                                    lineTo(it.x, 0f)
                                    lineTo(it.x, it.y)
                                }
                            }
                        ) {
                            Container(
                                width = size,
                                height = size,
                                color = 0xFF_FF_57_33.toInt(),
                                clipBehavior = ClipBehavior.ANTI_ALIAS,
                                decoration = BoxDecoration(
                                    shape = BoxShape.CIRCLE
                                ),
                            )
                        }
                    },
                )
            }
        }
    }
}