package com.muedsa.snapshot.paint.gradient

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.drawPainter
import org.jetbrains.skia.Paint
import kotlin.test.Test

class LinearGradientTest {

    @Test
    fun linear_test() {
        val size = Size(600f, 200f)
        drawPainter("paint/gradient/linear", size = size) {
            val gradient = LinearGradient(
                begin = BoxAlignment.TOP_LEFT,
                end = BoxAlignment.BOTTOM_RIGHT,
                colors = intArrayOf(
                    0XFF_00_FF_87.toInt(),
                    0XFF_60_EF_FF.toInt(),
                )
            )
            val rect = Offset.ZERO combine size
            val shader = gradient.createShader(rect)
            it.drawRect(rect, Paint().apply { this@apply.shader = shader })
        }
    }
}