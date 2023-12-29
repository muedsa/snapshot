package com.muedsa.snapshot.paint

import com.muedsa.geometry.EdgeInsets
import org.jetbrains.skia.Path
import org.jetbrains.skia.Rect
import java.util.*

abstract class Decoration {

    open val padding: EdgeInsets = EdgeInsets.ZERO

    open val isComplex: Boolean = false

    protected open fun lerpFrom(a: Decoration?, t: Float): Decoration? = null

    protected open fun lerpTo(b: Decoration?, t: Float): Decoration? = null

    abstract fun createBoxPainter(): BoxPainter

    open fun getClipPath(rect: Rect): Path {
        throw UnsupportedOperationException("$this does not expect to be used for clipping.")
    }

    companion object {
        @JvmStatic
        fun lerp(a: Decoration?, b: Decoration?, t: Float): Decoration? {
            if (Objects.equals(a, b)) {
                return a
            }
            if (a == null) {
                return b!!.lerpFrom(null, t) ?: b
            }
            if (b == null) {
                return a.lerpTo(null, t) ?: a
            }
            if (t == 0f) {
                return a
            }
            if (t == 1f) {
                return b
            }
            return b.lerpFrom(a, t)
                ?: a.lerpTo(b, t)
                ?: if(t < 0.5) (a.lerpTo(null, t * 2f) ?: a) else (b.lerpFrom(null, (t - 0.5f) * 2f) ?: b)
        }
    }
}