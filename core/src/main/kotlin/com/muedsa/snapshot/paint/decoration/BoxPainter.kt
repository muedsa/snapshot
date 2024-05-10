package com.muedsa.snapshot.paint.decoration

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import org.jetbrains.skia.Canvas

abstract class BoxPainter {
    abstract fun paint(canvas: Canvas, offset: Offset, size: Size)
}