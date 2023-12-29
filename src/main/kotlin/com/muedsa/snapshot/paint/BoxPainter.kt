package com.muedsa.snapshot.paint

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.annotation.MustCallSuper
import org.jetbrains.skia.Canvas

abstract class BoxPainter {
    abstract fun paint(canvas: Canvas, offset: Offset)

    @MustCallSuper
    fun dispose() { }
}