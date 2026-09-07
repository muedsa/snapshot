package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.Path
import org.jetbrains.skia.PathBuilder


class RenderClipPath(
    clipper: ((Size) -> Path)? = null,
    clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
) : RenderCustomClip<Path>(
    clipper = clipper,
    clipBehavior = clipBehavior
) {
    override val defaultClip: Path
        get() = PathBuilder().addRect(Offset.ZERO combine definiteSize).detach()


    override fun paint(context: PaintingContext, offset: Offset) {
        if (child != null) {
            if (clipBehavior != ClipBehavior.NONE) {
                context.pushClipPath(
                    offset = offset,
                    bounds = Offset.ZERO combine definiteSize,
                    clipPath = getClip(),
                    clipBehavior = clipBehavior
                ) { c, o ->
                    super.paint(c, o)
                }
            } else {
                super.paint(context, offset)
            }
        }
    }

    override fun debugPaint(context: PaintingContext, offset: Offset) {
        if (child != null) {
            super.debugPaint(context, offset)
            if (clipBehavior != ClipBehavior.NONE) {
                context.canvas.drawPath(PathBuilder(getClip()).offset(offset.x, offset.y).detach(), debugPaint!!)
                debugText!!.paint(context.canvas, offset)
            }
        }
    }
}