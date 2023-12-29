package com.muedsa.snapshot.paint

import com.muedsa.geometry.EdgeInsets
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Path
import org.jetbrains.skia.Rect

abstract class ShapeBorder {

    abstract val dimensions: EdgeInsets

    protected open fun add(other: ShapeBorder, reversed: Boolean = false): ShapeBorder? = null

    abstract fun scale(t: Float): ShapeBorder

    protected open fun lerpFrom(a: ShapeBorder?, t: Float): ShapeBorder? = if (a == null) scale(t) else null

    protected open fun lerpTo(b: ShapeBorder?, t: Float): ShapeBorder? = if (b == null) scale(1f - t) else null

    abstract fun getOuterPath(rect: Rect): Path

    abstract fun getInnerPath(rect: Rect): Path

    open fun paintInterior(canvas: Canvas, rect: Rect, paint: Paint) {

    }

    open val preferPaintInterior: Boolean = false

    abstract fun paint(canvas: Canvas, rect: Rect)

    companion object {
        @JvmStatic
        fun lerp(a: ShapeBorder?, b: ShapeBorder?, t: Float): ShapeBorder? {
            if (a == b) {
                return a
            }
            val result: ShapeBorder? = null
            if (b != null) {
                b.lerpFrom(a, t)
            }
            if (result == null && a != null) {
                a.lerpTo(b, t)
            }
            return result ?: if (t < 0.5) a else b
        }
    }
}