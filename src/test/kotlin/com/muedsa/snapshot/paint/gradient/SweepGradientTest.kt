package com.muedsa.snapshot.paint.gradient

import com.muedsa.geometry.MATH_PI
import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.drawPainter
import org.jetbrains.skia.Paint
import kotlin.test.Test

class SweepGradientTest {

    @Test
    fun sweep_test() {
        val size = Size(600f, 600f)
        drawPainter("gradient/sweep", size = size) {
            val gradient = SweepGradient(
                colors = intArrayOf(
                    0xFF4285F4.toInt(),
                    0xFF34A853.toInt(),
                    0xFFFBBC05.toInt(),
                    0xFFEA4335.toInt(),
                    0xFF4285F4.toInt(),
                )
            )
            val rect = Offset.ZERO combine size
            val shader = gradient.createShader(rect)
            it.drawRect(rect, Paint().apply { this@apply.shader = shader })
        }
    }

    @Test
    fun sweep_stops_test() {
        val size = Size(600f, 600f)
        drawPainter("gradient/sweep_stops", size = size) {
            val gradient = SweepGradient(
                colors = intArrayOf(
                    0xFF4285F4.toInt(),
                    0xFF34A853.toInt(),
                    0xFFFBBC05.toInt(),
                    0xFFEA4335.toInt(),
                    0xFF4285F4.toInt(),
                ),
                stops = floatArrayOf(0f, 0.1f, 0.5f, 0.9f, 1f)
            )
            val rect = Offset.ZERO combine size
            val shader = gradient.createShader(rect)
            it.drawRect(rect, Paint().apply { this@apply.shader = shader })
        }
    }

    @Test
    fun sweep_stops_transform_test() {
        val size = Size(600f, 600f)
        drawPainter("gradient/sweep_stops_transform", size = size) {
            val gradient = SweepGradient(
                colors = intArrayOf(
                    0xFF4285F4.toInt(),
                    0xFF34A853.toInt(),
                    0xFFFBBC05.toInt(),
                    0xFFEA4335.toInt(),
                    0xFF4285F4.toInt(),
                ),
                transform = GradientRotation(MATH_PI / 4)
            )
            val rect = Offset.ZERO combine size
            val shader = gradient.createShader(rect)
            it.drawRect(rect, Paint().apply { this@apply.shader = shader })
        }
    }

}