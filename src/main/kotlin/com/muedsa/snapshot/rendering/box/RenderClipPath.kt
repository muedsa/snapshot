package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.Path


class RenderClipPath(
    clipper: ((Size) -> Path)? = null,
    clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
    child: RenderBox? = null,
) : RenderCustomClip<Path>(
    clipper = clipper,
    clipBehavior = clipBehavior,
    child = child
) {
    override val defaultClip: Path
        get() = Path().addRect(Offset.ZERO combine definiteSize)


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
                context.paintChild(child, offset)
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun debugPaint(context: PaintingContext, offset: Offset) {
        if (child != null) {
            super.debugPaint(context, offset)
            if (clipBehavior != ClipBehavior.NONE) {
                context.canvas.drawPath(Path().also { getClip().offset(offset.x, offset.y, it) }, debugPaint!!)
                debugText!!.paint(context.canvas, offset)
            }
        }
    }
}