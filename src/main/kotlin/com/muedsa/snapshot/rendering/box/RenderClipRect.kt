package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.geometry.shift
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.Rect

class RenderClipRect(
    clipper: ((Size) -> Rect)? = null,
    clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
) : RenderCustomClip<Rect>(
    clipper = clipper,
    clipBehavior = clipBehavior
) {

    override val defaultClip: Rect
        get() = Offset.ZERO combine definiteSize


    override fun paint(context: PaintingContext, offset: Offset) {
        if (child != null) {
            if (clipBehavior != ClipBehavior.NONE) {
                context.pushClipRect(
                    offset = offset,
                    clipRect = getClip(),
                    clipBehavior = clipBehavior
                ) { c, o ->
                    super.paint(c, o)
                }
            } else {
                super.paint(context, offset)
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun debugPaint(context: PaintingContext, offset: Offset) {
        if (child != null) {
            super.debugPaint(context, offset)
            if (clipBehavior != ClipBehavior.NONE) {
                context.canvas.drawRect(getClip().shift(offset), debugPaint!!)
                debugText!!.paint(context.canvas, offset + Offset(getClip().width / 8f, -debugText!!.fontSize * 1.1f))
            }
        }
    }
}