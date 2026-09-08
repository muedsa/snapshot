package com.muedsa.snapshot.paint.gradient

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.goldenPixels
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import kotlin.test.Test

class GradientGoldenTest {

    @Test
    fun linear_two_stop_golden() {
        val size = Size(600f, 200f)
        goldenPixels("gradient/linear_two_stop", size.width, size.height) { canvas ->
            val gradient = LinearGradient(
                begin = BoxAlignment.TOP_LEFT,
                end = BoxAlignment.BOTTOM_RIGHT,
                colors = intArrayOf(0xFF00FF87.toInt(), 0xFF60EFFF.toInt()),
            )
            val rect = Offset.ZERO combine size
            canvas.drawRect(rect, Paint().apply { shader = gradient.createShader(rect) })
        }
    }

    @Test
    fun radial_two_stop_golden() {
        val size = Size(600f, 600f)
        goldenPixels("gradient/radial_two_stop", size.width, size.height) { canvas ->
            val gradient = RadialGradient(
                colors = intArrayOf(0xFFFCEF64.toInt(), 0xFFF44C7D.toInt()),
            )
            val rect = Offset.ZERO combine size
            canvas.drawRect(rect, Paint().apply { shader = gradient.createShader(rect) })
        }
    }

    @Test
    fun sweep_google_golden() {
        val size = Size(600f, 600f)
        goldenPixels("gradient/sweep_google", size.width, size.height) { canvas ->
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
            canvas.drawRect(rect, Paint().apply { shader = gradient.createShader(rect) })
        }
    }

    @Test
    fun linear_translucent_golden() {
        val size = Size(600f, 200f)
        goldenPixels("gradient/linear_translucent", size.width, size.height, background = Color.TRANSPARENT) { canvas ->
            val gradient = LinearGradient(
                begin = BoxAlignment.TOP_LEFT,
                end = BoxAlignment.BOTTOM_RIGHT,
                colors = intArrayOf(0xFF0000FF.toInt(), 0x8000FF00.toInt()),
            )
            val rect = Offset.ZERO combine size
            canvas.drawRect(rect, Paint().apply { shader = gradient.createShader(rect) })
        }
    }
}
