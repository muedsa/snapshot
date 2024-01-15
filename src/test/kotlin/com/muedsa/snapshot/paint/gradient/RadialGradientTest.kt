package com.muedsa.snapshot.paint.gradient

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.drawPainter
import org.jetbrains.skia.Paint
import kotlin.test.Test

class RadialGradientTest {

    @Test
    fun radial_test() {
        val size = Size(600f, 600f)
        drawPainter("gradient/radial", size = size) {
            val gradient = RadialGradient(
                colors = intArrayOf(
                    0XFF_FC_EF_64.toInt(),
                    0XFF_F4_4C_7D.toInt(),
                )
            )
            val rect = Offset.ZERO combine size
            val shader = gradient.createShader(rect)
            it.drawRect(rect, Paint().apply { this@apply.shader = shader })
        }
    }

    @Test
    fun radial_focal_test() {
        val size = Size(600f, 600f)
        drawPainter("gradient/radial_focal_bottom_center", size = size) {
            val gradient = RadialGradient(
                colors = intArrayOf(
                    0XFF_FC_EF_64.toInt(),
                    0XFF_F4_4C_7D.toInt(),
                ),
                focal = BoxAlignment.BOTTOM_CENTER
            )
            val rect = Offset.ZERO combine size
            val shader = gradient.createShader(rect)
            it.drawRect(rect, Paint().apply { this@apply.shader = shader })
        }
    }
}