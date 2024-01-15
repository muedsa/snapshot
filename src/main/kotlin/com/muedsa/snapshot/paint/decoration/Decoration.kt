package com.muedsa.snapshot.paint.decoration

import com.muedsa.geometry.EdgeInsets
import org.jetbrains.skia.Path
import org.jetbrains.skia.Rect

abstract class Decoration {

    open val padding: EdgeInsets = EdgeInsets.ZERO

    open val isComplex: Boolean = false

    abstract fun createBoxPainter(): BoxPainter

    open fun getClipPath(rect: Rect): Path {
        throw UnsupportedOperationException("$this does not expect to be used for clipping.")
    }
}