package com.muedsa.snapshot.paint.decoration

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.annotation.MustCallSuper
import org.jetbrains.skia.Canvas

abstract class BoxPainter {
    abstract fun paint(canvas: Canvas, offset: Offset, size: Size)

    @MustCallSuper
    fun dispose() { }
}