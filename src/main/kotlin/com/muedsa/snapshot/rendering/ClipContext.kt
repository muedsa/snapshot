package com.muedsa.snapshot.rendering

import org.jetbrains.skia.*


abstract class ClipContext {

    abstract val canvas: Canvas

    private fun clipAndPaint(
        clipBehavior: ClipBehavior,
        bounds: Rect,
        painter: () -> Unit,
        canvasClipCall: (Boolean) -> Unit
    ) {
        canvas.save()
        when (clipBehavior) {
            ClipBehavior.NONE -> Unit
            ClipBehavior.HARD_EDGE -> {
                canvasClipCall(false)
            }

            ClipBehavior.ANTI_ALIAS -> {
                canvasClipCall(true)
            }

            ClipBehavior.ANTI_ALIAS_WITH_SAVE_LAYER -> {
                canvasClipCall(true)
                canvas.saveLayer(bounds, Paint())
            }
        }
        painter()
        if (clipBehavior == ClipBehavior.ANTI_ALIAS_WITH_SAVE_LAYER) {
            canvas.restore();
        }
        canvas.restore();
    }

    protected fun clipPathAndPaint(path: Path, clipBehavior: ClipBehavior, bounds: Rect, painter: () -> Unit) {
        clipAndPaint(clipBehavior, bounds, painter) { canvas.clipPath(path, it) }
    }

    protected fun clipRRectAndPaint(rrect: RRect, clipBehavior: ClipBehavior, bounds: Rect, painter: () -> Unit) {
        clipAndPaint(clipBehavior, bounds, painter) { canvas.clipRRect(rrect, it) }
    }

    protected fun clipRectAndPaint(rect: Rect, clipBehavior: ClipBehavior, bounds: Rect, painter: () -> Unit) {
        clipAndPaint(clipBehavior, bounds, painter) { canvas.clipRect(rect, it) }
    }

}