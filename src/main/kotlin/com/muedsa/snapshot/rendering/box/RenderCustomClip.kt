package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.paint.text.SimpleTextPainter
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.Direction

abstract class RenderCustomClip<T>(
    val clipper: ((Size) -> T)? = null,
    val clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
    child: RenderBox? = null,
) : RenderSingleChildBox(child = child) {

    abstract val defaultClip: T

    private var clip: T? = null

    fun getClip(): T {
        if (clip == null) {
            clip = clipper?.invoke(definiteSize) ?: defaultClip
        }
        return clip!!
    }

    protected var debugPaint: Paint? = null

    @OptIn(ExperimentalStdlibApi::class)
    protected var debugText: SimpleTextPainter? = null

    @OptIn(ExperimentalStdlibApi::class)
    override fun debugPaint(context: PaintingContext, offset: Offset) {
        super.debugPaint(context, offset)
        if (debugPaint == null) {
            debugPaint = Paint().apply {
                shader = Shader.makeLinearGradient(
                    x0 = 0f,
                    y0 = 0f,
                    x1 = 10f,
                    y1 = 10f,
                    colors = intArrayOf(0x00000000, 0xFFFF00FF.toInt(), 0xFFFF00FF.toInt(), 0x00000000),
                    positions = floatArrayOf(0.25f, 0.25f, 0.75f, 0.75f),
                    style = GradientStyle.DEFAULT.withTileMode(FilterTileMode.REPEAT)
                )
                strokeWidth = 2f
                mode = PaintMode.STROKE
            }
        }
        if (debugText == null) {
            debugText = SimpleTextPainter(
                text = "✂",
                color = 0xFFFF00FF.toInt(),
                fontSize = 14f,
                textDirection = Direction.RTL
            ).apply {
                layout()
            }
        }
    }
}