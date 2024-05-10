package com.muedsa.snapshot.paint.decoration

import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Path
import org.jetbrains.skia.Rect

interface DecorationImagePainter {
    fun paint(canvas: Canvas, rect: Rect, clipPath: Path?, blend: Float = 1f, blendMode: BlendMode = BlendMode.SRC_OVER)
}