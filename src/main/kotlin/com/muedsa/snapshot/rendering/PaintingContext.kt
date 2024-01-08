package com.muedsa.snapshot.rendering

import com.muedsa.geometry.Matrix44CMO
import com.muedsa.geometry.Offset
import com.muedsa.geometry.shift
import com.muedsa.snapshot.rendering.box.RenderBox
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Path
import org.jetbrains.skia.RRect
import org.jetbrains.skia.Rect

class PaintingContext(
    override val canvas: Canvas,
    var debug: Boolean = false,
) : ClipContext() {

    fun paintChild(child: RenderBox, offset: Offset) {
        child.paint(this, offset)
        if (debug) {
            child.debugPaint(this, offset)
        }
    }

    fun doClipPath(
        offset: Offset,
        bounds: Rect,
        clipPath: Path,
        clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
        painter: (PaintingContext, Offset) -> Unit,
    ) {
        if (clipBehavior == ClipBehavior.NONE) {
            painter(this, offset)
            return
        }
        val offsetBounds: Rect = bounds.shift(offset)
        val offsetClipPath: Path = Path().also {
            clipPath.offset(offset.x, offset.y, it)
        }
        clipPathAndPaint(offsetClipPath, clipBehavior, offsetBounds) {
            painter(this, offset)
        }
    }

    fun doClipRect(
        offset: Offset,
        clipRect: Rect,
        clipBehavior: ClipBehavior = ClipBehavior.HARD_EDGE,
        painter: (PaintingContext, Offset) -> Unit,
    ) {
        if (clipBehavior == ClipBehavior.NONE) {
            painter(this, offset)
            return
        }
        val offsetClipRect: Rect = clipRect.shift(offset)
        clipRectAndPaint(offsetClipRect, clipBehavior, offsetClipRect) {
            painter(this, offset)
        }
    }

    fun doClipRRect(
        offset: Offset,
        bounds: Rect,
        clipRRect: RRect,
        clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
        painter: (PaintingContext, Offset) -> Unit,
    ) {
        if (clipBehavior == ClipBehavior.NONE) {
            painter(this, offset)
            return
        }
        val offsetBounds: Rect = bounds.shift(offset)
        val offsetClipRRect: RRect = clipRRect.shift(offset)
        clipRRectAndPaint(offsetClipRRect, clipBehavior, offsetBounds) {
            painter(this, offset)
        }
    }

    fun doTransform(offset: Offset, transform: Matrix44CMO, painter: (PaintingContext, Offset) -> Unit) {
        val effectiveTransform: Matrix44CMO = Matrix44CMO.translationValues(
            x = offset.x,
            y = offset.y,
            z = 0f
        ).apply {
            multiply(transform)
            translate(-offset.x, -offset.y)
        }
        canvas.save()
        // skia的Matrix44为行顺序
        canvas.concat(effectiveTransform.toRMO())
        painter(this, offset);
        canvas.restore()
    }
}